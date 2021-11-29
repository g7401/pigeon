<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <a-row
      type="flex"
      justify="end"
    >
      <a-button
        type="primary"
        :loading="saveLoading"
        @click="saveScheduleSource"
        icon="save"
      >Save</a-button>
    </a-row>
    <a-icon v-if="configLoading" type="loading" />
    <BaseForm
      ref="baseForm"
      v-if="loaded"
      v-model="formData"
      :item="formItem"
      :config="scheduleFormConfig"
      :formItems="scheduleFormItems"
    ></BaseForm>
    <!-- <a-form-model
      :colon="false"
      :model="formData"
      :label-col="{ span: 4 }"
      :wrapper-col="{span: 12}"
      ref="scheduleForm"
    >
      <a-form-model-item
        label=" "
        prop="enabled"
      >
        <a-checkbox v-model="formData.enabled">Enabled</a-checkbox>
      </a-form-model-item>
      <a-form-model-item
        prop="schedule_type"
        label="Schedule Type"
      >
        <a-radio-group v-model="formData.schedule_type">
          <a-radio value="PERIODIC">
            PERIODIC
          </a-radio>
          <a-radio value="ADHOC">
            AD-HOC
          </a-radio>
        </a-radio-group>
      </a-form-model-item>
      <a-form-model-item
        label="Schedule Type Ext Details"
        prop="schedule_type_ext_details"
      >
        <a-input
          v-model="formData.schedule_type_ext_details"
          placeholder="cron expressions if PERIODIC"
        />
      </a-form-model-item>
    </a-form-model> -->
  </a-card>
</template>

<script>
import BaseForm from '@/components/common/BaseForm'
export default {
  components: { BaseForm },
  props: {
    epProcessDefUid: {
      type: String,
      default: undefined
    }
  },
  data () {
    return {
      scheduleFormItems: [
        {
          label: 'Enabled',
          key: 'enabled',
          type: 'checkbox'
        },
        {
          label: 'Schedule Type',
          key: 'schedule_type',
          type: 'radio-group',
          options: [
            {
              label: 'PERIODIC',
              value: 'PERIODIC'
            },
            {
              label: 'AD-HOC',
              value: 'ADHOC'
            }
          ]
        },
        {
          label: 'Schedule Type Ext Details',
          key: 'schedule_type_ext_details',
          placeholder: 'cron expression',
          required: true,
          show: form => form.schedule_type === 'PERIODIC'
        }
      ],
      scheduleFormConfig: { labelCol: { span: 4 }, wrapperCol: { span: 12 } },
      configLoading: false,
      formData: {},
      formItem: {
        enabled: true,
        schedule_type: 'PERIODIC',
        schedule_type_ext_details: undefined
      },
      saveLoading: false,
      loaded: false
    }
  },
  created () {
    this.getScheduleSource()
  },
  methods: {
    handleDsChange () {
      this.selectedAttrs = []
    },
    async getScheduleSource () {
      this.configLoading = true
      const resp = await this.$api.getEbSchedule(this.epProcessDefUid).finally(() => {
        this.configLoading = false
      })
      if (!resp) return
      if (resp.object) {
        Object.assign(this.formItem, resp.object)
      }
      this.loaded = true
    },
    async saveScheduleSource () {
      if (this.$refs.baseForm.validate() === false) return
      this.saveLoading = true
      await this.$api.saveEbSchedule(this.epProcessDefUid, this.formData).finally(() => {
        this.saveLoading = false
      })
    }
  }
}
</script>

<style lang="less" scoped>
.center-text {
  text-align: center;
  line-height: 32px;
}
</style>
