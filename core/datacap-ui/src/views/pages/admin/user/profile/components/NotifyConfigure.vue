<template>
  <ShadcnModal width="40%"
               :model-value="modelValue"
               :mask-closable="false"
               :title="$t('user.common.notify')"
               @on-close="onCancel">

    <ShadcnForm v-model="formState">
      <ShadcnFormItem name="dataset" class="space-y-3" :label="$t('common.dataset')">
        <ShadcnCheckboxGroup v-model="formState.services">
          <ShadcnCheckbox value="CREATED">{{ $t('notify.text.created') }}</ShadcnCheckbox>
          <ShadcnCheckbox value="UPDATED">{{ $t('notify.text.updated') }}</ShadcnCheckbox>
          <ShadcnCheckbox value="DELETED">{{ $t('notify.text.deleted') }}</ShadcnCheckbox>
          <ShadcnCheckbox value="SYNCDATA">{{ $t('notify.text.syncData') }}</ShadcnCheckbox>
        </ShadcnCheckboxGroup>
      </ShadcnFormItem>
    </ShadcnForm>

    <template #footer>
      <ShadcnButton type="primary" @click="handleSave">
        {{ $t('common.confirm') }}
      </ShadcnButton>
    </template>
  </ShadcnModal>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

const props = defineProps<{
  modelValue: boolean,
  item: any
}>()

const formState = reactive({ ...props.item })

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'update:configure', value: any): void
}>()

const onCancel = () => {
  emit('update:modelValue', false)
}

const handleSave = () => {
  emit('update:configure', formState)
  emit('update:modelValue', false)
}
</script>
