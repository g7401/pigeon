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
      @cancel="handleCancel"
    >
      <template>
        <a-row class="mg-b-5">
          <a-checkbox v-model="onlySelectedFields">只显示已勾选字段</a-checkbox>
        </a-row>
        <SearchTree
          mode="checkbox"
          searchAction="change"
          checkable
          :tree-data="treeData"
          :checkedKeys="checkedKeys"
          :replaceFields="replaceFields"
          height="50vh"
          placeholder="过滤"
          @check="handleSelect"
        ></SearchTree>
      </template>
      <template slot="footer">
        <a-button key="submit" type="primary" :loading="mergeConfig.confirmLoading" @click="handleOk">
          确认
        </a-button>
        <a-button key="back" @click="clearConfig">
          清空配置
        </a-button>
      </template>
    </a-modal>
  </div>
</template>

<script>
import SearchTree from '@/components/common/SearchTree'
const baseConfig = {
  okText: '确认',
  cancelText: '清空配置',
  width: 900,
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
      default: '选择字段'
    },
    value: {
      type: Boolean,
      default: false
    },
    fields: {
      type: Array,
      default () {
        return []
      }
    },
    replaceFields: {
      type: Object,
      default () {
        return { children: 'children', key: 'field_name', title: 'title' }
      }
    }
  },
  data () {
    return {
      onlySelectedFields: false,
      checkedKeys: [],
      formRefName: Math.random()
        .toString()
        .slice(2)
    }
  },
  watch: {
    fields (val) {
      if (val && val.length) {
        // this.initCheckedKeys()
      }
    }
  },
  created () {
    // this.initCheckedKeys()
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    },
    treeData () {
      return this.fields.map(x => ({
        title: [x.field_label, x.field_name, x.field_description].filter(x => x).join(','),
        checkable: true,
        scopedSlots: { title: 'title' },
        ...x
      })).filter(x => !this.onlySelectedFields || this.checkedKeys.includes(x[this.replaceFields.key]))
    },
    selectedFields () {
      return this.fields.filter(x => this.checkedKeys.includes(x[this.replaceFields.key]))
    }
  },
  methods: {
    // initCheckedKeys () {
    //   this.checkedKeys = this.fields.filter(x => x.selected).map(x => x[this.replaceFields.key])
    // },
    handleSelect (checkedKeys) {
      this.checkedKeys = checkedKeys
    },
    async handleOk () {
      if (!this.checkedKeys.length) {
        return this.$message.info('Pleace select')
      }
      this.$emit('selected', this.checkedKeys)
      this.close()
    },
    clearConfig () {
      this.checkedKeys = []
      this.$emit('selected', this.checkedKeys)
      this.close()
    },
    handleCancel () {
      // this.initCheckedKeys()
      this.close()
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
    }
  }
}
</script>

<style>
</style>
