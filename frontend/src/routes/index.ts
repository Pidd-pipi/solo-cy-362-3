export const routes = [
  { path: "/", label: "运营总览", hash: "#/" },
  { path: "/scripts", label: "剧本库浏览", hash: "#/scripts" },
];

export function routeFromHash(hash: string): string {
  if (hash.startsWith("#/scripts")) {
    return "/scripts";
  }
  return "/";
}
