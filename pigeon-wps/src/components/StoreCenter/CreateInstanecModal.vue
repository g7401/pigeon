<template>
  <div>
    <a-modal
      :title="config.title"
      :visible="value"
      :confirm-loading="config.confirmLoading"
      :width="config.width"
      :footer="null"
      @cancel="$emit('input', false)"
    >
      <div class="create-instance">
        <a-form-model
          size="small"
          :labelCol="{span: 4}"
          :wrapperCol="{span: 10}"
          labelAlign="left"
          :colon="false"
        >
          <!-- <a-form-model-item
            label="DS Group"
            prop="ds_group"
          >
            <a-select
              v-model="selectedGroup"
              :options="groupOptions"
              placeholder="ds group"
            >
            </a-select>
          </a-form-model-item> -->
          <a-form-model-item
            label="Data Source"
            prop="ds_name"
          >
            <DSTreeSelect v-model="selectedDsUid" @select="handleSelectDS"></DSTreeSelect>
          </a-form-model-item>
        </a-form-model>

        <template v-if="selectedDS">
          <div v-if="selectedDS.ds_type_name === 'File'">
            <p class="tip-text">The selected data source type is File Service, you can upload the data file you want to import.</p>
            <SUpload :config="{btnText: 'Upload Data File', beforeUpload: beforeUpload}" @input="afterUpload"></SUpload>
          </div>
          <div v-else-if="selectedDS.ds_type_name === 'MSSQL' || selectedDS.ds_type_name === 'MYSQL'">
            <p class="tip-text">The selected data source type is SQL Server, no need to manually instantiates.</p>
            <a-button @click="$emit('input', false)">Close</a-button>
          </div>
        </template>
      </div>
    </a-modal>
  </div>
</template>
<script>
import SUpload from '@/components/common/SUpload'
import DSTreeSelect from '@/components/StoreCenter/DSTreeSelect'

const baseConfig = {
  title: 'Create DAQ Process Instance',
  width: 800,
  confirmLoading: false
}
export default {
  components: { SUpload, DSTreeSelect },
  props: {
    value: {
      type: Boolean,
      default () {
        return false
      }
    },
    modalConfig: {
      type: Object,
      default () {
        return { ...baseConfig }
      }
    }
  },
  data () {
    return {
      config: {},
      selectedDS: null,
      selectedDsUid: null,
      createLoading: false
    }
  },
  async created () {
    this.config = { ...this.baseConfig, ...this.modalConfig }
  },
  methods: {
    handleSelectDS (node) {
      this.selectedDS = node
    },
    async afterUpload (fileId) {
      this.createProcessor(fileId)
    },
    async createProcessor (fileId) {
      if (!this.selectedDS) return
      this.createLoading = true
      const resp = await this.$api.createDaqProcessInst(this.selectedDS.uid, fileId).finally(() => {
        this.createLoading = false
      })
      if (!resp || !resp.object) return
      this.$message.success('Close the window after three seconds')
      this.$emit('done')
      setTimeout(() => {
        this.$emit('input', false)
      }, 3000)
    },
    beforeUpload () {
      return !!this.selectedDS
    }
  }
}
</script>

<style scoped>
.create-processor {
  min-height: 500px;
}
.create-instance {
  width: 650px;
  margin: 0 auto;
}
</style>
