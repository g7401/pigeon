<template>
  <a-tree-select
    :dropdownStyle="{ maxHeight: '300px' }"
    treeDefaultExpandAll
    :treeData="treeData"
    placeholder="Please select"
    :disabled="disabled"
    :value="value"
    @change="$emit('input', $event)"
    @select="handleSelect"
  ></a-tree-select>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
export default {
  name: 'CacheTreeSelect',
  props: {
    value: {
      type: String,
      default: undefined
    },
    disabled: {
      type: Boolean,
      default: false
    },
    treeName: {
      type: String,
      default: 'dsTree'
    },
    actionName: {
      type: String,
      default: 'getDataSourceTree'
    }
  },
  data () {
    return {
      treeLoading: false
    }
  },
  created () {
    this.loadDataSourceList()
  },
  computed: {
    treeData () {
      return this.generateTreeData(cloneDeep(this.tree))
    },
    tree () {
      return this.$store.getters[this.treeName]
    }
  },
  methods: {
    handleSelect (value, node) {
      this.$emit('select', node.dataRef)
    },
    async loadDataSourceList () {
      this.treeLoading = true
      await this.$store.dispatch(this.actionName).finally(() => (this.treeLoading = false))
      this.$emit('loaded')
    },
    generateTreeData (items, parent) {
      const data = items.slice()
      data.forEach(item => {
        item.title = item.name
        item.value = item.uid
        item.key = item.uid
        if (item.children) this.generateTreeData(item.children, item)
      })
      return data
    }
  }
}
</script>

<style lang="less">
  .ant-select-tree-dropdown .ant-select-tree li span.ant-select-tree-switcher {
    float: left;
  }
</style>
