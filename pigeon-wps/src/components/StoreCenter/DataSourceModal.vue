<template>
  <div class="data-source-form">
    <a-modal
      :title="title"
      :ok-text="config.okText"
      :cancel-text="config.cancelText"
      :visible="value"
      :confirm-loading="config.confirmLoading"
      :width="config.width"
      :footer="type === 'view' ? null : undefined"
      @ok="handleOk"
      @cancel="close"
    >
      <template>
        <a-form-model
          :colon="false"
          :model="formData"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >

          <a-form-model-item
            label="Data Source Name"
            prop="name"
            required
          >
            <a-input
              v-model="formData.name"
              :disabled="type === 'view'"
              placeholder="name"
            />
          </a-form-model-item>

          <a-form-model-item
            label="Data Source Description"
            prop="description"
          >
            <a-textarea
              v-model="formData.description"
              :maxLength="200"
              :autoSize="{ minRows: 3, maxRows: 10 }"
              :disabled="type === 'view'"
            ></a-textarea>
          </a-form-model-item>

          <a-form-model-item
            label="Data Source Type"
            prop="ds_type_uid"
            :autoLink="false"
            required
          >
            <a-select
              v-model="formData.ds_type_uid"
              :disabled="type === 'view'"
              :options="dsTypeOptions"
              @change="handleDsTypeChange"
            ></a-select>
          </a-form-model-item>
        </a-form-model>
        <template v-if="selectedDsType && selectedDsType.connection_profile_required">
          <p>Data Source Connection Profile</p>
          <a-card size="small">
            <Processor
              v-if="processorVisiable"
              ref="processor"
              :disabled="type === 'view'"
              :uid="selectedDsType.processor_uid"
              :processorUid="selectedDsType.processor_uid"
              :dataProps="formData.processor_data_props"
              @changeData="handleDataChange"
            ></Processor>
          </a-card>
        </template>
      </template>
    </a-modal>
  </div>
</template>

<script>
import Processor from '@/components/StoreCenter/Processor'
export default {
  components: { Processor },
  props: {
    uid: {
      type: String,
      default: ''
    },
    value: {
      type: Boolean,
      default: false
    },
    parent: {
      type: Object,
      default () {
        return {}
      }
    },
    type: {
      type: String,
      default: 'create'
    }
  },
  data () {
    return {
      config: {
        okText: 'Save',
        cancelText: 'Cancel',
        width: 750,
        confirmLoading: false
      },
      labelCol: { span: 6 },
      wrapperCol: { span: 18 },
      formData: {
        name: '',
        description: '',
        ds_type_uid: undefined,
        parent_uid: '',
        processor_data_props: undefined
      },
      cacheDataPropsMap: {},
      dsTypeOptions: [],
      dsTypes: [],
      item: null,
      processorVisiable: true
    }
  },
  computed: {
    title () {
      const base = 'Data Source'
      const prefixMap = {
        create: 'Create ',
        edit: 'Edit ',
        view: 'View '
      }
      const prefix = prefixMap[this.type]
      return prefix + base
    },
    selectedDsType () {
      return this.dsTypes.find(x => x.uid === this.formData.ds_type_uid)
    }
  },
  watch: {
    uid: {
      immediate: true,
      handler (val) {
        val && this.loadItem()
      }
    },
    'formData.processor_data_props': {
      immediate: true,
      handler (val) {
        if (val) {
          this.cacheDataPropsMap[this.formData.ds_type_uid] = val
        }
      }
    }
  },
  async created () {
    if (!this.uid) {
      this.formData.parent_uid = this.parent.uid
    }
    this.$store.dispatch('GetDsTypes').then(dsTypes => {
      this.dsTypes = dsTypes
      this.dsTypeOptions = dsTypes.map(x => ({
        label: x.name,
        value: x.uid
      }))
      if (!this.formData.ds_type_uid && this.dsTypes.length) {
        this.formData.ds_type_uid = this.dsTypes[0].uid
      }
    })
  },
  methods: {
    refresh () {
      this.processorVisiable = false
      this.$nextTick(() => {
        this.processorVisiable = true
      })
    },
    handleDsTypeChange () {
      this.formData.processor_data_props = this.cacheDataPropsMap[this.formData.ds_type_uid] || undefined
      this.refresh()
    },
    async loadItem () {
      const resp = await this.$api.getDataSourceDetail(this.uid)
      this.item = resp.object
      this.initValue()
    },
    handleDataChange (data) {
      this.formData.processor_data_props = JSON.stringify(data)
    },
    async initValue () {
      this.formData.name = this.item.name
      this.formData.description = this.item.description
      this.formData.ds_type_uid = this.item.ds_type_uid
      this.formData.parent_uid = this.item.parent_uid
      this.formData.processor_data_props = this.item.processor_data_props
    },
    save () {
      if (this.type === 'edit') {
        return this.$api.updateDataSource(
          this.item.uid,
          this.formData.name,
          this.formData.description,
          this.formData.ds_type_uid,
          this.formData.parent_uid,
          this.formData.processor_data_props
        )
      } else {
        return this.$api.createDataSource(
          this.formData.name,
          this.formData.description,
          this.formData.ds_type_uid,
          this.formData.parent_uid,
          this.formData.processor_data_props
        )
      }
    },
    async handleOk () {
      if (!this.formData.name) {
        this.$message.warning('data source name is required')
        return
      }
      const validate = this.$refs.processor && this.$refs.processor.validate()
      if (validate === false) return
      this.config.confirmLoading = true
      this.save()
        .then(() => {
          this.$emit('saved')
          this.close()
        })
        .catch(err => {
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
