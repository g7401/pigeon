<template>
  <a-card
    class="data-source-structure"
    :bordered="false"
    size="small"
  >
    <a-card
      class="search-box"
      :bordered="false"
      size="small"
    >
      <a-row
        type="flex"
        justify="space-between"
      >
        <a-col :span="20">
          <a-form-model
            layout="vertical"
            :colon="false"
            :model="params"
            :labal-col="{ span: 6 }"
          >
            <a-row :gutter="24">
              <a-col :span="8">
                <a-form-model-item label="Data Source Name">
                  <a-input
                    allowClear
                    v-model="params.name"
                    placeholder="Data Source Name"
                  ></a-input>
                </a-form-model-item>
              </a-col>
            </a-row>
          </a-form-model>
        </a-col>
        <a-col :span="4">
        </a-col>
      </a-row>
    </a-card>
    <a-row class="toolbar-box">
      <a-button
        class="pull-right"
        type="primary"
        icon="plus-circle"
        @click="addChild(dtTree[0])"
      >Create Data Target</a-button>
    </a-row>
    <a-table
      v-if="tableVisiable"
      :bordered="true"
      rowKey="uid"
      :dataSource="filterTableData"
      :columns="columns"
      :components="getComponents(columns, scroll)"
      :pagination="false"
      :loading="treeLoading"
      :defaultExpandAllRows="true"
      :expandIconColumnIndex="1"
      :indentSize="10"
      :transformCellText="renderCellFn"
      :scroll="scroll"
      size="small"
      class="more-small-table"
    >
      <template
        slot="source"
        slot-scope="text, record"
      >
        {{ record.sds.concat(record.dt).join(', ') }}
      </template>
      <template
        slot="name"
        slot-scope="text"
      >
        <template v-if="text.indexOf(params.name) > -1">
          {{ text.substr(0, text.indexOf(params.name)) }}<span style="color: #f50">{{ params.name }}</span>{{ text.substr(text.indexOf(params.name) + params.name.length) }}
        </template>
        <template v-else>{{ text }}</template>
      </template>
      <template
        slot="operation"
        slot-scope="text, dataSource"
      >
        <a-button-group>
          <a-button
            class="op-btn"
            size="small"
            @click="view(dataSource)"
          >View</a-button>
          <a-button
            class="op-btn"
            size="small"
            type="primary"
            @click="addChild(dataSource)"
          >Add Child</a-button>
          <a-button
            class="op-btn"
            size="small"
            type="primary"
            ghost
            @click="edit(dataSource)"
          >Edit</a-button>
          <a-popconfirm
            title="confirm delete?"
            cancel-text="No"
            ok-text="Yes"
            :loading="deleteLoading"
            @confirm="del(dataSource)"
          >
            <a-button
              class="op-btn"
              size="small"
              type="danger"
              v-if="!dataSource.children || !dataSource.children.length"
            >Delete</a-button>
          </a-popconfirm>
        </a-button-group>
      </template>
    </a-table>
  </a-card>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import { mapGetters } from 'vuex'
// import { cutStrByFullLength } from '@/components/_util/util'
import tableResizeMixin from '@/components/StoreCenter/mixins/tableResizeMixin'
export default {
  name: 'DataTargetDef',
  mixins: [tableResizeMixin],
  data () {
    return {
      tableVisiable: true,
      replaceFields: { children: 'children', title: 'name', key: 'uid' },
      dsFormModalVisible: false,
      activeParent: {},
      activeItem: {},
      deleteLoading: false,
      treeLoading: false,
      params: {},
      formItems: [
        {
          label: 'Data Target Name',
          key: 'name',
          type: 'singleInput',
          placeholder: 'Data Target Name',
          span: 8
        }
      ],
      scroll: { y: '60vh', x: 1550 },
      columns: [
        {
          title: 'No.',
          dataIndex: 'serial',
          scopedSlots: { customRender: 'serial' },
          width: 70,
          fixed: 'left'
        },
        {
          title: 'Data Target Name',
          dataIndex: 'name',
          scopedSlots: { customRender: 'name' },
          width: 250,
          fixed: 'left'
          // ellipsis: true
        },
        {
          title: 'Data Target Description',
          dataIndex: 'description',
          width: 250
          // ellipsis: true
        },
        {
          title: 'Data Source Name',
          dataIndex: 'source',
          scopedSlots: { customRender: 'source' },
          width: 150
        },
        {
          title: 'Create Time',
          dataIndex: 'create_time',
          key: 'create_time',
          width: 150
        },
        {
          title: 'Create By',
          dataIndex: 'create_by',
          key: 'create_by',
          width: 150
        },
        {
          title: 'Last Update Time',
          dataIndex: 'last_update_time',
          key: 'last_update_time',
          width: 150
        },
        {
          title: 'Last Update By',
          dataIndex: 'last_update_by',
          key: 'last_update_by'
        },
        {
          title: 'Operation',
          dataIndex: 'operation',
          scopedSlots: { customRender: 'operation' },
          width: 210,
          fixed: 'right'
        }
      ]
    }
  },
  async activated () {
    await this.loadDataSourceList()
  },
  computed: {
    tableData () {
      return this.getFormatTree((this.dtTree && this.dtTree.length && this.dtTree[0].children) || [])
    },
    filterTableData () {
      if (!this.params.name) return this.tableData
      let treeData = cloneDeep(this.tableData)
      treeData = this.filterHasSearchValue(treeData, this.params.name)
      return treeData
    },
    ...mapGetters(['dtTree'])
  },
  watch: {
    filterTableData: {
      deep: true,
      handler (newVal, oldVal) {
        this.refreshTable()
      }
    }
  },
  methods: {
    renderCellFn ({ text }, maxLength = 30) {
      return text
      // if (typeof text !== 'string') return text
      // return cutStrByFullLength(text, maxLength)
    },
    refreshTable () {
      this.tableVisiable = false
      this.$nextTick(() => {
        this.tableVisiable = true
      })
    },
    filterHasSearchValue (treeData, searchValue, deep = true) {
      const copyTree = cloneDeep(treeData)
      const newTreeData = copyTree.filter(x => {
        const hasValue = this.hasSearchValue(x, searchValue)
        if (x[this.replaceFields.children] && hasValue !== 1) {
          x[this.replaceFields.children] = this.filterHasSearchValue(
            x[this.replaceFields.children],
            searchValue,
            !hasValue
          )
        }
        return hasValue
      })
      return newTreeData
    },
    hasSearchValue (node, searchValue, deep = 1) {
      if (node[this.replaceFields.title].indexOf(searchValue) > -1) {
        return deep
      } else if (node[this.replaceFields.children] && node[this.replaceFields.children].length) {
        return node[this.replaceFields.children].some(x => this.hasSearchValue(x, searchValue, deep++))
      } else {
        return false
      }
    },
    getFormatTree (tree, cache = { i: 1 }) {
      const formatTree = cloneDeep(tree)
      formatTree.forEach(x => {
        x.serial = cache.i
        cache.i++
        if (x.children) {
          if (!x.children.length) {
            x.children = null
          } else {
            x.children = this.getFormatTree(x.children, cache)
          }
        }
      })
      return formatTree
    },

    addChild (parent) {
      this.type = 'create'
      this.activeParent = parent
      this.$router.push({
        name: 'CreateDataTarget',
        query: {
          parent_uid: parent.uid
        }
      })
    },
    edit (item) {
      this.$router.push({
        name: 'EditDataTarget',
        query: {
          dt_def_uid: item.uid
        }
      })
    },
    view (item) {
      this.$router.push({
        name: 'ViewDataTarget',
        query: {
          dt_def_uid: item.uid
        }
      })
    },
    async loadDataSourceList () {
      this.treeLoading = true
      await this.$store.dispatch('getDataTargetTree', false).finally(() => (this.treeLoading = false))
    },
    async del (dataSource) {
      this.deleteLoading = true
      await this.$api.deleteDataTarget(dataSource.uid).finally(() => (this.deleteLoading = false))
      this.loadDataSourceList()
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .op-btn {
  // width: 18px;
  height: 18px;
  margin-left: 5px;
  font-size: 12px;
  line-height: 12px;
}
/deep/ .btn-group {
  float: right;
  display: flex;
  position: relative;
  top: 50%;
  transform: translateY(-50%);
}
/deep/ .top-operation {
  margin: 5px 6px;
  .top-right-btns {
    display: flex;
    justify-content: flex-end;
    align-items: center;
  }
}
/deep/ .only-show-dfk {
  margin: 0 6px;
  font-size: 12px;
}
/deep/ .ant-tree li span.ant-tree-iconEle {
  display: inline-block;
  width: 18px;
  height: 24px;
  margin: 0;
  line-height: 24px;
  text-align: center;
  vertical-align: top;
  border: 0 none;
  outline: none;
  cursor: pointer;
}
/deep/ .ant-tree li .ant-tree-node-content-wrapper {
  padding-left: 0;
}
/deep/ .ant-tree li ul {
  margin: 0;
  padding: 0 0 0 10px;
}
/deep/ .ant-layout-sider-collapsed {
  overflow-x: hidden;
}
/deep/ td {
  text-overflow: ellipsis;
  overflow: hidden;
}
/deep/ .ant-checkbox-wrapper {
  margin-right: 5px;
}
/deep/ .tree-loading-icon {
  display: inline-block;
  margin-top: 10px;
  margin-left: 6px;
}

.search-btn-group {
  text-align: right;
  padding: 0;
  position: absolute;
  bottom: 0;
  right: 0;
}
.toolbar-box {
  border-top: 1px solid #e8e8e8;
}
.toolbar-box {
  padding: 10px 0;
}
.ant-form-item {
  margin-bottom: 0;
}
.ant-form-item {
  padding-bottom: 0;
}
</style>
