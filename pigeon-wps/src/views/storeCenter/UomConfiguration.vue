<template>
  <a-card size="small" :bordered="false">
    <a-tabs v-model="activeTab">
      <a-tab-pane v-for="tab in tabs" :key="tab.key" :tab="tab.label">
        <DataSourceSelect :settings="tab.settings"></DataSourceSelect>
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script>
import DataSourceSelect from '@/components/StoreCenter/DataSourceSelect'
export default {
  name: 'UomConfiguration',
  components: { DataSourceSelect },
  data () {
    return {
      activeTab: '0',
      tabs: [
        {
          label: '选择产品规格来源',
          key: '0',
          settings: [{
            getApi: 'getUomDsOptions',
            saveApi: 'saveSelectedUomDs',
            type: 'product_uom'
          }]
        }
      ]
    }
  }
}
</script>

<style>

</style>
