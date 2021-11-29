<template>
  <div>
    <h3 class="title">Output Table(s)</h3>
    <p
      class="tip-text"
      v-if="type === 'template'"
    >
      Help system determine data field format by importing a CSV template.
    </p>
    <a-form-model
      :colon="false"
      labelAlign="left"
      ref="csvForm"
      :rules="rules"
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
        required
        label="Delimiter"
        prop="delimiter"
        ref="delimiter"
        :autoLink="false"
      >
        <a-select
          style="width: 100%"
          @change="() => {$refs.delimiter.onFieldChange();change()}"
          v-model="dataProperties.delimiter"
          :options="delimiterOptions"
        ></a-select>
      </a-form-model-item>
      <a-form-model-item
        required
        label="Text Encoding"
        prop="textEncoding"
        ref="textEncoding"
        :autoLink="false"
      >
        <a-select
          style="width: 100%"
          @change="() => {$refs.textEncoding.onFieldChange();change()}"
          v-model="dataProperties.textEncoding"
          :options="textEncodingOptions"
        ></a-select>
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
            <a-icon type="upload" />Upload CSV Template
          </a-button>
        </a-upload>
      </a-form-model-item>
    </a-form-model>
    <p class="tip-text">
      The system infers the type of each field based on the template data, which requires your confirmation and correction,
      especially the text, date, datetime, decimal, and boolean types.
    </p>
    <template v-if="table">
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
        :bordered="true"
        :columns="columns"
        :data-source="table.fields"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template
          slot="serial"
          slot-scope="text, record, index"
        >
          {{ index + 1 }}
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
    </template>
  </div>
</template>

<script>
/* eslint-disable */

export default {
  name: 'ExtractCSVPropertiestemplate',
  props: {
    importData: {
      type: Object,
      default() {
        return {}
      }
    },
    type: {
      type: String,
      default: 'template'
    }
  },
  data() {
    return {
      rules: {
        headerRowNum: [{ required: true, message: 'Please input headerRowNum', trigger: 'blur' }],
        delimiter: [{ required: true, message: 'Please select Delimiter', trigger: 'change' }],
        textEncoding: [{ required: true, message: 'Please select Text Encoding', trigger: 'change' }]
      },
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
          width: 110
        },
        {
          title: 'Original Col. Name',
          key: 'originalColumnName',
          dataIndex: 'originalColumnName',
          width: 150
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
          width: 50
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
        { label: 'yyyy/MM/dd', value: 'yyyy/MM/dd' },
        { label: 'yyyy-MM-dd', value: 'yyyy-MM-dd' },
        { label: 'yyyyMMdd', value: 'yyyyMMdd' }
      ],
      dateTimeOptions: [
        { label: 'yyyy/MM/dd HH:mm:ss', value: 'yyyy/MM/dd HH:mm:ss' },
        { label: 'yyyy-MM-dd HH:mm:ss', value: 'yyyy-MM-dd HH:mm:ss' },
        { label: 'yyyyMMdd HH:mm:ss', value: 'yyyyMMdd HH:mm:ss' }
      ],
      boolOptions: [
        { label: 'Yes', value: true },
        { label: 'No', value: false }
      ],
      delimiterOptions: [
        { label: 'Comma', value: ',' },
        { label: 'Semicolon', value: ';' },
        { label: 'Tab', value: '  ' },
        { label: 'Space', value: ' ' }
      ],
      textEncodingOptions: [
        { label: 'Unicode(UTF-8)', value: 'UTF-8' },
        { label: 'Simplified Chinese(GB 2312)', value: 'GB2312' }
      ],
      dataProperties: {
        headerRowNum: 0,
        delimiter: ',',
        textEncoding: 'UTF-8',
        tables: [],
        sourceDataFields: []
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
    table: {
      set(value) {
        this.dataProperties.tables[0] = value
      },
      get() {
        if (!this.dataProperties.tables || !this.dataProperties.tables.length) return false
        return this.dataProperties.tables[0]
      }
    },
    sourceDataFields() {
      return this.table.fields
    },
    uploadApi() {
      return this.$api.config.upload
    }
  },
  created() {
    console.log('this.importData: ', this.importData)
    this.initData()
  },
  methods: {
    handleTableChange(pagination) {
      this.pagination = pagination
    },
    validate() {
      if (!this.dataProperties.tables.length) {
        this.$message.warning('Operation failed. More info: pleace upload CSV Template')
        return false
      }
      if (!/^[0-9a-zA-Z_]{1,}$/.test(this.dataProperties.tables[0].tableName)) {
        this.$message.warning(
          'Operation failed. More info: Table name must be a combination of letters,underline or numbers'
        )
        return false
      }
    },
    setDefaultValues(tables) {
      tables.forEach(table => table.fields.forEach(field => this.setDefaultValue(field)))
    },
    setDefaultValue(item) {
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
    typeChange(item) {
      item.typeSize = null
      item.typeFormat = null
      this.setDefaultValue(item)
      this.change()
    },
    nameChange(table) {
      if (!/^di_olo_.*/.test(table.tableName)) {
        table.tableName = 'di_olo_'
      }
      this.change()
    },
    change() {
      this.dataProperties.sourceDataFields = this.sourceDataFields
      this.$emit('changeData', this.dataProperties)
    },
    initData() {
      if (this.importData && Object.keys(this.importData).length) {
        Object.assign(this.dataProperties, this.importData)
      }
      // console.log('this.dataProperties', this.dataProperties)
      // if (
      //   !this.dataProperties.tables ||
      //   !this.dataProperties.tables.length ||
      //   !this.dataProperties.sourceFields ||
      //   !this.dataProperties.sourceFields.length
      // ) {
      //   return
      // }
      // this.dataProperties.tables[0].fields &&
      //   this.dataProperties.tables[0].fields.forEach((item, index) => {
      //     Object.assign(item, this.dataProperties.sourceFields[index])
      //   })
    },
    handleUpload({ file, fileList }) {
      this.fileList = fileList.slice(-1)
      if (!file.response) {
        return
      }
      if (file.response.success) {
        this.$message.success('Upload Operation succeeded')
        this.$emit('fileId', file.response.object)
        this.parseCsv(file.response.object)
      } else {
        this.$message.error('Upload Operation failed')
        file.response.err_message && this.$message.error(file.response.err_message)
      }
    },
    toHump(name) {
      return name.replace(/_(\w)/g, (all, letter) => {
        return letter.toUpperCase()
      })
    },
    isObj(i) {
      return Object.prototype.toString.call(i) === '[object Object]'
    },
    humpObj(obj) {
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
    async parseCsv(sharedFileId) {
      const params = {
        shared_file_id: sharedFileId,
        delimiter: this.dataProperties.delimiter,
        text_encoding: this.dataProperties.textEncoding,
        header_row_num: this.dataProperties.headerRowNum
      }
      const resp = await this.$api.parseDsFromCsvApi(params)
      if (resp.success) {
        const table = this.humpObj(resp.object)
        this.dataProperties.tables = [table]
        this.setDefaultValues(this.dataProperties.tables)
        // this.dataProperties.sourceFields = table.fields
        this.change()
      }
    },
    beforeUpload() {
      let isValid = true
      this.$refs.csvForm.validate(valid => {
        isValid = valid
        if (valid) {
          this.$refs.uploadBtn.click()
        }
      })
      return isValid
    }
  }
}
</script>
