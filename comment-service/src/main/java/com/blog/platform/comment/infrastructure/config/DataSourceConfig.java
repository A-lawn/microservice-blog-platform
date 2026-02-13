package com.blog.platform.comment.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.blog.platform.comment.infrastructure.persistence.repository",
    entityManagerFactoryRef = "commentEntityManagerFactory",
    transactionManagerRef = "commentTransactionManager"
)
public class DataSourceConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.comment")
    public HikariConfig commentHikariConfig() {
        return new HikariConfig();
    }
    
    @Bean
    @Primary
    public DataSource commentDataSource() {
        return new HikariDataSource(commentHikariConfig());
    }
    
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean commentEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(commentDataSource());
        em.setPackagesToScan("com.blog.platform.comment.infrastructure.persistence.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        
        em.setJpaProperties(properties);
        em.setPersistenceUnitName("commentPersistenceUnit");
        
        return em;
    }
    
    @Bean
    @Primary
    public PlatformTransactionManager commentTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(commentEntityManagerFactory().getObject());
        return transactionManager;
    }
}