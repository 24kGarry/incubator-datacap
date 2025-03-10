<template>
  <ShadcnCard>
    <template #title>
      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <ShadcnIcon icon="Bell" class="w-5 h-5 text-gray-600"/>
          <div class="ml-2 font-normal text-sm">{{ $t('notify.text.center') }}</div>
        </div>
      </div>
    </template>

    <div class="min-h-screen divide-y divide-gray-200 relative">
      <ShadcnSpin v-if="loading" fixed/>

      <ShadcnEmpty v-else-if="messages.length === 0" class="mt-6">
        <template #image>
          <svg class="w-24 h-24 text-gray-400"
               xmlns="http://www.w3.org/2000/svg"
               fill="none"
               viewBox="0 0 24 24"
               stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4"/>
          </svg>
        </template>

        <template #actions>
          <span></span>
        </template>
      </ShadcnEmpty>

      <div v-else>
        <div v-for="message in messages"
             class="p-3 transition-colors border-b"
             :key="message.code"
             :class="{ 'bg-blue-50': !message.isRead }">
          <div class="flex items-start">
            <div class="ml-3 flex-1">
              <div class="flex items-center justify-between">
                <div class="text-sm font-medium text-gray-900">{{ $t(`common.${ message.entityType?.toLowerCase() }`) }}</div>
                <div class="flex items-center space-x-2">
                  <span class="text-xs text-gray-500">{{ message?.createTime }}</span>

                  <ShadcnDropdown trigger="click" position="right">
                    <template #trigger>
                      <ShadcnButton circle size="small">
                        <ShadcnIcon icon="Cog" size="15"/>
                      </ShadcnButton>
                    </template>
                    <ShadcnDropdownItem :disabled="message.isRead" @on-click="handleMarkAsRead(message)">
                      {{ $t('notify.text.markAsRead') }}
                    </ShadcnDropdownItem>
                    <ShadcnDropdownItem @on-click="handleDelete(message)">
                      {{ $t('notify.text.delete') }}
                    </ShadcnDropdownItem>
                  </ShadcnDropdown>
                </div>
              </div>

              <div class="mt-1 text-sm text-gray-600">
                <NotifyMessage :message="message"/>
              </div>
            </div>
          </div>
        </div>
      </div>

      <ShadcnPagination v-if="messages.length > 0"
                        v-model="pageIndex"
                        class="py-2"
                        show-total
                        show-sizer
                        :page-size="pageSize"
                        :total="dataCount"
                        @on-change="onPageChange"
                        @on-prev="onPageChange"
                        @on-next="onPageChange"
                        @on-change-size="onSizeChange"/>
    </div>
  </ShadcnCard>
</template>

<script setup lang="ts">
import { getCurrentInstance, ref } from 'vue'
import NotificationService from '@/services/notification'
import { FilterModel } from '@/model/filter.ts'
import { useUserStore } from '@/stores/user'
import NotifyMessage from '@/views/pages/admin/notify/components/NotifyMessage.vue'

const { proxy } = getCurrentInstance()!

const filter: FilterModel = new FilterModel()
const loading = ref(false)
const messages = ref<any[]>([])
const pageIndex = ref<number>(1)
const pageSize = ref<number>(10)
const dataCount = ref<number>(0)
const userStore = useUserStore()

const handleMarkAsRead = (message: any) => {
  const { id, code } = message
  const payload = {
    id,
    code,
    isRead: true
  }
  NotificationService.saveOrUpdate(payload)
                     .then(response => {
                       if (response.status && response.data) {
                         fetchMessages()
                         userStore.fetchUserInfo()
                       }
                       else {
                         // @ts-ignore
                         proxy.$Message.error({
                           content: response.message,
                           showIcon: true
                         })
                       }
                     })
}

const handleDelete = async (message: any) => {
  NotificationService.deleteByCode(message.code)
                     .then(response => {
                       if (response.status) {
                         fetchMessages()
                         userStore.fetchUserInfo()
                       }
                       else {
                         // @ts-ignore
                         proxy.$Message.error({
                           content: response.message,
                           showIcon: true
                         })
                       }
                     })
}

const fetchMessages = async (value: number = 1) => {
  filter.page = value
  filter.size = pageSize.value
  filter.orders = [{ column: 'createTime', order: 'desc' }]
  loading.value = true
  try {
    const response = await NotificationService.getAll(filter)
    if (response.status && response.data) {
      messages.value = response.data.content
      dataCount.value = response.data.total
      pageSize.value = response.data.size
      pageIndex.value = response.data.page
    }
    else {
      // @ts-ignore
      proxy.$Message.error({
        content: response.message,
        showIcon: true
      })
    }
  }
  finally {
    loading.value = false
  }
}

const onPageChange = (value: number) => {
  fetchMessages(value)
}

const onSizeChange = (value: number) => {
  pageSize.value = value
  fetchMessages(pageIndex.value)
}

fetchMessages()
</script>
