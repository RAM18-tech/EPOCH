package com.dtv.ct.commercetool.config;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.defaultconfig.ApiRootBuilder;
import com.commercetools.api.defaultconfig.ServiceRegion;
import com.commercetools.http.okhttp4.CtOkHttp4Client;
import io.vrap.rmf.base.client.VrapHttpClient;
import io.vrap.rmf.base.client.oauth2.ClientCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class ProjectRootConfiguration {

    // Staging1
    @Value("${STAGING1_CLIENTID}")
    private String staging1ClientId;
    @Value("${STAGING1_CLIENTSECRET}")
    private String staging1ClientSecret;
    @Value("${STAGING1_SCOPES}")
    private String staging1Scope;

    // Staging2
    @Value("${STAGING2_CLIENTID}")
    private String staging2ClientId;
    @Value("${STAGING2_CLIENTSECRET}")
    private String staging2ClientSecret;
    @Value("${STAGING2_SCOPES}")
    private String staging2Scope;

    // Dev
    @Value("${DEV_CLIENTID}")
    private String devClientId;
    @Value("${DEV_CLIENTSECRET}")
    private String devClientSecret;
    @Value("${DEV_SCOPES}")
    private String devScope;

    // Staging3
    @Value("${STAGING3_CLIENTID}")
    private String staging3ClientId;
    @Value("${STAGING3_CLIENTSECRET}")
    private String staging3ClientSecret;
    @Value("${STAGING3_SCOPES}")
    private String staging3Scope;

    // Production7
    @Value("${PRODUCTION7_CLIENTID}")
    private String production7ClientId;
    @Value("${PRODUCTION7_CLIENTSECRET}")
    private String production7ClientSecret;
    @Value("${PRODUCTION7_SCOPES}")
    private String production7Scope;

    // Production8
    @Value("${PRODUCTION8_CLIENTID}")
    private String production8ClientId;
    @Value("${PRODUCTION8_CLIENTSECRET}")
    private String production8ClientSecret;
    @Value("${PRODUCTION8_SCOPES}")
    private String production8Scope;

    private String proxy_att = "pxyapp.proxy.att.com";

    private int port = 8080;

    @Bean
    public ProjectApiRoot createApiClientDev() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_att, port));
        VrapHttpClient httpClient = new CtOkHttp4Client(builder -> builder.proxy(proxy));

        final ProjectApiRoot apiRoot = ApiRootBuilder.of(httpClient)
                .defaultClient(ClientCredentials.of()
                                .withClientId(devClientId)
                                .withClientSecret(devClientSecret)
                                .withScopes(devScope)
                                .build(),
                        ServiceRegion.AWS_US_EAST_2)
                .build("epoch-dev");

        return apiRoot;
    }

    @Bean
    public ProjectApiRoot createApiClient() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_att, port));
        VrapHttpClient httpClient = new CtOkHttp4Client(builder -> builder.proxy(proxy));

        final ProjectApiRoot apiRoot = ApiRootBuilder.of(httpClient)
                .defaultClient(ClientCredentials.of()
                                .withClientId(staging1ClientId)
                                .withClientSecret(staging1ClientSecret)
                                .withScopes(staging1Scope)
                                .build(),
                        ServiceRegion.AWS_US_EAST_2)
                .build("epoch-staging");

        return apiRoot;
    }

    @Bean
    public ProjectApiRoot createApiClientStaging2() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_att, port));
        VrapHttpClient httpClient = new CtOkHttp4Client(builder -> builder.proxy(proxy));

        final ProjectApiRoot apiRoot = ApiRootBuilder.of(httpClient)
                .defaultClient(ClientCredentials.of()
                                .withClientId(staging2ClientId)
                                .withClientSecret(staging2ClientSecret)
                                .withScopes(staging2Scope)
                                .build(),
                        ServiceRegion.AWS_US_EAST_2)
                .build("epoch-staging2");

        return apiRoot;
    }


    @Bean
    public ProjectApiRoot createApiClientStaging3() {

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_att, port));
            VrapHttpClient httpClient = new CtOkHttp4Client(builder -> builder.proxy(proxy));

            final ProjectApiRoot apiRoot = ApiRootBuilder.of(httpClient)
                    .defaultClient(ClientCredentials.of()
                                    .withClientId(staging3ClientId)
                                    .withClientSecret(staging3ClientSecret)
                                    .withScopes(staging3Scope)
                                    .build(),
                            ServiceRegion.AWS_US_EAST_2)
                    .build("epoch-staging3");

            return apiRoot;

    }


    @Bean
    public ProjectApiRoot createApiClientProduction7() {

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_att, port));
        VrapHttpClient httpClient = new CtOkHttp4Client(builder -> builder.proxy(proxy));

        final ProjectApiRoot apiRoot = ApiRootBuilder.of(httpClient)
                .defaultClient(ClientCredentials.of()
                                .withClientId(production7ClientId)
                                .withClientSecret(production7ClientSecret)
                                .withScopes(production7Scope)
                                .build(),
                        ServiceRegion.AWS_US_EAST_2)
                .build("epoch-production7");

        return apiRoot;

    }



    @Bean
    public ProjectApiRoot createApiClientProduction8() {

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_att, port));
        VrapHttpClient httpClient = new CtOkHttp4Client(builder -> builder.proxy(proxy));

        final ProjectApiRoot apiRoot = ApiRootBuilder.of(httpClient)
                .defaultClient(ClientCredentials.of()
                                .withClientId(production8ClientId)
                                .withClientSecret(production8ClientSecret)
                                .withScopes(production8Scope)
                                .build(),
                        ServiceRegion.AWS_US_EAST_2)
                .build("epoch-production8");

        return apiRoot;

    }

	public ProjectApiRoot getEnvironment(String environment) {
        if ("dev".equalsIgnoreCase(environment)) {
            return createApiClientDev();
        } else if ("staging".equalsIgnoreCase(environment)) {
            return createApiClient();
        } else if ("staging2".equalsIgnoreCase(environment)) {
            return createApiClientStaging2();
        } else if ("staging3".equalsIgnoreCase(environment)) {
            return createApiClientStaging3();
        }
        else if ("production7".equalsIgnoreCase(environment)) {
            return createApiClientProduction7();
        } else if ("production8".equalsIgnoreCase(environment)) {
            return createApiClientProduction8();
        }
        return null;
    }
}
