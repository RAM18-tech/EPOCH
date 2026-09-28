# Copilot Instructions: Editing Multiple Files

## Purpose
This file guides Copilot on how to handle requests that involve editing multiple files in this repository.

## Guidelines
##- When a request requires changes in several files, Copilot should:
  ##1. Identify all relevant files based on the request and workspace structure.
  ##2. Edit each file as needed, using concise comments to indicate unchanged regions.
  ##3. Validate changes by running tests or checking for errors after each edit.
  ##4. Track progress using a structured todo list for complex tasks.
- When I type "edit files for local run" :
  1. File CpopClient.java method "getProducts" replace line "if (featureManagerHelper.isEnabled(Constants.SVC_PRODUCTS_CACHE_FLAG)) {" with "if (false) {"
  2. File CpopClient.java method "isEligibleToCallCacheV2" replace line "return isEligible;" with "return false;"
  3. File CpopClient.java method "isEligibleToCallCache" replace line "return isEligible;" with "return false;"
  4. File CpopPrimaryClient.java comment all the lines where "@Cacheable" annotation is there
  5. File DataSourceConfig.java comment the line "@Autowired SecretManagementClient secretManagementClient;"
  6. File DataSourceConfig.java method "getDataSource" replace line "hikariDataSource.setPassword(Preconditions.checkNotNull(secretManagementClient.retrieveCredentials().get("EPOCH_POSTGRES_PASSWORD")));" with "hikariDataSource.setPassword("Epoch_01");"
  7. File FeatureManagerRestClient.java comment the line where "@Cacheable" annotation is there
  8. File OffersResourceImpl.java replace line "List<String> salesChannelFromConfig = Arrays.asList(redisCacheHelper.getValues(Constants.REDIS_CACHE_SALESCHANNEL, Constants.OTT).get(0).split("\\s*,\\s*"));" with "List<String> salesChannelFromConfig = new ArrayList<String>();
        	salesChannelFromConfig.add("directvOnline");
        	salesChannelFromConfig.add("opus");"
  9. File RSNClient.java method "init" replace line "this.restTemplate = getRestTemplate("enabled".equalsIgnoreCase(isProxyRequired), RSN, sslEnabled, connectTimeout, readTimeout, noOfConnections);" with "this.restTemplate = getRestTemplate(false, RSN, sslEnabled, connectTimeout, readTimeout, noOfConnections);"
  10. File SecretManagementClient.java comment all the lines of this class
  11. File Application.java comment line "import com.dtv.dcp.dpd.SecretsManagementImpl;"
  12. File Application.java comment all the lines inside "public class Application {" except main method
  13. File CpopBackUpClient.java comment all the lines where "@Cacheable" annotation is there

## Example Workflow
1. User requests a new feature involving both Java and configuration files.
2. Copilot:
   - Locates the Java source files in `src/main/java/`.
   - Locates configuration files in `opt/config/`.
   - Makes edits in each file, marking unchanged code with `// ...existing code...`.
   - Validates changes and updates the todo list.

## Best Practices
- Always edit files in place, do not duplicate code.
- Use comments to indicate unchanged regions.
- Validate changes after each edit.
- Use the todo list for multi-step tasks.
