package com.bililive.entity.config;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import reactor.core.Disposable;
@Configuration
public class EsConfiguration extends AbstractElasticsearchConfiguration implements DisposableBean {
    @Autowired
    private AppConfig appConfig;

    private RestHighLevelClient client;
    @Override
    public void destroy() throws Exception {
        if(client!=null){
            client.close();
        }
    }
    @Override
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(appConfig.getEsHostPort())
                .build();
        client = RestClients.create(clientConfiguration).rest();
        return client;
    }
}
