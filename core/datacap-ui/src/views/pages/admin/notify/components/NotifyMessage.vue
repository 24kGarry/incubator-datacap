<template>
  <div class="mt-1 text-sm text-gray-600">
    <div v-if="message.entityExists" class="flex space-x-1">
      <span>{{ $t(`common.${ entityTypeLabel }`) }}</span>

      <template v-if="message.entityType === 'DATASET'">
        <RouterLink :to="`/admin/dataset/info/${message.entityCode}`" target="_blank" class="hover:text-blue-400 flex items-center">
          [ {{ message.entityName }} ]
        </RouterLink>
      </template>

      <template v-else>
        <ShadcnLink class="hover:text-blue-400" :to="'/' + message.entityType + '/' + message.entityCode">[ {{ message.entityName }} ]</ShadcnLink>
      </template>

      <span>{{ $t(`common.${ actionType }`) }}</span>
    </div>
    <div v-else>
      {{ $t(`common.${ entityTypeLabel }`) }} [ {{ message.entityName }} ] {{ $t(`common.${ actionType }`) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const entityTypeLabel = computed(() => {
  return props.message?.entityType?.toLowerCase() || ''
})

const actionType = computed(() => {
  return props.message?.type?.toLowerCase() || ''
})
</script>
