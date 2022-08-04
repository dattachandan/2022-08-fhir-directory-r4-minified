//package net.healthlink.fhirdirectory.configuration;
//
//import com.azure.core.credential.TokenCredential;
//import com.azure.identity.EnvironmentCredentialBuilder;
//import com.azure.spring.cloud.config.AppConfigurationCredentialProvider;
//
//public class AzureCredentials implements AppConfigurationCredentialProvider {
//
//	@Override
//	public TokenCredential getAppConfigCredential(String uri) {
//		return getCredential();
//	}
//	
//	private TokenCredential getCredential() {
//        return new EnvironmentCredentialBuilder().build();
//    }
//
//}
