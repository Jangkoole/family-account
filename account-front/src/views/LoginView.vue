<template>
  <LoginChinese v-if="themeStore.current === 'chinese'" />
  <LoginEditorial v-else-if="themeStore.current === 'editorial'" />
  <LoginJournal v-else />
</template>

<script setup>
import { useThemeStore } from '../stores/theme'
import LoginChinese from './login/LoginChinese.vue'
import LoginEditorial from './login/LoginEditorial.vue'
import LoginJournal from './login/LoginJournal.vue'

const themeStore = useThemeStore()
</script>
