<template>
  <div
    class="base-form"
    v-if="value"
  >
    <a-modal
      :title="title"
      :ok-text="mergeConfig.okText"
      :cancel-text="mergeConfig.cancelText"
      :visible="value"
      :confirm-loading="mergeConfig.confirmLoading"
      :width="mergeConfig.width"
      @ok="handleOk"
      @cancel="close"
    >
      <template>
        <p class="tip-text">Choose another Data Facet and copy the configuration of the fields with the same name from it.</p>
        <SearchTree
          v-if="dataSources && dataSources.length"
          ref="tree"
          searchAction="change"
          :tree-data="dataSources"
          :replaceFields="replaceFields"
          :selectedKeys="selectedKeys"
          :defaultFilter="filterDataFacet"
          @select="handleSelect"
        ></SearchTree>
      </template>
    </a-modal>
  </div>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import SearchTree from '@/components/common/SearchTree'
import { mapGetters } from 'vuex'
const baseConfig = {
  okText: 'Confirm',
  cancelText: 'Cancel',
  width: 700,
  confirmLoading: false
}
export default {
  components: { SearchTree },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    title: {
      type: String,
      default: 'Copy Configuration From Another Data Facet'
    },
    value: {
      type: Boolean,
      default: false
    },
    uid: {
      type: [Number, String],
      default: null
    }
  },
  data () {
    return {
      formRefName: Math.random()
        .toString()
        .slice(2),
      replaceFields: { children: 'children', key: 'key', title: 'label' },
      joinStr: '___',
      selectedKeys: [],
      selectedId: null
    }
  },
  watch: {
    value (val) {
      this.selectedKeys = []
      this.selectedId = null
      val && this.$nextTick(() => {
        if (!this.$refs.tree) return
        this.$refs.tree.expandParent(item => item.type === 'df')
      })
    }
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    },
    dataSources () {
      return this.generateTreeData(cloneDeep(this.d1DsTree))
    },
    ...mapGetters(['d1DsTree'])
  },
  methods: {
    filterDataFacet (item) {
      return item.type === 'df'
    },
    generateTreeData (items, parent) {
      const data = items
      data.forEach(item => {
        item.checkable = false
        item.scopedSlots = { title: 'label' }
        // item.disabled = item.type !== 'df' || item.id === this.uid
        item.selectable = item.type === 'df' && item.id !== this.uid
        // item.disableCheckbox = item.id === this.uid
        if (item.selectable) {
          item.class = 'selectable-node'
        } else {
          item.class = 'disabled-node'
        }
        if (parent) {
          item.key = `${parent.key}${this.joinStr}${item.id || item.label}`
        } else {
          item.key = item.id || item.label
        }
        if (item.type === 'df') {
          item.checkable = true
          delete item.children
        }
        if (item.type !== 'df' && item.children) this.generateTreeData(item.children, item)
      })
      return data
    },
    handleSelect (selectedKeys, { node }) {
      this.selectedKeys = selectedKeys
      this.selectedId = node.dataRef.id
    },
    handleOk () {
      if (!this.selectedId) {
        return this.$message.info('Please Choose Data Facet')
      }
      this.$emit('select', this.selectedId)
      this.close()
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .selectable-node {
  font-weight: 550;
}
</style>
