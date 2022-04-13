package br.com.rfsystems.aws_spring.config.local;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.s3.model.TopicConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class S3ConfigLocal {

    private static final String BUCKET_NAME = "pcs-invoice";

    private AmazonS3 amazonS3;

    //essa classe visa criar toda a stack local para o s3, precisamos criar o bucket, um topic
    //para qual o event de criacao de arquivos notificará e a fila que vai receber as notificaões do topic
    public S3ConfigLocal(){
        amazonS3 = getAmazonS3();
        createBucket();
        AmazonSNS snsClient = getAmazonSNS();
        String s3invoiceEbventsTopicArn = createTopic(snsClient);
        AmazonSQS sqsClient = getAmazonSQS();
        createQueue(snsClient, s3invoiceEbventsTopicArn, sqsClient);
        configureBucket(s3invoiceEbventsTopicArn);
    }

    private void configureBucket(String s3invoiceEbventsTopicArn) {
        //criando configuração dizendo que quando o evento createobject put for emitido, ns vamos publicar no topic s3InvoiceEventsTopic
        TopicConfiguration topicConfiguration = new TopicConfiguration();
        topicConfiguration.setTopicARN(s3invoiceEbventsTopicArn);
        topicConfiguration.addEvent(S3Event.ObjectCreatedByPut);
        //aqui passamos nossa configuração personalizada para o bucket local
        amazonS3.setBucketNotificationConfiguration(BUCKET_NAME,
                new BucketNotificationConfiguration().addConfiguration("putObject", topicConfiguration));
    }


    private void createQueue(AmazonSNS snsClient, String s3InvoiceEventsTopicArn, AmazonSQS sqsClient) {
        String s3InvoiceQueueUrl = sqsClient.createQueue(
                new CreateQueueRequest("s3-invoice-events")).getQueueUrl();

        Topics.subscribeQueue(snsClient, sqsClient, s3InvoiceEventsTopicArn, s3InvoiceQueueUrl);
    }


    private String createTopic(AmazonSNS snsClient) {
        CreateTopicRequest createTopicRequest = new CreateTopicRequest("s3-invoice-events");
        return snsClient.createTopic(createTopicRequest).getTopicArn();
    }

    private AmazonSQS getAmazonSQS() {
        return AmazonSQSClient.builder()
                .withEndpointConfiguration(new AwsClientBuilder
                        .EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName()))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    private AmazonSNS getAmazonSNS() {
        return AmazonSNSClient.builder()
                .withEndpointConfiguration(new AwsClientBuilder
                        .EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName()))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    private void createBucket() {
        this.amazonS3.createBucket(BUCKET_NAME);
    }

    private AmazonS3 getAmazonS3() {
        AWSCredentials credentials = new BasicAWSCredentials("test", "test");
        this.amazonS3 = AmazonS3Client.builder()
                .withEndpointConfiguration( new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", Regions.US_EAST_1.getName()))
                .enablePathStyleAccess()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        return this.amazonS3;
    }

    @Bean
    public AmazonS3 amazonS3Client(){
        return this.amazonS3;
    }

}
