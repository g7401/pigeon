<template>
  <div>
    <a-form-model
      :colon="false"
      :model="form"
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
          v-model="form.host"
          :disabled="disabled"
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
          v-model="form.port"
          :disabled="disabled"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        label="Username"
        prop="user"
      >
        <a-input
          @change="change"
          v-model="form.user"
          :disabled="disabled"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        label="Password"
        prop="password"
      >
        <a-input-password
          @change="change"
          v-model="form.password"
          :disabled="disabled"
        />
      </a-form-model-item>
      <a-form-model-item label=" ">
        <a-button
          type="primary"
          :loading="conLoading"
          @click="testConnection"
        >
          Test Connection
        </a-button>
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
    },
    disabled: {
      type: Boolean,
      default: false
    },
    processorUid: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      form: { host: '', password: '', port: 0, user: '' },
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
      return isValid
    },
    async testConnection () {
      this.$refs.testCon.validate(async valid => {
        if (valid) {
          this.conLoading = true
          await this.$api
            .testProcessorConnection(this.processorUid, JSON.stringify(this.form))
            .finally(() => (this.conLoading = false))
        }
      })
    },
    change () {
      const formData = Object.assign({}, this.form)
      this.$emit('changeData', formData)
    }
  }
}
</script>
