<template>
  <div>
    <h3 class="title ant-form-item-required">Input Table(s)</h3>
    <a-row>
      <a-col :span="14">
        <a-row
          class="row-box mg-b-5"
          v-for="(row, index) in dataProperties.inputTables"
          :key="index"
          :gutter="15"
        >
          <a-col :span="7">
            <a-select
              style="width: 100%"
              :options="srcTypeOptions"
              v-model="row.dt_src_type"
              @change="change"
            ></a-select>
          </a-col>
          <a-col
            :span="15"
          >
            <a-select
              style="width: 100%"
              :options="srcOptionsMap[row.dt_src_type].options"
              :placeholder="srcOptionsMap[row.dt_src_type].placeholder"
              v-model="row.src_uid"
              @change="change"
            ></a-select>
          </a-col>
          <a-col :span="2">
            <a-button
              type="danger"
              ghost
              icon="minus"
              @click="removeRow(index)"
              @change="change"
            ></a-button>
          </a-col>
        </a-row>
        <a-row :gutter="15">
          <a-col :span="7"></a-col>
          <a-col :span="15"></a-col>
          <a-col :span="2">
            <a-button
              type="primary"
              ghost
              icon="plus"
              @click="addRow"
            ></a-button>
          </a-col>
        </a-row>
      </a-col>
    </a-row>
    <h3 class="title ant-form-item-required mg-t-10">Processing Logic</h3>
    <a-form-model
      :model="dataProperties"
      :rules="rules"
      ref="sql"
    >
      <a-form-model-item
        prop="sqlStatement"
        style="margin-bottom:0"
      >
        <MonacoEditor
          class="editor mg-b-10"
          v-model="dataProperties.sqlStatement"
          @change="change"
          language="sql"
          theme="vs-dark"
          :options="{ automaticLayout: true }"
          style="width: 100%; height: 300px;"
        />
      </a-form-model-item>
      <a-form-model-item label="">
        <a-button
          :loading="validateLoading"
          @click="validateSql"
        >Validate</a-button>
      </a-form-model-item>
    </a-form-model>

    <h3
      class="title mg-t-10"
    >Output Table(s)</h3>
    <div
      class="btn-group mg-b-10"
    >
      <a-upload
        :multiple="false"
        :action="uploadApi"
        :showUploadList="false"
        @change="handleUpload"
        style="margin-right: 10px;display: inline-block;"
        ref="uploadBtn"
      >
        <a-button type="primary">
          <a-icon type="upload" />Upload Output Table Definition (.xlsx)
        </a-button>
      </a-upload>
      <a-button
        type="default"
        v-if="table && table.templateSharedFileId"
        @click="downloadFile"
      >
        <a-icon type="download" />Download Output Table Defnition Template (.xlsx)
      </a-button>
    </div>
    <div
      class="table-form"
      :style="{ 'margin-bottom': '10px' }"
    >
      <a-form-model
        :colon="false"
        :model="table"
      >
        <a-row :gutter="10">
          <a-col :span="12">
            <a-form-model-item
              required
              label="Table Name"
              :label-col="{span: 8}"
              :wrapper-col="{span: 16}"
              style="margin-bottom: 0"
            >
              <a-input
                @change="nameChange(table)"
                v-model="table.tableName"
              />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item
              required
              label="Table Alias"
              :label-col="{span: 8}"
              :wrapper-col="{span: 16}"
              style="margin-bottom: 0"
            >
              <a-input
                @change="change"
                v-model="table.tableAlias"
              />
            </a-form-model-item>
          </a-col>
        </a-row>
        <a-row>
          <a-form-model-item
            required
            label="Table Comment"
            :label-col="{span: 4}"
            :wrapper-col="{span: 20}"
            style="margin-bottom: 0"
          >
            <a-input
              @change="change"
              v-model="table.tableComment"
            />
          </a-form-model-item>
        </a-row>
      </a-form-model>
    </div>
    <a-table
      class="more-small-table"
      size="small"
      :pagination="pagination"
      bordered
      :columns="columns"
      :data-source="table.fields"
      @change="handleTableChange"
    >
      <template
        slot="serial"
        slot-scope="text, record, i"
      >
        {{ i + 1 }}
      </template>
      <template
        slot="table_col_name"
        slot-scope="text, record"
      >
        <a-input
          v-model="record.tableColumnName"
          @change="change"
        />
      </template>
      <template
        slot="table_col_comment"
        slot-scope="text, record"
      >
        <a-input
          v-model="record.tableColumnComment"
          @change="change"
        />
      </template>
      <template
        slot="type"
        slot-scope="text, record"
      >
        <a-select
          style="width: 100%"
          class="type-select"
          @change="typeChange(record)"
          v-model="record.tableColumnType"
          :options="typeOptions"
          :style="{'min-width': '100px'}"
        >
        </a-select>
      </template>
      <template
        slot="typeSize"
        slot-scope="text, record"
      >
        <a-input-number
          style="width: 100%"
          :min="1"
          :max="5000"
          v-if="record.tableColumnType === 'TEXT'"
          v-model="record.typeSize"
          @change="change"
        />
        <a-input
          style="width: 100%"
          v-if="record.tableColumnType === 'DECIMAL'"
          v-model="record.typeSize"
          title="要求填写2个整数，用半角逗号分隔。第1个是 the precision/精度 (maximum number of digits), 第2个是 the scale/小数点后的数位位数 (the number of digits to the right of the decimal point)。"
          @change="change"
        />
      </template>
      <template
        slot="typeFormat"
        slot-scope="text, record"
      >
        <a-input
          style="width: 100%"
          v-if="record.tableColumnType === 'DATE'"
          v-model="record.typeFormat"
          title="半角逗号分隔的支持的date format"
          @change="change"
        />
        <a-input
          style="width: 100%"
          v-if="record.tableColumnType === 'DATETIME'"
          v-model="record.typeFormat"
          title="半角逗号分隔的支持的datetime format"
          @change="change"
        />
        <a-input
          style="width: 100%"
          v-if="record.tableColumnType === 'BOOLEAN'"
          v-model="record.typeFormat"
          title="表示true的值，其它都表示false"
          @change="change"
        />
      </template>
      <template
        slot="query"
        slot-scope="text, record"
      >
        <a-checkbox
          v-model="record.query"
          @change="change"
        ></a-checkbox>
      </template>
      <template
        slot="list"
        slot-scope="text, record"
      >
        <a-checkbox
          v-model="record.list"
          @change="change"
        ></a-checkbox>
      </template>
      <template
        slot="unique"
        slot-scope="text, record"
      >
        <a-checkbox
          v-model="record.unique"
          @change="change"
        ></a-checkbox>
      </template>
    </a-table>
  </div>
</template>

<script>
export default {
  props: {
    importData: {
      type: Object,
      default () {
        return {}
      }
    },
    dtUid: {
      type: String,
      default: ''
    },
    dtrTaskDefUid: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      srcTypeOptions: [
        {
          label: 'Structured Data Source',
          value: 'SDS'
        },
        {
          label: 'Intermediate Data Object',
          value: 'IDO'
        },
        {
          label: 'Data Target',
          value: 'DT'
        }
      ],
      srcOptionsMap: {
        SDS: {
          placeholder: 'Choose Structured Data Source',
          options: []
        },
        IDO: {
          placeholder: 'Choose the Intermediate Data Objects on this chain up to now',
          options: []
        },
        DT: {
          placeholder: 'Choose Data Target',
          options: []
        }
      },
      rules: {
        sqlStatement: [{ requied: true }]
      },
      validateLoading: false,
      columns: [
        {
          title: 'No.',
          key: 'serial',
          dataIndex: 'serial',
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: 'Original Col. Digital No.',
          key: 'originalColumnDigitalNo',
          dataIndex: 'originalColumnDigitalNo',
          width: 90
        },
        {
          title: 'Original Col. Alphabetical No.',
          key: 'originalColumnAlphabeticalNo',
          dataIndex: 'originalColumnAlphabeticalNo',
          width: 100
        },
        {
          title: 'Original Col. Name',
          key: 'originalColumnName',
          dataIndex: 'originalColumnName'
        },
        {
          title: 'Table Col. Name',
          key: 'table_col_name',
          dataIndex: 'table_col_name',
          scopedSlots: { customRender: 'table_col_name' },
          width: 200
        },
        {
          title: 'Table Col. Comment',
          key: 'table_col_comment',
          dataIndex: 'table_col_comment',
          scopedSlots: { customRender: 'table_col_comment' }
        },
        {
          title: 'Type Name',
          key: 'type',
          dataIndex: 'type',
          scopedSlots: { customRender: 'type' },
          width: 100
        },
        {
          title: 'Type Size',
          key: 'typeSize',
          dataIndex: 'typeSize',
          scopedSlots: { customRender: 'typeSize' },
          width: 150
        },
        {
          title: 'Type Format',
          key: 'typeFormat',
          dataIndex: 'typeFormat',
          scopedSlots: { customRender: 'typeFormat' },
          width: 150
        },
        {
          title: 'Query',
          key: 'query',
          dataIndex: 'query',
          scopedSlots: { customRender: 'query' }
        },
        {
          title: 'List',
          key: 'list',
          dataIndex: 'list',
          scopedSlots: { customRender: 'list' }
        },
        {
          title: 'Unique',
          key: 'unique',
          dataIndex: 'unique',
          scopedSlots: { customRender: 'unique' }
        }
      ],
      typeOptions: [
        { label: 'Text', value: 'TEXT', default: 45, re: /^\d+$/ },
        { label: 'Int', value: 'INT' },
        { label: 'Long', value: 'LONG' },
        { label: 'Date', value: 'DATE' },
        { label: 'Datetime', value: 'DATETIME' },
        { label: 'Boolean', value: 'BOOLEAN' },
        { label: 'Decimal', value: 'DECIMAL', default: '20,4', re: /^\d,\d$/ }
      ],
      dateOptions: [
        { label: 'yyyy/MM/dd', value: 'teyyyy/MM/dd' },
        { label: 'yyyy-MM-dd', value: 'yyyy-MM-dd' },
        { label: 'yyyyMMdd', value: 'yyyyMMdd' }
      ],
      dateTimeOptions: [
        { label: 'yyyy/MM/dd HH:mm:ss', value: 'teyyyy/MM/dd HH:mm:ss' },
        { label: 'yyyy-MM-dd HH:mm:ss', value: 'yyyy-MM-dd HH:mm:ss' },
        { label: 'yyyyMMdd HH:mm:ss', value: 'yyyyMMdd HH:mm:ss' }
      ],
      boolOptions: [
        { label: '真', value: true },
        { label: '假', value: false }
      ],
      dataProperties: {
        sqlStatement: '',
        sourceDataFields: [],
        inputTables: [],
        tables: [
          {
            fields: []
          }
        ]
      },
      pagination: {
        pageSize: 20,
        current: 1,
        showSizeChanger: true,
        showTotal: total => `${total} items`,
        pageSizeOptions: ['10', '20', '30', '40', '50', '60', '70', '80', '90', '100']
      }
    }
  },
  computed: {
    table: {
      set (value) {
        this.dataProperties.tables[0] = value
      },
      get () {
        if (!this.dataProperties.tables || !this.dataProperties.tables.length) return false
        return this.dataProperties.tables[0]
      }
    },
    sourceDataFields () {
      return this.dataProperties.tables.map(table => table.fields)
    },
    uploadApi () {
      return this.$api.config.upload
    }
  },
  created () {
    console.log('this.importData: ', this.importData)
    this.initData()
    this.loadSrcOptions()
  },
  methods: {
    toOptions (list, label = 'name', value = 'uid') {
      return list.map(x => ({
        label: x[label],
        value: x[value]
      }))
    },
    async loadSrcOptions () {
      this.$api.getDtSrcSdsList(this.dtUid).then((resp) => {
        if (!resp || !resp.object) return
        this.srcOptionsMap['SDS'].options = this.toOptions(resp.object.list_of_avail_src_sds_detail, 'sds_name', 'sds_uid')
      })
      this.$api.getDtSrcDtList(this.dtUid).then((resp) => {
        if (!resp || !resp.object) return
        this.srcOptionsMap['DT'].options = this.toOptions(resp.object.list_of_avail_src_dt_detail)
      })
      this.$api.getSrcIntermediate(this.dtUid, this.dtrTaskDefUid).then((resp) => {
        if (!resp || !resp.object) return
        this.srcOptionsMap['IDO'].options = this.toOptions(resp.object.list_of_avail_src_intermediate_detail, 'table_name', 'uid')
      })
    },
    addRow () {
      this.dataProperties.inputTables.push({
        dt_src_type: 'SDS',
        src_uid: undefined
      })
    },
    removeRow (index) {
      this.dataProperties.inputTables.splice(index)
    },
    handleTableChange (pagination) {
      this.pagination = pagination
    },
    handleTableKey (tables) {
      tables &&
        tables.forEach(item => {
          item.key = item.key || Math.random().toString()
        })
    },
    initData () {
      if (this.importData && Object.keys(this.importData).length) {
        Object.assign(this.dataProperties, this.importData)
      }
      this.handleTableKey(this.dataProperties.tables)
    },
    validate () {
      if (!this.dataProperties.tables.length) {
        this.$message.warning('Operation failed. More info: pleace upload MS Excel Template')
        return false
      }
      for (let index = 0; index < this.dataProperties.tables.length; index++) {
        const element = this.dataProperties.tables[index]
        if (!/^[0-9a-zA-Z_]{1,}$/.test(element.tableName)) {
          this.$message.warning(
            'Operation failed. More info: Table name must be a combination of letters,underline or numbers'
          )
          return false
        }
        if (!element.tableAlias) {
          this.$message.warning('Operation failed. More info: Table alias is required')
          return false
        }
        if (!element.tableComment) {
          this.$message.warning('Operation failed. More info: Table comment is required')
          return false
        }
      }
    },
    setDefaultValues (tables) {
      tables.forEach(table => table.fields.forEach(field => this.setDefaultValue(field)))
    },
    setDefaultValue (item) {
      const columnMap = {
        TEXT: 'typeSize',
        DECIMAL: 'typeSize',
        DATETIME: 'typeFormat',
        BOOLEAN: 'typeFormat',
        DATE: 'typeFormat'
      }
      const attrColumnKey = columnMap[item.tableColumnType]
      if (!attrColumnKey) return
      this.typeOptions
        .filter(option => option.default)
        .forEach(option => {
          if (item.tableColumnType === option.value) {
            if (option.re && !option.re.test(item[attrColumnKey])) {
              item[attrColumnKey] = option.default
            } else {
              item[attrColumnKey] = item[attrColumnKey] || option.default
            }
          }
        })
    },
    typeChange (item) {
      item.typeSize = null
      item.typeFormat = null
      this.setDefaultValue(item)
      this.change()
    },
    async validateSql () {
      this.$refs.sql.validate(async valid => {
        if (valid) {
          this.validateLoading = true
          await this.$api.validateTransformSql(this.dataProperties.sqlStatement).finally(() => (this.validateLoading = false))
        }
      })
    },
    nameChange (table) {
      if (!/^di_otr_.*/.test(table.tableName)) {
        table.tableName = 'di_otr_'
      }
      this.change()
    },
    change () {
      this.dataProperties.sourceDataFields = this.sourceDataFields
      this.$emit('changeData', this.dataProperties)
    },
    handleUpload ({ file }) {
      if (!file.response) {
        return
      }
      if (file.response.success) {
        this.$message.success('Upload Operation succeeded')
        this.parseExcel(file.response.object)
      } else {
        this.$message.success(`Upload Operation failed.`)
      }
    },
    toHump (name) {
      return name.replace(/_(\w)/g, (all, letter) => {
        return letter.toUpperCase()
      })
    },
    isObj (i) {
      return Object.prototype.toString.call(i) === '[object Object]'
    },
    humpObj (obj) {
      if (!this.isObj(obj)) {
        return obj
      }
      const newObj = {}
      Object.keys(obj).forEach(key => {
        const item = obj[key]

        if (Array.isArray(item)) {
          newObj[this.toHump(key)] = item.map(i => this.humpObj(i))
        } else if (this.isObj(item)) {
          newObj[this.toHump(key)] = this.humpObj(item)
        } else {
          newObj[this.toHump(key)] = item
        }
      })
      return newObj
    },
    async parseExcel (sharedFileId) {
      const params = {
        shared_file_id: sharedFileId,
        header_row_num: 0,
        multiple_sheets_option: 'active',
        sheet_name: 'Sheet1'
      }
      const resp = await this.$api.parseDsFromExcelApi(params)
      if (resp.success && resp.object) {
        if (Array.isArray(resp.object)) {
          this.dataProperties.tables = resp.object.map(table => this.humpObj(table))
        } else {
          this.dataProperties.tables = [this.humpObj(resp.object)]
        }
        this.dataProperties.tables.forEach(x => {
          x.unique = false
        })
        this.handleTableKey(this.dataProperties.tables)
        this.dataProperties.sheetIndexes = this.dataProperties.tables.map(table => table.sheetIndex)
        this.setDefaultValues(this.dataProperties.tables)
        this.change()
      }
    },
    downloadFile () {
      window.location.href = `${this.$api.config.download}?file_id=${this.table.templateSharedFileId}`
    }
  }
}
</script>
