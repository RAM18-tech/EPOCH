package com.dtv.dcp.epoch.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

// import com.att.idp.epoch.model.batch.CouponKafkaMessage;
import com.dtv.dcp.epoch.model.customergraph.CustomerGraphPublisherMessage;

@Configuration
public class KafkaConfiguration {

    private static final String group = "catalog-consumer-group";
    private static final String groupCG = "customer-graph-group";
    
    
    @Value("${spring.kafka.cg.bootstrap-servers}")
    private String serversForCGTopic;
    @Value("${spring.kafka.cg.username}")
    private String usernameCG;
    @Value("${spring.kafka.cg.password}")
    private String passwordCG;
    @Value("${spring.kafka.cg.sasl.mechanism}")
    private String saslMechanismCG;
    @Value("${spring.kafka.cg.security.protocol}")
    private String securityProtocolCG;


    @Bean
    public ProducerFactory<String, CustomerGraphPublisherMessage> producerFactoryCG() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serversForCGTopic);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        String jaasTemplate = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, usernameCG, passwordCG);
        props.put("sasl.mechanism", saslMechanismCG);
        props.put("security.protocol", securityProtocolCG);
        props.put("sasl.jaas.config", jaasCfg);
        return new DefaultKafkaProducerFactory<>(props);
    }
    
    @Bean
    public KafkaTemplate<String, CustomerGraphPublisherMessage> kafkaTemplateCG() {
        return new KafkaTemplate<>(producerFactoryCG());
    }
}

