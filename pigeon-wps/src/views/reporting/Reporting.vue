<template>
  <div style="height:100%">
    <a-row
      type="flex"
      justify="start"
      :gutter="12"
      style="height:100%"
    >
      <a-col
        :xs="{ span: 24 }"
        :lg="{ span: leftSize }"
      >
        <a-card style="height:100%">
          <div class="btn-bar">
            <a-button
              icon="plus"
              class="btn-gutter"
              @click="init"
            ></a-button>
            <a-button
              icon="save"
              class="btn-gutter"
              :disabled="datas.length == 0"
              @click="saveTemplate"
            ></a-button>
            <a-button
              class="btn-gutter"
              icon="folder"
              @click="openFilters('共享模板')"
            ></a-button>
            <a-button
              icon="menu-fold"
              @click="collapsed"
              style="position:absolute;right:24px"
            ></a-button>
          </div>
          <a-divider>数据源设置</a-divider>
          <a-form :form="form">
            <a-row style="line-height:40px">
              <a-col :span="16">
                <a-form-item
                  label="数据源"
                  :required="true"
                  :labelCol="{ lg: { span: 6 }, sm: { span: 8 } }"
                  :wrapperCol="{ lg: { span: 18 }, sm: { span: 16 } }"
                >
                  <a-select
                    v-decorator="['table']"
                    :options="datasources"
                    :disabled="!edit"
                    :showSearch="true"
                    @change="selectDataSources"
                  ></a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-button
                  type="primary"
                  style="margin-left:8px"
                  :disabled="!columnsButton"
                  @click="confirmDataSource"
                >Confirm Selection</a-button>
              </a-col>
            </a-row>
            <div style="marin">
              <a-button
                class="item-btn-gutter"
                type="primary"
                :disabled="!edit"
                @click="initial"
              >Or Initial Creation by Upload Reporting Datasets</a-button>
            </div>
            <div v-if="showUpRptsDatas">
              <a-button
                type="primary"
                class="item-btn-gutter"
                @click="additional"
              >Additional Upload Reporting Datasets</a-button>
            </div>
            <a-modal
              title="选择数据列"
              v-model="columnsVisible"
              :maskClosable="true"
              :footer="null"
              on-ok="handleOk"
            >
              <a-transfer
                :data-source="allFieldColums"
                :titles="['Source', 'Target']"
                :lazy="false"
                show-search
                :list-style="{
                  height: '400px'
                }"
                :target-keys="fieldColumsKey"
                :render="item => `${item.title}`"
                @change="handleChange"
              >
                <span slot="notFoundContent">没数据</span>
              </a-transfer>
            </a-modal>
            <a-row style="margin-bottom:8px">
              <a-col :span="2">
                <a-button
                  @click="showDatasModal"
                  icon="fullscreen"
                />
              </a-col>
              <a-col
                :offset="16"
                :span="3"
              >
                <a-button
                  shape="circle"
                  :disabled="allFieldColums.length === 0"
                  icon="database"
                  @click="selectFields"
                />
              </a-col>
              <a-col :span="2">
                <a-button
                  icon="filter"
                  @click="openFilters('过滤条件')"
                />
              </a-col>
            </a-row>
            <a-modal
              title="数据展示"
              v-model="datasVisible"
              width="800"
              :maskClosable="true"
              :footer="null"
              on-ok="handleOk"
            >
              <div style="width:100%;height:460px">
                <a-table
                  :pagination="{
                    showTotal: total => `共 ${total} 条`,
                    total: datasCount,
                    onChange: (page, size) => reloadDatas(page, size),
                    current: currenPage
                  }"
                  bordered
                  :scroll="{x: 'max-content'}"
                  size="small"
                  :columns="fieldColums"
                  :data-source="datas"
                ></a-table>
              </div>
            </a-modal>
            <div style="min-height:300px">
              <a-table
                rowKey="key"
                size="small"
                bordered
                :columns="fieldColums"
                :data-source="datas"
                :pagination="{
                  showTotal: total => `共 ${total} 条`,
                  total: datasCount,
                  onChange: (page, size) => reloadDatas(page, size),
                  current: currenPage
                }"
                :scroll="{ x: 'max-content' }"
              ></a-table>
            </div>
          </a-form>
        </a-card>
      </a-col>
      <a-col
        :xs="{ span: 24 }"
        :lg="{ span: rightSize }"
      >
        <a-drawer
          :title="drawerTitle"
          width="600"
          placement="left"
          :visible="visible"
          :get-container="false"
          :zIndex="zIndex"
          :wrap-style="{ position: 'absolute' }"
          @close="onClose"
        >
          <filter-form
            ref="$filters"
            :show="drawerTitle === '过滤条件'"
            :allFieldColums="allFieldColums"
            :filterOption="filterOption"
            :validateFields="validateFields"
            @reloadDatas="reloadDatas"
          ></filter-form>
          <template-list
            ref="$templates"
            @loadTemplate="loadTemplate"
            :show="drawerTitle === '共享模板'"
          ></template-list>
        </a-drawer>
        <a-card style="height:100%">
          <div v-show="fullscreenStatus !== 'fullscreen'">
            <div class="charts-bar">
              <a-button
                @click="changeType('bar')"
                :type="chartType == 'bar' ? 'primary' : 'defalut'"
                class="btn-gutter"
                icon="bar-chart"
              />
              <a-button
                @click="changeType('line')"
                :type="chartType == 'line' ? 'primary' : 'defalut'"
                class="btn-gutter"
                icon="line-chart"
              />
              <a-button
                @click="changeType('pie')"
                :type="chartType == 'pie' ? 'primary' : 'defalut'"
                class="btn-gutter"
                icon="pie-chart"
              />
              <a-button
                @click="changeType('scatter')"
                :type="chartType == 'scatter' ? 'primary' : 'defalut'"
                class="btn-gutter"
                icon="dot-chart"
              />
              <a-button
                @click="collapsed"
                style="position:absolute;right:24px"
                type="primary"
                v-show="isCollapsed"
                icon="menu-unfold"
              />
            </div>
            <div class="chartParmas">
              <div
                v-show="chartType === 'bar' || chartType === 'line'"
                class="bar-chart line-chart params-item"
              >
                <bar-or-line-params
                  ref="$barOrLineParams"
                  :filterOption="filterOption"
                  :fieldColums="fieldColums"
                  :validateFields="validateFields"
                  :groupBy="groupBy"
                  :chartType="chartType"
                ></bar-or-line-params>
              </div>
              <div
                v-show="chartType == 'pie'"
                class="pie-chart params-item"
              >
                <pie-params
                  ref="$pieParams"
                  :filterOption="filterOption"
                  :fieldColums="fieldColums"
                  :validateFields="validateFields"
                ></pie-params>
              </div>
              <div
                v-show="chartType == 'scatter'"
                class="dot-chart params-item"
              >
                <scatter-params
                  ref="$scatterParams"
                  :filterOption="filterOption"
                  :fieldColums="fieldColums"
                  :validateFields="validateFields"
                  :groupBy="groupBy"
                ></scatter-params>
              </div>
            </div>
          </div>
          <a-button
            class="btn-gutter"
            @click="draw"
            :disabled="datas.length == 0"
          >渲染图表</a-button>
          <a-button
            @click="fullscreenDo"
            type="primary"
            v-show="fullscreenStatus === 'ready'"
            icon="fullscreen"
          ></a-button>
          <a-button
            @click="fullscreenExit"
            type="primary"
            v-show="fullscreenStatus === 'fullscreen'"
            icon="fullscreen-exit"
          ></a-button>
          <div
            id="chart-wrapper"
            :style="chartsStyle"
          ></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style>
.btn-gutter {
  margin-right: 24px;
}
.item-btn-gutter {
  margin-bottom: 24px;
}
.params-item {
  padding-top: 16px;
}
.chart-icon-size {
  font-size: 60px;
  color: chartreuse;
}
</style>
<style lang="less" scoped>
/deep/ .ant-form-item {
  margin-bottom: 16px;
}
/deep/ .ant-table td {
  white-space: nowrap;
}
/deep/ .ant-table-small > .ant-table-content > .ant-table-body > table > .ant-table-thead > tr > th,
/deep/
  .ant-table-small
  > .ant-table-content
  > .ant-table-scroll
  > .ant-table-body
  > table
  > .ant-table-thead
  > tr
  > th {
  padding: 5px 8px;
  font-size: 13px;
  background: #fafafa;
}
/deep/ .ant-table-small.ant-table {
  font-size: 12px;
  line-height: 1.2;
}
/deep/ .ant-pagination.mini.ant-table-pagination {
  margin: 8px 0;
}
</style>

<script>
import echarts from '@/core/echarts_lib/echarts'
import TemplateList from './components/TemplateList'
import FilterForm from './components/FiltersForm'
import PieParams from './components/PieParams'
import ScatterParams from './components/ScatterParams'
import BarOrLineParams from './components/BarOrLineParams'
import { get, post } from './api/api'
import pageStateMixin from '@/views/mixins/pageState'

export default {
  name: 'Reporting',
  mixins: [pageStateMixin],
  components: {
    TemplateList,
    FilterForm,
    PieParams,
    ScatterParams,
    BarOrLineParams
  },
  data () {
    return {
      fullscreenStatus: 'normal',
      visible: false,
      showUpRptsDatas: false,
      zIndex: -100,
      edit: false,
      chartType: 'bar',
      datasVisible: false,
      columnsVisible: false,
      columnsButton: false,
      allFieldColums: [],
      allFieldType: {},
      fieldColumsKey: [],
      fieldColums: [],
      currenPage: 0,
      datas: [],
      datasources: [],
      form: this.$form.createForm(this),
      wrapper: null,
      options: {},
      isCollapsed: false,
      leftSize: 8,
      rightSize: 16,
      drawerTitle: '',
      currenTable: {},
      filters: [],
      datasCount: 0,
      chartsStyle: {
        width: '100%',
        height: '400px'
      },
      currentDataSource: '',
      createReportDatasets: false,
      updateReportDatasets: false,
      tableDIObject: {}
    }
  },
  methods: {
    init () {
      // clear datasource， table, filters，parmas
      this.edit = true
      this.columnsButton = false
      this.currenTable = {}
      this.fieldColums = []
      this.fieldColumsKey = []
      this.allFieldColums = []
      this.allFieldType = {}
      this.datas = []
      this.filters = []
      this.chartType = 'bar'
      this.form.resetFields()
      this.$refs.$barOrLineParams.init()
      this.$refs.$pieParams.init()
      this.$refs.$scatterParams.init()
      this.$refs.$filters.init()
      this.wrapper.clear()
      this.drowInit()
      this.fullscreenStatus = 'normal'
      this.showUpRptsDatas = false
      this.createReportDatasets = false
      this.updateReportDatasets = false
    },
    saveTemplate () {
      const $this = this
      this.$confirm({
        title: '保存模板',
        content: h => <input id="templateName" placeholder="请输入模板名称" type="text" class="ant-input" />,
        async onOk () {
          // 填入方案名称
          // todo 验证条件等表单信息
          const templateName = document.getElementById('templateName').value
          if (!templateName) {
            $this.$message.error('请输入模板名称')
            return false
          }
          let template = {}
          template.name = templateName
          const table = $this.currenTable
          template.dataSource = table.dataSource
          template.dataSourceName = table.dataSourceName
          template.chartType = $this.chartType
          template = { ...template, ...$this.$refs.$barOrLineParams.getFieldsValue() }
          template = { ...template, ...$this.$refs.$pieParams.getFieldsValue() }
          template = { ...template, ...$this.$refs.$scatterParams.getFieldsValue() }
          template.filters = $this.filters
          template.filedImteds = $this.fieldColums.map(item => {
            const obj = {}
            obj.field = item.dataIndex
            obj.fieldName = item.title
            obj.type = item.type
            return obj
          })
          console.log('template:' + template)
          // 调用服务端接口
          const response = await post('insert/template', null, template)
          if (response.success) {
            $this.$refs.$templates.addTemplate({
              id: response.obj.id,
              name: response.obj.name,
              data_source_name: response.obj.dataSourceName,
              chart_type: response.obj.chartType
            })
            $this.$message.success('保存成功！')
          } else {
            $this.$message.error(response.msg)
          }
        },
        onCancel () {
          console.log('Cancel')
        }
      })
    },
    loadTemplate (template) {
      console.log(template)
      // 加载模板
      this.init()
      this.changeType(template.chartType)
      this.form.setFieldsValue({
        table: JSON.stringify({ dataSource: template.dataSource, dataSourceName: template.dataSourceName })
      })
      this.$refs.$barOrLineParams.setFieldsValue({
        categoryField: template.categoryField,
        categoryLocation: template.categoryLocation,
        valueField: template.valueField,
        seriesField: template.seriesField
      })
      this.$refs.$pieParams.setFieldsValue({
        pieCategoryField: template.pieCategoryField,
        pieValueField: template.pieValueField
      })
      this.$refs.$scatterParams.setFieldsValue({
        dotValue1Field: template.dotValue1Field,
        dotValue2Field: template.dotValue2Field,
        dotSizeField: template.dotSizeField,
        dotSeriesField: template.dotSeriesField
      })
      this.$refs.$filters.reloadFilter(template.filters)
      this.fieldColumsKey = template.filedImteds.map(item => item.field)
      this.getAllFieldColums({ dataSource: template.dataSource, dataSourceName: template.dataSourceName }, () => {
        this.getfieldColums()
        this.reloadDatas(0, 10, template.filters)
        this.draw()
      })
      this.openFilters('')
    },
    openFilters (title) {
      this.drawerTitle = title
      this.visible = !this.visible
      if (this.visible) {
        setTimeout(() => {
          this.zIndex = 100
        }, 300)
      } else {
        this.zIndex = -100
      }
    },
    onClose () {
      this.visible = false
      this.zIndex = -100
    },
    changeType (type) {
      this.chartType = type
    },
    async reloadDatas (page, size, filters) {
      this.currenPage = page
      // 刷新数据
      if (filters !== undefined) this.filters = [...filters]
      const columns = this.fieldColumsKey.join(',')
      if (!columns) {
        this.$message.error('请先选择数据列！')
        return
      }
      const table = this.currenTable.dataSource
      if (filters && filters.length > 0) {
        filters.forEach(element => {
          element.type = this.allFieldType[element.field]
        })
      }
      if (this.createReportDatasets || this.updateReportDatasets) {
        if (!this.filters) filters = []
        this.filters.push({
          field: 'dpiid',
          operator: 'equals',
          value: this.$route.query.di_process_instance_id,
          type: 'varchar'
        })
      }
      const response = await post(
        'query/datas_filter',
        {
          table: table,
          columns: columns,
          page: page,
          size: size
        },
        this.filters
      )
      if (response.success) {
        this.datas = response.obj.records
        this.datasCount = response.obj.total
        if (this.datasCount === 0) {
          this.$message.warning('no datas')
        }
        // if (filters) this.$message.info('刷新数据成功！')
      } else {
        this.$message.error(response.msg)
      }
    },
    collapsed () {
      this.isCollapsed = !this.isCollapsed
      if (this.isCollapsed) {
        this.leftSize = 0
        this.rightSize = 24
      } else {
        this.leftSize = 8
        this.rightSize = 16
      }
      setTimeout(() => {
        this.wrapper.resize()
      }, 500)
    },
    filterOption (input, option) {
      return option.componentOptions.propsData.title.indexOf(input) >= 0
    },
    validateFields (form) {
      let flag = true
      form.validateFields((err, values) => {
        if (err) {
          this.$message.error('请填写完整的信息！')
          flag = false
        }
      })
      return flag
    },
    showDatasModal () {
      this.datasVisible = true
    },
    handleOk (e) {
      setTimeout(() => {
        this.datasVisible = true
        this.columnsVisible = true
      }, 2000)
    },
    selectDataSources (value) {
      this.columnsButton = true
    },
    async confirmDataSource () {
      const table = JSON.parse(this.form.getFieldsValue().table)
      if (this.tableDIObject[table.dataSource]['dsGroup'] === 'UPLOAD_REPORTING_DATASETS') {
        this.showUpRptsDatas = true
      } else {
        this.showUpRptsDatas = false
      }
      if (table.dataSource === this.currenTable.dataSource && this.currenTable) return false
      await this.getAllFieldColums(table)
      this.fieldColumsKey = this.allFieldColums.map(item => item.key)
      this.getfieldColums()
      this.reloadDatas(0, 10)
      this.$refs.$filters.init()
    },
    selectFields () {
      // 弹窗，选择显示列
      this.columnsVisible = true
    },
    async getAllFieldColums (table, fn) {
      const response = await get('query/columns/' + table.dataSource, null)
      if (response.success) {
        response.obj.forEach(element => {
          element.key = element.dataIndex
          element.title = element.title || element.dataIndex
          this.allFieldType[element.key] = element.type
        })
        this.allFieldColums = response.obj
        this.currenTable = table
        if (fn) fn()
      } else {
        this.$message.error(response.msg)
      }
    },
    handleChange (targetKeys, direction, moveKeys) {
      this.fieldColumsKey = targetKeys
    },
    getDatas () {
      // todo 获取初始化据
      this.getfieldColums()
      this.columnsVisible = false
      this.reloadDatas(0, 10)
    },
    getfieldColums () {
      this.fieldColums = this.fieldColumsKey.map(item => {
        return this.allFieldColums.filter(all => {
          return all.key === item
        })[0]
      })
    },
    groupBy (arr, func) {
      return arr
        .map(val => val[func])
        .reduce((acc, val, i) => {
          acc[val] = (acc[val] || []).concat(arr[i])
          return acc
        }, {})
    },
    async draw () {
      // 先清除图表
      this.wrapper.clear()
      this.chartOptionInit()
      const dimensions = this.fieldColums.map((item, index) => {
        return item.key
      })
      let valueField = ''
      let groupByFields = ''
      if (this.chartType === 'bar' || this.chartType === 'line') {
        // 验证数据填写
        if (!this.validateFields(this.$refs.$barOrLineParams.form)) return false
        // 获取后台计算后数据
        const value = this.$refs.$barOrLineParams.getFieldsValue()
        groupByFields += value.categoryField
        if (value.seriesField) groupByFields += ',' + value.seriesField
        valueField = value.valueField
        const response = await this.getChartsData(valueField, groupByFields)
        if (!response.success) {
          this.$message.error(response.msg)
          return
        }
        // 获取渲染option
        this.$refs.$barOrLineParams.getOptions(this.options, response.obj, dimensions)
      } else if (this.chartType === 'pie') {
        // 验证数据填写
        if (!this.validateFields(this.$refs.$pieParams.form)) return false
        const value = this.$refs.$pieParams.getFieldsValue()
        valueField = value.pieValueField
        groupByFields = value.pieCategoryField
        const response = await this.getChartsData(valueField, groupByFields)
        if (!response.success) {
          this.$message.error(response.msg)
          return
        }
        this.$refs.$pieParams.getOptions(this.options, response.obj, dimensions)
      } else if (this.chartType === 'scatter') {
        // 验证数据填写
        if (!this.validateFields(this.$refs.$scatterParams.form)) return false
        const value = this.$refs.$scatterParams.getFieldsValue()
        valueField = "'1'"
        groupByFields += value.dotValue1Field
        groupByFields += ',' + value.dotValue2Field
        if (value.dotSizeField) groupByFields += ',' + value.dotSizeField
        if (value.dotSeriesField) groupByFields += ',' + value.dotSeriesField
        const response = await this.getChartsData(valueField, groupByFields)
        if (!response.success) {
          this.$message.error(response.msg)
          return
        }
        this.$refs.$scatterParams.getOptions(this.options, response.obj, dimensions)
      }
      this.wrapper.setOption(this.options)
      this.fullscreenStatus = 'ready'
    },
    fullscreenDo () {
      this.fullscreenStatus = 'fullscreen'
      this.chartsStyle.height = document.body.clientHeight - 180 + 'px'
      this.fullCollapsed(!this.isCollapsed)
    },
    fullscreenExit () {
      this.fullscreenStatus = 'ready'
      this.chartsStyle.height = '400px'
      this.fullCollapsed(this.isCollapsed)
    },
    fullCollapsed (collapsed) {
      if (collapsed) {
        this.collapsed()
      } else {
        setTimeout(() => {
          this.wrapper.resize()
        }, 200)
      }
    },
    getChartsData (valueField, groupByFields) {
      return post(
        'query/datas_charts',
        { table: this.currenTable.dataSource, value_field: valueField, groupBy_fields: groupByFields },
        this.filters
      )
    },
    drowInit () {
      this.chartOptionInit()
      this.wrapper.setOption(this.options)
    },
    chartOptionInit () {
      this.options = {
        xAxis: { type: 'category' },
        yAxis: {},
        legend: {
          type: 'scroll',
          right: 40,
          top: 5
        },
        grid: {
          top: '10%',
          bottom: '15%'
        },
        tooltip: {},
        toolbox: {
          show: true,
          orient: 'vertical',
          feature: {
            saveAsImage: {}
          }
        }
      }
    },
    initial () {
      this.$router.push({
        name: 'uploadReportingDatasets'
      })
    },
    additional () {
      this.$router.push({
        name: 'additionalUploadReportingDatasets',
        query: {
          definition_id: this.tableDIObject[this.$route.query.table_name]['diProcessDefinitionId']
        }
      })
    },
    async backReload () {
      if (this.$route.query.table_name && this.$route.query.di_process_instance_id) {
        const tableName = this.$route.query.table_name
        // this.init()
        this.edit = true
        const value = JSON.stringify({ dataSource: tableName, dataSourceName: tableName })
        if (!this.tableDIObject[tableName]) {
          this.datasources.push({ value: value, key: tableName, title: tableName })
          this.tableDIObject[tableName] = {
            diProcessDefinitionId: this.$route.query.di_process_instance_id
          }
        }
        this.form.setFieldsValue({
          table: value
        })
        await this.getAllFieldColums({ dataSource: tableName, dataSourceName: tableName })
        this.fieldColumsKey = this.allFieldColums.map(item => item.key)
        this.getfieldColums()
        this.$refs.$filters.init()
        this.createReportDatasets = true
        this.updateReportDatasets = true
        this.reloadDatas(0, 10)
      }
    }
  },
  async mounted () {
    setTimeout(() => {
      this.wrapper = echarts.init(document.getElementById('chart-wrapper'))
      this.drowInit()
    }, 1000)
    const response = await get('query/data_sources/', null)
    if (response.success) {
      response.obj.forEach(element => {
        element.key = element.table_name
        element.title = element.table_name
        element.value = JSON.stringify({
          dataSource: element.table_name,
          dataSourceName: element.table_name
        })
        this.tableDIObject[element.key] = {
          dsGroup: element.ds_group,
          diProcessDefinitionId: element.di_process_definition_id
        }
      })
      this.datasources = response.obj
    } else {
      console.log(response.msg)
    }
    this.backReload()
  }
}
</script>
