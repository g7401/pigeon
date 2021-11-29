<template>
  <a-form :form="form">
    <a-row>
      <a-col :xs="{ span: 24 }" :lg="{ span: 12 }">
        <a-form-item
          :required="true"
          label="值项"
          :labelCol="{ lg: { span: 8 }, sm: { span: 8 } }"
          :wrapperCol="{ lg: { span: 16 }, sm: { span: 16 } }"
        >
          <a-select
            v-decorator="['dotValue1Field', { rules: [{ required: true }] }]"
            :options="fieldColums"
            :showSearch="true"
            :filter-option="filterOption"
          ></a-select>
        </a-form-item>
      </a-col>
      <a-col :xs="{ span: 24 }" :lg="{ span: 12 }">
        <a-form-item
          :required="true"
          label="值项"
          :labelCol="{ lg: { span: 8 }, sm: { span: 8 } }"
          :wrapperCol="{ lg: { span: 16 }, sm: { span: 16 } }"
        >
          <a-select
            v-decorator="['dotValue2Field', { rules: [{ required: true }] }]"
            :options="fieldColums"
            :showSearch="true"
            :filter-option="filterOption"
          ></a-select>
        </a-form-item>
      </a-col>
    </a-row>
    <a-row>
      <a-col :xs="{ span: 24 }" :lg="{ span: 12 }">
        <a-form-item
          label="点尺寸项(可选)"
          :labelCol="{ lg: { span: 8 }, sm: { span: 8 } }"
          :wrapperCol="{ lg: { span: 16 }, sm: { span: 16 } }"
        >
          <a-select
            v-decorator="['dotSizeField']"
            :options="fieldColums"
            :showSearch="true"
            :filter-option="filterOption"
            :allowClear="true"
          ></a-select>
        </a-form-item>
      </a-col>
      <a-col :xs="{ span: 24 }" :lg="{ span: 12 }">
        <a-form-item
          label="系列项(可选)"
          :labelCol="{ lg: { span: 8 }, sm: { span: 8 } }"
          :wrapperCol="{ lg: { span: 16 }, sm: { span: 16 } }"
        >
          <a-select
            v-decorator="['dotSeriesField']"
            :options="fieldColums"
            :showSearch="true"
            :filter-option="filterOption"
            :allowClear="true"
          ></a-select>
        </a-form-item>
      </a-col>
    </a-row>
  </a-form>
</template>

<script>
export default {
  name: 'ScatterParams',
  props: {
    filterOption: {
      type: Function,
      required: true
    },
    fieldColums: {
      type: Array,
      required: true
    },
    validateFields: {
      type: Function,
      required: true
    },
    groupBy: {
      type: Function,
      required: true
    }
  },
  data () {
    return {
      form: this.$form.createForm(this)
    }
  },
  methods: {
    init () {
      this.form.resetFields()
    },
    getFieldsValue () {
      return this.form.getFieldsValue()
    },
    setFieldsValue (value) {
      this.form.setFieldsValue(value)
    },
    getOptions (options, datas, dimensions) {
      const value = this.getFieldsValue()
      let datasGroups
      if (value.dotSeriesField) {
        datasGroups = this.groupBy(datas, value.dotSeriesField)
      } else {
        datasGroups = { undefined: datas }
      }
      options.dataset = []
      options.series = []
      options.xAxis = {}
      options.yAxis = {}
      let i = 0
      for (var key in datasGroups) {
        const data = {}
        data.dimensions = dimensions
        data.source = datasGroups[key]
        options.dataset.push(data)
        const serie = { type: 'scatter', encode: {}, datasetIndex: i }
        if (key !== 'undefined') serie.name = key
        // dot
        serie.encode.x = value.dotValue1Field
        serie.encode.y = value.dotValue2Field
        if (value.dotSize) serie.symbolSize = data => data[value.dotSizeField]
        options.tooltip.formatter = function (param, ticket, callback) {
          return param.data[value.dotValue1Field] + ',' + param.data[value.dotValue2Field]
        }
        options.series.push(serie)
        i++
      }
    }
  }
}
</script>
