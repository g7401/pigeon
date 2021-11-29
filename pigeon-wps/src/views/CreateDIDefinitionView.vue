<template>
  <div class="definition-view">
    <a-card class="basic-information">
      <template #title>
        <a-row
          type="flex"
          justify="space-between"
          class="form-header"
        >
          Basic Information
        </a-row>
      </template>
      <template #extra>
        <a-button
          v-if="mode !== 'view'"
          type="primary"
          :loading="saveLoading"
          @click="handleSubmit"
        >Save</a-button>
      </template>
      <a-form-model
        v-if="mode !== 'view'"
        ref="ruleForm"
        :rules="rules"
        :model="form"
        :wrapper-col="wrapperCol"
        :label-col="labelCol"
      >
        <a-form-model-item
          label="DS Group"
          prop="ds_group"
          required
        >

          <a-input
            class="group-input"
            v-model="form.ds_group"
            placeholder="Choose an existing DS Group or fill in a new one"
          />
          <div class="tag-box">
            <a-tag
              class="option-tag"
              :key="option.value"
              v-for="option in groupOptions"
              @click="chooseGroup(option)"
            >
              <Ellipsis :length="10">{{ option.label }}</Ellipsis>
            </a-tag>
          </div>

        </a-form-model-item>
        <a-form-model-item
          required
          ref="ds_name"
          label="DS Name"
          prop="ds_name"
        >
          <a-input
            v-model="form.ds_name"
            placeholder="Globally unique"
          />
        </a-form-model-item>
        <a-form-model-item
          label="Startup Type"
          prop="startup_type"
        >
          <a-select
            v-model="form.startup_type"
            :options="startupTypeOptions"
          >
          </a-select>
        </a-form-model-item>
        <a-form-model-item
          v-if="form.startup_type === 'SCHEDULE_TRIGGER'"
          label="Startup Type Ext"
          prop="startup_type_ext"
        >
          <a-input
            v-model="form.startup_type_ext"
            placeholder="cron expressions if SCHEDULE_TRIGGER"
          />
        </a-form-model-item>
        <a-form-model-item
          label="Enabled"
          prop="enabled"
        >
          <a-switch v-model="form.enabled" />
        </a-form-model-item>
      </a-form-model>
      <key-value-info
        v-if="mode === 'view'"
        :config="{bordered: true}"
        :options="formOptions"
        :obj="form"
      ></key-value-info>
    </a-card>
    <a-card
      title="DI Process Information"
      class="information-box"
    >
      <a-tabs
        size="large"
        v-model="activeTab"
        @change="changeTab"
      >
        <a-tab-pane
          v-for="tabKey in tabs"
          :key="tabKey"
          :tab="firstLetterUpper(tabKey)"
        >
          <template v-if="activeTab === tabKey">
            <TransformDIProcessInformation
              v-if="tabKey === 'transform'"
              :key="tabKey"
              :di-process-definition-id="diProcessDefinitionId"
            ></TransformDIProcessInformation>
            <DIProcessInformation
              v-else
              :key="tabKey"
              :type="activeTab"
              :di-process-definition-id="diProcessDefinitionId"
            ></DIProcessInformation>
          </template>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script>
import { firstLetterUpper } from '@/components/_util/util'
import {
  createDIProcessDefinition,
  getDIProcessDefinition,
  updateDIProcessDefinition,
  getDefinitionGroups
} from '@/api/definition'
import DIProcessInformation from '@/components/DIDefinitions/DIProcessInformation'
import TransformDIProcessInformation from '@/components/DIDefinitions/TransformDIProcessInformation'
import KeyValueInfo from '@/components/common/KeyValueInfo'
import Ellipsis from '@/components/Ellipsis'

export default {
  name: 'CreateDIDefinition',
  components: { DIProcessInformation, TransformDIProcessInformation, KeyValueInfo, Ellipsis },
  data () {
    return {
      activeTab: 'import',
      tabs: ['import', 'extract', 'transform', 'publish'],
      labelCol: { span: 4 },
      wrapperCol: { span: 14 },
      form: {
        ds_group: '',
        startup_type: 'MANUAL_UPLOAD',
        enabled: true
      },
      enableOptions: [
        { label: 'Yes', value: true },
        { label: 'No', value: false }
      ],
      startupTypeMap: {
        MANUAL_UPLOAD: 'MANUAL_UPLOAD',
        MANUAL_TRIGGER: 'MANUAL_TRIGGER',
        MESSAGE_TRIGGER: 'MESSAGE_TRIGGER',
        API_TRIGGER: 'API_TRIGGER',
        SCHEDULE_TRIGGER: 'SCHEDULE_TRIGGER'
      },
      rules: {
        ds_name: [{ required: true, message: 'Operation tips. more info: Please fill in ds name' }],
        ds_group: [{ required: true, message: 'Operation tips. more info: Please fill in ds group', trigger: 'change' }]
      },
      startupTypeOptions: [],
      groupOptions: [],
      diProcessDefinitionId: null,
      formOptions: [
        { label: 'DS Group', key: 'ds_group' },
        { label: 'DS Name', key: 'ds_name' },
        { label: 'Startup Type', key: 'startup_type' },
        { label: 'Startup Type Ext', key: 'startup_type_ext' },
        { label: 'Enabled', key: 'enabled' }
      ],
      saveLoading: false
    }
  },
  props: {
    mode: {
      type: String,
      default: 'create'
    }
  },
  created () {
    if (this.$route.query.definition_id) {
      this.activeTab = this.$route.query.type || 'import'
      this.diProcessDefinitionId = this.$route.query.definition_id
      this.loadDefinition(this.diProcessDefinitionId)
    }
    this.loadGroups()
    this.startupTypeOptions = this.maptoOptions(this.startupTypeMap)
  },
  methods: {
    firstLetterUpper,
    chooseGroup (option) {
      this.form.ds_group = option.value
    },
    changeTab (tab) {},
    async loadGroups () {
      const resp = await getDefinitionGroups()
      if (!resp || !resp.object) return
      this.groupOptions = this.ArrayToOptions(resp.object.ds_groups.filter(i => i))
    },
    handleSubmit (e) {
      this.$refs.ruleForm.validate(valid => {
        if (valid) {
          this.create()
        } else {
          return false
        }
      })
    },
    createOrupdateDefinition (data) {
      return !this.form.di_process_definition_id ? createDIProcessDefinition(data) : updateDIProcessDefinition(data)
    },
    async create () {
      if (this.form.startup_type !== 'SCHEDULE_TRIGGER') {
        delete this.form.startup_type_ext
      }
      this.saveLoading = true
      const resp = await this.createOrupdateDefinition(this.form).catch(() => (this.saveLoading = false))
      this.saveLoading = false
      if (resp && resp.object && resp.object.di_process_definition_id) {
        this.diProcessDefinitionId = resp.object.di_process_definition_id
        this.form.di_process_definition_id = resp.object.di_process_definition_id
      }
    },
    async loadDefinition (definitionId) {
      const resp = await getDIProcessDefinition(definitionId)
      this.form = resp.object
    },
    maptoOptions (obj) {
      return Object.keys(obj).map(key => ({ label: obj[key], value: key }))
    },
    ArrayToOptions (arr) {
      return arr.map(v => ({ label: v, value: v }))
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .basic-information {
  margin-bottom: 20px;
}
.title {
  margin-bottom: 20px;
  font-size: 20px;
}
.definition-view {
  margin: 0 auto;
}
.form-title {
  font-size: 20px;
  line-height: 28px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
  margin-bottom: 16px;
  -webkit-box-flex: 1;
  -ms-flex: auto;
  flex: auto;
}
.tag-box {
  display: inline-block;
}
/deep/ .option-tag {
  cursor: pointer;
}
/deep/ .ant-descriptions-item-label {
  width: 250px;
}
</style>
