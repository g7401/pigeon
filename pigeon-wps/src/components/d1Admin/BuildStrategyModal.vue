<template>
  <div
    class="base-form"
    v-if="value"
  >
    <a-modal
      :title="title"
      :ok-text="mergeConfig.okText"
      :cancel-text="mergeConfig.cancelText"
      :visible="value"
      :confirm-loading="mergeConfig.confirmLoading"
      :width="mergeConfig.width"
      @ok="handleOk"
      @cancel="close"
    >
      <template>
        <base-form
          :ref="formRefName"
          v-model="formData"
          :item="item"
          :formItems="formItems"
        ></base-form>
        <a-form-model
          v-if="formData.build_strategy_type === 'SQL'"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          :colon="false"
        >
          <a-form-model-item label=" ">
            <a-row>
              <a-button
                class="mg-r-20"
                @click="testSql"
                :loading="testLoading"
                :disabled="testDisabled"
              >Test</a-button>
              <span v-if="finishedTest">Below is the test result</span>
            </a-row>
          </a-form-model-item>
          <a-form-model-item
            v-if="finishedTest"
            label=" "
          >
            <SearchTree
              mode="disabled"
              searchAction="change"
              height="500px"
              :tree-data="treeData"
              :replaceFields="replaceFields"
            ></SearchTree>
          </a-form-model-item>
          <a-form-model-item label="Execution Record">
            <vxe-grid
              class="more-small-table mini-scrollbar mg-t-10"
              :loading="queryRecordLoading"
              show-overflow="title"
              show-header-overflow="title"
              :data="tableData"
              :columns="columns"
              :scrollX="{gt: 8}"
              size="mini"
              :pager-config="pagination"
              @page-change="handleTableChange"
            ></vxe-grid>
          </a-form-model-item>
        </a-form-model>
      </template>
    </a-modal>
  </div>
</template>

<script>
import '@/core/vxe-table'
import BaseForm from '@/components/common/BaseForm'
import SearchTree from '@/components/common/SearchTree'
import { mapGetters } from 'vuex'
const baseConfig = {
  okText: 'Save',
  cancelText: 'Cancel',
  width: 1000,
  confirmLoading: false
}

const scheduleTypeOptions = [
  {
    label: 'PERIODIC',
    value: 'PERIODIC'
  },
  {
    label: 'AD-HOC',
    value: 'ADHOC'
  }
]

export default {
  components: { BaseForm, SearchTree },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    value: {
      type: Boolean,
      default: false
    },
    extraFields: {
      type: Array,
      default () {
        return []
      }
    },
    extraData: {
      type: Object,
      default () {
        return {}
      }
    },
    type: {
      type: String,
      default: 'dict'
    }
  },
  data () {
    return {
      item: null,
      formRefName: Math.random()
        .toString()
        .slice(2),
      formData: {
      },
      labelCol: { span: 6 },
      wrapperCol: { span: 18 },
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
        align: 'right',
        pageSizes: [10, 20, 30, 40, 50, 60, 70, 80, 90, 100],
        layouts: ['Total', 'PrevPage', 'JumpNumber', 'NextPage', 'FullJump'],
        border: true
      },
      columns: [
        {
          title: 'No.',
          type: 'seq',
          width: 100
        },
        {
          title: 'UID',
          field: 'uid',
          width: 100
        },
        {
          title: 'Status',
          field: 'status',
          width: 100
        },
        {
          title: 'Start Time',
          field: 'start_timestamp',
          width: 100
        },
        {
          title: 'Done Time',
          field: 'done_timestamp',
          width: 100
        },
        {
          title: 'Failed Time',
          field: 'failed_timestamp',
          width: 100
        },
        {
          title: 'Failed Reason',
          width: 110,
          slots: {
            default: ({ row, column }) => {
              if (row.status === 'FAILED') {
                return [
                  <a-button size="small" loading={row.loading} { ... { on: { click: () => this.loadJobFaildReson(row) } } }
                    >Details</a-button>
                ]
              } else {
                return []
              }
            }
          }
        }
      ],
      replaceFields: { children: 'children', key: 'uid', title: 'name' },
      treeData: [],
      tableData: [],
      queryRecordLoading: false,
      finishedTest: false,
      testLoading: false,
      activeFailedReason: ''
    }
  },
  watch: {
    value: {
      immediate: true,
      async handler () {
        if (!this.value) return
        this.item = null
        this.getStrategyApiName && await this.loadStragery()
        if (this.formData.build_strategy_type === 'SQL') {
          this.$store.dispatch('getD1DataSourceTree')
          this.item && this.item.uid && this.getExecutionRecords()
        }
      }
    }
  },
  computed: {
    title () {
      const map = {
        'dict': 'Manage Dictionary Build Strategy',
        'defaultVal': 'Manage Defaults Build Strategy'
      }
      return map[this.type]
    },
    getStrategyApiName () {
      const map = {
        'dict': 'getDictStrategyByCat',
        'defaultVal': 'getDefaultValStrategyByCat'
      }
      return map[this.type]
    },
    apiName () {
      const map = {
        'dict': 'saveDictStrategy',
        'defaultVal': 'saveDefaultValStrategy'
      }
      return map[this.type]
    },
    updateApiName () {
      const map = {
        'dict': 'updateDictStrategy',
        'defaultVal': 'updateDefaultValStrategy'
      }
      return map[this.type]
    },
    recordApiName () {
      const map = {
        'dict': 'getDictExecutionRecords',
        'defaultVal': 'getDefaultValExecutionRecords'
      }
      return map[this.type]
    },
    testApiName () {
      const map = {
        'dict': 'testDictStrategy',
        'defaultVal': 'testDefaultValStrategy'
      }
      return map[this.type]
    },
    typeOptions () {
      const options = [
        {
          label: 'SQL',
          value: 'SQL'
        }
      ]
      // if (this.type === 'defaultVal') {
      //   options.push({
      //     label: 'Fixed Value',
      //     value: 'FIXED_VALUE'
      //   })
      // }
      return options
    },
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    },
    testDisabled () {
      return !this.formData.ds_uid || !this.formData.sql_statement || !this.formData.schedule_type_ext_details
    },
    formItems () {
      return [
        {
          label: 'Enable',
          key: 'enabled',
          type: 'checkbox',
          default: true
        },
        {
          label: 'Schedule Type',
          key: 'schedule_type',
          type: 'select',
          default: 'PERIODIC',
          options: scheduleTypeOptions,
          disabled: item => item.build_strategy_type === 'FIXED_VALUE'
        },
        {
          label: 'Schedule Type Ext Details',
          key: 'schedule_type_ext_details',
          required: true,
          show: item => item.build_strategy_type === 'SQL' && item.schedule_type === 'PERIODIC'
        },
        {
          label: 'Build Strategy Type',
          key: 'build_strategy_type',
          type: 'select',
          required: true,
          default: 'SQL',
          options: this.typeOptions,
          on: {
            change: (val, formData) => {
              if (val === 'FIXED_VALUE') {
                // eslint-disable-next-line vue/no-side-effects-in-computed-properties
                formData.schedule_type = 'ADHOC'
              }
            }
          }
        },
        {
          label: 'Fixed Value',
          key: 'fixed_value',
          required: true,
          show: item => item.build_strategy_type === 'FIXED_VALUE'
        },
        {
          label: 'Data Source',
          key: 'ds_uid',
          type: 'select',
          required: true,
          options: this.d1DsTree.map(x => ({
            label: x.label,
            value: x.id
          })),
          show: item => item.build_strategy_type === 'SQL'
        },
        {
          label: 'SQL',
          key: 'sql_statement',
          required: true,
          default: '',
          type: 'monaco-editor',
          language: 'sql',
          theme: 'vs-dark',
          show: item => item.build_strategy_type === 'SQL'
        }
      ]
    },
    ...mapGetters(['d1DsTree'])
  },
  methods: {
    async loadJobFaildReson (row) {
      const map = {
        'defaultVal': 'defaults_build',
        'dict': 'dictionary_build'
      }
      this.$set(row, 'loading', true)
      const result = await this.$api.getJobFaildReson(row.uid, map[this.type]).finally(() => {
        row.loading = false
      })
      this.$info({
        title: 'Failed Reason',
        content: result.content,
        okText: 'Close'
      })
    },
    async loadStragery () {
      this.item = await this.$api[this.getStrategyApiName](this.extraData)
    },
    handleTableChange ({ currentPage, pageSize }) {
      this.pagination.currentPage = currentPage
      this.pagination.pageSize = pageSize
      this.getExecutionRecords()
    },
    async testSql () {
      this.finishedTest = false
      this.testLoading = true
      const resp = await this.$api[this.testApiName](this.formData).finally(
        () => {
          this.testLoading = false
        }
      )
      this.treeData = [resp]
      this.finishedTest = true
    },
    async getExecutionRecords () {
      this.queryRecordLoading = true
      const reqParams = {
        page: this.pagination.currentPage - 1,
        size: this.pagination.pageSize
      }
      const resp = await this.$api[this.recordApiName](this.item.uid, reqParams).finally(
        () => {
          this.queryRecordLoading = false
        }
      )
      this.tableData = resp.content
      this.pagination.total = resp.total
    },
    async save () {
      const extraValues = this.extraFields.map(field => this.item && this.item[field])
      const data = { ...this.formData, ...this.extraData }
      let apiKey = this.apiName
      if (this.item && this.item.uid) {
        apiKey = this.updateApiName
        data['uid'] = this.item.uid
      }
      return this.$api[apiKey](...extraValues, data)
    },
    async handleOk () {
      const isValid = this.$refs[this.formRefName].validate()
      if (!isValid) return
      if (this.formData.build_strategy_type === 'SQL' && !this.finishedTest) {
        await this.testSql()
      }
      if (this.apiName) {
        this.config.confirmLoading = true
        this.save()
          .then(() => {
            this.$emit('saved')
            this.close()
          })
          .finally(() => {
            this.config.confirmLoading = false
          })
      } else {
        this.$emit('saved', this.formData)
        this.close()
      }
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
      this.formData = {}
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .ant-form-item {
  margin-bottom: 10px;
}
</style>
