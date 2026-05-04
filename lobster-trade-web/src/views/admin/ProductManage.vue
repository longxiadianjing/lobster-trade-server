<template>
  <div class="product-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <el-input v-model="keyword" placeholder="搜索商品名称" style="width:200px" clearable @keyup.enter="loadProducts">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>
      <el-table :data="products" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sellerId" label="卖家ID" width="90" />
        <el-table-column prop="gameName" label="游戏" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="totalOrders" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" v-if="row.status !== 1" @click="handleOn(row.id)">上架</el-button>
            <el-button size="small" v-else @click="handleOff(row.id)">下架</el-button>
            <el-button size="small" type="danger" @click="handleBan(row.id)">封禁</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :page-size="20"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadProducts"
        style="margin-top:16px;justify-content:center" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminProducts, productOn, productOff, productBan } from '@/api/admin'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const products = ref([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')

const statusMap = { 0:'待审核', 1:'上架中', 2:'已下架', 3:'已封禁' }
const statusTypeMap = { 0:'warning', 1:'success', 2:'info', 3:'danger' }
const getStatusText = (s) => statusMap[s] || '未知'
const getStatusType = (s) => statusTypeMap[s] || 'info'

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getAdminProducts({ page: page.value, size: 20, keyword: keyword.value || undefined })
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const handleOn = async (id) => {
  try { await productOn(id); ElMessage.success('已上架'); loadProducts() }
  catch (e) { ElMessage.error('操作失败') }
}
const handleOff = async (id) => {
  try { await productOff(id); ElMessage.success('已下架'); loadProducts() }
  catch (e) { ElMessage.error('操作失败') }
}
const handleBan = async (id) => {
  await ElMessageBox.confirm('确认封禁该商品？', '提示')
  try { await productBan(id); ElMessage.success('已封禁'); loadProducts() }
  catch (e) { ElMessage.error('操作失败') }
}

onMounted(() => {
  loadProducts()
  document.title = '商品管理 - 龙虾道具交易平台'
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>