package com.generated.ldmurdergame.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.generated.ldmurdergame.model.ScriptItem;
import com.generated.ldmurdergame.model.ScriptsResponse;

/**
 * 剧本库筛选服务测试：真实 MyBatis + H2（自动执行 schema.sql / data.sql），
 * 直接断言服务返回结果，不启动 Web 页面，可重复运行（种子数据幂等）。
 */
@SpringBootTest
class ScriptServiceTest {

  private static final int TOTAL_SCRIPTS = 13;

  @Autowired
  private ScriptService scriptService;

  private static List<String> namesOf(ScriptsResponse response) {
    return response.items().stream().map(ScriptItem::name).toList();
  }

  @Nested
  @DisplayName("空白参数处理")
  class WhitespaceTests {

    @Test
    @DisplayName("难度前后带空格时按有效难度匹配")
    void difficultyWithSurroundingSpacesMatchesTrimmedValue() {
      ScriptsResponse response =
        scriptService.queryScripts(null, null, "  新手友好  ", null);

      assertThat(namesOf(response))
        .containsExactly("月下告白", "星河彼方", "那年夏天的风", "海岛奇遇记");
    }

    @Test
    @DisplayName("难度为纯空白时忽略该条件，返回全部剧本")
    void blankDifficultyIsIgnored() {
      ScriptsResponse response =
        scriptService.queryScripts(null, null, "    ", null);

      assertThat(response.total()).isEqualTo(TOTAL_SCRIPTS);
      assertThat(namesOf(response)).hasSize(TOTAL_SCRIPTS);
    }

    @Test
    @DisplayName("名称前后带空格时按修剪后的名称模糊匹配")
    void nameWithSurroundingSpacesMatchesTrimmedValue() {
      ScriptsResponse response =
        scriptService.queryScripts("  雾都迷案  ", null, null, null);

      assertThat(namesOf(response)).containsExactly("雾都迷案");
    }

    @Test
    @DisplayName("名称为纯空白时忽略该条件，返回全部剧本")
    void blankNameIsIgnored() {
      ScriptsResponse response =
        scriptService.queryScripts("   ", null, null, null);

      assertThat(response.total()).isEqualTo(TOTAL_SCRIPTS);
    }
  }

  @Nested
  @DisplayName("标签处理")
  class TagTests {

    @Test
    @DisplayName("空标签与空白标签被忽略，等价于不筛选标签")
    void emptyAndBlankTagsAreIgnored() {
      ScriptsResponse response = scriptService.queryScripts(
        null, Arrays.asList("", "   ", null), null, null);

      assertThat(response.total()).isEqualTo(TOTAL_SCRIPTS);
    }

    @Test
    @DisplayName("重复标签不改变结果")
    void duplicateTagsDoNotChangeResult() {
      ScriptsResponse once = scriptService.queryScripts(
        null, List.of("推理"), null, null);
      ScriptsResponse repeated = scriptService.queryScripts(
        null, Arrays.asList("推理", "推理", "推理"), null, null);

      assertThat(namesOf(repeated)).isEqualTo(namesOf(once));
      assertThat(namesOf(repeated))
        .containsExactly("雾都迷案", "消失的乘客", "古宅惊魂夜", "无声证词");
    }

    @Test
    @DisplayName("重复/空值/空白标签混在一起时只按有效标签过滤")
    void duplicateAndBlankTagsMixed() {
      ScriptsResponse response = scriptService.queryScripts(
        null, Arrays.asList("推理", "", "推理", "   ", null), null, null);

      assertThat(namesOf(response))
        .containsExactly("雾都迷案", "消失的乘客", "古宅惊魂夜", "无声证词");
    }

    @Test
    @DisplayName("多个标签需同时满足（AND）")
    void multipleTagsMustAllMatch() {
      ScriptsResponse response = scriptService.queryScripts(
        null, List.of("推理", "现代"), null, null);

      assertThat(namesOf(response)).containsExactly("消失的乘客", "无声证词");
      // 返回的每个剧本必须同时带两个标签
      assertThat(response.items())
        .allSatisfy(item -> assertThat(item.tags())
          .contains("推理", "现代"));
    }

    @Test
    @DisplayName("互斥标签组合返回空列表")
    void mutuallyExclusiveTagsReturnEmpty() {
      ScriptsResponse response = scriptService.queryScripts(
        null, List.of("推理", "情感"), null, null);

      assertThat(response.items()).isEmpty();
      assertThat(response.total()).isZero();
    }
  }

  @Nested
  @DisplayName("组合筛选与无匹配")
  class CombinedFilterTests {

    @Test
    @DisplayName("名称+多标签(含重复)+带空格难度+上架状态四条件同时生效")
    void fourConditionsCombined() {
      ScriptsResponse response = scriptService.queryScripts(
        "  雾  ",
        Arrays.asList("推理", "推理"),
        "  硬核烧脑  ",
        Boolean.TRUE);

      assertThat(response.total()).isEqualTo(1);
      ScriptItem only = response.items().get(0);
      assertThat(only.name()).isEqualTo("雾都迷案");
      assertThat(only.difficulty()).isEqualTo("硬核烧脑");
      assertThat(only.published()).isTrue();
      assertThat(only.tags()).contains("推理");
    }

    @Test
    @DisplayName("四条件组合中上架状态不满足时返回空")
    void fourConditionsCombinedNoMatch() {
      ScriptsResponse response = scriptService.queryScripts(
        "雾", List.of("推理"), "硬核烧脑", Boolean.FALSE);

      assertThat(response.items()).isEmpty();
    }

    @Test
    @DisplayName("无匹配名称返回空列表")
    void nonExistentNameReturnsEmpty() {
      ScriptsResponse response =
        scriptService.queryScripts("不存在的剧本名", null, null, null);

      assertThat(response.items()).isEmpty();
      assertThat(response.total()).isZero();
    }

    @Test
    @DisplayName("难度与上架状态矛盾时返回空")
    void difficultyAndPublishedConflictReturnsEmpty() {
      ScriptsResponse response =
        scriptService.queryScripts(null, null, "硬核烧脑", Boolean.FALSE);

      assertThat(response.items()).isEmpty();
    }
  }

  @Nested
  @DisplayName("结果内容与元数据")
  class ResultContentTests {

    @Test
    @DisplayName("无筛选返回全部剧本并携带时长、人数、关联DM与标签")
    void noFiltersReturnsAllWithFields() {
      ScriptsResponse response =
        scriptService.queryScripts(null, Collections.emptyList(), null, null);

      assertThat(response.total()).isEqualTo(TOTAL_SCRIPTS);
      ScriptItem first = response.items().get(0);
      assertThat(first.name()).isEqualTo("雾都迷案");
      assertThat(first.durationMinutes()).isEqualTo(300);
      assertThat(first.minPlayers()).isEqualTo(6);
      assertThat(first.maxPlayers()).isEqualTo(8);
      assertThat(first.dmName()).isEqualTo("林墨");
      assertThat(first.tags()).containsExactly("推理", "本格", "民国");
    }

    @Test
    @DisplayName("响应携带可选项：全量标签与按业务顺序排列的难度")
    void responseCarriesFilterOptions() {
      ScriptsResponse response =
        scriptService.queryScripts(null, null, null, null);

      assertThat(response.allTags())
        .contains("推理", "情感", "机制", "恐怖");
      assertThat(response.difficulties())
        .containsExactly("新手友好", "进阶挑战", "硬核烧脑");
    }

    @Test
    @DisplayName("仅按已下架状态筛选")
    void filterByUnpublished() {
      ScriptsResponse response =
        scriptService.queryScripts(null, null, null, Boolean.FALSE);

      assertThat(namesOf(response)).containsExactly("玩偶屋", "金陵旧梦");
      assertThat(response.items())
        .allSatisfy(item -> assertThat(item.published()).isFalse());
    }
  }
}
