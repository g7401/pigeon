<template>
  <div>
    <a-modal
      :title="config.title"
      :ok-text="config.okText"
      :cancel-text="config.cancelText"
      :visible="value"
      :confirm-loading="config.confirmLoading"
      :width="config.width"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <div class="create-processor">
        <SUpload @input="afterUpload"></SUpload>
        <div
          v-if="uploadedProcessor"
          class="detail"
        >
          <KeyValueInfo
            :config="{title: 'Below is the details of uploaded processor'}"
            :options="detailOptions"
            :obj="uploadedProcessor"
          ></KeyValueInfo>
        </div>
      </div>
    </a-modal>
  </div>
</template>
<script>
import { parseProcessor, createProcessor } from '@/api/definition'
import KeyValueInfo from '@/components/common/KeyValueInfo'
import SUpload from '@/components/common/SUpload'

export default {
  components: {
    KeyValueInfo,
    SUpload
  },
  props: {
    type: {
      type: String,
      default () {
        return 'import'
      }
    },
    value: {
      type: Boolean,
      default () {
        return false
      }
    },
    modelConfig: {
      type: Object,
      default () {
        return this.baseConfig
      }
    },
    d1CoreBaseUrl: {
      type: String,
      default: ''
    },
    options: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      detailOptions: [
        { label: 'Name', key: 'name' },
        { label: 'Version', key: 'version' },
        { label: 'Owner', key: 'owner' },
        { label: 'Entry Class Name', key: 'entry_class_name' },
        { label: 'Program Package File Id', key: 'program_package_file_id' },
        { label: 'Program Package File Name', key: 'program_package_file_name' },
        { label: 'Program Package File Path', key: 'entry_class_name' },
        { label: 'Description', key: 'description' }
      ],
      uploadedProcessor: null,
      baseConfig: {
        title: 'Create Processor',
        okText: 'Commit',
        cancelText: 'Cancel',
        width: 800,
        confirmLoading: false,
        handleCancel: () => {},
        handleOk: () => {}
      },
      config: {},
      programPackageFileId: null,
      fileList: []
    }
  },
  created () {
    this.config = Object.assign(this.baseConfig, this.modelConfig)
  },
  methods: {
    async afterUpload (fileId) {
      this.programPackageFileId = fileId
      const resp = await parseProcessor(this.type.toUpperCase(), fileId)
      if (resp && resp.success && resp.object) {
        this.uploadedProcessor = resp.object
      }
    },
    async getCreateProcessor () {
      const resp = await createProcessor(this.type.toUpperCase(), this.programPackageFileId)
      if (!resp || !resp.object) return
      return resp.object
    },
    async handleOk (e) {
      this.$emit('input', false)
      const processor = await this.getCreateProcessor()
      this.$emit('selected', processor)
      this.config.handleOk()
    },
    handleCancel (e) {
      this.$emit('input', false)
      this.config.handleCancel()
    }
  }
}
</script>

<style scoped>
.create-processor {
  min-height: 500px;
}
.detail {
  margin-top: 20px;
}
</style>
