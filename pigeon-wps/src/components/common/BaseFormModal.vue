<template>
  <div class="base-form" v-if="value">
    <a-modal
      :title="title"
      :ok-text="mergeConfig.okText"
      :cancel-text="mergeConfig.cancelText"
      :visible="value"
      :confirm-loading="loading"
      :width="mergeConfig.width"
      @ok="handleOk"
      @cancel="close"
    >
      <template>
        <base-form :ref="formRefName" v-bind="$attrs" v-model="formData" :item="item" :formItems="formItems"></base-form>
        <slot></slot>
      </template>
    </a-modal>
  </div>
</template>

<script>
import BaseForm from '@/components/common/BaseForm'
const baseConfig = {
  okText: 'Save',
  cancelText: 'Cancel',
  width: 700
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
      default: 'Create Item'
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
            label: 'Name',
            key: 'name'
          },
          {
            label: 'Description',
            key: 'description',
            type: 'textarea'
          }
        ]
      }
    },
    apiName: {
      type: String,
      default: ''
    },
    extraFields: {
      type: Array,
      default () {
        return []
      }
    },
    extraArgs: {
      type: Array,
      default () {
        return []
      }
    },
    extraData: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      formRefName: Math.random()
        .toString()
        .slice(2),
      formData: {},
      loading: false
    }
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    }
  },
  methods: {
    save () {
      const data = { ...this.formData, ...this.extraData }
      this.extraFields.forEach(field => {
        data[field] = this.item[field]
      })
      return this.$api[this.apiName](...this.extraArgs, data)
    },
    validate () {
      return this.$refs[this.formRefName].validate()
    },
    async handleOk () {
      const isValid = this.$refs[this.formRefName].validate()
      if (!isValid) return
      this.loading = true
      this.save()
        .then((result) => {
          this.$emit('saved', result)
          this.close()
        })
        .finally(() => {
          this.loading = false
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
