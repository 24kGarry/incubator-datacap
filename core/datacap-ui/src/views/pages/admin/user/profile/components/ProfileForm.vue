<template>
  <div class="relative">
    <ShadcnSpin v-model="loading" fixed/>
    <ShadcnForm v-model="formState" v-if="formState">
      <ShadcnFormItem name="avatar"
                      class="w-[40%]"
                      :label="$t('user.common.avatar')"
                      :description="$t('user.tip.avatar')">
        <CropperHome :pic="formState.avatarConfigure?.path" @update:value="onCropper"/>
      </ShadcnFormItem>

      <ShadcnFormItem name="username"
                      class="w-[40%]"
                      :label="$t('user.common.username')"
                      :description="$t('user.tip.username')">
        <ShadcnInput v-model="formState.username" disabled/>
      </ShadcnFormItem>

      <ShadcnFormItem name="createTime"
                      class="w-[40%]"
                      :label="$t('user.common.createTime')"
                      :description="$t('user.tip.createTime')">
        <ShadcnInput v-model="formState.createTime" disabled/>
      </ShadcnFormItem>
    </ShadcnForm>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import UserService from '@/services/user'
import { UserModel } from '@/model/user'
import CropperHome from '@/views/components/cropper/CropperHome.vue'

export default defineComponent({
  name: 'ProfileForm',
  components: { CropperHome },
  data()
  {
    return {
      loading: false,
      formState: null as UserModel | null,
      inputFile: null as any,
      inputFileBase64: null as string | null,
      uploading: false
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
      UserService.getInfo()
                 .then(response => {
                   if (response.status) {
                     this.formState = response.data
                   }
                 })
                 .finally(() => this.loading = false)
    },
    onCropper(value: any)
    {
      const configure = {
        mode: 'AVATAR',
        file: value
      }
      UserService.uploadAvatar(configure)
                 .then(response => {
                   if (response.status) {
                     if (this.formState) {
                       this.formState.avatar = response.data
                     }
                     this.$Message.success({
                       content: this.$t('common.successfully'),
                       showIcon: true
                     })
                   }
                   else {
                     this.$Message.error({
                       content: response.message,
                       showIcon: true
                     })
                   }
                 })
    }
  }
})
</script>
