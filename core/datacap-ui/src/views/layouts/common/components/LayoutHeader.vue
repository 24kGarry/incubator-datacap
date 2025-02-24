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
            <LanguageSwitcher @changeLanguage="onChangeLanguage($event)"/>
          </div>

          <div v-if="userInfo" class="mt-2.5">
            <ShadcnLink link="/admin/notify">
              <template v-if="userInfo?.unreadCount > 0">
                <ShadcnBadge dot>
                  <ShadcnIcon icon="Bell" class="hover:text-blue-400" :size="20"/>
                </ShadcnBadge>
              </template>
              <template v-else>
                <ShadcnIcon icon="Bell" class="hover:text-blue-400" :size="20"/>
              </template>
            </ShadcnLink>
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

<script lang="ts">
import { defineComponent, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { TokenUtils } from '@/utils/token'
import router from '@/router'
import { createDefaultRouter } from '@/router/default'
import LanguageSwitcher from '@/views/layouts/common/components/components/LanguageSwitcher.vue'

export default defineComponent({
  name: 'LayoutHeader',
  setup()
  {
    const userStore = useUserStore()
    const { userInfo, isLoggedIn, menu: activeMenus } = storeToRefs(userStore)

    onMounted(async () => {
      if (TokenUtils.getAuthUser()) {
        await userStore.fetchUserInfo()
      }
    })

    const logout = () => {
      userStore.logout()
      createDefaultRouter(router)
      router.push('/auth/signin')
    }

    return {
      userInfo,
      isLoggedIn,
      activeMenus,
      logout
    }
  },
  components: {
    LanguageSwitcher
  },
  methods: {
    onChangeLanguage(language: string)
    {
      this.$emit('changeLanguage', language)
    }
  }
})
</script>