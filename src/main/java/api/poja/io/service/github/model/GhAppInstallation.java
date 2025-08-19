package api.poja.io.service.github.model;

public record GhAppInstallation(
    long appId, String ownerGithubLogin, String type, String avatarUrl) {}
