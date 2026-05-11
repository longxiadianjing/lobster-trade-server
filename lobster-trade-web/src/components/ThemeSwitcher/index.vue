<template>
  <div class="theme-switcher">
    <el-popover
      placement="bottom-start"
      :width="360"
      trigger="click"
      @show="visible = true"
      @hide="visible = false"
    >
      <template #reference>
        <div class="theme-trigger" :class="{ active: visible }">
          <span class="trigger-icon">&#127912;</span>
          <span class="trigger-text">主题</span>
          <span class="trigger-arrow" :class="{ rotated: visible }">▼</span>
        </div>
      </template>

      <div class="theme-panel">
        <div class="panel-header">
          <span class="panel-title">选择主题风格</span>
        </div>
        <div class="theme-list">
          <div
            v-for="theme in themes"
            :key="theme.id"
            class="theme-option"
            :class="{ active: currentTheme === theme.id }"
            @click="handleSelect(theme.id)"
          >
            <div class="theme-preview" :style="{ background: theme.preview.primary, borderColor: theme.preview.accent }">
              <div class="preview-accent" :style="{ background: theme.preview.accent }"></div>
              <div class="preview-text" :style="{ background: theme.preview.text }"></div>
            </div>
            <div class="theme-info">
              <div class="theme-name">{{ theme.name }}</div>
              <div class="theme-desc">{{ theme.desc }}</div>
            </div>
            <div class="theme-check" v-if="currentTheme === theme.id">✓</div>
          </div>
        </div>
        <div class="panel-footer">
          主题将自动保存，下次访问生效
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useTheme } from '@/composables/useTheme'

const { currentTheme, themes, switchTheme } = useTheme()
const visible = ref(false)

function handleSelect(themeId) {
  switchTheme(themeId)
  visible.value = false
}
</script>

<style scoped>
.theme-switcher { display: inline-block; }

.theme-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #fff;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}
.theme-trigger:hover, .theme-trigger.active {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}
.trigger-arrow { font-size: 10px; transition: transform 0.2s; }
.trigger-arrow.rotated { transform: rotate(180deg); }

.theme-panel { padding: 0; }
.panel-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}
.panel-title { font-size: 14px; font-weight: 600; color: var(--text-primary); }

.theme-list { padding: 8px 0; }

.theme-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-radius: 8px;
  margin: 0 8px;
}
.theme-option:hover { background: var(--bg-card-hover); }
.theme-option.active { background: rgba(255, 31, 75, 0.08); }

.theme-preview {
  width: 40px;
  height: 32px;
  border-radius: 6px;
  border: 2px solid;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 4px;
  gap: 3px;
}
.preview-accent { height: 6px; border-radius: 2px; width: 60%; }
.preview-text { height: 4px; border-radius: 2px; opacity: 0.4; width: 80%; }

.theme-info { flex: 1; min-width: 0; }
.theme-name { font-size: 13px; font-weight: 600; color: var(--text-primary); margin-bottom: 3px; }
.theme-desc { font-size: 11px; color: var(--text-secondary); line-height: 1.4; }

.theme-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--primary);
  color: white;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.panel-footer {
  padding: 10px 16px;
  border-top: 1px solid var(--border-color);
  font-size: 11px;
  color: var(--text-muted);
  text-align: center;
}
</style>