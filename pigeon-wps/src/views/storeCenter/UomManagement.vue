<template>
  <a-card
    :bordered="false"
    size="small"
  >
    <CustomUomModal
      key="create"
      v-model="createCustomVisiable"
      v-if="createCustomVisiable"
      @saved="afterSaved"
    ></CustomUomModal>
    <CustomUomModal
      key="edit"
      v-model="editCustomVisiable"
      v-if="editCustomVisiable"
      title="Edit Custom UOM"
      apiName="updateCustomUom"
      :item="activeCustomItem"
      :extraFields="['id']"
      @saved="afterSaved"
    ></CustomUomModal>
    <NativeUomModal
      v-model="editNativeVisiable"
      v-if="editNativeVisiable"
      :item="activeNativeItem"
      :extraFields="['id']"
      @saved="afterSaved"
    ></NativeUomModal>
    <d1-vue-component
      ref="d1Com"
      :options="options"
      @onToolbarButtonClick="handleToolbar"
      @onTableOperationButtonClick="handleOperation"
    ></d1-vue-component>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import CustomUomModal from '@/components/StoreCenter/CustomUomModal.vue'
import NativeUomModal from '@/components/StoreCenter/NativeUomModal.vue'
const baseOptions = {
  showPagination: true,
  showForm: true,
  serialNumber: true,
  showTableOperationButton: true,
  openform: true,
  asyncExport: true,
  showExportButton: true
}
export default {
  name: 'UomManagement',
  components: {
    d1VueComponent,
    CustomUomModal,
    NativeUomModal
  },
  data () {
    return {
      options: {
        dataFacetKey: 'material_dfk',
        d1ClientBaseUrl: '/swan-efs',
        showToolbarButtonList: true,
        toolbarButtonList: [
          {
            label: 'Create Custom UOM',
            type: 'primary',
            name: 'createCustomUom'
          }
        ],
        tableOperationButtonList: [
          {
            label: 'Edit Native UOM',
            type: 'primary',
            name: 'editNativeUom',
            show: item => item.is_native
          },
          {
            label: 'Edit Custom UOM',
            type: 'primary',
            name: 'editCustomUom',
            show: item => !item.is_native
          },
          {
            label: 'Delete',
            type: 'danger',
            name: 'delCustomUom',
            show: item => !item.is_native
          }
        ],
        operationWidth: 200,
        ...baseOptions
      },
      createCustomVisiable: false,
      editCustomVisiable: false,
      editNativeVisiable: false,
      activeCustomItem: null,
      activeNativeItem: null
    }
  },
  methods: {
    afterSaved () {
      this.$refs.d1Com.runQuery()
    },
    async createCustomUom (item) {
      this.createCustomVisiable = true
    },
    async editCustomUom (item) {
      this.editCustomVisiable = true
      this.activeCustomItem = item
    },
    async editNativeUom (item) {
      this.editNativeVisiable = true
      this.activeNativeItem = item
    },
    async delCustomUom (item) {
      const resp = await this.$api.delCustomUom(item.id)
      if (resp && resp.success) {
        this.$refs.d1Com.runQuery()
      }
    },
    handleOperation (item, operationName) {
      const operationFn = this[operationName]
      operationFn && operationFn(item)
    },
    handleToolbar (toolbarName) {
      this[toolbarName]()
    }
  }
}
</script>
