<template>
  <ShadcnCard :title="$t('user.common.notify')" :description="$t('user.tip.notify')" :border="false">
    <ShadcnDivider class="my-2"/>
    <div class="p-3">
      <NotifyForm/>
    </div>
  </ShadcnCard>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import NotifyForm from '@/views/pages/admin/user/profile/components/NotifyForm.vue'

export default defineComponent({
  name: 'NotifyHome',
  components: { NotifyForm }
})
</script>
