<template>
  <ShadcnModal v-model="visible"
               width="50%"
               height="60%"
               :title="title"
               @on-close="onCancel">
    <div v-if="info" class="relative w-full h-full flex flex-col items-center p-6">
      <div class="flex flex-col items-center mb-6">
        <ShadcnAvatar class="mb-4 shadow-lg border-4 border-gray-100"
                      style="width: 5rem; height: 5rem;"
                      :src="info.logo || '/static/images/plugin.png'"
                      :alt="info.label">
        </ShadcnAvatar>

        <h3 class="text-xl font-semibold text-gray-800 mb-2">{{ info.label }}</h3>
      </div>

      <div class="w-full max-w-md mb-6">
        <div class="bg-gray-50 rounded-lg p-4 text-center">
          <ShadcnText class="text-sm text-gray-600 leading-relaxed" type="small">
            {{ info.i18nFormat ? $t(info.description) : info.description }}
          </ShadcnText>
        </div>
      </div>

      <div class="w-full max-w-md space-y-4">
        <div class="flex items-center justify-between p-3 bg-white rounded-lg border border-gray-200">
          <span class="text-sm font-medium text-gray-700">{{ $t('common.plugin.version') }}</span>
          <ShadcnTag>{{ info.version }}</ShadcnTag>
        </div>

        <div v-if="info.installed" class="flex items-center justify-between p-3 bg-white rounded-lg border border-gray-200">
          <span class="text-sm font-medium text-gray-700">{{ $t('common.installVersion') }}</span>
          <ShadcnTag color="#00BFFF">{{ info.installVersion }}</ShadcnTag>
        </div>

        <div class="flex items-center justify-between p-3 bg-white rounded-lg border border-gray-200">
          <span class="text-sm font-medium text-gray-700">{{ $t('common.author') }}</span>
          <span class="text-sm text-gray-600">{{ info.author }}</span>
        </div>

        <div class="flex items-center justify-between p-3 bg-white rounded-lg border border-gray-200">
          <span class="text-sm font-medium text-gray-700">{{ $t('common.releasedTime') }}</span>
          <span class="text-sm text-gray-600">{{ info.released }}</span>
        </div>

        <div class="p-3 bg-white rounded-lg border border-gray-200" style="margin-bottom: 1rem;">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-700">{{ $t('common.plugin.list.supportVersion') }}</span>
          </div>

          <div class="flex flex-wrap gap-2">
            <ShadcnTag v-for="version in info.supportVersion" type="success" :key="version">
              {{ version }}
            </ShadcnTag>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <ShadcnButton type="default" @click="onCancel">
        {{ $t('common.cancel') }}
      </ShadcnButton>
    </template>
  </ShadcnModal>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

interface Props
{
  isVisible: boolean
  info: any | null
}

interface Emits
{
  (e: 'close', value: boolean): void
}

const props = withDefaults(defineProps<Props>(), {
  isVisible: false,
  info: null
})
const emit = defineEmits<Emits>()
const { t } = useI18n()

const title = ref<string | null>(null)

const visible = computed({
  get(): boolean
  {
    return props.isVisible
  },
  set(value: boolean)
  {
    emit('close', value)
  }
})

const handleInitialize = () => {
  if (props.info) {
    title.value = props.info.label
  }
}

const onCancel = () => {
  visible.value = false
  emit('close', false)
}

onMounted(() => {
  handleInitialize()
})

watch(() => props.info, () => {
  handleInitialize()
}, { immediate: true })
</script>