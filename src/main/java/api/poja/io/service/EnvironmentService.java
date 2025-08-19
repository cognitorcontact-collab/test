package api.poja.io.service;

import static api.poja.io.endpoint.event.model.EnvStatusUpdateRequested.StatusAlteration.ACTIVATE;
import static api.poja.io.endpoint.event.model.EnvStatusUpdateRequested.StatusAlteration.SUSPEND;
import static api.poja.io.endpoint.rest.mapper.ComputeStackResourceMapper.distinctByKey;
import static api.poja.io.endpoint.rest.model.DeploymentStateEnum.CODE_GENERATION_IN_PROGRESS;
import static api.poja.io.endpoint.rest.model.Environment.StatusEnum.UNDER_MODIFICATION;
import static api.poja.io.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static java.time.Instant.now;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.EnvArchivalRequested;
import api.poja.io.endpoint.event.model.EnvStatusUpdateRequested;
import api.poja.io.endpoint.event.model.PojaConfUploaded;
import api.poja.io.endpoint.event.model.PojaEvent;
import api.poja.io.endpoint.rest.model.Environment.StatusEnum;
import api.poja.io.endpoint.rest.model.EnvironmentType;
import api.poja.io.endpoint.rest.model.OneOfPojaConf;
import api.poja.io.model.EnvConf;
import api.poja.io.model.PojaVersion;
import api.poja.io.model.exception.ApiException;
import api.poja.io.model.exception.BadRequestException;
import api.poja.io.model.exception.NotFoundException;
import api.poja.io.repository.jpa.EnvironmentRepository;
import api.poja.io.repository.model.AppEnvironmentDeployment;
import api.poja.io.repository.model.EnvDeploymentConf;
import api.poja.io.repository.model.Environment;
import api.poja.io.repository.model.Stack;
import api.poja.io.service.appEnvConfigurer.AppEnvConfigurerService;
import api.poja.io.service.appEnvConfigurer.model.UploadedVersionnedConf;
import api.poja.io.service.workflows.DeploymentStateService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentService {
  private final EnvironmentRepository repository;
  private final AppEnvConfigurerService configurerService;
  private final StackService stackService;
  private final EventProducer<PojaEvent> eventProducer;
  private final EnvDeploymentConfService envDeploymentConfService;
  private final DeploymentStateService deploymentStateService;
  private final AppEnvironmentDeploymentService appEnvironmentDeploymentService;

  public EnvironmentService(
      EnvironmentRepository repository,
      AppEnvConfigurerService configurerService,
      @Lazy StackService stackService,
      EventProducer<PojaEvent> eventProducer,
      EnvDeploymentConfService envDeploymentConfService,
      DeploymentStateService deploymentStateService,
      AppEnvironmentDeploymentService appEnvironmentDeploymentService) {
    this.repository = repository;
    this.configurerService = configurerService;
    this.stackService = stackService;
    this.eventProducer = eventProducer;
    this.envDeploymentConfService = envDeploymentConfService;
    this.deploymentStateService = deploymentStateService;
    this.appEnvironmentDeploymentService = appEnvironmentDeploymentService;
  }

  public String deployEnvWithConf(String orgId, String appId, String envId, String envDeplConfId) {
    Environment environment = getById(envId);
    if (environment.isArchived()) {
      throw new BadRequestException("environment is already archived");
    }
    var envConf = envDeploymentConfService.getById(envDeplConfId);
    var pojaEnvConf =
        configurerService.readConfigAsDomain(orgId, appId, envId, envConf.getPojaConfFileKey());
    var savedAppEnvDeplID =
        saveDeplAndFireEvent(orgId, appId, environment, pojaEnvConf.getVersion(), envConf);
    save(
        environment.toBuilder()
            .appliedConfId(envConf.getId())
            .currentDeploymentId(savedAppEnvDeplID)
            .configurationFileKey(envConf.getPojaConfFileKey())
            .build());
    return savedAppEnvDeplID;
  }

  public List<Environment> findAllNotArchivedByApplicationId(String applicationId) {
    return findAllByApplicationIdAndArchived(applicationId, false);
  }

  public List<Environment> findAllByApplicationIdAndStatus(
      String applicationId, StatusEnum status) {
    return repository.findAllByApplicationIdAndStatusAndArchived(applicationId, status, false);
  }

  public List<Environment> findAllByApplicationIdAndArchived(
      String applicationId, boolean archived) {
    return repository.findAllByApplicationIdAndArchived(applicationId, archived);
  }

  public List<Environment> findAllEnvsToComputeBillingForByApplicationId(
      String applicationId, Instant computeDatetime, LocalDate endDate) {
    return repository.findAllEnvsToComputeBillingForByApplicationId(
        applicationId, computeDatetime, endDate);
  }

  public Environment getById(String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("Environment identified by id " + id + " not found"));
  }

  private PojaEvent toEnvArchivalEvent(Environment environment, boolean deleteCloudPermissions) {
    List<Stack> stacksToArchive =
        stackService.findAllByEnvId(environment.getId()).stream()
            .filter(stack -> !stack.isArchived())
            // stacks are all the same but we decided to save them multiple times by type in order
            // to know which stacks were needed for which deployments but we need to distinct by
            // cfStackId so that if stacks of same type have different id, they all get archived
            .filter(s -> s.getCfStackId() != null)
            .filter(distinctByKey(Stack::getCfStackId))
            .toList();
    return EnvArchivalRequested.builder()
        .requestedAt(now())
        .deleteCloudPermissions(deleteCloudPermissions)
        .appId(environment.getApplicationId())
        .envId(environment.getId())
        .stacks(stacksToArchive)
        .build();
  }

  public List<Environment> crupdateEnvironments(
      String applicationId,
      List<Environment> environments,
      boolean deleteCloudPermissionsForArchived) {
    environments.forEach(
        environment ->
            checkIfActiveEnvironmentExists(
                environment.getId(), applicationId, environment.getEnvironmentType()));
    List<Environment> toArchive = environments.stream().filter(Environment::isArchived).toList();
    if (!toArchive.isEmpty()) {
      List<PojaEvent> events =
          toArchive.stream()
              .map(e -> toEnvArchivalEvent(e, deleteCloudPermissionsForArchived))
              .toList();
      eventProducer.accept(events);
    }
    return repository.saveAll(environments);
  }

  public EnvConf configureEnvironment(
      String orgId,
      String appId,
      String environmentId,
      api.poja.io.endpoint.rest.model.EnvConf pojaConf) {
    Environment targettedEnvironment = getById(environmentId);
    if (targettedEnvironment.isArchived()) {
      throw new BadRequestException("environment is already archived");
    }
    UploadedVersionnedConf configuredPojaConf =
        configurerService.configureEnvironment(
            orgId, appId, targettedEnvironment, pojaConf.getConf());
    var envDeploymentConf =
        envDeploymentConfService.save(
            EnvDeploymentConf.builder()
                .id(pojaConf.getId())
                .envId(environmentId)
                .computePermissionStackFileKey(null)
                .storageBucketStackFileKey(null)
                .eventStackFileKey(null)
                .storageDatabaseSqliteStackFileKey(null)
                .buildTemplateFile(null)
                .creationDatetime(now())
                .pojaConfFileKey(configuredPojaConf.filename())
                .build());

    String confId = envDeploymentConf.getId();
    save(
        targettedEnvironment.toBuilder()
            .configurationFileKey(configuredPojaConf.filename())
            .currentConfId(confId)
            .build());
    return new EnvConf(pojaConf.getConf(), confId);
  }

  public Environment save(Environment env) {
    return repository.save(env);
  }

  private String saveDeplAndFireEvent(
      String orgId,
      String appId,
      Environment environment,
      PojaVersion pojaVersion,
      EnvDeploymentConf envDeploymentConf) {
    AppEnvironmentDeployment savedAppEnvDeployment =
        appEnvironmentDeploymentService.save(
            AppEnvironmentDeployment.builder()
                .envDeplConfId(envDeploymentConf.getId())
                .appId(appId)
                .env(environment)
                .build());
    String savedAppEnvDeploymentId = savedAppEnvDeployment.getId();
    deploymentStateService.save(
        savedAppEnvDeployment.getAppId(), savedAppEnvDeploymentId, CODE_GENERATION_IN_PROGRESS);

    PojaConfUploaded relatedEvent =
        PojaConfUploaded.builder()
            .pojaVersion(pojaVersion)
            .environmentId(environment.getId())
            .orgId(orgId)
            .filename(envDeploymentConf.getPojaConfFileKey())
            .appId(appId)
            .appEnvDeplId(savedAppEnvDeploymentId)
            .envDeplConfId(envDeploymentConf.getId())
            .build();
    eventProducer.accept(List.of(relatedEvent));
    return savedAppEnvDeploymentId;
  }

  @Transactional
  public EnvConf getConfig(String orgId, String appId, String environmentId, String configId) {
    EnvDeploymentConf envConf = envDeploymentConfService.getById(configId);
    String configurationFileKey = envConf.getPojaConfFileKey();
    if (configurationFileKey == null) {
      throw new ApiException(
          SERVER_EXCEPTION,
          "config not found in DB for org.Id = "
              + orgId
              + " app.Id = "
              + appId
              + " environment.Id = "
              + environmentId);
    }
    OneOfPojaConf conf =
        configurerService.readConfig(orgId, appId, environmentId, configurationFileKey);
    return new EnvConf(conf, envConf.getId());
  }

  public void checkIfActiveEnvironmentExists(String id, String appId, EnvironmentType type) {
    Optional<Environment> actualById = repository.findById(id);
    if (actualById.isEmpty()) {
      Optional<Environment> actualByAppIdAndType =
          repository.findFirstByApplicationIdAndEnvironmentTypeAndArchived(appId, type, false);
      if (actualByAppIdAndType.isPresent()) {
        throw new BadRequestException("Only one " + type + " environment can be created.");
      }
    }
  }

  public Environment getUserApplicationEnvironmentById(
      String orgId, String applicationId, String environmentId) {
    return repository
        .findByCriteria(orgId, applicationId, environmentId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Environment identified by id "
                        + environmentId
                        + " for application "
                        + applicationId
                        + " of org "
                        + orgId
                        + " not found"));
  }

  public Environment getUserApplicationEnvironmentByIdAndType(
      String applicationId, EnvironmentType environmentType) {
    return repository
        .findFirstByApplicationIdAndEnvironmentTypeAndArchived(
            applicationId, environmentType, false)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Environment identified by type "
                        + environmentType
                        + " for application "
                        + applicationId
                        + " not found"));
  }

  @Transactional
  public void updateEnvStatus(String id, StatusEnum status) {
    repository.updateStatus(id, status);
  }

  @Transactional
  public void updateUnarchivedStatusByApplicationId(String applicationId, StatusEnum status) {
    repository.updateUnarchivedStatusByApplicationId(applicationId, status);
  }

  @Transactional
  public Environment updateEnvStatusAsync(
      String orgId, String applicationId, String environmentId, StatusEnum statusEnum) {
    Environment environment =
        getUserApplicationEnvironmentById(orgId, applicationId, environmentId);
    if (environment.isArchived()) {
      throw new BadRequestException("Environment.Id=" + environment.getId() + " is archived.");
    }
    if (statusEnum.equals(environment.getStatus())) {
      return environment;
    }
    if (UNDER_MODIFICATION.equals(environment.getStatus())) {
      throw new BadRequestException(
          "Environment.Id=" + environment.getId() + " status is still under modification.");
    }
    updateEnvStatus(environment.getId(), UNDER_MODIFICATION);
    eventProducer.accept(
        List.of(
            new EnvStatusUpdateRequested(
                environment.getApplicationId(), environment.getId(), getStatus(statusEnum))));
    return environment;
  }

  private static EnvStatusUpdateRequested.StatusAlteration getStatus(StatusEnum status) {
    return switch (status) {
      case ACTIVE -> ACTIVATE;
      case SUSPENDED -> SUSPEND;
      case UNDER_MODIFICATION -> throw new IllegalArgumentException();
    };
  }
}
