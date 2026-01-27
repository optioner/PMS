<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>PMS Register</h2>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="Full Name" prop="fullName">
          <el-input v-model="form.fullName" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="Position" prop="position">
          <el-input v-model="form.position" placeholder="e.g. Developer" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">Register</el-button>
          <el-button @click="$router.push('/login')">Login</el-button>
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
  fullName: '',
  email: '',
  password: '',
  position: '',
  role: ['team_member']
})

const rules = reactive<FormRules>({
  fullName: [{ required: true, message: 'Please input full name', trigger: 'blur' }],
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
        await authStore.register(form)
        ElMessage.success('Registration successful! Please login.')
        router.push('/login')
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
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.register-card {
  width: 500px;
}
</style>
