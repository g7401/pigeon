<template>
  <div class="data-source-form">
    <a-modal
      :title="config.title"
      :ok-text="config.okText"
      :cancel-text="config.cancelText"
      :visible="value"
      :confirm-loading="config.confirmLoading"
      :width="config.width"
      @ok="handleOk"
      @cancel="close"
    >
      <div>
        <a-form-model
          ref="dictForm"
          :rules="rules"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          :model="formData"
        >
          <a-form-model-item
            label="Type"
            prop="type"
          >
            <a-select v-model="formData.type">
              <a-select-option value="SQL">SQL</a-select-option>
            </a-select>
          </a-form-model-item>
          <a-form-model-item
            label="JDBC URL"
            prop="param_jdbc_url"
          >
            <a-input
              v-model="formData.param_jdbc_url"
              placeholder="jdbc:mysql://localhost:3306/d1_core?useSSL=false"
            />
          </a-form-model-item>
          <a-form-model-item
            label="Username"
            prop="param_username"
          >
            <a-input v-model="formData.param_username" />
          </a-form-model-item>
          <a-form-model-item
            label="Password"
            prop="param_password"
          >
            <a-input-password v-model="formData.param_password" />
          </a-form-model-item>
          <a-form-model-item
            label="SQL"
            prop="param_sql"
          >
            <a-textarea
              v-model="formData.param_sql"
              placeholder="select id as value,id as sequence, model_name as label from model limit 10"
            />
          </a-form-model-item>
          <a-form-model-item
            label="Enable"
            prop="enable"
          >
            <a-switch v-model="formData.enable" />
          </a-form-model-item>
          <a-form-model-item
            label="Cron"
            prop="cron"
          >
            <a-input
              v-model="formData.cron"
              placeholder="Seconds Minutes Hours DayofMonth Month DayofWeek Year"
            />
          </a-form-model-item>
          <a-form-model-item
            :colon="false"
            label=" "
          >
            <a-button
              type="primary"
              ghost
              :loading="testLoading"
              @click="test"
            >Test Result</a-button>
          </a-form-model-item>
        </a-form-model>
        <template v-if="dictStrategyTableData.length">
          <p>Top 5</p>
          <a-table
            bordered
            class="more-small-table"
            size="small"
            :dataSource="dictStrategyTableData"
            :columns="columns"
          ></a-table>
        </template>

      </div>
    </a-modal>
  </div>
</template>

<script>
export default {
  props: {
    domain: {
      type: String,
      default: ''
    },
    item: {
      type: String,
      default: ''
    },
    value: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      config: {
        title: 'Edit Dict Strategy',
        okText: 'Save',
        cancelText: 'Close',
        width: 700,
        confirmLoading: false
      },
      labelCol: { span: 6 },
      wrapperCol: { span: 18 },
      rules: {},
      formData: {
        cron: '',
        domain: '',
        enable: false,
        id: '',
        item: '',
        param_jdbc_url: '',
        param_password: '',
        param_sql: '',
        param_username: '',
        type: 'SQL'
      },
      testLoading: false,
      dictStrategyTableData: [],
      columns: [
        {
          title: 'Label',
          dataIndex: 'label'
        },
        {
          title: 'Value',
          dataIndex: 'value'
        },
        {
          title: 'Sequence',
          dataIndex: 'sequence'
        }
      ]
    }
  },
  created () {
    this.formData.domain = this.domain
    this.formData.item = this.item
    this.loadStragery()
  },
  methods: {
    loadStragery () {
      this.$api.getDictStrategy(this.domain, this.item).then(data => {
        if (data) {
          Object.assign(this.formData, data)
        }
      })
    },
    async test () {
      this.testLoading = true
      await this.$api
        .testDictStrategy(this.formData)
        .then(resp => {
          this.dictStrategyTableData = resp.list
          this.$message.success('Operation succeeded')
        })
        .catch(() => (this.testLoading = false))
      this.testLoading = false
    },
    async save () {
      this.config.confirmLoading = true
      await this.$api
        .saveDictStrategy(this.formData)
        .then(() => {
          this.$message.success('Operation succeeded')
          this.$emit('saved')
          this.close()
        })
        .catch(() => {
          this.config.confirmLoading = false
          this.$message.error('Operation failed')
        })
      this.config.confirmLoading = false
    },
    async handleOk () {
      this.$refs.dictForm.validate(async valid => {
        if (valid) {
          await this.save()
        }
      })
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .more-small-table {
  margin-top: 10px;
}
</style>
