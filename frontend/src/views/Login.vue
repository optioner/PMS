<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>PMS Login</h2>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">Login</el-button>
          <el-button @click="$router.push('/register')">Register</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const authStore = useAuthStore()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  email: '',
  password: ''
})

const rules = reactive<FormRules>({
  email: [
    { required: true, message: 'Please input email address', trigger: 'blur' },
    { type: 'email', message: 'Please input correct email address', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please input password', trigger: 'blur' },
    { min: 6, message: 'Password length should be at least 6', trigger: 'blur' }
  ]
})

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(form)
        ElMessage.success('Login successful')
        router.push('/')
      } catch (error) {
        // Error handled in request interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
</style>
