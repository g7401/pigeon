<template>
  <a-card size="small">
    <OperationDetailModal :requestId="activeRequestId" v-model="detailModalVisible"></OperationDetailModal>
    <BaseVxeGrid
      queryApiName="getOperations"
      :formItems="formItems"
      :columns="columns"
      :operationButtonList="operationButtonList"
      :operationWidth="80"
      @onOperationButtonClick="handleOperation"
    >
    </BaseVxeGrid>
  </a-card>
</template>

<script>
import BaseVxeGrid from '@/components/common/BaseVxeGrid'
import OperationDetailModal from '@/components/App/OperationDetailModal'

export default {
  name: 'OperationList',
  components: {
    BaseVxeGrid,
    OperationDetailModal
  },
  data () {
    return {
      detailModalVisible: false,
      activeRequestId: null,
      formItems: [{
        label: 'Request ID',
        key: 'request_id'
      }, {
        label: 'Request URI',
        key: 'request_uri'
      }, {
        label: 'Request Parameters',
        key: 'parameters'
      }],
      columns: [{
        title: 'Request ID',
        field: 'request_id',
        minWidth: 150
      }, {
        title: 'Request URI',
        field: 'request_uri',
        minWidth: 250
      }, {
        title: 'Request Parameters',
        field: 'parameters',
        minWidth: 450
      }, {
        title: 'Create Timestamp',
        field: 'create_timestamp',
        minWidth: 150
      }],
      operationButtonList: [
        {
          label: 'Details',
          type: 'primary',
          key: 'showDetailModal'
        }
      ]
    }
  },
  methods: {
    showDetailModal (item) {
      this.activeRequestId = item.request_id
      this.detailModalVisible = true
    },
    handleOperation (item, operationName) {
      this[operationName](item)
    }
  }
}
</script>
