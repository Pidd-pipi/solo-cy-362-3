package com.generated.ldmurdergame.model;

import java.util.List;

public record ScriptItem(
  Long id,
  String name,
  List<String> tags,
  String difficulty,
  Integer durationMinutes,
  Integer minPlayers,
  Integer maxPlayers,
  Long dmId,
  String dmName,
  Boolean published
) {
}
