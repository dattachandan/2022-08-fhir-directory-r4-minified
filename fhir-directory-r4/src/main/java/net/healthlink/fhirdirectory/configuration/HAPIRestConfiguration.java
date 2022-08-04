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

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.transaction.Transactional;

import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
//import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
//import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
//import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
//import org.hl7.fhir.r4.hapi.validation.ValidationSupportChain;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.cors.CorsConfiguration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.provider.r4.JpaSystemProviderR4;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
//import ca.uhn.fhir.jpa.util.ResourceProviderFactory;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;

public class HAPIRestConfiguration extends RestfulServer {

	private static final long serialVersionUID = -382175639829088882L;

	private final Logger log;

	private final FhirContext context;

	final int defPageSize;
	final int maxPageSize;
	String fhirrootpath;

	public HAPIRestConfiguration(Logger log, FhirContext context,
			int defPageSize, int maxPageSize, String fhirrootpath) {
		super();
		this.log = log;
		this.context = context;
		this.defPageSize = defPageSize;
		this.maxPageSize = maxPageSize;
		this.fhirrootpath = fhirrootpath;
	}

	@Override
	public void initialize() throws ServletException {

		super.initialize();
		log.info("HAPIServlet is being initialized");

		/*
		 * Create a FhirContext object that uses the version of FHIR specified in the
		 * properties file.
		 */
		ApplicationContext appCtx = (ApplicationContext) getServletContext()
				.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");

		/*
		 * ResourceProviders are fetched from the Spring context
		 */
		ResourceProviderFactory resourceProviders;
		Object systemProvider;

		resourceProviders = appCtx.getBean("myResourceProvidersR4", ResourceProviderFactory.class);
		systemProvider = appCtx.getBean("mySystemProviderR4", JpaSystemProviderR4.class);

		setFhirContext(context);

		registerProviders(resourceProviders.createProviders());
		registerProvider(systemProvider);

		/*
		 * Enable CORS
		 */
		CorsConfiguration config = new CorsConfiguration();
		CorsInterceptor corsInterceptor = new CorsInterceptor(config);
		config.addAllowedHeader("Accept");
		config.addAllowedHeader("Content-Type");
		config.addAllowedOrigin("*");
		config.addExposedHeader("Location");
		config.addExposedHeader("Content-Location");
		config.setAllowedMethods(Arrays.asList("GET", "OPTIONS"));
		registerInterceptor(corsInterceptor);

		/*
		 * This server interceptor causes the server to return nicely formatter and
		 * coloured responses instead of plain JSON/XML if the request is coming from a
		 * browser window. It is optional, but can be nice for testing.
		 * 
		 * 
		 */

		/*
		 * Tells the server to return pretty-printed responses by default
		 */
		setDefaultPrettyPrint(true);

		/*
		 * If you are hosting this server at a specific DNS name, the server will try to
		 * figure out the FHIR base URL based on what the web container tells it, but
		 * this doesn't always work. If you are setting links in your search bundles
		 * that just refer to "localhost", you might want to use a server address
		 * strategy:
		 */
		if (fhirrootpath != null && fhirrootpath.length() > 0) {
			setServerAddressStrategy(new HardcodedServerAddressStrategy(fhirrootpath));
		}


//		FhirValidator validator = context.newValidator();
//		
//		// Typically if you are doing profile validation, you want to disable
//		// the schema/schematron validation since the profile will specify
//		// all the same rules (and more)
//		validator.setValidateAgainstStandardSchema(false);
//		validator.setValidateAgainstStandardSchematron(false);
//		
//		// Create an interceptor to validate incoming requests
//		RequestValidatingInterceptor requestInterceptor = new RequestValidatingInterceptor();
//		
//		// Register a validator module (you could also use SchemaBaseValidator and/or SchematronBaseValidator)
//		FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport);
//		requestInterceptor.addValidatorModule(instanceValidator);
//		validator.registerValidatorModule(instanceValidator);
//		
//		ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(context), validationSupport);
//		instanceValidator.setValidationSupport(support);
//		
//		requestInterceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);
//		requestInterceptor.setAddResponseHeaderOnSeverity(ResultSeverityEnum.INFORMATION);
//		requestInterceptor.setResponseHeaderValue("Validation on ${line}: ${message} ${severity}");
//		requestInterceptor.setResponseHeaderValueNoIssues("No issues detected");
//		
//		// Now register the validating interceptor
//		registerInterceptor(requestInterceptor);


	}

}
