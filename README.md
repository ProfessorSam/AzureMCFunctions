# AzureMCFunctions

## Envoirement
- `AZURE_TENANT_ID`: Tenant id
- `AZURE_CLIENT_ID`: Client id
- `AZURE_CLIENT_SECRET`: Client secret
- `AZURE_RESOURCE_GROUP_NAME`: Name of resource group where vms are created
- `AZURE_REGION`: Region of azure mc server. Defaults to westeurope
- `GLOBAL_ID`: Random (8 char) String for global identifiers
- `VM_SIZE`: Size of the vm. Defaults to DS2_v2
- `VM_PASSWORD`: SSH password. Defaults to random password
- `BLOB_STRING`: Env for bootstrapper
- `DOMAIN_NAME`: Env for bootstrapper