<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { fetchScripts } from "../api/client";
import type { ScriptFilters, ScriptItem, ScriptsResponse } from "../types";

const EMPTY_RESPONSE: ScriptsResponse = { items: [], total: 0, allTags: [], difficulties: [] };

const filters = ref<ScriptFilters>({ name: "", tags: [], difficulty: "", published: null });
const data = ref<ScriptsResponse>(EMPTY_RESPONSE);
const loading = ref(false);
const errorMessage = ref("");
const hasLoaded = ref(false);

let requestSeq = 0;
let nameTimer: ReturnType<typeof setTimeout> | undefined;

const tagOptions = computed(() => data.value.allTags);
const difficultyOptions = computed(() => data.value.difficulties);
const items = computed(() => data.value.items);

const hasActiveFilters = computed(() =>
  filters.value.name.trim() !== ""
  || filters.value.tags.length > 0
  || filters.value.difficulty !== ""
  || filters.value.published !== null
);

async function loadScripts() {
  const seq = ++requestSeq;
  loading.value = true;
  errorMessage.value = "";
  try {
    const response = await fetchScripts(filters.value);
    if (seq !== requestSeq) {
      return;
    }
    data.value = response;
    hasLoaded.value = true;
  } catch {
    if (seq !== requestSeq) {
      return;
    }
    errorMessage.value = "剧本数据加载失败，请检查后端服务后重试。";
  } finally {
    if (seq === requestSeq) {
      loading.value = false;
    }
  }
}

function onNameInput() {
  if (nameTimer) {
    clearTimeout(nameTimer);
  }
  nameTimer = setTimeout(loadScripts, 300);
}

function resetFilters() {
  filters.value = { name: "", tags: [], difficulty: "", published: null };
}

function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  if (hours > 0 && rest > 0) {
    return `${hours} 小时 ${rest} 分钟`;
  }
  if (hours > 0) {
    return `${hours} 小时`;
  }
  return `${rest} 分钟`;
}

function playerRange(row: ScriptItem): string {
  return row.minPlayers === row.maxPlayers
    ? `${row.minPlayers} 人`
    : `${row.minPlayers}-${row.maxPlayers} 人`;
}

function difficultyTagType(difficulty: string): "success" | "warning" | "danger" | "info" {
  if (difficulty === "新手友好") {
    return "success";
  }
  if (difficulty === "进阶挑战") {
    return "warning";
  }
  if (difficulty === "硬核烧脑") {
    return "danger";
  }
  return "info";
}

watch(
  () => [filters.value.tags, filters.value.difficulty, filters.value.published],
  loadScripts,
  { deep: true }
);

onMounted(loadScripts);

onUnmounted(() => {
  if (nameTimer) {
    clearTimeout(nameTimer);
  }
});
</script>

<template>
  <section class="work-panel script-library">
    <div class="panel-heading">
      <div>
        <h2>剧本库浏览</h2>
        <p class="panel-subtitle">门店人员可按名称、标签、难度和上架状态组合筛选，多标签需同时满足。</p>
      </div>
      <el-tag v-if="hasLoaded && !errorMessage" type="info" size="large" effect="plain">
        {{ loading ? "筛选中…" : `共 ${items.length} 个剧本` }}
      </el-tag>
    </div>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      class="error-alert"
    >
      <el-button type="primary" size="small" @click="loadScripts">重新加载</el-button>
    </el-alert>

    <div class="filter-bar" v-loading="loading && !hasLoaded">
      <el-input
        v-model="filters.name"
        class="filter-name"
        placeholder="按剧本名称搜索"
        clearable
        @input="onNameInput"
        @clear="loadScripts"
      />
      <el-select
        v-model="filters.tags"
        class="filter-tags"
        multiple
        collapse-tags
        collapse-tags-tooltip
        filterable
        placeholder="按标签筛选（多选同时满足）"
      >
        <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
      </el-select>
      <el-select
        v-model="filters.difficulty"
        class="filter-difficulty"
        clearable
        placeholder="全部难度"
      >
        <el-option
          v-for="level in difficultyOptions"
          :key="level"
          :label="level"
          :value="level"
        />
      </el-select>
      <el-radio-group v-model="filters.published" class="filter-published">
        <el-radio-button :value="null">全部状态</el-radio-button>
        <el-radio-button :value="true">已上架</el-radio-button>
        <el-radio-button :value="false">已下架</el-radio-button>
      </el-radio-group>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table
      :data="items"
      style="width: 100%"
      size="large"
      v-loading="loading"
      element-loading-text="正在加载剧本数据…"
      :empty-text="hasActiveFilters ? '' : '暂无剧本数据'"
    >
      <el-table-column prop="name" label="剧本名称" min-width="150">
        <template #default="{ row }">
          <span class="script-name">{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="标签" min-width="180">
        <template #default="{ row }">
          <template v-if="row.tags.length">
            <el-tag
              v-for="tag in row.tags"
              :key="tag"
              size="small"
              effect="plain"
              class="tag-chip"
            >
              {{ tag }}
            </el-tag>
          </template>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="难度" width="110">
        <template #default="{ row }">
          <el-tag :type="difficultyTagType(row.difficulty)" size="small">
            {{ row.difficulty }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时长" width="140">
        <template #default="{ row }">{{ formatDuration(row.durationMinutes) }}</template>
      </el-table-column>
      <el-table-column label="人数" width="100">
        <template #default="{ row }">{{ playerRange(row) }}</template>
      </el-table-column>
      <el-table-column label="关联DM" width="130">
        <template #default="{ row }">
          <span v-if="row.dmName">{{ row.dmName }}</span>
          <span v-else class="muted">暂未关联</span>
        </template>
      </el-table-column>
      <el-table-column label="上架状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.published ? 'success' : 'info'" size="small">
            {{ row.published ? "已上架" : "已下架" }}
          </el-tag>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty v-if="hasActiveFilters" description="没有符合当前筛选条件的剧本，请调整名称、标签、难度或上架状态后重试。">
          <el-button type="primary" plain @click="resetFilters">清除全部筛选</el-button>
        </el-empty>
      </template>
    </el-table>
  </section>
</template>
