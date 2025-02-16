<template>
  <div class="relative">
    <ShadcnSpin v-model="loading.default" fixed/>

    <ShadcnForm v-model="formState" v-if="formState" @on-submit="onSubmit">
      <ShadcnFormItem name="fontSize"
                      class="w-[40%]"
                      :label="$t('user.common.fontSize')"
                      :description="$t('user.tip.fontSize')"
                      :rules="[
                          { pattern: /^[0-9]*$/, message: 'Please enter number!' }
                      ]">
        <ShadcnInput v-model="formState.fontSize" name="fontSize"/>
      </ShadcnFormItem>

      <ShadcnButton submit :loading="loading.submitting" :disabled="loading.submitting">
        {{ $t('common.save') }}
      </ShadcnButton>
    </ShadcnForm>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import UserService from '@/services/user'
import { UserEditor } from '@/model/user'
import Common from '@/utils/common'

export default defineComponent({
  name: 'EditorForm',
  data()
  {
    return {
      loading: {
        default: false,
        submitting: false
      },
      formState: null as UserEditor | null,
      value: 'SHOW TABLES\nSELECT * FROM table\nCREATE TABLE table\nDROP TABLE table\nALTER TABLE table'
    }
  },
  created()
  {
    this.handlerInitialize()
  },
  methods: {
    handlerInitialize()
    {
      this.loading.default = true
      UserService.getInfo()
                 .then(response => {
                   if (response.status) {
                     const configure = response.data.editorConfigure
                     if (response.data && configure) {
                       this.formState = configure as UserEditor
                     }
                   }
                 })
                 .finally(() => this.loading.default = false)
    },
    onSubmit()
    {
      this.loading.submitting = true
      UserService.changeEditor(this.formState as UserEditor)
                 .then((response) => {
                   if (response.status) {
                     this.$Message.success({
                       content: this.$t('common.successfully') as string,
                       showIcon: true
                     })
                     localStorage.setItem(Common.userEditorConfigure, JSON.stringify(this.formState))
                   }
                   else {
                     this.$Message.error({
                       content: response.message,
                       showIcon: true
                     })
                   }
                 })
                 .finally(() => this.loading.submitting = false)
    }
  }
})
</script>
