<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <RelationGraph
      class="mg-b-10"
      :nodes="nodes"
      :edges="edges"
    ></RelationGraph>
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
            :type="tab.key"
            :taskUid="activeTypeConfig.taskUid"
            :initDataProps="activeTypeConfig.dataProps"
            :processorUid="activeTypeConfig.processorUid"
            :dsUid="config.ds_uid"
            @saved="loadConfig"
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
  name: 'DaqProcessConfig',
  components: { ProcessorDetail, RelationGraph, BaseForm },
  props: {
    uid: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      nodes: [],
      edges: [],
      tabs: [
        {
          tab: 'Extract',
          key: 'extract'
        },
        {
          tab: 'Load',
          key: 'load'
        },
        {
          tab: 'Schedule',
          key: 'schedule'
        }
      ],
      activeTab: 'extract',
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
      formItem: null
    }
  },
  watch: {
    existSchedule () {
      if (this.existSchedule) {
        this.finishNode('Schedule')
      }
    }
  },
  computed: {
    activeTypeConfig () {
      if (!this.config || this.activeTab === 'schedule') return null
      let dataProps = null
      const props = this.config[`${this.activeTab}_task_data_props`]
      if (props) {
        const allProps = JSON.parse(props)
        dataProps = allProps.processor_data_props
      }
      return {
        taskUid: this.config[`${this.activeTab}_task_def_uid`],
        processorUid: this.config[`${this.activeTab}_task_processor_uid`],
        dataProps
      }
    }
  },
  created () {
    this.loadConfig()
  },
  methods: {
    finishNode (id) {
      const node = this.nodes.find(x => x.id === id)
      node && (node.node_type = 'finish')
    },
    async loadGraphData () {
      const resp = await this.$api.getDaqProcessConfigGraph(this.uid)
      if (!resp || !resp.object) return
      const { nodes, edges } = resp.object
      this.nodes = nodes
      this.edges = edges
    },
    async loadConfig () {
      this.configLoading = true
      const resp = await this.$api.getDapTaskConfig(this.uid).finally(() => {
        this.configLoading = false
      })
      if (resp && resp.object) {
        this.config = resp.object
        if (this.config.schedule_data_props) {
          this.formItem = JSON.parse(this.config.schedule_data_props)
          this.existSchedule = true
        }
        this.loadGraphData()
      }
    },
    async saveSchedule () {
      if (this.$refs.baseForm[0].validate() === false) return
      this.saveLoading = true
      const apiName = this.existSchedule ? 'updateDapConfSchedule' : 'createDapConfSchedule'
      await this.$api[apiName](this.uid, this.form).finally(() => {
        this.saveLoading = false
      })
      this.existSchedule = true
    }
  }
}
</script>

<style lang="less" scoped>
.op-btn-row {
  overflow: hidden;
}
</style>
