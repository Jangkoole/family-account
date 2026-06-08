<template>
  <DashboardChinese v-if="themeStore.current === 'chinese'" />
  <DashboardEditorial v-else-if="themeStore.current === 'editorial'" />
  <DashboardJournal v-else />
</template>

<script setup>
import { useThemeStore } from '../stores/theme'
import DashboardChinese from './dashboard/DashboardChinese.vue'
import DashboardEditorial from './dashboard/DashboardEditorial.vue'
import DashboardJournal from './dashboard/DashboardJournal.vue'

const themeStore = useThemeStore()
</script>
