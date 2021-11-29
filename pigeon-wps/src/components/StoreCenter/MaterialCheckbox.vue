<template>
  <div>
    <a-checkbox class="mg-t-20 mg-b-10" v-model="isApply" @change="handleChange">Automatically applied to all material codes that belong to the same product.</a-checkbox>
    <a-table
      v-if="isApply"
      class="more-small-table"
      rowKey="MATERIAL_CODE"
      bordered
      :pagination="false"
      size="small"
      :columns="columns"
      :dataSource="materials"
      :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
    ></a-table>
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Array,
      default () {
        return []
      }
    },
    materialCode: {
      type: String,
      default: ''
    },
    uomId: {
      type: Number,
      default: null
    }
  },
  data () {
    return {
      loaded: false,
      selectedRowKeys: [],
      materials: [],
      columns: [
        {
          title: 'Material Code',
          dataIndex: 'MATERIAL_CODE'
        },
        {
          title: 'Material Name',
          dataIndex: 'MATERIAL_NAME'
        },
        {
          title: 'Mapping Method',
          dataIndex: 'MAPPING_METHOD'
        },
        {
          title: 'Mapping Value',
          dataIndex: 'MAPPING_VALUE'
        }
      ]
    }
  },
  computed: {
    isApply: {
      get () {
        return this.value.length > 0
      },
      set (val) {
        if (val) {
          this.selectedRowKeys = this.materials.map(x => x.MATERIAL_CODE)
        } else {
          this.selectedRowKeys = []
        }
      }
    }
  },
  watch: {
    selectedRowKeys: {
      deep: true,
      handler () {
        this.$emit('input', this.materials.filter(x => this.selectedRowKeys.includes(x.MATERIAL_CODE)))
      }
    },
    materialCode (val) {
      if (!val || this.uomId) return
      this.getAllMaterial()
    },
    uomId: {
      immediate: true,
      handler (val) {
        val && this.getSelectedMeterials()
      }
    }
  },
  created () {
  },
  methods: {
    handleChange () {
      if (!this.materialCode) {
        this.$message.info('Operation failed. More info: no material code')
        return
      }
      if (!this.loaded) {
        this.$message.info('Operation failed. More info: loading, pleace wait')
        return
      }
      if (this.loaded && !this.materials.length) {
        this.$message.info('Operation failed. More info: no content')
      }
    },
    onSelectChange (selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    async getAllMaterial () {
      this.loaded = false
      const resp = await this.$api.getAllMaterials(this.materialCode)
      if (!resp || !resp.object) return
      this.materials = resp.object
      this.loaded = true
      this.selectedRowKeys = []
    },
    async getSelectedMeterials () {
      const resp = await this.$api.getSelectedMeterials(this.uomId)
      if (!resp || !resp.object) return
      this.selectedRowKeys = resp.object.filter(x => x.SELECTED).map(x => x.MATERIAL_CODE)
      const materials = resp.object.slice()
      materials.forEach(x => {
        delete x.SELECTED
      })
      this.materials = materials
    }
  }
}
</script>

<style>
</style>
