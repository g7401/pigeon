<template>
  <div class="base-form" v-if="value">
    <a-modal
      :title="title"
      :visible="value"
      :confirm-loading="loading"
      :width="1000"
      :footer="false"
      @cancel="close"
    >
      <template>
        <a-icon type="loading" v-if="loading" />
        <template v-if="operationDetail">
          <KeyValueInfo :obj="operationDetail" :options="options" :config="{ bordered: true }"></KeyValueInfo>
          <h3 class="mg-t-10">List of Tasks</h3>
          <template v-if="operationDetail && operationDetail.tasks">
            <div class="" :key="task.task" v-for="task in operationDetail.tasks">
              <div class="datetime">{{ task.create_timestamp }}</div>
              <MonacoEditor
                class="editor mg-b-10"
                :key="task.task"
                :value="task.format"
                language="json"
                theme="vs-dark"
                :options="{ automaticLayout: true }"
              />
            </div>
          </template>
        </template>
      </template>
    </a-modal>
  </div>
</template>

<script>
import KeyValueInfo from '@/components/common/KeyValueInfo'
const baseConfig = {
  width: 700
}
export default {
  components: { KeyValueInfo, MonacoEditor: () => import('vue-monaco') },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    title: {
      type: String,
      default: 'Operation Detail'
    },
    requestId: {
      type: [Number, String],
      default: null
    },
    value: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      loading: false,
      operationDetail: null,
      options: [{
        label: 'Request ID',
        key: 'request_id'
      }, {
        label: 'Request URI',
        key: 'request_uri'
      }, {
        label: 'Request Parameters',
        key: 'parameters'
      }]
    }
  },
  computed: {
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    }
  },
  watch: {
    value (val) {
      if (val) {
        this.getOperationDetail()
      }
    }
  },
  methods: {
    async getOperationDetail () {
      this.loading = true
      const result = await this.$api.getOperationDetail(this.requestId).finally(() => {
        this.loading = false
      })
      if (result.tasks) {
        result.tasks.forEach(task => {
          task.format = JSON.stringify({
            'task': task.task,
            'input': JSON.parse(task.input),
            'output': JSON.parse(task.output)
          }, null, 2)
        })
      }
      this.operationDetail = result
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .editor {
  height: 450px;
}
/deep/ .datetime {
  font-size: 15px;
  font-weight: 550;
}
</style>
