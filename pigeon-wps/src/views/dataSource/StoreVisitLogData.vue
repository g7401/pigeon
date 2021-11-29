<template>
  <a-card size="small" :bordered="false">
    <d1-vue-component
      :options="d1Options"
    ></d1-vue-component>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
export default {
  name: 'StoreVisitLogData',
  components: { d1VueComponent },
  data () {
    return {
      d1Options: {
        queryUrl: '/d1/query-suite/execute-query',
        d1ClientBaseUrl: null,
        dataFacetKey: 'rp_fs_dfk',
        pageSize: 10,
        showToolbarButtonList: true,
        asyncExport: true,
        showExportButton: true,
        serialNumber: true,
        openform: true
      }
    }
  }
}
</script>

<style>
</style>
