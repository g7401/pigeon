<template>
  <div class="base-form" v-if="value">
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
        <a-row>
          <a-col :span="12">
            <h3>Dictionary Category & Structure</h3>
            <DictDef @select="handleSelectDomain" :categoryId="categoryId" mode="select"></DictDef>
          </a-col>
          <a-col :span="12" v-if="dictDomain || categoryId">
            <h3>Dictionary Content</h3>
            <DictValue mode="view" :categoryId="dictDomain ? dictDomain.id : categoryId"></DictValue>
          </a-col>
        </a-row>
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
import DictDef from '@/views/d1/DictDef'
import DictValue from '@/views/d1/DictValue'
const baseConfig = {
  okText: 'Save',
  cancelText: 'Cancel',
  width: 1000,
  confirmLoading: false
}
export default {
  components: { DictDef, DictValue },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    title: {
      type: String,
      default: 'Choose Dictionary Category'
    },
    value: {
      type: Boolean,
      default: false
    },
    categoryId: {
      type: [Number, String],
      default: null
    }
  },
  data () {
    return {
      formRefName: Math.random()
        .toString()
        .slice(2),
      dictDomain: null
    }
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    }
  },
  methods: {
    handleSelectDomain (item) {
      this.dictDomain = item
    },
    handleOk () {
      if (!this.dictDomain) {
        return this.$message.info('Operation failed. More info: Pleace select category')
      }
      this.$emit('select', this.dictDomain.id, this.dictDomain.name)
      this.close()
    },
    clearConfig () {
      this.dictDomain = null
      this.$emit('select', null, null)
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
