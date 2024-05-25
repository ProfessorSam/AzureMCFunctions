package com.gh.professorsam.azmc;

import java.util.*;

import com.azure.resourcemanager.resources.models.ForceDeletionResourceType;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure VM Delete
 */
public class VmStopTrigger {
    @FunctionName("stopVM")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Stopping vm");
        if(request.getBody().isEmpty()) return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("").build();
        if(!request.getBody().get().equals("stop")) return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("").build();
        ResourceGroup resourceGroup = AzureMCFunctions.getAzureResourceManager().resourceGroups().getByName(AzureMCFunctions.getResourceGroupName());
        if (resourceGroup == null) {
            context.getLogger().severe("Resource group not found.");
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("Resource not found.").build();
        }
        AzureMCFunctions.getAzureResourceManager().resourceGroups().deleteByName(resourceGroup.name(), ForceDeletionResourceType.values());
        return request.createResponseBuilder(HttpStatus.OK).body("").build();
    }
}
