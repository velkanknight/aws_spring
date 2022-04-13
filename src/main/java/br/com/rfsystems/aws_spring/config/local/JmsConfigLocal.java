package br.com.rfsystems.aws_spring.config.local;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.Session;

@Configuration
@EnableJms
@Profile("local")
public class JmsConfigLocal {

    @Value("${aws.region}") private String awsRegion;

    private SQSConnectionFactory sqsConnectionFactory;

    //ESSA CLASSE CRIA UM CLIENT E APONTA PARA O ENDEREÇO LOCAL, SERVE APENAS PARA TESTE
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(){
        sqsConnectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClient.builder()
                        .withEndpointConfiguration(
                                new AwsClientBuilder.
                                        EndpointConfiguration("http://localhost:4566",
                                        Regions.US_EAST_1.getName()))
                        .withCredentials(new DefaultAWSCredentialsProviderChain())
                        .build());

        //CRIANDO A CONFIGURAÇÃO DO JMS PARA ACESSAR A FILA
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(sqsConnectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        //DEFININDO O NUMERO DE THREADS QUEREMOS TER POR FILA, 2 POR FILA
        factory.setConcurrency("2");
        //JMS RECONHECE QUANDO A MENSAGEM FOI TRATADA
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return factory;

    }

}