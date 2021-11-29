<template>
  <SearchVirtualTree
    ref="tree"
    :class="{'row--selectable': isSelect}"
    :tree-data="treeData"
    :replaceFields="replaceFields"
    :loading="treeLoading"
    :treeConfig="{ lazy: treeLazy, loadMethod: loadMethod }"
    :checkbox-config="isCheckbox ? {labelField: replaceFields.title, checkField: 'checked', checkStrictly: true, halfField: 'indeterminate', checkMethod} : undefined"
    :radio-config="!isRadio ? { labelField: replaceFields.title } : undefined"
    :defaultExpandKeys="defaultExpandKeys"
    :columns="tableColumn"
    :max-height="maxHeight"
    :operation="operation"
    :highlight-current-row="isSelect"
    :defaultFilter="isCheckedOnly ? checkedDefaultFilter : null"
    :row-class-name="handleRowClass"
    @checkbox-change="checkboxChange"
    @radio-change="radioChange"
    @current-change="currentChangeEvent"
    @search="handleSearch"
    v-bind="$attrs"
    v-on="$listeners"
  >
    <template v-if="CheckboxToolbarVisible" #searchBottom>
      <a-row class="mg-b-10" type="flex" justify="space-between">
        <a-checkbox v-model="isCheckedOnly">Show Checked Only</a-checkbox>
        <a-button v-if="!disableCheckbox" size="small" :disabled="tempValue.length === 0" @click="uncheckAll">Uncheck all</a-button>
      </a-row>
    </template>
    <template
      v-slot:operation="{ node }"
    >
      <slot name="operation" :node="node"></slot>
    </template>
  </SearchVirtualTree>
</template>

<script>
import treeMixin from '@/components/common/mixins/treeMixin'
import SearchVirtualTree from '@/components/common/SearchVirtualTree'

const defaultNodeMethod = (item) => {}

export default {
  mixins: [treeMixin],
  components: {
    SearchVirtualTree
  },
  props: {
    mode: {
      type: String,
      default: null
    },
    checkboxToolbar: {
      type: [Boolean, Object],
      default: false
    },
    disableCheckbox: {
      type: Boolean,
      default: false
    },
    operation: {
      type: Boolean,
      default: false
    },
    apiName: {
      type: String,
      default: 'getDictContentsByParent'
    },
    searchApiName: {
      type: String,
      default: 'getDictContentsByLabel'
    },
    selectApiName: {
      type: String,
      default: 'getUserDictContentBySelect'
    },
    nodeMethod: {
      type: Function,
      default: null
    },
    defaultSelectedKeys: {
      type: Array,
      default: null
    },
    maxHeight: {
      type: Number,
      default: 350
    },
    extraData: {
      type: Object,
      default () {
        return {}
      }
    },
    rootExtraData: {
      type: Object,
      default () {
        return {}
      }
    },
    defaultExtraData: {
      type: Object,
      default: null
    },
    replaceFields: {
      type: Object,
      default () {
        return { children: 'children', title: 'name', key: 'uid', value: 'uid' }
      }
    },
    lazy: {
      type: Boolean,
      default: true
    },
    parentUidField: {
      type: String,
      default: 'dictionary_content_uid'
    }
  },
  data () {
    return {
      treeLazy: true,
      treeLoading: false,
      rawTree: null,
      tempValue: [],
      treeData: [],
      searchValue: undefined,
      isCheckedOnly: false
    }
  },
  created () {
    this.init()
  },
  computed: {
    CheckboxToolbarVisible () {
      return this.checkboxToolbar !== false && this.isCheckbox
    },
    isCheckbox () {
      return this.mode === 'checkbox'
    },
    isSelect () {
      return this.mode === 'select'
    },
    isRadio () {
      return this.mode === 'radio'
    },
    defaultExpandKeys () {
      const defaultExpandKeys = this.$attrs.defaultExpandKeys || []
      if (this.isCheckbox) {
        defaultExpandKeys.push(...this.tempValue)
      }
      if (this.isRadio) {
        defaultExpandKeys.push(this.tempValue)
      }
      return defaultExpandKeys
    },
    tableColumn () {
      const columns = [
        {
          type: this.isCheckbox ? 'checkbox' : this.isRadio ? 'radio' : undefined,
          field: this.replaceFields.title,
          title: 'Name',
          treeNode: true,
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
          slots: { default: 'operation' }
        })
      }
      return columns
    }
  },
  methods: {
    handleRowClass ({ row }) {
      if (this.isSelect && (row.type !== 'ROOT' && !row.disableSelect)) {
        return 'selectable-node'
      }
    },
    checkedDefaultFilter (item) {
      return this.tempValue.includes(item[this.replaceFields.value])
    },
    uncheckAll () {
      this.tempValue = []
      this.treeData = this.generateTreeData(this.rawTree)
      this.$emit('checkboxChange', this.tempValue.slice(), this.extraData)
    },
    async init (cache = true) {
      if (typeof this.checkboxToolbar === 'object' && this.checkboxToolbar) {
        this.isCheckedOnly = this.checkboxToolbar.isCheckedOnly
      }
      if (this.defaultSelectedKeys) {
        if (this.isCheckbox) {
          this.tempValue = this.defaultSelectedKeys.slice()
        }
        if (this.isRadio) {
          this.tempValue = this.defaultSelectedKeys[0]
        }
      }
      await this.loadTreeData(cache, this.defaultExtraData)
    },
    checkMethod ({ row }) {
      if (this.disableCheckbox) return false
      return true
    },
    currentChangeEvent ({ row }) {
      if (row.type === 'ROOT' || row.disableSelect) {
        this.$refs.tree.clearCurrentRow()
        return
      }
      this.$emit('select', row)
    },
    checkboxChange ({ row, checked }) {
      this.tempValue = this.tempValue || []
      if (checked) {
        this.tempValue.push(row[this.replaceFields.value])
      } else {
        const index = this.tempValue.indexOf(row[this.replaceFields.value])
        if (index !== -1) {
          this.tempValue.splice(index, 1)
        }
      }
      this.$emit('checkboxChange', this.tempValue.slice(), row, checked)
    },
    radioChange ({ row }) {
      this.tempValue = row[this.replaceFields.value]
      this.$emit('radioChange', this.tempValue, row)
    },
    handleSearch (value) {
      this.isCheckedOnly = false
      this.searchValue = value
      if (value) {
        this.loadTreeDataByLabel(value)
      } else {
        this.init()
      }
    },
    async loadTreeDataByLabel (searchValue) {
      this.treeLoading = true
      const requestData = {
        label: searchValue,
        ...this.extraData
      }
      if (!this.lazy) {
        this.treeLazy = false
      } else {
        this.treeLazy = true
      }
      const result = await this.$api[this.searchApiName](requestData).catch(() => {
        this.treeLoading = false
      })
      this.rawTree = result?.children || []
      if (this.rawTree) {
        this.treeData = this.generateTreeData(this.rawTree)
      } else {
        this.treeData = []
      }
      this.treeLoading = false
    },
    async loadTreeData (cache = true, extraData = null) {
      !cache && this.$api.removeCache(this.apiName)
      this.treeLoading = true
      const requestData = {
        ...this.extraData,
        ...this.rootExtraData
      }
      let apiName = this.apiName
      if (extraData) {
        Object.assign(requestData, extraData)
        apiName = this.selectApiName
      }
      if (!this.lazy) {
        this.treeLazy = this.lazy
      } else {
        this.treeLazy = true
      }
      const result = await this.$api[apiName](requestData).catch(() => {
        this.treeLoading = false
      })
      this.rawTree = result?.children || []
      if (this.rawTree) {
        this.treeData = this.generateTreeData(this.rawTree)
      } else {
        this.treeData = []
      }
      this.treeLoading = false
    },
    loadMethod ({ row }) {
      return new Promise((resolve, reject) => {
        const requestData = {
          [this.parentUidField]: row[this.replaceFields.key === 'value' ? 'uid' : this.replaceFields.key],
          ...this.extraData
        }
        this.$api[this.apiName](requestData).then(result => {
          if (result && result.children.length) {
            resolve(this.generateTreeData(result.children, row))
          } else {
            row.hasChild = false
            resolve(null)
          }
        })
      })
    },
    generateTreeData (items, parent) {
      if (!Array.isArray(items) && typeof items === 'object' && items !== null) {
        items = [items]
      }
      if (!Array.isArray(items)) {
        return items
      }
      const data = items.slice()
      data.forEach(item => {
        if (item.children && item.children.length) {
          item.hasChild = false
        } else {
          item.hasChild = true
        }
        defaultNodeMethod(item)
        if (this.nodeMethod) {
          this.nodeMethod(item, parent)
        }
        if (this.isCheckbox && this.tempValue) {
          item.checked = this.tempValue.includes(item[this.replaceFields.value])
        }
        if (this.isRadio && item[this.replaceFields.value] === this.tempValue) {
          this.$refs.tree.setRadioRow(item)
        }
        if (item.children) this.generateTreeData(item.children, item)
      })
      return data
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .ant-select {
  width: 100%;
}
/deep/ .selectable-node {
  font-weight: 550;
  cursor: pointer;
}
</style>
