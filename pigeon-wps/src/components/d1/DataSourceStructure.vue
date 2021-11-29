<template>
  <div class="data-source-structure">
    <DataSourceFormModal
      v-if="dsFormModalVisible"
      v-model="dsFormModalVisible"
      :dsId="dsId"
      @saved="handleAfterSave"
      @close="dsId = 0"
    ></DataSourceFormModal>
    <DfkFormModal
      v-if="editDfkVisible"
      v-model="editDfkVisible"
      :addData="addData"
      :uid="activeDfkItem ? activeDfkItem.id : null"
      @saved="afterSaveDfk"
      @close="activeDfkItem = null;activeTable = null"
    >
      <template v-slot:header>
        <a-breadcrumb
          class="mg-b-15"
          v-if="formCrumbs.length"
        >
          <a-breadcrumb-item
            v-for="crumb in formCrumbs"
            :key="crumb"
          >{{ crumb }}</a-breadcrumb-item>
        </a-breadcrumb>
      </template>
    </DfkFormModal>
    <div class="op-btn-box">
      <a-row>
        <a-button
          class="left-btn"
          type="primary"
          size="small"
          @click="showAddModal"
        >Add Data Source</a-button>
        <a-button
          class="pull-right"
          type="primary"
          size="small"
          :loading="refreshAllLoading"
          @click="reFreshAll"
        >Refresh All Data Source</a-button>
      </a-row>
      <a-row
        class="mg-t-5"
      >
        <a-checkbox
          style="font-size: 12px"
          v-model="isOnlyDfk"
        >Show Data Facet Only</a-checkbox>
      </a-row>
    </div>
    <div class="pd-lr-5">
      <SearchVirtualTree
        ref="vTree"
        size="mini"
        class="more-small-table row--selectable"
        treeBoxClass="tree-box"
        v-if="treeVisiable"
        maxHeight="100%"
        :loading="treeLoading"
        :treeData="treeData"
        :defaultFilter="defaultFilter"
        :replaceFields="replaceFields"
        highlight-current-row
        operation
        operationWidth="30%"
        labelWidth="70%"
        expandDefaultFilter
        @current-change="handleSelect"
      >
        <template #icon="{ node }">
          <a-icon
            class="node-icon mg-r-5"
            :type="iconMap[node.type]"
          />
        </template>

        <template
          #operation="{ node }"
        >
          <div v-if="node.type === 'server'" class="btn-group">
            <a-button
              class="op-btn"
              icon="reload"
              size="small"
              type="primary"
              :loading="refreshAllLoading || refreshLoadingMap[node.id]"
              @click="refresh(node)"
            ></a-button>
            <a-button
              class="op-btn"
              icon="edit"
              size="small"
              type="primary"
              ghost
              @click="edit(node)"
            ></a-button>
            <a-popconfirm
              title="confirm delete?"
              cancel-text="No"
              ok-text="Yes"
              :loading="deleteLoading"
              @confirm="del(node)"
            >
              <a-button
                class="op-btn"
                icon="close"
                size="small"
                type="danger"
              ></a-button>
            </a-popconfirm>
          </div>
          <div v-else-if="tableTypes.includes(node.type)" class="btn-group">
            <a-button
              class="op-btn"
              icon="plus"
              size="small"
              type="primary"
              @click="addDfk(node)"
            ></a-button>
          </div>
          <div v-else-if="node.type === 'df'" class="btn-group">
            <a-button
              class="op-btn"
              icon="edit"
              size="small"
              type="primary"
              ghost
              @click.stop="editDfk(node)"
            ></a-button>
            <a-popconfirm
              :title="'confirm delete dfk ' + node.label + ' ?'"
              cancel-text="No"
              ok-text="Yes"
              @confirm="deleteDfk(node)"
            >
              <a-button
                class="op-btn"
                icon="close"
                size="small"
                type="danger"
                ghost
                @click.stop
              ></a-button>
            </a-popconfirm>
          </div>
        </template>
      </SearchVirtualTree>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import cloneDeep from 'lodash.clonedeep'
import DataSourceFormModal from './DataSourceFormModal'
import DfkFormModal from './DfkFormModal'
import treeMixin from '@/components/common/mixins/treeMixin'
import SearchVirtualTree from '@/components/common/SearchVirtualTree'
export default {
  mixins: [treeMixin],
  components: {
    DataSourceFormModal,
    DfkFormModal,
    SearchVirtualTree
  },
  props: {
    selectedKey: {
      type: String,
      default: null
    }
  },
  data () {
    return {
      refreshLoadingMap: {},
      selectedKeys: [],
      iconMap: {
        server: 'hdd',
        database: 'database',
        schema: 'user',
        view: 'eye',
        table: 'table',
        df: 'setting'
      },
      // levels: [{ icon: 'hdd', type: 'server' }, { icon: 'database', type: 'database' }, { icon: 'user', type: 'schema' }, { icon: 'eye', type: 'view' }, { icon: 'table', type: 'table' }, { icon: 'setting', type: 'df' }],
      dataSourceList: [],
      dsFormModalVisible: false,
      dsId: 0,
      replaceFields: { children: 'children', title: 'title', key: 'key' },
      editDfkVisible: false,
      dfkObj: {},
      treeData: [],
      activeDfkItem: null,
      activeTable: null,
      deleteLoading: false,
      isOnlyDfk: false,
      treeLoading: false,
      addData: null,
      formCrumbs: [],
      refreshAllLoading: false,
      treeVisiable: true,
      tableTypes: ['table', 'view'],
      shadowVisible: false
    }
  },
  computed: {
    defaultFilter () {
      if (this.isOnlyDfk) {
        return (node) => node.type === 'df'
      }
      return null
    }
  },
  watch: {
    selectedKey (val) {
      if (val !== this.selectedKeys[0]) {
        this.selectedKeys = [val]
        this.setCurrentRow()
      }
    }
  },
  async created () {
    await this.loadDataSourceList()
    const dfId = this.$route.query.uid || Vue.ls.get('CURRENT_DF_ID')
    dfId && this.showFormTableSetting(dfId)
  },
  mounted () {
    const handleScorll = (e) => {
      this.shadowVisible = !!e.target.scrollTop
    }
    const siderElement = document.querySelector('.sider')
    siderElement.addEventListener('scroll', handleScorll, true)
    this.$once('hook:beforeDestroy', () => {
      siderElement.removeEventListener('scroll', handleScorll, true)
    })
  },
  methods: {
    setCurrentRow () {
      this.$refs.vTree.setCurrentRowByKey(this.selectedKeys[0])
    },
    async deleteDfk (node) {
      await this.$api.deleteDfk(node.id)
      this.loadDataSourceList()
    },
    editDfk (node) {
      this.activeDfkItem = node
      this.addData = this.getAddData(node, 'df')
      this.formCrumbs = this.getFormCrumbs(node.label, 'df')
      this.editDfkVisible = true
    },
    addDfk (node) {
      this.activeTable = node
      this.addData = this.getAddData(node)
      this.formCrumbs = this.getFormCrumbs(node.key, 'table')
      this.editDfkVisible = true
    },
    handleAfterSave (dataSource) {
      setTimeout(() => {
        this.loadDataSourceList()
      }, 100)
    },
    showFormTableSetting (id) {
      Vue.ls.set('CURRENT_DF_ID', id)
      const crumbs = this.getCrumbs(id)
      const df = this.dataList.find(x => x.type === 'df' && x.id === id)
      if (!df) return
      if (!crumbs) return
      const data = {
        crumbs,
        id,
        name: df.label,
        selectedKey: df.key
      }
      this.$nextTick(() => {
        this.$refs.vTree.expandParentByKeys([df.key])
      })
      setTimeout(() => {
        this.setCurrentRow()
      }, 50)
      this.$emit('showFormTableSetting', data)
    },
    async afterSaveDfk (dfkItem) {
      if (this.activeDfkItem) {
        this.selectedKeys = [this.activeDfkItem.key]
      } else {
        this.selectedKeys = [`${this.activeTable.key}_${dfkItem.uid}`]
      }
      await this.loadDataSourceList()
      this.$refs.vTree.expandParentByKeys(this.selectedKeys)
      this.showFormTableSetting(dfkItem.uid)
    },
    edit (dataSource) {
      this.dsId = dataSource.id
      this.dsFormModalVisible = true
    },
    showAddModal () {
      this.dsFormModalVisible = true
    },
    async loadDataSourceList () {
      this.treeLoading = true
      const resp = await this.$store.dispatch('getD1DataSourceTree', false)
      this.dataSourceList = resp.children
      this.treeData = this.generateTreeData(cloneDeep(this.dataSourceList))
      this.treeLoading = false
      this.generateList(this.treeData)
      const activeUid = Vue.ls.get('CURRENT_DF_ID')
      if (activeUid) {
        const crumbs = this.getCrumbs(activeUid)
        this.$emit('crumbs:update', crumbs)
      }
    },
    getAddData (obj, type) {
      type = type || this.tableTypes
      const chain = this.getTreeChain(obj.key, type, true, [], 'key')
      const data = {}
      chain.forEach((x, i) => {
        if (x.type === 'view') {
          data[`table_name`] = x.label
          data['table_id'] = x.id
        } else {
          data[`${x.type}_name`] = x.label
          data[`${x.type}_id`] = x.id
        }
      })
      return data
    },
    getTreeChain (key, type, needSelf = false, chain = [], keyName = 'label', typeName = 'type', selfKeyName = null) {
      const types = Array.isArray(type) ? type : [type]
      const tempKeyName = (selfKeyName && !chain.length) ? selfKeyName : keyName
      if (needSelf && !chain.length) {
        const selfItem = this.dataList.find(x => x[tempKeyName] === key && types.includes(x[typeName]))
        if (selfItem) {
          chain.push(selfItem)
        }
      }
      const lastLevel = this.dataList.find(
        item => item.children && item.children.some(child => child[tempKeyName] === key && types.includes(child[typeName]))
      )
      if (lastLevel) {
        chain.unshift(lastLevel)
        this.getTreeChain(lastLevel[keyName], lastLevel[typeName], needSelf, chain, keyName, typeName)
      }
      return chain
    },
    getFormCrumbs (key, type) {
      let chain
      if (this.tableTypes.includes(type)) {
        chain = this.getTreeChain(key, this.tableTypes, true, [], 'key')
      } else if (type === 'df') {
        chain = this.getTreeChain(key, type, false, [], 'key', 'type', 'label')
      }
      return chain.map(x => x.label)
    },
    getCrumbs (dfId) {
      const chain = this.getTreeChain(dfId, 'df', true, [], 'key', 'type', 'id')
      const crumbs = chain.map(x => x.label)
      return crumbs
    },
    generateNode (item, parent) {
      if (item.type === 'df') {
        item.title = item.name || item.label
      } else {
        item.title = item.label
      }
      item.selectable = item.type === 'df'
      if (parent) {
        item.key = `${parent.key}_${item.id || item.label}`
      } else {
        item.key = item.id || item.label
      }
      return item
    },
    generateTreeData (items, parent) {
      const data = items.slice()
      data.forEach(item => {
        this.generateNode(item, parent)
        if (item.children) this.generateTreeData(item.children, item)
      })
      return data
    },
    async refresh (dataSource) {
      this.$set(this.refreshLoadingMap, dataSource.id, true)
      await this.$api.refreshDataSource(dataSource.id).finally(() => {
        this.refreshLoadingMap[dataSource.id] = false
      })
      setTimeout(() => {
        this.loadDataSourceList()
      }, 500)
    },
    async reFreshAll () {
      this.refreshAllLoading = true
      await this.$api.refreshAllDataSource().finally(() => (this.refreshAllLoading = false))
      setTimeout(() => {
        this.loadDataSourceList()
      }, 500)
    },
    async del (dataSource) {
      this.deleteLoading = true
      await this.$api.deleteD1DataSource(dataSource.id).finally(() => (this.deleteLoading = false))
      this.loadDataSourceList()
    },
    handleSelect ({ row }) {
      this.showFormTableSetting(row.id)
      this.selectedKeys = [row.key]
    },
    showDictPage () {
      this.$router.push({
        name: 'DictDef'
      })
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .op-btn {
  width: 18px;
  height: 18px;
  margin-left: 5px;
  font-size: 12px;
  line-height: 12px;
}
/deep/ .btn-group {
  float: right;
  // display: flex;
  // position: relative;
  // top: 50%;
  // transform: translateY(-50%);
}
/deep/ .top-operation {
  // margin: 5px 6px;
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
.text-ellipsis {
  width: 65%;
  display: inline-block;
  text-overflow: ellipsis;
  overflow: hidden;
}
/deep/ .ant-checkbox-wrapper {
  // margin-right: 5px;
  width: 160px;
  margin-top: 4px;
}
/deep/ .tree-loading-icon {
  display: inline-block;
  margin-top: 10px;
  margin-left: 6px;
}
 .op-btn-box {
  padding: 5px;
  position: sticky;
  top: 42px;
  background: #fff;
  z-index: 1;
}
.scroll-shadow {
  box-shadow: 0px 2px 6px rgba(0, 21, 41, 0.35);
}
/deep/ .left-btn {
  width: 150px;
}
/deep/ .active-search-icon .ant-input:focus + .ant-input-suffix {
  color: #1890ff;
}
.no-tag {
 padding-left: 5px;
 text-align: center;
 font-size: 12px;
}
/deep/ .tree-box {
  height: calc(100vh - 220px);
  position: relative
}
</style>
