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
            <h3>Defaults Category</h3>
            <DefaultValCategoryDef @select="handleSelectDomain" :categoryId="categoryId" mode="select"></DefaultValCategoryDef>
          </a-col>
          <a-col :span="12" v-if="dictDomain || categoryId">
            <h3>Defaults Content</h3>
            <DefaultValContentList mode="select" :categoryId="dictDomain ? dictDomain.id : categoryId"></DefaultValContentList>
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
import DefaultValCategoryDef from '@/views/d1/DefaultValCategoryDef'
import DefaultValContentList from '@/views/d1/DefaultValContentList'
const baseConfig = {
  okText: 'Save',
  cancelText: 'Cancel',
  width: 900,
  confirmLoading: false
}
export default {
  components: { DefaultValCategoryDef, DefaultValContentList },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    title: {
      type: String,
      default: 'Choose Defaults Category'
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
