<template>
  <a-card size="small">
    <a-form-model
      ref="baseForm"
      :model="form"
      :colon="false"
      labelAlign="left"
      :labelCol="{ span: 5}"
      :wrapperCol="{span: 10}"
      :rules="rules"
    >
      <a-form-model-item
        label="DS Name"
        required
        prop="ds_name"
      >
        <a-input
          v-model="form.ds_name"
          placeholder="Globally unique"
        />
      </a-form-model-item>
      <a-form-model-item
        label="Select Data File Format"
        required
        prop="file_format"
      >
        <a-select
          :options="formatOptions"
          default-value="CSV"
          @change="handleFormatChange"
        ></a-select>
      </a-form-model-item>
    </a-form-model>
    <component
      ref="templateCom"
      :is="activeCom"
      type="page"
      @changeData="handleChangeData"
      @fileId="saveFileId"
    ></component>
    <a-row class="save-btn-row">
      <a-button
        @click="handleSave"
        type="primary"
        :loading="pageLoading"
      >Save Above Template and Show Data</a-button>
    </a-row>

    <a-row class="instance-info">
      <p>
        <template v-if="instanceId">{{ instanceId }}</template>
        <template v-if="instanceStatus">,
          <a-badge
            :status="badgeMap[instanceStatus]"
            :text="instanceStatus"
          /></template>
      </p>
    </a-row>
    <TaskInstanceOutputTable
      v-if="table"
      ref="ouputTable"
      :table="table"
      :taskInstanceId="taskInstanceId"
    ></TaskInstanceOutputTable>
  </a-card>
</template>

<script>
import ExtractCSVPropertiestemplate from '@/components/DIDefinitions/PropertiesTemplate/ExtractCSVPropertiestemplate'
import ExtractExcelPropertiestemplate from '@/components/DIDefinitions/PropertiesTemplate/ExtractExcelPropertiestemplate'
import TaskInstanceOutputTable from '@/components/DIDefinitions/TaskInstanceOutputTable'
import featureDatasetsMixin from './mixins/featureDatasetsMixin'
export default {
  name: 'UploadFeatureDatasets',
  mixins: [featureDatasetsMixin],
  components: {
    ExtractCSVPropertiestemplate,
    ExtractExcelPropertiestemplate,
    TaskInstanceOutputTable
  },
  props: {
    saveApiName: {
      type: String,
      default: 'initialUploadFeatureDatasets'
    }
  },
  data () {
    return {
      activeCom: 'ExtractCSVPropertiestemplate',
      formatComMap: {
        CSV: ExtractCSVPropertiestemplate,
        EXCEL: ExtractExcelPropertiestemplate
      },
      formatOptions: [
        { label: 'CSV', value: 'CSV' },
        { label: 'Excel', value: 'EXCEL' }
      ],
      form: {
        ds_name: '',
        file_format: 'CSV'
      },
      rules: {
        ds_name: [{ required: true }],
        file_format: [{ required: true }]
      },
      dataProperties: null,
      fileId: '',
      pageLoading: false,
      definitionId: '',
      lastRoute: null
    }
  },
  methods: {
    handleFormatChange (value) {
      this.form.file_format = value
      this.activeCom = this.formatComMap[value]
    },
    async getTaskInstance (instanceId) {
      const resp = await this.$api.getPublishDiTaskInstance(instanceId)
      if (resp.success && resp.object && resp.object.output_tables) {
        return { table: resp.object.output_tables[0], taskInstanceId: resp.object.basic.di_task_instance_id }
      }
    },
    async handleSave () {
      let isValid = false
      this.$refs.baseForm.validate(valid => {
        isValid = valid
      })
      if (this.$refs.templateCom.validate() === false || !isValid) return
      this.showLoading()
      this.instanceId = await this.getProcessorInstanceId()
      if (!this.instanceId) {
        this.hideLoading()
        return
      }
      this.$store.commit('savePage', {
        name: this.lastRouteName,
        state: {
          di_process_instance_id: this.instanceId
        }
      })
      const status = await this.checkInstanceStatus(this.instanceId)
      if (status === 'FAILED') {
        this.hideLoading()
        return
      }
      const { table, taskInstanceId } = await this.getTaskInstance(this.instanceId).catch(() => this.hideLoading())
      this.taskInstanceId = taskInstanceId
      this.table = table
      this.$store.commit('savePage', {
        name: this.lastRouteName,
        update: true,
        state: {
          table_name: this.table.table_name
        }
      })
      this.hideLoading()
    },
    async getProcessorInstanceId () {
      this.pageLoading = true
      const dataProperties = JSON.stringify(this.dataProperties)
      const resp = await this.$api[this.saveApiName](
        dataProperties,
        this.form.ds_name,
        this.form.file_format,
        this.fileId
      ).catch(() => {
        this.hideLoading()
        this.pageLoading = false
      })
      this.pageLoading = false
      if (!resp || !resp.object) return
      this.definitionId = resp.object.di_process_definition_id
      return resp.object.di_process_instance_id
    },
    saveFileId (fileId) {
      this.fileId = fileId
    },
    handleChangeData (data) {
      this.dataProperties = data
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .instance-info {
  height: 40px;
  margin-top: 20px;
}
/deep/ .save-btn-row {
  margin-top: 15px;
}
/deep/ .top-row {
  margin-bottom: 10px;
}
</style>
