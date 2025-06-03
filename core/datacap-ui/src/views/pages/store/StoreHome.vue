<template>
  <div class="relative min-h-screen">
    <ShadcnSpin v-if="loading" fixed/>
    <ShadcnAlert type="warning" class="mb-2">
      {{ $t('common.plugin.systemVersion') }}
      <ShadcnTag class="text-red-400">{{ version }}</ShadcnTag>
    </ShadcnAlert>

    <ShadcnTab v-model="activeTab" v-show="!loading">
      <template #extra>
        <ShadcnHoverCard :arrow="false" position="bottom">
          <ShadcnButton circle size="small">
            <ShadcnIcon icon="Cog" :size="15"/>
          </ShadcnButton>

          <template #content>
            <div class="p-2 space-y-2 flex flex-col">
              <ShadcnFormItem :label="$t('common.plugin.metadata.url')" :description="$t('common.plugin.metadata.description')">
                <ShadcnInput v-model="applyMetadataUrl"/>
              </ShadcnFormItem>

              <ShadcnButton type="info" @click="onSave">{{ $t('common.save') }}</ShadcnButton>
            </div>
          </template>
        </ShadcnHoverCard>
      </template>

      <ShadcnTabItem v-for="type in pluginTypes" :key="type" :label="type" :value="type">
        <div class="relative">
          <ShadcnSpace wrap size="15">
            <ShadcnAlert v-if="typeDescription(type)">
              {{ typeDescription(type) }}
            </ShadcnAlert>

            <ShadcnCard v-for="plugin in pluginsByType(type)" :key="plugin.key" class="w-full">
              <ShadcnRow class="p-3 px-6 items-center">
                <ShadcnCol span="2">
                  <!-- Logo and Name -->
                  <div class="flex flex-col items-center space-y-2 justify-between">
                    <ShadcnAvatar class="bg-transparent"
                                  size="large"
                                  :src="plugin.logo"
                                  :alt="plugin.i18nFormat ? $t(plugin.label) : plugin.label"
                                  @click="onVisibleInfo(plugin, true)">
                    </ShadcnAvatar>

                    <ShadcnText type="h6">
                      {{ plugin.i18nFormat ? $t(plugin.label) : plugin.label }}
                    </ShadcnText>
                  </div>
                </ShadcnCol>

                <ShadcnCol span="9">
                  <!-- Plugin -->
                  <div class="flex items-center space-x-4 justify-between">
                    <ShadcnSpace class="pl-8" wrap :size="[20, 40]">
                      <!-- Description -->
                      <div class="flex flex-col space-y-4">
                        <ShadcnText class="text-sm text-gray-500" type="small">
                          {{ plugin.i18nFormat ? $t(plugin.description) : plugin.description }}
                        </ShadcnText>

                        <!-- Support Version -->
                        <div class="flex space-x-2 text-sm text-gray-500">
                          <div class="flex items-center space-x-2">
                            {{ $t('common.plugin.list.supportVersion') }} :
                          </div>

                          <ShadcnTag v-for="version in plugin.supportVersion" type="success" :key="version">
                            {{ version }}
                          </ShadcnTag>
                        </div>

                        <!-- Version -->
                        <div class="flex space-x-2 text-sm text-gray-500">
                          <div class="space-x-1">
                            {{ $t('common.plugin.version') }} :
                            <ShadcnTag>{{ plugin.version }}</ShadcnTag>
                          </div>

                          <ShadcnDivider v-if="plugin.installed" type="vertical"/>

                          <div v-if="plugin.installed" class="space-x-1">
                            {{ $t('common.installVersion') }}:
                            <ShadcnTag color="#00BFFF">{{ plugin.installVersion }}</ShadcnTag>
                          </div>
                        </div>

                        <!-- Time -->
                        <div class="flex space-x-2 text-sm text-gray-500">
                          <div class="space-x-1">
                            {{ $t('common.releasedTime') }}：
                            <ShadcnTag type="warning">{{ plugin.released }}</ShadcnTag>
                          </div>

                          <ShadcnDivider v-if="plugin.installed" type="vertical"/>

                          <div v-if="plugin.installed" class="space-x-1">
                            {{ $t('common.installTime') }}：
                            <ShadcnTag color="#00BFFF">{{ plugin.installTime }}</ShadcnTag>
                          </div>
                        </div>

                        <!-- Other -->
                        <div class="flex space-x-2 text-sm text-gray-500">
                          <div class="space-x-1">
                            {{ $t('common.author') }}： {{ plugin.author }}
                          </div>
                        </div>
                      </div>
                    </ShadcnSpace>
                  </div>
                </ShadcnCol>

                <ShadcnCol span="1">
                  <!-- Action -->
                  <ShadcnButton :type="plugin.installed ? 'danger' : 'primary'" :loading="plugin.loading" @click="plugin.installed ? onUninstall(plugin) : onInstall(plugin)">
                    <template #icon>
                      <ShadcnIcon :icon="plugin.installed ? 'Trash' : 'Plus'" size="15"/>
                    </template>
                    {{ plugin.installed ? $t('common.uninstall') : $t('common.install') }}
                  </ShadcnButton>
                </ShadcnCol>
              </ShadcnRow>
            </ShadcnCard>
          </ShadcnSpace>
        </div>
      </ShadcnTabItem>
    </ShadcnTab>
  </div>

  <PluginInfo v-if="infoVisible && info"
              :info="info"
              :is-visible="infoVisible"
              @close="onVisibleInfo(null, false)">
  </PluginInfo>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, onBeforeMount, ref, watch } from 'vue'
import { useI18nHandler } from '@/i18n/I18n'
import { PackageUtils } from '@/utils/package.ts'
import PluginService from '@/services/plugin.ts'
import PluginInfo from '@/views/pages/store/components/PluginInfo.vue'

interface MetadataItem
{
  key: string
  label: string
  description: string
  logo: string
  type: string
  released: string
  i18nFormat: boolean
  version: string
  supportVersion: string[]
  author: string
  installed: boolean
  installTime: string
  installVersion: string
  url: string
  loading: boolean
}

interface Metadata
{
  key: string
  label: string
  description: string
  i18nFormat: boolean
  children: MetadataItem[]
}

const DEFAULT_METADATA_URL = 'https://cdn.north.devlive.org/applications/datacap/metadata/metadata.json'

// URL validation helper
const isValidUrl = (urlString: string): boolean => {
  try {
    new URL(urlString)
    return true
  }
  catch {
    return false
  }
}

const { proxy } = getCurrentInstance()!
// @ts-ignore
const { loadingState } = useI18nHandler()
const loading = ref(false)
const metadata = ref<Metadata>(null)
const version = ref(PackageUtils.get('version'))
const info = ref<MetadataItem>(null)
const infoVisible = ref(false)

// Default to first plugin type found
const activeTab = ref('')

// Get all unique plugin types
const pluginTypes = computed(() => {
  if (!metadata.value?.children?.length) {
    return []
  }

  const types = new Set(metadata.value.children.map(plugin => plugin.type))
  return Array.from(types)
})

// Group plugins by type
const pluginsByType = (type: string) => {
  if (!metadata.value?.children?.length) {
    return []
  }
  return metadata.value.children.filter(plugin => plugin.type === type)
}

// Get description for a type (could be extended to add custom descriptions per type)
const typeDescription = (type: string) => {
  const defaultDescriptions: Record<string, string> = {
    'FileSystem': 'File system plugins provide storage integrations',
    'Connector': 'Connector plugins enable data source connections',
    'Visualization': 'Visualization plugins provide data display capabilities'
    // Add more defaults for other types
  }

  return defaultDescriptions[type] || `${ type } plugins for DataCap`
}

const metadataUrl = computed({
  get: () => {
    const savedUrl = localStorage.getItem('metadataUrl')
    return savedUrl && isValidUrl(savedUrl) ? savedUrl : DEFAULT_METADATA_URL
  },
  set: (value: string) => {
    const trimmedUrl = value?.trim()
    if (trimmedUrl && isValidUrl(trimmedUrl)) {
      localStorage.setItem('metadataUrl', trimmedUrl)
    }
    else {
      localStorage.removeItem('metadataUrl')
    }
  }
})
const applyMetadataUrl = ref(metadataUrl.value)

watch(loadingState, async (newVal) => {
  if (!newVal && !metadata.value) {
    await loadMetadata()
  }
})

watch(pluginTypes, (newTypes) => {
  // Set active tab to first type when types are loaded
  if (newTypes.length && !activeTab.value) {
    activeTab.value = newTypes[0]
  }
})

const loadMetadata = async () => {
  loading.value = true
  try {
    const url = metadataUrl.value
    if (!isValidUrl(url)) {
      throw new Error('Invalid metadata URL')
    }

    const response = await fetch(url)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${ response.status }`)
    }

    const data = await response.json()
    metadata.value = data

    const installResponse = await PluginService.getPlugins()
    if (!installResponse.status) {
      // @ts-ignore
      proxy?.$Message.error({ content: installResponse.message, showIcon: true })
      return
    }

    // Bind installation information
    metadata.value.children.forEach(item => {
      item.loading = false
      installResponse.data.some((installedPlugin: { key: string, loadTime: string, version: string }) => {
        if (installedPlugin.key === item.key) {
          item.installed = true
          item.installTime = installedPlugin.loadTime
          item.installVersion = installedPlugin.version
          return true
        }
        return false
      })
    })
  }
  catch (error) {
    if (error instanceof TypeError) {
      // @ts-ignore
      proxy?.$Message.error({ content: proxy?.$t('common.tip.pageNotNetwork'), showIcon: true })
    }
    else {
      // @ts-ignore
      proxy?.$Message.error({ content: `${ proxy?.$t('common.pageNotFoundTip') }: ${ error.message }`, showIcon: true })
    }

    // Reset to default URL if current URL is invalid
    if (!isValidUrl(metadataUrl.value)) {
      metadataUrl.value = DEFAULT_METADATA_URL
      await loadMetadata()
    }
  }
  finally {
    loading.value = false
  }
}

// Install plugin
const onInstall = async (item: MetadataItem) => {
  if (item.loading) {
    return
  }
  try {
    item.loading = true
    const installResponse = await PluginService.install({ name: item.key, url: item.url })
    if (installResponse.status) {
      // @ts-ignore
      proxy?.$Message.success({ content: proxy?.$t('common.installSuccess'), showIcon: true })
    }
    else {
      // @ts-ignore
      proxy?.$Message.error({ content: installResponse.message, showIcon: true })
    }

    await loadMetadata()
  }
  finally {
    item.loading = false
  }
}

// Uninstall plugin
const onUninstall = async (item: MetadataItem) => {
  if (item.loading) {
    return
  }
  try {
    item.loading = true
    const unInstallResponse = await PluginService.uninstall(item.label)
    if (unInstallResponse.status) {
      // @ts-ignore
      proxy?.$Message.success({ content: proxy?.$t('common.uninstallSuccess'), showIcon: true })
    }
    else {
      // @ts-ignore
      proxy?.$Message.error({ content: unInstallResponse.message, showIcon: true })
    }

    await loadMetadata()
  }
  finally {
    item.loading = false
  }
}

const onSave = async () => {
  try {
    const url = applyMetadataUrl.value.trim()
    if (!isValidUrl(url)) {
      // @ts-ignore
      proxy?.$Message.error({
        content: 'Invalid Url',
        showIcon: true
      })
      return
    }

    metadataUrl.value = url
    // @ts-ignore
    proxy?.$Message.success({
      content: proxy?.$t('common.successfully'),
      showIcon: true
    })

    await loadMetadata()
  }
  catch (error) {
    // @ts-ignore
    proxy?.$Message.error({
      content: proxy?.$t('common.error'),
      showIcon: true
    })
  }
}

const onVisibleInfo = (item: MetadataItem, opened?: boolean) => {
  infoVisible.value = opened
  if (opened) {
    info.value = item
  }
}

onBeforeMount(() => {
  if (!loadingState.value) {
    loadMetadata()
  }
})
</script>