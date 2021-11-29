<template>
  <div>
    <h3 class="title ant-form-item-required">SQL statement</h3>
    <a-form-model
      :model="dataProperties"
      :rules="rules"
      ref="sql"
    >
      <a-form-model-item
        prop="sqlStatement"
        style="margin-bottom:0"
      >
        <a-textarea
          rows="5"
          v-model="dataProperties.sqlStatement"
          @change="change"
        ></a-textarea>
      </a-form-model-item>
      <a-form-model-item label="">
        <a-button
          :loading="validateLoading"
          @click="validateSql"
        >Validate</a-button>
      </a-form-model-item>
    </a-form-model>

    <h3
      class="title"
      style="margin-top: 10px"
    >Definition of Output Table</h3>
    <div
      class="btn-group"
      style="margin-bottom: 10px"
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
          <a-icon type="upload" />Upload Output Table Defnition (.xlsx)</a-button>
      </a-upload>
      <a-button
        type="default"
        v-if="table && table.templateSharedFileId"
        @click="downloadFile"
      >
        <a-icon type="download" />Download Table Defnition Template (.xlsx)</a-button>
    </div>
    <div
      v-if="table"
      class="table-form"
      :style="{ 'margin-bottom': '10px' }"
    >
      <a-form-model
        :colon="false"
        :model="table"
        style="display: flex"
      >
        <a-form-model-item
          label="Table Name"
          :label-col="{span: 6}"
          :wrapper-col="{span: 18}"
          required
          style="width: 400px;margin-bottom: 0"
        >
          <a-input
            @change="nameChange(table)"
            disabled
            required
            v-model="table.tableName"
            placeholder="the output table mentioned in the SQL statement"
          />
        </a-form-model-item>
        <a-form-model-item
          label="Table Comment"
          style="flex: auto;margin-bottom: 0"
          :label-col="{span: 4}"
          :wrapper-col="{span: 20}"
        >
          <a-input
            @change="change"
            disabled
            v-model="table.tableComment"
          />
        </a-form-model-item>
      </a-form-model>
    </div>
    <a-table
      class="more-small-table"
      size="small"
      :pagination="false"
      :bordered="true"
      :columns="columns"
      :data-source="table.fields"
    >
      <template
        slot="serial"
        slot-scope="text, record, i"
      >
        {{ i + 1 }}
      </template>
      <template
        slot="not_null"
        slot-scope="text"
      >
        {{ text ? 'Yes' : 'No' }}
      </template>
      <template
        slot="unique"
        slot-scope="text"
      >
        {{ text ? 'Yes' : 'No' }}
      </template>
      <template
        slot="unsigned"
        slot-scope="text"
      >
        {{ text ? 'Yes' : 'No' }}
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
    }
  },
  data () {
    return {
      uploadApi: '/fs/storage/upload',
      parseApi: '/kapok-dis/master/templates/parse_table_definition_from_excel',
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
          title: 'Table Col. Name',
          key: 'tableColumnName',
          dataIndex: 'tableColumnName'
        },
        {
          title: 'Table Col. Comment',
          key: 'tableColumnComment',
          dataIndex: 'tableColumnComment'
        },
        {
          title: 'Type',
          key: 'tableColumnType',
          dataIndex: 'tableColumnType'
        },
        {
          title: 'Type Attributes',
          key: 'tableColumnTypeAttributes',
          dataIndex: 'tableColumnTypeAttributes'
        },
        {
          title: 'Not NULL',
          key: 'not_null',
          dataIndex: 'not_null',
          scopedSlots: { customRender: 'not_null' }
        },
        {
          title: 'Unique',
          key: 'unique',
          dataIndex: 'unique',
          scopedSlots: { customRender: 'unique' }
        },
        {
          title: 'Unsigned',
          key: 'unsigned',
          dataIndex: 'unsigned',
          scopedSlots: { customRender: 'unsigned' }
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
        }
      ],
      dataProperties: {
        sqlStatement: '',
        tables: [
          {
            tableName: '',
            tableComment: '',
            fields: []
          }
        ]
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
    }
  },
  created () {
    console.log('this.importData: ', this.importData)
    if (this.importData && Object.keys(this.importData).length) {
      Object.assign(this.dataProperties, this.importData)
    }
  },
  methods: {
    validate () {
      if (!this.dataProperties.sqlStatement) {
        this.$message.warning('Operation failed. More info: sqlStatement is required')
        return false
      }
      if (!this.dataProperties.tables[0].tableName) {
        this.$message.warning('Operation failed. More info: pleace upload output table definition')
        return false
      }
    },
    async validateSql () {
      this.$refs.sql.validate(async valid => {
        if (valid) {
          this.validateLoading = true
          await this.$http({
            url: '/kapok-dis/master/plugin/button/validate-sql',
            method: 'post',
            data: { sql: this.dataProperties.sqlStatement }
          }).catch(() => (this.validateLoading = false))
          this.validateLoading = false
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
        shared_file_id: sharedFileId
      }
      const resp = await this.$http({ url: this.parseApi, method: 'post', params })
      if (resp.success) {
        if (Array.isArray(resp.object)) {
          this.dataProperties.tables = resp.object.map(table => this.humpObj(table))
        } else {
          this.dataProperties.tables = [this.humpObj(resp.object)]
        }
        console.log('this.dataProperties: ', this.dataProperties)
        this.change()
      }
    },
    downloadFile () {
      window.location.href = `/fs/storage/download?file_id=${this.table.templateSharedFileId}`
    }
  }
}
</script>
