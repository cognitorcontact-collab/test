package api.poja.io.service;

import api.poja.io.model.PojaVersion;
import api.poja.io.repository.PojaVersionRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PojaVersionService {
  private final PojaVersionRepository repository;

  public List<PojaVersion> findAll() {
    return repository.findAll();
  }
}
