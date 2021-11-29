<template>
  <div
    class="base-form"
    v-if="value"
  >
    <a-modal
      :title="title"
      :ok-text="mergeConfig.okText"
      :cancel-text="mergeConfig.cancelText"
      :visible="value"
      :confirm-loading="mergeConfig.confirmLoading"
      :width="mergeConfig.width"
      @ok="handleOk"
      @cancel="close"
    >
      <template>
        <a-row :gutter="10">
          <a-col :span="16">
            <base-form
              :ref="formRefName"
              v-model="formData"
              :item="item"
              :formItems="formItems"
              :config="{ labelCol: { span: 9 }, wrapperCol: { span: 15 } }"
            ></base-form>
          </a-col>
          <a-col :span="8">
            <a-descriptions
              title="Reference"
              :colon="false"
              :column="1"
              layout="vertical"
              size="small"
              :bordered="!!filterRadios.length"
            >
              <a-descriptions-item
                :label="getRatioLabel(ratioItem)"
                v-for="ratioItem in filterRadios"
                :key="ratioItem.uom_localized_name"
              >
                = <span class="ratio-text">{{ ratioItem.ratio.toFixed(2) }}</span>
              </a-descriptions-item>
            </a-descriptions>
            <a-icon
              type="loading"
              v-show="ratioLoading"
            />
          </a-col>
        </a-row>
        <MaterialCheckbox
          v-model="selectedMeterials"
          :materialCode="formData.material_code"
          :uomId="item ? item.id : undefined"
        ></MaterialCheckbox>
      </template>
    </a-modal>
  </div>
</template>

<script>
import BaseForm from '@/components/common/BaseForm'
import MaterialCheckbox from '@/components/StoreCenter/MaterialCheckbox'
import debounce from 'lodash.debounce'
const baseConfig = {
  okText: 'Save',
  cancelText: 'Cancel',
  width: 800,
  confirmLoading: false
}
export default {
  components: { BaseForm, MaterialCheckbox },
  props: {
    config: {
      type: Object,
      default () {
        return {}
      }
    },
    title: {
      type: String,
      default: 'Create Custom UOM'
    },
    item: {
      type: Object,
      default () {
        return null
      }
    },
    value: {
      type: Boolean,
      default: false
    },
    apiName: {
      type: String,
      default: 'createCustomUom'
    },
    extraFields: {
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
      ratioLoading: false,
      ratios: [],
      selectedMeterials: []
    }
  },
  computed: {
    formItems () {
      return [
        {
          label: 'Enabled',
          key: 'status',
          type: 'str-checkbox',
          checkedValue: 'Enabled',
          uncheckedValue: 'Disabled',
          default: 'Enabled'
        },
        {
          label: 'Material Code',
          key: 'material_code',
          required: true,
          disabled: this.item && this.item.id
        },
        {
          label: 'UOM Localized Name',
          key: 'uom_localized_name',
          required: true
        },
        {
          label: 'UOM Native Name',
          key: 'uom_native_name',
          required: true
        },
        {
          label: 'Numerator',
          key: 'numerator',
          type: 'input-number',
          required: true
        },
        {
          label: 'Denominator',
          key: 'denominator',
          type: 'input-number',
          required: true
        },
        {
          label: 'Barcode',
          key: 'barcode'
        },
        {
          label: 'Length',
          key: 'length',
          type: 'input-number'
        },
        {
          label: 'Width',
          key: 'width',
          type: 'input-number'
        },
        {
          label: 'Height',
          key: 'height',
          type: 'input-number'
        },
        {
          label: 'Unit of Length/Width/Height',
          key: 'unit_of_length_or_width_or_height'
        },
        {
          label: 'Volume',
          key: 'volume',
          type: 'input-number'
        },
        {
          label: 'Unit of Volume',
          key: 'unit_of_volume'
        },
        {
          label: 'Gross Weight',
          key: 'gross_weight',
          type: 'input-number'
        },
        {
          label: 'Unit of Gross Weight',
          key: 'unit_of_gross_weight'
        }
      ]
    },
    mergeConfig () {
      return { ...baseConfig, ...this.config }
    },
    filterRadios () {
      if (!this.item) return this.ratios
      return this.ratios.filter(x => x.uom_native_name !== this.item.uom_native_name)
    }
  },
  watch: {
    'formData.numerator': {
      immediate: true,
      handler () {
        this.ratios = []
        this.getRatios()
      }
    },
    'formData.denominator': {
      immediate: true,
      handler () {
        this.ratios = []
        this.getRatios()
      }
    }
  },
  created () {
    this.getRatios = debounce(this.getRatios, 500)
  },
  methods: {
    async save () {
      const extraValues = this.extraFields.map(field => this.item[field])
      const requestData = { ...this.formData, matching_material_codes: this.selectedMeterials }
      return this.$api[this.apiName](...extraValues, requestData)
    },
    async handleOk () {
      const isValid = this.$refs[this.formRefName].validate()
      if (!isValid) return
      this.config.confirmLoading = true
      this.save()
        .then(() => {
          this.$emit('saved')
          this.close()
        })
        .finally(() => {
          this.config.confirmLoading = false
        })
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
      this.formData = {}
    },
    async getRatios () {
      if (!this.formData.numerator || !this.formData.denominator) return
      this.ratios = []
      this.ratioLoading = true
      // eslint-disable-next-line no-unused-vars
      const resp = await this.$api
        .getMaterialRatios(this.formData.material_code, this.formData.numerator, this.formData.denominator)
        .finally(() => {
          this.ratioLoading = false
        })
      if (!resp && !resp.object) return
      this.ratios = resp.object || []
    },
    getRatioLabel (ratioItem) {
      return `${ratioItem.uom_native_name || ratioItem.uom_localized_name}/${this.formData.uom_localized_name} = (${
        ratioItem.numerator
      }/${ratioItem.denominator})/(${this.formData.numerator}/${this.formData.denominator})`
    }
  }
}
</script>

<style lang="less" scoped>
.ratio-text {
  font-weight: 500;
  color: #87d068;
}
/deep/ .ant-form-item {
  margin-bottom: 0px;
}
</style>
