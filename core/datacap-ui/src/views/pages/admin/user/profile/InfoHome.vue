<template>
  <ShadcnCard :border="false"
              :title="$t('user.common.info')"
              :description="$t('user.tip.info')">
    <ShadcnDivider class="my-2"/>
    <ShadcnRow :gutter="16">
      <ShadcnCol :span="12">
        <ShadcnCard only-content-loading
                    :title="$t('user.common.contribution')"
                    :description="$t('user.tip.contribution')"
                    :loading="loading">
          <div class="p-2">
            <ShadcnContribution :data="heatmap.data"/>
          </div>
        </ShadcnCard>
      </ShadcnCol>
      <ShadcnCol :span="12">
        <ShadcnCard only-content-loading
                    :title="$t('user.common.radar7Days')"
                    :description="$t('user.tip.radar7Days')"
                    :loading="loading">
          <div class="p-2">
            <VisualPie v-if="radar.configuration" :configuration="radar.configuration as any" :height="'200px'" :submitted="false"/>
          </div>
        </ShadcnCard>
      </ShadcnCol>
    </ShadcnRow>
  </ShadcnCard>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { HttpUtils } from '@/utils/http'
import UserService from '@/services/user'
import { Configuration } from '@/views/components/visual/Configuration'
import VisualPie from '@/views/components/visual/components/VisualPie.vue'
import { DateUtils } from '@/utils/date'

export default defineComponent({
  name: 'InfoHome',
  components: {
    VisualPie
  },
  data()
  {
    return {
      loading: false,
      heatmap: {
        data: []
      },
      radar: {
        configuration: null as Configuration | null
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
      axios.all([UserService.getUserContribution(), UserService.getUserContributionRadar()])
           .then(axios.spread((fetchContribution, fetchRadar) => {
             if (fetchContribution.status) {
               this.heatmap.data = fetchContribution.data
               if (fetchContribution.data.length > 0) {
                 if (this.heatmap.data.length > 0) {
                   const item = this.heatmap.data[this.heatmap.data.length - 1] as any
                   this.heatmap.endDate = item.date
                 }
               }
               else {
                 const now = new Date()
                 this.heatmap.endDate = DateUtils.formatTime(now, 'YYYY-MM-DD')
               }
             }
             if (fetchRadar.status) {
               const configuration = new Configuration()
               configuration.columns = fetchRadar.data
               configuration.chartConfigure = { yAxis: 'count', xAxis: 'label', outerRadius: [1.2] }
               this.radar.configuration = configuration
             }
           }))
           .finally(() => this.loading = false)
    }
  }
})
</script>
