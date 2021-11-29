<template>
  <a-form-model
    :ref="formRefName"
    :rules="rules"
    :model="formData"
    :labelCol="mergeConfig.labelCol"
    :wrapperCol="mergeConfig.wrapperCol"
    :layout="mergeConfig.layout"
    :colon="mergeConfig.colon"
    :labelAlign="mergeConfig.labelAlign"
  >
    <div class="mg-t-20" v-if="tips.length">
      <p class="tip-text" v-for="tip in tips" :key="tip">{{ tip }}</p>
    </div>
    <a-row :gutter="mergeConfig.rowGutter">
      <a-col
        :span="formItem.span || mergeConfig.colSpan"
        :key="formItem.key"
        v-for="formItem in filterFormItems"
      >
        <a-form-model-item
          :label="checkboxTypes.includes(formItem.type) ? ' ' : formItem.label"
          :prop="formItem.key"
          :key="formItem.key"
          :required="formItem.required"
        >
          <span class="form-text" v-if="formItem.type === 'text'">{{ formItem.options && formItem.options.find(x => x.value === formItem.default).label || item[formItem.key] }}</span>
          <template v-else>
            <component
              :style="{height: formItem.type === 'monaco-editor' ? '200px' : 'auto'}"
              v-if="!formItem.arrayNumber"
              :is="formItem.type ? ('a-' + formItem.type) : 'a-input'"
              v-model="formData[formItem.key]"
              v-bind="getAttrs(formItem)"
              v-on="getOn(formItem.on)"
            >{{ checkboxTypes.includes(formItem.type) ? formItem.label : '' }}</component>
            <a-row :gutter="10" v-else>
              <a-col :span="24 / formItem.arrayNumber" v-for="(num, index) in formItem.arrayNumber" :key="index" :class="{ 'center-line': num < formItem.arrayNumber }">
                <component
                  :is="formItem.type ? ('a-' + formItem.type) : 'a-input'"
                  v-model="formData[formItem.key][index]"
                  v-bind="getAttrs(formItem)"
                ></component>
              </a-col>
            </a-row>
          </template>
        </a-form-model-item>
      </a-col>
    </a-row>
  </a-form-model>
</template>

<script>
import StrCheckbox from '@/components/common/StrCheckbox'
import SecretInput from '@/components/common/SecretInput'
const baseConfig = {
  labelCol: { span: 6 },
  wrapperCol: { span: 18 },
  colSpan: 24,
  rowGutter: 10,
  layout: 'horizontal',
  labelAlign: 'right',
  colon: false
}
export default {
  components: { AStrCheckbox: StrCheckbox, ASecretInput: SecretInput, AMonacoEditor: () => import('vue-monaco') },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    item: {
      type: Object,
      default () {
        return null
      }
    },
    value: {
      type: Object,
      default () {
        return {}
      }
    },
    formItems: {
      type: Array,
      default () {
        return [
          {
            label: 'Name',
            key: 'name',
            required: true
          },
          {
            label: 'Description',
            key: 'description',
            type: 'textarea'
          }
        ]
      }
    },
    tips: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      formRefName: Math.random()
        .toString()
        .slice(2),
      formData: {},
      checkboxTypes: ['checkbox', 'str-checkbox']
    }
  },
  computed: {
    filterFormItems () {
      return this.formItems.filter((x) => !x.show || x.show(this.formData))
    },
    filterFormData () {
      const value = {}
      this.formItems.forEach(formItem => {
        if (!formItem.show || formItem.show(this.formData)) {
          value[formItem.key] = this.formData[formItem.key]
        }
      })
      return value
    },
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    },
    rules () {
      const rules = {}
      this.filterFormItems.forEach(formItem => {
        if (formItem.required) {
          rules[formItem.key] = [{ required: true, message: `${formItem.label} is required`, trigger: formItem.trigger }]
        }
      })
      return rules
    }
  },
  watch: {
    formData: {
      deep: true,
      handler () {
        this.$emit('input', this.filterFormData)
      }
    },
    item: {
      deep: true,
      immediate: true,
      handler () {
        this.initValue()
      }
    }
  },
  methods: {
    getOn (on) {
      if (!on) return {}
      const newOn = {}
      Object.keys(on).forEach(key => {
        newOn[key] = (e) => { on[key](e, this.formData) }
      })
      return newOn
    },
    getAttrs (atts) {
      const obj = {
        ...atts
      }
      const delKeys = ['label', 'key', 'type', 'arrayNumber', 'default']
      delKeys.forEach(key => {
        delete obj[key]
      })
      Object.keys(obj).forEach(key => {
        if (typeof obj[key] === 'function') {
          obj[key] = obj[key](this.formData)
        }
      })
      return obj
    },
    initValue () {
      this.formItems.forEach(formItem => {
        let itemValue = this.item && this.item[formItem.key] !== null && this.item[formItem.key] !== undefined ? this.item[formItem.key] : formItem.default
        if (formItem.arrayNumber && itemValue === undefined) {
          itemValue = []
        }
        this.$set(this.formData, formItem.key, itemValue)
      })
      this.$emit('input', this.filterFormData)
    },
    validate () {
      let isValid = false
      this.$refs[this.formRefName].validate(valid => {
        isValid = valid
      })
      return isValid
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .center-line:after {
  position: absolute;
  width: 10px;
  content: '~';
  text-align: center;
  float: left;
}
</style>
