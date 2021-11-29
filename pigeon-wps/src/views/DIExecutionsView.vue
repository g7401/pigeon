<template>
  <a-card
    :bordered="false"
    size="small"
  >
    <CreateInstanceModal
      v-model="showCreateModal"
      @done="handleDone"
    ></CreateInstanceModal>
    <d1-vue-component
      ref="d1"
      :options="generateOption"
      @onToolbarButtonClick="handleToolbar"
      @handleTableRowClick="handleSelect"
    >
      <template #di_process_instance_status="{ val }">
        {{ executionStatusMap[val] }}
      </template>
      <template #operation="{ record }">
        <a-button-group>
          <a-popconfirm
            v-for="button in buttons"
            :key="button.operationType"
            :title="'confirm ' + button.operationType + ' ?'"
            cancel-text="No"
            ok-text="Yes"
            @confirm="changeStatus(record, button.operationType)"
          >
            <a-button
              v-if="button.showStatus.includes(executionStatusMap[record.di_process_instance_status])"
              :type="button.type || 'default'"
              size="small"
              :ghost="button.isGhost"
              :loading="statusLoading"
            >{{ firstLetterUpper(button.operationType) }}</a-button>
          </a-popconfirm>
        </a-button-group>
      </template>
    </d1-vue-component>
    <div
      class="task-instance-box"
      v-if="activeInstance"
    >
      <h3 class="title">Below is the DI Process Pipeline of selected DI Process Instance:({{ activeInstance.di_process_instance_id }})</h3>
      <a-tabs
        type="card"
        size="large"
        v-model="activeTab"
        @change="handleTabChange"
      >
        <a-tab-pane
          v-for="tabKey in tabs"
          :key="tabKey"
          :tab="firstLetterUpper(tabKey)"
        >
          <template v-if="taskInstance">
            <TransformTaskInstance
              v-if="tabKey === 'transform'"
              :taskInstances="taskInstance.units || []"
            ></TransformTaskInstance>
            <TaskInstanceInfo
              v-else
              :type="tabKey"
              :taskInstance="taskInstance"
            >
            </TaskInstanceInfo>
          </template>
          <template v-else>
            <template v-if="!taskLoading">
              <a-divider dashed>no {{ activeTab }}</a-divider>
            </template>
          </template>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-card>
</template>

<script>
import { firstLetterUpper } from '@/components/_util/util'
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import TaskInstanceInfo from '@/components/DIDefinitions/TaskInstanceInfo'
import CreateInstanceModal from '@/components/DIDefinitions/CreateInstanecModal'
import TransformTaskInstance from '@/components/DIDefinitions/TransformTaskInstance'
import { executionStatusMap } from '@/assets/execution/map'

export default {
  name: 'DiExecutions',
  components: {
    d1VueComponent,
    TaskInstanceInfo,
    TransformTaskInstance,
    CreateInstanceModal
  },
  data () {
    return {
      buttons: [
        { operationType: 'cancel', showStatus: ['WAITING', 'RUNNING'], type: 'primary', isGhost: false },
        { operationType: 'suspend', showStatus: ['RUNNING'], type: 'primary', isGhost: false },
        { operationType: 'resume', showStatus: ['SUSPENDED'], type: 'primary', isGhost: false },
        { operationType: 'rollback', showStatus: ['DONE'], type: 'primary', isGhost: false },
        { operationType: 'delete', showStatus: ['CANCELED', 'FAILED', 'ROLLBACKED', 'SUSPENDED'], type: 'danger' }
      ],
      taskLoading: false,
      statusLoading: false,
      showCreateModal: false,
      activeTab: 'import',
      executionStatusMap,
      tabs: ['import', 'extract', 'transform', 'publish'],
      activeInstance: null,
      taskInstance: null,
      generateOption: {
        dataFacetKey: 'di_process_instance_dfk',
        pageSize: 10,
        showToolbarButtonList: true,
        showHlightCurrentRow: true,
        asyncExport: true,
        showExportButton: true,
        serialNumber: true,
        showTableOperationButton: true,
        openform: true,
        operationWidth: 80,
        toolbarButtonList: [
          {
            label: 'Create DI Process Instance',
            type: 'primary',
            name: 'create',
            elColWidth: 3
          }
        ]
      }
    }
  },
  methods: {
    firstLetterUpper,
    async loadTask () {
      if (!this.activeInstance) {
        return
      }
      this.taskInstance = null
      const apiKey = `get${firstLetterUpper(this.activeTab)}DiTaskInstance`
      this.taskLoading = true
      const resp = await this.$api[apiKey](this.activeInstance.di_process_instance_id)
      this.taskLoading = false
      if (resp.success && resp.object) {
        this.taskInstance = resp.object
      }
    },
    handleTabChange () {
      this.loadTask()
    },
    async changeStatus (item, type) {
      this.statusLoading = true
      const apiKey = `${type}ProcessInstance`
      const resp = await this.$api[apiKey](item.di_process_instance_id).catch(() => (this.statusLoading = false))
      this.statusLoading = false
      if (resp && resp.success) {
        this.$refs.d1.runQuery()
      }
    },
    goCreateDefinitionPage () {
      this.$router.push('/di-definition/create')
    },
    handleToolbar (toolbarName) {
      if (toolbarName === 'create') {
        this.showCreateModal = true
      }
    },
    goEditPage (item, mode = 'edit') {
      this.$router.push({
        path: '/di-definition/create',
        query: {
          definition_id: item.di_process_definition_id,
          mode
        }
      })
    },
    goViewPage (item) {
      this.goEditPage(item, 'view')
    },
    handleOperation (item, operationName) {
      const operationMap = {
        delete: this.delete,
        edit: this.goEditPage,
        view: this.goViewPage
      }
      const operationFn = operationMap[operationName]
      operationFn && operationFn(item)
    },
    handleSelect (item) {
      this.activeInstance = item
      this.loadTask()
    },
    handleDone (instance) {
      this.$refs.d1.runQuery()
    }
  }
}
</script>

<style lang="less" scoped>
.title {
  text-align: center;
  font-size: 20px;
  margin-top: 30px;
  margin-bottom: 30px;
}
.task-instance-box {
  margin-top: 20px;
}
/deep/ .ant-divider {
  font-weight: 300;
}
</style>
