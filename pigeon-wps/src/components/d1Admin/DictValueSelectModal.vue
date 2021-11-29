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
        <DictValue
          ref="tree"
          mode="select"
          :replaceFields="replaceFields"
          :nodeMethod="nodeMethod"
          @select="handleSelect"
        ></DictValue>
      </template>
    </a-modal>
  </div>
</template>

<script>
// import cloneDeep from 'lodash.clonedeep'
import DictValue from '@/views/d1/DictValue'
// import { mapGetters } from 'vuex'
const baseConfig = {
  okText: 'Confirm',
  cancelText: 'Cancel',
  width: 700,
  confirmLoading: false
}
export default {
  components: { DictValue },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    title: {
      type: String,
      default: 'Choose Default Value from Dictionary Content'
    },
    value: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      formRefName: Math.random()
        .toString()
        .slice(2),
      replaceFields: { children: 'children', title: 'name', key: 'id', value: 'value' },
      joinStr: '>',
      selectNode: null,
      treeLoading: false
    }
  },
  watch: {
    async value (val) {
      this.selectNode = null
      val && this.$nextTick(() => {
        if (!this.$refs.tree) return
        this.$refs.tree.expandParent(item => item.type === 'content')
      })
    }
  },
  async created () {
    // this.treeLoading = true
    // await this.$store.dispatch('getDomainAndValueTree').catch(() => {
    //   this.treeLoading = false
    // })
    // this.treeLoading = false
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    }
    // treeData () {
    //   return this.generateTreeData(cloneDeep(this.domainAndValueTree))
    // },
    // ...mapGetters(['domainAndValueTree'])
  },
  methods: {
    handleRowClass ({ row }) {
      if (row.selectable) {
        return 'selectable-node'
      } else {
        return 'disabled-node'
      }
    },
    nodeMethod (item, parent) {
      item.value = [parent?.value, item.name].filter(x => x).join(this.joinStr)
    },
    handleSelect (row) {
      if (row.type !== 'content') {
        this.$refs.tree.clearCurrentRow()
        this.selectNode = null
        return
      }
      this.selectNode = row
    },
    handleOk () {
      if (!this.selectNode) {
        return this.$message.info('Please Choose Dictionary Content')
      }
      const value = this.selectNode[this.replaceFields.value]
      this.$emit('select', value, this.selectNode.description)
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
/deep/ .selectable-node {
  font-weight: 550;
}
</style>
