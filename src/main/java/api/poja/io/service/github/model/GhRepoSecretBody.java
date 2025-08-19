package api.poja.io.service.github.model;

public record GhRepoSecretBody(String encrypted_value, String key_id) {}
