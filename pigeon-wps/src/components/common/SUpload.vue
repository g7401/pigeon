<template>
  <a-upload
    :multiple="mainConfig.multiple"
    :action="mainConfig.uploadApi"
    :before-upload="mainConfig.beforeUpload"
    :fileList="fileList"
    :remove="()=> {$emit('input', ''); return true}"
    @change="handleUpload"
  >
    <slot>
      <a-button
        type="primary"
        icon="upload"
        :loading="uploadLoading"
        :disabled="disabled"
      >
        {{ mainConfig.btnText }}</a-button>
    </slot>
  </a-upload>
</template>

<script>
import api from '@/api/d1'
const baseConfig = {
  btnText: 'Upload',
  uploadApi: api.upload,
  multiple: false,
  beforeUpload: () => {}
}
export default {
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    value: {
      type: String,
      default: ''
    },
    config: {
      type: Object,
      default () {
        return { ...baseConfig }
      }
    }
  },
  data () {
    return {
      fileList: [],
      uploadLoading: false,
      mainConfig: {}
    }
  },
  created () {
    this.mainConfig = { ...baseConfig, ...this.config }
  },
  methods: {
    async handleUpload ({ file, fileList }) {
      if (!this.mainConfig.multiple) this.fileList = fileList.slice(-1)
      if (file.status === 'uploading') {
        this.uploadLoading = true
      } else if (file.status === 'done') {
        this.uploadLoading = false
        this.$message.success(`Upload Operation succeeded`)
        if (!file.response.success || !file.response.object) {
          return
        }
        const fileId = file.response.object
        this.$emit('input', fileId)
        this.$emit('uploading', { ...file, deployFileId: fileId }, fileList)
      } else if (file.status === 'error') {
        this.uploadLoading = false
        this.$message.error(`Operation failed. More info: ${file.name} file upload failed.`)
      }
    }
  }
}
</script>

<style>
</style>
