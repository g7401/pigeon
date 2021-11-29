<template>
  <a-card size="small" :bordered="false">
    <d1-vue-component
      :options="d1Options"
      @onToolbarButtonClick="handleToolbar"
      @onTableOperationButtonClick="handleOperation"
    ></d1-vue-component>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import { mapGetters } from 'vuex'
export default {
  name: 'PlanningInstance',
  components: { d1VueComponent },
  computed: {
    ...mapGetters(['suppliers', 'nickname'])
  },
  data () {
    return {
      d1Options: {
        dataFacetKey: 'rp_plan_instance_dfk',
        d1ClientBaseUrl: '/rp-ms',
        pageSize: 10,
        asyncExport: true,
        showExportButton: true,
        serialNumber: true,
        openform: true,
        showToolbarButtonList: true,
        toolbarButtonList: [
          {
            label: '开始制定计划',
            type: 'primary',
            name: 'goCreatePlan'
          }
        ],
        showTableOperationButton: true,
        tableOperationButtonList: [
          {
            label: 'Details',
            type: 'primary',
            name: 'goDetailPage',
            width: 60
          }
        ]
      }
    }
  },
  created () {
    if (this.suppliers) {
      this.d1Options.preFilters = {
        supplier_name: this.suppliers
      }
    }
  },
  methods: {
    handleToolbar (toolbarName) {
      this[toolbarName]()
    },
    handleOperation (item, operationName) {
      this[operationName](item)
    },
    goDetailPage (item) {
      const pageName = this.nickname === 'admin' ? 'AdminEditPlan' : 'EditPlan'
      this.$router.push({
        name: pageName,
        query: {
          plan_instance_id: item.plan_instance_id
        }
      })
    },
    goCreatePlan () {
      const pageName = this.nickname === 'admin' ? 'AdminCreatePlan' : 'CreatePlan'
      this.$router.push({
        name: pageName
      })
    }
  }
}
</script>

<style>
</style>
