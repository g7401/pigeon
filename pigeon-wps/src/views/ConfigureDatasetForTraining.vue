<template>
  <div class="confg-dataset">
    <a-card>
      <a-row>
        <a-col v-show="mode === 'view'">
          <a-button
            class="btn-gutter"
            icon="left"
            @click="cancel"
            type="primary"
          ></a-button>
        </a-col>
        <a-col
          v-show="mode === 'edit'"
          :offset="20"
          :span="4"
        >
          <a-button
            class="btn-gutter"
            type="primary"
            @click="save"
            :disabled="data.length == 0"
          >Save</a-button>
          <a-button
            class="btn-gutter"
            @click="cancel"
          >Cancel</a-button>
        </a-col>
      </a-row>
      <H2>Step 1. Select Table</H2>
      <H3> List Of Objects in Feature Repository</H3>
      <a-row style="margin-bottom:36px">
        <a-col :span="8">
          <a-select
            style="width: 100%"
            v-model="dataStoreId"
            :options="dataStore"
            :showSearch="true"
            :disabled="mode === 'view'"
          ></a-select>
        </a-col>
        <a-col :span="3">
          <a-button
            class="btn-gutter"
            type="primary"
            @click="configureColumn"
            v-show="mode === 'edit'"
            :disabled="!dataStoreId"
          >Confirm Selection</a-button>
        </a-col>
      </a-row>
      <H2>Step 2. Configure Columns as Features or Filters</H2>
      <a-row class="bottom-gutter">
        <a-col
          :offset="20"
          :span="4"
        >
          <a-button
            class="btn-gutter"
            type="primary"
            @click="previewDatasets"
          >Preview the Datasets</a-button>
        </a-col>
      </a-row>
      <vxe-grid
        border
        ref="xGrid"
        :data="data"
        :columns="columns"
        :edit-config="tabEdit"
      >
      </vxe-grid>
      <a-modal
        title="Preview the Datasets"
        v-model="datasVisible"
        :width="1200"
        :maskClosable="true"
        :footer="null"
        on-ok="handleOk"
      >
        <a-row :gutter="18">
          <a-form-model
            ref="form"
            :model="form"
          >
            <a-col
              :span="item.type === 'DATETIME_RANGE' ? 12 : 6"
              style="line-height:36px"
              v-for="(item,index) in filters"
              :key="index"
            >
              <a-form-model-item :label="item.key">
                <a-input
                  v-model="form[item.key]"
                  v-if="item.type==='EXACT_MATCHING_TEXT' || item.type==='FUZZY_MATCHING_TEXT'"
                ></a-input>
                <a-date-picker
                  v-model="form[item.key + '_date']"
                  @change="(date, dateString) => {form[item.key] = dateString}"
                  v-if="item.type==='SINGLE_DATE'"
                />
                <a-row v-if="item.type==='SINGLE_DATETIME'">
                  <a-col :span="12">
                    <a-date-picker
                      v-model="form[item.key + '_datetime_date']"
                      @change="(date, dateString) => {form[item.key + '__date__'] = dateString}"
                    />
                  </a-col>
                  <a-col :span="4">
                    <a-time-picker
                      v-model="form[item.key + '_datetime_time']"
                      @change="(date, dateString) => {form[item.key + '__time__'] = dateString}"
                    />
                  </a-col>
                </a-row>
                <a-row v-if="item.type==='DATETIME_RANGE'">
                  <a-col :span="5">
                    <a-date-picker
                      v-model="form[item.key + '_datetime_start_date']"
                      @change="(date, dateString) => {form[item.key + '__start_date__'] = dateString}"
                    />
                  </a-col>
                  <a-col :span="6">
                    <a-time-picker
                      v-model="form[item.key + '_datetime_start_time']"
                      @change="(date, dateString) => {form[item.key + '__start_time__'] = dateString}"
                    />
                  </a-col>
                  <a-col :span="1">
                    <span>to</span>
                  </a-col>
                  <a-col :span="6">
                    <a-date-picker
                      v-model="form[item.key + '_datetime_end_date']"
                      @change="(date, dateString) => {form[item.key + '__end_date__'] = dateString}"
                    />
                  </a-col>
                  <a-col :span="5">
                    <a-time-picker
                      v-model="form[item.key + '_datetime_end_time']"
                      @change="(date, dateString) => {form[item.key + '__end_time__'] = dateString}"
                    />
                  </a-col>
                </a-row>
                <a-range-picker
                  v-model="form[item.key + '_daterange']"
                  @change="(date, dateString) => {form[item.key] = dateString}"
                  v-if="item.type==='DATE_RANGE'"
                />
                <a-row
                  v-if="item.type==='NUMBER_RANGE'"
                  :gutter="12"
                >
                  <a-col :span="8">
                    <a-input-number
                      id="inputNumber"
                      v-model="form[item.key + '__start__']"
                    />
                  </a-col>
                  <a-col :span="2">
                    <span>to</span>
                  </a-col>
                  <a-col :span="11">
                    <a-input-number
                      id="inputNumber"
                      v-model="form[item.key + '__end__']"
                    />
                  </a-col>
                </a-row>
              </a-form-model-item>
            </a-col>
          </a-form-model>
        </a-row>
        <a-row class="bottom-gutter">
          <a-col
            :offset="20"
            :span="4"
          >
            <a-button
              type="primary"
              @click="search()"
            >Search</a-button>
            <a-button
              @click="reset"
            >Reset</a-button>
          </a-col>
        </a-row>
        <div style="height:400px">
          <a-table
            bordered
            :pagination="{showTotal: total => `Total ${total} items`, total: datasCount, onChange: (page, size) => reloadDatas(page, size)}"
            size="small"
            :columns="fieldColums"
            :data-source="datas"
            :scroll="{x: 'max-content'}"
          ></a-table>
        </div>
      </a-modal>
    </a-card>
  </div>
</template>

<script>
import '@/core/vxe-table'
import { searchTableData, saveTableSetting } from '@/api/modelBuilding'
const columns = [
  {
    title: 'No.',
    dataIndex: 'no',
    type: 'seq',
    width: 50
  },
  {
    title: 'Column Name',
    dataIndex: 'db_field_name',
    field: 'db_field_name',
    type: 'span',
    width: 120
  },
  {
    title: 'Column Type',
    dataIndex: 'db_field_type',
    field: 'db_field_type',
    type: 'span',
    width: 120
  },
  {
    title: 'Column Comment',
    dataIndex: 'db_field_comment',
    field: 'db_field_comment',
    type: 'span',
    width: 150
  },
  {
    title: 'As Feature',
    dataIndex: 'table_field_visible',
    field: 'table_field_visible',
    cellRender: { name: 'ASwitch' },
    width: 120
  },
  {
    title: 'Feature Sequence',
    dataIndex: 'table_field_sequence',
    field: 'table_field_sequence',
    editRender: { name: 'AInputNumber' },
    width: 160
  },
  {
    title: 'As Filter',
    dataIndex: 'form_field_visible',
    field: 'form_field_visible',
    cellRender: { name: 'ASwitch' },
    width: 120
  },
  {
    title: 'Filter Sequence',
    dataIndex: 'form_field_sequence',
    field: 'form_field_sequence',
    editRender: { name: 'AInputNumber' },
    width: 160
  },
  {
    title: 'Filter Type',
    dataIndex: 'form_field_query_type',
    field: 'form_field_query_type',
    editRender: {
      name: 'ASelect',
      options: [
        // { label: 'EXACT_MATCHING_TEXT', value: 'EXACT_MATCHING_TEXT' },
        { label: 'FUZZY_MATCHING_TEXT', value: 'FUZZY_MATCHING_TEXT' }
        // { label: 'SINGLE_CHOICE_LIST', value: 'SINGLE_CHOICE_LIST' },
        // { label: 'MULTIPLE_CHOICE_LIST', value: 'MULTIPLE_CHOICE_LIST' },
        // { label: 'DATE_RANGE', value: 'DATE_RANGE' },
        // { label: 'SINGLE_DATE', value: 'SINGLE_DATE' },
        // { label: 'DATETIME_RANGE', value: 'DATETIME_RANGE' },
        // { label: 'SINGLE_DATETIME', value: 'SINGLE_DATETIME' },
        // { label: 'NUMBER_RANGE', value: 'NUMBER_RANGE' }
      ]
    }
  }
]
export default {
  name: 'TrainingConfigureDatasets',
  activated () {
    this.init()
  },
  created () {
    this.getAllTable()
    this.init()
  },
  data () {
    return {
      mode: 'edit',
      dataStore: [],
      dataStoreId: '',
      dataStoreIdSave: '',
      columns,
      data: [],
      datasVisible: false,
      fieldColums: [],
      filters: [],
      tabEdit: { trigger: 'click', mode: 'row' },
      datas: [],
      defaultDataFacetKey: '',
      saveDataFacetKey: '',
      form: {},
      datasCount: 0
    }
  },
  methods: {
    init () {
      if (this.$route.query.mode) {
        this.mode = this.$route.query.mode
      } else {
        this.mode = 'edit'
      }
      if (this.mode === 'edit') {
        this.tabEdit = { trigger: 'click', mode: 'row' }
      } else {
        this.tabEdit = false
      }
      if (this.$route.query.dataFacetKey) {
        this.reload(this.$route.query.dataFacetKey)
      }
    },
    reload (key) {
      this.saveDataFacetKey = key
      this.getTableSetting()
    },
    configureColumn () {
      if (this.dataStoreIdSave !== this.dataStoreId) this.saveDataFacetKey = null
      this.getTableSetting()
    },
    previewDatasets () {
      this.fieldColums = this.data
        .filter(item => item.table_field_visible)
        .map(obj => {
          const x = {}
          x.title = obj.db_field_name
          x.dataIndex = obj.db_field_name
          x.sequence = obj.table_field_sequence
          return x
        })
      this.fieldColums.sort((a, b) => this.sortByKey(a, b, 'sequence'))
      if (this.fieldColums.length === 0) {
        this.$message.error('please check the As Feature')
        return
      }
      this.datasVisible = true
      this.filters = this.data
        .filter(item => item.form_field_visible)
        .map(obj => {
          const x = {}
          x.key = obj.db_field_name
          x.type = obj.form_field_query_type
          x.sequence = obj.form_field_sequence
          return x
        })
      this.filters.sort((a, b) => this.sortByKey(a, b, 'sequence'))
      this.search()
    },
    sortByKey (a, b, key) {
      if (a[key] < b[key]) {
        return -1
      } else if (a[key] === b[key]) {
        return 0
      } else {
        return 1
      }
    },
    handleOk () {
      this.datasVisible = true
    },
    async save () {
      const respone = await saveTableSetting(
        { table_name: this.dataStoreId, data_facet_key: this.defaultDataFacetKey },
        this.data
      )
      if (respone.success) {
        this.saveDataFacetKey = respone.object.df_key
        this.$router.push({
          name: 'createModelManagement',
          query: {
            dataFacetKeyForTraining: this.saveDataFacetKey,
            dataStoreId: this.dataStoreId
          }
        })
      }
    },
    cancel () {
      this.$router.back()
    },
    reloadDatas (page, size) {
      this.search(size, page)
    },
    async search (size = 10, page = 1) {
      let params = {}
      for (const key in this.form) {
        if (key.indexOf('_daterange') > 0 && this.form[key]) {
          const datefile = key.replace('_daterange', '')
          const datarange = this.form[datefile]
          if (datarange) {
            params[datefile + '__start__'] = datarange[0]
            params[datefile + '__end__'] = datarange[1]
          }
        } else if (key.indexOf('_datetime_date') > 0 && this.form[key]) {
          const datetime = key.replace('_datetime_date', '')
          let value = this.form[datetime + '__date__']
          if (value) {
            value = value + ' ' + (this.form[datetime + '__time__'] ? this.form[datetime + '__time__'] : '00:00:00')
            params[datetime] = value
          }
        } else if (key.indexOf('_datetime_start_date') > 0 && this.form[key]) {
          const datetime = key.replace('_datetime_start_date', '')
          let value = this.form[datetime + '__start_date__']
          if (value) {
            value =
              value + ' ' + (this.form[datetime + '__start_time__'] ? this.form[datetime + '__start_time__'] : '00:00:00')
            params[datetime + '__start__'] = value
          }
        } else if (key.indexOf('_datetime_end_date') > 0 && this.form[key]) {
          const datetime = key.replace('_datetime_end_date', '')
          let value = this.form[datetime + '__end_date__']
          if (value) {
            value =
              value + ' ' + (this.form[datetime + '__end_time__'] ? this.form[datetime + '__end_time__'] : '00:00:00')
            params[datetime + '__end__'] = value
          }
        } else if (
          key.indexOf('__date__') > 0 ||
          key.indexOf('__time__') > 0 ||
          key.indexOf('__start_time__') > 0 ||
          key.indexOf('__start_date__') > 0 ||
          key.indexOf('__end_time__') > 0 ||
          key.indexOf('__end_date__') > 0
        ) {
        } else {
          if (typeof this.form[key] !== 'object' && this.form[key]) {
            params[key] = this.form[key]
          }
        }
      }
      params = {
        ...params,
        ...{ data_facet_key: this.defaultDataFacetKey || this.saveDataFacetKey, size: size, page: page - 1 }
      }
      const respone = await searchTableData(params)
      if (respone.success) {
        this.datasCount = respone.object.total
        this.datas = respone.object.content
      }
    },
    reset () {},
    async getAllTable () {
      const response = await this.$api.getAllTable()
      if (response.success) {
        response.object.forEach(element => {
          const obj = {}
          obj.key = element
          obj.title = element
          this.dataStore.push(obj)
        })
      }
    },
    async getTableSetting () {
      const response = await this.$api.getTableSetting(this.dataStoreId, this.saveDataFacetKey)
      if (response.success) {
        this.dataStoreIddatasCountSave = response.object.tableName
        this.dataStoreId = response.object.tableName
        this.data = []
        this.defaultDataFacetKey = response.object.sysDefaultDfKey
        response.object.formTableSetting.forEach(element => {
          this.data.push(element)
        })
      }
    }
  }
}
</script>

<style lang="less" scoped>
.btn-gutter {
  margin-left: 12px;
}
.bottom-gutter {
  margin-bottom: 12px;
}
/deep/ .ant-table td {
  white-space: nowrap;
}

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
/deep/ .ant-form-item-label {
  line-height: 18px;
}
/deep/ .ant-form-item {
  margin-bottom: 0px;
}
</style>
