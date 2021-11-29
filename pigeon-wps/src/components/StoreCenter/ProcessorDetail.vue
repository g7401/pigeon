<template>
  <div class="di-process-informaion">
    <CreateProcessorModel
      v-model="showCreateProcessor"
      :type="type"
      @selected="handleCreateProcessor"
    ></CreateProcessorModel>
    <ChooseProcessModal
      v-if="showChooseModel"
      v-model="showChooseModel"
      :model-config="modelConfig"
      :type="type"
      @selected="handleSelected"
      @selecting="handleSelecting"
    >
    </ChooseProcessModal>
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
        v-if="hasSave"
        type="primary"
        :loading="saveTaskLoading"
        @click="saveTask"
        icon="save"
      >Save</a-button>
    </a-row>
    <a-row
      class="mg-b-10"
      v-if="mode !== 'view'"
      type="flex"
    >
      <span class="tip-text mg-r-10">
        If cannot find a appropriate {{ type }} processor, you can create a custom {{ type }} processor.
      </span>
      <a-button
        type="primary"
        size="small"
        @click="handleShowCreateProcessor"
      >Create a Custom {{ firstUpperType }} Processor</a-button>
    </a-row>
    <a-row
      class="mg-b-10"
      v-if="mode !== 'view'"
      type="flex"
    >
      <span class="tip-text mg-r-10">
        Here is an example of a custom {{ type }} processor that you can download for reference.
      </span>
      <a-button
        :loading="downloadLoading"
        disabled
        @click="handleDownload"
        size="small"
      >Download</a-button>
    </a-row>
    <div>
      <div class="overview content-box">
        <h3 class="detail-title">· Overview</h3>
        <div class="indent">
          <KeyValueInfo
            v-if="selectedProcessor"
            key="processor"
            :config="{bordered: true}"
            :options="overviewOptions"
            :obj="selectedProcessor"
          ></KeyValueInfo>
          <slot name="overview"></slot>
        </div>
      </div>
      <div class="description content-box">
        <h3 class="detail-title">· Program</h3>
        <div class="indent">
          <KeyValueInfo
            v-if="selectedProcessor"
            key="processor"
            :config="{bordered: true}"
            :options="programOptions"
            :obj="selectedProcessor"
          ></KeyValueInfo>
          <slot name="description"></slot>
        </div>
      </div>
      <div class="properties content-box">
        <h3 class="detail-title">· Properties</h3>
        <div class="indent">
          <Processor
            ref="processor"
            v-if="selectedProcessor"
            :key="selectedProcessor.key"
            :pageProps="selectedProcessor.page_props"
            :dataProps="dataProps"
            v-bind="$attrs"
            @changeData="updateDataProperties"
          ></Processor>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import { firstLetterUpper } from '@/components/_util/util'
import KeyValueInfo from '@/components/common/KeyValueInfo'
import CreateProcessorModel from '@/components/StoreCenter/CreateProcessorModel'
import Processor from '@/components/StoreCenter/Processor'
import ChooseProcessModal from '@/components/StoreCenter/ChooseProcessModal'

export default {
  components: { ChooseProcessModal, KeyValueInfo, CreateProcessorModel, Processor },
  props: {
    type: {
      type: String,
      default: 'extract'
    },
    taskUid: {
      type: String,
      default: ''
    },
    initDataProps: {
      type: String,
      default: ''
    },
    processorUid: {
      type: String,
      default: ''
    },
    createApi: {
      type: String,
      default: 'createDapTaskConfig'
    },
    updateApi: {
      type: String,
      default: 'updateDapTaskConfig'
    },
    hasSave: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      mode: 'create',
      showChooseModel: false,
      showCreateProcessor: false,
      modelConfig: {},
      processor: null,
      selectedProcessor: null,
      dataProps: null,
      taskId: null,
      overviewOptions: [
        { label: 'UID', key: 'uid' },
        { label: 'Name', key: 'name' },
        { label: 'Description', key: 'description' }
      ],
      programOptions: [
        { label: 'Is Plugin', key: 'plugin', type: 'bool' },
        { label: 'Entry Class Name', key: 'entry_class_name' },
        { label: 'Program File Id', key: 'program_package_file_id' },
        { label: 'Program File Path', key: 'program_package_file_path', type: 'link' }
      ],
      downloadLoading: false,
      saveTaskLoading: false
    }
  },
  created () {
    this.modelConfig.title = `Choose ${this.firstUpperType} Processor`
    if (this.type === 'transform') {
      Vue.component('MonacoEditor', () => import('vue-monaco'))
    }
    if (this.processorUid) {
      this.loadProcessorDetail(this.processorUid)
    }
    if (this.initDataProps) {
      this.mode = 'update'
      this.dataProps = JSON.parse(this.initDataProps)
    }
  },
  watch: {
    selectedProcessor (value) {
      value && this.$emit('processor:update', value)
    }
  },
  computed: {
    firstUpperType () {
      return firstLetterUpper(this.type)
    },
    detailTitle () {
      if (!this.processor) return ''
      return `Below is the details of selected processor (${this.processor.name})`
    }
  },
  methods: {
    async loadProcessorDetail (processorUid) {
      const resp = await this.$api.getProcessorDetail(processorUid)
      if (resp && resp.object) {
        this.selectedProcessor = resp.object
        console.log('resp.object', resp.object)
      }
    },
    updateDataProperties (data) {
      this.dataProps = data
      this.$emit('changeData', data)
    },
    validate () {
      return this.$refs.processor.validate()
    },
    async saveTask () {
      if (!this.selectedProcessor) {
        return this.$message.warning('Operation tips. more info: Please choose processor or create processor')
      }
      if (!this.$refs.processor) return
      const validate = this.validate()
      if (validate === false) return
      console.log('this.dataProps', this.dataProps)
      this.saveTaskLoading = true
      const apiName = this.mode === 'update' ? this.updateApi : this.createApi
      const resp = await this.$api[apiName](
        this.taskUid,
        this.selectedProcessor.uid,
        JSON.stringify(this.dataProps),
        this.selectedProcessor.default_data_props
      ).finally(() => (this.saveTaskLoading = false))
      if (resp.success) {
        this.mode = 'update'
        this.$emit('saved', this.taskUid)
      }
    },
    handleSelected () {
      this.processor.key = Math.random().toString()
      this.selectedProcessor = this.processor
    },
    async handleSelecting (processor) {
      this.processor = processor
    },
    handleShowChooseModel () {
      this.showChooseModel = true
    },
    handleShowCreateProcessor () {
      this.showCreateProcessor = true
    },
    handleCreateProcessor (processor) {
      this.selectedProcessor = processor
    },
    async handleDownload () {
      // this.downloadLoading = true
      // const resp = await this.$api.getDemoPlugin().finally(() => (this.downloadLoading = false))
      // if (!resp || !resp.success) return
      // window.location.href = `${this.$api.config.download}?file_id=${resp.object}`
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
.content-box {
  margin-bottom: 30px;
}
.indent {
  padding-left: 10px;
}
/deep/ .ant-descriptions-item-label {
  width: 30%;
}
</style>
