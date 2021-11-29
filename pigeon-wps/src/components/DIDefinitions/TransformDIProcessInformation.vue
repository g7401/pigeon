<template>
  <div class="di-process-informaion">
    <CreateProcessorModel
      v-model="showCreateProcessor"
      :type="type"
      @selected="handleCreateProcessor"
    ></CreateProcessorModel>
    <D1VueComponentModal
      v-model="showChooseModel"
      :model-config="modalConfig"
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

    <div>
      <draggable
        class="steps-box ant-steps ant-steps-horizontal ant-steps-label-horizontal ant-steps-navigation"
        v-if="processores.length"
        v-model="processores"
      >
        <div
          title="draggable to sort"
          class="ant-steps-item ant-steps-item-process ant-steps-item-active"
          v-for="(item, index) in processores"
          :key="item.di_task_definition_id"
        >
          <div class="ant-steps-item-container">
            <div class="ant-steps-item-tail"></div>
            <div class="ant-steps-item-icon">
              <span class="ant-steps-icon">{{ index + 1 }}</span>
            </div>
            <div class="ant-steps-item-content">
              <div class="ant-steps-item-title">{{ item.name }}
                <a-icon
                  v-if="mode !== 'view'"
                  title="delete current processor"
                  type="close-circle"
                  @click="removeProcessor(index)"
                />
              </div>
            </div>
          </div>
        </div>
      </draggable>
    </div>

    <a-row
      v-if="mode !== 'view'"
      type="flex"
      justify="space-between"
      class="button-box"
    >
      <a-button
        type="primary"
        @click="handleShowChooseModel"
      >Choose an {{ firstUpperType }} Processer</a-button>
      <a-button
        :loading="saveTaskLoading"
        type="primary"
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
    <a-tabs
      v-if="processores.length"
      type="card"
      :hideAdd="true"
      v-model="activeTab"
      @edit="onEditTabs"
    >
      <a-tab-pane
        v-for="(processorObj, index) in processores"
        type="card"
        :key="index + ''"
        :tab="index + 1 + '. ' + processorObj.name"
      >
        <Processor
          :ref="'processor' + index"
          v-if="activeTab == index "
          :key="processorObj.key || processorObj.di_task_definition_id || processorObj.di_processor_id"
          :processor="processorObj"
          @changeData="handleChangeData($event, index)"
        ></Processor>
      </a-tab-pane>
    </a-tabs>

  </div>
</template>

<script>
/* eslint-disable */
import Vue from 'vue'
import draggable from 'vuedraggable'
import { firstLetterUpper, isObj } from '@/components/_util/util'
import {
  getProcesssorDetail,
  getDiTaskDefinition,
  saveDiTaskDefinitionBatch,
  updateDiTaskDefinition
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
  components: { D1VueComponentModal, KeyValueInfo, CreateProcessorModel, Processor, draggable },
  props: {
    type: {
      type: String,
      default() {
        return 'transform'
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
      modalConfig: {},
      activeTab: '0',
      processores: [],
      processor: null,
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
    this.modalConfig.title = `Choose ${this.firstUpperType} Processor`
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
    stepsComponentData() {
      return {
        props: {
          current: this.processores.length - 1,
          type: 'navigation'
        },
        on: {},
        attrs: {
          wrap: true
        }
      }
    },
    onEditTabs(targetKey, action) {
      if (action === 'remove') {
        this.removeProcessor(targetKey)
      }
    },
    resetActiveTab() {
      this.activeTab = this.processores.length - 1 + ''
    },
    removeProcessor(index) {
      this.processores.splice(index, 1)
      this.resetActiveTab()
    },
    handleChangeData(data, index) {
      this.processores[index].data_properties = data
    },
    async loadTask() {
      const data = {
        di_process_definition_id: this.diProcessDefinitionId,
        di_task_type: this.type.toUpperCase()
      }
      const resp = await getDiTaskDefinition(data)
      if (resp.success && resp.object) {
        this.processores = resp.object.map(task => ({
          di_task_definition_id: task.di_task_definition_id,
          ...task.di_processor
        }))
      }
    },
    isValid() {
      let valid = true
      const falseValidLenth = this.processores.filter((item, index) => {
        if (!this.$refs[`processor${index}`] || !this.$refs[`processor${index}`].length) return false
        return this.$refs[`processor${index}`][0].validate() === false
      }).length
      if (falseValidLenth) valid = false
      return valid
    },
    async saveTask() {
      if (this.isValid() === false) return
      if (!this.diProcessDefinitionId) {
        return this.$message.warning('Operation tips. more info: Please create di process definition first')
      }
      if (!this.processores.length) {
        return this.$message.warning('Operation tips. more info: Please choose a processor or create a processor first')
      }
      const units = this.processores.map((processor, index) => ({
        di_processor_id: processor.di_processor_id,
        data_properties: isObj(processor.data_properties)
          ? JSON.stringify(processor.data_properties)
          : processor.data_properties,
        sequence: index,
        di_task_definition_id: processor.di_task_definition_id
      }))
      const data = {
        di_process_definition_id: this.diProcessDefinitionId,
        di_task_type: this.type.toUpperCase(),
        units
      }
      this.saveTaskLoading = true
      const resp = await saveDiTaskDefinitionBatch(data).catch(() => (this.saveTaskLoading = false))
      if (resp && resp.success && resp.object) {
        this.loadTask()
      }
      this.saveTaskLoading = false
    },
    async loadDetail(id) {
      const resp = await getProcesssorDetail(id)
      if (!resp || !resp.object) return
      this.processor = resp.object
    },
    handleSelected() {
      this.processor.key = Math.random().toString()
      this.processores.push(this.processor)
      this.resetActiveTab()
    },
    async handleSelecting(processor) {
      await this.loadDetail(processor.id)
    },
    handleShowChooseModel() {
      this.showChooseModel = true
    },
    handleShowCreateProcessor() {
      this.showCreateProcessor = true
    },
    handleCreateProcessor(processor) {
      this.processores.push(processor)
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

/deep/ .ant-form-item {
  margin-bottom: 0;
}
.button-box {
  margin-bottom: 10px;
}

.steps-box {
  margin-bottom: 30px;
}
.ant-steps-navigation .ant-steps-item:before {
  background: transparent;
}
.ant-steps-item {
  cursor: pointer;
}
/deep/ .anticon-close-circle {
  visibility: hidden;
  color: rgb(245, 34, 45);
  font-size: 20px;
  position: relative;
  top: -4px;
}
.ant-steps-item:hover .anticon-close-circle {
  visibility: visible;
}
.text-btn {
  margin-left: 10px;
}
</style>
