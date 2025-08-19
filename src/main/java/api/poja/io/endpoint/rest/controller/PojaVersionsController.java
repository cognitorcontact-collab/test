package api.poja.io.endpoint.rest.controller;

import api.poja.io.endpoint.rest.mapper.PojaVersionMapper;
import api.poja.io.endpoint.rest.model.PojaVersionsResponse;
import api.poja.io.service.PojaVersionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PojaVersionsController {
  private final PojaVersionService service;
  private final PojaVersionMapper mapper;

  @GetMapping("/poja-versions")
  public PojaVersionsResponse getPojaVersions() {
    var data = service.findAll().stream().map(mapper::toRest).toList();
    return new PojaVersionsResponse().data(data);
  }
}
