<template>
  <div>
    <a-form-model
      :colon="false"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 12 }"
      :model="form"
      ref="dataForm"
    >
      <a-form-model-item
        required
        label="Database"
        prop="sourceDatabase"
      >
        <a-input
          @change="change"
          v-model="form.sourceDatabase"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        prop="sourceTable"
        label="Table"
      >
        <a-input
          @change="change"
          v-model="form.sourceTable"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        prop="fetchingColumnsMode"
        label="Fetching Columns"
      >
        <a-radio-group
          @change="change"
          v-model="form.fetchingColumnsMode"
        >
          <a-radio value="ALL">
            All Columns
          </a-radio>
          <a-radio value="SPECIFIC">
            Specific Columns
          </a-radio>
        </a-radio-group>
        <a-input
          v-if="form.fetchingColumnsMode === 'SPECIFIC'"
          @change="change"
          v-model="form.fetchingColumns"
          placeholder="用半角逗号分隔列"
        />
      </a-form-model-item>
      <a-form-model-item
        required
        prop="fetchingMode"
        label="Fetching Mode"
      >
        <a-radio-group
          @change="change"
          v-model="form.fetchingMode"
        >
          <a-radio value="FULL">
            Full
          </a-radio>
          <a-radio value="INCREMENTAL">
            Incremental
          </a-radio>
        </a-radio-group>
        <template v-if="form.fetchingMode === 'INCREMENTAL'">
          <a-form-model-item
            prop="timestampColumn"
            label="Incremental Control Column"
            :label-col="{ span: 8 }"
            labelAlign="left"
          >
            <a-input
              @change="change"
              v-model="form.timestampColumn"
              placeholder="指定1个时间戳列"
            />
          </a-form-model-item>
          <a-form-model-item
            label="Initial Watermark Value"
            :label-col="{ span: 8 }"
            labelAlign="left"
          >
            <a-input
              @change="change"
              v-model="form.initialWatermarkValue"
              placeholder="初始水位值，即第1次从哪个位置取数"
            />
          </a-form-model-item>
          <a-form-model-item
            label="Current Watermark Value"
            :label-col="{ span: 8 }"
            labelAlign="left"
          >
            <a-input
              @change="change"
              v-model="form.currentWatermarkValue"
              placeholder="当前水位值，即最近一次完成取数到了哪个位置"
            />
          </a-form-model-item>
        </template>
      </a-form-model-item>
      <!-- <a-form-model-item
        required
        prop="deleteMode"
        label="Delete Mode"
      >
        <a-radio-group
          @change="change"
          v-model="form.deleteMode"
        >
          <a-radio value="physicalDelete">
            Physical Delete
          </a-radio>
          <a-radio value="logicalDelete">
            Logical Delete
          </a-radio>
        </a-radio-group>
        <a-input
          @change="change"
          v-if="form.deleteMode"
          v-model="form.logicalDeleteColumn"
          placeholder="指定1个逻辑删除标志列，例如：is_deleted"
        />
      </a-form-model-item> -->
      <a-form-model-item
        required
        prop="orderByColumns"
        label="Order By Columns"
      >
        <a-input
          @change="change"
          v-model="form.orderByColumns"
          placeholder="用半角逗号分隔列，例如：id asc, test desc"
        />
      </a-form-model-item>
    </a-form-model>
  </div>
</template>

<script>
export default {
  props: {
    importData: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      form: {
        fetchingMode: '',
        orderByColumns: '',
        fetchingColumnsMode: '',
        sourceDatabase: '',
        sourceTable: '',
        fetchingColumns: '',
        timestampColumn: '',
        initialWatermarkValue: '',
        currentWatermarkValue: ''
      },
      rules: {
        fetchingMode: [{ requied: true }],
        orderByColumns: [{ requied: true }],
        fetchingColumnsMode: [{ requied: true }],
        sourceDatabase: [{ requied: true }],
        sourceTable: [{ requied: true }],
        fetchingColumns: [{ requied: true }]
      },
      conLoading: false
    }
  },
  created () {
    console.log('this.importData: ', this.importData)
    Object.assign(this.form, this.importData)
  },
  methods: {
    validate () {
      let isValid = false
      this.$refs.dataForm.validate(valid => {
        isValid = valid
      })
      return isValid
    },
    change () {
      const formData = Object.assign({}, this.form)
      this.$emit('changeData', formData)
    }
  }
}
</script>
