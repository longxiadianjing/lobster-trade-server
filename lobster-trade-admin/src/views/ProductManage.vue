<template>
  <div class="product-manage">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards" v-loading="statsLoading">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon blue"><el-icon :size="22"><Goods /></el-icon></div>
            <div class="stat-text">
              <div class="stat-num">{{ stats.total }}</div>
              <div class="stat-label">商品总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon green"><el-icon :size="22"><CircleCheck /></el-icon></div>
            <div class="stat-text">
              <div class="stat-num">{{ stats.online }}</div>
              <div class="stat-label">上架中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon yellow"><el-icon :size="22"><Bottom /></el-icon></div>
            <div class="stat-text">
              <div class="stat-num">{{ stats.offline }}</div>
              <div class="stat-label">已下架</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon red"><el-icon :size="22"><Warning /></el-icon></div>
            <div class="stat-text">
              <div class="stat-num">{{ stats.banned }}</div>
              <div class="stat-label">违规封禁</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">商品列表</span>
          <div class="filter-row">
            <el-input v-model="keyword" placeholder="搜索商品名称" style="width: 200px;" clearable @keyup.enter="loadProducts">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="filterStatus" placeholder="全部状态" style="width: 120px;" clearable @change="loadProducts">
              <el-option label="全部状态" value="" />
              <el-option label="上架" value="1" />
              <el-option label="下架" value="2" />
              <el-option label="违规封禁" value="3" />
            </el-select>
            <el-button type="primary" @click="loadProducts">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button type="success" @click="batchOn" :disabled="selectedRows.length === 0">批量上架</el-button>
            <el-button type="warning" @click="batchOff" :disabled="selectedRows.length === 0">批量下架</el-button>
            <el-button type="danger" @click="batchBan" :disabled="selectedRows.length === 0">批量封禁</el-button>
          </div>
        </div>
      </template>

      <el-table :data="products" v-loading="loading" stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="40" />
        <el-table-column label="商品信息" min-width="280">
          <template #default="{ row }">
            <div class="product-cell">
              <div class="thumb-wrap">
                <img v-if="row.coverImage" :src="row.coverImage" class="product-thumb" @error="e => e.target.style.display='none'" />
                <div v-else class="product-thumb placeholder"><el-icon><Picture /></el-icon></div>
                <span class="type-dot" :class="row.productType"></span>
              </div>
              <div class="product-info">
                <div class="product-title" :title="row.title">{{ row.title }}</div>
                <div class="product-sub">
                  <el-tag size="small" type="info">{{ typeMap[row.productType] || row.productType }}</el-tag>
                  <span class="game-name">{{ row.gameName || '未知游戏' }}</span>
                  <span v-if="row.gameZone" class="zone-text">{{ row.gameZone }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="130">
          <template #default="{ row }">
            <div class="price-cell">
              <span class="price-num">¥{{ row.price }}</span>
              <span v-if="row.unit" class="price-unit">/{{ row.unit }}</span>
            </div>
            <div class="price-type-tag">{{ priceTypeMap[row.priceType] || row.priceType }}</div>
          </template>
        </el-table-column>
        <el-table-column label="库存/销量" width="110" align="center">
          <template #default="{ row }">
            <div class="stock-cell">
              <span class="stock-num">{{ row.stock ?? 0 }}</span>
              <span class="stock-label">库存</span>
            </div>
            <div class="sales-text">{{ row.totalOrders ?? 0 }} 笔销量</div>
          </template>
        </el-table-column>
        <el-table-column label="浏览/收藏" width="100" align="center">
          <template #default="{ row }">
            <div class="stat-text-sm">
              <div><span class="stat-label-inline">浏览</span> {{ row.viewCount ?? 0 }}</div>
              <div><span class="stat-label-inline">收藏</span> {{ row.favoriteCount ?? 0 }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="卖家" width="130">
          <template #default="{ row }">
            <div class="seller-cell">
              <el-avatar :size="24">{{ (row.sellerNickname || '匿名').charAt(0) }}</el-avatar>
              <div class="seller-info">
                <div class="seller-name">{{ row.sellerNickname || '匿名' }}</div>
                <div class="seller-phone">{{ row.sellerPhone ? row.sellerPhone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="95" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]" size="small" class="status-tag">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="140" align="center">
          <template #default="{ row }">
            <div class="time-cell">{{ formatTime(row.createTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button size="small" type="primary" plain @click="viewDetail(row)">详情</el-button>
              <el-button size="small" type="info" plain @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="warning" plain v-if="row.status === 1" @click="handleOff(row)">下架</el-button>
              <el-button size="small" type="success" plain v-if="row.status === 2" @click="handleOn(row)">上架</el-button>
              <el-button size="small" type="danger" plain v-if="row.status !== 3" @click="handleBan(row)">封禁</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <span class="total-hint">共 {{ pagination.total }} 条</span>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next"
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>
    </el-card>

    <!-- 商品详情 -->
    <el-dialog v-model="detailVisible" title="商品详情" width="720px" destroy-on-close>
      <div v-if="detail" class="detail-content">
        <div class="detail-layout">
          <div class="detail-pics">
            <img v-if="detailMainImg" :src="detailMainImg" class="detail-main-pic" @error="e => e.target.style.display='none'" />
            <div v-else class="detail-main-pic placeholder"><el-icon :size="48"><Picture /></el-icon></div>
            <div v-if="detailImages.length > 1" class="detail-thumbs">
              <img v-for="(img, idx) in detailImages" :key="idx" :src="img" class="detail-thumb" :class="{ active: detailMainImg === img }" @click="detailMainImg = img" @error="e => e.target.style.display='none'" />
            </div>
          </div>
          <div class="detail-info-panel">
            <div class="detail-title">{{ detail.title }}</div>
            <div class="detail-price-row">
              <span class="detail-price">¥{{ detail.price }}</span>
              <span v-if="detail.unit" class="detail-unit">/{{ detail.unit }}</span>
              <el-tag size="small" type="info">{{ priceTypeMap[detail.priceType] }}</el-tag>
            </div>
            <el-descriptions :column="2" size="small" border class="detail-desc-table">
              <el-descriptions-item label="游戏">{{ detail.gameName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="区服">{{ detail.gameZone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="服务器">{{ detail.server || '-' }}</el-descriptions-item>
              <el-descriptions-item label="平台">{{ detail.platform || '-' }}</el-descriptions-item>
              <el-descriptions-item label="库存">{{ detail.stock ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="销量">{{ detail.totalOrders ?? 0 }} 笔</el-descriptions-item>
              <el-descriptions-item label="浏览">{{ detail.viewCount ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="收藏">{{ detail.favoriteCount ?? 0 }}</el-descriptions-item>
            </el-descriptions>
            <div class="detail-section-label">商品描述</div>
            <div class="detail-desc-text">{{ detail.description || '暂无详细描述' }}</div>
            <div class="detail-section-label" style="margin-top:16px">卖家信息</div>
            <div class="seller-detail-row">
              <el-avatar :size="36">{{ (detail.sellerNickname || '匿名').charAt(0) }}</el-avatar>
              <div>
                <div class="seller-name-d">{{ detail.sellerNickname || '匿名用户' }}</div>
                <div class="seller-phone-d">{{ detail.sellerPhone || '-' }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="detailImages.length > 0" class="detail-all-pics">
          <div class="detail-section-label">全部图片 ({{ detailImages.length }})</div>
          <div class="all-pics-row">
            <img v-for="(img, idx) in detailImages" :key="idx" :src="img" class="all-pic" @error="e => e.target.style.display='none'" />
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 商品编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑商品" width="560px" destroy-on-close>
      <el-form :model="editForm" label-width="85px" v-loading="editLoading">
        <el-form-item label="商品标题">
          <el-input v-model="editForm.title" placeholder="商品标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="商品详细描述" maxlength="500" show-word-limit />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="价格">
              <el-input-number v-model="editForm.price" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="editForm.unit" placeholder="如：万、局、个" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="库存">
              <el-input-number v-model="editForm.stock" :min="0" :max="99999" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最小定金">
              <el-input-number v-model="editForm.minDeposit" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="价格类型">
              <el-select v-model="editForm.priceType" style="width:100%">
                <el-option label="一口价" value="fixed" />
                <el-option label="每万" value="per_wan" />
                <el-option label="每小时" value="per_hour" />
                <el-option label="每局" value="per_game" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品状态">
              <el-select v-model="editForm.status" style="width:100%">
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="2" />
                <el-option label="违规封禁" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="区服">
              <el-input v-model="editForm.gameZone" placeholder="区服" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务器">
              <el-input v-model="editForm.server" placeholder="服务器" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="平台">
          <el-input v-model="editForm.platform" placeholder="如：PC、移动端、全平台" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="doEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Location, Search, Goods, CircleCheck, Bottom, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const statsLoading = ref(false)
const products = ref([])
const keyword = ref('')
const filterStatus = ref('')
const selectedRows = ref([])

const onSelectionChange = (rows) => { selectedRows.value = rows }
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })
const stats = reactive({ total: 0, online: 0, offline: 0, banned: 0 })

const detailVisible = ref(false)
const detail = ref(null)
const detailMainImg = ref('')
const detailImages = computed(() => {
  if (!detail.value || !detail.value.images) return []
  try { return JSON.parse(detail.value.images) } catch { return [] }
})

const editVisible = ref(false)
const editLoading = ref(false)
const editId = ref(null)
const editForm = reactive({
  title: '', description: '', price: null, unit: '', priceType: '',
  gameZone: '', server: '', platform: '', minDeposit: null,
  stock: null, estimatedHours: null, status: null
})

const statusMap = { 1: '上架', 2: '下架', 3: '违规封禁' }
const statusTypeMap = { 1: 'success', 2: 'warning', 3: 'danger' }
const typeMap = { goods: '游戏币', equipment: '装备', boosting: '代练', accompany: '陪同', escort: '护送' }
const priceTypeMap = { fixed: '一口价', per_wan: '每万', per_hour: '每小时', per_game: '每局' }

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').slice(0, 16)
}

const loadStats = async () => {
  statsLoading.value = true
  try {
    const res = await request.get('/admin/products', { params: { page: 1, pageSize: 1, status: '' } })
    const resOn = await request.get('/admin/products', { params: { page: 1, pageSize: 1, status: '1' } })
    const resOff = await request.get('/admin/products', { params: { page: 1, pageSize: 1, status: '2' } })
    const resBan = await request.get('/admin/products', { params: { page: 1, pageSize: 1, status: '3' } })
    stats.total = res.data?.total || 0
    stats.online = resOn.data?.total || 0
    stats.offline = resOff.data?.total || 0
    stats.banned = resBan.data?.total || 0
  } catch (e) { console.error(e) } finally { statsLoading.value = false }
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/products', {
      params: { page: pagination.page, pageSize: pagination.pageSize, status: filterStatus.value, keyword: keyword.value }
    })
    if (res.data) {
      products.value = res.data.records || res.data
      pagination.total = res.data.total || 0
    }
  } catch (e) { console.error(e); ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleOff = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要下架商品【${row.title}】吗？`,
      '确认下架',
      { confirmButtonText: '确认下架', cancelButtonText: '取消', type: 'warning' }
    )
    await request.post(`/admin/product/${row.id}/off`)
    ElMessage.success('已下架')
    loadProducts()
  } catch (e) {}
}
const handleOn = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要上架商品【${row.title}】吗？`,
      '确认上架',
      { confirmButtonText: '确认上架', cancelButtonText: '取消', type: 'info' }
    )
    await request.post(`/admin/product/${row.id}/on`)
    ElMessage.success('已上架')
    loadProducts()
  } catch (e) {}
}
const handleBan = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要封禁商品【${row.title}】吗？封禁后商品将无法展示。`,
      '⚠️ 确认封禁',
      { confirmButtonText: '确认封禁', cancelButtonText: '取消', type: 'danger' }
    )
    await request.post(`/admin/product/${row.id}/ban`)
    ElMessage.success('已封禁')
    loadProducts()
  } catch (e) {}
}

const batchOn = async () => {
  const rows = selectedRows.value.filter(r => r.status !== 1)
  if (rows.length === 0) { ElMessage.warning('没有可上架的商品'); return }
  try {
    await ElMessageBox.confirm(
      `确定要批量上架 ${rows.length} 个商品吗？`,
      '确认批量上架',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
    await Promise.all(rows.map(r => request.post(`/admin/product/${r.id}/on`)))
    ElMessage.success(`成功上架 ${rows.length} 个商品`)
    loadProducts()
  } catch (e) {}
}

const batchOff = async () => {
  const rows = selectedRows.value.filter(r => r.status !== 2)
  if (rows.length === 0) { ElMessage.warning('没有可下架的商品'); return }
  try {
    await ElMessageBox.confirm(
      `确定要批量下架 ${rows.length} 个商品吗？`,
      '⚠️ 确认批量下架',
      { confirmButtonText: '确认下架', cancelButtonText: '取消', type: 'warning' }
    )
    await Promise.all(rows.map(r => request.post(`/admin/product/${r.id}/off`)))
    ElMessage.success(`成功下架 ${rows.length} 个商品`)
    loadProducts()
  } catch (e) {}
}

const batchBan = async () => {
  const rows = selectedRows.value.filter(r => r.status !== 3)
  if (rows.length === 0) { ElMessage.warning('没有可封禁的商品'); return }
  try {
    await ElMessageBox.confirm(
      `确定要批量封禁 ${rows.length} 个商品吗？此操作不可恢复。`,
      '⚠️ 确认批量封禁',
      { confirmButtonText: '确认封禁', cancelButtonText: '取消', type: 'danger' }
    )
    await Promise.all(rows.map(r => request.post(`/admin/product/${r.id}/ban`)))
    ElMessage.success(`成功封禁 ${rows.length} 个商品`)
    loadProducts()
  } catch (e) {}
}

const viewDetail = (row) => {
  detail.value = row
  detailMainImg.value = row.coverImage || (detailImages.value[0]) || ''
  detailVisible.value = true
}

const handleEdit = (row) => {
  editId.value = row.id
  editForm.title = row.title || ''
  editForm.description = row.description || ''
  editForm.price = row.price
  editForm.unit = row.unit || ''
  editForm.priceType = row.priceType || ''
  editForm.gameZone = row.gameZone || ''
  editForm.server = row.server || ''
  editForm.platform = row.platform || ''
  editForm.minDeposit = row.minDeposit
  editForm.stock = row.stock
  editForm.estimatedHours = row.estimatedHours
  editForm.status = row.status
  editVisible.value = true
}

const doEdit = async () => {
  if (!editId.value) { ElMessage.error('商品ID无效'); return }
  editLoading.value = true
  try {
    await request.put(`/admin/product/${editId.value}`, editForm)
    ElMessage.success('保存成功')
    editVisible.value = false
    loadProducts()
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    editLoading.value = false
  }
}

onMounted(() => { loadProducts(); loadStats() })
</script>

<style scoped>
/* 统计卡片 */
.stat-cards { margin-bottom: 16px; }
.stat-card { border: none; background: #fff; }
.stat-inner { display: flex; align-items: center; gap: 14px; }
.stat-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-icon.blue { background: #e8f0fe; color: #667eea; }
.stat-icon.green { background: #e8f8f0; color: #52c41a; }
.stat-icon.yellow { background: #fff8e8; color: #faad14; }
.stat-icon.red { background: #fff0f0; color: #ff4d4f; }
.stat-text { flex: 1; }
.stat-num { font-size: 22px; font-weight: 700; color: #333; line-height: 1.2; }
.stat-label { font-size: 12px; color: #999; margin-top: 2px; }

/* 卡片头部 */
.table-card { border: none; }
.card-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.filter-row { display: flex; gap: 10px; align-items: center; }

/* 商品单元格 */
.product-cell { display: flex; align-items: center; gap: 10px; }
.thumb-wrap { position: relative; flex-shrink: 0; }
.product-thumb { width: 52px; height: 52px; border-radius: 8px; object-fit: cover; background: #f5f5f5; }
.product-thumb.placeholder { display: flex; align-items: center; justify-content: center; color: #ccc; }
.type-dot { position: absolute; bottom: 3px; right: 3px; width: 8px; height: 8px; border-radius: 50%; border: 1.5px solid #fff; }
.type-dot.goods { background: #667eea; }
.type-dot.equipment { background: #52c41a; }
.type-dot.boosting { background: #faad14; }
.type-dot.accompany { background: #722ed1; }
.type-dot.escort { background: #13c2c2; }
.product-info { flex: 1; min-width: 0; }
.product-title { font-size: 13px; font-weight: 500; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 200px; margin-bottom: 5px; }
.product-sub { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.game-name { font-size: 12px; color: #667eea; font-weight: 500; }
.zone-text { font-size: 12px; color: #999; }

/* 价格 */
.price-cell { display: flex; align-items: baseline; gap: 2px; }
.price-num { font-size: 15px; font-weight: 700; color: #f56c6c; }
.price-unit { font-size: 11px; color: #999; }
.price-type-tag { font-size: 11px; color: #999; margin-top: 2px; }

/* 库存 */
.stock-cell { display: flex; align-items: baseline; gap: 3px; justify-content: center; }
.stock-num { font-size: 15px; font-weight: 700; color: #333; }
.stock-label { font-size: 11px; color: #999; }
.sales-text { font-size: 11px; color: #999; margin-top: 2px; }

/* 统计小字 */
.stat-text-sm { font-size: 12px; color: #666; }
.stat-label-inline { color: #999; }

/* 卖家 */
.seller-cell { display: flex; align-items: center; gap: 8px; }
.seller-name { font-size: 13px; font-weight: 500; color: #333; }
.seller-phone { font-size: 11px; color: #999; margin-top: 1px; }

/* 状态 */
.status-tag { font-size: 12px; }

/* 时间 */
.time-cell { font-size: 12px; color: #999; }

/* 操作 */
.action-cell { display: flex; gap: 4px; flex-wrap: nowrap; justify-content: center; }
.action-cell .el-button { padding: 4px 8px; font-size: 12px; }

/* 分页 */
.pagination-wrap { margin-top: 16px; display: flex; align-items: center; justify-content: space-between; }
.total-hint { font-size: 13px; color: #999; }

/* 详情弹窗 */
.detail-content { display: flex; flex-direction: column; gap: 20px; }
.detail-layout { display: flex; gap: 24px; }
.detail-pics { width: 300px; flex-shrink: 0; display: flex; flex-direction: column; gap: 8px; }
.detail-main-pic { width: 300px; height: 220px; object-fit: cover; border-radius: 10px; background: #f5f5f5; }
.detail-main-pic.placeholder { display: flex; align-items: center; justify-content: center; color: #ccc; }
.detail-thumbs { display: flex; gap: 6px; flex-wrap: wrap; }
.detail-thumb { width: 58px; height: 58px; object-fit: cover; border-radius: 6px; cursor: pointer; border: 2px solid transparent; }
.detail-thumb.active { border-color: #667eea; }
.detail-info-panel { flex: 1; min-width: 0; }
.detail-title { font-size: 17px; font-weight: 600; color: #333; margin-bottom: 10px; }
.detail-price-row { display: flex; align-items: baseline; gap: 6px; margin-bottom: 12px; }
.detail-price { font-size: 26px; font-weight: 800; color: #f56c6c; }
.detail-unit { font-size: 13px; color: #999; }
.detail-desc-table { margin-bottom: 12px; }
.detail-section-label { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 8px; }
.detail-desc-text { font-size: 13px; color: #666; line-height: 1.8; white-space: pre-wrap; }
.seller-detail-row { display: flex; align-items: center; gap: 10px; }
.seller-name-d { font-size: 14px; font-weight: 500; color: #333; }
.seller-phone-d { font-size: 12px; color: #999; margin-top: 2px; }
.detail-all-pics { border-top: 1px solid #f0f0f0; padding-top: 16px; }
.all-pics-row { display: flex; gap: 8px; flex-wrap: wrap; }
.all-pic { width: 80px; height: 80px; object-fit: cover; border-radius: 6px; cursor: pointer; }
</style>
