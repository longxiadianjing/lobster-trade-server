import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref('净蓝商务')

  function setTheme(theme) {
    currentTheme.value = theme
    document.documentElement.setAttribute('data-theme', theme)
  }

  function initTheme() {
    // No-op for now - restore theme system later
    document.documentElement.setAttribute('data-theme', '净蓝商务')
  }

  return { currentTheme, setTheme, initTheme }
})
