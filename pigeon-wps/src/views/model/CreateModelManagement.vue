<template>
  <div
    class="model_content"
    :class="{'create-mode': mode === 'create' || mode === 'edit' }"
  >
    <a-card
      v-if="page === 'basic'"
      size="small"
    >
      <a-row
        class="top-op-btn"
        v-if="mode != 'view'"
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
        :wrapper-col="wrapperCol"
        :label-col="labelCol"
      >
        <a-form-model-item
          label="Model Type Name"
          prop="model_type"
          required
        >
          <a-select
            v-model="form.model_type"
            v-show="mode != 'view'"
            :options="modelTypeList"
            :showSearch="true"
            :filterOption="filterOption"
            @select="modelTypeChange"
          ></a-select>
          <span
            class="text-indent"
            v-show="mode === 'view'"
          >{{ form.model_type_name }}</span>
        </a-form-model-item>
        <a-form-model-item
          v-show="mode != 'view'"
          label=" "
          :colon="false"
        >
          <a-button
            class="btn-gutter"
            type="primary"
            @click="createModelType"
          >Create Model Type</a-button>
          <span class="btn-gutter tip-text">If cannot find what you want, you can create a new model type</span>
        </a-form-model-item>

        <a-form-model-item
          label="Model Name"
          prop="model_name"
          required
        >
          <a-input
            v-show="mode != 'view'"
            v-model="form.model_name"
          />
          <span
            class="text-indent"
            v-show="mode === 'view'"
          >{{ form.model_name }}</span>
        </a-form-model-item>
        <a-form-model-item
          v-if="modelTypeObj.required_model_instance"
          label="Feature Repository Object For Training"
          prop="feature_store_reference_traning"
        >
          <a-button
            type="primary"
            @click="featureSotoreRefTraining"
          >Configure the Source of the Datasets</a-button>
          <span
            class="text-indent"
            v-show="tableForTraining"
          >{{ tableForTraining }}</span>
        </a-form-model-item>

        <a-form-model-item
          label="Feature Repository Object For Serving"
          prop="feature_store_reference_serving"
        >
          <a-button
            type="primary"
            @click="featureSotoreRefServing"
          >Configure the Source of the Datasets</a-button>
          <span
            class="text-indent"
            v-show="tableForServing"
          >{{ tableForServing }}</span>
        </a-form-model-item>

        <a-form-model-item
          label="Model Configuration "
          prop="model_conf_file_id"
        >
          <SUpload
            v-if="mode !='view'"
            key="2"
            @uploading="afterUploadConfig"
            v-model="form.model_conf_file_id"
          ></SUpload>
          <div
            class="text-indent downloadHover upload-file-text"
            @click="downField(modelTypeObj.model_conf_file_id)"
          >{{ modelTypeObj.model_conf_filename }}</div>
        </a-form-model-item>

        <a-form-model-item
          class="model_amee"
          label="AMEE Processor"
        >
          <span
            class="text-indent downloadHover"
            @click="downField(modelTypeObj.amee_processor_file_id)"
          >{{ modelTypeObj.amee_processor_filename }}</span>
        </a-form-model-item>

        <a-form-model-item
          label="Brief Description (Less than 100 characters)"
          prop="brief_description"
          required
        >
          <a-textarea
            rows="4"
            :max-length="100"
            width="500"
            v-model="form.brief_description"
            :disabled="mode === 'view'"
          ></a-textarea>
        </a-form-model-item>
        <a-form-model-item
          label="Detailed Description"
          prop="detailed_description"
        >
          <a-textarea
            rows="8"
            width="500"
            v-model="form.detailed_description"
            :disabled="mode === 'view'"
          ></a-textarea>
        </a-form-model-item>
        <a-form-model-item
          label=" "
          :colon="false"
          v-if="mode === 'create' && modelTypeObj.required_model_instance"
        >
          <a-checkbox v-model="modelInstanceChecked">
            Deploy First Model Instance
          </a-checkbox>
          <SUpload
            :config="{btnText: 'Upload Model Instance' }"
            :disabled="!modelInstanceChecked"
            class="btn-gutter"
            key="3"
            @uploading="uploadModelInstance"
            v-model="form.first_model_instance_file_id"
          ></SUpload>
        </a-form-model-item>
      </a-form-model>
    </a-card>
    <a-card v-if="page === 'advanced'">
      <h2>List Of Model [ {{ form.model_name }} ] Instances</h2>
      <d1-vue-component
        ref="d1"
        :options="generateOption"
        @onTableOperationButtonClick="handleOperation"
        @onToolbarButtonClick="handleToolbar"
      >
        <template #is_deployed="{ val }">
          {{ val === '1' ? 'Yes' : 'No' }}
        </template>
      </d1-vue-component>
    </a-card>
    <a-modal
      title="Upload a Model Instance"
      v-model="advanceUpVisable"
      :maskClosable="true"
      :footer="null"
      :destroyOnClose="true"
      on-ok="handleOk"
    >
      <a-row>
        <a-col :offset="8">
          <SUpload
            key="1"
            @uploading="addModelInstance"
            v-model="deployFileId"
          ></SUpload>
        </a-col>
      </a-row>
    </a-modal>
    <a-modal
      title="Deploy Model Instance"
      v-model="deployVisable"
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="deploy"
        >
          Confirm
        </a-button>
        <a-button
          key="back"
          @click="handleCancel"
        >
          Cancel
        </a-button>
      </template>
      <a-row>
        <a-col :offset="3">
          <span>Are you sure to deploy model instance {{ deployInstanceId }} for model name {{ form.model_name }} ?</span>
        </a-col>
      </a-row>
    </a-modal>
    <ModelInstanceDetails
      v-if="modelInstance"
      v-model="details"
      :modelInstance="modelInstance"
    ></ModelInstanceDetails>
  </div>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import SUpload from '@/components/common/SUpload'
import Ellipsis from '@/components/Ellipsis'
import ModelInstanceDetails from '@/components/model/modelInstanceDetails'
import api from '@/api/d1'
const deploy = {
  label: 'Deploy', // 按钮显示的名称
  type: 'primary', // 按钮的类型,默认空,支持  defualt primary dashed danger link
  name: 'edit', // 按钮点击时间触发的函数名称
  width: 60
}
const view = {
  label: 'Details', // 按钮显示的名称
  type: 'default', // 按钮的类型,默认空,支持  defualt primary dashed danger link
  name: 'view', // 按钮点击时间触发的函数名称
  width: 60
}
export default {
  name: 'CreateModelManagement',
  components: { SUpload, Ellipsis, d1VueComponent, ModelInstanceDetails },
  activated () {
    if (this.$route.query.dataFacetKeyForTraining) {
      this.dataFacetKeyForTraining = this.$route.query.dataFacetKeyForTraining
      this.tableForTraining = this.$route.query.dataStoreId
    }
    if (this.$route.query.dataFacetKeyForServing) {
      this.dataFacetKeyForServing = this.$route.query.dataFacetKeyForServing
      this.tableForServing = this.$route.query.dataStoreId
    }
  },
  created () {
    // 获取model type
    this.getAllModelType()
    if (this.mode === 'view' || this.mode === 'edit') {
      this.getModel(this.$route.query.model_id)
      this.generateOption.preFilters = { model_id: this.$route.query.model_id }
    }
    if (this.mode === 'edit') {
      this.generateOption.tableOperationButtonList = [deploy, view]
    } else {
      this.generateOption.tableOperationButtonList = [view]
    }
  },
  props: {
    page: {
      type: String,
      default: 'basic'
    },
    mode: {
      type: String,
      default: 'create'
    },
    lastRouteName: {
      type: String,
      default: 'modelBuilding'
    }
  },
  data () {
    return {
      deployFileId: '',
      details: false,
      modelInstance: null,
      modelTypeObj: {},
      modelTypeList: [],
      labelCol: { span: 8 },
      wrapperCol: { span: 12 },
      dataFacetKeyForTraining: '',
      tableForTraining: '',
      dataFacetKeyForServing: '',
      tableForServing: '',
      form: {
        model_type: '',
        model_name: '',
        model_conf_file_id: '',
        brief_description: '',
        detailed_description: ''
      },
      rules: {
        model_type: [{ required: true, message: 'type name is require: Please choose model type' }],
        model_name: [{ required: true, message: 'model name is require: Please fill in model name' }],
        brief_description: [
          { required: true, message: 'brief description is require: Please fill in brief description' }
        ]
      },
      modelInstanceChecked: true,
      generateOption: {
        d1ClientBaseUrl: '/lotus-mms',
        formSize: 'small',
        tableSize: 'small',
        dataFacetKey: 'model_advance_dfk',
        pageSize: 10,
        showToolbarButtonList: true,
        asyncExport: false,
        showExportButton: false,
        serialNumber: true,
        showTableOperationButton: true,
        openform: true,
        operationWidth: 200,
        toolbarButtonList: [
          {
            label: 'Upload a Model Instance', // 按钮显示的名称
            type: 'primary', // 按钮的类型,默认空,支持  primary，success，info，warning，danger
            name: 'create', // 用户点击时返回的组件
            elColWidth: 3 // 按钮的占位
          }
        ],
        tableOperationButtonList: [view]
      },
      advanceUpVisable: false,
      deployVisable: false,
      deployInstanceId: ''
    }
  },
  methods: {
    async getModel (id) {
      const respone = await this.$api.getModel(id)
      if (respone.success) {
        this.form = respone.object
        this.dataFacetKeyForTraining = respone.object.training_feature_store_reference_key
        this.tableForTraining = respone.object.training_feature_store_reference
        this.dataFacetKeyForServing = respone.object.prediction_feature_store_reference_key
        this.tableForServing = respone.object.prediction_feature_store_reference
        const modelRes = await this.$api.getModelType(respone.object.model_type_id)
        this.modelTypeObj = modelRes.object
        this.form.model_type = JSON.stringify(this.modelTypeObj)
        console.log(modelRes)
        this.modelTypeObj.model_conf_file_id = respone.object.model_conf_file_id
        this.modelTypeObj.model_conf_filename = respone.object.model_conf_filename
      }
    },
    changePage (page) {
      this.page = page
    },
    confirm () {
      this.$refs.form.validate(valid => {
        if (valid) {
          const value = {}
          value.model_name = this.form.model_name
          value.model_type_id = this.modelTypeObj.model_type_id
          value.brief_description = this.form.brief_description
          value.detailed_description = this.form.detailed_description
          value.model_conf_file_id = this.form.model_conf_file_id
          if (value.model_conf_file_id) {
            value.model_conf_filename = this.form.model_conf_filename
          } else {
            value.model_conf_file_id = this.modelTypeObj.model_conf_file_id
            value.model_conf_filename = this.modelTypeObj.model_conf_filename
          }
          value.training_feature_store_reference_key = this.dataFacetKeyForTraining
          value.training_feature_store_reference = this.tableForTraining
          value.prediction_feature_store_reference = this.tableForServing
          value.prediction_feature_store_reference_key = this.dataFacetKeyForServing
          value.model_id = this.form.model_id
          if (this.form.first_model_instance_file_id) {
            value.first_model_instance_file_id = this.form.first_model_instance_file_id
            value.first_model_instance_filename = this.form.first_model_instance_filename
            value.first_model_instance_file_size = this.form.first_model_instance_file_size
          }
          if (value.model_id) {
            this.updateModel(value)
          } else {
            this.createModel(value)
          }
        } else {
          return false
        }
      })
    },
    async createModel (value) {
      const respone = await this.$api.createModel(value)
      if (respone.success) {
        this.cancel()
      }
    },
    async updateModel (value) {
      const respone = await this.$api.updateModel(value)
      if (respone.success) {
        this.cancel()
      }
    },
    afterUploadConfig (file) {
      this.form.model_conf_filename = file.name
    },
    uploadModelInstance (file) {
      if (file) {
        this.form.first_model_instance_filename = file.name
        this.form.first_model_instance_file_size = file.size
      }
    },
    cancel () {
      this.$multiTab.remove(this.$route.fullPath)
      this.$router.push({
        name: this.lastRouteName,
        params: {
          type: 'modelmanagement'
        }
      })
    },
    featureSotoreRefTraining () {
      if (this.mode === 'view') {
        this.$router.push({
          name: 'trainingConfigureDatasets',
          query: { dataFacetKey: this.dataFacetKeyForTraining, mode: 'view' }
        })
      } else {
        this.$router.push({
          name: 'trainingConfigureDatasets',
          query: { dataFacetKey: this.dataFacetKeyForTraining }
        })
      }
    },
    featureSotoreRefServing () {
      if (this.mode === 'view') {
        this.$router.push({
          name: 'servingConfigureDatasets',
          query: { dataFacetKey: this.dataFacetKeyForServing, mode: 'view' }
        })
      } else {
        this.$router.push({
          name: 'servingConfigureDatasets',
          query: { dataFacetKey: this.dataFacetKeyForServing }
        })
      }
    },
    createModelType () {
      this.$router.push({ name: 'createModelType' })
    },
    downField (fileId) {
      window.location.href = `${api.download}?file_id=${fileId}`
    },
    modelTypeChange (value) {
      this.modelTypeObj = JSON.parse(value)
    },
    filterOption (input, option) {
      return option.componentOptions.propsData.title.toUpperCase().indexOf(input.toUpperCase()) >= 0
    },
    afterUpload () {},
    async loadModelType (id) {
      const response = await this.$api.getModelType(this.$route.query.model_type_ids)
      this.form = response.obj
    },
    async getAllModelType () {
      const response = await this.$api.getAllModelType()
      if (response.success) {
        response.object.forEach(element => {
          const obj = {}
          obj.key = element.model_type_id
          obj.title = element.model_type_name
          obj.value = JSON.stringify(element)
          this.modelTypeList.push(obj)
        })
      }
    },
    goCreateDefinitionPage () {
      this.advanceUpVisable = true
    },
    handleToolbar (toolbarName) {
      if (toolbarName === 'create') {
        this.goCreateDefinitionPage()
      }
    },
    goEditPage (item, mode = 'edit') {
      console.log(item)
      this.deployInstanceId = item.model_instance_id
      this.deployVisable = true
    },
    goViewPage (item) {
      this.details = true
      this.modelInstance = item
    },
    handleOperation (item, operationName) {
      const operationMap = {
        edit: this.goEditPage,
        view: this.goViewPage
      }
      const operationFn = operationMap[operationName]
      operationFn && operationFn(item)
    },
    handleOk () {
      this.advanceUpVisable = false
    },
    async deploy () {
      const respone = await this.$api.deploy({ model_id: this.form.model_id, model_instance_id: this.deployInstanceId })
      if (respone.success) {
        this.deployVisable = false
        this.$refs.d1.runQuery()
      }
    },
    addModelInstance (file) {
      if (file) {
        this.uploadInstance({
          model_id: this.form.model_id,
          model_instance_file_id: file.deployFileId,
          model_instance_filename: file.name,
          model_instance_file_size: file.size
        })
      }
    },
    async uploadInstance (data) {
      const respone = await this.$api.uploadInstance(data)
      if (respone.success) {
        this.advanceUpVisable = false
        this.$refs.d1.runQuery()
      }
    },
    handleCancel () {
      this.deployVisable = false
    }
  }
}
</script>

<style lang="less" scoped>
.model-content {
  margin: 0 auto;
}
.btn-gutter {
  margin-left: 12px;
}
/deep/ .ant-form-item-label label {
  white-space: pre-wrap;
}

.downloadHover {
  cursor: pointer;
  color: #1890ff;
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
