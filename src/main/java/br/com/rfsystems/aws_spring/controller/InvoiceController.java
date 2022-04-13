package br.com.rfsystems.aws_spring.controller;

import br.com.rfsystems.aws_spring.model.Invoice;
import br.com.rfsystems.aws_spring.model.UrlResponse;
import br.com.rfsystems.aws_spring.repository.InvoiceRepository;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Value("${aws.s3.bucket.invoice.name}") private String bucketName;


    private AmazonS3 amazonS3;
    private InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceController(AmazonS3 amazonS3, InvoiceRepository invoiceRepository){
        this.amazonS3 = amazonS3;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> createInvoiceUrl(){
        UrlResponse urlResponse = new UrlResponse();
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(5));
        String processId = UUID.randomUUID().toString();

        //gerando uma url para acessar o file
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, processId)
                //definindo qual metodo usaremos para acessar
                .withMethod(HttpMethod.PUT)
                //quanto tempo a url sera valida
                .withExpiration(Date.from(expirationTime));
        urlResponse.setExpirationTime(expirationTime.getEpochSecond());
        urlResponse.setUrl(amazonS3.generatePresignedUrl(
                generatePresignedUrlRequest).toString());
        return new ResponseEntity<UrlResponse>(urlResponse, HttpStatus.OK);
    }

    @GetMapping
    public Iterable<Invoice> findAll(){
        return invoiceRepository.findAll();
    }

    @GetMapping(path = "/bycustomername")
    public Iterable<Invoice> findByCustomerName(@RequestParam String customerName){
        return invoiceRepository.findAllByCustomerName(customerName);
    }


}
