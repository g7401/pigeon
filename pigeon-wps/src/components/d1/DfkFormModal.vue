<template>
  <div class="data-source-form">
    <a-modal
      :title="title"
      :ok-text="config.okText"
      :cancel-text="config.cancelText"
      :visible="value"
      :confirm-loading="config.confirmLoading"
      :width="config.width"
      @ok="handleOk"
      @cancel="close"
    >
      <template>
        <slot name="header"></slot>
        <a-icon
          type="loading"
          v-if="uid && infoLoading"
        ></a-icon>
        <template v-else>
          <a-form-model
            ref="form1"
            :rules="form1Rules"
            :model="formData"
            :labelCol="labelCol"
            :wrapperCol="wrapperCol"
            :colon="false"
          >
            <a-form-model-item
              label="Data Facet Key"
              prop="key"
              key="key"
              required
            >
              <a-input
                v-model="formData.key"
                placeholder="letters,underline or numbers,length < 50"
              />
            </a-form-model-item>
            <a-form-model-item
              label="Data Facet Name"
              prop="name"
              key="name"
              required
            >
              <a-input v-model="formData.name" />
            </a-form-model-item>

            <a-form-model-item
              label="Data Facet Description"
              prop="description"
            >
              <WangEditor v-model="formData.description">
              </WangEditor>
              <!-- <a-textarea
              v-model="formData.description"
              size="small"
            ></a-textarea> -->
            </a-form-model-item>
            <a-form-model-item
              label="Tag"
              prop="tags"
            >
              <a-select
                style="width:100%"
                mode="multiple"
                v-model="formData.tags"
                :options="tagOptions"
                allowClear
              ></a-select>
            </a-form-model-item>
          </a-form-model>
          <a-checkbox
            class="mg-b-10"
            v-model="formData.processing_needed"
            @change="handleCombineChange"
          >Combine with other table(s)/view(s) or transform w/ group by functionality</a-checkbox>
          <div v-if="formData.processing_needed">
            <a-row :gutter="10">
              <a-col :span="10">
                <SearchTree
                  v-if="chainData.server_id"
                  mode="checkbox"
                  searchAction="change"
                  checkable
                  :tree-data="dataSources"
                  :checkedKeys="checkedKeys"
                  :replaceFields="replaceFields"
                  height="565px"
                  @check="handleSelect"
                ></SearchTree>
              </a-col>
              <a-col :span="14">
                <a-form-model
                  class="sql-form"
                  ref="form2"
                  :model="formData"
                  :rules="form2Rules"
                  :colon="false"
                >
                  <a-form-model-item
                    label="Processing Logic (SQL)"
                    required
                    prop="processing_logic"
                    style="margin-bottom:0"
                  >
                    <MonacoEditor
                      class="editor mg-b-10"
                      v-model="formData.processing_logic"
                      @change="handleSqlChange"
                      language="sql"
                      theme="vs-dark"
                      :options="{ automaticLayout: true }"
                    />
                  </a-form-model-item>
                  <a-form-model-item label="">
                    <a-button
                      :loading="testLoading"
                      :disabled="!formData.processing_logic"
                      @click="testExpression"
                    >Test</a-button>
                  </a-form-model-item>
                </a-form-model>
                <template v-if="loadedTable">
                  <div v-if="tableData.length">Below is the test result.</div>
                  <div v-else>No result.</div>
                </template>
              </a-col>
            </a-row>
            <template v-if="loadedTable">
              <vxe-grid
                class="more-small-table mini-scrollbar mg-t-10"
                show-overflow="title"
                show-header-overflow="title"
                :data="tableData"
                :columns="columns"
                :scrollX="{gt: 8}"
                size="mini"
              ></vxe-grid>
            </template>
          </div>
        </template>
      </template>
    </a-modal>
  </div>
</template>

<script>
import '@/core/vxe-table'
import cloneDeep from 'lodash.clonedeep'
import WangEditor from '@/components/Editor/WangEditor'
import SearchTree from '@/components/common/SearchTree'
import { mapGetters } from 'vuex'
export default {
  components: { WangEditor, MonacoEditor: () => import('vue-monaco'), SearchTree },
  props: {
    uid: {
      type: [String, Number],
      default: null
    },
    value: {
      type: Boolean,
      default: false
    },
    addData: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      config: {
        okText: 'Save',
        cancelText: 'Cancel',
        width: 1000,
        confirmLoading: false
      },
      labelCol: { span: 6 },
      wrapperCol: { span: 18 },
      formData: {
        key: '',
        name: '',
        description: '',
        processing_logic: '',
        processing_needed: false,
        secondary_data_object_uids: [],
        primary_data_object_uid: null,
        tags: []
      },
      tagOptions: [],
      // isCombine: false,
      replaceFields: { children: 'children', key: 'key', title: 'label' },
      testLoading: false,
      loadedTable: false,
      columns: [],
      form1Rules: {
        key: [{ requied: true }],
        name: [{ requied: true }]
      },
      form2Rules: {
        processing_logic: [{ requied: true }]
      },
      joinStr: '___',
      checkedKeys: [],
      hasSchema: false,
      chainData: {
        server_id: undefined,
        database_name: undefined,
        schema_name: undefined,
        table_name: undefined,
        table_id: undefined
      },
      infoLoading: false,
      tableTypes: ['table', 'view'],
      tableKeyName: 'table'
    }
  },
  computed: {
    title () {
      const base = 'Data Facet'
      const prefix = this.uid ? 'Edit ' : 'Create '
      return prefix + base
    },
    ...mapGetters(['d1DsTree']),
    dataSources () {
      return this.generateTreeData(cloneDeep(this.d1DsTree.filter(x => x.id === this.chainData.server_id)))
    },
    currentTableKey () {
      if (!this.chainData) return null
      return this.generateKey(this.chainData.table_id)
    }
  },
  created () {
    this.getDataFacetTags()
    this.chainData = Object.assign(this.chainData, this.addData)
    if (this.addData.schema_name) {
      this.hasSchema = true
    }
    if (this.uid) {
      this.loadDfkInfo(this.uid)
    } else {
      this.formData.key = `${this.addData.table_name}_dfk`
    }
  },
  methods: {
    async testExpression () {
      this.testLoading = true
      const result = await this.$api
        .testProcessLogicSql(this.chainData.server_id, this.formData.processing_logic)
        .finally(() => {
          this.testLoading = false
        })
      if (!result) return
      this.tableData = result.rows
      const columns = result.column_names.map(key => ({
        title: key,
        field: key,
        minWidth: 150
      }))
      this.columns = [
        {
          title: 'No.',
          type: 'seq'
        },
        ...columns
      ]
      this.loadedTable = true
    },
    handleSelect (checkedKeys, { checkedNodes }) {
      this.checkedKeys = checkedKeys
    },
    generateTreeData (items, parent) {
      const data = items
      data.forEach(item => {
        item.checkable = false
        if (this.tableTypes.includes(item.type)) {
          item.key = this.generateKey(item.id)
          item.checkable = true
          item.disableCheckbox = this.generateKey(item.id) === this.currentTableKey
          delete item.children
        } else {
          item.key = `${parent ? parent.key : ''}${item.type}${item.label}`
          if (item.children) {
            this.generateTreeData(item.children, item)
          }
        }
      })
      return data
    },
    handleCombineChange () {
      if (!this.formData.processing_needed) {
        this.formData.secondary_data_object_uids = undefined
        this.formData.processing_logic = undefined
      } else if (!this.checkedKeys.length) {
        this.checkedKeys = [this.currentTableKey]
      }
    },
    async getDataFacetTags () {
      const resp = await this.$api.getDataFacetTags()
      this.tagOptions = resp.map(x => ({
        label: x,
        value: x
      }))
    },
    async loadDfkInfo (uid) {
      this.infoLoading = true
      const resp = await this.$api.getDfk(uid).finally(() => {
        this.infoLoading = false
      })
      if (!resp) return
      if (resp.schema_name) {
        this.hasSchema = true
      }
      Object.keys(this.formData).forEach(key => {
        if (resp[key] !== null) {
          this.formData[key] = resp[key]
        }
      })
      if (this.formData.processing_logic) {
        this.formData.processing_needed = true
      }
      if (this.formData.secondary_data_object_uids) {
        this.checkedKeys = [this.generateKey(this.formData.primary_data_object_uid), ...this.formData.secondary_data_object_uids.map(x => this.generateKey(x))]
      }
    },
    keyToId (key) {
      return parseInt(key.replace(this.tableKeyName, ''))
    },
    generateKey (id) {
      return `${this.tableKeyName}${id}`
    },
    save () {
      const data = {
        ...this.formData
      }
      data['primary_data_object_uid'] = this.addData.table_id
      if (this.uid) {
        data['uid'] = this.uid
        return this.$api.updateDfk(data)
      } else {
        return this.$api.addDfk(data)
      }
    },
    handleSqlChange () {
      this.loadedTable = false
    },
    async handleOk () {
      let isForm1Valid = true
      let isForm2Valid = true
      this.$refs.form1 &&
        this.$refs.form1.validate(valid => {
          isForm1Valid = valid
        })
      this.$refs.form2 &&
        this.$refs.form2.validate(valid => {
          isForm2Valid = valid
        })
      if (!isForm1Valid || !isForm2Valid) return
      if (this.formData.processing_needed && !this.loadedTable) {
        await this.testExpression()
      }
      if (this.formData.processing_needed && this.formData.processing_logic && !this.loadedTable) {
        return this.$message.warning('SQL 必须测试通过后才能保存')
      }
      if (!this.formData.key || !/^[0-9a-zA-Z_]{1,}$/.test(this.formData.key)) {
        this.$message.warning('data facet key 必须由字母数字下划线组成')
        return
      }
      if (this.formData.key.length > 50) {
        this.$message.warning('data facet key 的长度不能大于50')
        return
      }
      if (this.formData.processing_needed) {
        this.formData.secondary_data_object_uids = this.checkedKeys.filter(x => x !== this.generateKey(this.addData.table_id)).map(x => this.keyToId(x))
      }
      this.config.confirmLoading = true
      this.save()
        .then(resp => {
          this.$emit('saved', resp)
          this.close()
        })
        .finally(() => {
          this.config.confirmLoading = false
        })
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="less" scoped>
.editor {
  width: 100%;
  height: 500px;
}
/deep/ .ant-form-item {
  margin-bottom: 5px;
}
/deep/ .sql-form .ant-form-item-label {
  line-height: 32px;
}
</style>
