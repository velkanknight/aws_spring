package br.com.rfsystems.aws_spring.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.Topic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

//AQUI FALAMOS PARA O SPRING Q ESSA CLASSE SO SERA EXECUTADA EM PERFIL DIFERENTE DO LOCAL
//ISSO FAZ COM QUE NAO CRIEMOS O TOPIC NEM O CLIENT, E PODEMOS USAR ASSIM O LOCALSTACK DA AWS PRA TESTE
@Profile("!local")
@Configuration
public class SnsConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.sns.topic.product.events.arn}")
    private String productEventsTopic;

    //CRIANDO CLIENT PARA ENVIAR MENSAGENS PARA O SNS DA AWS
    @Bean
    public AmazonSNS snsClient(){
        return AmazonSNSClientBuilder
                .standard()
                .withRegion(awsRegion)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    //CRIANDO O TOPICO
    @Bean(name="productEventsTopic")
    public Topic snsProductEventsTopic(){
        return new Topic().withTopicArn(productEventsTopic);
    }




}
