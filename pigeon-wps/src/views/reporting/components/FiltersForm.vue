<template>
  <div class="fliter-list" v-show="show">
    <a-button type="primary" icon="plus-circle" @click="addFilter" />
    <div style="line-height:40px">
      <span style="color:red">可点击按钮添加过滤条件筛选数据,如不需过滤也可以直接刷新数据</span>
    </div>
    <a-form :form="filterForm">
      <a-row :gutter="12" style="line-height:40px" v-for="(k, index) in filters" :key="index">
        <a-col :span="5">
          <a-form-item>
            <a-select
              :options="allFieldColums"
              v-decorator="[filters[index]['id'], { rules: [{ required: true }] }]"
              :showSearch="true"
              :filter-option="filterOption"
            ></a-select>
          </a-form-item>
        </a-col>
        <a-col :span="5">
          <a-form-item>
            <a-select
              :options="filteroperations"
              v-decorator="['operation_' + filters[index]['num'], { rules: [{ required: true }] }]"
            ></a-select>
          </a-form-item>
        </a-col>
        <a-col :span="13">
          <a-form-item>
            <a-input
              v-decorator="['value_' + filters[index]['num'], { rules: [{ required: true }] }]"
              :showSearch="true"
              :filter-option="filterOption"
              placeholder="请输入过滤值，如有多个值请用 ， 隔开"
              allow-clear
            />
          </a-form-item>
        </a-col>
        <a-col :span="1">
          <a-button icon="minus-circle" @click="removeFilter(index)" />
        </a-col>
      </a-row>
    </a-form>
    <a-button type="primary" icon="reload" @click="reloadDatas">刷新数据</a-button>
  </div>
</template>

<script>
import { get } from '../api/api'
export default {
  name: 'FilterForm',
  props: {
    allFieldColums: {
      type: Array,
      required: true
    },
    show: {
      type: Boolean,
      required: true,
      default: false
    },
    filterOption: {
      type: Function,
      required: true
    },
    validateFields: {
      type: Function,
      required: true
    }
  },
  data () {
    return {
      filters: [],
      filterForm: this.$form.createForm(this),
      filteroperations: []
    }
  },
  methods: {
    init () {
      for (let i = 0, len = this.filters.length; i < len; i++) {
        this.removeFilter(0)
      }
      this.filterForm.resetFields()
    },
    reloadFilter (filters) {
      const values = {}
      for (let index = 0, len = filters.length; index < len; index++) {
        const element = filters[index]
        this.addFilter()
        values['filter_' + index] = element.field
        values['operation_' + index] = element.operator
        values['value_' + index] = element.value
      }
      setTimeout(() => {
        this.filterForm.setFieldsValue(values)
      }, 500)
    },
    addFilter () {
      let maxNum = 0
      if (this.filters.length >= 1) {
        const item = this.filters[this.filters.length - 1]
        maxNum = parseInt(item.id.replace('filter_', '')) + 1
      }
      this.filterForm.getFieldDecorator('filter_' + maxNum, {
        initialValue: '',
        preserve: true
      })
      this.filters = this.filters.concat({ id: 'filter_' + maxNum, num: maxNum })
    },
    removeFilter (index) {
      const deleteObj = this.filters.splice(index, 1)
      const deleteItem = deleteObj[0]
      const num = deleteItem.num
      this.filterForm.getFieldDecorator([deleteItem.id], {
        required: false
      })
      this.filterForm.getFieldDecorator(['operation_' + num], {
        required: false
      })
      this.filterForm.getFieldDecorator(['value_' + num], {
        required: false
      })
      this.filterForm.setFieldsValue({
        [deleteItem.id]: '',
        ['operation_' + num]: '',
        ['value_' + num]: ''
      })
    },
    reloadDatas () {
      if (!this.validateFields(this.filterForm)) return false
      var formValue = this.filterForm.getFieldsValue()
      let filters = []
      for (var key in formValue) {
        if (key.indexOf('filter_') >= 0 && formValue[key]) {
          const value = {}
          value.field = formValue[key]
          const num = parseInt(key.replace('filter_', ''))
          value.operator = formValue['operation_' + num]
          value.value = formValue['value_' + num]
          filters = [...filters, value]
        }
      }
      this.$emit('reloadDatas', 0, 10, filters)
    }
  },
  async mounted () {
    const response = await get('query/operation/', null)
    if (response.success) {
      this.filteroperations = response.obj
    } else {
      console.log('err:' + response.msg)
    }
  }
}
</script>
