<template>
  <a-card size="small">
    <a-row
      class="attribute-select-box"
      :gutter="15"
    >
      <a-col :span="5">
        <SearchTree
          :tree-data="dataSources"
          :selectedKeys="selectedKeys"
          :replaceFields="replaceFields"
          :loadData="isLoad ? onLoadData: null"
          :loading="treeLoading"
          :isLoad="isLoad"
          :isExpand="isExpand"
          @select="handleSelect"
          @search="loadTreeByName"
        ></SearchTree>
      </a-col>
      <a-col :span="19">
        <base-table
          ref="baseTables"
          v-for="table in tables"
          :key="table.queryApiName"
          :isLoad="false"
          :formItems="table.formItems"
          :queryApiName="table.queryApiName"
          :columns="table.columns"
          :extraData="extraData"
          :exportApi="table.exportApi"
          :exportFileApi="table.exportFileApi"
        ></base-table>
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
import SearchTree from '@/components/common/SearchTree.vue'
import BaseTable from '@/components/StoreCenter/BaseTable.vue'

export default {
  components: { SearchTree, BaseTable },
  props: {
    treeApiName: {
      type: String,
      default: ''
    },
    treeByNameApiName: {
      type: String,
      default: ''
    },
    isLoad: {
      type: Boolean,
      default: false
    },
    treeHeight: {
      type: Number,
      default: 4
    },
    tables: {
      type: Array,
      default () {
        return []
      }
    },
    replaceFields: {
      type: Object,
      default () {
        return { children: 'children', key: 'dict_item_id', title: 'item_name' }
      }
    }
  },
  data () {
    return {
      selectedKeys: [],
      dataSources: [],
      treeLoading: false,
      nullKey: '0-0',
      extraData: {
        dict_item_id: undefined
      },
      isExpand: false
    }
  },
  computed: {
    scroll () {
      return {
        x: this.columns.length > 6 ? 'max-content' : false
      }
    }
  },
  async created () {
    this.loadTree()
  },
  methods: {
    handleSelect (selectedKeys) {
      if (!selectedKeys.length) return
      this.selectedKeys = selectedKeys
      this.extraData.dict_item_id = selectedKeys[0] === this.nullKey ? undefined : selectedKeys[0]
      // this.extraData.level_code = node.dataRef.level_code
      this.$refs.baseTables &&
        this.$refs.baseTables.forEach(x => {
          x.search()
        })
    },
    handleTreeData (treeData) {
      if (!treeData) return undefined
      treeData.forEach(x => {
        // x.selectable = !!x[this.replaceFields.key]
        if (this.isLoad) {
          x.isLeaf = x.level_code === this.treeHeight
        }
        x.children && this.handleTreeData(x.children)
      })
      return treeData
    },
    async loadTree (loadTable = true) {
      this.treeLoading = true
      this.selectedKeys = [this.nullKey]
      this.dataSources = []
      const resp = await this.$api[this.treeApiName]().finally(() => {
        this.treeLoading = false
      })
      if (!resp || !resp.object) return
      this.dataSources = this.handleTreeData([resp.object])
      if (this.dataSources.length && this.dataSources[0] && loadTable) {
        this.handleSelect([this.dataSources[0][this.replaceFields.key] || this.nullKey])
      }
    },
    async loadTreeByName (name) {
      console.log('name', name)
      if (!this.isLoad) return
      this.isExpand = false
      if (!name) {
        await this.loadTree()
        this.isExpand = true
        return
      }
      this.treeLoading = true
      this.dataSources = []
      const resp = await this.$api[this.treeByNameApiName](name).finally(() => {
        this.treeLoading = false
      })
      if (!resp || !resp.object) return
      if (resp.object.children) {
        this.dataSources = this.handleTreeData([resp.object])
      }
      this.isExpand = true
    },
    onLoadData (treeNode) {
      return new Promise(resolve => {
        if (treeNode.dataRef.children && treeNode.dataRef.children.length || !treeNode.dataRef[this.replaceFields.key]) {
          resolve()
          return
        }
        this.$api[this.treeApiName](treeNode.dataRef[this.replaceFields.key]).then(resp => {
          treeNode.dataRef.children = this.handleTreeData(resp.object.children)
          this.dataSources = [...this.dataSources]
          resolve()
        }).catch(() => resolve())
      })
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .base-table {
  margin-bottom: 20px;
  &:last-child {
    margin-bottom: 5px;
  }
}
</style>
