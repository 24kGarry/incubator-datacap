<template>
  <ShadcnAlert banner show-icon closable>
    <ShadcnLink link="/admin/chat" target="_blank">
      Support ChatGPT
    </ShadcnLink>
  </ShadcnAlert>

  <div class="border-b">
    <div class="container">
      <div class="flex items-center">
        <div class="flex items-center shrink-0 mt-1">
          <ShadcnLink link="/" class="block">
            <ShadcnAvatar src="/static/images/logo.png" alt="DataCap Logo"/>
          </ShadcnLink>
        </div>

        <ShadcnLayoutHeader class="ml-6">
          <ShadcnMenu direction="horizontal">
            <div v-for="item in activeMenus" :key="item.id">
              <ShadcnMenuSub v-if="item.children" :name="item.id">
                <template #title>
                  <div class="flex items-center space-x-2">
                    <ShadcnIcon v-if="item.icon" :icon="item.icon" size="18"/>
                    <div>{{ item.i18nKey ? $t(item.i18nKey) : 'Unknown' }}</div>
                  </div>
                </template>

                <ShadcnMenuItem v-for="children in item.children"
                                class="w-full"
                                :name="children.id"
                                :active="$route.path === children.url"
                                :to="children.url">
                  <div class="flex items-center space-x-2">
                    <ShadcnIcon v-if="children.icon" :icon="children.icon" size="18"/>
                    <div>{{ children.i18nKey ? $t(children.i18nKey) : 'Unknown' }}</div>
                  </div>
                </ShadcnMenuItem>
              </ShadcnMenuSub>
              <ShadcnMenuItem v-else
                              class="w-full"
                              :name="item.id"
                              :active="$route.path === item.url"
                              :to="item.url">
                <div class="flex items-center space-x-2">
                  <ShadcnIcon v-if="item.icon" :icon="item.icon" size="18"/>
                  <div>{{ item.i18nKey ? $t(item.i18nKey) : 'Unknown' }}</div>
                </div>
              </ShadcnMenuItem>
            </div>
          </ShadcnMenu>
        </ShadcnLayoutHeader>

        <ShadcnSpace size="large">
          <!-- Language Switcher -->
          <div class="mt-2.5 items-center">
            <ShadcnTooltip :content="$t('common.feedback')">
              <ShadcnLink link="https://github.com/devlive-community/datacap" external target="_blank">
                <ShadcnIcon icon="CircleHelp" :size="20"/>
              </ShadcnLink>
            </ShadcnTooltip>
          </div>
          <div class="mt-1">
            <LanguageSwitcher @changeLanguage="onChangeLanguage"/>
          </div>

          <div v-if="userInfo" class="mt-2.5">
            <ShadcnNotification :height="300" :loadData="loadMoreNotifications">
              <template #trigger>
                <ShadcnBadge v-if="userInfo?.unreadCount > 0" dot :text="userInfo?.unreadCount">
                  <ShadcnIcon icon="Bell" class="hover:text-blue-400 cursor-pointer" :size="20"/>
                </ShadcnBadge>
                <ShadcnIcon v-else icon="Bell" class="hover:text-blue-400 cursor-pointer" :size="20"/>
              </template>

              <template #actions>
                <span></span>
              </template>

              <ShadcnNotificationItem v-for="(item, index) in messages"
                                      :key="index"
                                      :item="item"
                                      @on-click="handleNotificationClick">
                <template #title>
                  <div class="mt-1 text-sm text-gray-600">
                    <div v-if="item.entityExists" class="flex space-x-1">
                      <span>{{ $t(`common.${ item.entityType?.toLowerCase() || '' }`) }}</span>

                      <template v-if="item.entityType === 'DATASET'">
                        <RouterLink :to="`/admin/dataset/info/${item.entityCode}`" target="_blank" class="hover:text-blue-400 flex items-center">
                          [ {{ item.entityName }} ]
                        </RouterLink>
                      </template>

                      <template v-else>
                        <ShadcnLink class="hover:text-blue-400" :to="'/' + item.entityType + '/' + item.entityCode">[ {{ item.entityName }} ]</ShadcnLink>
                      </template>

                      <span>{{ $t(`common.${ item.type?.toLowerCase() || '' }`) }}</span>
                    </div>
                    <div v-else>
                      {{ $t(`common.${ item.entityType?.toLowerCase() || '' }`) }} [ {{ item.entityName }} ] {{ $t(`common.${ item.type?.toLowerCase() || '' }`) }}
                    </div>
                  </div>
                </template>

                <template #time>
                  <ShadcnTime relative :reference-time="item.createTime"/>
                </template>
              </ShadcnNotificationItem>
            </ShadcnNotification>
          </div>

          <!-- User Info -->
          <ShadcnSpace v-if="!isLoggedIn">
            <ShadcnButton to="/auth/signin">
              {{ $t('user.common.signin') }}
            </ShadcnButton>
            <ShadcnButton to="/auth/signup" type="default">
              {{ $t('user.common.signup') }}
            </ShadcnButton>
          </ShadcnSpace>
          <div v-else>
            <ShadcnDropdown position="right">
              <template #trigger>
                <ShadcnAvatar class="mt-1"
                              style="width: 2rem;"
                              :src="userInfo?.avatarConfigure?.path"
                              :alt="userInfo?.username">
                </ShadcnAvatar>
              </template>

              <ShadcnDropdownItem>
                <div class="flex flex-col space-y-1">
                  <p class="text-sm font-medium leading-none text-center">{{ userInfo?.username }}</p>
                  <p class="text-xs leading-none text-muted-foreground"></p>
                </div>
              </ShadcnDropdownItem>

              <ShadcnDropdownItem divided>
                <ShadcnLink link="/admin/user">
                  <ShadcnSpace>
                    <ShadcnIcon icon="Settings"/>
                    {{ $t('user.common.setting') }}
                  </ShadcnSpace>
                </ShadcnLink>
              </ShadcnDropdownItem>

              <ShadcnDropdownItem @on-click="logout">
                <ShadcnSpace>
                  <ShadcnIcon icon="LogOut"/>
                  {{ $t('user.common.signout') }}
                </ShadcnSpace>
              </ShadcnDropdownItem>
            </ShadcnDropdown>
          </div>
        </ShadcnSpace>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getCurrentInstance, onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { TokenUtils } from '@/utils/token'
import router from '@/router'
import { createDefaultRouter } from '@/router/default'
import LanguageSwitcher from '@/views/layouts/common/components/components/LanguageSwitcher.vue'
import NotificationService from '@/services/notification'
import { FilterModel } from '@/model/filter.ts'
import { cloneDeep } from 'lodash'

const userStore = useUserStore()
const { proxy } = getCurrentInstance()!
const filter: FilterModel = new FilterModel()
const messages = ref<any[]>([])
const pageIndex = ref<number>(1)
const hasMoreData = ref(true)
const loading = ref(false)

const { userInfo, isLoggedIn, menu: activeMenus } = storeToRefs(userStore)

const emit = defineEmits<{
  changeLanguage: [language: string]
}>()

onMounted(async () => {
  if (TokenUtils.getAuthUser()) {
    await userStore.fetchUserInfo()
    await fetchMessages()
  }
})

const logout = () => {
  userStore.logout()
  createDefaultRouter(router)
  router.push('/auth/signin')
}

const onChangeLanguage = (language: string) => {
  emit('changeLanguage', language)
}

const handleNotificationClick = (message: any) => {
  const { id, code } = message
  const payload = { id, code, isRead: true }
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

const fetchMessages = async (value: number = 1) => {
  filter.page = value
  filter.orders = [{ column: 'createTime', order: 'desc' }]
  loading.value = true
  try {
    const response = await NotificationService.getAll(filter)
    if (response.status && response.data) {
      messages.value = response.data.content.map((item: any) => {
        item.read = item.isRead
        return item
      })
      pageIndex.value = response.data.page
      hasMoreData.value = response.data.page < response.data.totalPage
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

const loadMoreNotifications = async (callback: (items: any[]) => void) => {
  if (loading.value || !hasMoreData.value) {
    callback([])
    return
  }

  const oldData = cloneDeep(messages.value)
  loading.value = true
  pageIndex.value++
  await fetchMessages(pageIndex.value)
  const newItems = messages.value
  messages.value = [...oldData, ...newItems]
  callback(newItems)
  loading.value = false
}
</script>