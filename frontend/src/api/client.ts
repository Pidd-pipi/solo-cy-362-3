import { API_BASE_URL } from "../constants/app";
import type { OverviewResponse, ScriptFilters, ScriptsResponse } from "../types";

export async function fetchOverview(): Promise<OverviewResponse> {
  const response = await fetch(`${API_BASE_URL}/overview`, {
    headers: { Accept: "application/json" },
  });

  if (!response.ok) {
    throw new Error(`Overview request failed: ${response.status}`);
  }

  return response.json() as Promise<OverviewResponse>;
}

export async function fetchScripts(filters: ScriptFilters): Promise<ScriptsResponse> {
  const params = new URLSearchParams();
  const name = filters.name.trim();
  if (name) {
    params.set("name", name);
  }
  filters.tags.forEach((tag) => params.append("tag", tag));
  if (filters.difficulty) {
    params.set("difficulty", filters.difficulty);
  }
  if (filters.published !== null) {
    params.set("published", String(filters.published));
  }

  const query = params.toString();
  const response = await fetch(`${API_BASE_URL}/scripts${query ? `?${query}` : ""}`, {
    headers: { Accept: "application/json" },
  });

  if (!response.ok) {
    throw new Error(`Scripts request failed: ${response.status}`);
  }

  return response.json() as Promise<ScriptsResponse>;
}
