<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <a-row :gutter="24">
      <a-col :span="10">
        <h3>Overall</h3>
        <a-form-model layout="vertical">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-model-item label="Attribute Mapping Type">
                <a-select
                  allowClear
                  v-model="params.attr_mapping_type"
                  :options="mappingTypeOptions"
                ></a-select>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="Target Attribute">
                <a-select
                  allowClear
                  v-model="params.target_attr_uid"
                  :options="toOptions(targetAttrs)"
                ></a-select>
              </a-form-model-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-model-item label="Configuration Status">
                <a-select
                  allowClear
                  v-model="params.configured"
                  :options="configOptions"
                ></a-select>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="Attribute Build Strategy">
                <a-select
                  allowClear
                  v-model="params.attr_build_strategy"
                  :options="strategyOptions"
                ></a-select>
              </a-form-model-item>
            </a-col>
          </a-row>
        </a-form-model>
        <a-row>
          <a-button
            class="pull-right mg-b-10"
            type="primary"
            @click="getTargetAttrConfs"
            :loading="queryLoading"
          >Refresh</a-button>
        </a-row>
        <vxe-table
          class="more-small-table row--selectable mini-scrollbar"
          size="mini"
          ref="xTable"
          :data="filterTargetAttrs"
          :loading="queryLoading"
          max-height="600"
          highlight-hover-row
          highlight-current-row
          @current-change="currentChangeEvent"
        >
          <vxe-table-column
            type="seq"
            title="No."
            width="40"
            fixed="left"
          ></vxe-table-column>
          <vxe-table-column
            v-for="column in columns"
            :key="column.dataIndex"
            :field="column.dataIndex"
            :title="column.title"
            :width="column.width"
            :min-width="column.minWidth"
            :fixed="column.fixed"
          >
            <template
              v-if="column.dataIndex === 'configured'"
              v-slot="{ row }"
            >
              {{ row['configured'] ? 'Y' : 'N' }}
            </template>
          </vxe-table-column>
        </vxe-table>
        <!-- <a-table
          :bordered="true"
          class="more-small-table"
          size="small"
          :pagination="false"
          :loading="queryLoading"
          :columns="columns"
          :data-source="filterTargetAttrs"
          :customRow="customRow"
          :rowClassName="rowClassName"
          row-key="target_attr_uid"
          :scroll="{ x: 600, y: '60vh' }"
        >
          <template
            slot="serial"
            slot-scope="text, record, i"
          >
            {{ i + 1 }}
          </template>
          <template
            slot="configured"
            slot-scope="text"
          >
            {{ text ? 'Y' : 'N' }}
          </template>
        </a-table> -->
      </a-col>
      <a-col :span="14">
        <h3>Details</h3>
        <div v-if="selectedTargetAttr && taskConfig">
          <h4>Configuration of {{ selectedTargetAttr.target_attr_name }}</h4>
          <RelationGraph
            class="mg-b-10"
            :nodes="nodes"
            :edges="edges"
          ></RelationGraph>
          <a-card size="small">
            <a-row
              justify="end"
              type="flex"
              v-if="taskConfig"
            >
              <a-button
                class="mg-b-5"
                type="primary"
                @click="saveTaskConfig"
                :loading="saveLoading"
                icon="save"
              >Save</a-button>
            </a-row>
            <div
              class="config-detail"
              v-if="taskConfig"
            >
              <a-form-model-item
                class="strategy-select"
                labelAlign="left"
                :colon="false"
                label="Attribute Build Strategy"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-select
                  :options="strategyOptions"
                  v-model="taskConfig.attr_build_strategy"
                  @change="updatedDataProps = null"
                ></a-select>
              </a-form-model-item>
              <component
                ref="processor"
                :is="componentMap[taskConfig.attr_build_strategy]"
                :importData="dataProps"
                :extraData="{ eb_task_def_uid: taskConfig.eb_task_def_uid}"
                :initInputTargetAttrs="taskConfig.input_target_attrs || []"
                @changeData="updateDataProperties"
                @processorData:update="updateProcessorData"
                @processor:update="updateProcessor"
                @inputTargetAttrs:update="updateInputTargetAttrs"
              ></component>
            </div>
          </a-card>
        </div>
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
import '@/core/vxe-table'
import DirectCopyTemplate from '@/components/StoreCenter/template/DirectCopyTemplate'
import SingleSourceTemplate from '@/components/StoreCenter/template/SingleSourceTemplate'
import MappingFuncExpression from '@/components/StoreCenter/template/MappingFuncExpressionTemplate'
import RelationGraph from '@/components/common/RelationGraph'
import cloneDeep from 'lodash.clonedeep'
export default {
  components: { DirectCopyTemplate, SingleSourceTemplate, MappingFuncExpression, RelationGraph },
  props: {
    epProcessDefUid: {
      type: String,
      default: undefined
    }
  },
  data () {
    return {
      nodes: [],
      edges: [],
      componentMap: {
        DIRECT_COPY: 'DirectCopyTemplate',
        SINGLE_SOURCE_OF_TRUTH: 'SingleSourceTemplate',
        MAPPING_FUNC_EXPRESSION: 'MappingFuncExpression'
      },
      taskConfig: null,
      saveLoading: false,
      selectedTargetAttr: null,
      queryLoading: false,
      targetAttrs: [],
      params: {
        attr_mapping_type: undefined,
        target_attr_name: undefined,
        configured: undefined,
        attr_build_strategy: undefined
      },
      mappingTypeOptions: [
        {
          label: 'One To One',
          value: 'ONE_TO_ONE'
        },
        {
          label: 'Many To One',
          value: 'MANY_TO_ONE'
        },
        {
          label: 'Independent',
          value: 'INDEPENDENT'
        }
      ],
      configOptions: [
        {
          label: 'Y',
          value: true
        },
        {
          label: 'N',
          value: false
        }
      ],
      strategyOptions: [
        {
          label: 'Direct Copy',
          value: 'DIRECT_COPY'
        },
        {
          label: 'Single Source Of Truth',
          value: 'SINGLE_SOURCE_OF_TRUTH'
        },
        {
          label: 'Mapping Func Expression',
          value: 'MAPPING_FUNC_EXPRESSION'
        },
        {
          label: 'None',
          value: undefined
        }
      ],
      columns: [
        // {
        //   title: 'No.',
        //   dataIndex: 'serial',
        //   width: 40,
        //   scopedSlots: { customRender: 'serial' }
        // },
        {
          title: 'Target Attribute Name',
          dataIndex: 'target_attr_name',
          width: 150
        },
        {
          title: 'Attribute Mapping Type',
          dataIndex: 'attr_mapping_type',
          width: 100
        },
        {
          title: 'Configuration Status',
          dataIndex: 'configured',
          scopedSlots: { customRender: 'configured' },
          width: 80
        },
        {
          title: 'Attribute Build Strategy',
          dataIndex: 'attr_build_strategy',
          minWidth: 100
        }
      ],
      updatedDataProps: null,
      processorDataProps: null,
      processorUid: '',
      inputTargetAttrs: [],
      initStrategy: null
    }
  },
  computed: {
    filterTargetAttrs () {
      console.log('this.params', this.params)
      const list = this.targetAttrs.filter(x => {
        let isShow = true
        Object.keys(this.params).forEach(key => {
          if (this.params[key] !== undefined && x[key] !== this.params[key]) {
            isShow = false
          }
        })
        return isShow
      })
      return list
    },
    dataProps () {
      if (!this.taskConfig || !this.taskConfig.processor_data_props || !this.isInitStrategy) return {}
      return JSON.parse(this.taskConfig.processor_data_props)
    },
    isInitStrategy () {
      return this.taskConfig && this.taskConfig.attr_build_strategy === this.initStrategy
    }
  },
  watch: {
    filterTargetAttrs: {
      handler (value) {
        if (!value.length) {
          this.selectedTargetAttr = null
        } else if (!this.selectedTargetAttr) {
          this.$refs.xTable.setCurrentRow(value[0])
          this.handleClickRow(value[0])
        }
      },
      deep: true
    },
    selectedTargetAttr (value) {
      if (this.selectedTargetAttr && this.selectedTargetAttr.eb_task_def_uid) {
        this.loadEbTaskConfig(this.selectedTargetAttr.eb_task_def_uid)
        this.loadGraphData(this.selectedTargetAttr.eb_task_def_uid)
      } else {
        this.taskConfig = null
        this.nodes = []
        this.edges = []
      }
    }
  },
  created () {
    this.getTargetAttrConfs()
  },
  methods: {
    updateInputTargetAttrs (data) {
      this.taskConfig.input_target_attrs = data
    },
    async loadGraphData (taskDefUid) {
      const resp = await this.$api.getEbTaskDefImg(taskDefUid)
      if (!resp || !resp.object) return
      const { nodes, edges } = resp.object
      this.nodes = nodes
      this.edges = edges
    },
    updateDataProperties (data) {
      console.log('update data: ', data)
      // this.taskConfig.processor_data_props = JSON.stringify(data)
      this.updatedDataProps = data
    },
    updateProcessorData (data) {
      this.processorDataProps = data
    },
    updateProcessor (processor) {
      this.processorUid = processor.uid
    },
    async loadEbTaskConfig (ebTaskDefUid) {
      this.taskConfig = null
      const resp = await this.$api.getEbTask(ebTaskDefUid)
      if (!resp || !resp.object) return
      this.taskConfig = resp.object
      this.initStrategy = this.taskConfig.attr_build_strategy
    },
    async saveTaskConfig () {
      if (this.componentMap[this.taskConfig.attr_build_strategy]) {
        const validate = this.$refs.processor.validate && this.$refs.processor.validate()
        if (validate === false) return
      }
      this.saveLoading = true
      const data = cloneDeep(this.taskConfig)
      if (this.updatedDataProps) {
        data.processor_data_props = JSON.stringify(this.updatedDataProps)
      } else {
        data.processor_data_props = JSON.stringify(this.dataProps)
      }
      if (data.processor_data_props === '{}') {
        data.processor_data_props = undefined
      }
      // return
      await this.$api.saveEbTask(data).finally(() => {
        this.saveLoading = false
      })
      this.initStrategy = this.taskConfig.attr_build_strategy
      if (this.selectedTargetAttr && this.selectedTargetAttr.eb_task_def_uid) {
        this.loadGraphData(this.selectedTargetAttr.eb_task_def_uid)
      }
      this.getTargetAttrConfs()
    },
    toOptions (list, label = 'target_attr_name', value = 'target_attr_uid') {
      return list.map(x => ({
        label: x[label],
        value: x[value]
      }))
    },
    async getTargetAttrConfs () {
      this.queryLoading = true
      const resp = await this.$api.getTargetAttrsConfigs(this.epProcessDefUid).finally(() => {
        this.queryLoading = false
      })
      if (!resp || !resp.success) return
      const targetAttrs = resp.object || []
      targetAttrs.sort(function (a, b) {
        const x = a.target_attr_name.toLowerCase()
        const y = b.target_attr_name.toLowerCase()
        if (x < y) {
          return -1
        }
        if (x > y) {
          return 1
        }
        return 0
      })
      this.targetAttrs = targetAttrs
    },
    currentChangeEvent ({ row }) {
      this.handleClickRow(row)
    },
    handleClickRow (row) {
      console.log('row', row)
      this.selectedTargetAttr = row
    }
    // rowClassName (record) {
    //   if (this.selectedTargetAttr && this.selectedTargetAttr.target_attr_uid === record.target_attr_uid) {
    //     return 'active-row'
    //   }
    // },
    // customRow (record) {
    //   return {
    //     on: {
    //       click: () => {
    //         this.clickRow(record)
    //       }
    //     }
    //   }
    // }
  }
}
</script>

<style lang="less" scoped>
/deep/ .active-row {
  background: #bae7ff;
}
/deep/ .ant-table-row {
  cursor: pointer;
}
/deep/ .ant-form-vertical .ant-form-item {
  margin-bottom: 0;
}
/deep/ .strategy-select .ant-select {
  width: 350px;
}
// /deep/ .ant-table-body {
//   max-height: 80vh;
//   overflow: auto;
// }
</style>
