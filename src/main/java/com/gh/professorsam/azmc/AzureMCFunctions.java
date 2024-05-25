package com.gh.professorsam.azmc;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.UUID;

public class AzureMCFunctions {

    private static final AzureProfile AZURE_PROFILE;
    private static final AzureResourceManager AZURE_RESOURCE_MANAGER;
    private static final String RESOURCE_GROUP_NAME;
    private static final String REGION;
    private static final String GLOBAL_ID;
    private static final String VM_PASSWORD;
    private static final String VM_SIZE;
    private static final String BLOB_STRING;
    private static final String DOMAIN_NAME;

    static {
        AZURE_PROFILE = new AzureProfile(AzureEnvironment.AZURE);
        Dotenv dotenv = Dotenv.configure().load();
        TokenCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(dotenv.get("AZURE_TENANT_ID"))
                .clientId(dotenv.get("AZURE_CLIENT_ID"))
                .clientSecret(dotenv.get("AZURE_CLIENT_SECRET"))
                .authorityHost(AZURE_PROFILE.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        AZURE_RESOURCE_MANAGER = AzureResourceManager.authenticate(credential, AZURE_PROFILE).withSubscription(dotenv.get("AZURE_SUBSCRIPTION_ID"));
        GLOBAL_ID = dotenv.get("GLOBAL_ID");
        BLOB_STRING = dotenv.get("BLOB_STRING");
        DOMAIN_NAME = dotenv.get("DOMAIN_NAME");
        VM_PASSWORD = dotenv.get("VM_PASSWORD", UUID.randomUUID().toString());
        VM_SIZE = dotenv.get("VM_SIZE", "Standard_D2_v2");
        RESOURCE_GROUP_NAME = dotenv.get("AZURE_RESOURCE_GROUP_NAME", "azuremcgroup");
        REGION = dotenv.get("AZURE_REGION", "westeurope");
    }

    public static AzureProfile getAzureProfile() {
        return AZURE_PROFILE;
    }

    public static AzureResourceManager getAzureResourceManager(){
        return AZURE_RESOURCE_MANAGER;
    }

    public static String getResourceGroupName() {
        return RESOURCE_GROUP_NAME;
    }

    public static String getRegion() {
        return REGION;
    }

    public static String getGlobalId() {
        return GLOBAL_ID;
    }

    public static String getVmPassword() {
        return VM_PASSWORD;
    }

    public static String getVmSize() {
        return VM_SIZE;
    }

    public static String getBlobString() {
        return BLOB_STRING;
    }

    public static String getDomainName() {
        return DOMAIN_NAME;
    }
}
