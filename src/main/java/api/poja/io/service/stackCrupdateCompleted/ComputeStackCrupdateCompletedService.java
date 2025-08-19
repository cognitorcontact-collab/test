package api.poja.io.service.stackCrupdateCompleted;

import static api.poja.io.repository.model.ComputeStackResource.computeLambdaFunctionArn;
import static api.poja.io.repository.model.ComputeStackResource.computeLogGroupArn;
import static api.poja.io.service.event.OrganizationUpsertedService.POJA_USER_GROUP_NAME_PREFIX;
import static api.poja.io.service.event.StackCrupdateCompletedService.getPhysicalResourceId;
import static com.amazonaws.services.lambda.runtime.events.IamPolicyResponse.ALLOW;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import api.poja.io.aws.AwsConf;
import api.poja.io.aws.iam.IamComponent;
import api.poja.io.endpoint.event.model.StackCrupdateCompleted;
import api.poja.io.repository.jpa.ComputeStackResourceRepository;
import api.poja.io.repository.model.ComputeStackResource;
import api.poja.io.repository.model.ConsoleUserGroup;
import api.poja.io.repository.model.Organization;
import api.poja.io.repository.model.Stack;
import api.poja.io.service.AppEnvironmentDeploymentService;
import api.poja.io.service.ComputeStackResourceService;
import api.poja.io.service.ConsoleUserGroupService;
import api.poja.io.service.organization.OrganizationService;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.policybuilder.iam.IamPolicy;
import software.amazon.awssdk.services.cloudformation.model.StackResource;
import software.amazon.awssdk.services.iam.model.LimitExceededException;

@Service
@Slf4j
public class ComputeStackCrupdateCompletedService
    extends AbstractStackCrupdatedCompletedService<ComputeStackResource> {
  private final ComputeStackResourceRepository computeStackResourceRepository;
  public static final String LAMBDA_DASHBOARD_SID = "LambdaDashboard";
  public static final String CLOUDWATCH_LOGS_SID = "CloudwatchLogs";
  public static final String CLOUDWATCH_METRICS_SID = "CloudwatchMetrics";

  private final ComputeStackResourceService computeStackResourceService;
  private final OrganizationService organizationService;
  private final ConsoleUserGroupService consoleUserGroupService;
  private final AppEnvironmentDeploymentService appEnvironmentDeploymentService;

  public ComputeStackCrupdateCompletedService(
      IamComponent iamComponent,
      AwsConf awsConf,
      ComputeStackResourceService computeStackResourceService,
      OrganizationService organizationService,
      ConsoleUserGroupService consoleUserGroupService,
      AppEnvironmentDeploymentService appEnvironmentDeploymentService,
      ComputeStackResourceRepository computeStackResourceRepository) {
    super(iamComponent, awsConf);
    this.computeStackResourceService = computeStackResourceService;
    this.organizationService = organizationService;
    this.consoleUserGroupService = consoleUserGroupService;
    this.appEnvironmentDeploymentService = appEnvironmentDeploymentService;
    this.computeStackResourceRepository = computeStackResourceRepository;
  }

  @Override
  protected final ComputeStackResource getStackResource(
      StackCrupdateCompleted stackCrupdateCompleted, List<StackResource> stackResources) {
    String FRONTAL_FUNCTION_LOGICAL_RESOURCE_ID = "FrontalFunction";
    String frontalFunctionName =
        getPhysicalResourceId(stackResources, FRONTAL_FUNCTION_LOGICAL_RESOURCE_ID);
    String WORKER_FUNCTION_1_LOGICAL_RESOURCE_ID = "WorkerFunction1";
    String workerFunction1Name =
        getPhysicalResourceId(stackResources, WORKER_FUNCTION_1_LOGICAL_RESOURCE_ID);
    String WORKER_FUNCTION_2_LOGICAL_RESOURCE_ID = "WorkerFunction2";
    String workerFunction2Name =
        getPhysicalResourceId(stackResources, WORKER_FUNCTION_2_LOGICAL_RESOURCE_ID);
    Stack crupdatedStack = stackCrupdateCompleted.getCrupdatedStack();
    return ComputeStackResource.builder()
        .frontalFunctionName(frontalFunctionName)
        .worker1FunctionName(workerFunction1Name)
        .worker2FunctionName(workerFunction2Name)
        .stackId(crupdatedStack.getId())
        .environmentId(crupdatedStack.getEnvironmentId())
        .environmentId(crupdatedStack.getEnvironmentId())
        .appEnvDeplId(stackCrupdateCompleted.getAppEnvDeplId())
        .creationDatetime(stackCrupdateCompleted.getCompletionTimestamp())
        .build();
  }

  private boolean existsIgnoringIdAndCreationTimestamp(ComputeStackResource stackResource) {
    List<ComputeStackResource> existingEntities =
        computeStackResourceService.findAllByEnvironmentId(stackResource.getEnvironmentId());
    return existingEntities.stream()
        .map(ComputeStackCrupdateCompletedService::resetGeneratedValues)
        .toList()
        .contains(resetGeneratedValues(stackResource));
  }

  private static ComputeStackResource resetGeneratedValues(
      ComputeStackResource computeStackResource) {
    return computeStackResource.toBuilder()
        .id(null)
        .creationDatetime(null)
        .consoleUserGroup(null)
        /*
            do not include as generated values in order to have compute stack resources per deployment
        .id(null)
        */
        .build();
  }

  private ConsoleUserGroup updateLogGroupInlinePolicy(Organization org, IamPolicy newPolicy) {
    var currentConsoleUserGroups = consoleUserGroupService.findAvailablesByOrgId(org.getId());
    return updatePolicyInAvailableGroupOrCreateNew(currentConsoleUserGroups, org, newPolicy);
  }

  private ConsoleUserGroup updatePolicyInAvailableGroupOrCreateNew(
      java.util.Stack<ConsoleUserGroup> consoleUserGroups, Organization org, IamPolicy newPolicy) {
    if (consoleUserGroups.isEmpty()) {
      var newConsoleUserGroup =
          consoleUserGroupService.createNewByOrg(
              org.getId(),
              org.getOwnerId(),
              org.getConsoleUsername(),
              ConsoleUserGroup.builder()
                  .name(POJA_USER_GROUP_NAME_PREFIX + randomUUID().toString().substring(0, 8))
                  .available(true)
                  .archived(false)
                  .orgId(org.getId())
                  .build());
      iamComponent.crupdateGroupPolicyStatements(
          newConsoleUserGroup.getName(), org.getConsoleUserGroupPolicyDocumentName(), newPolicy);
      return newConsoleUserGroup;
    }
    var currentConsoleUserGroup = consoleUserGroups.pop();
    try {
      iamComponent.crupdateGroupPolicyStatements(
          currentConsoleUserGroup.getName(),
          org.getConsoleUserGroupPolicyDocumentName(),
          newPolicy);
      return currentConsoleUserGroup;
    } catch (LimitExceededException e) {
      consoleUserGroupService.save(currentConsoleUserGroup.toBuilder().available(false).build());
      return updatePolicyInAvailableGroupOrCreateNew(consoleUserGroups, org, newPolicy);
    } catch (Exception e) {
      log.error("e", e);
      // TODO: check if exists before saying it is archived
      consoleUserGroupService.save(
          currentConsoleUserGroup.toBuilder().available(false).archived(true).build());
      return updatePolicyInAvailableGroupOrCreateNew(consoleUserGroups, org, newPolicy);
    }
  }

  @Override
  public void accept(
      StackCrupdateCompleted computeStackCrupdateCompleted, List<StackResource> stackResources) {
    if (stackResources.isEmpty()) {
      return;
    }
    ComputeStackResource computeStackResource =
        getStackResource(computeStackCrupdateCompleted, stackResources);
    Stack crupdatedStack = computeStackCrupdateCompleted.getCrupdatedStack();
    String stackName = crupdatedStack.getName();
    if (!existsIgnoringIdAndCreationTimestamp(computeStackResource)) {
      log.info("Saving stack name={} compute resources name", stackName);
      var org = organizationService.getById(computeStackCrupdateCompleted.getOrgId());
      var saved = computeStackResourceService.save(computeStackResource);
      var consoleUserGroup =
          updateLogGroupInlinePolicy(org, iamPolicy(saved, List.of(org.getConsoleUsername())));
      computeStackResourceRepository.save(
          saved.toBuilder().consoleUserGroup(consoleUserGroup).build());
    }
    String appEnvDeplId = computeStackCrupdateCompleted.getAppEnvDeplId();
    Optional<URI> computeStackFrontalUrl =
        computeStackResourceService.getComputeStackFrontalUrl(stackName);
    if (computeStackFrontalUrl.isEmpty()) {
      log.error("could not find stack frontal URL, INTERNAL ERROR for depl id {}", appEnvDeplId);
      return;
    }
    appEnvironmentDeploymentService.updateDeployedUri(appEnvDeplId, computeStackFrontalUrl.get());
  }

  @Override
  protected final IamPolicy iamPolicy(
      ComputeStackResource computeStackResource, List<String> usernames) {
    String accountId = awsConf.getAccountId();
    var region = awsConf.getRegion();

    List<String> logGroupArns =
        getFunctionNames(computeStackResource).stream()
            .map(a -> computeLogGroupArn(region, accountId, a))
            .toList();
    List<String> lambdaFunctionArns =
        getFunctionNames(computeStackResource).stream()
            .map(a -> computeLambdaFunctionArn(region, accountId, a))
            .toList();

    return IamPolicy.builder()
        .addStatement(
            req ->
                req.sid(LAMBDA_DASHBOARD_SID)
                    .effect(ALLOW)
                    .addAction("lambda:Get*")
                    .addAction("lambda:List*")
                    .resourceIds(lambdaFunctionArns))
        .addStatement(
            req ->
                req.sid(CLOUDWATCH_LOGS_SID)
                    .effect(ALLOW)
                    .addAction("logs:GetLogRecord")
                    .addAction("logs:FilterLogEvents")
                    .addAction("logs:GetLogEvents")
                    .resourceIds(logGroupArns))
        .addStatement(
            req ->
                req.sid(CLOUDWATCH_METRICS_SID)
                    .effect(ALLOW)
                    .addAction("cloudwatch:GetMetricData")
                    .addAction("cloudwatch:ListMetrics")
                    .addResource("*")) // TODO(UNSAFE): Too permissive. Restrict further.
        .build();
  }

  public static List<String> getFunctionNames(ComputeStackResource computeStackResource) {
    return Stream.of(
            computeStackResource.getFrontalFunctionName(),
            computeStackResource.getWorker1FunctionName(),
            computeStackResource.getWorker2FunctionName())
        .filter(Objects::nonNull)
        .collect(toList());
  }
}
