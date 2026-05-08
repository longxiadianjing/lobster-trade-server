<template>
  <div class="publish-container">
    <!-- 顶部导航 -->
    <div class="top-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">🦞 龙虾道具交易平台</router-link>
        <div class="header-actions">
          <el-button @click="router.push({ path: '/user' })">个人中心</el-button>
        </div>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="publish-main">
      <!-- 左侧边栏 -->
      <aside class="sidebar">
        <el-card class="menu-card" shadow="never">
          <template #header>
            <span class="menu-title">快捷导航</span>
          </template>
          <el-menu :default-active="activeMenu" router @select="activeMenu = $event">
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="/wallet">
              <el-icon><Wallet /></el-icon>
              <span>我的钱包</span>
            </el-menu-item>
            <el-menu-item index="/product/list">
              <el-icon><Goods /></el-icon>
              <span>商品列表</span>
            </el-menu-item>
            <el-menu-item index="/order/center">
              <el-icon><List /></el-icon>
              <span>订单中心</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </aside>

      <!-- 右侧内容 -->
      <main class="content">
        <el-card class="publish-card" shadow="never" v-loading="pageLoading">
          <template #header>
            <div class="card-header">
              <span class="card-title">发布商品</span>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
            <!-- 游戏选择 -->
            <el-form-item label="游戏" prop="gameId">
              <el-select v-model="form.gameId" placeholder="请选择游戏" style="width: 240px;" @change="onGameChange">
                <el-option
                  v-for="game in gameOptions"
                  :key="game.id"
                  :label="game.gameName"
                  :value="game.id"
                />
              </el-select>
            </el-form-item>

            <!-- 商品分类 -->
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 240px;" :disabled="!form.gameId">
                <el-option
                  v-for="cat in categoryOptions"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>

            <!-- 商品类型 -->
            <el-form-item label="商品类型" prop="productType">
              <el-radio-group v-model="form.productType">
                <el-radio label="goods">游戏币/道具</el-radio>
                <el-radio label="boost">代练服务</el-radio>
                <el-radio label="accompany">陪玩陪练</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 商品标题 -->
            <el-form-item label="商品标题" prop="title">
              <el-input v-model="form.title" placeholder="简洁明了，描述商品核心信息" maxlength="50" show-word-limit style="width: 400px;" />
            </el-form-item>

            <!-- 商品描述 -->
            <el-form-item label="商品描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="4"
                placeholder="详细描述商品信息（游戏区服、交易方式、交付方式等）"
                maxlength="500"
                show-word-limit
                style="width: 500px;"
              />
            </el-form-item>

            <!-- 价格信息 -->
            <el-form-item label="价格类型" prop="priceType">
              <el-radio-group v-model="form.priceType">
                <el-radio label="fixed">一口价</el-radio>
                <el-radio label="negotiable">可议价</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0.01" :precision="2" placeholder="0.00" style="width: 160px;" />
              <span class="unit-text">元</span>
            </el-form-item>

            <el-form-item label="单位" prop="unit" v-if="form.productType === 'goods' || form.productType === 'accompany'">
              <el-input v-model="form.unit" placeholder="如：万、个、套" style="width: 120px;" />
            </el-form-item>

            <!-- 库存 -->
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="1" :max="9999" style="width: 120px;" />
            </el-form-item>

            <!-- 游戏区服 -->
            <el-form-item label="游戏区服">
              <el-input v-model="form.gameZone" placeholder="如：烽火区、战场区、全面战场" style="width: 240px;" />
            </el-form-item>

            <!-- 服务器 -->
            <el-form-item label="服务器">
              <el-input v-model="form.server" placeholder="如：QQ区、微信区" style="width: 200px;" />
            </el-form-item>

            <!-- 平台 -->
            <el-form-item label="交易平台">
              <el-input v-model="form.platform" placeholder="如：官方渠道、第三方平台" style="width: 240px;" />
            </el-form-item>

            <!-- 代练服务专用 -->
            <template v-if="form.productType === 'boosting'">
              <el-form-item label="最低押金">
                <el-input-number v-model="form.minDeposit" :min="0" :precision="2" style="width: 160px;" />
                <span class="unit-text">元</span>
              </el-form-item>

              <el-form-item label="预计时长">
                <el-input-number v-model="form.estimatedHours" :min="1" style="width: 120px;" />
                <span class="unit-text">小时</span>
              </el-form-item>
            </template>

            <!-- 图片上传 -->
            <el-form-item label="商品图片">
              <div class="upload-area">
                <el-upload
                  ref="uploadRef"
                  :auto-upload="false"
                  :limit="9"
                  list-type="picture-card"
                  :on-preview="handlePictureCardPreview"
                  :on-remove="handleRemove"
                  :on-change="handleFileChange"
                  :file-list="imageList"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
                <p class="upload-tip">最多上传9张图片，建议尺寸 800x800</p>
              </div>
            </el-form-item>

            <!-- 提交按钮 -->
            <el-form-item>
              <el-button type="primary" size="large" :loading="submitLoading" @click="handleSubmit">
                立即发布
              </el-button>
              <el-button size="large" @click="router.push({ path: '/product/list' })">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </main>
    </div>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="previewVisible">
      <img :src="previewUrl" alt="预览" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { publishProduct, uploadImage } from '@/api/product'
import { getGameList } from '@/api/game'
import { getCategoriesByGameId } from '@/api/category'
import { User, Wallet, Goods, List } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const submitLoading = ref(false)
const pageLoading = ref(true)
const uploadLoading = ref(false)
const uploadRef = ref(null)
const activeMenu = ref('/product/publish')

const imageList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')
const gameOptions = ref([])
const categoryOptions = ref([])

const form = reactive({
  gameId: null,
  categoryId: null,
  productType: 'goods',
  title: '',
  description: '',
  priceType: 'fixed',
  price: null,
  unit: '',
  stock: 1,
  gameZone: '',
  server: '',
  platform: '',
  minDeposit: 0,
  estimatedHours: null,
  images: []
})

const rules = {
  gameId: [{ required: true, message: '请选择游戏', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  productType: [{ required: true, message: '请选择商品类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请设置库存', trigger: 'blur' }],
  priceType: [{ required: true, message: '请选择价格类型', trigger: 'change' }]
}

// 模拟分类（实际从API获取）
const mockCategories = {
  1: [
    { id: 101, name: '哈夫币' },
    { id: 102, name: '装备' },
    { id: 103, name: '段位代练' },
    { id: 104, name: '任务代肝' }
  ],
  2: [
    { id: 201, name: '点券' },
    { id: 202, name: '英雄' },
    { id: 203, name: '皮肤' }
  ]
}

const onGameChange = async (gameId) => {
  form.categoryId = null
  if (!gameId) {
    categoryOptions.value = []
    return
  }
  try {
    const res = await getCategoriesByGameId(gameId)
    if (res.data && res.data.length > 0) {
      categoryOptions.value = res.data.map(c => ({ id: c.id, name: c.name }))
    } else {
      categoryOptions.value = mockCategories[gameId] || []
    }
  } catch (e) {
    categoryOptions.value = mockCategories[gameId] || []
  }
}

const handlePictureCardPreview = (file) => {
  previewUrl.value = file.url
  previewVisible.value = true
}

const handleRemove = (file, fileList) => {
  imageList.value = fileList
}

// 文件选中后立即上传到服务器（同步等待）
const handleFileChange = async (uploadFile, uploadFileList) => {
  imageList.value = uploadFileList

  // 如果是新增文件（status 为 ready 或 ''），执行上传
  if (!uploadFile.status || uploadFile.status === 'ready') {
    try {
      const res = await uploadImage(uploadFile.raw)
      if (res.code === 200 || res.code === 0) {
        const serverUrl = res.data || res.url
        // 找到对应条目，更新 url
        const idx = imageList.value.findIndex(f => f.uid === uploadFile.uid)
        if (idx !== -1) {
          imageList.value[idx] = {
            ...imageList.value[idx],
            url: serverUrl,
            status: 'success'
          }
        }
        ElMessage.success('图片上传成功')
      } else {
        ElMessage.error(res.message || '图片上传失败')
        // 从列表移除失败的文件
        imageList.value = imageList.value.filter(f => f.uid !== uploadFile.uid)
      }
    } catch (e) {
      console.error('图片上传异常:', e)
      ElMessage.error('图片上传失败')
      imageList.value = imageList.value.filter(f => f.uid !== uploadFile.uid)
    }
  }
}


const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      // 收集图片URL列表
      const imageUrls = imageList.value
        .map(f => f.url || (f.response && f.response.data) || (f.response && f.response.url) || '')
        .filter(url => url && (url.startsWith('http') || url.startsWith('/')))

      // 允许无图片发布
      const payload = { ...form, images: JSON.stringify(imageUrls) }

      await publishProduct(payload)
      ElMessage.success('发布成功')
      router.push({ path: '/product/list' })
    } catch (e) {
      console.error('发布失败:', e)
      ElMessage.error(e.message || '发布失败，请重试')
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(async () => {
  document.title = '发布商品 - 龙虾道具交易平台'
  pageLoading.value = true
  try {
    const res = await getGameList()
    if (res.data && res.data.length > 0) {
      gameOptions.value = res.data.map(g => ({ id: g.id, name: g.gameName }))
    }
  } catch (e) {
    console.error('加载游戏列表失败:', e)
  } finally {
    pageLoading.value = false
  }
})
</script>

<style scoped>
.publish-container {
  min-height: 100vh;
  background: #f0f2f5;
}

.top-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-decoration: none;
}

.publish-main {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
}

.menu-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.menu-card :deep(.el-card__header) {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.menu-card :deep(.el-card__body) {
  padding: 0;
}

.menu-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.menu-card :deep(.el-menu) {
  border: none;
}

.content {
  flex: 1;
  min-width: 0;
}

.publish-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.publish-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.publish-card :deep(.el-card__body) {
  padding: 24px 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.unit-text {
  margin-left: 8px;
  color: #999;
  font-size: 14px;
}

.upload-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-tip {
  margin: 0;
  font-size: 12px;
  color: #999;
}

@media (max-width: 900px) {
  .publish-main {
    flex-direction: column;
    padding: 16px;
  }

  .sidebar {
    width: 100%;
    position: static;
  }
}
</style>
