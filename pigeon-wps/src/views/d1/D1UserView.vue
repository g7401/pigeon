<template>
  <a-layout class="d1-user-view">
    <a-layout-sider
      class="sider mini-scrollbar pd-5"
      theme="light"
      :collapsible="true"
      :collapsed="collapsed"
      :collapsedWidth="30"
      breakpoint="lg"
      :style="{ 'overflow-y': collapsed ? 'hidden' : 'auto', 'overflow-x': 'hidden', height: 'auto', maxHeight: 'auto', position: 'relative', left: 0 }"
      :class="{ 'hidden-scroll': collapsed }"
      :width="siderWidth + 'px'"
      :trigger="null"
    >
      <a-icon
        class="expand-button"
        type="double-right"
        v-if="collapsed"
        @click="setSidebar(true)"
      />
      <template v-show="!collapsed">
        <a-row class="mg-b-5">
          <!-- <div class="label mg-b-5">Tag</div> -->
          <a-select
            style="width:100%"
            @change="loadTableData"
            mode="multiple"
            v-model="params.tag"
            :options="tagOptions"
            placeholder="Tags"
            allowClear
          ></a-select>
        </a-row>
        <a-row class="mg-b-5">
          <a-input
            v-model="params.name"
            placeholder="Filter"
            allowClear
            @change="loadTableData"
          ></a-input>
        </a-row>
        <vxe-pager
          class="sider-pager"
          perfect
          size="mini"
          :loading="queryLoading"
          v-bind="pagination"
          @page-change="handleTableChange">
        </vxe-pager>
        <vxe-grid
          v-show="dataFacetVisible"
          :ref="randomRef"
          show-overflow
          class="more-small-table mini-scrollbar row--selectable"
          :data="tableData"
          :loading="queryLoading"
          :columns="columns"
          :show-header="false"
          size="small"
          highlight-hover-row
          highlight-current-row
          @current-change="currentChangeEvent"
        >
        </vxe-grid>
      </template>
      <!-- <vue-draggable-resizable
        class="draggable-handle"
        :w="10"
        :z="1"
        :x="siderWidth - 10"
        axis="x"
        :draggable="true"
        :resizable="false"
        :onDrag="onDrag"
      >
      </vue-draggable-resizable> -->
    </a-layout-sider>
    <a-layout :style="{ overflow: 'hidden' }">
      <a-layout-content
        class="table-area"
        :class="{'is-initial': !activeTab}"
      >
        <a-tabs
          v-if="activeTab"
          v-model="activeTab"
          hideAdd
          type="editable-card"
          size="small"
          :tabBarGutter="1"
          @edit="onEdit"
          @change="handleTabChange"
        >
          <a-tab-pane
            v-for="dfkItem in dfks"
            :key="dfkItem.key"
          >
            <template #tab>
              <a-dropdown :trigger="['contextmenu']">
                <span>{{ dfkItem.name }}</span>
                <a-menu slot="overlay">
                  <a-menu-item @click="refresh(dfkItem.key)">
                    Refresh
                  </a-menu-item>
                </a-menu>
              </a-dropdown>
            </template>
            <D1PreviewView :ref="dfkItem.key" :dfk="dfkItem.key" apiType="deployment"></D1PreviewView>
          </a-tab-pane>
        </a-tabs>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script>
import '@/core/vxe-table'
import D1PreviewView from '@/views/d1/D1PreviewView'
import debounce from 'lodash.debounce'
// import cloneDeep from 'lodash.clonedeep'
import VueDraggableResizable from 'vue-draggable-resizable'
import { mapActions, mapGetters } from 'vuex'
export default {
  name: 'D1UserView',
  components: { D1PreviewView, VueDraggableResizable },
  data () {
    return {
      siderWidth: 280,
      randomRef: Math.random().toString(),
      tableData: [],
      dfks: [],
      activeTab: '',
      columns: [
        {
          title: 'Data Facet Name',
          field: 'name'
          // slots: {
          //   default: ({ row, column }) => {
          //     const strs = [`key: ${row.key}`]
          //     if (row.description) {
          //       strs.push(`description: ${row.description}`)
          //     }
          //     return [<a-tooltip placement="left" title={strs.join('\n')}><span>{row.name}</span></a-tooltip>]
          //   }
          // }
        }
      ],
      params: {
        tag: [],
        name: undefined
      },
      pagination: {
        currentPage: 1,
        pageSize: 20,
        total: 0,
        align: 'left',
        layouts: ['PrevPage', 'Jump', 'PageCount', 'NextPage', 'Total']
        // perfect: true
      },
      tagOptions: [],
      queryLoading: false,
      dataFacetVisible: true,
      localUserKey: 'LOCAL_USER_DATA_FACET_KEY'
    }
  },
  computed: {
    ...mapGetters(['sidebar']),
    collapsed () {
      return !this.sidebar
    }
  },
  created () {
    const cacheDfk = this.$ls.get(this.localUserKey)
    if (cacheDfk) {
      this.activeTab = cacheDfk
    }
    this.loadTableData = debounce(this.loadTableData, 500)
    this.getDataFacetTags()
    this.loadTableData()
  },
  // async mounted () {
  //   await this.loadTableData()
  // },
  watch: {
    activeTab (newVal) {
      if (!newVal) return
      this.setActiveRow(newVal)
    },
    collapsed () {
      if (!this.collapsed) {
        this.refreshGrid()
      } else {
        this.dataFacetVisible = false
      }
    }
  },
  methods: {
    ...mapActions(['setSidebar']),
    handleTabChange () {
      this.$refs[this.activeTab][0].refreshTable()
    },
    refresh (key) {
      this.$refs[key][0].refresh()
    },
    refreshGrid () {
      this.dataFacetVisible = false
      setTimeout(() => {
        this.$refs[this.randomRef].recalculate(true)
        this.dataFacetVisible = true
      }, 250)
    },
    onDrag (x) {
      if (x < 220 || x > 500) {
        return false
      }
      this.siderWidth = x + 10
      this.refreshGrid()
    },
    async getDataFacetTags () {
      const result = await this.$api.getAllTags()
      this.tagOptions = result.map(x => ({
        label: x,
        value: x
      }))
    },
    deleteDfk (key) {
      const index = this.dfks.findIndex(item => item.key === key)
      if (index !== -1 && this.dfks.length > 1) {
        this.dfks.splice(index, 1)
        this.activeTab = this.dfks[this.dfks.length - 1].key
      } else {
        this.$message.info("can't close the last tab")
      }
    },
    onEdit (key, action) {
      if (action === 'remove') {
        this.deleteDfk(key)
      }
    },
    handleTableChange ({ currentPage, pageSize }) {
      this.pagination.currentPage = currentPage
      this.pagination.pageSize = pageSize
      this.loadTableData()
    },
    async loadTableData () {
      const requestData = {
        ...this.params,
        ...this.extraData
      }
      Object.assign(requestData, {
        page: this.pagination.currentPage - 1,
        size: this.pagination.pageSize
      })
      this.queryLoading = true
      const result = await this.$api.getUserDataFacets(requestData).finally(() => (this.queryLoading = false))
      this.tableData = result.content || this.dfks
      this.pagination.total = result.total
      if (this.activeTab) {
        this.setActiveRow(this.activeTab)
        // if (!isSet) {
        //   this.activeTab = null
        // }
        return
      }
      if (this.tableData.length) {
        this.handleClickRow(this.tableData[0])
      } else {
        this.handleClickRow(null)
      }
    },
    currentChangeEvent ({ row }) {
      this.handleClickRow(row)
    },
    handleClickRow (record) {
      if (record) {
        this.$ls.set(this.localUserKey, record.key)
        this.$nextTick(() => {
          this.$refs[this.randomRef].setCurrentRow(record)
        })
        if (!this.dfks.find(item => item.key === record.key)) {
          this.dfks.push(record)
        }
      } else {
        this.$refs[this.randomRef].clearCurrentRow()
      }
      this.activeTab = record?.key
    },
    setActiveRow (key) {
      const record = this.tableData.find(item => item.key === key)
      if (record) {
        this.handleClickRow(record)
        return true
      } else {
        this.$refs[this.randomRef].clearCurrentRow()
      }
      return false
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .row--selectable .vxe-body--row {
  cursor: pointer;
  &.row--current {
    cursor: default;
  }
}
.label {
  color: rgba(0, 0, 0, 0.85);
  font-size: 13px;
  line-height: 1.5;
}
/deep/ .expand-button {
  height: 100%;
  width: 100%;
  color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
}
/deep/ .hidden-scroll .ant-layout-sider-children {
  overflow: hidden;
}
/deep/ .sider .vxe-pager .vxe-pager--prev-btn {
  margin-left: 0;
}
/deep/ .ant-tabs-bar {
  margin-bottom: 0px;
}
/deep/ .table-area .ant-layout .ant-card {
  border: 1px solid #e8e8e8;
  border-top: 0;
}
.draggable-handle {
  height: 100% !important;
  bottom: 0;
  cursor: col-resize;
  touch-action: none;
  position: absolute;
}
/deep/ .ant-tabs-tab {
  font-size: 13px;
}
/deep/ .sider-pager {
  padding: 5px;
  border-bottom: 0px;
  border-top: 1px solid #e8eaec;
}
</style>
