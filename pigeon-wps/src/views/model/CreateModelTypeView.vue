<template>
  <div
    class="model_content"
    :class="{'create-mode': mode === 'create' || mode === 'edit' }"
  >
    <a-card size="small">
      <a-row
        class="top-op-btn"
        v-if="mode === 'edit'"
        type="flex"
        justify="end"
      >
        <a-button
          class="btn-gutter"
          type="primary"
          @click="confirm"
        >Confirm</a-button>
        <a-button
          class="btn-gutter"
          @click="cancel"
        >Cancel</a-button>

      </a-row>
      <a-form-model
        ref="form"
        :rules="rules"
        :model="form"
        :wrapper-col="{span: 12}"
        :label-col="{span: 8}"
      >
        <a-form-model-item
          label="Model Type Name"
          prop="model_type_name"
          required
        >
          <a-input
            v-show="mode === 'edit'"
            v-model="form.model_type_name"
          />
          <span
            class="text-indent"
            v-show="mode === 'view'"
          >{{ form.model_type_name }}</span>
        </a-form-model-item>

        <a-form-model-item
          label="Model Configuration Template"
          prop="model_conf_file_id"
          required
        >
          <SUpload
            v-show="mode === 'edit'"
            @uploading="afterUploadConfig"
            v-model="form.model_conf_file_id"
          ></SUpload>
          <span
            v-show="mode === 'view' || (mode === 'edit' && form.id)"
            class="text-indent downloadHover upload-file-text"
            @click="downField(confFileViewId)"
          >{{ confFileView }}</span>
        </a-form-model-item>

        <a-form-model-item label="Training Required" prop="required_model_instance" required>
          <a-switch v-model="form.required_model_instance" size="small" :disabled="mode === 'view'"></a-switch>
        </a-form-model-item>

        <a-form-model-item
          label="AMEE Processor"
          prop="amee_processor_file_id"
          required
        >
          <SUpload
            v-show="mode === 'edit'"
            @uploading="afterUploadAmee"
            v-model="form.amee_processor_file_id"
          ></SUpload>
          <span
            class="text-indent downloadHover upload-file-text"
            @click="downField(ameeprocessorViewId)"
            v-show="mode === 'view' || (mode === 'edit' && form.id)"
          >{{ ameeprocessorView }}</span>
          <a-button
            class="btn-gutter"
            v-if="mode === 'edit' && form.id === undefined"
            @click="downAmee"
          >Download a template</a-button>
        </a-form-model-item>

        <a-form-model-item
          label="Brief Description (Less than 100 characters)"
          prop="brief_description"
          required
        >
          <a-textarea
            rows="4"
            :disabled="mode === 'view'"
            :max-length="100"
            width="500"
            v-model="form.brief_description"
          ></a-textarea>
        </a-form-model-item>
        <a-form-model-item
          label="Detailed Description"
          prop="detail_description"
        >
          <a-textarea
            rows="8"
            width="500"
            :disabled="mode === 'view'"
            v-model="form.detail_description"
          ></a-textarea>
        </a-form-model-item>
      </a-form-model>
    </a-card>
  </div>
</template>

<script>
import SUpload from '@/components/common/SUpload'
import Ellipsis from '@/components/Ellipsis'
import api from '@/api/d1'
export default {
  name: 'CreateModelType',
  components: {
    SUpload,
    Ellipsis
  },
  created () {
    if (this.$route.query.model_type_id) {
      this.loadModelType(this.$route.query.model_type_id)
    }
    if (this.mode === 'view') {
      this.viewSpan = {
        col: 3,
        label: 24,
        wrapper: 0
      }
    } else {
      this.viewSpan = {
        col: 6,
        label: 12,
        wrapper: 12
      }
    }
  },
  props: {
    mode: {
      type: String,
      default: 'edit'
    },
    lastRouteName: {
      type: String,
      default: 'modelBuilding'
    }
  },
  data () {
    return {
      confFileViewId: '',
      confFileView: '',
      ameeprocessorView: '',
      ameeprocessorViewId: '',
      viewSpan: {
        col: 6,
        label: 12,
        warpper: 12
      },
      form: {
        model_type_name: '',
        model_conf_file_id: '',
        amee_processor_file_id: '',
        brief_description: '',
        detail_description: '',
        required_model_instance: true
      },
      rules: {
        model_type_name: [
          {
            required: true,
            message: 'type name is require: Please fill in type name'
          }
        ],
        model_configuration_template: [
          {
            required: true,
            message: 'configuration template is require: Please upload configuration template'
          }
        ],
        amee_processor: [
          {
            required: true,
            message: 'amee processor is require: Please upload configuration amee_processor'
          }
        ],
        brief_description: [
          {
            required: true,
            message: 'brief description is require: Please fill in brief description'
          }
        ]
      }
    }
  },
  methods: {
    confirm () {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.saveOrEdit()
        } else {
          return false
        }
      })
    },
    async saveOrEdit () {
      if (this.form.id) {
        const response = await this.$api.editModelType(this.form)
        if (response.success) this.cancel()
      } else {
        const response = await this.$api.createModelType(this.form)
        if (response.success) this.cancel()
      }
    },
    cancel () {
      this.$multiTab.remove(this.$route.fullPath)
      this.$router.push({
        name: this.lastRouteName,
        params: {
          type: 'modeltype'
        }
      })
    },
    afterUploadAmee (file) {
      this.form.amee_processor_filename = file.name
    },
    afterUploadConfig (file) {
      this.form.model_conf_filename = file.name
    },
    downField (fileId) {
      window.location.href = `${api.download}?file_id=${fileId}`
    },
    downAmee () {
      this.downAmeeField()
    },
    async loadModelType (id) {
      const response = await this.$api.getModelType(id)
      this.form = response.object
      this.confFileView = this.form.model_conf_filename
      this.confFileViewId = this.form.model_conf_file_id
      this.ameeprocessorView = this.form.amee_processor_filename
      this.ameeprocessorViewId = this.form.amee_processor_file_id
    },
    async downAmeeField () {
      const response = await this.$api.downAmeeProcessorTemplate()
      if (response.success) {
        this.downField(response.object)
      }
    }
  }
}
</script>

<style lang="less" scoped>
.model-content {
  margin: 0 auto;
}

.downloadHover {
  cursor: pointer;
  color: blue;
}

.btn-gutter {
  margin-right: 10px;
}

/deep/ .ant-form-item-label label {
  white-space: pre-wrap;
}
.text-indent {
  margin-left: 10px;
}
/deep/ .top-op-btn {
  margin-bottom: 15px;
}
.create-mode .upload-file-text {
  position: absolute;
  left: 100px;
  top: 0;
  line-height: 40px;
}
/deep/ .ant-form-item-children {
  position: static;
}
/deep/ .ant-form-item {
  margin-bottom: 12px;
}
</style>
