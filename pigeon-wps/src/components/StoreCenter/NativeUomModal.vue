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
        <base-form :ref="formRefName" v-model="formData" :item="item" :formItems="formItems"></base-form>
      </template>
    </a-modal>
  </div>
</template>

<script>
import BaseForm from '@/components/common/BaseForm'
const baseConfig = {
  okText: 'Save',
  cancelText: 'Cancel',
  width: 700,
  confirmLoading: false
}
export default {
  components: { BaseForm },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    title: {
      type: String,
      default: 'Edit Native UOM'
    },
    item: {
      type: Object,
      default () {
        return null
      }
    },
    value: {
      type: Boolean,
      default: false
    },
    formItems: {
      type: Array,
      default () {
        return [
          {
            label: 'Material Code',
            key: 'material_code',
            type: 'text'
          },
          {
            label: 'UOM Native Name',
            key: 'uom_native_name',
            type: 'text'
          },
          {
            label: 'UOM Localized Name',
            key: 'uom_localized_name',
            required: true
          }
        ]
      }
    },
    apiName: {
      type: String,
      default: 'updateNativeUom'
    },
    extraFields: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      formRefName: Math.random()
        .toString()
        .slice(2),
      formData: {}
    }
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    }
  },
  methods: {
    save () {
      const extraValues = this.extraFields.map(field => this.item[field])
      return this.$api[this.apiName](...extraValues, this.formData)
    },
    async handleOk () {
      const isValid = this.$refs[this.formRefName].validate()
      if (!isValid) return
      this.config.confirmLoading = true
      this.save()
        .then(() => {
          this.$emit('saved')
          this.close()
        })
        .finally(() => {
          this.config.confirmLoading = false
        })
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
      this.formData = {}
    }
  }
}
</script>

<style>
</style>
