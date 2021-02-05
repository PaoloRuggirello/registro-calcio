package com.elis.registrocalcio.repositoryConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@PropertySource({"classpath:application.yml"})
@EnableJpaRepositories(
        basePackages = "com.elis.registrocalcio.repository.general",
        entityManagerFactoryRef = "generalEntityManager",
        transactionManagerRef = "generalTransactionManager"
)
public class PersistenceGeneralConfiguration {

    @Autowired
    Environment env;


    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource generalDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean generalEntityManager() {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(generalDataSource());
        em.setPackagesToScan(new String[] { "com.elis.registrocalcio.model.general" });

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.datasource.ddl-auto"));
        properties.put("hibernate.dialect",env.getProperty("spring.datasource.dialect"));
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Primary
    @Bean
    public PlatformTransactionManager generalTransactionManager() {
        JpaTransactionManager transactionManager
                = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                generalEntityManager().getObject());
        return transactionManager;
    }
}
