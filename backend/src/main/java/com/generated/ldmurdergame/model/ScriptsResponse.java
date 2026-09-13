package com.generated.ldmurdergame.model;

import java.util.List;

public record ScriptsResponse(
  List<ScriptItem> items,
  int total,
  List<String> allTags,
  List<String> difficulties
) {
}
