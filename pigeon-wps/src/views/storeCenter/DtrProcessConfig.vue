<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <RelationGraph class="mg-b-10" :nodes="nodes" :edges="edges"></RelationGraph>
    <a-tabs
      v-model="activeTab"
      size="large"
    >
      <a-tab-pane
        v-for="tab in tabs"
        :key="tab.key"
        :tab="tab.tab"
      >
        <template v-if="tab.key === 'schedule'">
          <div class="op-btn-row">
            <a-button
              class="pull-right"
              type="primary"
              icon="save"
              @click="saveSchedule"
              :loading="saveLoading"
            >Save</a-button>
          </div>
          <a-icon v-if="configLoading" type="loading" />
          <BaseForm
            ref="baseForm"
            v-else
            v-model="form"
            :item="formItem"
            :config="scheduleFormConfig"
            :formItems="scheduleFormItems"
          ></BaseForm>
          <!-- <a-form-model
            :colon="false"
            :model="form"
            :label-col="{ span: 4 }"
            :wrapper-col="{span: 12}"
            ref="scheduleForm"
          >
            <a-form-model-item
              label=" "
              prop="enabled"
            >
              <a-checkbox v-model="form.enabled">Enabled</a-checkbox>
            </a-form-model-item>
            <a-form-model-item
              prop="schedule_type"
              label="Schedule Type"
            >
              <a-radio-group v-model="form.schedule_type">
                <a-radio value="PERIODIC">
                  PERIODIC
                </a-radio>
                <a-radio value="ADHOC">
                  AD-HOC
                </a-radio>
              </a-radio-group>
            </a-form-model-item>
            <a-form-model-item
              label="Schedule Type Ext Details"
              prop="schedule_type_ext_details"
            >
              <a-input
                v-model="form.schedule_type_ext_details"
                placeholder="cron expressions if PERIODIC"
              />
            </a-form-model-item>
          </a-form-model> -->
        </template>
        <template v-else>
          <ProcessorDetail
            v-if="activeTab === tab.key && activeTypeConfig"
            :key="tab.key"
            type="transform"
            :taskUid="activeTypeConfig.taskUid"
            :initDataProps="activeTypeConfig.dataProps"
            :processorUid="activeTypeConfig.processorUid"
            :dtUid="dsUid"
            :dtrTaskDefUid="activeTypeConfig.taskUid"
            createApi="createDtrTaskConfig"
            updateApi="updateDtrTaskConfig"
            @saved="loadGraphData"
          ></ProcessorDetail>
        </template>
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script>
import ProcessorDetail from '@/components/StoreCenter/ProcessorDetail'
import RelationGraph from '@/components/common/RelationGraph'
import BaseForm from '@/components/common/BaseForm'
export default {
  name: 'DtrProcessConfig',
  components: { ProcessorDetail, RelationGraph, BaseForm },
  props: {
    dtrProcessDefUid: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      nodes: [],
      edges: [],
      tabs: [],
      activeTab: '',
      form: {
        enabled: true,
        schedule_type: 'PERIODIC',
        schedule_type_ext_details: undefined
      },
      scheduleFormItems: [
        {
          label: 'Enabled',
          key: 'enabled',
          type: 'checkbox'
        },
        {
          label: 'Schedule Type',
          key: 'schedule_type',
          type: 'radio-group',
          options: [
            {
              label: 'PERIODIC',
              value: 'PERIODIC'
            },
            {
              label: 'AD-HOC',
              value: 'ADHOC'
            }
          ]
        },
        {
          label: 'Schedule Type Ext Details',
          key: 'schedule_type_ext_details',
          placeholder: 'cron expression',
          required: true,
          show: form => form.schedule_type === 'PERIODIC'
        }
      ],
      scheduleFormConfig: { labelCol: { span: 4 }, wrapperCol: { span: 12 } },
      configLoading: false,
      config: null,
      saveLoading: false,
      existSchedule: false,
      activeTypeConfig: null,
      dsUid: null,
      formItem: null
    }
  },
  computed: {
  },
  created () {
    this.loadDtrDetail(this.dtrProcessDefUid)
    this.loadGraphData()
  },
  watch: {
    existSchedule () {
      if (this.existSchedule) {
        this.finishNode('Schedule')
      }
    },
    activeTab (value) {
      this.handleChangeTab(value)
    }
  },
  methods: {
    async loadGraphData () {
      const resp = await this.$api.getDtrProcessConfigGraph(this.dtrProcessDefUid)
      if (!resp || !resp.object) return
      const { nodes, edges } = resp.object
      this.nodes = nodes
      this.edges = edges
    },
    finishNode (taskUid) {
      const node = this.nodes.find(x => x.id === taskUid)
      if (node) {
        node.node_type = 'finish'
      }
    },
    generateGraph (tabs) {
      this.nodes = tabs.map(x => ({
        id: x.key,
        label: x.tab,
        node_type: ''
      }))
      this.edges = Array.from({ length: tabs.length - 1 }).map((item, i) => ({
        source: tabs[i].key,
        target: tabs[i + 1].key
      }))
    },
    async loadDtrDetail (dtrProcessDefUid) {
      this.configLoading = true
      const resp = await this.$api.getDtrConfDetail(dtrProcessDefUid).finally(() => {
        this.configLoading = false
      })
      if (!resp || !resp.object) return
      const taskTabs = resp.object.list_of_dtr_task_def.map(x => ({
        tab: x.name,
        key: x.uid
      }))
      taskTabs.push({
        tab: 'Schedule',
        key: 'schedule'
      })
      this.tabs = taskTabs
      this.activeTab = this.tabs[0].key
      this.dsUid = resp.object.dt_uid
      if (resp.object.dtr_process_conf_schedule) {
        this.existSchedule = true
        this.formItem = resp.object.dtr_process_conf_schedule
      }
    },
    handleChangeTab (key) {
      if (key !== 'schedule') {
        this.loadConfig(key)
      }
    },
    async loadConfig (taskUid) {
      this.activeTypeConfig = null
      const resp = await this.$api.getDtrTaskConfig(taskUid)
      if (resp && resp.object) {
        this.activeTypeConfig = {
          taskUid,
          processorUid: resp.object.processor_uid,
          dataProps: resp.object.processor_data_props
        }
        if (resp.object.processor_data_props) {
          this.finishNode(taskUid)
        }
      }
    },
    async saveSchedule () {
      if (this.$refs.baseForm[0].validate() === false) return
      this.saveLoading = true
      const apiName = this.existSchedule ? 'updateDtrConfSchedule' : 'createDtrConfSchedule'
      const resp = await this.$api[apiName](this.dtrProcessDefUid, this.form).finally(() => {
        this.saveLoading = false
      })
      if (resp && resp.success) {
        this.existSchedule = true
      }
    }
  }
}
</script>

<style lang="less" scoped>
.op-btn-row {
  overflow: hidden;
}
</style>
