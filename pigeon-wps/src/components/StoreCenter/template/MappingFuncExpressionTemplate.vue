<template>
  <div>
    <h3 class="title">Input Attribute(s) Coming From Source Attributes</h3>
    <div class="mg-b-10">
      <template v-if="srcAttrs.length">
        <div
          v-for="attr in srcAttrs"
          :key="attr.ds_uid + attr.src_attr_uid"
        >
          {{ attr.ds_name }}, {{ attr.src_attr_name }}
        </div>
      </template>
      <template v-else>
        N/A
      </template>
    </div>
    <h3 class="mg-t-20 title">Input Attribute(s) Coming From Target Attributes</h3>
    <div
      class="target-attrs-box mg-b-10"
      style="overflow: hidden"
    >
      <a-row
        class="mg-b-5"
        :gutter="15"
        v-for="(attrText, index) in inputTargetAttrs"
        :key="index"
      >
        <a-col :span="20">
          <a-select
            :options="options"
            v-model="inputTargetAttrs[index]"
            @change="change"
          ></a-select>
        </a-col>
        <a-col :span="4">
          <a-button
            type="danger"
            ghost
            icon="minus"
            @click="removeItem(index)"
          ></a-button>
        </a-col>
      </a-row>
      <a-row :gutter="15">
        <a-col :span="20"></a-col>
        <a-col :span="4">
          <a-button
            type="primary"
            ghost
            icon="plus"
            @click="addItem()"
          ></a-button>
        </a-col>
      </a-row>
    </div>
    <h3 class="mg-t-20 title">Processing Logic</h3>
    <!-- <ProcessorDetail
      ref="ProcessorDetail"
      :hasSave="false"
      type="mapping"
      @processorData:update="$emit('processorData:update', $event)"
      @processor:update="$emit('processor:update', $event)"
    ></ProcessorDetail> -->
    <div class="mg-t-15 mg-b-10 tip-text">关于表达式语法说明，请点击<a href="https://github.com/alibaba/QLExpress" target="_blank" rel="noopener noreferrer"> 链接 </a>了解。</div>
    <a-form-model
      :model="dataProperties"
      :rules="rules"
    >
      <a-form-model-item
        label="Program"
        required
        prop="program_template"
        style="margin-bottom:0"
      >
        <!-- <a-textarea
          rows="10"
          v-model="dataProperties.program_template"
          @change="programChange"
        ></a-textarea> -->
        <MonacoEditor
          class="editor mg-b-10"
          v-model="dataProperties.program_template"
          @change="programChange"
          language="java"
          theme="vs-dark"
          :options="{ automaticLayout: true }"
        />
      </a-form-model-item>
      <a-form-model-item label="">
        <a-button
          class="pull-right"
          :loading="validateLoading"
          :disabled="!dataProperties.program_template"
          @click="validateExpression"
        >Validate</a-button>
      </a-form-model-item>
    </a-form-model>
    <p class="ant-form-item-required">Specify the program variable(s):</p>
    <template>
      <a-row
        class="select-row mg-b-5"
        v-for="variable in dataProperties.expression_variables"
        :key="variable.variable_name"
        :gutter="5"
      >
        <a-col :span="3">{{ variable.variable_name }}</a-col>
        <a-col :span="7">
          <a-select
            v-model="variable.src"
            :options="typeOptions"
            @change="change"
          ></a-select>
        </a-col>
        <a-col :span="7">
          <a-select
            allowClear
            v-if="variable.src"
            v-model="variable.ds_uid"
            :options="toOptions(selectConfig && selectConfig.avail_src_attrs || [], 'ds_name', 'ds_uid')"
            @change="change"
          ></a-select>
        </a-col>
        <a-col :span="7">
          <a-select
            allowClear
            v-if="variable.src"
            v-model="variable.attr_uid"
            :options="getDsAttrsOptions(variable.ds_uid)"
            @change="change"
          ></a-select>
          <a-select
            allowClear
            v-else
            v-model="variable.attr_uid"
            :options="getTargetAttrsOptions()"
            @change="change"
          ></a-select>
        </a-col>
      </a-row>
    </template>

  </div>
</template>

<script>
// import ProcessorDetail from '@/components/StoreCenter/ProcessorDetail'
export default {
  components: { MonacoEditor: () => import('vue-monaco') },
  props: {
    importData: {
      type: Object,
      default () {
        return {}
      }
    },
    extraData: {
      type: Object,
      default () {
        return {}
      }
    },
    initInputTargetAttrs: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      typeOptions: [
        {
          label: 'From Source Attributes',
          value: true
        },
        {
          label: 'From Target Attributes',
          value: false
        }
      ],
      options: [],
      srcAttrs: [],
      selectConfig: null,
      inputTargetAttrs: [],
      dataProperties: {
        program_template: '',
        program: '',
        expression_variables: []
      },
      rules: {
        sqlStatement: [{ requied: true }]
      },
      validateLoading: false
    }
  },
  // watch: {
  //   'dataProperties.expression_variables': {
  //     deep: true,
  //     handler () {

  //       this.dataProperties.program = program
  //     }
  //   }
  // },
  computed: {
    selectTemp () {
      const map = {}
      this.dataProperties.expression_variables.forEach(x => {
        if (x.src) {
          if (x.ds_uid && x.attr_uid) {
            map[`${x.ds_uid}___${x.attr_uid}`] = true
          }
        } else {
          if (x.attr_uid) {
            map[`instance___${x.attr_uid}`] = true
          }
        }
      })
      return map
    }
  },
  created () {
    console.log('this.importData: ', this.importData)
    this.loadTargetAttrs()
    this.loadSrcAttrs()
    this.getAttrMappingSrc()
    if (this.importData && Object.keys(this.importData).length) {
      Object.assign(this.dataProperties, this.importData)
      Object.assign(this.inputTargetAttrs, this.initInputTargetAttrs)
    }
  },
  methods: {
    getProgram () {
      let program = this.dataProperties.program_template
      this.dataProperties.expression_variables.forEach(variable => {
        if (!variable.variable_name) return
        if (variable.src) {
          if (variable.ds_uid && variable.attr_uid) {
            const reg = new RegExp(`@${variable.variable_name}`, 'g')
            // eslint-disable-next-line no-useless-escape
            program = program.replace(reg, `_${variable.ds_uid.replace(/-/g, '')}.get("${variable.attr_uid}")`)
          }
        } else {
          if (variable.attr_uid) {
            const reg = new RegExp(`@${variable.variable_name}`, 'g')
            // eslint-disable-next-line no-useless-escape
            program = program.replace(reg, `instance.get("${variable.attr_uid}")`)
          }
        }
      })
      return program
    },
    getDsAttrsOptions (dsUid) {
      if (!dsUid || !this.selectConfig) return []
      const ds = this.selectConfig.avail_src_attrs.find(x => x.ds_uid === dsUid)
      if (ds && ds.attrs) {
        const attrs = ds.attrs.filter(attr => !this.selectTemp[`${ds.ds_uid}___${attr.uid}`])
        return this.toOptions(attrs)
      } else {
        return []
      }
    },
    getTargetAttrsOptions () {
      if (!this.selectConfig) return []
      const attrs = this.toOptions(this.selectConfig.avail_target_attrs || [], 'target_attr_name', 'target_attr_uid')
      return attrs.filter(attr => !this.selectTemp[`instance___${attr.target_attr_uid}`])
    },
    toOptions (list, label = 'name', value = 'uid') {
      return list.map(x => ({
        label: x[label],
        value: x[value]
      }))
    },
    async validateSql () {
      this.$refs.sql.validate(async valid => {
        if (valid) {
          this.validateLoading = true
          await this.$http({
            url: '/master/plugin/button/db/validate',
            method: 'post',
            data: { sql: this.dataProperties.sqlStatement }
          }).catch(() => (this.validateLoading = false))
          this.validateLoading = false
        }
      })
    },
    async validateExpression () {
      this.validateLoading = true
      await this.$api.validateExpression(this.dataProperties.program).finally(() => (this.validateLoading = false))
    },
    removeItem (index) {
      this.inputTargetAttrs.splice(index, 1)
    },
    addItem () {
      this.inputTargetAttrs.push('')
    },
    validate () {
      if (!this.dataProperties.program_template) {
        this.$message.warning('Operation failed. More info: program is required')
        return false
      }
      this.dataProperties.program = this.getProgram()
      if (!this.dataProperties.program || this.dataProperties.program.indexOf('@') !== -1) {
        this.$message.warning('Operation failed. More info: all select option is required')
        return false
      }
    },
    getVariableTexts (str) {
      return [
        ...new Set(
          str
            .split(/[^\w^@]/)
            .filter(x => /^@\w+$/.test(x))
            .map(x => x.replace('@', ''))
        )
      ]
    },
    programChange () {
      const varTexts = this.getVariableTexts(this.dataProperties.program_template)
      const oldVarNames = this.dataProperties.expression_variables.map(x => x.variable_name)
      varTexts.forEach(text => {
        if (!oldVarNames.includes(text)) {
          this.dataProperties.expression_variables.push({
            variable_name: text,
            src: true,
            ds_uid: '',
            attr_uid: ''
          })
        }
      })
      const variables = []
      this.dataProperties.expression_variables.forEach((varObj, index) => {
        if (varTexts.includes(varObj.variable_name)) {
          variables.push(varObj)
        }
      })
      this.dataProperties.expression_variables = variables
      this.change()
    },
    change () {
      this.dataProperties.program = this.getProgram()
      this.$emit('changeData', this.dataProperties)
      this.$emit('inputTargetAttrs:update', this.inputTargetAttrs)
    },
    async loadTargetAttrs () {
      const resp = await this.$api.getEbTaskTargetAttrs(this.extraData.eb_task_def_uid)
      if (!resp || !resp.object) return
      this.options = resp.object.map(x => ({
        label: x.target_attr_name,
        value: x.target_attr_uid
      }))
    },
    async loadSrcAttrs () {
      const resp = await this.$api.getEbTaskSrcAttrs(this.extraData.eb_task_def_uid)
      if (!resp || !resp.object) return
      this.srcAttrs = resp.object
    },
    async getAttrMappingSrc () {
      const resp = await this.$api.getAttrMappingSrc(this.extraData.eb_task_def_uid)
      if (!resp || !resp.object) return
      this.selectConfig = resp.object
    }
  }
}
</script>

<style lang="less" scoped>
.target-attrs-box {
  width: 60%;
}
/deep/ .ant-select {
  width: 100%;
}
/deep/ .select-row {
  line-height: 32px;
}
.editor {
  width: 100%;
  height: 300px;
}
</style>
