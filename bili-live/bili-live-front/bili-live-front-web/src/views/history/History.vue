<template>
  <div class="history-list">
    <div class="top-info">
      <div class="iconfont icon-wating">历史记录</div>
      <div>
        <el-button type="primary" @click="cleanAll">清空历史</el-button>
      </div>
    </div>
    <el-timeline>
      <div class="data-list">
        <DataLoadMoreList :dataSource="dataSource" :loading="loadingData" @loadData="loadDataList" layoutType="line">
          <template #default="{ data }">
            <el-timeline-item :timestamp="proxy.Utils.formatDate(data.lastUpdateTime)" placement="top">
              <div class="history-item">
                <div class="cover" @click="goDetail(data.videoId)" width="235">
                  <Cover :source="data.videoCover" :width="235" fit="cover"></Cover>
                </div>
                <div class="video-info" @click="goDetail(data.videoId)">
                  {{ data.videoName || '已失效视频' }}
                </div>
                <div class="op-btns">
                  <div class="iconfont icon-delete" @click="delHisotry(data.videoId)"></div>
                </div>
              </div>
            </el-timeline-item>
          </template>
        </DataLoadMoreList>
      </div>
    </el-timeline>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const { proxy } = getCurrentInstance()
const router = useRouter()

import { useNavAction } from '@/stores/navActionStore'
const navActionStore = useNavAction()

const loadingData = ref(false)
const dataSource = ref({})
const loadDataList = async () => {
  let params = {
    pageNo: dataSource.value.pageNo,
  }
  loadingData.value = true
  let result = await proxy.Request({
    url: proxy.Api.playHisotry,
    params,
  })
  loadingData.value = false
  if (!result) {
    return
  }

  const dataList = dataSource.value.list
  dataSource.value = Object.assign({}, result.data)
  if (result.data.pageNo > 1) {
    dataSource.value.list = dataList.concat(result.data.list)
  }
}
loadDataList()

onMounted(() => {
  //初始化store
  navActionStore.setShowHeader(true)
  navActionStore.setFixedHeader(true)
  navActionStore.setFixedCategory(false)
  navActionStore.setShowCategory(false)
})

const cleanAll = () => {
  proxy.Confirm({
    message: '确定要清空历史记录吗？',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.cleanHistory,
      })
      if (!result) {
        return
      }
      proxy.Message.success('删除成功')
      dataSource.value = { list: [] }
    },
  })
}

const delHisotry = (videoId) => {
  proxy.Confirm({
    message: '确定要删除记录吗？',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delHistory,
        params: {
          videoId,
        },
      })
      if (!result) {
        return
      }
      proxy.Message.success('删除成功')
      dataSource.value.list = dataSource.value.list.filter((item) => {
        return item.videoId != videoId
      })
    },
  })
}

const goDetail = (videoId) => {
  router.push(`/video/${videoId}`)
}
</script>

<style lang="scss" scoped>
.history-list {
  margin: 20px auto 0px;
  width: 1200px;

  .top-info {
    // 原有样式保持不变
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-left: 40px;
    margin-bottom: 20px;
    .icon-wating {
      font-size: 16px;
      &::before {
        margin-right: 5px;
        font-size: 22px;
        color: #e3936c;
        float: left;
      }
    }
  }

  // 【修正】直接给 .history-item 写样式，移除 .data-list 嵌套
  .history-item {
    margin-top: 10px;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-items: center;
    width: 100%; // 占满父容器，避免宽度不足

    .cover {
      width: 235px !important;
      height: 141px !important;
      flex: 0 0 235px !important; // 固定宽度，不伸缩
      flex-shrink: 0 !important;
      overflow: hidden;
      border-radius: 5px;
      margin-right: 15px;
      // 原有 Cover 组件样式保持不变
      :deep(.image-panel),
      :deep(.el-image),
      :deep(.el-image__inner) {
        width: 235px !important;
        height: 141px !important;
        object-fit: cover !important;
      }
    }

    .video-info {
      flex: 1; // 占剩余所有空间
      margin-left: 15px;
      cursor: pointer;
      font-size: 15px;
      font-weight: 500;
      // 【新增】文本溢出省略，避免长标题撑开布局
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      line-height: 141px; // 【新增】与封面高度一致，垂直居中
      &:hover {
        color: #0088cc;
        text-decoration: underline;
      }
    }

    .op-btns {
      margin-left: auto !important; // 强制靠右对齐
      flex-shrink: 0 !important; // 不压缩
      .iconfont {
        cursor: pointer;
        color: #ff4d4f;
        font-size: 20px;
        &:hover {
          color: #ff2b2b;
        }
      }
    }
  }
}
</style>
