<template>
  <RegisterChinese v-if="themeStore.current === 'chinese'" />
  <RegisterEditorial v-else-if="themeStore.current === 'editorial'" />
  <RegisterJournal v-else />
</template>

<script setup>
import { useThemeStore } from '../stores/theme'
import RegisterChinese from './register/RegisterChinese.vue'
import RegisterEditorial from './register/RegisterEditorial.vue'
import RegisterJournal from './register/RegisterJournal.vue'

const themeStore = useThemeStore()
</script>
