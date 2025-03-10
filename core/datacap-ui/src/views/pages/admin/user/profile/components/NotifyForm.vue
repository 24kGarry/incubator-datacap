<template>
  <div class="relative min-h-screen">
    <ShadcnSpin v-model="loading" fixed/>

    <ShadcnForm v-model="formState" @on-submit="onSubmit">
      <ShadcnSpace wrap>
        <div v-for="item in formState.notifyConfigure" class="flex items-center justify-between w-full space-y-2">
          <span class="text-muted-foreground">
            {{ $t('notify.text.' + item.type.toLowerCase()) }}
          </span>
          <div class="flex items-center space-x-2">
            <ShadcnSwitch v-model="item.enabled"/>
            <ShadcnButton circle size="small" :disabled="!item.enabled" @click="visibleConfigure(true, item)">
              <ShadcnIcon icon="Cog" size="15"/>
            </ShadcnButton>
          </div>
        </div>
      </ShadcnSpace>

      <ShadcnButton submit :loading="submitting" :disabled="submitting">
        {{ $t('common.save') }}
      </ShadcnButton>
    </ShadcnForm>

    <NotifyConfigure v-if="visible"
                     :model-value="visible"
                     :item="info"
                     @update:model-value="visibleConfigure($event)"
                     @update:configure="updateConfigure">
    </NotifyConfigure>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import UserService from '@/services/user.ts'
import PluginService from '@/services/plugin.ts'
import { HttpUtils } from '@/utils/http.ts'
import NotifyConfigure from '@/views/pages/admin/user/profile/components/NotifyConfigure.vue'

export default defineComponent({
  name: 'NotifyForm',
  components: { NotifyConfigure },
  data()
  {
    return {
      visible: false,
      info: {},
      loading: false,
      submitting: false,
      plugins: [],
      formState: {
        notifyConfigure: []
      }
    }
  },
  created()
  {
    this.handlerInitialize()
  },
  methods: {
    handlerInitialize()
    {
      this.loading = true
      const axios = new HttpUtils().getAxios()

      axios.all([UserService.getInfo(), PluginService.getPlugins()])
           .then(axios.spread((info, plugin) => {
             // 初始化默认配置
             this.formState = {
               notifyConfigure: [
                 { type: 'Internal', enabled: false, services: [] }
               ]
             }

             // 处理插件数据
             if (plugin.status && plugin.data && Array.isArray(plugin.data)) {
               this.plugins = plugin.data.filter((v: { type: string }) => v.type === 'NOTIFY')

               // 添加插件相关的配置
               this.plugins.forEach((item: { name: string }) => {
                 if (item && item.name) {
                   this.formState.notifyConfigure.push({
                     type: item.name,
                     enabled: false,
                     services: []
                   })
                 }
               })
             }
             else {
               this.$Message.error({
                 content: plugin.message,
                 showIcon: true
               })
             }

             // 处理用户信息数据
             if (info.status && info.data) {
               // 合并服务器配置与本地默认配置
               if (info.data.notifyConfigure && Array.isArray(info.data.notifyConfigure) &&
                   info.data.notifyConfigure.length > 0) {

                 // 创建一个映射以便更高效地查找和合并
                 const serverConfigMap = {}
                 info.data.notifyConfigure.forEach(config => {
                   if (config && config.type) {
                     serverConfigMap[config.type] = config
                   }
                 })

                 // 更新本地配置，保留所有插件项
                 this.formState.notifyConfigure = this.formState.notifyConfigure.map(localConfig => {
                   return serverConfigMap[localConfig.type] || localConfig
                 })

                 // 添加服务器上有但本地没有的配置
                 info.data.notifyConfigure.forEach(serverConfig => {
                   if (serverConfig && serverConfig.type) {
                     const exists = this.formState.notifyConfigure.some(
                         localConfig => localConfig.type === serverConfig.type
                     )

                     if (!exists) {
                       this.formState.notifyConfigure.push(serverConfig)
                     }
                   }
                 })
               }
             }
             else {
               this.$Message.error({
                 content: info.message,
                 showIcon: true
               })
             }
           }))
           .finally(() => {
             this.loading = false
           })
    },
    visibleConfigure(opened?: boolean, item?: any)
    {
      this.visible = opened
      this.info = item
    },
    updateConfigure(updatedItem)
    {
      if (updatedItem && updatedItem.type) {
        const index = this.formState.notifyConfigure.findIndex((item: { type: string }) => item.type === updatedItem.type)
        if (index !== -1) {
          this.formState.notifyConfigure[index] = { ...updatedItem }
        }
      }
    },
    onSubmit()
    {
      this.submitting = true
      UserService.changeNotify(this.formState)
                 .then((response) => {
                   if (response.status) {
                     this.$Message.success({
                       content: this.$t('common.successfully') as string,
                       showIcon: true
                     })
                   }
                   else {
                     this.$Message.error({ content: response.message, showIcon: true })
                   }
                 })
                 .finally(() => this.submitting = false)
    }
  }
})
</script>
