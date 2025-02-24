import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AuthResponse } from '@/model/user/response/auth'
import { TokenUtils } from '@/utils/token'
import Common from '@/utils/common'
import UserService from '@/services/user'

export const useUserStore = defineStore('user', () => {
    // 初始化时从 TokenUtils 获取数据
    const userInfo = ref<AuthResponse>(TokenUtils.getAuthUser() || {} as AuthResponse)
    const menu = ref(TokenUtils.getUserMenu() || [])
    const isLoggedIn = ref(!!TokenUtils.getAuthUser())

    // 获取最新的用户信息
    const fetchUserInfo = async () => {
        try {
            const response = await UserService.getInfo()
            userInfo.value = response.data
            // 更新本地存储
            localStorage.setItem(Common.token, JSON.stringify(response.data))
            isLoggedIn.value = true
        }
        catch (error) {
            console.error('Failed to fetch user info:', error)
        }
    }

    // 更新菜单
    const updateMenu = (newMenu: any) => {
        menu.value = newMenu
        localStorage.setItem(Common.menu, JSON.stringify(newMenu))
    }

    const logout = () => {
        userInfo.value = {} as AuthResponse
        menu.value = []
        isLoggedIn.value = false
        localStorage.removeItem(Common.token)
        localStorage.removeItem(Common.menu)
        localStorage.removeItem(Common.userEditorConfigure)
    }

    return {
        userInfo,
        menu,
        isLoggedIn,
        fetchUserInfo,
        updateMenu,
        logout
    }
})