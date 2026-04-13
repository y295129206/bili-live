<template>
  <div class="message-item">
    <Avatar :avatar="data.sendUserAvatar" :userId="data.sendUserId"></Avatar>
    <div class="user-info-panel">
      <div class="user-info">
        <router-link
          :to="`/user/${data.sendUserId}`"
          class="user-name"
          target="_blank"
        >
          {{ data.sendUserName }}
        </router-link>
        <span class="title-info">{{ convertTitle() }}</span>
      </div>
      <template v-if="data.messageType == 4">
        <div class="comment">{{ getExtendDto().messageContent }}</div>
        <div class="reply" v-if="getExtendDto().messageContentReply">
          {{ getExtendDto().messageContentReply }}
        </div>
      </template>
      <div class="send-time">
        {{ proxy.Utils.formatDate(data.createTime) }}
        <span
          class="iconfont icon-delete"
          @click="delMessage(data.messageId)"
        ></span>
      </div>
    </div>
    <div class="video-cover">
      <router-link :to="`/video/${data.videoId}`" target="_blank">
        <Cover :source="data.videoCover"></Cover>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { getCurrentInstance } from "vue";
import { useRouter } from "vue-router";
const { proxy } = getCurrentInstance();
const router = useRouter();
const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
});

// 获取扩展信息对象（兼容后端是否已解析）
const getExtendDto = () => {
  // 如果后端已经解析了 extendDto，直接返回
  if (props.data.extendDto) {
    return props.data.extendDto;
  }
  // 否则从 extendJson 解析
  if (props.data.extendJson) {
    try {
      return JSON.parse(props.data.extendJson);
    } catch (e) {
      console.error('解析 extendJson 失败:', e);
      return {};
    }
  }
  return {};
};

const MESSAGE_TYPE = {
  1: "系统消息",
  2: "点赞",
  3: "收藏",
  4: "评论",
};

const convertTitle = () => {
  if (props.data.messageType == 4) {
    const extendDto = getExtendDto();
    if (extendDto.messageContentReply) {
      return `在视频中回复了你的评论`;
    }
    return `在视频中发表了评论`;
  } else {
    return `${MESSAGE_TYPE[props.data.messageType]}了视频`;
  }
};

const emit = defineEmits(["delMessage"]);
const delMessage = (messageId) => {
  emit("delMessage", messageId);
};
</script>

<style lang="scss" scoped>
.message-item {
  display: flex;
  padding: 10px 5px;
  border-bottom: 1px solid #ddd;
  align-items: center;
  .user-info-panel {
    margin-left: 10px;
    flex: 1;
    .user-info {
      .user-name {
        font-weight: bold;
        color: var(--text);
        text-decoration: none;
      }
      .title-info {
        margin-left: 5px;
        color: var(--text3);
        font-size: 13px;
      }
    }
    .comment {
      font-size: 13px;
      margin-top: 5px;
      color: var(--text2);
    }
    .reply {
      border-left: 2px solid #ddd;
      padding-left: 5px;
      font-size: 12px;
      margin-top: 5px;
    }
    .send-time {
      margin-top: 5px;
      font-size: 12px;
      color: var(--text3);
    }
    .icon-delete {
      font-size: 14px;
      cursor: pointer;
      margin-left: 10px;
    }
  }
  .video-cover {
    margin-left: 10px;
    width: 100px;
  }
}
</style>
