package api.poja.io.service.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record UserConf(@Value("${max.users}") long maxUsersNb) {}
