<template>
  <div class="di-process-informaion">
    <CreateProcessorModel
      v-model="showCreateProcessor"
      :type="type"
      @selected="handleCreateProcessor"
    ></CreateProcessorModel>
    <D1VueComponentModal
      v-model="showChooseModel"
      :model-config="modelConfig"
      :options="options"
      @selected="handleSelected"
      @selecting="handleSelecting"
    >
      <div
        v-if="processor"
        class="processor-detail"
      >
        <KeyValueInfo
          v-if="processor"
          :config="{title: detailTitle, bordered: true}"
          :options="detailOptions"
          :obj="processor"
        ></KeyValueInfo>
      </div>
    </D1VueComponentModal>
    <a-row
      v-if="mode !== 'view'"
      type="flex"
      class="button-box"
      justify="space-between"
    >
      <a-button
        type="primary"
        @click="handleShowChooseModel"
      >Choose an {{ firstUpperType }} Processer</a-button>
      <a-button
        type="primary"
        :loading="saveTaskLoading"
        @click="saveTask"
      >Save</a-button>
    </a-row>
    <a-row
      v-if="mode !== 'view'"
      type="flex"
    >
      <p class="tip-text ant-form-item-required">
        If cannot find a usable {{ type }} processor, you can create a custom {{ type }} processor.
      </p>
      <a-button
        class="text-btn"
        type="primary"
        size="small"
        @click="handleShowCreateProcessor"
      >Create a Custom {{ firstUpperType }} Processor</a-button>
    </a-row>
    <a-row
      v-if="mode !== 'view'"
      type="flex"
    >
      <p class="tip-text ant-form-item-required">
        Here is an example of a custom {{ type }} processor that you can download for reference.
      </p>
      <a-button
        class="text-btn"
        :loading="downloadLoading"
        @click="handleDownload"
        size="small"
      >Download</a-button>
    </a-row>
    <Processor
      ref="processor"
      v-if="selectedProcessor"
      :key="selectedProcessor.key"
      :processor="selectedProcessor"
      @changeData="updateDataProperties"
    ></Processor>
  </div>
</template>

<script>
/* eslint-disable */
import { firstLetterUpper, isObj } from '@/components/_util/util'
import {
  getProcesssorDetail,
  getDiTaskDefinition,
  saveDiTaskDefinition,
  updateDiTaskDefinition,
  downloadFile,
  download
} from '@/api/definition'
import D1Api from '@/api/d1'
import D1VueComponentModal from '@/components/d1/D1VueComponentModal'
import KeyValueInfo from '@/components/common/KeyValueInfo'
import CreateProcessorModel from '@/components/DIDefinitions/CreateProcessorModel'
import Processor from '@/components/DIDefinitions/Processor'

const typeMap = {
  import: 0,
  extract: 1,
  transform: 2,
  publish: 3
}

export default {
  components: { D1VueComponentModal, KeyValueInfo, CreateProcessorModel, Processor },
  props: {
    type: {
      type: String,
      default() {
        return 'import'
      }
    },
    diProcessDefinitionId: {
      type: String,
      default() {
        return ''
      }
    }
  },
  data() {
    return {
      mode: 'create',
      showChooseModel: false,
      showCreateProcessor: false,
      modelConfig: {},
      processor: null,
      selectedProcessor: null,
      taskId: null,
      options: {
        dataFacetKey: 'di_processor_dfk',
        preFilters: {
          di_task_type: typeMap[this.type]
        },
        tableSize: 'small',
        formSize: 'small',
        serialNumber: true,
        openform: true,
        showHlightCurrentRow: true
      },
      overviewOptions: [
        { label: 'Name', key: 'name' },
        { label: 'Version', key: 'version' },
        { label: 'Owner', key: 'owner' },
        { label: 'Entry Class Name', key: 'entry_class_name' }
      ],
      detailOptions: [
        { label: 'Name', key: 'name' },
        { label: 'Version', key: 'version' },
        { label: 'Owner', key: 'owner' },
        { label: 'Entry Class Name', key: 'entry_class_name' },
        { label: 'Program Package File Id', key: 'program_package_file_id' },
        { label: 'Program Package File Name', key: 'program_package_file_name' },
        { label: 'Program Package File Path', key: 'program_package_file_path' },
        { label: 'Description', key: 'description' }
      ],
      downloadLoading: false,
      saveTaskLoading: false
    }
  },
  created() {
    this.modelConfig.title = `Choose ${this.firstUpperType} Processor`
    if (this.$route.query.definition_id) {
      this.mode = this.$route.query.mode || 'edit'
      this.loadTask()
    }
  },
  computed: {
    firstUpperType() {
      return firstLetterUpper(this.type)
    },
    detailTitle() {
      if (!this.processor) return ''
      return `Below is the details of selected processor (${this.processor.name})`
    }
  },
  methods: {
    updateDataProperties(data) {
      this.selectedProcessor.data_properties = data
    },
    async loadTask() {
      const data = {
        di_process_definition_id: this.diProcessDefinitionId,
        di_task_type: this.type.toUpperCase()
      }
      const resp = await getDiTaskDefinition(data)
      if (resp.success && resp.object) {
        this.selectedProcessor = resp.object[0].di_processor
        this.taskId = resp.object[0].di_task_definition_id
      }
    },
    saveOrUpdateTask(data) {
      return this.taskId ? updateDiTaskDefinition(data) : saveDiTaskDefinition(data)
    },
    async saveTask() {
      const validate = this.$refs.processor.validate()
      if (validate === false) return
      if (!this.diProcessDefinitionId) {
        return this.$message.warning('Operation tips. more info: Please create di process definition')
      }
      if (!this.selectedProcessor) {
        return this.$message.warning('Operation tips. more info: Please choose di processor or create di processor')
      }
      const data = {
        di_process_definition_id: this.diProcessDefinitionId,
        di_processor_id: this.selectedProcessor.di_processor_id,
        di_task_type: this.type.toUpperCase(),
        data_properties: isObj(this.selectedProcessor.data_properties)
          ? JSON.stringify(this.selectedProcessor.data_properties)
          : this.selectedProcessor.data_properties,
        sequence: 0
      }
      if (this.taskId) {
        data['di_task_definition_id'] = this.taskId
      }
      this.saveTaskLoading = true
      const resp = await this.saveOrUpdateTask(data).catch(() => (this.saveTaskLoading = false))
      this.saveTaskLoading = false
      if (resp.success) {
        this.taskId = resp.object.di_task_definition_id
      }
    },
    async loadDetail(id) {
      const resp = await getProcesssorDetail(id)
      if (!resp || !resp.object) return
      this.processor = resp.object
    },
    handleSelected() {
      this.processor.key = Math.random().toString()
      this.selectedProcessor = this.processor
    },
    async handleSelecting(processor) {
      this.processor = processor
      processor && (await this.loadDetail(processor.id))
    },
    handleShowChooseModel() {
      this.showChooseModel = true
    },
    handleShowCreateProcessor() {
      this.showCreateProcessor = true
    },
    handleCreateProcessor(processor) {
      this.selectedProcessor = processor
    },
    async handleDownload() {
      this.downloadLoading = true
      const resp = await this.$api.getDemoPlugin().catch(() => (this.downloadLoading = false))
      if (!resp || !resp.success) return
      this.downloadLoading = false
      const file_id = resp.object
      window.location.href = `${D1Api.download}?file_id=${file_id}`
    }
  }
}
</script>

<style lang="less" scoped>
.title {
  margin-bottom: 20px;
}
.detail-title {
  font-size: 18px;
}
.processor-detail {
  margin: 0 auto;
}
.content-box {
  margin-bottom: 30px;
}
/deep/ .ant-form-item {
  margin-bottom: 0;
}
.button-box {
  margin-bottom: 10px;
}
.text-btn {
  margin-left: 10px;
}
</style>
