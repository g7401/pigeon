<template>
  <a-form :form="form">
    <a-row>
      <a-col :xs="{ span: 24 }" :lg="{ span: 12 }">
        <a-form-item
          :required="true"
          label="类目项"
          :labelCol="{ lg: { span: 8 }, sm: { span: 8 } }"
          :wrapperCol="{ lg: { span: 16 }, sm: { span: 16 } }"
        >
          <a-select
            v-decorator="['categoryField', { rules: [{ required: true }] }]"
            :options="fieldColums"
            :showSearch="true"
            :filter-option="filterOption"
          ></a-select>
        </a-form-item>
      </a-col>
      <a-col :xs="{ span: 24 }" :lg="{ span: 12 }">
        <a-form-item
          :required="true"
          label="类目位置"
          :labelCol="{ lg: { span: 8 }, sm: { span: 8 } }"
          :wrapperCol="{ lg: { span: 16 }, sm: { span: 16 } }"
        >
          <a-radio-group v-decorator="['categoryLocation', { initialValue: 'x' }]">
            <a-radio value="x">X轴</a-radio>
            <a-radio value="y">Y轴</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-col>
    </a-row>
    <a-row>
      <a-col :xs="{ span: 24 }" :lg="{ span: 12 }">
        <a-form-item
          :required="true"
          label="数值项"
          :labelCol="{ lg: { span: 8 }, sm: { span: 8 } }"
          :wrapperCol="{ lg: { span: 16 }, sm: { span: 16 } }"
        >
          <a-select
            v-decorator="['valueField', { rules: [{ required: true }] }]"
            :options="fieldColums"
            :showSearch="true"
            :filter-option="filterOption"
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
            v-decorator="['seriesField']"
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
  name: 'BarOrLineParams',
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
    },
    chartType: {
      type: String,
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
      if (value.seriesField) {
        datasGroups = this.groupBy(datas, value.seriesField)
      } else {
        datasGroups = { undefined: datas }
      }
      options.dataset = []
      options.series = []
      let i = 0
      let maxCount = 0
      for (var key in datasGroups) {
        const data = {}
        data.dimensions = dimensions
        data.source = datasGroups[key]
        if (data.source.length > maxCount) maxCount = data.source.length
        options.dataset.push(data)
        const serie = { type: this.chartType, encode: {}, datasetIndex: i }
        if (key !== 'undefined') serie.name = key
        // bar or line
        if (value.categoryLocation === 'x') {
          serie.encode.x = [value.categoryField]
          serie.encode.y = [value.valueField]
        } else {
          options.yAxis.type = 'category'
          options.xAxis.type = 'value'
          options.yAxis.axisLabel = {}
          options.yAxis.axisLabel.interval = 0
          serie.encode.y = [value.categoryField]
          serie.encode.x = [value.valueField]
        }
        // 数字长时会有重叠问题
        // serie.itemStyle = { normal: { label: { show: true, position: 'top' } } }
        options.series.push(serie)
        i++
      }
      if (value.categoryLocation === 'x') {
        options.xAxis.type = 'category'
        options.yAxis.type = 'value'
        options.xAxis.axisLabel = {}
        options.xAxis.axisLabel.interval = 0
        options.xAxis.axisLabel.rotate = maxCount > 15 ? 40 : maxCount >= 6 ? 20 : 0
      } else {
        options.yAxis.type = 'category'
        options.xAxis.type = 'value'
        options.yAxis.axisLabel = {}
        options.yAxis.axisLabel.interval = 0
      }
    }
  }
}
</script>
