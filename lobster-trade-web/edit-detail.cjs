const fs = require('fs')
const path = 'C:\\Users\\Administrator\\.openclaw\\workspace\\projects\\龙虾道具交易平台\\code\\lobster-trade-web\\src\\views\\order\\Detail.vue'
const content = fs.readFileSync(path, 'utf8')

// Find the exact pattern in the template (after order progress list, before action buttons)
const marker = `            <!-- 操作按钮 -->`
const existingProgressSection = `            <!-- 代练进度（boost订单显示） -->`

if (content.includes(existingProgressSection)) {
  console.log('Already has progress section, skipping')
  process.exit(0)
}

const newSection = `
            <!-- 代练进度（boost订单显示） -->
            <div class="info-section" v-if="order.tradeType === 'boost' && progressData">
              <h3 class="section-title">代练进度</h3>
              <div class="progress-panel">
                <el-progress :percentage="progressData.progressPercent || 0" :color="progressColor(progressData.progressPercent)" />
                <p class="progress-note" v-if="progressData.progressNote">{{ progressData.progressNote }}</p>
                <p class="progress-time" v-if="progressData.sellerSubmitTime">更新时间：{{ formatTime(progressData.sellerSubmitTime) }}</p>
                <div class="progress-actions">
                  <el-button size="small" type="success" v-if="isBuyer && progressData.sellerSubmit && !progressData.buyerAck" @click="handleAckProgress">确认进度</el-button>
                  <el-tag v-if="progressData.buyerAck" type="success" size="small">买家已确认</el-tag>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->`

const updated = content.replace(marker, newSection)
if (updated === content) {
  console.error('Replacement did not happen!')
  process.exit(1)
}
fs.writeFileSync(path, updated)
console.log('Done')