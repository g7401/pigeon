<template>
  <a-modal
    :title="modalTitle"
    :visible="value"
    :width="config.width"
    :footer="null"
    @cancel="handleCancel"
  >
    <div class="model-instance-detail">
      <KeyValueInfo
        :config="{bordered: true}"
        :options="modelOptions"
        :obj="modelInstance"
      ></KeyValueInfo>
      <div
        v-if="modelInstance.model_source === 'Trained'"
        class="detail"
      >
        <KeyValueInfo
          :config="{title: 'Model Training Information', bordered: true}"
          :options="trainOptions"
          :obj="modelInstance"
        ></KeyValueInfo>
      </div>
    </div>
  </a-modal>
</template>
<script>
import KeyValueInfo from '@/components/common/KeyValueInfo'

export default {
  components: {
    KeyValueInfo
  },
  props: {
    value: {
      type: Boolean,
      default () {
        return false
      }
    },
    modelInstance: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      config: {
        width: 900
      },
      modelOptions: [
        { label: 'Model Instance ID', key: 'model_instance_id' },
        { label: 'Deployed', key: 'is_deployed', type: 'bool' },
        { label: 'Deploy Time', key: 'deployed_time' },
        { label: 'Deploy By', key: 'deployed_by' },
        { label: 'Source', key: 'model_source' }
      ],
      trainOptions: [
        {
          key: 'model_training_process_instance_id',
          label: 'Model Training Process Instance ID'
        },
        {
          key: 'model_training_process_start_time',
          label: 'Model Training Process Instance Start Time'
        },
        {
          key: 'model_training_process_done_time',
          label: 'Model Training Process Instance Done Time'
        },
        {
          key: 'model_conf_filename',
          label: 'Model Configuration File Name'
        },
        {
          key: 'model_conf_file_size',
          label: 'Model Configuration File Size',
          type: 'fileSize'
        },
        {
          key: 'model_conf_file_id',
          label: 'Model Configuration File Link',
          isLink: true
        },
        {
          key: 'data_set_filename',
          label: 'Data Set File Name'
        },
        {
          key: 'data_set_file_size',
          label: 'Data Set File Size',
          type: 'fileSize'
        },
        {
          key: 'data_set_file_id',
          label: 'Data Set File Link',
          isLink: true
        },
        {
          key: 'model_instance_id',
          label: 'Model Instance Id'
        },
        {
          key: 'model_instance_filename',
          label: 'Model File Name'
        },
        {
          key: 'model_instance_file_size',
          label: 'Model File Size',
          type: 'fileSize'
        },
        {
          key: 'model_instance_file_id',
          label: 'Model File Link',
          isLink: true
        },
        {
          key: 'model_accuracy_metrics_filename',
          label: 'Model Accuracy Metrics File Name'
        },
        {
          key: 'model_accuracy_metrics_file_size',
          label: 'Model Accuracy Metrics File Size',
          type: 'fileSize'
        },
        {
          key: 'model_accuracy_metrics_file_id',
          label: 'Model Accuracy Metrics File Link',
          isLink: true
        }
      ]
    }
  },
  computed: {
    modalTitle () {
      const baseTitle = 'Model Instance Details'
      if (!this.modelInstance.model_instance_id) return baseTitle
      return `${baseTitle}(${this.modelInstance.model_instance_id})`
    }
  },
  methods: {
    handleCancel (e) {
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="less" scoped>
.model-instance-detail {
  min-height: 400px;
}
.detail {
  margin-top: 20px;
}
/deep/ .ant-descriptions-item-label {
  width: 310px;
}
</style>
