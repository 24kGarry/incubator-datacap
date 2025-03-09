<template>
  <div class="relative min-h-screen">
    <ShadcnSpin v-model="loading" fixed/>

    <ShadcnForm v-model="formState" v-if="formState && formState.notifyConfigure?.length > 0" @on-submit="onSubmit">
      <ShadcnFormItem name="dataset" class="space-y-3" :label="$t('common.dataset')">
        <ShadcnCheckboxGroup v-model="formState.notifyConfigure[0].types">
          <ShadcnCheckbox value="Internal">{{ $t('notify.text.internal') }}</ShadcnCheckbox>
          <ShadcnCheckbox v-for="item in plugins" :value="item.name">{{ item.name }}</ShadcnCheckbox>
        </ShadcnCheckboxGroup>
      </ShadcnFormItem>

      <ShadcnButton submit :loading="submitting" :disabled="submitting">
        {{ $t('common.save') }}
      </ShadcnButton>
    </ShadcnForm>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import UserService from '@/services/user.ts'
import PluginService from '@/services/plugin.ts'
import { HttpUtils } from '@/utils/http.ts'

export default defineComponent({
  name: 'NotifyForm',
  data()
  {
    return {
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
             if (info.status && info.data) {
               if (info.data?.notifyConfigure && info.data.notifyConfigure.length > 0) {
                 this.formState.notifyConfigure = info.data.notifyConfigure
               }
               else {
                 this.formState = {
                   notifyConfigure: [
                     { service: 'DATASET', types: [] }
                   ]
                 }
               }
             }
             else {
               this.$Message.error({ content: info.message, showIcon: true })
             }

             if (plugin.status && plugin.data) {
               this.plugins = plugin.data.filter((v: { type: string }) => v.type === 'NOTIFY')
             }
             else {
               this.$Message.error({ content: plugin.message, showIcon: true })
             }
           }))
           .finally(() => this.loading = false)
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
