<template>
  <div>
    <h3 class="title">Output Table(s)</h3>
    <p
      v-if="type === 'template'"
      class="tip-text"
    >
      Help system determine data field format by importing an MS Excel template.
    </p>
    <a-form-model
      ref="csvForm"
      :colon="false"
      labelAlign="left"
      :model="dataProperties"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 10 }"
    >
      <a-form-model-item
        required
        label="Header Row Num(Start from 0)"
        prop="headerRowNum"
        ref="headerRowNum"
        :autoLink="false"
      >
        <a-input-number
          style="width: 100%"
          @change="() => {$refs.headerRowNum.onFieldChange();change()}"
          :min="0"
          v-model="dataProperties.headerRowNum"
        />
      </a-form-model-item>
      <a-form-model-item
        ref="multipleSheetsOption"
        label="Multiple Sheets Support"
        prop="multipleSheetsOption"
        :autoLink="false"
      >
        <a-radio-group
          @change="() => {$refs.multipleSheetsOption.onFieldChange();change()}"
          v-model="dataProperties.multipleSheetsOption"
        >
          <a-radio value="active">
            Only read active sheet
          </a-radio>
          <a-radio value="all">
            Read all sheets
          </a-radio>
          <a-radio value="one">
            Specific sheet name
          </a-radio>
        </a-radio-group>
        <a-input
          v-if="dataProperties.multipleSheetsOption === 'one'"
          @change="change"
          v-model="dataProperties.sheetName"
        />
      </a-form-model-item>
      <a-form-model-item label=" ">
        <a-upload
          :multiple="false"
          :action="uploadApi"
          :before-upload="beforeUpload"
          :fileList="fileList"
          @change="handleUpload"
          ref="uploadBtn"
        >
          <a-button type="primary">
            <a-icon type="upload" />Upload MS Excel (.xlsx) Template
          </a-button>
        </a-upload>
      </a-form-model-item>
    </a-form-model>
    <p class="tip-text">
      The system infers the type of each field based on the template data, which requires your confirmation and correction,
      especially the text, date, datetime, decimal, and boolean types.
    </p>
    <a-tabs
      type="card"
      v-if="dataProperties.tables && dataProperties.tables.length"
    >
      <a-tab-pane
        v-for="table in dataProperties.tables"
        type="card"
        :key="table.key"
        :tab="table.tableName"
      >
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
      </a-tab-pane>
    </a-tabs>
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
    type: {
      type: String,
      default: 'template'
    }
  },
  data () {
    return {
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
        headerRowNum: 0,
        multipleSheetsOption: 'active',
        sourceDataFields: [],
        tables: []
      },
      fileList: [],
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
  },
  methods: {
    handleTableChange (pagination) {
      this.pagination = pagination
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
    change () {
      this.dataProperties.sourceDataFields = this.sourceDataFields
      this.$emit('changeData', this.dataProperties)
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
      // if (
      //   !this.dataProperties.tables ||
      //   !this.dataProperties.tables.length ||
      //   !this.sourceDataFields ||
      //   !this.sourceDataFields.length
      // ) {
      //   return
      // }
      // this.dataProperties.tables.forEach(table => {
      //   table.fields &&
      //     table.fields.forEach((item, index) => {
      //       Object.assign(item, this.sourceDataFields[index])
      //     })
      // })
    },
    handleUpload ({ file, fileList }) {
      this.fileList = fileList.slice(-1)
      if (!file.response) {
        return
      }
      if (file.response.success) {
        this.$message.success('Upload Operation succeeded')
        this.$emit('fileId', file.response.object)
        this.parseExcel(file.response.object)
      } else {
        this.$message.error('Upload Operation failed')
        file.response.err_message && this.$message.error(file.response.err_message)
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
        multiple_sheets_option: this.dataProperties.multipleSheetsOption,
        sheet_name: this.dataProperties.sheetName || 'no_name',
        header_row_num: this.dataProperties.headerRowNum
      }
      const resp = await this.$api.parseDsFromExcelApi(params)
      if (resp.success && resp.object) {
        if (Array.isArray(resp.object)) {
          this.dataProperties.tables = resp.object.map(table => this.humpObj(table))
          // this.dataProperties.sourceDataFields = resp.object
          //   .map(table => this.humpObj(table))
          //   .map(table => table.fields)
        } else {
          this.dataProperties.tables = [this.humpObj(resp.object)]
          // this.dataProperties.sourceDataFields = [this.humpObj(resp.object).fields]
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
    nameChange (table) {
      if (!/^di_olo_.*/.test(table.tableName)) {
        table.tableName = 'di_olo_'
      }
      this.change()
    },
    beforeUpload () {
      let isValid = true
      this.$refs.csvForm.validate(valid => {
        isValid = valid
      })
      return isValid
    }
  }
}
</script>
