package com.generated.ldmurdergame.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.generated.ldmurdergame.mapper.ScriptMapper;
import com.generated.ldmurdergame.model.ScriptItem;
import com.generated.ldmurdergame.model.ScriptRow;
import com.generated.ldmurdergame.model.ScriptTagRow;
import com.generated.ldmurdergame.model.ScriptsResponse;

@Service
public class ScriptService {
  // 难度按业务顺序展示，不直接依赖数据库排序
  private static final List<String> DIFFICULTY_ORDER =
    List.of("新手友好", "进阶挑战", "硬核烧脑");

  private final ScriptMapper scriptMapper;

  public ScriptService(ScriptMapper scriptMapper) {
    this.scriptMapper = scriptMapper;
  }

  public ScriptsResponse queryScripts(String name, List<String> tags,
      String difficulty, Boolean published) {
    List<ScriptRow> rows = scriptMapper.selectScripts(
      name == null ? null : name.trim(),
      normalizeTags(tags),
      difficulty,
      published);

    Map<Long, List<String>> tagsByScript = scriptMapper.selectAllTags().stream()
      .collect(Collectors.groupingBy(ScriptTagRow::scriptId,
        LinkedHashMap::new,
        Collectors.mapping(ScriptTagRow::tag, Collectors.toList())));

    List<ScriptItem> items = rows.stream()
      .map(row -> new ScriptItem(
        row.id(),
        row.name(),
        tagsByScript.getOrDefault(row.id(), List.of()),
        row.difficulty(),
        row.durationMinutes(),
        row.minPlayers(),
        row.maxPlayers(),
        row.dmId(),
        row.dmName(),
        row.published()))
      .toList();

    return new ScriptsResponse(
      items,
      items.size(),
      scriptMapper.selectDistinctTags(),
      orderedDifficulties(scriptMapper.selectDistinctDifficulties()));
  }

  private List<String> normalizeTags(List<String> tags) {
    if (tags == null) {
      return List.of();
    }
    return tags.stream()
      .filter(tag -> tag != null && !tag.isBlank())
      .map(String::trim)
      .distinct()
      .toList();
  }

  private List<String> orderedDifficulties(List<String> found) {
    List<String> ordered = new ArrayList<>();
    for (String level : DIFFICULTY_ORDER) {
      if (found.contains(level)) {
        ordered.add(level);
      }
    }
    // 兜底：数据库中若出现未收录的难度，仍展示给前端
    found.stream().filter(level -> !ordered.contains(level)).forEach(ordered::add);
    return ordered;
  }
}
