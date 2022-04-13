package br.com.rfsystems.aws_spring.consumer;

import br.com.rfsystems.aws_spring.model.Invoice;
import br.com.rfsystems.aws_spring.model.SnsMessage;
import br.com.rfsystems.aws_spring.repository.InvoiceRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class S3InvoiceConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(S3InvoiceConsumer.class);
    private ObjectMapper objectMapper;
    private InvoiceRepository invoiceRepository;
    private AmazonS3 amazonS3;

    @Autowired
    public S3InvoiceConsumer(ObjectMapper objectMapper, InvoiceRepository invoiceRepository, AmazonS3 amazonS3) {
        this.objectMapper = objectMapper;
        this.invoiceRepository = invoiceRepository;
        this.amazonS3 = amazonS3;
    }

    @JmsListener(destination = "${aws.sqs.queue.invoice.events.name}")
    public void receiveS3Event(TextMessage textMessage) throws IOException, JMSException {

         SnsMessage snsMessage = objectMapper.readValue(textMessage.getText(), SnsMessage.class);

        S3EventNotification s3EventNotification = objectMapper.readValue(snsMessage.getMessage(), S3EventNotification.class);

        processInvoiceNotification(s3EventNotification);

    }

    //se ocorrer algum problema de parser ou qq outro, ele vai lançar uma exceção e caira no mecanismo de dlq
    private void processInvoiceNotification(S3EventNotification s3EventNotification) throws IOException {
        for (S3EventNotification.S3EventNotificationRecord s3EventNotificationRecord : s3EventNotification.getRecords()){
            S3EventNotification.S3Entity s3Entity = s3EventNotificationRecord.getS3();

            String bucketName = s3Entity.getBucket().getName();
            String key = s3Entity.getObject().getKey();

            String invoiceFile = downloadObject(bucketName, key);

            Invoice invoice = objectMapper.readValue(invoiceFile, Invoice.class);
            LOG.info("Invoice received {}", invoice.getInvoiceNumber());
            invoiceRepository.save(invoice);
            amazonS3.deleteObject(bucketName, key);

        }
    }
//baixando o arquivo e devolvendo em formato string
    private String downloadObject(String bucketName, String key) throws IOException {
        S3Object s3Object = amazonS3.getObject(bucketName, key);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader =  new BufferedReader( new InputStreamReader(s3Object.getObjectContent()));
        String content = null;
        while ((content = bufferedReader.readLine()) != null){
            stringBuilder.append(content);
        }
        return stringBuilder.toString();
    }

}
