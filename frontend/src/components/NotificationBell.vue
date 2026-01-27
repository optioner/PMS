<template>
  <el-popover
    placement="bottom"
    :width="300"
    trigger="click"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-button :icon="Bell" circle />
      </el-badge>
    </template>
    
    <div class="notification-list">
      <div v-if="notifications.length === 0" class="empty-text">No notifications</div>
      <div 
        v-for="notification in notifications" 
        :key="notification.id" 
        class="notification-item"
        :class="{ unread: !notification.read }"
        @click="markAsRead(notification)"
      >
        <div class="title">{{ notification.title }}</div>
        <div class="content">{{ notification.content }}</div>
        <div class="time">{{ new Date(notification.createdAt).toLocaleString() }}</div>
      </div>
      <div v-if="notifications.length > 0" class="mark-all">
        <el-button type="text" size="small" @click="markAllRead">Mark all as read</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import request from '@/utils/request'

const notifications = ref<any[]>([])
const unreadCount = ref(0)
let timer: any = null

const fetchNotifications = async () => {
  try {
    const listRes = await request.get('/notifications')
    notifications.value = listRes as any
    const countRes: any = await request.get('/notifications/count')
    unreadCount.value = countRes
  } catch (e) {
    // ignore
  }
}

const markAsRead = async (notification: any) => {
  if (!notification.read) {
    await request.put(`/notifications/${notification.id}/read`)
    notification.read = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

const markAllRead = async () => {
  await request.put('/notifications/read-all')
  notifications.value.forEach(n => n.read = true)
  unreadCount.value = 0
}

onMounted(() => {
  fetchNotifications()
  // Poll every 30 seconds
  timer = setInterval(fetchNotifications, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.notification-badge {
  margin-right: 20px;
}
.notification-list {
  max-height: 400px;
  overflow-y: auto;
}
.notification-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}
.notification-item:hover {
  background-color: #f5f7fa;
}
.notification-item.unread {
  background-color: #ecf5ff;
}
.title {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 4px;
}
.content {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}
.time {
  font-size: 10px;
  color: #909399;
}
.empty-text {
  text-align: center;
  padding: 20px;
  color: #909399;
}
.mark-all {
  text-align: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
</style>
