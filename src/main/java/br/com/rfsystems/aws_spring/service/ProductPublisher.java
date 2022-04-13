package br.com.rfsystems.aws_spring.service;

import br.com.rfsystems.aws_spring.enums.EventType;
import br.com.rfsystems.aws_spring.model.Envelope;
import br.com.rfsystems.aws_spring.model.Product;
import br.com.rfsystems.aws_spring.model.ProductEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(ProductPublisher.class);
    private AmazonSNS snsClient;
    private Topic productEventsTopic;
    private ObjectMapper mapper;

    public ProductPublisher(AmazonSNS snsClient, @Qualifier("productEventsTopic")Topic productEventsTopic, ObjectMapper mapper){

        this.snsClient = snsClient;
        this.productEventsTopic = productEventsTopic;
        this.mapper = mapper;
    }

    public void publishProcuctEvent(Product product, EventType eventType, String username){

        ProductEvent productEvent = new ProductEvent();
        productEvent.setProductId(product.getId());
        productEvent.setCode(product.getCode());
        productEvent.setUserName(username);
        productEvent.setProductId(product.getId());
        Envelope envelope = new Envelope();
        envelope.setEventType(eventType);
        try {
            envelope.setData(mapper.writeValueAsString(productEvent));
            PublishResult publishResult = snsClient.publish(
                    productEventsTopic.getTopicArn(),
                    mapper.writeValueAsString(envelope));

            LOG.info("Product event sent - Event: {} - ProductId: {} - MessageId: {}"
            , envelope.getEventType(),
              productEvent.getProductId(),
              publishResult.getMessageId());

        } catch (JsonProcessingException e) {
            LOG.error("Falha para criar o objeto productvent message");
        }


    }

}
