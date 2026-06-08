<template>
  <LayoutChinese v-if="themeStore.current === 'chinese'" />
  <LayoutEditorial v-else-if="themeStore.current === 'editorial'" />
  <LayoutJournal v-else />
</template>

<script setup>
import { useThemeStore } from '../stores/theme'
import LayoutChinese from '../layouts/LayoutChinese.vue'
import LayoutEditorial from '../layouts/LayoutEditorial.vue'
import LayoutJournal from '../layouts/LayoutJournal.vue'

const themeStore = useThemeStore()
</script>
