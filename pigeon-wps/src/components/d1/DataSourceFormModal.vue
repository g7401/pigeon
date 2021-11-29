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
          ref="dataSourceForm1"
          :rules="rules"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          :model="formData"
        >
          <a-form-model-item
            required
            label="Data Source Name"
            prop="name"
          >
            <a-input v-model="formData.name" />
          </a-form-model-item>
          <a-form-model-item
            required
            label="Data Source Type"
            prop="type"
          >
            <a-select v-model="formData.type">
              <a-select-option value="MSSQL">MSSQL</a-select-option>
              <a-select-option value="MYSQL">MYSQL</a-select-option>
            </a-select>
          </a-form-model-item>

        </a-form-model>
        <a-tabs v-model="activeTab">
          <a-tab-pane
            tab="General"
            key="General"
          >
            <a-form-model
              :labelCol="labelCol"
              :wrapperCol="wrapperCol"
              :model="formData.connection_profile_props"
              ref="dataSourceForm2"
            >
              <a-form-model-item
                required
                label="Host"
                prop="host"
              >
                <a-input v-model="formData.connection_profile_props.host" />
              </a-form-model-item>
              <a-form-model-item
                required
                label="Port"
                prop="port"
              >
                <a-input-number
                  :min="0"
                  v-model="formData.connection_profile_props.port"
                />
              </a-form-model-item>
              <a-form-model-item
                required
                label="User"
                prop="user"
              >
                <a-input v-model="formData.connection_profile_props.user" />
              </a-form-model-item>
              <a-form-model-item
                required
                label="Password"
                prop="password"
              >
                <a-input-password :visibility-toggle="false" v-model="formData.connection_profile_props.password" />
              </a-form-model-item>
              <a-form-model-item
                label="More Parameters"
                prop="more_parameters"
              >
                <a-textarea :autoSize="{ minRows: 3, maxRows: 10 }" v-model="formData.connection_profile_props.more_parameters" />
              </a-form-model-item>
              <a-form-model-item
                label=" "
                :colon="false"
              >
                <a-button :loading="testLoading" :disabled="disableTest" @click="testDataSourceConnection">Test Connection</a-button>
              </a-form-model-item>
            </a-form-model>
          </a-tab-pane>
          <!-- <a-tab-pane
            tab="Advanced"
            key="Advanced"
          >
            <a-form-model
              :labelCol="labelCol"
              :wrapperCol="wrapperCol"
              :model="formData.security_config_dto"
            >
              <a-form-model-item
                label=" "
                prop="use_ssh_tunnel"
                :colon="false"
              >
                <a-checkbox
                  true-label="true"
                  v-model="formData.security_config_dto.use_ssh_tunnel"
                >
                  Use SSH tunnel
                </a-checkbox>
              </a-form-model-item>
              <a-form-model-item
                label="Proxy Host"
                prop="ssh_proxy_host"
              >
                <a-input v-model="formData.security_config_dto.ssh_proxy_host" />
              </a-form-model-item>
              <a-form-model-item
                label="Port"
                prop="ssh_proxy_port"
              >
                <a-input-number
                  :min="0"
                  v-model="formData.security_config_dto.ssh_proxy_port"
                />
              </a-form-model-item>
              <a-form-model-item
                label="Proxy User"
                prop="ssh_proxy_user"
              >
                <a-input v-model="formData.security_config_dto.ssh_proxy_user" />
              </a-form-model-item>
              <a-form-model-item
                label="Auth type"
                prop="ssh_auth_type"
              >
                <a-select v-model="formData.security_config_dto.ssh_auth_type">
                  <a-select-option value="Password">password</a-select-option>
                  <a-select-option value="openSSHOrPuTTY">Key pair (OpenSSH or PuTTY)</a-select-option>
                </a-select>
              </a-form-model-item>
              <a-form-model-item
                v-if="formData.security_config_dto.ssh_auth_type === 'Password'"
                label="Proxy Password"
                prop="ssh_proxy_password"
              >
                <a-input-password v-model="formData.security_config_dto.ssh_proxy_password" />
              </a-form-model-item>
              <a-form-model-item
                label=" "
                :colon="false"
                prop="use_ssl"
              >
                <a-checkbox
                  true-label="true"
                  v-model="formData.security_config_dto.use_ssl"
                >Use SSH
                </a-checkbox>
              </a-form-model-item>
            </a-form-model>
          </a-tab-pane> -->
        </a-tabs>
      </div>
    </a-modal>
  </div>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
export default {
  props: {
    dsId: {
      type: [Number, String],
      default: 0
    },
    value: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      config: {
        title: 'Add Data Source',
        okText: 'Save',
        cancelText: 'Cancel',
        width: 700,
        confirmLoading: false
      },
      labelCol: { span: 6 },
      wrapperCol: { span: 18 },
      activeTab: 'General',
      rules: {
        type: [{ required: true }],
        port: [{ required: true }],
        host: [{ required: true }],
        user: [{ required: true }],
        password: [{ required: true }],
        name: [{ required: true, message: 'data_source_name is required' }]
      },
      formData: {
        uid: undefined,
        name: '',
        type: 'MYSQL',
        connection_profile_props: {
          port: 0,
          host: '',
          user: '',
          password: '',
          more_parameters: ''
        }
        // security_config_dto: {
        //   use_ssh_tunnel: false,
        //   use_ssl: false,
        //   ssh_auth_type: 'Password',
        //   current_file_name: '',
        //   file_name: '',
        //   ssh_proxy_port: 22
        // }
      },
      testLoading: false,
      isConnectSuccess: false,
      isTest: false
    }
  },
  computed: {
    disableTest () {
      return !this.formData.connection_profile_props.port || !this.formData.connection_profile_props.host || !this.formData.connection_profile_props.user || !this.formData.connection_profile_props.password
    }
  },
  created () {
    this.dsId && this.getDataSource()
  },
  methods: {
    async testDataSourceConnection () {
      this.testLoading = true
      const isConnectSuccess = await this.$api.testDataSourceConnection(this.formData.type, JSON.stringify(this.formData.connection_profile_props)).finally(() => {
        this.testLoading = false
      })
      this.isConnectSuccess = isConnectSuccess
      this.isTest = true
      if (!isConnectSuccess) {
        this.$message.error('Connect Fail')
      } else {
        this.$message.success('Connect Successed')
      }
    },
    async save () {
      const apiKey = this.dsId ? 'editDataSource' : 'addDataSource'
      this.config.confirmLoading = true
      const requsetData = cloneDeep(this.formData)
      requsetData.connection_profile_props = JSON.stringify(requsetData.connection_profile_props)
      const resp = await this.$api[apiKey](requsetData).finally(() => (this.config.confirmLoading = false))
      return resp
    },
    async getDataSource () {
      const result = await this.$api.getDataSource(this.dsId)
      if (result) {
        Object.keys(this.formData).forEach(key => {
          this.formData[key] = result[key]
        })
        this.formData.connection_profile_props = JSON.parse(result.connection_profile_props)
      }
    },
    async handleOk () {
      let isValid = true
      this.$refs.dataSourceForm1.validate(async valid => {
        if (!valid) {
          isValid = false
        }
      })
      this.$refs.dataSourceForm2.validate(async valid => {
        if (!valid) {
          isValid = false
        }
      })
      if (!isValid) return
      if (!this.isTest) {
        await this.testDataSourceConnection()
      }
      if (!this.isConnectSuccess) {
        return this.$message.info('Operation failed. More info: please click save btn after test connection successed')
      }
      const resp = await this.save()
      this.$emit('saved', resp)
      this.close()
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="less" scoped>
.ant-form-item {
  margin-bottom: 5px;
}
</style>
