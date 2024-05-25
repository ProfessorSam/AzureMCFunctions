package com.gh.professorsam.azmc;

import java.util.*;

import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

public class VmStartTrigger {
    @FunctionName("startVM")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Creating vm...");
        if(request.getBody().isEmpty()) return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();
        if(!request.getBody().get().equals("start")) return request.createResponseBuilder(HttpStatus.BAD_REQUEST).build();
        try {
            context.getLogger().info("Creating Resource group");
            ResourceGroup resourceGroup = AzureMCFunctions.getAzureResourceManager().resourceGroups().list().stream()
                    .filter(rg -> rg.name().equals(AzureMCFunctions.getResourceGroupName()))
                    .findFirst()
                    .orElseGet(() -> AzureMCFunctions.getAzureResourceManager().resourceGroups()
                            .define(AzureMCFunctions.getResourceGroupName())
                            .withRegion(AzureMCFunctions.getRegion())
                            .withTag("azmc", "azmc")
                            .create());

            context.getLogger().info("Creating NSG");
            NetworkSecurityGroup networkSecurityGroup = AzureMCFunctions.getAzureResourceManager().networkSecurityGroups().define(AzureMCFunctions.getGlobalId() + "-nsg")
                    .withRegion(AzureMCFunctions.getRegion())
                    .withExistingResourceGroup(resourceGroup)
                    .defineRule("Allow-Port-22")
                    .allowInbound()
                    .fromAnyAddress()
                    .fromAnyPort()
                    .toAnyAddress()
                    .toPort(22)
                    .withProtocol(SecurityRuleProtocol.TCP)
                    .withPriority(200)
                    .attach()
                    .defineRule("Allow-Port-25565")
                    .allowInbound()
                    .fromAnyAddress()
                    .fromAnyPort()
                    .toAnyAddress()
                    .toPort(25565)
                    .withProtocol(SecurityRuleProtocol.TCP)
                    .withPriority(100)
                    .attach()
                    .withTag("azmc", "azmc")
                    .create();

            context.getLogger().info("Creating Network");
            Network network = AzureMCFunctions.getAzureResourceManager().networks().define(AzureMCFunctions.getGlobalId() + "-vnet")
                    .withRegion(AzureMCFunctions.getRegion())
                    .withExistingResourceGroup(resourceGroup)
                    .withAddressSpace("10.0.0.0/16")
                    .defineSubnet("default")
                    .withAddressPrefix("10.0.0.0/24")
                    .withExistingNetworkSecurityGroup(networkSecurityGroup)
                    .attach()
                    .withTag("azmc", "azmc")
                    .create();

            context.getLogger().info("Creating PublicIPAdress");
            PublicIpAddress publicIpAddress = AzureMCFunctions.getAzureResourceManager().publicIpAddresses().define(AzureMCFunctions.getGlobalId() + "-ip")
                    .withRegion(AzureMCFunctions.getRegion())
                    .withExistingResourceGroup(resourceGroup)
                    .withDynamicIP()
                    .withLeafDomainLabel(AzureMCFunctions.getGlobalId() + "-azmc")
                    .withTag("azmc", "azmc")
                    .create();


            context.getLogger().info("Creating VM");
            VirtualMachine virtualMachine = AzureMCFunctions.getAzureResourceManager().virtualMachines().define(AzureMCFunctions.getGlobalId() + "-vm")
                    .withRegion(AzureMCFunctions.getRegion())
                    .withExistingResourceGroup(resourceGroup)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("default")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingPrimaryPublicIPAddress(publicIpAddress)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS)
                    .withRootUsername("az")
                    .withRootPassword(AzureMCFunctions.getVmPassword())
                    .withCustomData(Base64.getEncoder().encodeToString(String.format(
                            """
                                    #cloud-config
                                    package_upgrade: true
                                    packages:
                                      - openjdk-21-jdk
                                      - curl
                                      - git
                                    runcmd:
                                      - mkdir -m 777 -p /tmp/azmcbootstrapper
                                      - mkdir -m 777 -p /tmp/azmcserver
                                      - curl -L -o /tmp/azmcbootstrapper/bootstrapper.jar https://github.com/ProfessorSam/AzureMcBootstrapper/releases/download/v1.1.1/AzureMCBootstrapper-1.1.1-all.jar
                                      - cd /tmp/azmcserver
                                      - bash -c "java -jar /tmp/azmcbootstrapper/bootstrapper.jar %s %s" &
                                    """,
                            Base64.getEncoder().encodeToString(AzureMCFunctions.getDomainName().getBytes()), Base64.getEncoder().encodeToString(AzureMCFunctions.getBlobString().getBytes())).getBytes()))
                    .withTag("asmc", "azmc")
                    .withSize(AzureMCFunctions.getVmSize())
                    .create();

            String fqdn = publicIpAddress.fqdn();
            context.getLogger().info("VM FQDN: " + fqdn);
        } catch (Exception e) {
            context.getLogger().warning(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }

        return request.createResponseBuilder(HttpStatus.OK).body("ok").build();
    }
}
