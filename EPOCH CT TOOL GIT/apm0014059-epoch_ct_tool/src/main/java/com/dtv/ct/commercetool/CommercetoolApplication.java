package com.dtv.ct.commercetool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommercetoolApplication {

    public static void main(String[] args) {
       /* System.setProperty("datastax-java-driver.basic.request.timeout","60 seconds");
        System.setProperty("javax.net.ssl.trustStore", "C:\\Git\\epoch_ct_tool\\opt\\ajsc\\etc\\config\\keys\\cadi_truststore2020.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");*/
        SpringApplication.run(CommercetoolApplication.class, args);
    }

}
