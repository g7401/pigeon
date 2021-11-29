<template>
  <div class="mini-scrollbar">
    <a-input-search
      class="mg-b-10"
      :value="searchValue"
      @change="onChange"
      @search="onSearch"
      @pressEnter="onEnter"
      :loading="loading"
      :placeholder="placeholder"
      allowClear
      :class="{'active-search-icon': searchAction === 'click'}"
    ></a-input-search>
    <a-card
      size="small"
      class="tree-card"
      :style="{maxHeight: height}"
    >
      <a-icon
        class="tree-loading-icon"
        type="loading"
        v-if="loading"
      />
      <span v-if="!loading && !filterTreeData.length">N/A</span>
      <a-tree
        ref="tree"
        v-show="!loading"
        v-bind="$attrs"
        :tree-data="filterTreeData"
        :expanded-keys="expandedKeys"
        :auto-expand-parent="autoExpandParent"
        :replaceFields="replaceFields"
        :selectedKeys="mode === 'select' ? keys : undefined"
        :checkedKeys="mode === 'checkbox' ? keys : undefined"
        :checkable="mode === 'checkbox'"
        :selectable="mode === 'select'"
        @expand="onExpand"
        v-on="$listeners"
      >
        <template v-slot:[replaceFields.title]="node">
          <span class="str-parent" v-if="node[replaceFields.title] && node[replaceFields.title].indexOf(searchValue) > -1">
            <span class="str-child">{{ (node[replaceFields.title]).substr(0, node[replaceFields.title].indexOf(searchValue)) }}</span>
            <span class="str-child" style="color: #f50">{{ searchValue }}</span>
            <span class="str-child">{{ (node[replaceFields.title]).substr(node[replaceFields.title].indexOf(searchValue) + searchValue.length) }}</span>
          </span>
          <span v-else>{{ node[replaceFields.title] }}</span>
          <slot name="after" :node="node"></slot>
        </template>
      </a-tree>
    </a-card>
  </div>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import treeMixin from '@/components/common/mixins/treeMixin'
export default {
  mixins: [treeMixin],
  props: {
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
    selectedKeys: {
      type: Array,
      default () {
        return []
      }
    },
    checkedKeys: {
      type: Array,
      default () {
        return []
      }
    },
    searchAction: {
      type: String,
      default: 'click'
    },
    isLoad: {
      type: Boolean,
      default: false
    },
    isExpand: {
      type: Boolean,
      default: false
    },
    mode: {
      type: String,
      default: 'select'
    },
    height: {
      type: String,
      default: '60vh'
    },
    placeholder: {
      type: String,
      default: 'Filter'
    },
    defaultFilter: {
      type: Function,
      default: null
    }
  },
  data () {
    return {
      searchValue: '',
      expandedKeys: [],
      autoExpandParent: false,
      firstExpand: false
    }
  },
  computed: {
    filterTreeData () {
      let treeData = cloneDeep(this.treeData)
      if (this.defaultFilter) {
        treeData = this.filterDeep(treeData, this.defaultFilter)
      }
      if (!this.searchValue || this.isLoad) return this.generateTreeData(treeData)
      treeData = this.filterHasSearchValue(treeData, this.searchValue)
      return this.generateTreeData(treeData)
    },
    keys () {
      if (this.mode === 'select') {
        return this.selectedKeys
      } else {
        return this.checkedKeys
      }
    }
  },
  watch: {
    keys: {
      immediate: true,
      handler (val) {
        if (!val.length) return
        if (this.firstExpand) return
        this.expandParentByKeys(val)
        this.firstExpand = true
      }
    },
    searchValue (value) {
      value && this.expandParentByText(value)
    },
    isExpand (value) {
      value && this.isLoad && this.expandParentByText(this.searchValue)
    },
    treeData: {
      immediate: true,
      handler () {
        if (!this.keys.length && this.treeData.length === 1) {
          this.onExpand([this.treeData[0][this.replaceFields.key]])
        }
      }
    }
  },
  methods: {
    waitExpandParent (fn) {
      const callback = () => this.waitExpandParent(fn)
      if (this.loading) {
        setTimeout(callback, 100)
      } else {
        this.expandParent(fn)
      }
    },
    expandParentByKeys (keys) {
      this.waitExpandParent((item) => keys.includes(item[this.replaceFields.key]))
    },
    expandSelfByKeys (expandedKeys) {
      this.expandedKeys = expandedKeys
      this.autoExpandParent = true
    },
    onExpand (expandedKeys) {
      this.expandedKeys = expandedKeys
      this.autoExpandParent = false
    },
    filterHasSearchValue (treeData, searchValue) {
      return this.filterDeep(treeData, node => node[this.replaceFields.title].indexOf(searchValue) > -1)
    },
    expandParentByText (value) {
      this.waitExpandParent((item) => value && item[this.replaceFields.title].indexOf(value) > -1)
    },
    handleSearch (value) {
      this.searchValue = value
      if (this.isLoad) {
        this.$emit('search', value)
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
    },
    generateTreeData (items, parent) {
      if (!parent) {
        items = cloneDeep(items)
      }
      items.forEach(item => {
        if (!item.scopedSlots) {
          item.scopedSlots = { title: this.replaceFields.title }
        }
        if (item.children) this.generateTreeData(item.children, item)
      })
      return items
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .tree-card {
  overflow-y: auto;
}
/deep/ .tree-loading-icon {
  display: inline-block;
  margin-top: 10px;
  margin-left: 6px;
}
/deep/ .active-search-icon .ant-input:focus + .ant-input-suffix {
  color: #1890ff;
}
// .str-child {
//   font-size: 12px;
// }
</style>
