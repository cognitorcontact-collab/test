package api.poja.io.service;

import static api.poja.io.endpoint.event.model.AppStatusUpdateRequested.StatusAlteration.ACTIVATE;
import static api.poja.io.endpoint.event.model.AppStatusUpdateRequested.StatusAlteration.SUSPEND;
import static api.poja.io.endpoint.rest.model.Application.StatusEnum.UNDER_MODIFICATION;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.function.Predicate.not;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.AppStatusUpdateRequested;
import api.poja.io.endpoint.event.model.ApplicationCrupdated;
import api.poja.io.endpoint.event.model.PojaEvent;
import api.poja.io.endpoint.rest.model.Application.StatusEnum;
import api.poja.io.endpoint.rest.model.ApplicationBase;
import api.poja.io.endpoint.rest.model.Environment;
import api.poja.io.model.BoundedPageSize;
import api.poja.io.model.PageFromOne;
import api.poja.io.model.UserApplicationsDto;
import api.poja.io.model.exception.BadRequestException;
import api.poja.io.model.exception.NotFoundException;
import api.poja.io.model.page.Page;
import api.poja.io.repository.jpa.ApplicationRepository;
import api.poja.io.repository.jpa.dao.ApplicationDao;
import api.poja.io.repository.model.Application;
import api.poja.io.repository.model.mapper.ApplicationMapper;
import api.poja.io.service.organization.OrganizationService;
import api.poja.io.service.validator.AppValidator;
import api.poja.io.service.validator.UserAppThresholdValidator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ApplicationService {
  private final ApplicationRepository repository;
  private final ApplicationDao dao;
  private final ApplicationMapper mapper;
  private final EventProducer<PojaEvent> eventProducer;
  private final UserAppThresholdValidator appThresholdValidator;
  private final OrganizationService organizationService;
  private final EnvironmentService environmentService;
  private final AppValidator appValidator;

  public ApplicationService(
      ApplicationRepository repository,
      ApplicationDao dao,
      @Qualifier("DomainApplicationMapper") ApplicationMapper mapper,
      EventProducer<PojaEvent> eventProducer,
      UserAppThresholdValidator appThresholdValidator,
      OrganizationService organizationService,
      EnvironmentService environmentService,
      AppValidator appValidator) {
    this.repository = repository;
    this.dao = dao;
    this.mapper = mapper;
    this.eventProducer = eventProducer;
    this.appThresholdValidator = appThresholdValidator;
    this.organizationService = organizationService;
    this.environmentService = environmentService;
    this.appValidator = appValidator;
  }

  @Transactional
  public List<Application> saveOrgApplications(String orgId, List<ApplicationBase> toSave) {
    var organization = organizationService.getOrganizationById(orgId);
    if (!toSave.stream().allMatch(app -> TRUE.equals(app.getArchived()))) {
      appThresholdValidator.accept(
          organization.getOwnerId(),
          toSave.stream()
              .filter(app -> FALSE.equals(app.getArchived()))
              .map(ApplicationBase::getId)
              .toList());
    }
    List<PojaEvent> events = new ArrayList<>();
    List<Application> entities = toSave.stream().map(mapper::toDomain).toList();
    for (Application app : entities) {
      appValidator.accept(app);
      events.add(toApplicationCrupdatedEvent(app));
    }
    var saved = repository.saveAll(entities);
    eventProducer.accept(events);
    return saved;
  }

  private ApplicationCrupdated toApplicationCrupdatedEvent(Application entity) {
    return ApplicationCrupdated.builder()
        .applicationId(entity.getId())
        .applicationRepoName(entity.getGithubRepositoryName())
        .repoUrl(entity.getGithubRepositoryUrl())
        .installationId(entity.getInstallationId())
        .description(entity.getDescription())
        .repoPrivate(entity.isGithubRepositoryPrivate())
        .previousApplicationRepoName(entity.getPreviousGithubRepositoryName())
        .archived(entity.isArchived())
        .imported(entity.isImported())
        .build();
  }

  public Application getById(String id) {
    return findById(id)
        .orElseThrow(
            () -> new NotFoundException("Application identified by id=" + id + " not found"));
  }

  public Optional<Application> findById(String id) {
    return repository.findById(id);
  }

  public Page<Application> findAllByCriteria(
      String orgId, String name, PageFromOne pageFromOne, BoundedPageSize boundedPageSize) {
    var data =
        dao.findAllByCriteria(
            orgId, name, PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue()));
    return new Page<>(pageFromOne, boundedPageSize, data);
  }

  public List<Application> findAllByOrgIdAndArchived(String orgId, boolean archived) {
    return repository.findAllByOrgIdAndArchived(orgId, archived);
  }

  public UserApplicationsDto findAllByOrgsOwnedByUser(
      String userId, PageFromOne pageFromOne, BoundedPageSize boundedPageSize) {
    Pageable pageable = PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue());
    var data = repository.findAllFromOrgsOwnedByUserByCriteria(userId, false, pageable);
    return new UserApplicationsDto(
        new Page<>(pageFromOne, boundedPageSize, data.getContent()),
        userId,
        data.getTotalElements());
  }

  public Application findByRepositoryId(String repositoryId) {
    return repository
        .findByGithubRepositoryId(repositoryId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Application identified by repository id=" + repositoryId + " not found"));
  }

  public Application getById(String id, String orgId) {
    return repository
        .findByIdAndOrgId(id, orgId)
        .orElseThrow(
            () -> new NotFoundException("Application identified by id=" + id + " not found"));
  }

  public List<Application> findAllToBillByUserId(String userId, YearMonth yearMonth) {
    return repository.findAllToBillByUserId(
        userId, yearMonth.getYear() * 100L + yearMonth.getMonth().getValue());
  }

  public List<Application> findAllToComputeBillingForByOrgId(
      String orgId, Instant computeDatetime, LocalDate endDate) {
    return repository.findAllToComputeBillingForByOrgId(orgId, computeDatetime, endDate);
  }

  public List<Application> findAllNotArchivedAndNotSuspendedByUserId(String userId) {
    return repository.findAllByUserIdAndArchived(userId, false).stream()
        .filter(not(Application::isSuspended))
        .toList();
  }

  public List<Application> findAllNotArchivedAndSuspendedByUserId(String userId) {
    return repository.findAllByUserIdAndArchived(userId, false).stream()
        .filter(Application::isSuspended)
        .toList();
  }

  @Transactional
  public Application updateAppStatusAsync(
      String orgId, String applicationId, StatusEnum statusEnum) {
    var application = getById(applicationId, orgId);

    if (application.isArchived()) {
      throw new BadRequestException("Application.Id=" + application.getId() + " is archived.");
    }
    if (statusEnum.equals(application.getStatus())) {
      return application;
    }
    if (UNDER_MODIFICATION.equals(application.getStatus())) {
      throw new BadRequestException(
          "Application.Id=" + application.getId() + " status is still under modification.");
    }

    environmentService.updateUnarchivedStatusByApplicationId(
        application.getId(), Environment.StatusEnum.UNDER_MODIFICATION);

    eventProducer.accept(
        List.of(
            AppStatusUpdateRequested.builder()
                .userId(application.getUserId())
                .status(getStatus(statusEnum))
                .build()));
    return application;
  }

  public long countByUserIdAndArchived(String userId, boolean archived) {
    return repository.countByUserIdAndArchived(userId, archived);
  }

  private static AppStatusUpdateRequested.StatusAlteration getStatus(StatusEnum status) {
    return switch (status) {
      case ACTIVE -> ACTIVATE;
      case SUSPENDED -> SUSPEND;
      case UNKNOWN, UNDER_MODIFICATION -> throw new IllegalArgumentException();
    };
  }
}
