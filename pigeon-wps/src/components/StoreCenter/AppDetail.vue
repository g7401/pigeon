<template>
  <div>
    <h3>Authorize the APIs</h3>
    <a-row :gutter="15">
      <a-tabs
        size="small"
        v-model="activeTab"
      >
        <a-tab-pane
          v-for="tab in tabs"
          :key="tab.key"
          :tab="tab.label"
        >
          <a-table
            class="more-small-table"
            size="small"
            bordered
            rowKey="uid"
            :pagination="false"
            :dataSource="getItem(tab.key).fields"
            :columns="columns"
            :loading="fieldLoading"
            :scroll="{ x: 350, y: '60vh' }"
            :row-selection="{ selectedRowKeys: getItem(tab.key).selectedRowKeys, onChange: ($event) => onSelectChange($event, getItem(tab.key)), getCheckboxProps: getCheckboxProps }"
          ></a-table>
        </a-tab-pane>
      </a-tabs>
    </a-row>
  </div>
</template>

<script>
import debounce from 'lodash.debounce'
export default {
  props: {
    appId: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      activeTab: 'PRODUCT',
      fieldsMap: {},
      fields: [],
      tabs: [
        {
          label: 'Product General Query API',
          key: 'PRODUCT'
        },
        {
          label: 'Product Identity Query API',
          key: 'PRODUCT_ID_MAPPING'
        }
      ],
      columns: [
        {
          dataIndex: 'name',
          title: 'Field Name',
          width: 150
        },
        {
          dataIndex: 'description',
          title: 'Field Description',
          width: 150
        }
      ],
      fieldLoading: false
    }
  },
  computed: {
    formData () {
      return Object.keys(this.fieldsMap).map(key => ({
        api_key: key,
        fields: this.fieldsMap[key]?.fields
      }))
    }
  },
  watch: {
    formData: {
      immediate: true,
      deep: true,
      handler () {
        this.$emit('change', this.formData)
      }
    },
    activeTab: {
      immediate: true,
      handler () {
        this.loadAppFields()
      }
    },
    appId () {
      this.fieldsMap = {}
      this.loadAppFields()
    }
  },
  created () {
    // this.initValue()
    // this.loadAppFields()
    this.loadAppFields = debounce(this.loadAppFields, 500)
  },
  methods: {
    getItem (key) {
      const item = this.fieldsMap[key]
      if (item) return item
      return {
        fields: [],
        selectedRowKeys: []
      }
    },
    async loadAppFields () {
      if (this.fieldsMap[this.activeTab] || !this.appId) {
        return
      }
      this.fieldLoading = true
      const resp = await this.$api.getAppFields(this.appId, this.activeTab).finally(() => {
        this.fieldLoading = false
      })
      if (!resp || !resp.object) return
      this.$set(this.fieldsMap, this.activeTab, {
        fields: resp.object,
        selectedRowKeys: resp.object.filter(x => x.is_selected).map(x => x.uid)
      })
    },
    onSelectChange (selectedRowKeys, item) {
      item.selectedRowKeys = selectedRowKeys
      item.fields.forEach(field => {
        field.is_selected = selectedRowKeys.includes(field.uid)
      })
    },
    getCheckboxProps (record) {
      return {
        props: {
          disabled: this.disabled
        }
      }
    }
  }
}
</script>

<style>
</style>
