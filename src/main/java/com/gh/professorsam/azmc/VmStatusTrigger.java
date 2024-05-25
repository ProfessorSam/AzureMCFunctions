package com.gh.professorsam.azmc;

import java.util.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

public class VmStatusTrigger {
    @FunctionName("statusVM")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Processing Status Request");

        if(AzureMCFunctions.getAzureResourceManager().resourceGroups().contain(AzureMCFunctions.getResourceGroupName())){
            return request.createResponseBuilder(HttpStatus.OK).body("""
                    {
                    "isOn":"true"
                    }
                    """).build();
        }
        return request.createResponseBuilder(HttpStatus.OK).body("""
                    {
                    "isOn":"false"
                    }
                    """).build();
    }
}
