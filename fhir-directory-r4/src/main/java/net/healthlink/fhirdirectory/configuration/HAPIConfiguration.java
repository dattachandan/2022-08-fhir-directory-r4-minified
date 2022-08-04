/*
 * Copyright (c) 2020 HealthLink Limited.
 *
 * This document is copyright. Except for the purpose of fair reviewing, no part
 * of this publication may be reproduced or transmitted in any form or by any
 * means, electronic or mechanical, including photocopying, recording, or any
 * information storage and retrieval system, without permission in writing from
 * the publisher. Infringers of copyright render themselves liable for
 * prosecution.
 * 
 * @author Sajith Jamal
 */

package net.healthlink.fhirdirectory.configuration;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.SearchParameter;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.slf4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.google.common.collect.Sets;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.config.DaoConfig.IndexEnabledEnum;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.config.BaseJavaConfigR4;
import ca.uhn.fhir.jpa.config.r4.BaseR4Config;
import ca.uhn.fhir.jpa.dao.BaseHapiFhirResourceDao;
import ca.uhn.fhir.jpa.dao.TransactionProcessor;
import ca.uhn.fhir.jpa.dao.r4.FhirSystemDaoR4;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.entity.ModelConfig;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.util.DerbyTenSevenHapiFhirDialect;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.parser.LenientErrorHandler;
import ca.uhn.fhir.rest.api.server.storage.ResourcePersistentId;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

//import org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration;

@Configuration
@EnableTransactionManagement
public class HAPIConfiguration extends BaseJavaConfigR4 {
	
	
	@Value("${spring.datasource.url}")
	private String jdbcUrl;
	
	@Value("${spring.datasource.driverClassName}")
	private String driverClassName;
	
	@Value("${spring.jpa.properties.hibernate.dialect}")
	private String dialect;
	
	@Value("${spring.datasource.username}")
	private String username;
	
	@Value("${spring.datasource.password}")
	private String password;

	@Value("${run_schedule}")
	private boolean runSch;
	
	@Value("${maxActive}")
	private int maxActive;
	
	@Value("${initialSize}")
	private int initialSize;
	
	@Value("${maxWait}")
	private int maxWait;
	
	@Value("${removeAbandonedTimeout}")
	private int removeAbandonedTimeout;
	
	@Value("${evicIdleTime}")
	private int evicIdleTime;
	
	@Value("${minIdle}")
	private int minIdle;
	
//	@Value("${searchtype}")
//	private String searchType;
	


	@Bean
    @ConfigurationProperties(prefix="spring.datasource")
	@Primary
    public DataSource dataSource() {	
    	//return DataSourceBuilder.create().build();
		
		
		PoolConfiguration p = new PoolProperties();
		p.setUrl(jdbcUrl);
		p.setDriverClassName(driverClassName);
		p.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		p.setUsername(username);
		p.setPassword(password);
		p.setMaxActive(maxActive);
		p.setInitialSize(initialSize);
		p.setMaxWait(maxWait);
		p.setRemoveAbandonedTimeout(removeAbandonedTimeout);
		p.setMinEvictableIdleTimeMillis(evicIdleTime);
		p.setMinIdle(minIdle);
		
		
		
		org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(p);
		
		
		return ds;
    }

	
	@Override
    @Bean()
	@Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(ConfigurableListableBeanFactory myConfigurableListableBeanFactory) {
        LocalContainerEntityManagerFactoryBean retVal = super.entityManagerFactory(myConfigurableListableBeanFactory);
        retVal.setPersistenceUnitName("HAPI_PU");
        retVal.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        retVal.setPackagesToScan("net.healthlink.fhirdirectory","ca.uhn.fhir.jpa.model","ca.uhn.fhir.jpa.entity");
        try {
            retVal.setDataSource(dataSource());
        } catch (Exception e) {
            throw new ConfigurationException("Could not set the data source due to a configuration issue", e);
        }
        
        Properties props = new Properties();
		props.setProperty("hibernate.dialect", dialect);
		props.setProperty("hibernate.format_sql", "false");
		props.setProperty("hibernate.show_sql", "false");
		props.setProperty("hibernate.hbm2ddl.auto", "update");
		props.setProperty("hibernate.jdbc.batch_size", "200");
		props.setProperty("hibernate.cache.use_query_cache", "false");
		props.setProperty("hibernate.cache.use_second_level_cache", "false");
		props.setProperty("hibernate.cache.use_structured_entries", "false");
		props.setProperty("hibernate.cache.use_minimal_puts", "false");

		props.setProperty("hibernate.search.backend.type", "lucene");
        props.setProperty("hibernate.search.backend.directory.type", "local-filesystem");
        props.setProperty("hibernate.search.backend.directory.root", "lucenefiles");
        props.setProperty("hibernate.search.backend.lucene_version", "LUCENE_CURRENT");
        
        props.setProperty("hibernate.search.worker.thread_pool.size", "500");

        props.setProperty("hibernate.search.backend.analysis.configurer", "ca.uhn.fhir.jpa.search.HapiLuceneAnalysisConfigurer");

		
		
        retVal.setJpaProperties(props);
        
        
        return retVal;
    }
	
	
	
	@Bean
	public DaoConfig daoConfig() {
	    DaoConfig retVal = new DaoConfig();
	    //retVal.setEnforceReferentialIntegrityOnWrite(false);
	    retVal.setAllowMultipleDelete(true);
	    //retVal.setAllowInlineMatchUrlReferences(false);
	    //retVal.setAllowExternalReferences(true);
	    
	    //force use my ids, not auto-generated ids
	    retVal.setResourceClientIdStrategy(DaoConfig.ClientIdStrategyEnum.ANY);
	    retVal.setResourceServerIdStrategy(DaoConfig.IdStrategyEnum.UUID);
	    
	    retVal.setExpungeEnabled(true);
	    retVal.setCacheControlNoStoreMaxResultsUpperLimit(200000);
	    retVal.setReuseCachedSearchResultsForMillis(null);
	    //retVal.setAutoCreatePlaceholderReferenceTargets(true);
	    retVal.setAllowContainsSearches(true);

	    //retVal.setSchedulingDisabled(false);
	    //retVal.setMarkResourcesForReindexingUponSearchParameterChange(true);
	   
	    retVal.setIndexMissingFields(IndexEnabledEnum.DISABLED); //setting disabled. :missing is now not supported, if need to support, enable this
	    retVal.setAdvancedLuceneIndexing(true); //I think this allows :contains searches according to method doc
	    retVal.setAllowContainsSearches(true);
	    
	    
	    
	    //taken from DAOConfig class: DEFAULT_BUNDLE_TYPES_ALLOWED_FOR_STORAGE
	    Set<String> bundleTypes = Collections.unmodifiableSet(new TreeSet<>(Sets.newHashSet(
	    		Bundle.BundleType.COLLECTION.toCode(),
	    		Bundle.BundleType.DOCUMENT.toCode(),
	    		Bundle.BundleType.MESSAGE.toCode(),
	    		Bundle.BundleType.TRANSACTION.toCode()
	    	)));
	    
	    retVal.setBundleTypesAllowedForStorage(bundleTypes);
	    return retVal;
	}
	
	@Bean
    public ModelConfig modelConfig() {
        
        return daoConfig().getModelConfig();
    }
	
	@Bean
	@Primary
	@Autowired
	public PlatformTransactionManager hapiTransactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager retVal = new JpaTransactionManager();
		retVal.setEntityManagerFactory(entityManagerFactory);
		return retVal;
	}

	@Override
	@Bean
	@Primary
	public DatabaseBackedPagingProvider databaseBackedPagingProvider() {
		// TODO Auto-generated method stub
		return super.databaseBackedPagingProvider();
	}
	
	@Bean
	public FhirContext getFhirContext()
	{
		FhirContext ctx = FhirContext.forR4();
		ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
		return ctx;
	}
	
	@Bean
	public PartitionSettings getPSettings()
	{
		return new PartitionSettings();
	}
	
	@Bean
    public ServletRegistrationBean<HAPIRestConfiguration> initializeHapiServer(FhirContext ctx, Logger log, @Value("${defaultpagesize}") int defPagSize, @Value("${maxpagesize}") int maxPagSize, @Value("${fhirrootpath}") String fhirrootpath) {
        ServletRegistrationBean<HAPIRestConfiguration> serv = new ServletRegistrationBean<>(new HAPIRestConfiguration(log, ctx, defPagSize, maxPagSize,fhirrootpath), "/fhir/*");
        serv.setLoadOnStartup(1);
        return serv;
    }

	
}
