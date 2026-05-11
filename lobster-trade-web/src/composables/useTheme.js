import { useThemeStore } from '@/stores/theme'

export function useTheme() {
  const themeStore = useThemeStore()

  const themes = [
    { id: "净蓝商务", name: "净蓝商务", desc: "白底 · 沉稳蓝 · 橙色价格", preview: { primary: "#f8fafc", accent: "#2563eb", text: "#0f172a" } },
    { id: "暗灰游戏", name: "暗灰游戏", desc: "深蓝灰底 · 暖橙强调", preview: { primary: "#1e2430", accent: "#f97316", text: "#f1f5f9" } },
    { id: "轻橙活力", name: "轻橙活力", desc: "暖白底 · 深橙品牌", preview: { primary: "#fef7f0", accent: "#e65300", text: "#1a1a1a" } }
  ]

  function switchTheme(themeId) {
    themeStore.setTheme(themeId)
  }

  return { currentTheme: themeStore.currentTheme, themes, switchTheme }
}
