<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <DictStrategyModal
      v-if="dictStrategyVisiable && activeDict"
      :domain="activeDict.field_domain"
      :item="activeDict.field_item"
      v-model="dictStrategyVisiable"
      @saved="handleSavedStrategy"
    ></DictStrategyModal>
    <a-collapse
      expandIconPosition="right"
    >
      <a-collapse-panel
        key="1"
        header="Filter"
      >
        <a-row
          type="flex"
          justify="space-between"
        >
          <a-col :span="18">
            <a-form-model
              layout="vertical"
              :colon="false"
              :model="params"
              :labal-col="{ span: 6 }"
            >
              <a-row :gutter="24">
                <a-col :span="4">
                  <a-form-model-item label="Domain">
                    <a-input v-model="params.field_domain" />
                  </a-form-model-item>
                </a-col>
                <a-col :span="4">
                  <a-form-model-item label="Item">
                    <a-input v-model="params.field_item" />
                  </a-form-model-item>
                </a-col>
                <a-col :span="4">
                  <a-form-model-item label="Value">
                    <a-input v-model="params.field_value" />
                  </a-form-model-item>
                </a-col>
              </a-row>
            </a-form-model>
          </a-col>
          <a-col :span="6">
            <div class="search-btn-group">
              <a-button
                type="primary"
                icon="search"
                :loading="queryLoading"
                @click="loadTableData"
                style="margin-right: 5px"
              >Search</a-button>
              <a-button
                icon="reload"
                :loading="queryLoading"
                @click="reset"
              >Reset</a-button>
            </div>
          </a-col>
        </a-row>
      </a-collapse-panel>
    </a-collapse>
    <a-row
      class="op-box"
      type="flex"
      justify="end"
    >
      <a-button
        class="op-btn"
        type="primary"
        @click="addDict"
      >Add</a-button>
      <a-button
        class="op-btn"
        type="primary"
        :loading="saveLoading"
        @click="saveDict"
      >Save</a-button>
    </a-row>
    <a-table
      bordered
      class="more-small-table"
      rowKey="field_item"
      :columns="columns"
      :data-source="dictList"
      :loading="queryLoading"
      :pagination="pagination"
      size="small"
      :expandedRowKeys="expandedRowKeys"
      :customRow="customRow"
      :rowClassName="rowClassName"
      @change="handleTableChange"
      @expandedRowsChange="expandedRowsChange"
    >
      <div
        slot="filterDropdown"
        slot-scope="{ setSelectedKeys, selectedKeys, confirm, clearFilters, column }"
        style="padding: 8px"
      >
        <a-input
          v-ant-ref="c => (searchInput = c)"
          :placeholder="`Search ${column.title}`"
          :value="selectedKeys[0]"
          style="width: 188px; margin-bottom: 8px; display: block;"
          @change="e => setSelectedKeys(e.target.value ? [e.target.value] : [])"
          @pressEnter="() => handleSearch(selectedKeys, confirm, column.title)"
        />
        <a-button
          type="primary"
          icon="search"
          size="small"
          style="width: 90px; margin-right: 8px"
          @click="() => handleSearch(selectedKeys, confirm, column.dataIndex)"
        >
          Search
        </a-button>
        <a-button
          size="small"
          style="width: 90px"
          @click="() => handleReset(clearFilters)"
        >
          Reset
        </a-button>
      </div>

      <template
        slot="serial"
        slot-scope="text, record, i"
      >
        {{ i+(pagination.current - 1) * pagination.pageSize + 1 }}
      </template>
      <template
        slot="operation"
        slot-scope="text, record, i"
      >
        <a-button
          class="op-btn"
          size="small"
          @click="showStrategyModal(record)"
        >Strategy</a-button>
        <a-button
          class="op-btn"
          size="small"
          type="primary"
          ghost
          @click="addField(i)"
        >Add</a-button>
        <a-popconfirm
          title="confirm deletion？"
          cancel-text="No"
          ok-text="Yes"
          @confirm="removeDict(i)"
        >
          <a-button
            :loading="removeDictLoading"
            class="op-btn"
            size="small"
            type="danger"
            ghost
          >Delete</a-button>
        </a-popconfirm>
      </template>
      <template
        v-for="column in editColumns"
        :slot="column.key"
        slot-scope="text, record"
      >
        <EditCell
          v-model="record[column.key]"
          :key="column.key"
        ></EditCell>
      </template>
      <a-table
        bordered
        slot="expandedRowRender"
        slot-scope="record, dictIndex"
        :columns="innerColumns"
        :dataSource="record.dict_list"
        :pagination="false"
      >
        <template
          slot="serial"
          slot-scope="text, innerRecord, fieldIndex"
        >
          {{ dictIndex+1 }}.{{ fieldIndex+1 }}
        </template>
        <template
          slot="operation"
          slot-scope="text, innerRecord, fieldIndex"
        >
          <a-popconfirm
            v-if="record.dict_list.length > 1"
            title="confirm deletion？"
            cancel-text="No"
            ok-text="Yes"
            @confirm="removeField(dictIndex, fieldIndex)"
          >
            <a-button
              class="op-btn"
              size="small"
              type="danger"
              ghost
              :loading="removeFieldLoading"
            >Delete</a-button>
          </a-popconfirm>
        </template>
        <template
          v-for="column in editInnerColumns"
          :slot="column.key"
          slot-scope="text, innerRecord"
        >
          <EditCell
            v-model="innerRecord[column.key]"
            :type="column.editType"
            :key="column.key"
          ></EditCell>
        </template>
      </a-table>
    </a-table>
  </a-card>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import EditCell from './EditCell'
import DictStrategyModal from './DictStrategyModal'
const baseDict = {
  field_domain: '',
  field_item: '',
  dict_list: []
}
const baseField = {
  field_value: '',
  field_label: ''
}
export default {
  components: {
    EditCell,
    DictStrategyModal
  },
  props: {
    field: {
      type: Object,
      default () {
        return null
      }
    }
  },
  data () {
    return {
      dictList: [],
      params: {
        field_domain: '',
        field_item: '',
        field_value: ''
      },
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      columns: [
        {
          title: 'No.',
          dataIndex: 'serail',
          key: 'serail',
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: 'Domain',
          dataIndex: 'field_domain',
          key: 'field_domain',
          width: 400,
          editable: true,
          scopedSlots: {
            filterDropdown: 'filterDropdown',
            filterIcon: 'filterIcon',
            customRender: 'field_domain'
          },
          onFilter: (value, record) =>
            record.field_domain
              .toString()
              .toLowerCase()
              .includes(value.toLowerCase()),
          onFilterDropdownVisibleChange: visible => {
            if (visible) {
              setTimeout(() => {
                this.searchInput.focus()
              }, 0)
            }
          }
        },
        {
          title: 'Item',
          dataIndex: 'field_item',
          key: 'field_item',
          width: 400,
          editable: true,
          scopedSlots: {
            filterDropdown: 'filterDropdown',
            filterIcon: 'filterIcon',
            customRender: 'field_item'
          },
          onFilter: (value, record) =>
            record.field_item
              .toString()
              .toLowerCase()
              .includes(value.toLowerCase()),
          onFilterDropdownVisibleChange: visible => {
            if (visible) {
              setTimeout(() => {
                this.searchInput.focus()
              }, 0)
            }
          }
        },
        {
          title: 'Operation',
          dataIndex: 'operation',
          key: 'operation',
          scopedSlots: { customRender: 'operation' }
        }
      ],
      innerColumns: [
        {
          title: 'No.',
          dataIndex: 'serail',
          key: 'serail',
          width: 40,
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: 'Value',
          dataIndex: 'field_value',
          key: 'field_value',
          width: 135,
          editable: true,
          scopedSlots: { customRender: 'field_value' }
        },
        {
          title: 'Label',
          dataIndex: 'field_label',
          key: 'field_label',
          width: 135,
          editable: true,
          scopedSlots: { customRender: 'field_label' }
        },
        {
          title: 'Sequence',
          dataIndex: 'field_sequence',
          key: 'field_sequence',
          width: 100,
          editType: 'InputNumber'
        },
        {
          title: 'ID',
          dataIndex: 'field_id',
          key: 'field_id',
          width: 70
        },
        {
          title: 'Parent Id',
          dataIndex: 'field_parent_id',
          key: 'field_parent_id',
          width: 100,
          editable: true,
          scopedSlots: { customRender: 'field_parent_id' }
        },
        {
          title: 'Operation',
          dataIndex: 'operation',
          key: 'operation',
          width: 200,
          scopedSlots: { customRender: 'operation' }
        }
      ],
      queryLoading: false,
      saveLoading: false,
      removeFieldLoading: false,
      removeDictLoading: false,
      expandedRowKeys: [],
      activeDict: null,
      selectedDictKey: null,
      dictStrategyVisiable: false
    }
  },
  computed: {
    editColumns () {
      return this.columns.filter(x => x.editable)
    },
    editInnerColumns () {
      return this.innerColumns.filter(x => x.editable)
    }
  },
  created () {
    if (this.field) this.loadDicVal()
    this.loadTableData()
  },
  methods: {
    rowClassName (record) {
      if (this.selectedDictKey && this.selectedDictKey === `${record.field_domain}_${record.field_item}`) {
        return 'active-row'
      }
    },
    findSelectedDict (domain, item) {
      return this.dictList.find((x) => x.field_domain === domain && x.field_item === item)
    },
    async loadDicVal () {
      const dfKey = this.field.df_key
      const fieldName = this.field.db_field_name
      this.params.field_form_df_key = dfKey
      this.params.field_form_field_key = fieldName
      const dictConfiguration = this.field.dict_configuration
      if (this.field.form_field_dict_domain_name) {
        this.selectedDictKey = `${this.field.form_field_dict_domain_name}_${this.field.form_field_dict_item}`
      }
      if (!dictConfiguration) {
        const data = await this.$api.getDictConfiguration(dfKey, fieldName)
        Object.assign(this.params, data)
      } else {
        Object.assign(this.params, dictConfiguration)
      }
    },
    handleSearch (selectedKeys, confirm, dataIndex) {
      confirm()
      this.searchText = selectedKeys[0]
      this.searchedColumn = dataIndex
    },
    handleReset (clearFilters) {
      clearFilters()
      this.searchText = ''
    },
    handleSavedStrategy () {
      this.loadTableData()
    },
    showStrategyModal (dict) {
      this.activeDict = dict
      this.dictStrategyVisiable = true
    },
    customRow (record) {
      return {
        on: {
          click: () => {
            if (!record.field_domain || !record.field_item || !record.dict_list || !record.dict_list.length) {
              return
            }
            this.selectedDictKey = `${record.field_domain}_${record.field_item}`
            this.$emit('selected', record, this.params)
          }
        }
      }
    },
    expandedRowsChange (expandedRows) {
      this.expandedRowKeys = [...expandedRows]
    },
    addDict () {
      this.pagination.pageSize++
      this.dictList.unshift(cloneDeep(baseDict))
    },
    async removeDict (index) {
      const item = this.dictList[index]
      this.removeDictLoading = true
      await this.$api
        .deleteDictList({
          field_domain: item.field_domain,
          field_item: item.field_item
        })
        .catch(() => (this.removeDictLoading = false))
      this.$message.success('Operation succeeded')
      this.removeDictLoading = false
      this.dictList.splice(index, 1)
      this.pagination.pageSize--
    },
    async removeField (dictIndex, fieldIndex) {
      const field = this.dictList[dictIndex].dict_list[fieldIndex]
      this.removeFieldLoading = true
      await this.$api.deleteDictField([field.field_id])
      this.removeFieldLoading = false
      this.dictList[dictIndex].dict_list.splice(fieldIndex)
    },
    addField (dictIndex) {
      const item = this.dictList[dictIndex]
      if (!item.field_domain || !item.field_item) {
        return this.$message.info('Please fill field domain and field item first before add field.')
      }
      if (this.expandedRowKeys.indexOf(item.field_item) === -1) {
        this.expandedRowKeys.push(item.field_item)
      }
      const field = cloneDeep(baseField)
      item.dict_list.push(field)
    },
    sliptCat (dictList) {
      const newItems = []
      const oldItems = []
      dictList.forEach(item => {
        item.dict_list.forEach(field => {
          if (field.field_id) {
            oldItems.push({
              field_domain: item.field_domain,
              field_item: item.field_item,
              field_value: field.field_value,
              field_label: field.field_label,
              field_id: field.field_id,
              field_sequence: field.field_sequence,
              field_parent_id: field.field_parent_id
            })
          } else {
            newItems.push({
              field_domain: item.field_domain,
              field_item: item.field_item,
              field_value: field.field_value,
              field_label: field.field_label
            })
          }
        })
      })
      return { newItems, oldItems }
    },
    validate (item) {
      if (!item.field_domain) {
        this.$message.warning('Field Domain is required')
        return false
      } else if (!item.field_item) {
        this.$message.warning('Field Item is required')
        return false
      } else if (!item.dict_list || !item.dict_list.length) {
        this.$message.warning('At least one Field')
        return false
      } else if (item.dict_list.some(x => !x.field_value || !x.field_label)) {
        this.$message.warning('Field Value and Field Label is required')
        return false
      }
      return true
    },
    validateAll (items) {
      return !items.some(item => !this.validate(item))
    },
    async saveDict () {
      if (!this.validateAll(this.dictList)) return
      const { newItems, oldItems } = this.sliptCat(this.dictList)
      this.saveLoading = true
      newItems.length &&
        (await this.$api.saveDictList(newItems).catch(() => {
          this.$message.error('Operation failed')
          this.saveLoading = false
        }))
      oldItems &&
        (await this.$api.updateDictList(oldItems).catch(() => {
          this.$message.error('Operation failed')
          this.saveLoading = false
        }))
      this.saveLoading = false
      this.$message.success('Operation succeeded')
    },
    handleTableChange (pagination) {
      this.pagination.current = pagination.current
      this.loadTableData()
    },
    async loadTableData () {
      this.queryLoading = true
      const resp = await this.$api
        .getDictList({
          page: this.pagination.current - 1,
          size: this.pagination.pageSize,
          ...this.params
        })
        .catch(() => (this.queryLoading = false))
      this.queryLoading = false
      if (!resp) return
      this.dictList = resp.content
      this.pagination.total = resp.total_elements
    },
    reset () {
      this.params = {}
      this.loadTableData()
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .op-btn.ant-btn {
  margin-left: 5px;
}
/deep/ .op-box {
  margin-bottom: 10px;
}
.search-btn-group {
  text-align: right;
  padding: 0;
  position: absolute;
  bottom: 0;
  right: 0;
}
/deep/ .ant-collapse {
  margin-bottom: 10px;
}
/deep/ .ant-collapse-icon-position-right > .ant-collapse-item > .ant-collapse-header {
  padding: 5px 10px;
}
/deep/ .active-row {
  background: #bae7ff;
}
</style>
