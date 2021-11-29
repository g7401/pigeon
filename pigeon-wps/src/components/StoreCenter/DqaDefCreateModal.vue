<template>
  <div class="data-source-form">
    <a-modal
      :title="title"
      :ok-text="config.okText"
      :cancel-text="config.cancelText"
      :visible="value"
      :confirm-loading="config.confirmLoading"
      :width="config.width"
      @ok="handleOk"
      @cancel="close"
    >
      <template>
        <a-form-model
          :model="formData"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-form-model-item
            label="Data Source"
            prop="ds"
            required
          >
            <DSTreeSelect
              :disabled="item && !!item.uid"
              v-model="formData.ds_uid"
            ></DSTreeSelect>
          </a-form-model-item>
          <a-form-model-item
            label="Name"
            prop="name"
            required
          >
            <a-input
              v-model="formData.name"
              placeholder="name"
            />
          </a-form-model-item>

          <a-form-model-item
            label="Description"
            prop="description"
          >
            <a-textarea
              :maxLength="200"
              :autoSize="{ minRows: 3, maxRows: 10 }"
              v-model="formData.description"
            ></a-textarea>
          </a-form-model-item>
        </a-form-model>
      </template>
    </a-modal>
  </div>
</template>

<script>
import DSTreeSelect from '@/components/StoreCenter/DSTreeSelect'
export default {
  components: { DSTreeSelect },
  props: {
    item: {
      type: Object,
      default () {
        return {}
      }
    },
    value: {
      type: Boolean,
      default: false
    },
    ds: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      config: {
        okText: 'Save',
        cancelText: 'Cancel',
        width: 700,
        confirmLoading: false
      },
      labelCol: { span: 6 },
      wrapperCol: { span: 18 },
      formData: {
        name: '',
        description: '',
        ds_uid: ''
      }
    }
  },
  computed: {
    title () {
      const base = 'DAQ Process Def'
      const prefix = this.item && this.item.uid ? 'Update ' : 'Create '
      return prefix + base
    }
  },
  async created () {
    console.log('this.item', this.item)
    if (this.item && this.item.uid) {
      this.formData.name = this.item.name
      this.formData.description = this.item.description
      this.formData.ds_uid = this.item.ds_uid
    }
  },
  methods: {
    save () {
      if (this.item && this.item.uid) {
        return this.$api.updateDaqProcessDef(this.item.uid, this.formData.name, this.formData.description)
      } else {
        return this.$api.createDaqProcessDef(this.formData.ds_uid, this.formData.name, this.formData.description)
      }
    },
    async handleOk () {
      // if (!this.formData.name || !/^[0-9a-zA-Z_]{1,}$/.test(this.formData.name)) {
      //   this.$message.warning('data source name must be a combination of letters,underline or numbers')
      //   return
      // }
      if (!this.formData.ds_uid) {
        this.$message.warning('Data Source is required')
        return
      }
      if (!this.formData.name) {
        this.$message.warning('DAQ Process Def name is required')
        return
      }
      this.config.confirmLoading = true
      this.save()
        .then(() => {
          // this.$message.success('Operation succeeded')
          this.$emit('saved')
          this.close()
        })
        .finally((err) => {
          console.log('err: ', err)
          // this.$message.error('Operation failed')
          this.config.confirmLoading = false
        })
    },
    close () {
      this.$emit('closed')
      this.$emit('input', false)
    }
  }
}
</script>

<style>
</style>
