package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.model.ScriptRow;
import com.generated.ldmurdergame.model.ScriptTagRow;

@Mapper
public interface ScriptMapper {

  @Select("""
    <script>
    SELECT s.id, s.name, s.difficulty, s.duration_minutes, s.min_players,
           s.max_players, s.dm_id, d.name AS dm_name, s.published
    FROM scripts s
    LEFT JOIN dms d ON d.id = s.dm_id
    <where>
      <if test="name != null and name != ''">
        AND LOWER(s.name) LIKE CONCAT('%', LOWER(#{name}), '%')
      </if>
      <if test="difficulty != null and difficulty != ''">
        AND s.difficulty = #{difficulty}
      </if>
      <if test="published != null">
        AND s.published = #{published}
      </if>
      <if test="tags != null and tags.size() > 0">
        AND
        <foreach collection="tags" item="tag" open="(" separator=" AND " close=")">
          EXISTS (
            SELECT 1 FROM script_tags st
            WHERE st.script_id = s.id AND st.tag = #{tag}
          )
        </foreach>
      </if>
    </where>
    ORDER BY s.id
    </script>
    """)
  List<ScriptRow> selectScripts(
    @Param("name") String name,
    @Param("tags") List<String> tags,
    @Param("difficulty") String difficulty,
    @Param("published") Boolean published);

  @Select("""
    SELECT script_id AS scriptId, tag
    FROM script_tags
    ORDER BY script_id, id
    """)
  List<ScriptTagRow> selectAllTags();

  @Select("SELECT DISTINCT tag FROM script_tags ORDER BY tag")
  List<String> selectDistinctTags();

  @Select("SELECT DISTINCT difficulty FROM scripts ORDER BY difficulty")
  List<String> selectDistinctDifficulties();
}
