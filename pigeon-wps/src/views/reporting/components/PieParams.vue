<template>
  <a-form :form="form">
    <a-row>
      <a-col
        :xs="{ span: 24 }"
        :lg="{ span: 12 }"
      >
        <a-form-item
          :required="true"
          label="类目项"
          :labelCol="{ lg: { span: 8 }, sm: { span: 8 } }"
          :wrapperCol="{ lg: { span: 16 }, sm: { span: 16 } }"
        >
          <a-select
            v-decorator="['pieCategoryField', { rules: [{ required: true }] }]"
            :options="fieldColums"
            :showSearch="true"
            :filter-option="filterOption"
          ></a-select>
        </a-form-item>
      </a-col>
      <a-col
        :xs="{ span: 24 }"
        :lg="{ span: 12 }"
      >
        <a-form-item
          :required="true"
          label="值项"
          :labelCol="{ lg: { span: 8 }, sm: { span: 8 } }"
          :wrapperCol="{ lg: { span: 16 }, sm: { span: 16 } }"
        >
          <a-select
            v-decorator="['pieValueField', { rules: [{ required: true }] }]"
            :options="fieldColums"
            :showSearch="true"
            :filter-option="filterOption"
          ></a-select>
        </a-form-item>
      </a-col>
    </a-row>
  </a-form>
</template>

<script>
export default {
  name: 'PieParams',
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
      if (!this.validateFields(this.form)) return false
      const value = this.getFieldsValue()
      delete options.xAxis
      delete options.yAxis
      options.legend.orient = 'vertical'
      options.dataset = [
        {
          dimensions: dimensions,
          source: datas
        }
      ]
      options.series = [
        {
          type: 'pie',
          encode: {
            itemName: value.pieCategoryField,
            value: value.pieValueField
          },
          center: ['40%', '50%'],
          datasetIndex: 0,
          itemStyle: {
            normal: {
              label: {
                show: true,
                position: 'top',
                formatter: function (e) {
                  var newStr = ' '
                  var newStr2 = ' '
                  var start, end
                  var nameLen = e.name.length // 每个内容名称的长度
                  var maxName = 8 // 每行最多显示的字数，超过八个字换行处理
                  var newRow = Math.ceil(nameLen / maxName) // 最多能显示几行，向上取整比如2.1就是3行
                  if (nameLen > maxName) {
                    // 如果长度大于每行最多显示的字数
                    for (var i = 0; i < newRow; i++) {
                      // 循环次数就是行数
                      var old = '' // 每次截取的字符
                      start = i * maxName // 截取的起点
                      end = start + maxName // 截取的终点
                      if (i === newRow - 1) {
                        // 最后一行就不换行了
                        old = e.name.substring(start)
                      } else {
                        old = e.name.substring(start, end) + '\n'
                      }
                      newStr += old // 拼接字符串
                    }
                  } else {
                    // 如果小于每行最多显示的字数就返回原来的字符串
                    newStr = e.name
                  }
                  newStr2 += e.percent + '% ' + newStr // 添加百分号处理
                  return newStr2
                }
              }
            }
          }
        }
      ]
    }
  }
}
</script>
