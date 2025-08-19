package api.poja.io.endpoint.rest.controller;

import static api.poja.io.endpoint.rest.model.Application.StatusEnum.ACTIVE;
import static api.poja.io.endpoint.rest.model.Application.StatusEnum.SUSPENDED;
import static api.poja.io.model.RangedInstant.getRangedInstant;
import static java.util.Objects.requireNonNull;

import api.poja.io.endpoint.rest.mapper.ApplicationMapper;
import api.poja.io.endpoint.rest.mapper.BillingInfoMapper;
import api.poja.io.endpoint.rest.model.Application;
import api.poja.io.endpoint.rest.model.CrupdateApplicationsRequestBody;
import api.poja.io.endpoint.rest.model.CrupdateApplicationsResponse;
import api.poja.io.endpoint.rest.model.EnvBillingInfo;
import api.poja.io.endpoint.rest.model.MonthType;
import api.poja.io.endpoint.rest.model.PagedApplicationsResponse;
import api.poja.io.endpoint.rest.model.PagedUserApplicationsResponse;
import api.poja.io.endpoint.rest.model.UpdateAppStatusRequestBody;
import api.poja.io.model.BoundedPageSize;
import api.poja.io.model.PageFromOne;
import api.poja.io.model.RangedInstant;
import api.poja.io.model.page.Page;
import api.poja.io.service.ApplicationService;
import api.poja.io.service.BillingInfoService;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {
  private final ApplicationService applicationService;
  private final ApplicationMapper mapper;
  private final BillingInfoService billingInfoService;
  private final BillingInfoMapper billingInfoMapper;

  public ApplicationController(
      ApplicationService applicationService,
      @Qualifier("RestApplicationMapper") ApplicationMapper mapper,
      BillingInfoService billingInfoService,
      BillingInfoMapper billingInfoMapper) {
    this.applicationService = applicationService;
    this.mapper = mapper;
    this.billingInfoService = billingInfoService;
    this.billingInfoMapper = billingInfoMapper;
  }

  @PutMapping("/orgs/{orgId}/applications")
  public CrupdateApplicationsResponse crupdateApplications(
      @PathVariable String orgId, @RequestBody CrupdateApplicationsRequestBody toCrupdate) {
    var data = requireNonNull(toCrupdate.getData());
    var mappedData =
        applicationService.saveOrgApplications(orgId, data).stream().map(mapper::toRest).toList();
    return new CrupdateApplicationsResponse().data(mappedData);
  }

  @GetMapping("/orgs/{orgId}/applications")
  public PagedApplicationsResponse getApplications(
      @PathVariable String orgId,
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(required = false, defaultValue = "10", name = "page_size")
          BoundedPageSize pageSize,
      @RequestParam(required = false) String name) {
    var pagedData = applicationService.findAllByCriteria(orgId, name, page, pageSize);
    var mappedData = pagedData.data().stream().map(mapper::toRest).toList();
    return new PagedApplicationsResponse()
        .count(pagedData.count())
        .hasPrevious(pagedData.hasPrevious())
        .pageSize(pagedData.queryPageSize().getValue())
        .pageNumber(pagedData.queryPage().getValue())
        .data(mappedData);
  }

  @GetMapping("/users/{userId}/applications")
  public PagedUserApplicationsResponse getApplications(
      @PathVariable String userId,
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(name = "page_size", required = false, defaultValue = "10")
          BoundedPageSize pageSize) {
    var dto = applicationService.findAllByOrgsOwnedByUser(userId, page, pageSize);
    Page<Application> pagedData = dto.apps().map(mapper::toRest);
    return new PagedUserApplicationsResponse()
        .count(pagedData.count())
        .hasPrevious(pagedData.hasPrevious())
        .pageSize(pagedData.queryPageSize().getValue())
        .pageNumber(pagedData.queryPage().getValue())
        .userAppsCount(dto.userAppsNb())
        .data(pagedData.data().stream().toList());
  }

  @GetMapping("/orgs/{orgId}/applications/{applicationId}")
  public Application getApplicationById(
      @PathVariable String orgId, @PathVariable String applicationId) {
    return mapper.toRest(applicationService.getById(applicationId, orgId));
  }

  @PutMapping("/orgs/{orgId}/applications/{applicationId}/statuses")
  public Application updateAppStatus(
      @PathVariable String orgId,
      @PathVariable String applicationId,
      @RequestBody UpdateAppStatusRequestBody requestBody) {
    return mapper.toRest(
        applicationService.updateAppStatusAsync(
            orgId, applicationId, getStatusEnum(requireNonNull(requestBody.getAction()))));
  }

  private static Application.StatusEnum getStatusEnum(
      UpdateAppStatusRequestBody.ActionEnum action) {
    return switch (action) {
      case ACTIVATE -> ACTIVE;
      case SUSPEND -> SUSPENDED;
    };
  }

  @GetMapping("/orgs/{orgId}/applications/{applicationId}/billing")
  public List<EnvBillingInfo> getUserApplicationBillingInfo(
      @PathVariable String orgId,
      @PathVariable String applicationId,
      @RequestParam(required = false) Instant startTime,
      @RequestParam(required = false) Instant endTime,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) MonthType month) {
    RangedInstant datetimeRange = getRangedInstant(startTime, endTime, year, month);
    return billingInfoService
        .getOrgBillingInfoByApplication(orgId, applicationId, datetimeRange)
        .stream()
        .map(
            billingInfo ->
                billingInfoMapper.toEnvRest(
                    billingInfo, datetimeRange.start(), datetimeRange.end()))
        .toList();
  }
}
