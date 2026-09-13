package com.generated.ldmurdergame.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.generated.ldmurdergame.model.ScriptsResponse;
import com.generated.ldmurdergame.service.ScriptService;

@RestController
public class ScriptController {
  private final ScriptService scriptService;

  public ScriptController(ScriptService scriptService) {
    this.scriptService = scriptService;
  }

  @GetMapping({"/scripts", "/api/scripts"})
  public ScriptsResponse scripts(
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "tag", required = false) List<String> tags,
      @RequestParam(name = "difficulty", required = false) String difficulty,
      @RequestParam(name = "published", required = false) Boolean published) {
    return scriptService.queryScripts(name, tags, difficulty, published);
  }
}
