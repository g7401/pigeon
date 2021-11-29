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
      @cancel="handleCancel"
    >
      <template>
        <h3 class="ant-form-item-required">选择维度字段（可多选）</h3>
        <SearchTree
          mode="checkbox"
          searchAction="change"
          checkable
          :tree-data="treeData"
          :checkedKeys="checkedKeys"
          :replaceFields="replaceFields"
          draggable
          height="50vh"
          placeholder="过滤"
          @check="handleSelect"
          @drop="handleDrop"
        ></SearchTree>
        <a-radio-group
          class="mg-t-20"
          v-model="activeOption"
        >
          <a-radio value="RETURN_ONLY_SELECTED_FIELDS">显示维度字段和KPI字段(KPI字段默认按聚合配置统计)</a-radio>
          <a-radio value="RETURN_ALL_FIELDS">显示全部字段(KPI字段默认按聚合配置统计，非KPI字段显示空值)</a-radio>
          <a-radio value="ADVANCED">高级</a-radio>
        </a-radio-group>
        <template v-if="activeOption === 'ADVANCED'">
          <h3 class="mg-t-20">选择显示字段</h3>
          <a-card size="small" class="limit-height">
            <a-row :gutter="10" class="field-row" :key="field.field_name + '1'" v-for="field in selectedFields">
              <a-col :span="8">
                <a-select
                  disabled
                  v-model="field.field_name"
                  :options="choosedFieldOptions"
                ></a-select>
              </a-col>
              <a-col :span="5"></a-col>
              <a-col :span="5"></a-col>
              <a-col :span="3">
                <a-select
                  v-model="field.sort_direction"
                  :options="orderByOptions"
                ></a-select>
              </a-col>
              <a-col :span="2"></a-col>
            </a-row>
            <a-row
              class="field-row"
              :gutter="10"
              v-for="(field, index) in newFields"
              :key="field.field_name + '2'"
            >
              <a-col :span="8">
                <a-select
                  v-model="field.field_name"
                  :options="getDisabledFieldOptionsByFieldName(field.field_name)"
                  @change="handleFieldNameChange(field)"
                  :disabled="fieldHasCondition(field)"
                ></a-select>
              </a-col>
              <a-col :span="5">
                <a-select
                  v-model="field.aggregate_function_type"
                  :options="aggregateFunctionOptions"
                  :disabled="getFieldRole(field.field_name) === 'DIMENSION'"
                ></a-select>
              </a-col>
              <a-col :span="5">
                <a-input
                  :value="field.new_field_label"
                  :disabled="fieldHasCondition(field)"
                  @change="labelNameChange(field, $event.target.value)"
                ></a-input>
              </a-col>
              <a-col :span="3">
                <a-select
                  v-model="field.sort_direction"
                  :options="orderByOptions"
                ></a-select>
              </a-col>
              <a-col :span="2">
                <a-popconfirm
                  :title="getDelFieldTip(field)"
                  cancel-text="否"
                  ok-text="是"
                  @confirm="removeField(index)"
                >
                  <a-button
                    type="danger"
                    ghost
                    icon="minus"
                  ></a-button>
                </a-popconfirm>
              </a-col>
            </a-row>
          </a-card>
          <a-row class="mg-t-10" :gutter="10">
            <a-col :span="24">
              <a-button
                class="mg-r-10"
                type="primary"
                ghost
                icon="plus"
                @click="addField('KPI')"
              >KPI字段</a-button>
              <a-tooltip title="维度字段只能进行COUNT()计算">
                <a-button
                  type="primary"
                  ghost
                  icon="plus"
                  @click="addField('DIMENSION')"
                >维度字段</a-button>
              </a-tooltip>
            </a-col>
          </a-row>
          <template v-if="newFieldOptions.length">
            <div class="mg-t-20 mg-b-10"><a-checkbox v-model="havingVisible">高级查询条件</a-checkbox></div>
            <template v-if="havingVisible">
              <a-card size="small" class="limit-height">
                <a-row
                  class="field-row"
                  :gutter="10"
                  v-for="(item, index) in queryConditions"
                  :key="item.new_field_label"
                >
                  <a-col :span="8">
                    <a-select
                      v-model="item.new_field_label"
                      :options="disabledNewFieldOptions"
                    ></a-select>
                  </a-col>
                  <a-col :span="6">
                    <a-select
                      v-model="item.query_condition_type"
                      :options="operators"
                    ></a-select>
                  </a-col>
                  <a-col :span="7">
                    <a-input
                      v-show="needValues(item.query_condition_type)"
                      v-model="item.values"
                    ></a-input>
                  </a-col>
                  <a-col :span="2">
                    <a-button
                      type="danger"
                      ghost
                      icon="minus"
                      @click="removeCondition(index)"
                    ></a-button>
                  </a-col>
                </a-row>
              </a-card>
              <a-row class="mg-t-10" :gutter="10">
                <!-- <a-col :span="5"></a-col>
                <a-col :span="6"></a-col>
                <a-col :span="7"></a-col> -->
                <a-col :span="24">
                  <a-button
                    type="primary"
                    ghost
                    icon="plus"
                    @click="addCondition"
                  >查询条件</a-button>
                </a-col>
              </a-row>
            </template>
          </template>
        </template>
      </template>
      <template slot="footer">
        <a-button key="submit" type="primary" :loading="mergeConfig.confirmLoading" @click="handleOk">
          确认
        </a-button>
        <a-button key="back" @click="clearConfig">
          清空配置
        </a-button>
      </template>
    </a-modal>
  </div>
</template>

<script>
import SearchTree from '@/components/common/SearchTree'

const aggregateFunctionOptions = [
  // {
  //   label: 'None',
  //   value: null
  // },
  {
    label: 'AVG()',
    value: 'AVG'
  },
  {
    label: 'COUNT()',
    value: 'COUNT'
  },
  {
    label: 'MAX()',
    value: 'MAX'
  },
  {
    label: 'MIN()',
    value: 'MIN'
  },
  {
    label: 'SUM()',
    value: 'SUM'
  }
]

const orderByOptions = [
  { label: 'None', value: undefined },
  { label: 'DESC', value: 'DESC' },
  { label: 'ASC', value: 'ASC' }
]

const operators = [
  { label: '=', value: 'EQUAL_TO' },
  { label: '<>', value: 'NOT_EQUAL_TO' },
  { label: '>', value: 'GREATER_THAN' },
  { label: '>=', value: 'GREATER_THAN_OR_EQUAL_TO' },
  { label: '<', value: 'LESS_THAN' },
  { label: '<=', value: 'LESS_THAN_OR_EQUAL_TO' },
  { label: 'BETWEEN', value: 'BETWEEN' },
  { label: 'IS NULL', value: 'IS_NULL' },
  { label: 'IS NOT NULL', value: 'IS_NOT_NULL' },
  { label: "LIKE 'placeholder%'", value: 'LIKE_RIGHT_FUZZY' },
  { label: "LIKE '%placeholder'", value: 'LIKE_LEFT_FUZZY' },
  { label: "LIKE '%placeholder%'", value: 'LIKE_FULL_FUZZY' },
  { label: 'IN', value: 'IN' }
]

const baseConfig = {
  okText: '确认',
  cancelText: '清空配置',
  width: 900,
  confirmLoading: false
}
export default {
  components: { SearchTree },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    title: {
      type: String,
      default: '备选'
    },
    value: {
      type: Boolean,
      default: false
    },
    dfk: {
      type: String,
      default: null
    },
    fields: {
      type: Array,
      default () {
        return []
      }
    },
    choosedFields: {
      type: Array,
      default () {
        return []
      }
    },
    replaceFields: {
      type: Object,
      default () {
        return { children: 'children', key: 'field_name', title: 'title' }
      }
    }
  },
  data () {
    return {
      havingVisible: false,
      aggregateFunctionOptions,
      orderByOptions,
      operators,
      formRefName: Math.random()
        .toString()
        .slice(2),
      activeOption: 'RETURN_ONLY_SELECTED_FIELDS',
      checkedKeys: [],
      newFields: [],
      selectedFields: [],
      queryConditions: []
    }
  },
  watch: {
    checkedKeys () {
      this.selectedFields = this.selectedFields.filter(x => this.checkedKeys.includes(x.field_name))
      const list = this.fields.filter(x => this.checkedKeys.includes(x.field_name) && !this.selectedFields.some(y => y.field_name === x.field_name)).map(x => ({
        field_name: x.field_name,
        sort_direction: null
      }))
      this.selectedFields = [...this.selectedFields, ...list]
    }
  },
  created () {
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    },
    treeData () {
      return this.fields.map(x => ({
        title: [x.field_label, x.field_name, x.field_description].filter(x => x).join(','),
        checkable: true,
        scopedSlots: { title: 'title' },
        ...x
      }))
    },
    choosedFieldOptions () {
      return this.choosedFields.map(x => ({
        label: x.field_label,
        value: x.field_name
      }))
    },
    newFieldOptions () {
      return this.newFields.map(x => ({
        label: x.new_field_label,
        value: x.new_field_label
      }))
    },
    disabledNewFieldOptions () {
      return this.newFieldOptions.map(x => ({
        disabled: this.queryConditions.findIndex(y => y.new_field_label === x.value) !== -1,
        ...x
      }))
    },
    leftNewFieldOptions () {
      return this.disabledNewFieldOptions.filter(x => !x.disabled)
    }
  },
  methods: {
    needValues (type) {
      return !['IS_NULL', 'IS_NOT_NULL'].includes(type)
    },
    getleftFieldOptions (role) {
      return this.getDisabledFieldOptions(role).filter(x => !x.disabled)
    },
    getDisabledFieldOptions (role) {
      return this.getFieldOptions(role).map(x => ({
        disabled: this.newFields.findIndex(y => y.field_name === x.value) !== -1,
        ...x
      }))
    },
    getFieldOptions (role) {
      return this.choosedFields.filter(x => !this.checkedKeys.includes(x.field_name) && x.role === role).map(x => ({
        label: x.field_label,
        value: x.field_name
      }))
    },
    getDisabledFieldOptionsByFieldName (fieldName) {
      return this.getDisabledFieldOptions(this.getFieldRole(fieldName))
    },
    getFieldRole (fieldName) {
      const field = this.choosedFields.find(x => x.field_name === fieldName)
      return field?.role
    },
    fieldHasCondition (field) {
      return !!this.queryConditions.find(x => x.new_field_label === field.new_field_label)
    },
    handleFieldNameChange (field) {
      const rawFiled = this.choosedFields.find(x => x.field_name === field.field_name)
      field.new_field_label = `New ${rawFiled.field_label}`
    },
    handleDrop (info) {
      console.log('info', info)
    },
    getNewFieldOptionValue (field) {
      return `${field.aggregate_function_type} ${field.field_name}`
    },
    labelNameChange (field, labelValue) {
      if (this.newFields.some(x => x.field_label === labelValue)) {
        return this.$message.info('字段 label 重复')
      }
      field.new_field_label = labelValue
    },
    generateDisableOperators (newField) {
      // const oldOperators = this.queryConditions.filter(x => x.new_field_label === newField).map(x => x.query_condition_type)
      return this.aggregateFunctionOptions.map(x => ({
        // disabled: oldOperators.includes(x.value),
        ...x
      }))
    },
    getLeftOperators (rawField) {
      return this.generateDisableOperators(rawField).filter(x => !x.disabled)
    },
    addCondition () {
      let field
      // let leftOperators
      for (let i = 0; i <= this.leftNewFieldOptions.length; i++) {
        field = this.leftNewFieldOptions[i]
        if (!field) {
          this.$message.info('无法创建更多查询条件')
          return
        } else {
          break
        }
        // leftOperators = this.getLeftOperators(field.value)
        // if (leftOperators.length) {
        //   break
        // }
      }
      const base = {
        new_field_label: field.value,
        query_condition_type: operators[0].value,
        values: ''
      }
      this.queryConditions.push(base)
    },
    removeCondition (index) {
      this.queryConditions.splice(index, 1)
    },
    generateDisableAggrFuncs (rawField) {
      // const oldAggrFunc = this.newFields.filter(x => x.field_name === rawField).map(x => x.aggregate_function_type)
      return this.aggregateFunctionOptions.map(x => ({
        // disabled: oldAggrFunc.includes(x.value),
        ...x
      }))
    },
    getLeftAggrFuncs (rawField) {
      return this.generateDisableAggrFuncs(rawField).filter(x => !x.disabled)
    },
    getNewLabel (label, suffix = 'copy') {
      if (this.newFields.some(x => x.field_label === label)) {
        return this.getNewLabel(`${label} ${suffix}`)
      } else {
        return label
      }
    },
    addField (role) {
      if (!this.selectedFields.length) {
        this.$message.info('请先选择备选字段')
        return
      }
      let field
      // let leftAggrFuncs
      const leftKpiFieldOptions = this.getleftFieldOptions(role)
      for (let i = 0; i <= leftKpiFieldOptions.length; i++) {
        field = leftKpiFieldOptions[i]
        if (!field) {
          this.$message.info(`无法创建更多${role}字段`)
          return
        } else {
          break
        }
        // leftAggrFuncs = this.getLeftAggrFuncs(field.value)
        // if (leftAggrFuncs.length) {
        //   break
        // }
      }
      const baseNewField = {
        field_name: field.value,
        aggregate_function_type: role === 'KPI' ? this.aggregateFunctionOptions[0].value : 'COUNT',
        sort_direction: null,
        new_field_label: this.getNewLabel(this.aggregateFunctionOptions[0].value ? `New ${field.label}` : field.label)
      }
      this.newFields.push(baseNewField)
    },
    getDelFieldTip (field) {
      if (this.queryConditions.some(x => x.new_field_label === field.new_field_label)) {
        return `删除该字段会将相关查询条件一起删除，确定删除吗？`
      } else {
        return `确定删除？`
      }
    },
    removeField (index) {
      const field = this.newFields[index]
      this.queryConditions = this.queryConditions.filter(x => x.new_field_label !== field.new_field_label)
      this.newFields.splice(index, 1)
    },
    handleSelect (checkedKeys) {
      this.checkedKeys = checkedKeys
    },
    validate () {
      if (!this.selectedFields.length) {
        this.$message.info('至少选择一个维度字段')
        return false
      }
      if (this.havingVisible && this.queryConditions.some(x => this.needValues(x.query_condition_type) && !x.values)) {
        this.$message.info('高级查询条件必须填值')
        return false
      }
      return true
    },
    async handleOk () {
      if (!this.validate()) return false
      const groupByData = {
        field_processing_type: this.activeOption
      }
      if (this.checkedKeys.length) {
        groupByData['selected_group_by_field_names'] = this.checkedKeys
      }
      if (this.activeOption === 'ADVANCED') {
        groupByData['result_fields'] = [...this.selectedFields, ...this.newFields]
        if (this.havingVisible) {
          groupByData['having_fields'] = this.queryConditions
        }
      }
      this.$emit('selected', groupByData)
      this.close()
    },
    clearConfig () {
      this.checkedKeys = []
      this.activeOption = 'RETURN_ALL_FIELDS'
      this.newFields = []
      this.selectedFields = []
      this.queryConditions = []
      this.$emit('selected', undefined)
      this.close()
    },
    handleCancel () {
      // this.initCheckedKeys()
      this.close()
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .ant-select {
  width: 100%;
}
/deep/ .ant-radio-wrapper {
  display: block;
  margin-bottom: 10px;
}
.limit-height {
  min-height: 56px;
  max-height: 392px;
  overflow: auto;
}
/deep/ .field-row {
  margin-bottom: 10px;
  &:last-child {
    margin-bottom: 0;
  }
}
</style>
