package com.generated.ldmurdergame.model;

public record ScriptRow(
  Long id,
  String name,
  String difficulty,
  Integer durationMinutes,
  Integer minPlayers,
  Integer maxPlayers,
  Long dmId,
  String dmName,
  Boolean published
) {
}
