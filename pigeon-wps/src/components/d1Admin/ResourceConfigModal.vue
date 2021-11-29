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
        <SearchTree
          ref="tree"
          searchAction="change"
          :tree-data="treeData"
          :replaceFields="replaceFields"
          :selectedKeys="selectedKeys"
          :loading="treeLoading"
          @select="handleSelect"
        ></SearchTree>
      </template>
      <template slot="footer">
        <a-button key="submit" type="primary" :loading="mergeConfig.confirmLoading" @click="handleOk">
          Confirm
        </a-button>
        <a-button key="back" @click="clearConfig">
          Clear
        </a-button>
      </template>
    </a-modal>
  </div>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import SearchTree from '@/components/common/SearchTree'
// import { mapGetters } from 'vuex'
const baseConfig = {
  okText: 'Save',
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
      default: 'Choose Data Permission'
    },
    value: {
      type: Boolean,
      default: false
    },
    uid: {
      type: Number,
      default: null
    }
  },
  data () {
    return {
      formRefName: Math.random()
        .toString()
        .slice(2),
      replaceFields: { children: 'children', key: 'key', title: 'name' },
      joinStr: '___',
      selectedKeys: [],
      selectedNode: null,
      resourceList: [],
      treeLoading: false
    }
  },
  watch: {
    uid: {
      immediate: true,
      handler () {
        if (this.uid) {
          this.selectedKeys = [`structure${this.uid}`]
        } else {
          this.selectedKeys = []
        }
      }
    }
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    },
    treeData () {
      return this.generateTreeData(cloneDeep(this.resourceList))
    }
  },
  created () {
    this.loadTreeData()
  },
  methods: {
    async loadTreeData () {
      this.treeLoading = true
      const result = await this.$api.getResourceStructures().finally(() => {
        this.treeLoading = false
      })
      this.resourceList = result ? [result] : []
    },
    generateTreeData (items, parent) {
      const data = items
      data.forEach(item => {
        item.selectable = item.type !== 'category' && item.type !== 'ROOT'
        if (item.selectable) {
          item.class = 'selectable-node'
        } else {
          item.class = 'disabled-node'
        }
        if (item.type !== 'category' && item.type !== 'ROOT') {
          item.category = parent.category || parent
        }
        if (item.type === 'category' || item.type === 'ROOT') {
          item.key = item.type + item.uid
        } else {
          item.key = 'structure' + item.uid
        }
        if (item.children) this.generateTreeData(item.children, item)
      })
      return data
    },
    handleSelect (selectedKeys, { node }) {
      this.selectedKeys = selectedKeys
      this.selectedNode = node.dataRef
    },
    handleOk () {
      if (this.uid && !this.selectedNode) {
        return this.$message.info('No Change')
      }
      if (!this.selectedNode) {
        return this.$message.info('Please Choose Resource Structure')
      }
      this.$emit('select', this.selectedNode)
      this.close()
    },
    clearConfig () {
      this.selectedNode = null
      this.$emit('select', null)
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
