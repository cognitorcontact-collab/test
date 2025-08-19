package api.poja.io.endpoint.rest.mapper;

import api.poja.io.endpoint.rest.model.PojaVersion;
import org.springframework.stereotype.Component;

@Component
public class PojaVersionMapper {
  public PojaVersion toRest(api.poja.io.model.PojaVersion domain) {
    return new PojaVersion()
        .major(domain.getMajor())
        .minor(domain.getMinor())
        .patch(domain.getPatch())
        .humanReadableValue(domain.toHumanReadableValue());
  }
}
