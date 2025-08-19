package api.poja.io.service;

import static api.poja.io.endpoint.rest.model.StackType.COMPUTE_PERMISSION;
import static api.poja.io.endpoint.rest.model.StackType.EVENT;
import static api.poja.io.endpoint.rest.model.StackType.EVENT_SCHEDULER;
import static api.poja.io.endpoint.rest.model.StackType.STORAGE_BUCKET;
import static api.poja.io.model.CancelResult.NEEDS_BACKOFF;
import static api.poja.io.service.event.StackCrupdatedService.mergeAndSortStackEventList;
import static java.io.File.createTempFile;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Optional.empty;

import api.poja.io.aws.cloudformation.CloudformationComponent;
import api.poja.io.aws.cloudformation.CloudformationTemplateConf;
import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.AppEnvDeployRequested;
import api.poja.io.endpoint.event.model.PojaEvent;
import api.poja.io.endpoint.event.model.StackCrupdateCompleted;
import api.poja.io.endpoint.event.model.StackCrupdateRequested;
import api.poja.io.endpoint.event.model.StackCrupdateRequested.StackPair;
import api.poja.io.endpoint.event.model.StackCrupdated;
import api.poja.io.endpoint.rest.mapper.StackMapper;
import api.poja.io.endpoint.rest.model.StackDeployment;
import api.poja.io.endpoint.rest.model.StackEvent;
import api.poja.io.endpoint.rest.model.StackOutput;
import api.poja.io.endpoint.rest.model.StackType;
import api.poja.io.file.ExtendedBucketComponent;
import api.poja.io.model.BoundedPageSize;
import api.poja.io.model.CancelResult;
import api.poja.io.model.PageFromOne;
import api.poja.io.model.StackStatus;
import api.poja.io.model.UpdateStackResult;
import api.poja.io.model.exception.InternalServerErrorException;
import api.poja.io.model.exception.NotFoundException;
import api.poja.io.model.page.Page;
import api.poja.io.model.page.Paginator;
import api.poja.io.repository.jpa.StackRepository;
import api.poja.io.repository.jpa.dao.StackDao;
import api.poja.io.repository.model.Application;
import api.poja.io.repository.model.EnvDeploymentConf;
import api.poja.io.repository.model.Environment;
import api.poja.io.repository.model.Stack;
import api.poja.io.service.organization.OrganizationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudformation.model.Output;

@AllArgsConstructor
@Service
@Slf4j
public class StackService {
  public static final String STACK_EVENT_FILENAME = "log.json";
  private final CloudformationTemplateConf cloudformationTemplateConf;
  private final CloudformationComponent cloudformationComponent;
  private final EnvironmentService environmentService;
  private final ApplicationService applicationService;
  private final OrganizationService organizationService;
  private final StackRepository repository;
  private final StackMapper mapper;
  private final StackDao dao;
  private final EventProducer<PojaEvent> eventProducer;
  private final ExtendedBucketComponent bucketComponent;
  private final ObjectMapper om;
  private final EnvDeploymentConfService envDeploymentConfService;
  private final Paginator paginator;

  public StackStatus getStackStatus(String stackId) {
    Optional<StackStatus> stackStatus = cloudformationComponent.getStackStatus(stackId);
    return stackStatus.orElseThrow(() -> new NotFoundException("stack not found : " + stackId));
  }

  public CancelResult cancelStackDepl(String envId) {
    List<Stack> envStacks = findAllByEnvId(envId);
    for (Stack envStack : envStacks) {
      String stackId = envStack.getCfStackId();
      StackStatus stackStatus = getStackStatus(stackId);
      boolean cancellable = stackStatus.isCancellable();
      if (cancellable) {
        cloudformationComponent.cancelExistingStackUpdate(stackId);
      }
    }
    return NEEDS_BACKOFF;
  }

  public Page<StackEvent> getStackEvents(
      String orgId,
      String applicationId,
      String environmentId,
      StackType stackType,
      Instant from,
      Instant to,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    var org = organizationService.getById(orgId);

    String orgStackEventsBucketKey =
        getOrgStackEventsBucketKey(
            orgId, applicationId, environmentId, stackType, STACK_EVENT_FILENAME);
    String userStackEventsBucketKey =
        getUserStackEventsBucketKey(
            org.getOwnerId(), applicationId, environmentId, stackType, STACK_EVENT_FILENAME);

    Instant now = now();
    Predicate<StackEvent> filterByInstantInterval =
        se -> {
          Instant timestamp = se.getTimestamp();
          assert timestamp != null;
          return lessThanOrEquals(
                  Optional.ofNullable(from).orElse(now.minus(10, MINUTES)), timestamp)
              && greaterThanOrEquals(Optional.ofNullable(to).orElse(now), timestamp);
        };

    var filteredStacks =
        getFilteredStackData(
            orgStackEventsBucketKey,
            filterByInstantInterval,
            pageFromOne,
            boundedPageSize,
            StackEvent.class);

    if (filteredStacks.data().isEmpty()) {
      return getFilteredStackData(
          userStackEventsBucketKey,
          filterByInstantInterval,
          pageFromOne,
          boundedPageSize,
          StackEvent.class);
    }

    return filteredStacks;
  }

  private boolean greaterThanOrEquals(Instant instant1, Instant instant2) {
    return instant1.compareTo(instant2) >= 0;
  }

  private boolean lessThanOrEquals(Instant instant1, Instant instant2) {
    return instant1.compareTo(instant2) <= 0;
  }

  public Page<StackEvent> getStackEvents(
      String orgId,
      String applicationId,
      String environmentId,
      StackType stackType,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    var org = organizationService.getById(orgId);

    String orgStackEventsBucketKey =
        getOrgStackEventsBucketKey(
            orgId, applicationId, environmentId, stackType, STACK_EVENT_FILENAME);

    String userStackEventsBucketKey =
        getUserStackEventsBucketKey(
            org.getOwnerId(), applicationId, environmentId, stackType, STACK_EVENT_FILENAME);

    Predicate<StackEvent> noFilterPredicate = (stackEvent) -> true;

    var filteredStacks =
        getFilteredStackData(
            orgStackEventsBucketKey,
            noFilterPredicate,
            pageFromOne,
            boundedPageSize,
            StackEvent.class);

    if (filteredStacks.data().isEmpty()) {
      return getFilteredStackData(
          userStackEventsBucketKey,
          noFilterPredicate,
          pageFromOne,
          boundedPageSize,
          StackEvent.class);
    }

    return filteredStacks;
  }

  public List<StackOutput> getStackOutputs(String stackId) {
    return cloudformationComponent.getStackOutputs(stackId).stream()
        .map(StackService::toStackOutput)
        .toList();
  }

  private static StackOutput toStackOutput(Output output) {
    return new StackOutput()
        .key(output.outputKey())
        .value(output.outputValue())
        .description(output.description());
  }

  private <T> Page<T> getFilteredStackData(
      String bucketKey,
      Predicate<? super T> filterPredicate,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize,
      Class<T> clazz) {
    try {
      List<T> stackData = fromStackDataFileToList(bucketComponent, om, bucketKey, clazz);
      return paginator.apply(
          pageFromOne, boundedPageSize, stackData.stream().filter(filterPredicate).toList());
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  public void processIndependantStacksDeployment(
      List<StackDeployment> independantStacksDeployments,
      String orgId,
      String applicationId,
      String environmentId,
      AppEnvDeployRequested appEnvDeployRequested) {
    List<PojaEvent> eventsToSend =
        independantStacksDeployments.stream()
            .map(
                stack ->
                    this.deployIndependantStack(
                        stack, orgId, applicationId, environmentId, appEnvDeployRequested))
            .flatMap(List::stream)
            .toList();
    eventProducer.accept(eventsToSend);
  }

  @Transactional
  public Page<Stack> findAllBy(
      String orgId,
      String applicationId,
      String environmentId,
      String appEnvDeplId,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    var data =
        dao
            .findAllByCriteria(
                orgId,
                applicationId,
                environmentId,
                appEnvDeplId,
                PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue()))
            .stream()
            .toList();
    return new Page<>(pageFromOne, boundedPageSize, data);
  }

  public List<Stack> findAllByEnvId(String envId) {
    return repository.findAllByEnvironmentId(envId);
  }

  public Stack getById(String orgId, String applicationId, String environmentId, String stackId) {
    assert orgId != null;
    return repository
        .findByApplicationIdAndEnvironmentIdAndId(applicationId, environmentId, stackId)
        .orElseThrow(() -> new NotFoundException("Stack id=" + stackId + " not found"));
  }

  public Stack save(Stack toSave) {
    return repository.save(toSave);
  }

  /**
   * method to deploy independant stacks except EVENT_SCHEDULER which depends on event.
   *
   * @param independantStackToDeploy
   * @param orgId
   * @param applicationId
   * @param environmentId
   * @param appEnvDeployRequested
   * @return list of poja events polling stack deployment progress
   */
  public List<PojaEvent> deployIndependantStack(
      StackDeployment independantStackToDeploy,
      String orgId,
      String applicationId,
      String environmentId,
      AppEnvDeployRequested appEnvDeployRequested) {
    Application app = applicationService.getById(applicationId);
    Environment env = environmentService.getById(environmentId);
    String environmentType = env.getFormattedEnvironmentType();
    Optional<Stack> optionalIndependantStack =
        dao.findLatestByCriteria(
            applicationId, environmentId, independantStackToDeploy.getStackType());
    var appEnvDeplId = appEnvDeployRequested.getAppEnvDeploymentId();
    var envDeploymentConf = envDeploymentConfService.getByAppEnvDeplId(appEnvDeplId);

    var dependantStack =
        saveDependantStackIfHasDependantStack(independantStackToDeploy, app, env, appEnvDeplId);
    if (optionalIndependantStack.isPresent()) {
      return updateStackInDbAndCreateAsyncEvent(
          independantStackToDeploy,
          orgId,
          applicationId,
          environmentId,
          appEnvDeployRequested,
          optionalIndependantStack,
          appEnvDeplId,
          envDeploymentConf,
          dependantStack);
    } else {
      return createStackInDbAndCreateAsyncEvent(
          independantStackToDeploy,
          orgId,
          applicationId,
          environmentId,
          appEnvDeployRequested,
          environmentType,
          app,
          appEnvDeplId,
          dependantStack,
          envDeploymentConf);
    }
  }

  private List<PojaEvent> createStackInDbAndCreateAsyncEvent(
      StackDeployment independantStackToDeploy,
      String orgId,
      String applicationId,
      String environmentId,
      AppEnvDeployRequested appEnvDeployRequested,
      String environmentType,
      Application app,
      String appEnvDeplId,
      Optional<StackPair> dependantStack,
      EnvDeploymentConf envDeploymentConf) {

    String stackName =
        String.format(
            "%s-%s-%s-%s",
            environmentType,
            String.valueOf(independantStackToDeploy.getStackType()).toLowerCase().replace("_", "-"),
            app.getFormattedName(),
            app.getFormattedUserId());
    Stack saved =
        save(
            Stack.builder()
                .name(stackName)
                .cfStackId(null)
                .applicationId(applicationId)
                .environmentId(environmentId)
                .type(independantStackToDeploy.getStackType())
                .appEnvDeplId(appEnvDeplId)
                .build());
    return List.of(
        StackCrupdateRequested.builder()
            .independantStackToDeploy(independantStackToDeploy)
            .orgId(orgId)
            .applicationId(applicationId)
            .environmentId(environmentId)
            .appEnvDeployRequested(appEnvDeployRequested)
            .appEnvDeplId(appEnvDeplId)
            .stackToCrupdate(new StackPair(null, saved))
            .envDeploymentConf(envDeploymentConf)
            .dependantStack(dependantStack.orElse(null))
            .build());
  }

  private List<PojaEvent> updateStackInDbAndCreateAsyncEvent(
      StackDeployment independantStackToDeploy,
      String orgId,
      String applicationId,
      String environmentId,
      AppEnvDeployRequested appEnvDeployRequested,
      Optional<Stack> optionalIndependantStack,
      String appEnvDeplId,
      EnvDeploymentConf envDeploymentConf,
      Optional<StackPair> dependantStack) {
    var independantStackToUpdate = optionalIndependantStack.get();
    Stack saved =
        save(
            Stack.builder()
                .name(independantStackToUpdate.getName())
                .applicationId(applicationId)
                .environmentId(environmentId)
                .type(independantStackToUpdate.getType())
                .appEnvDeplId(appEnvDeplId)
                .build());
    return List.of(
        StackCrupdateRequested.builder()
            .independantStackToDeploy(independantStackToDeploy)
            .orgId(orgId)
            .applicationId(applicationId)
            .environmentId(environmentId)
            .appEnvDeployRequested(appEnvDeployRequested)
            .appEnvDeplId(appEnvDeplId)
            .envDeploymentConf(envDeploymentConf)
            .stackToCrupdate(new StackPair(independantStackToUpdate, saved))
            .dependantStack(dependantStack.orElse(null))
            .envDeploymentConf(envDeploymentConf)
            .build());
  }

  public List<PojaEvent> createStack(
      StackDeployment independantStackToDeploy,
      String orgId,
      String applicationId,
      String environmentId,
      AppEnvDeployRequested appEnvDeployRequested,
      Map<String, String> parameters,
      EnvDeploymentConf envDeploymentConf,
      Map<String, String> tags,
      Stack concernedStack,
      Optional<StackPair> dependantStack) {
    String stackName = concernedStack.getName();
    String cfStackId =
        createStack(
            orgId,
            applicationId,
            environmentId,
            independantStackToDeploy,
            parameters,
            envDeploymentConf,
            stackName,
            tags);
    Stack saved = save(concernedStack.toBuilder().cfStackId(cfStackId).build());
    return getStackCrupdatedEvents(orgId, saved, appEnvDeployRequested, dependantStack);
  }

  public List<PojaEvent> updateStackOrCreateIfNotExistsOnCloud(
      StackDeployment independantStackToDeploy,
      String orgId,
      String applicationId,
      String environmentId,
      AppEnvDeployRequested appEnvDeployRequested,
      Map<String, String> parameters,
      EnvDeploymentConf envDeploymentConf,
      Map<String, String> tags,
      StackPair stackPair,
      Optional<StackPair> dependantStack) {
    var previousStack = stackPair.first();
    var stackToUpdate = stackPair.last();
    // TODO: review hot fix, if cfStackId is null, recreate it
    if (previousStack.getCfStackId() == null) {
      return createStack(
          independantStackToDeploy,
          orgId,
          applicationId,
          environmentId,
          appEnvDeployRequested,
          parameters,
          envDeploymentConf,
          tags,
          stackToUpdate,
          dependantStack);
    }
    var updateStackResult =
        updateStack(
            orgId,
            applicationId,
            environmentId,
            independantStackToDeploy,
            parameters,
            envDeploymentConf,
            previousStack.getName(),
            tags);
    if (updateStackResult.isUpdated()) {
      Stack saved = save(stackToUpdate.toBuilder().cfStackId(updateStackResult.stackId()).build());
      return getStackCrupdatedEvents(orgId, saved, appEnvDeployRequested, dependantStack);
    }
    if (updateStackResult.isSuccess()) {
      var saved = save(stackToUpdate.toBuilder().cfStackId(previousStack.getCfStackId()).build());
      var events = new ArrayList<PojaEvent>();
      var stackCrupdatedEvents =
          getStackCrupdatedEvents(orgId, saved, appEnvDeployRequested, dependantStack);
      var resourceRetrievingEvents =
          getResourceRetrievingEvents(orgId, saved, appEnvDeployRequested.getAppEnvDeploymentId());
      events.addAll(stackCrupdatedEvents);
      events.addAll(resourceRetrievingEvents);
      return events;
    }
    return List.of();
  }

  private Optional<StackPair> saveDependantStackIfHasDependantStack(
      StackDeployment independantStackToDeploy,
      Application app,
      Environment env,
      String appEnvDeplId) {
    if (independantStackToDeploy.getDependantStackType() == null) {
      return empty();
    }
    StackType dependantStackType = independantStackToDeploy.getDependantStackType();
    String applicationId = app.getId();
    String environmentId = env.getId();
    var dependantStack = dao.findLatestByCriteria(applicationId, environmentId, dependantStackType);

    if (dependantStack.isPresent()) {
      var stackToUpdate = dependantStack.get();
      return Optional.of(
          new StackPair(
              dependantStack.get(),
              save(
                  Stack.builder()
                      .name(stackToUpdate.getName())
                      .cfStackId(null)
                      .applicationId(applicationId)
                      .environmentId(environmentId)
                      .type(dependantStackType)
                      .appEnvDeplId(appEnvDeplId)
                      .build())));
    }
    String stackName =
        String.format(
            "%s-%s-%s",
            env.getFormattedEnvironmentType(),
            String.valueOf(independantStackToDeploy.getDependantStackType())
                .toLowerCase()
                .replace("_", "-"),
            app.getFormattedName());
    return Optional.of(
        new StackPair(
            null,
            save(
                Stack.builder()
                    .name(stackName)
                    .cfStackId(null)
                    .applicationId(applicationId)
                    .environmentId(environmentId)
                    .type(dependantStackType)
                    .appEnvDeplId(appEnvDeplId)
                    .build())));
  }

  private static List<PojaEvent> getStackCrupdatedEvents(
      String orgId,
      Stack saved,
      AppEnvDeployRequested appEnvDeployRequested,
      Optional<StackPair> dependantStack) {
    if (dependantStack.isPresent()) {
      return List.of(
          StackCrupdated.builder()
              .orgId(orgId)
              .stack(saved)
              .dependantStack(dependantStack.get())
              .parentAppEnvDeployRequested(appEnvDeployRequested)
              .appEnvDeplId(appEnvDeployRequested.getAppEnvDeploymentId())
              .build());
    }
    return List.of(
        StackCrupdated.builder()
            .orgId(orgId)
            .stack(saved)
            .parentAppEnvDeployRequested(appEnvDeployRequested)
            .appEnvDeplId(appEnvDeployRequested.getAppEnvDeploymentId())
            .build());
  }

  public static String getOrgStackEventsBucketKey(
      String orgId, String appId, String envId, StackType stackType, String filename) {
    return String.format(
        "orgs/%s/apps/%s/envs/%s/stacks/%s/events/%s",
        orgId, appId, envId, stackType.getValue().toUpperCase(), filename);
  }

  public static String getUserStackEventsBucketKey(
      String userId, String appId, String envId, StackType stackType, String filename) {
    return String.format(
        "users/%s/apps/%s/envs/%s/stacks/%s/events/%s",
        userId, appId, envId, stackType.getValue().toUpperCase(), filename);
  }

  public static <T> List<T> fromStackDataFileToList(
      ExtendedBucketComponent bucketComponent, ObjectMapper om, String bucketKey, Class<T> clazz)
      throws IOException {
    if (bucketComponent.doesExist(bucketKey)) {
      File stackDataFile = bucketComponent.download(bucketKey);
      return om.readValue(
          stackDataFile, om.getTypeFactory().constructCollectionType(List.class, clazz));
    }
    return List.of();
  }

  public static Map<String, String> getParametersFrom(String environmentType) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("Env", environmentType);
    return parameters;
  }

  private String getStackTemplateUrlFrom(
      String orgId,
      String appId,
      String envId,
      StackType type,
      EnvDeploymentConf envDeploymentConf) {
    Map<StackType, Supplier<String>> stackFileKeyMap =
        Map.of(
            EVENT,
            envDeploymentConf::getEventStackFileKey,
            STORAGE_BUCKET,
            envDeploymentConf::getStorageBucketStackFileKey,
            COMPUTE_PERMISSION,
            envDeploymentConf::getComputePermissionStackFileKey,
            EVENT_SCHEDULER,
            envDeploymentConf::getEventSchedulerStackFileKey);
    String filename = stackFileKeyMap.getOrDefault(type, () -> null).get();
    return cloudformationTemplateConf
        .getCloudformationTemplateUrl(orgId, appId, envId, filename)
        .toString();
  }

  private String createStack(
      String orgId,
      String appId,
      String envId,
      StackDeployment toDeploy,
      Map<String, String> parameters,
      EnvDeploymentConf envDeploymentConf,
      String stackName,
      Map<String, String> tags) {
    return cloudformationComponent.createStack(
        stackName,
        getStackTemplateUrlFrom(orgId, appId, envId, toDeploy.getStackType(), envDeploymentConf),
        parameters,
        tags);
  }

  private UpdateStackResult updateStack(
      String orgId,
      String appId,
      String envId,
      StackDeployment toDeploy,
      Map<String, String> parameters,
      EnvDeploymentConf envDeploymentConf,
      String stackName,
      Map<String, String> tags) {
    return cloudformationComponent.updateStack(
        stackName,
        getStackTemplateUrlFrom(orgId, appId, envId, toDeploy.getStackType(), envDeploymentConf),
        parameters,
        tags);
  }

  public static Map<String, String> setUpTags(String applicationName, String applicationEnv) {
    Map<String, String> tags = new HashMap<>();
    tags.put("app", applicationName);
    tags.put("env", applicationEnv);
    tags.put("user:poja", applicationName);
    return tags;
  }

  public Optional<String> getCloudformationStackId(String stackName) {
    return cloudformationComponent.findStackIdByName(stackName);
  }

  public void initiateStackDelete(Stack stack) {
    cloudformationComponent.deleteStack(stack.getName());
  }

  public Stack archiveStack(Stack stack) {
    stack.setArchived(true);
    return save(stack);
  }

  public List<StackEvent> crupdateStackEvents(String stackIdOrName, String bucketKey) {
    List<StackEvent> stackEvents =
        cloudformationComponent.getStackEvents(stackIdOrName).stream().map(mapper::toRest).toList();
    try {
      File stackEventJsonFile;
      if (bucketComponent.doesExist(bucketKey)) {
        stackEventJsonFile = bucketComponent.download(bucketKey);
        List<StackEvent> actual = om.readValue(stackEventJsonFile, new TypeReference<>() {});
        List<StackEvent> merged = mergeAndSortStackEventList(actual, stackEvents);
        om.writeValue(stackEventJsonFile, merged);
        bucketComponent.upload(stackEventJsonFile, bucketKey);
        return merged;
      } else {
        stackEventJsonFile = createTempFile("log", ".json");
        om.writeValue(stackEventJsonFile, stackEvents);
        bucketComponent.upload(stackEventJsonFile, bucketKey);
        return stackEvents;
      }
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  private List<PojaEvent> getResourceRetrievingEvents(
      String orgId, Stack stack, String appEnvDeplId) {
    return switch (stack.getType()) {
      case STORAGE_BUCKET, EVENT ->
          List.of(
              StackCrupdateCompleted.builder()
                  .appEnvDeplId(appEnvDeplId)
                  .orgId(orgId)
                  .completionTimestamp(now())
                  .crupdatedStack(stack)
                  .build());
      case COMPUTE_PERMISSION, EVENT_SCHEDULER -> {
        log.info("Get resources for stack type={} not implemented", stack.getType());
        yield List.of();
      }
      case COMPUTE ->
          throw new RuntimeException("Compute stack update is not done by StackService");
    };
  }

  public List<Stack> getAllByApplicationId(String applicationId) {
    return repository.findAllByApplicationId(applicationId);
  }

  public boolean existsByNameAndArchived(String name, boolean archived) {
    return repository.existsByNameAndArchived(name, archived);
  }
}
