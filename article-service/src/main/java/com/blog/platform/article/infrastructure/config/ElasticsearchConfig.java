package com.blog.platform.article.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "article-service.enableSearch", havingValue = "true")
@EnableElasticsearchRepositories(basePackages = "com.blog.platform.article.infrastructure.elasticsearch.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {
    
    @Value("${spring.data.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUris;
    
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUris.replace("http://", ""))
                .build();
    }
}