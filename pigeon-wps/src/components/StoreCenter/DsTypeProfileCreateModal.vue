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
          labelAlign="left"
        >

          <a-form-model-item
            label="Connection Profile Name"
            prop="name"
            required
          >
            <a-input
              v-model="formData.name"
              placeholder="name"
            />
          </a-form-model-item>

          <!-- <a-form-model-item
            label="Connection Profile Description"
            prop="description"
          >
            <a-textarea
              v-model="formData.description"
              size="small"
            ></a-textarea>
          </a-form-model-item> -->
        </a-form-model>
        <a-card size="small" v-if="processorUid">
          <Processor ref="processor" :uid="processorUid" @changeData="handleDataChange"></Processor>
        </a-card>
      </template>
    </a-modal>
  </div>
</template>

<script>
import Processor from '@/components/StoreCenter/Processor'
export default {
  components: { Processor },
  props: {
    item: {
      type: Object,
      default () {
        return {}
      }
    },
    processorUid: {
      type: String,
      default: ''
    },
    value: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      config: {
        okText: 'Save',
        cancelText: 'Cancel',
        width: 800,
        confirmLoading: false
      },
      labelCol: { span: 6 },
      wrapperCol: { span: 18 },
      formData: {
        name: '',
        description: '',
        ds_type_uid: '',
        processor_data_props: ''
      },
      dsTypeOptions: []
    }
  },
  computed: {
    title () {
      return 'Create Connection Profile'
    }
  },
  async created () {
    console.log('this.item', this.item)
    this.formData.ds_type_uid = this.item.ds_type_uid
  },
  methods: {
    handleDataChange (data) {
      this.formData.processor_data_props = JSON.stringify(data)
    },
    save () {
      return this.$api.createDsTypeProfile(this.formData.name, this.formData.description, this.formData.ds_type_uid, this.formData.processor_data_props)
    },
    async handleOk () {
      // if (!this.formData.name || !/^[0-9a-zA-Z_]{1,}$/.test(this.formData.name)) {
      //   this.$message.warning('data source name must be a combination of letters,underline or numbers')
      //   return
      // }
      if (!this.formData.name || this.formData.name.length > 50) {
        this.$message.warning("Connection Profile name's length should not exceed 50 ")
        return
      }
      const validate = this.$refs.processor.validate()
      if (validate === false) return
      this.config.confirmLoading = true
      this.save()
        .then((resp) => {
          this.$emit('saved', resp.object)
          this.close()
        })
        .catch((err) => {
          console.log('err: ', err)
          this.config.confirmLoading = false
        })
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
