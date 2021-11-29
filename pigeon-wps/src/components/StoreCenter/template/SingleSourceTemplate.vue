<template>
  <div>
    <h3 class="title">Input Attribute(s) Coming From Source Attributes</h3>
    <a-form-model-item label="Select One as Single Source of Truth">
      <a-select
        class="single-select"
        placeholder="Select DataSource Attribute"
        v-model="selectedValue"
        :options="options"
        @change="change"
      ></a-select>
    </a-form-model-item>
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
    },
    extraData: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      dataProperties: {
        truth_data_source_uid: '',
        truth_data_source_attr: ''
      },
      selectedValue: undefined,
      options: [],
      connection: '___'
    }
  },
  created () {
    this.loadSrcAttrs()
    this.initData()
  },
  methods: {
    initData () {
      if (this.importData && Object.keys(this.importData).length) {
        Object.assign(this.dataProperties, this.importData)
        if (this.dataProperties.truth_data_source_uid && this.dataProperties.truth_data_source_attr) {
          this.selectedValue = `${this.dataProperties.truth_data_source_uid}${this.connection}${this.dataProperties.truth_data_source_attr}`
        }
      }
    },
    change (item) {
      if (this.selectedValue) {
        const [ds, attr] = this.selectedValue.split(this.connection)
        this.dataProperties['truth_data_source_uid'] = ds
        this.dataProperties['truth_data_source_attr'] = attr
      }
      console.log('this.dataProperties', this.dataProperties)
      this.$emit('changeData', this.dataProperties)
    },
    async loadSrcAttrs () {
      const resp = await this.$api.getEbTaskSrcAttrs(this.extraData.eb_task_def_uid)
      if (!resp || !resp.object) return
      this.options = resp.object.map(x => ({
        label: `${x.ds_name}, ${x.src_attr_name}`,
        value: `${x.ds_uid}${this.connection}${x.src_attr_uid}`,
        key: `${x.ds_uid}${this.connection}${x.src_attr_uid}`
      }))
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .single-select {
    width: 400px;
  }
</style>
