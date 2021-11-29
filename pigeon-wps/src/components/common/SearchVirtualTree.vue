<template>
  <div>
    <a-input-search
      class="mg-b-10"
      @change="onChange"
      @search="onSearch"
      @pressEnter="onEnter"
      :enter-button="searchAction === 'click'"
      :loading="loading"
      :placeholder="placeholder"
      allowClear
    ></a-input-search>
    <slot name="searchBottom"></slot>
    <div :class="treeBoxClass">
      <vxe-virtual-tree
        ref="vTree"
        class="row--selectable"
        :loading="loading"
        show-overflow
        :show-header="false"
        :max-height="maxHeight"
        :height="maxHeight ? undefined : height"
        :size="size ? size : operation ? 'small' : 'mini'"
        border="outer"
        :row-id="replaceFields.key"
        row-key
        :data="filterTreeData"
        :tree-config="{ children: replaceFields.children, ...treeConfig }"
        :columns="columns || tableColumns"
        v-bind="$attrs"
        v-on="$listeners"
      >
        <template
          v-slot:name="{ row }"
        >
          <slot name="icon" :node="row"></slot>
          <span class="vxe-checkbox--label vxe-radio--label" v-if="row[replaceFields.title] && row[replaceFields.title].indexOf(searchValue) > -1">
            <span class="str-child">{{ (row[replaceFields.title]).substr(0, row[replaceFields.title].indexOf(searchValue)) }}</span>
            <span class="str-child" style="color: #f50">{{ searchValue }}</span>
            <span class="str-child">{{ (row[replaceFields.title]).substr(row[replaceFields.title].indexOf(searchValue) + searchValue.length) }}</span>
          </span>
          <span class="vxe-checkbox--label vxe-radio--label" v-else>{{ row[replaceFields.title] }}</span>
        </template>
        <template
          v-slot:operation="{ row }"
        >
          <slot name="operation" :node="row"></slot>
        </template>
      </vxe-virtual-tree>
    </div>
  </div>
</template>

<script>
import treeMixin from '@/components/common/mixins/treeMixin'

export default {
  mixins: [treeMixin],
  props: {
    size: {
      type: String,
      default: null
    },
    treeConfig: {
      type: Object,
      default () {
        return {}
      }
    },
    treeData: {
      type: Array,
      default () {
        return []
      }
    },
    replaceFields: {
      type: Object,
      default () {
        return { children: 'children', title: 'title', key: 'key' }
      }
    },
    loading: {
      type: Boolean,
      default: false
    },
    searchAction: {
      type: String,
      default: 'click'
    },
    mode: {
      type: String,
      default: 'select'
    },
    operation: {
      type: Boolean,
      default: false
    },
    height: {
      type: [Number, String],
      default: 500
    },
    maxHeight: {
      type: [Number, String],
      default: undefined
    },
    placeholder: {
      type: String,
      default: 'Filter'
    },
    defaultFilter: {
      type: Function,
      default: null
    },
    expandDefaultFilter: {
      type: Boolean,
      default: false
    },
    defaultExpandKeys: {
      type: Array,
      default: null
    },
    columns: {
      type: Array,
      default: null
    },
    operationWidth: {
      type: String,
      default: null
    },
    labelWidth: {
      type: String,
      default: null
    },
    treeBoxClass: {
      type: String,
      default: null
    }
  },
  data () {
    return {
      searchValue: '',
      treeDataKey: 'filterTreeData',
      filterTreeData: []
    }
  },
  computed: {
    tableColumns () {
      const columns = [
        {
          field: this.replaceFields.title,
          title: 'Name',
          treeNode: true,
          width: this.labelWidth || undefined,
          slots: {
            default: 'name'
          }
        }
      ]
      if (this.operation) {
        columns.push({
          field: 'operation',
          title: 'Operation',
          align: 'left',
          width: this.operationWidth || undefined,
          slots: { default: 'operation' }
        })
      }
      return columns
    }
  },
  watch: {
    defaultFilter: {
      handler (value) {
        this.updateFilterTreeData()
      }
    },
    loading: {
      immediate: true,
      handler (value) {
        !value && this.updateFilterTreeData()
      }
    },
    searchValue: {
      handler (value) {
        this.updateFilterTreeData()
      }
    }
  },
  methods: {
    exprand () {
      if (this.searchValue) {
        this.expandParentByText(this.searchValue)
      } else if (this.defaultFilter && this.expandDefaultFilter) {
        this.waitExpandParent(this.defaultFilter)
      } else if (this.defaultExpandKeys && this.defaultExpandKeys.length) {
        this.expandParentByKeys(this.defaultExpandKeys)
      } else if (this.treeData.length === 1) {
        this.expandSelfByKeys([this.treeData[0][this.replaceFields.key]])
      }
    },
    updateFilterTreeData () {
      let treeData = this.treeData
      if (this.defaultFilter) {
        treeData = this.filterDeep(treeData, this.defaultFilter)
      }
      if (this.searchValue) {
        treeData = this.filterHasSearchValue(treeData, this.searchValue)
      }
      this.filterTreeData = treeData
      this.$refs.vTree && this.recalculate()
      this.exprand()
    },
    setCurrentRow (row) {
      this.$refs.vTree.setCurrentRow(row)
    },
    setCurrentRowByKey (key) {
      const realRow = this.dataList.find(x => x[this.replaceFields.key] === key)
      realRow && this.setCurrentRow(realRow)
    },
    clearCurrentRow () {
      this.$refs.vTree.clearCurrentRow(...arguments)
    },
    recalculate () {
      this.$refs.vTree.recalculate(...arguments)
    },
    setTreeExpand () {
      this.$refs.vTree.setTreeExpand(...arguments)
    },
    setRadioRow () {
      this.$refs.vTree.setRadioRow(...arguments)
    },

    getCheckboxRecords () {
      return this.$refs.vTree.getCheckboxRecords(...arguments)
    },
    getRadioRecord () {
      return this.$refs.vTree.getRadioRecord(...arguments)
    },
    expandParent (fn) {
      const chain = this.dataList.filter(item => fn(item)).reduce((acc, item) => acc.concat(this.getChain(item[this.replaceFields.key], false)), [])
      setTimeout(() => {
        this.$refs.vTree && this.$refs.vTree.setTreeExpand(chain, true)
      }, 50)
    },
    waitExpandParent (fn) {
      const callback = () => this.waitExpandParent(fn)
      if (this.loading) {
        setTimeout(callback, 50)
      } else {
        this.expandParent(fn)
      }
    },
    expandParentByKeys (keys) {
      this.waitExpandParent((item) => keys.includes(item[this.replaceFields.key]))
    },
    expandSelfByKeys (expandedKeys) {
      setTimeout(() => {
        const items = this.dataList.filter(x => expandedKeys.includes(x[this.replaceFields.key]))
        this.$refs.vTree && this.$refs.vTree.setTreeExpand(items, true)
      }, 50)
    },
    filterHasSearchValue (treeData, searchValue) {
      return this.filterDeep(treeData, node => node[this.replaceFields.title].indexOf(searchValue) > -1)
    },
    expandParentByText (value) {
      this.waitExpandParent((item) => value && item[this.replaceFields.title].indexOf(value) > -1)
    },
    handleSearch (value) {
      this.searchValue = value
      this.$emit('search', value)
      if (!value) {
        this.$emit('clearValue')
      }
    },
    onChange (e) {
      if (this.searchAction === 'click') return
      this.handleSearch(e.target.value)
    },
    onSearch (value) {
      if (this.searchAction === 'change') return
      this.handleSearch(value)
    },
    onEnter (e) {
      if (this.searchAction === 'change') return
      this.handleSearch(e.target.value)
    }
  }
}
</script>

<style lang="less" scoped>
</style>
