<template>
  <div>
    <h3 class="title ant-form-item-required">Input Table(s)</h3>
    <a-radio-group style="width: 50%" v-model="dataProperties.srcUid" @change="srcChange">
      <a-row class="row-box mg-b-5" v-for="(row, index) in srcList" :key="index" :gutter="10">
        <a-col :span="1">
          <a-radio style="line-height: 32px" :value="row.src_uid"></a-radio>
        </a-col>
        <a-col :span="7">
          <a-select style="width: 100%" :options="typeOptions" v-model="row.dt_src_type" disabled></a-select>
        </a-col>
        <a-col v-if="row.dt_src_type === 'SDS'" :span="7">
          <a-select style="width: 100%" v-model="row.ds_uid" :options="[{ label: row.ds_name, value: row.ds_uid }]" disabled></a-select>
        </a-col>
        <a-col :span="7" v-if="row.dt_src_type === 'DT'"></a-col>
        <a-col :span="7">
          <a-select style="width: 100%" v-model="row.src_uid" :options="[{ label: row.src_name, value: row.src_uid }]" disabled></a-select>
        </a-col>
      </a-row>
    </a-radio-group>
    <h3
      class="title mg-t-20"
    >Processing Logic</h3>
    <p>Direct Copy from one table to the other table.</p>
    <h3
      class="title ant-form-item-required mg-t-20"
    >Output Table(s)</h3>
    <p
      class="tip-text"
    >
      Copy the structure of the load output table, except that the table name needs to be changed.
    </p>
    <template v-if="table">
      <div
        class="table-form mg-b-10"
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
        :pagination="false"
        bordered
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
          slot="query"
          slot-scope="text"
        >
          {{ text | boolean }}
        </template>
        <template
          slot="list"
          slot-scope="text"
        >
          {{ text | boolean }}
        </template>
        <template
          slot="unique"
          slot-scope="text"
        >
          {{ text | boolean }}
        </template>
      </a-table>
    </template>
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
    }
  },
  data () {
    return {
      srcList: [],
      tableMap: {},
      columns: [
        {
          title: 'No.',
          dataIndex: 'serial',
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: 'Table Col. Name',
          dataIndex: 'tableColumnName',
          width: 200
        },
        {
          title: 'Table Col. Comment',
          dataIndex: 'tableColumnComment'
        },
        {
          title: 'Type Name',
          dataIndex: 'tableColumnType',
          width: 100
        },
        {
          title: 'Type Size',
          dataIndex: 'typeSize',
          width: 150
        },
        {
          title: 'Type Format',
          dataIndex: 'typeFormat',
          width: 150
        },
        {
          title: 'Query',
          dataIndex: 'query',
          scopedSlots: { customRender: 'query' }
        },
        {
          title: 'List',
          dataIndex: 'list',
          scopedSlots: { customRender: 'list' }
        },
        {
          title: 'Unique',
          dataIndex: 'unique',
          scopedSlots: { customRender: 'unique' }
        }
      ],
      typeOptions: [
        {
          label: 'Data Source',
          value: 'SDS'
        },
        {
          label: 'Data Target',
          value: 'DT'
        }
      ],
      dataProperties: {
        srcUid: '',
        srcName: '',
        srcType: '',
        tables: []
      }
    }
  },
  filter: {
    boolean (value) {
      if (value === true) return 'true'
      if (value === false) return 'false'
      return ''
    }
  },
  computed: {
    table: {
      set (value) {
        this.dataProperties.tables[0] = value
      },
      get () {
        if (!this.dataProperties.tables || !this.dataProperties.tables.length) return
        return this.dataProperties.tables[0]
      }
    }
  },
  created () {
    console.log('this.importData: ', this.importData)
    this.initData()
    this.loadSrcList()
  },
  methods: {
    async loadSrcList () {
      const resp = await this.$api.getDataTargetDetail(this.dtUid)
      if (!resp || !resp.object) return
      this.srcList = resp.object.list_of_src || []
      if (!this.dataProperties.srcUid && this.srcList.length) {
        const firstSrc = this.srcList[0]
        this.dataProperties.srcUid = firstSrc.src_uid
        this.dataProperties.srcType = firstSrc.dt_src_type
        this.dataProperties.srcName = firstSrc.src_name
      }
      this.srcChange()
    },
    async loadOutputTable () {
      if (!this.dataProperties.srcUid) return
      const resp = await this.$api.getDtSrcDataFields(this.dataProperties.srcType, this.dataProperties.srcUid)
      if (!resp || !resp.object) return
      const cacheTable = this.tableMap[this.dataProperties.srcUid]
      const table = {
        tableName: cacheTable ? cacheTable.tableName : '',
        tableAlias: cacheTable ? cacheTable.tableAlias : '',
        tableComment: cacheTable ? cacheTable.tableComment : '',
        fields: resp.object.map(field => this.humpObj(field))
      }
      this.tableMap[this.dataProperties.srcUid] = table
      this.dataProperties.tables = [table]
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
    initData () {
      if (this.importData && Object.keys(this.importData).length) {
        Object.assign(this.dataProperties, this.importData)
        if (this.dataProperties.srcUid && this.dataProperties.tables && this.dataProperties.tables.length) {
          this.tableMap[this.dataProperties.srcUid] = this.dataProperties.tables[0]
        }
      }
    },
    nameChange (table) {
      if (!/^di_otr_.*/.test(table.tableName)) {
        table.tableName = `di_otr_${table.tableName}`
      }
      this.change()
    },
    async srcChange () {
      const activeSrc = this.srcList.find(x => x.src_uid === this.dataProperties.srcUid)
      this.dataProperties.srcName = activeSrc.src_name
      this.dataProperties.srcType = activeSrc.dt_src_type
      await this.loadOutputTable()
      this.change()
    },
    change () {
      this.$emit('changeData', this.dataProperties)
    },
    validate () {
      if (!this.dataProperties.tables.length) {
        this.$message.warning('Operation failed. More info: no output table')
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
    }
  }
}
</script>
