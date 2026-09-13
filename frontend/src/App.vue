<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import { APP_CODE, APP_NAME } from "./constants/app";
import { REQUEST_MESSAGES } from "./constants/messages";
import { routeFromHash, routes } from "./routes";
import OverviewView from "./views/OverviewView.vue";
import ScriptLibraryView from "./views/ScriptLibraryView.vue";

const activePath = ref(routeFromHash(window.location.hash));

function syncPath() {
  activePath.value = routeFromHash(window.location.hash);
}

function go(path: string) {
  window.location.hash = path === "/" ? "#/" : `#${path}`;
}

function goHealth() {
  window.location.href = REQUEST_MESSAGES.healthPath;
}

onMounted(() => window.addEventListener("hashchange", syncPath));
onBeforeUnmount(() => window.removeEventListener("hashchange", syncPath));
</script>

<template>
  <main class="app-shell">
    <header class="topbar">
      <div>
        <span class="brand-code">{{ APP_CODE }}</span>
        <h1 class="brand-title">{{ APP_NAME }}</h1>
      </div>
      <nav class="topnav">
        <button
          v-for="route in routes"
          :key="route.path"
          type="button"
          class="nav-item"
          :class="{ 'is-active': activePath === route.path }"
          @click="go(route.path)"
        >
          {{ route.label }}
        </button>
        <el-button type="primary" @click="goHealth">API Health</el-button>
      </nav>
    </header>
    <OverviewView v-if="activePath === '/'" />
    <section v-else class="workspace">
      <ScriptLibraryView />
    </section>
  </main>
</template>

<style scoped>
.topnav {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.nav-item {
  appearance: none;
  border: 1px solid color-mix(in srgb, #19212e 18%, transparent);
  background: transparent;
  color: #19212e;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.nav-item:hover {
  border-color: #3268b8;
  color: #3268b8;
}

.nav-item.is-active {
  background: #3268b8;
  border-color: #3268b8;
  color: #fff;
}
</style>
