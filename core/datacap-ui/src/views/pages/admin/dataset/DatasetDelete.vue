<template>
  <ShadcnModal v-model="visible" :title="title" @on-close="onCancel">
    <ShadcnSpace wrap>
      <ShadcnAlert type="error" :title="$t('dataset.tip.deleteAlert1')"/>
      <ShadcnAlert type="error" :title="$t('dataset.tip.deleteAlert2')"/>
      <ShadcnAlert type="info" :title="$t('dataset.tip.deleteAlert3').replace('$VALUE', String(info?.name))"/>
    </ShadcnSpace>

    <ShadcnForm v-model="formState" @on-submit="onSubmit">
      <ShadcnFormItem name="name"
                      :rules="[
                            { required: true, message: $t('dataset.validator.name.required') },
                            { validator: validateMatch }
                      ]">
        <ShadcnInput v-model="formState.name" name="name" :placeholder="$t('dataset.placeholder.name')"/>
      </ShadcnFormItem>

      <div class="flex justify-end">
        <ShadcnSpace>
          <ShadcnButton type="default" @click="onCancel">
            {{ $t('common.cancel') }}
          </ShadcnButton>

          <ShadcnButton submit type="error" :loading="loading" :disabled="loading">
            {{ title }}
          </ShadcnButton>
        </ShadcnSpace>
      </div>
    </ShadcnForm>
  </ShadcnModal>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { DatasetModel } from '@/model/dataset.ts'
import DatasetService from '@/services/dataset.ts'

export default defineComponent({
  name: 'DatasetDelete',
  computed: {
    visible: {
      get(): boolean
      {
        return this.isVisible
      },
      set(value: boolean)
      {
        this.$emit('close', value)
      }
    }
  },
  props: {
    isVisible: {
      type: Boolean
    },
    info: {
      type: Object as () => DatasetModel | null
    }
  },
  data()
  {
    return {
      title: null as string | null,
      loading: false,
      formState: {
        name: ''
      }
    }
  },
  created()
  {
    this.handleInitialize()
  },
  methods: {
    handleInitialize()
    {
      if (this.info) {
        this.title = `${ this.$t('dataset.common.deleteInfo').replace('$VALUE', String(this.info.name)) }`
      }
    },
    onSubmit()
    {
      if (this.info) {
        this.loading = true
        DatasetService.deleteByCode(this.info.code!)
                      .then((response) => {
                        if (response.status) {
                          this.$Message.success({
                            content: this.$t('dataset.tip.deleteSuccess').replace('$VALUE', String(this.info?.name)),
                            showIcon: true
                          })

                          this.onCancel()
                        }
                        else {
                          this.$Message.error({
                            content: response.message,
                            showIcon: true
                          })
                        }
                      })
                      .finally(() => this.loading = false)
      }
    },
    onCancel()
    {
      this.visible = false
    },
    validateMatch(value: string)
    {
      if (value !== String(this.info?.name)) {
        return Promise.reject(new Error(this.$t('dataset.validator.name.match').replace('$VALUE', String(this.info?.name))))
      }
      return Promise.resolve(true)
    }
  }
})
</script>