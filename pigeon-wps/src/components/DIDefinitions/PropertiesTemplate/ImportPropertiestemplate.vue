<template>
  <div>
    <h3 class="title">Connection Parameters</h3>
    <a-form-model
      :colon="false"
      :model="form.connectionParameters"
      :rules="rules"
      :label-col="{ span: 4 }"
      :wrapper-col="{span: 12}"
      ref="testCon"
    >
      <a-form-model-item
        required
        label="Host name"
        prop="host"
      >
        <a-input
          @change="change"
          v-model="form.connectionParameters.host"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        label="Port"
        prop="port"
        :autoLink="false"
        ref="port"
      >
        <a-input-number
          @change="() => {$refs.port.onFieldChange();change()}"
          v-model="form.connectionParameters.port"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        label="Username"
        prop="user"
      >
        <a-input
          @change="change"
          v-model="form.connectionParameters.user"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        label="Password"
        prop="password"
      >
        <a-input
          @change="change"
          v-model="form.connectionParameters.password"
        />
      </a-form-model-item>
      <a-form-model-item label=" ">
        <a-checkbox
          @change="change"
          v-model="form.use_ssl"
        >
          Use SSL
        </a-checkbox>
      </a-form-model-item>
      <a-form-model-item label="SSL Key File">
        <a-input
          @change="change"
          v-model="form.ssl_key_file"
          :disabled="!form.use_ssl"
        />
      </a-form-model-item>
      <a-form-model-item label="SSL CERT File">
        <a-input
          @change="change"
          v-model="form.ssl_cert_file"
          :disabled="!form.use_ssl"
        />
      </a-form-model-item>
      <a-form-model-item label="SSL CA File">
        <a-input
          @change="change"
          v-model="form.ssl_ca_file"
          :disabled="!form.use_ssl"
        />
      </a-form-model-item>
      <a-form-model-item label=" ">
        <a-button
          type="primary"
          :loading="conLoading"
          @click="testConnection"
        >
          test Connection
        </a-button>
      </a-form-model-item>
    </a-form-model>
    <h3 class="title">Data Parameters</h3>
    <a-form-model
      :colon="false"
      :label-col="{ span: 4 }"
      :wrapper-col="{span: 12}"
      :model="form"
      ref="dataForm"
    >
      <a-form-model-item
        required
        label="Source Database"
        prop="sourceDatabase"
      >
        <a-input
          @change="change"
          v-model="form.sourceDatabase"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        label="Source Schema"
        prop="sourceSchema"
      >
        <a-input
          @change="change"
          v-model="form.sourceSchema"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        prop="sourceTable"
        label="Source Table"
      >
        <a-input
          @change="change"
          v-model="form.sourceTable"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        prop="sourceColumnsMode"
        label="Source Columns"
      >
        <a-radio-group
          @change="change"
          v-model="form.sourceColumnsMode"
        >
          <a-radio value="ALL">
            All Columns
          </a-radio>
          <a-radio value="SPECIFIC">
            Specific Columns
          </a-radio>
        </a-radio-group>
        <a-input
          v-if="form.sourceColumnsMode === 'SPECIFIC'"
          @change="change"
          v-model="form.specificColumns"
          placeholder="用半角逗号分隔列"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        prop="fetchingMode"
        label="Fetching Mode"
      >
        <a-radio-group
          @change="change"
          v-model="form.fetchingMode"
        >
          <a-radio value="full">
            Full
          </a-radio>
          <a-radio value="incremental">
            Incremental
          </a-radio>
        </a-radio-group>
        <a-input
          v-if="form.fetchingMode === 'incremental'"
          @change="change"
          v-model="form.timestampColumn"
          placeholder="指定1个时间戳列"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        prop="orderByColumns"
        label="Order By Columns"
      >
        <a-input
          @change="change"
          v-model="form.orderByColumns"
          placeholder="用半角逗号分隔列，例如：id asc, test desc"
        />
      </a-form-model-item>
    </a-form-model>
  </div>
</template>

<script>
export default {
  props: {
    importData: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      form: {
        connectionParameters: { host: '', password: '', port: 0, user: '' },
        fetchingMode: '',
        orderByColumns: '',
        sourceColumnsMode: '',
        sourceDatabase: '',
        sourceTable: '',
        specificColumns: '',
        timestampColumn: ''
      },
      rules: {
        host: [{ requied: true }],
        port: [{ requied: true, type: 'number' }],
        password: [{ requied: true }],
        user: [{ requied: true }]
      },
      conLoading: false
    }
  },
  created () {
    console.log('this.importData: ', this.importData)
    Object.assign(this.form, this.importData)
  },
  methods: {
    validate () {
      let isValid = false
      this.$refs.testCon.validate(valid => {
        isValid = valid
      })
      this.$refs.dataForm.validate(valid => {
        isValid = valid
      })
      return isValid
    },
    async testConnection () {
      console.log('gggg', this.form)
      this.$refs.testCon.validate(async valid => {
        if (valid) {
          this.conLoading = true
          await this.$http
            .post('/kapok-dis/master/plugin/button/test-SqlServer-connection', this.form.connectionParameters)
            .catch(() => (this.conLoading = false))
          this.conLoading = false
        }
      })
    },
    change () {
      const formData = Object.assign({}, this.form.connectionParameters.port)
      if (!this.form.use_ssl) {
        delete formData.ssl_key_file
        delete formData.ssl_cert_file
        delete formData.ssl_ca_file
      }
      this.$emit('changeData', formData)
    }
  }
}
</script>
