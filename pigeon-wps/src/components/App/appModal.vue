<template>
  <div v-if="value">
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
        <base-form
          :ref="formRefName"
          v-model="formData"
          :item="item"
          :formItems="formItems"
          :config="{ labelCol: { span: 5 }, wrapperCol: { span: 17 } }"
        ></base-form>
        <!-- <AppDetail
          v-if="formData.app_id"
          class="mg-t-20"
          :appId="formData.app_id"
          @change="apiQueryFields = $event"
        ></AppDetail> -->
      </template>
    </a-modal>
  </div>
</template>

<script>
import BaseForm from '@/components/common/BaseForm'
// import AppDetail from '@/components/StoreCenter/AppDetail'
const baseConfig = {
  okText: 'Save',
  cancelText: 'Cancel',
  width: 600,
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
      default: 'Create App'
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
    apiName: {
      type: String,
      default: 'createApp'
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
      formData: {},
      apiQueryFields: []
    }
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    },
    formItems () {
      return [
        {
          label: 'Enabled',
          key: 'enabled',
          type: 'checkbox',
          default: true
        },
        {
          label: 'App Name',
          key: 'app_name',
          required: true
        },
        {
          label: 'App Key',
          key: 'app_key',
          type: 'secret-input',
          required: true,
          disabled: this.apiName === 'updateApp'
        },
        {
          label: 'App Secret',
          key: 'app_secret',
          type: 'secret-input',
          required: true
        }
      ]
    }
  },
  created () {
  },
  methods: {
    async save () {
      const extraValues = this.extraFields.map(field => this.item[field])
      const requestData = { ...this.formData }
      if (this.item) {
        requestData['uid'] = this.item.uid
      }
      return this.$api[this.apiName](...extraValues, requestData)
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
