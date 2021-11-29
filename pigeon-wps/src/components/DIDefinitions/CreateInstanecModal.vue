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
          <a-form-model-item
            label="DS Group"
            prop="ds_group"
          >
            <a-select
              v-model="selectedGroup"
              :options="groupOptions"
              placeholder="ds group"
            >
            </a-select>
          </a-form-model-item>
          <a-form-model-item
            label="DS Name"
            prop="ds_name"
          >
            <a-select
              v-model="selectedDSname"
              :options="nameOptions"
              placeholder="ds name"
            >
            </a-select>
          </a-form-model-item>
        </a-form-model>

        <template v-if="selectedDefinition">
          <div v-if="selectedDefinition.startup_type === 'MANUAL_UPLOAD'">
            <p class="tip-text">The selected data source has configured MANUAL_UPLOAD, you can upload the data file you want to import.</p>
            <SUpload :config="{btnText: 'Upload Data File', beforeUpload: beforeUpload}" @input="afterUpload"></SUpload>
          </div>
          <div v-else-if="selectedDefinition.startup_type === 'MANUAL_TRIGGER'">
            <p class="tip-text">The selected data source has configured MANUAL_TRIGGER, you can upload the data file you want to import.</p>
            <a-button
              type="primary"
              :loading="createLoading"
              @click="createProcessor()"
            >Create DI Process Instance</a-button>
          </div>
          <div v-else-if="selectedDefinition.startup_type">
            <p class="tip-text">The selected data source has configured {{ selectedDefinition.startup_type }}, no need to manually instantiates.</p>
            <a-button @click="$emit('input', false)">Close</a-button>
          </div>
        </template>
      </div>
    </a-modal>
  </div>
</template>
<script>
import SUpload from '@/components/common/SUpload'

const baseConfig = {
  title: 'Create DI Process Instance',
  width: 800,
  confirmLoading: false
}
export default {
  components: { SUpload },
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
      groupOptions: [],
      selectedDSname: null,
      selectedGroup: null,
      createLoading: false
    }
  },
  async created () {
    this.config = { ...this.baseConfig, ...this.modalConfig }
    this.loadGroups()
  },
  computed: {
    selectedDefinition () {
      if (!this.selectedGroup || !this.selectedDSname) return null
      const group = this.groups.find(x => x.ds_group === this.selectedGroup)
      if (!group) return null
      return group.di_process_definitions.find(item => item.ds_name === this.selectedDSname)
    },
    nameOptions () {
      if (!this.selectedGroup) return []
      const group = this.groups.find(x => x.ds_group === this.selectedGroup)
      return this.ArrayToOptions(group.di_process_definitions.map(x => x.ds_name))
    }
  },
  methods: {
    ArrayToOptions (arr) {
      return arr.map(v => ({ label: v, value: v }))
    },
    async loadGroups () {
      const resp = await this.$api.getDsgroupDetails()
      if (resp.success && resp.object) {
        this.groups = resp.object.ds_group_details
        this.groupOptions = this.ArrayToOptions(resp.object.ds_group_details.map(x => x.ds_group).filter(i => i))
      }
    },
    async afterUpload (fileId) {
      this.createProcessor(fileId)
    },
    async createProcessor (fileId) {
      if (!this.selectedDefinition) return
      this.createLoading = true
      const resp = await this.$api.createProcessInstance(this.selectedDefinition.di_process_definition_id, fileId)
      this.createLoading = false
      if (!resp || !resp.object) return
      this.$message.success('Close the window after three seconds')
      this.$emit('done')
      setTimeout(() => {
        this.$emit('input', false)
      }, 3000)
    },
    beforeUpload () {
      return !!this.selectedDefinition && !this.uploadLoading
    }
  }
}
</script>

<style scoped>
.create-processor {
  min-height: 500px;
}
.create-instance {
  width: 500px;
  margin: 0 auto;
}
</style>
