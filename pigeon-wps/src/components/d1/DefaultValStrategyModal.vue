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
          :rules="rules"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          :model="formData"
        >
          <a-form-model-item
            label="DF Key"
            prop="form_df_key"
          >
            <a-input
              v-model="formData.form_df_key"
              disabled
            />
          </a-form-model-item>
          <a-form-model-item
            label="Field Key"
            prop="form_df_key"
          >
            <a-input
              v-model="formData.form_field_key"
              disabled
            />
          </a-form-model-item>
          <a-form-model-item
            label="Element Type"
            prop="element_type"
          >
            <a-input
              v-model="formData.element_type"
              disabled
            />
          </a-form-model-item>
        </a-form-model>
        <a-tabs v-model="formData.field_type">
          <a-tab-pane
            tab="Manual"
            key="MANUAL"
          >
            <a-form-model
              :labelCol="labelCol"
              :wrapperCol="wrapperCol"
              :model="formData"
            >
              <a-form-model-item
                label="Value"
                prop="manual_conf"
              >
                <a-input
                  v-model="formData.manual_conf"
                  :placeholder="manualPlaceHolder"
                />
              </a-form-model-item>
            </a-form-model>
          </a-tab-pane>
          <a-tab-pane
            tab="Auto"
            key="AUTO"
          >
            <a-form-model
              :labelCol="labelCol"
              :wrapperCol="wrapperCol"
              :model="formData"
            >
              <a-form-model-item
                label="Type"
                prop="plugin_type"
              >
                <a-select v-model="formData.plugin_type">
                  <a-select-option value="SQL">SQL</a-select-option>
                </a-select>
              </a-form-model-item>
              <a-form-model-item
                label="JDBC URL"
                prop="plugin_jdbc_url"
              >
                <a-input
                  v-model="formData.plugin_jdbc_url"
                  placeholder="jdbc:mysql://localhost:3306/d1_core?useSSL=false"
                />
              </a-form-model-item>
              <a-form-model-item
                label="Username"
                prop="plugin_username"
              >
                <a-input v-model="formData.plugin_username" />
              </a-form-model-item>
              <a-form-model-item
                label="Password"
                prop="plugin_password"
              >
                <a-input-password v-model="formData.plugin_password" />
              </a-form-model-item>
              <a-form-model-item
                label="SQL"
                prop="plugin_sql"
              >
                <a-textarea
                  v-model="formData.plugin_sql"
                  placeholder="select id as value,id as sequence, model_name as label from model limit 10"
                />
              </a-form-model-item>
              <a-form-model-item
                label="Cron"
                prop="plugin_cron"
              >
                <a-input
                  v-model="formData.plugin_cron"
                  placeholder="Seconds Minutes Hours DayofMonth Month DayofWeek Year"
                />
              </a-form-model-item>
              <a-form-model-item
                label="Enabled"
                prop="plugin_enabled"
              >
                <a-switch v-model="formData.plugin_enabled" />
              </a-form-model-item>
              <a-form-model-item
                :colon="false"
                label=" "
              >
                <a-button
                  type="primary"
                  ghost
                  :loading="testLoading"
                  @click="testStrategy"
                >Test Result</a-button>
              </a-form-model-item>
              <div
                class="test-result"
                v-if="testResult"
              >
                {{ testResult }}
              </div>
            </a-form-model>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
  </div>
</template>

<script>
import { QueryFormType } from '@/assets/d1/map'
export default {
  props: {
    field: {
      type: Object,
      default () {
        return {}
      }
    },
    value: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      config: {
        title: 'Default Val Strategy',
        okText: 'Save',
        cancelText: 'Cancel',
        width: 700,
        confirmLoading: false
      },
      labelCol: { span: 6 },
      wrapperCol: { span: 18 },
      activeTab: 'General',
      rules: {
        db_type: [{ required: true }],
        db_port: [{ required: true }],
        db_host: [{ required: true }],
        db_user: [{ required: true }],
        db_password: [{ required: true }],
        db_name: [{ required: true }]
      },
      formData: {
        id: '',
        form_field_key: '',
        form_df_key: '',
        field_type: 'MANUAL',
        plugin_cron: '',
        plugin_jdbc_url: '',
        plugin_username: '',
        plugin_password: '',
        plugin_sql: '',
        plugin_enabled: 'false',
        manual_conf: '',
        plugin_type: 'SQL',
        element_type: ''
      },
      manualPlaceHolder: 'Please inter a json Array',
      testResult: ''
    }
  },
  created () {
    this.init()
  },
  methods: {
    init () {
      this.formData.form_df_key = this.field.df_key
      this.formData.form_field_key = this.field.db_field_name
      this.formData.element_type = this.field.form_field_query_type
      this.handleManualConfPlaceHolder(this.formData.element_type)
      if (this.field.defaults_configuration) {
        Object.assign(this.formData, this.field.defaults_configuration)
      } else {
        this.loadStragery()
      }
    },
    loadStragery () {
      this.$api.getDefaultValStrategy(this.formData.form_df_key, this.formData.form_field_key).then(data => {
        if (data) {
          Object.assign(this.formData, data)
        }
      })
    },
    handleManualConfPlaceHolder (elementType) {
      if (QueryFormType.MULTIPLE_CHOICE_LIST === elementType) {
        this.manualPlaceHolder = 'for example : ["value1","value2"]'
      } else if (
        QueryFormType.SINGLE_CHOICE_LIST === elementType ||
        QueryFormType.SINGLE_CHOICE_LIST_R1 === elementType
      ) {
        this.manualPlaceHolder = 'for example : ["value1"]'
      } else if (QueryFormType.SINGLE_DATE === elementType) {
        this.manualPlaceHolder = 'for example : ["1970-01-01"]'
      } else if (QueryFormType.DATE_RANGE === elementType) {
        this.manualPlaceHolder = 'for example : ["1970-01-01","2010-01-09"]'
      } else if (QueryFormType.SINGLE_DATETIME === elementType) {
        this.manualPlaceHolder = 'for example : ["1970-01-01 00:00:00"]'
      } else if (QueryFormType.DATE_TIME_RANGE === elementType) {
        this.manualPlaceHolder = 'for example : ["1970-01-01 00:00:00","1970-01-01 08:00:00"]'
      } else {
        this.manualPlaceHolder = 'for example : ["inputValue"]'
      }
    },
    async testStrategy () {
      this.testLoading = true
      await this.$api
        .testDefaultValStrategy(this.formData)
        .then(resp => {
          this.testResult = resp
          this.$message.success('Operation succeeded')
        })
        .catch(() => (this.testLoading = false))
      this.testLoading = false
    },
    handleOk () {
      if (this.formData.field_type === 'MANUAL') {
        const manualVal = this.formData.manual_conf
        if (manualVal) {
          try {
            const manualValArr = JSON.parse(manualVal)
            if (manualValArr instanceof Array) {
              // Nothing to do
            } else {
              this.$message.warning("Manual Method's value must be a json array ")
              return
            }
          } catch (e) {
            this.$message.warning("Manual Method's value must be a json array ")
            return
          }
        }
      }
      this.$message.success('Operation succeeded')
      this.$emit('saved', this.formData)
      this.close()
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
    }
  }
}
</script>

<style scoped>
.test-result {
  margin-top: 10px;
  text-align: center;
}
</style>
