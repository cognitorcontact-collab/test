package api.poja.io.service.pojaConfHandler;

import static api.poja.io.endpoint.rest.model.DeploymentStateEnum.CODE_PUSH_FAILED;
import static api.poja.io.endpoint.rest.model.DeploymentStateEnum.CODE_PUSH_IN_PROGRESS;
import static api.poja.io.endpoint.rest.model.DeploymentStateEnum.CODE_PUSH_SUCCESS;
import static api.poja.io.file.ExtendedBucketComponent.getOrgBucketKey;
import static api.poja.io.file.FileType.DEPLOYMENT_FILE;
import static api.poja.io.file.FileType.POJA_CONF;
import static api.poja.io.model.PojaVersion.*;
import static api.poja.io.service.event.PojaConfUploadedService.POJA_BOT_USERNAME;
import static java.lang.Boolean.FALSE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Locale.ROOT;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM;
import static org.eclipse.jgit.api.ResetCommand.ResetType.HARD;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.UP_TO_DATE;

import api.poja.io.endpoint.event.model.PojaConfUploaded;
import api.poja.io.file.ExtendedBucketComponent;
import api.poja.io.file.FileFinder;
import api.poja.io.file.FileUnzipper;
import api.poja.io.model.exception.InternalServerErrorException;
import api.poja.io.model.pojaConf.PojaConf;
import api.poja.io.model.pojaConf.conf1.PojaConf1;
import api.poja.io.model.pojaConf.conf1.PojaConf1.Integration;
import api.poja.io.model.pojaConf.conf2.PojaConf2;
import api.poja.io.model.pojaConf.conf3.PojaConf3;
import api.poja.io.model.pojaConf.conf6.PojaConf6;
import api.poja.io.repository.model.AppInstallation;
import api.poja.io.repository.model.Application;
import api.poja.io.repository.model.EnvDeploymentConf;
import api.poja.io.repository.model.Environment;
import api.poja.io.service.AppEnvironmentDeploymentService;
import api.poja.io.service.AppInstallationService;
import api.poja.io.service.ApplicationService;
import api.poja.io.service.EnvDeploymentConfService;
import api.poja.io.service.EnvironmentService;
import api.poja.io.service.api.pojaSam.PojaSamApi;
import api.poja.io.service.appEnvConfigurer.AppEnvConfigurerService;
import api.poja.io.service.github.GithubService;
import api.poja.io.service.github.model.GhWorkflowRunRequestBody;
import api.poja.io.service.workflows.DeploymentStateService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipFile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class PojaUploadedHandler extends AbstractPojaConfUploadedHandler {
  public static final String CD_COMPUTE_WORKFLOW_ID = "cd-compute.yml";
  private static final String MAIN_JAVA_SOURCE_PATH = "src/main/java/";
  private static final String GITHUB_WORKFLOWS_CD_SCHEDULER_YML =
      ".github/workflows/cd-scheduler.yml";
  private String userHome;

  @PostConstruct
  public void init() {
    userHome = System.getProperty("user.home");
    System.setProperty("user.home", "/tmp");
  }

  @PreDestroy
  public void destroy() {
    System.setProperty("user.home", userHome);
  }

  protected PojaUploadedHandler(
      ExtendedBucketComponent bucketComponent,
      PojaSamApi pojaSamApi,
      GithubService githubService,
      AppInstallationService appInstallService,
      AppEnvConfigurerService appEnvConfigurerService,
      EnvironmentService envService,
      ApplicationService appService,
      FileUnzipper unzipper,
      EnvDeploymentConfService envDeploymentConfService,
      DeploymentStateService deploymentStateService,
      AppEnvironmentDeploymentService appEnvironmentDeploymentService,
      FileFinder finder) {
    super(List.of(POJA_1, POJA_2, POJA_3, POJA_4, POJA_5, POJA_6), appEnvConfigurerService);
    this.bucketComponent = bucketComponent;
    this.pojaSamApi = pojaSamApi;
    this.githubService = githubService;
    this.appInstallService = appInstallService;
    this.envService = envService;
    this.appService = appService;
    this.unzipper = unzipper;
    this.envDeploymentConfService = envDeploymentConfService;
    this.deploymentStateService = deploymentStateService;
    this.appEnvironmentDeploymentService = appEnvironmentDeploymentService;
    this.finder = finder;
  }

  private static final String BUILD_TEMPLATE_FILENAME_YML = "template.yml";
  private static final String CF_STACKS_CD_COMPUTE_PERMISSION_YML_PATH =
      "cf-stacks/compute-permission-stack.yml";
  private static final String CF_STACKS_EVENT_STACK_YML_PATH = "cf-stacks/event-stack.yml";
  private static final String CF_STACKS_STORAGE_BUCKET_STACK_YML_PATH =
      "cf-stacks/storage-bucket-stack.yml";
  private static final String CF_STACKS_STORAGE_SQLITE_STACK_YML_PATH =
      "cf-stacks/storage-efs-stack.yml";
  private static final String CD_SCHEDULER_KEY_STACK_YML_PATH = "cf-stacks/scheduler-stack.yml";
  private static final String CD_COMPUTE_BUCKET_KEY = "poja-templates/cd-compute.yml";
  private static final String REMOTE_ORIGIN = "origin";
  private static final RefSpec FETCH_ALL_AND_UPDATE_REFSPEC =
      new RefSpec("+refs/heads/*:refs/heads/*");
  private final ExtendedBucketComponent bucketComponent;
  private final PojaSamApi pojaSamApi;
  private final GithubService githubService;
  private final AppInstallationService appInstallService;
  private final ApplicationService appService;
  private final EnvironmentService envService;
  private final FileUnzipper unzipper;
  private final EnvDeploymentConfService envDeploymentConfService;
  private final DeploymentStateService deploymentStateService;
  private final AppEnvironmentDeploymentService appEnvironmentDeploymentService;
  private final FileFinder finder;

  /**
   * uploads deployment files and saves config
   *
   * @return the saved config
   */
  private String uploadAndSaveDeploymentFiles(
      File toUnzip, PojaConfUploaded pojaConfUploaded, String appEnvDeploymentId) {
    var unzippedCode = createTempDir("unzipped");
    unzip(asZipFile(toUnzip), unzippedCode);
    var tempDirPath = createTempDir("deployment_files");
    EnvDeploymentConf savedConf =
        envDeploymentConfService.save(
            getEnvDeploymentConf(pojaConfUploaded, unzippedCode, tempDirPath));
    String confId = savedConf.getId();
    appEnvironmentDeploymentService.updateEnvDeploymentConf(appEnvDeploymentId, confId);
    return confId;
  }

  private EnvDeploymentConf getEnvDeploymentConf(
      PojaConfUploaded pojaConfUploaded, Path unzippedCode, Path tempDirPath) {
    UUID random = randomUUID();
    var optionalBuildTemplateFilename =
        copyToIfExists(
            unzippedCode, BUILD_TEMPLATE_FILENAME_YML, tempDirPath, "template" + random + ".yml");
    var optionalComputePermissionStackFilename =
        copyToIfExists(
            unzippedCode,
            CF_STACKS_CD_COMPUTE_PERMISSION_YML_PATH,
            tempDirPath,
            "compute-permission" + random + ".yml");
    var optionalEventStackFilename =
        copyToIfExists(
            unzippedCode,
            CF_STACKS_EVENT_STACK_YML_PATH,
            tempDirPath,
            "event-stack" + random + ".yml");
    var optionalStorageBucketStackFilename =
        copyToIfExists(
            unzippedCode,
            CF_STACKS_STORAGE_BUCKET_STACK_YML_PATH,
            tempDirPath,
            "storage-bucket-stack" + random + ".yml");
    var optionalStorageSqliteStackFilename =
        copyToIfExists(
            unzippedCode,
            CF_STACKS_STORAGE_SQLITE_STACK_YML_PATH,
            tempDirPath,
            "storage-efs-stack" + random + ".yml");
    var optionalSchedulerStackFilename =
        copyToIfExists(
            unzippedCode,
            CD_SCHEDULER_KEY_STACK_YML_PATH,
            tempDirPath,
            "scheduler" + random + ".yml");

    var environmentId = pojaConfUploaded.getEnvironmentId();
    bucketComponent.upload(
        tempDirPath.toFile(),
        getOrgBucketKey(
            pojaConfUploaded.getOrgId(),
            pojaConfUploaded.getAppId(),
            environmentId,
            DEPLOYMENT_FILE));
    return EnvDeploymentConf.builder()
        .id(pojaConfUploaded.getEnvDeplConfId())
        .envId(environmentId)
        .computePermissionStackFileKey(optionalComputePermissionStackFilename.orElseThrow())
        .storageBucketStackFileKey(optionalStorageBucketStackFilename.orElse(null))
        .eventStackFileKey(optionalEventStackFilename.orElse(null))
        .storageDatabaseSqliteStackFileKey(optionalStorageSqliteStackFilename.orElse(null))
        .eventSchedulerStackFileKey(optionalSchedulerStackFilename.orElse(null))
        .buildTemplateFile(optionalBuildTemplateFilename.orElseThrow())
        .creationDatetime(Instant.now())
        .pojaConfFileKey(pojaConfUploaded.getFilename())
        .build();
  }

  private static Optional<String> copyToIfExists(
      Path source, String originalFilePath, Path destination, String newFilename) {
    var originalFile = source.resolve(originalFilePath);
    var copySucceeded = copyFile(originalFile, destination, newFilename);
    if (!copySucceeded) {
      return empty();
    }
    return Optional.of(newFilename);
  }

  /**
   * copies a file from source to target with the new filename. if copy fails, it throws
   * RuntimeException if file does not exist, it returns false if file exists, it returns true the
   * boolean return value is used to handle non-existing file copies e.g: storage-bucket-stack does
   * not exist if with_file_storage is false in the code_gen conf, hence the need to check whether
   * the file exists or not on copy result
   */
  private static boolean copyFile(Path source, Path target, String newFilename) {
    if (source.toFile().exists()) {
      Path newFilenameResolved = target.resolve(newFilename);
      log.info("Copying {} to {}", source, newFilenameResolved);
      try {
        Files.move(source, newFilenameResolved, REPLACE_EXISTING);
      } catch (IOException e) {
        log.info("failed to copy");
        throw new RuntimeException(e);
      }
      return true;
    }
    log.info("file does not exist {}", source.toAbsolutePath());
    return false;
  }

  private static void checkoutAndCreateBranch(Git git, String branchName) {
    try {
      git.checkout().setCreateBranch(true).setName(branchName).setUpstreamMode(SET_UPSTREAM).call();
      log.info("successfully created and checked out branch {}", branchName);
    } catch (RefNotFoundException | RefAlreadyExistsException | InvalidRefNameException e) {
      // unreachable because we check for branch existence in remote first then create it with name
      // "PREPROD" or "PROD" which are very valid.
      log.info("RefException ", e);
    } catch (CheckoutConflictException e) {
      // unreachable because this function creates a branch from an existing branch via checkout
      // command, hence no conflict is possible
      log.info("checkoutConflictException", e);
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private static FetchResult fetchAllRefs(Git git, CredentialsProvider credentialsProvider) {
    try {
      return git.fetch()
          .setCredentialsProvider(credentialsProvider)
          .setRemote(REMOTE_ORIGIN)
          .setRefSpecs(FETCH_ALL_AND_UPDATE_REFSPEC)
          .call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private void pushAndCheckResult(
      CredentialsProvider ghCredentialsProvider, String branchName, Git git)
      throws GitAPIException {
    var results =
        git.push()
            .setRefSpecs(new RefSpec(getFormattedBranchName(branchName)))
            .setCredentialsProvider(ghCredentialsProvider)
            .call();
    for (PushResult r : results) {
      for (RemoteRefUpdate update : r.getRemoteUpdates()) {
        log.info("Having results: {}", update);
        if (update.getStatus() != OK && update.getStatus() != UP_TO_DATE) {
          String errorMessage = "Push failed: " + update.getStatus();
          throw new RuntimeException(errorMessage);
        }
      }
    }
  }

  private static String formatShortBranchName(Environment env) {
    return env.getEnvironmentType().name().toLowerCase(ROOT);
  }

  /**
   * pushes code to github
   *
   * @param ghCredentialsProvider
   * @param app
   * @param env
   * @param toUnzip
   * @param cloneDirPath
   * @param appEnvDeploymentId
   * @return true if changes were made, false otherwise, a change could be a new commit or a new
   *     branch
   */
  private boolean cloneAndApplyChanges(
      CredentialsProvider ghCredentialsProvider,
      PojaConf pojaConf,
      Application app,
      Environment env,
      File toUnzip,
      Path cloneDirPath,
      String appEnvDeploymentId) {
    String branchName = formatShortBranchName(env);
    try (Git git =
        Git.cloneRepository()
            .setCredentialsProvider(ghCredentialsProvider)
            .setDirectory(cloneDirPath.toFile())
            .setURI(app.getGithubRepositoryUrl())
            .setDepth(1)
            .setNoCheckout(true)
            .call()) {
      configureGitRepositoryGpg(git);
      var doesBranchExist = doesBranchExist(ghCredentialsProvider, git, branchName);
      checkoutBranch(doesBranchExist, git, branchName);
      log.info("successfully cloned in {}", cloneDirPath.toAbsolutePath());

      var hasApiSpec = hasApiSpec(cloneDirPath);
      unzip(asZipFile(toUnzip), cloneDirPath);
      getAndConfigureCdCompute(cloneDirPath);
      addExecutePermissionToFormat(cloneDirPath);

      boolean hasModifiedRepository =
          applyAndCommitChanges(
              ghCredentialsProvider,
              cloneDirPath,
              pojaConf,
              app,
              appEnvDeploymentId,
              git,
              hasApiSpec,
              branchName,
              doesBranchExist);
      if (hasModifiedRepository) return true;

      deploymentStateService.save(app.getId(), appEnvDeploymentId, CODE_PUSH_SUCCESS);
      return false;
    } catch (GitAPIException e) {
      deploymentStateService.save(app.getId(), appEnvDeploymentId, CODE_PUSH_FAILED);
      throw new RuntimeException(e);
    }
  }

  /**
   * needs to be used after cloning project and adding generated code will remove unnused files if
   * they got disabled by configuration will ignore Worker and File Storage files as their disabling
   * is not supported yet
   *
   * @param cloneDirPath path where the git project has been cloned
   * @param pojaConf the configuration to apply
   */
  void rmUnusedPreviouslyGeneratedFiles(Git git, Path cloneDirPath, PojaConf pojaConf) {
    switch (pojaConf.getVersion()) {
      case POJA_1 -> {
        PojaConf1 domain = (PojaConf1) pojaConf;
        Integration integration = domain.integration();
        rmIntegrationGeneratedFiles(git, cloneDirPath, integration);
      }
      case POJA_2 -> {
        var domain = (PojaConf2) pojaConf;
        rmIntegrationGeneratedFiles(git, cloneDirPath, domain.integration());
        List<PojaConf2.ScheduledTask> scheduledTasks = domain.scheduledTasks();
        if (scheduledTasks.isEmpty()) {
          gitRm(git, List.of(Path.of(GITHUB_WORKFLOWS_CD_SCHEDULER_YML)));
        }
        var previousHandlers =
            finder.apply(
                getPackageSourcePath(cloneDirPath, domain.general().packageFullName()),
                Set.of("MailboxEventHandler.java", "ApiEventHandler.java"));
        var handlers =
            previousHandlers.stream()
                .filter(f -> !f.getParent().getFileName().toString().equals("handler"))
                .toList();
        if (!handlers.isEmpty()) {
          gitRm(git, handlers.stream().map(cloneDirPath::relativize).toList());
        }
      }
      case POJA_3 -> {
        var domain = (PojaConf3) pojaConf;
        rmIntegrationGeneratedFiles(git, cloneDirPath, domain.integration());
        List<PojaConf2.ScheduledTask> scheduledTasks = domain.scheduledTasks();
        if (scheduledTasks.isEmpty()) {
          gitRm(git, List.of(Path.of(".github/workflows/cd-scheduler.yml")));
        }
        Path packageSourcePath =
            getPackageSourcePath(cloneDirPath, domain.general().packageFullName());
        Path handlerPath = packageSourcePath.resolve("handler");
        gitRm(git, List.of(handlerPath.resolve("model"), handlerPath.resolve("exceptionHandler")));
        var previousHandlers =
            finder.apply(
                packageSourcePath, Set.of("MailboxEventHandler.java", "ApiEventHandler.java"));
        var handlers =
            previousHandlers.stream()
                .filter(f -> !f.getParent().getFileName().toString().equals("handler"))
                .toList();
        if (!handlers.isEmpty()) {
          gitRm(git, handlers.stream().map(cloneDirPath::relativize).toList());
        }
      }
    }
  }

  private Path getPackageSourcePath(Path cloneDirPath, String packageName) {
    return cloneDirPath.resolve(
        Path.of(MAIN_JAVA_SOURCE_PATH + packageName.replaceAll("\\.", "/")));
  }

  private void rmIntegrationGeneratedFiles(Git git, Path cloneDirPath, Integration integration) {
    if (FALSE.equals(integration.withCodeql())) {
      gitRm(git, List.of(Path.of(".github/workflows/codeql.yml")));
    }
    if (FALSE.equals(integration.withSentry())) {
      var sentryConfs = finder.apply(cloneDirPath, Set.of("SentryConf.java"));
      if (!sentryConfs.isEmpty()) {
        gitRm(git, List.of(cloneDirPath.relativize(sentryConfs.getFirst())));
      }
    }
  }

  @SneakyThrows
  private static void gitRm(Git git, List<Path> toRemove) {
    var rm = git.rm();
    toRemove.forEach(f -> rm.addFilepattern(String.valueOf(f)));
    rm.call();
  }

  private boolean applyAndCommitChanges(
      CredentialsProvider ghCredentialsProvider,
      Path cloneDirPath,
      PojaConf pojaConf,
      Application app,
      String appEnvDeploymentId,
      Git git,
      boolean hasApiSpec,
      String branchName,
      boolean doesBranchExist)
      throws GitAPIException {
    rmUnusedPreviouslyGeneratedFiles(git, cloneDirPath, pojaConf);
    var hasUncommittedChanges = rmPojaWorkflowsAndAddChanges(git, hasApiSpec);
    if (hasUncommittedChanges) {
      boolean isCommitEmpty = false;
      unsignedCommitAsBot(
          git, "poja: deployment ID: " + appEnvDeploymentId, ghCredentialsProvider, isCommitEmpty);
      pushAndCheckResult(ghCredentialsProvider, branchName, git);
      deploymentStateService.save(app.getId(), appEnvDeploymentId, CODE_PUSH_SUCCESS);
      return true;
    }
    if (!doesBranchExist) {
      // empty commit because branch creation counts as a repo change, and it helps us create a new
      // deployment for the newly created branch
      boolean isCommitEmpty = true;
      unsignedCommitAsBot(
          git, "poja: deployment ID: " + appEnvDeploymentId, ghCredentialsProvider, isCommitEmpty);
      pushAndCheckResult(ghCredentialsProvider, branchName, git);
      deploymentStateService.save(app.getId(), appEnvDeploymentId, CODE_PUSH_SUCCESS);
      return true;
    }
    return false;
  }

  private static boolean doesBranchExist(
      CredentialsProvider ghCredentialsProvider, Git git, String branchName) {
    var fetchResult = fetchAllRefs(git, ghCredentialsProvider);
    return fetchResult.getAdvertisedRefs().stream()
        .anyMatch(ref -> ref.getName().equals(getFormattedBranchName(branchName)));
  }

  private static void checkoutBranch(boolean doesBranchExist, Git git, String branchName)
      throws GitAPIException {
    if (!doesBranchExist) {
      log.info("branch does not exist");
      checkoutAndCreateBranch(git, branchName);
    } else {
      git.reset().setRef(branchName).setMode(HARD).call(); // avoid CheckoutConflictException
      git.checkout().setUpstreamMode(SET_UPSTREAM).setName(branchName).call();
    }
  }

  private static void addExecutePermissionToFormat(Path cloneDirPath) {
    cloneDirPath.resolve("format.sh").toFile().setExecutable(true);
  }

  @SneakyThrows
  private static ZipFile asZipFile(File toUnzip) {
    return new ZipFile(toUnzip);
  }

  private void unzip(ZipFile downloaded, Path destination) {
    unzipper.apply(downloaded, destination);
  }

  private static String getFormattedBranchName(String branchName) {
    return "refs/heads/" + branchName;
  }

  /**
   * @param pojaConfUploaded
   * @param orgId
   * @param appId
   * @param environmentId
   * @return zip file but we do not use ZipFile type so we can reuse this
   */
  private File generateCodeFromPojaConf(
      PojaConfUploaded pojaConfUploaded, String orgId, String appId, String environmentId) {
    String formattedFilename =
        getOrgBucketKey(orgId, appId, environmentId, POJA_CONF, pojaConfUploaded.getFilename());
    log.info("downloading pojaConfFile: {}", formattedFilename);
    var pojaConfFile = bucketComponent.download(formattedFilename);
    log.info("downloaded pojaConfFile: {}", pojaConfFile.getName());
    return pojaSamApi.genCodeTo(pojaConfUploaded.getPojaVersion(), pojaConfFile);
  }

  /**
   * adds changes to git
   *
   * @param git
   * @param hasApiSpec
   * @return true if changes were made, false otherwise, a change is a modified(added, deleted, or
   *     altered) file other than those we don't track
   */
  private static boolean rmPojaWorkflowsAndAddChanges(Git git, boolean hasApiSpec) {
    try {
      if (hasApiSpec) {
        git.checkout().addPath("doc/api.yml").call();
      }
      git.add().addFilepattern(".").call();
      git.rm().addFilepattern("cf-stacks/").call();
      git.rm().addFilepattern("poja-custom-java-env-vars.txt").call();
      git.rm().addFilepattern("poja-custom-java-repositories.txt").call();
      git.rm().addFilepattern("poja-custom-java-deps.txt").call();
      git.rm().addFilepattern(BUILD_TEMPLATE_FILENAME_YML).call();
      git.rm().addFilepattern("poja.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-scheduler.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-compute-permission.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-event.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-storage-bucket.yml").call();
      git.rm().addFilepattern(".github/workflows/cd-storage-database.yml").call();
      git.rm().addFilepattern(".github/workflows/health-check-email.yml").call();
      git.rm().addFilepattern(".github/workflows/health-check-infra.yml").call();
      git.rm().addFilepattern(".github/workflows/health-check-poja.yml").call();
      Status status = git.status().call();
      return !status.isClean();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  private static void configureGitRepositoryGpg(Git git) {
    StoredConfig storedConfig = git.getRepository().getConfig();
    storedConfig.setString("gpg", null, "format", "openpgp");
  }

  private static void unsignedCommitAsBot(
      Git git, String commitMessage, CredentialsProvider credentialsProvider, boolean isEmpty) {
    PersonIdent author = new PersonIdent(POJA_BOT_USERNAME, "bot@poja.io");
    try {
      git.commit()
          .setMessage(commitMessage)
          .setAuthor(author)
          .setAllowEmpty(isEmpty)
          .setCommitter(author)
          .setCredentialsProvider(credentialsProvider)
          .setSign(false)
          .call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private String getInstallationToken(AppInstallation appInstallation, Duration tokenDuration) {
    return githubService.getInstallationToken(appInstallation.getGhId(), tokenDuration);
  }

  private void getAndConfigureCdCompute(Path clonedDirPath) {
    File rawCdComputeFile = bucketComponent.download(CD_COMPUTE_BUCKET_KEY);
    String placeHolder = "<?env>";
    Path ghWorkflowDir = Path.of(clonedDirPath + "/.github/workflows/cd-compute.yml");
    String env = System.getenv("ENV");
    try {
      Path rawFilePath = Paths.get(rawCdComputeFile.toURI());
      String fileContent = new String(Files.readAllBytes(rawFilePath));
      fileContent = fileContent.replace(placeHolder, env);
      Files.write(rawFilePath, fileContent.getBytes(), TRUNCATE_EXISTING);
      Files.copy(rawFilePath, ghWorkflowDir, REPLACE_EXISTING);
    } catch (IOException e) {
      log.error("Error occurred during configuration of cd-compute-file.");
      throw new InternalServerErrorException(e);
    }
  }

  @SneakyThrows
  private static Path createTempDir(String prefix) {
    return Files.createTempDirectory(prefix);
  }

  @Override
  public void applyConf(PojaConfUploaded pojaConfUploaded, PojaConf pojaConf) {
    String appId = pojaConfUploaded.getAppId();
    String envId = pojaConfUploaded.getEnvironmentId();
    var savedAppEnvDeploymentId = pojaConfUploaded.getAppEnvDeplId();
    var app = appService.getById(appId);
    var env = envService.getById(envId);
    var appInstallation = appInstallService.getById(app.getInstallationId());
    var appInstallationToken =
        getInstallationToken(appInstallation, pojaConfUploaded.maxConsumerDuration());
    var cloneDirPath = createTempDir("github_clone");
    CredentialsProvider ghCredentialsProvider =
        new UsernamePasswordCredentialsProvider("x-access-token", appInstallationToken);
    var generatedCode =
        generateCodeFromPojaConf(pojaConfUploaded, pojaConfUploaded.getOrgId(), appId, envId);

    if (pojaConf instanceof PojaConf6 pojaConf6) {
      var customEnvVars = pojaConf6.general().customJavaEnvVars();
      var environmentType = pojaConf6.general().environmentType();
      if (customEnvVars != null && !customEnvVars.isEmpty()) {
        log.info("Configuring GitHub secrets for repository {}", app.getGithubRepositoryName());
        try {
          githubService.configureRepositorySecrets(
              appInstallation.getOwnerGithubLogin(),
              app.getGithubRepositoryName(),
              appInstallationToken,
              customEnvVars,
              environmentType);
          log.info("Successfully configured GitHub repository secrets.");
        } catch (Exception e) {
          log.error(
              "Failed to configure GitHub repository secrets for {}/{}. Deployment will continue"
                  + " without these secrets. Error: {}",
              appInstallation.getOwnerGithubLogin(),
              app.getGithubRepositoryName(),
              e.getMessage(),
              e);
        }
      } else {
        log.info("No custom environment variables to configure for PojaConf5.");
      }
    }

    deploymentStateService.save(app.getId(), savedAppEnvDeploymentId, CODE_PUSH_IN_PROGRESS);
    var hasRepoBeenModified =
        cloneAndApplyChanges(
            ghCredentialsProvider,
            pojaConf,
            app,
            env,
            generatedCode,
            cloneDirPath,
            savedAppEnvDeploymentId);
    var savedConfId =
        uploadAndSaveDeploymentFiles(generatedCode, pojaConfUploaded, savedAppEnvDeploymentId);

    if (!hasRepoBeenModified) {
      githubService.runWorkflowDispatch(
          appInstallation.getOwnerGithubLogin(),
          app.getGithubRepositoryName(),
          appInstallationToken,
          CD_COMPUTE_WORKFLOW_ID,
          new GhWorkflowRunRequestBody(
              formatShortBranchName(env),
              Map.of("env_conf_id", savedConfId, "deployment_id", savedAppEnvDeploymentId)));
    }
  }

  private boolean hasApiSpec(Path directory) {
    return directory.resolve("doc/api.yml").toFile().exists();
  }
}
