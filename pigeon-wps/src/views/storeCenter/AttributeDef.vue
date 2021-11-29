<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <a-card
      size="small"
      :bordered="false"
    >
      <a-row
        class="filter-box"
        type="flex"
        justify="space-between"
      >
        <a-col :span="20">
          <a-form-model
            layout="vertical"
            :colon="false"
            :model="params"
            :labal-col="{ span: 6 }"
          >
            <a-row :gutter="24">
              <a-col
                :span="6"
                :key="formItem.key"
                v-for="formItem in formItems"
              >
                <a-form-model-item :label="formItem.label">
                  <a-select
                    v-model="params[formItem.key]"
                    allowClear
                    :options="formItem.options"
                    v-if="formItem.type === 'select'"
                    :placeholder="formItem.placeholder"
                  ></a-select>
                  <a-range-picker
                    allowClear
                    v-else-if="formItem.type === 'range-picker'"
                    v-model="selectedDateRange"
                  />
                  <a-input
                    v-model="params[formItem.key]"
                    :placeholder="formItem.placeholder"
                    allowClear
                    v-else
                  ></a-input>
                </a-form-model-item>
              </a-col>
            </a-row>
          </a-form-model>
        </a-col>
        <a-col :span="4">
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
    </a-card>
    <a-row class="toolbar-box">
      <a-button
        class="pull-right"
        @click="createModalVisible = true"
        type="primary"
        icon="plus-circle"
      >Create Attribute</a-button>
    </a-row>
    <vxe-table
      class="more-small-table mini-scrollbar"
      :data="tableData"
      :loading="queryLoading"
      size="mini"
      max-height="400"
      highlight-hover-row
    >
      <vxe-table-column
        type="seq"
        title="No."
        width="60"
        fixed="left"
      ></vxe-table-column>
      <vxe-table-column
        v-for="column in columns"
        :key="column.dataIndex"
        :field="column.dataIndex"
        :title="column.title"
        :width="column.width"
        :min-width="column.minWidth"
        :fixed="column.fixed"
      >
        <template
          v-if="column.dataIndex === 'operation'"
          v-slot="{ row }"
        >
          <a-button-group>
            <template v-for="button in buttons">
              <a-popconfirm
                :key="button.operationType"
                :title="'confirm ' + button.operationType + ' ?'"
                cancel-text="No"
                ok-text="Yes"
                @confirm="handleClick(row, button)"
                v-if="button.confirm"
              >
                <a-button
                  v-if="button.show && button.show(row) || button.showStatus && button.showStatus.includes(row.target_attr_status)"
                  :type="button.type || 'default'"
                  size="small"
                  :loading="button.loading"
                >{{ firstLetterUpper(button.operationType) }}</a-button>
              </a-popconfirm>
              <template v-else>
                <a-button
                  :key="button.operationType"
                  v-if="button.show && button.show(row) || button.showStatus && button.showStatus.includes(row.target_attr_status)"
                  :type="button.type || 'default'"
                  size="small"
                  :loading="button.loading"
                  @click="handleClick(row, button)"
                >{{ firstLetterUpper(button.operationType) }}</a-button>
              </template>
            </template>
          </a-button-group>
        </template>
      </vxe-table-column>
    </vxe-table>
    <!-- <a-table
      :bordered="true"
      class="more-small-table"
      size="small"
      :rowKey="(record) => record.target_attr_uid + record.src_attr_uid"
      :data-source="tableData"
      :columns="columns"
      :loading="queryLoading"
      :scroll="scroll"
      :pagination="false"
    >
      <template
        slot="serial"
        slot-scope="text, record, index"
      >
        {{ index + 1 }}
      </template>
      <template
        slot="operation"
        slot-scope="text, record"
      >
        <a-button-group>
          <template v-for="button in buttons">
            <a-popconfirm
              :key="button.operationType"
              :title="'confirm ' + button.operationType + ' ?'"
              cancel-text="No"
              ok-text="Yes"
              @confirm="handleClick(record, button)"
              v-if="button.confirm"
            >
              <a-button
                v-if="button.show && button.show(record) || button.showStatus && button.showStatus.includes(record.target_attr_status)"
                :type="button.type || 'default'"
                size="small"
                :loading="button.loading"
              >{{ firstLetterUpper(button.operationType) }}</a-button>
            </a-popconfirm>
            <template v-else>
              <a-button
                :key="button.operationType"
                v-if="button.show && button.show(record) || button.showStatus && button.showStatus.includes(record.target_attr_status)"
                :type="button.type || 'default'"
                size="small"
                :loading="button.loading"
                @click="handleClick(record, button)"
              >{{ firstLetterUpper(button.operationType) }}</a-button>
            </template>
          </template>
        </a-button-group>
      </template>
    </a-table> -->
    <BaseFormModal
      v-model="createModalVisible"
      api-name="createTargetAtrr"
      title="Create Target Attribute"
      :formItems="modalFormItems"
      @saved="loadTableData"
    ></BaseFormModal>
    <BaseFormModal
      v-model="editModalVisible"
      api-name="updateTargetAtrr"
      title="Update Target Attribute"
      :item="activeItem"
      :formItems="modalFormItems"
      :extraFields="['target_attr_uid']"
      @saved="loadTableData"
    ></BaseFormModal>
  </a-card>
</template>

<script>
import { firstLetterUpper } from '@/components/_util/util'
import BaseFormModal from '@/components/common/BaseFormModal'
import '@/core/vxe-table'
export default {
  name: 'AttributeDef',
  components: { BaseFormModal },
  data () {
    return {
      createModalVisible: false,
      editModalVisible: false,
      activeItem: null,
      modalFormItems: [
        {
          label: 'Name',
          key: 'target_attr_name',
          required: true
        },
        {
          label: 'Description',
          key: 'target_attr_description',
          type: 'textarea'
        }
      ],
      tableData: [],
      params: {},
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      buttons: [
        // { operationType: 'eliminate', showStatus: ['NORMAL'], type: 'primary', confirm: true, loading: false },
        // { operationType: 'retain', showStatus: ['ELIMINATED'], type: 'primary', confirm: true, loading: false },
        {
          operationType: 'edit',
          show: record => record.target_attr_build_type === 'BUILT_FROM_ZERO',
          type: 'primary',
          loading: false
        },
        {
          operationType: 'delete',
          show: record => record.target_attr_build_type === 'BUILT_FROM_ZERO',
          type: 'danger',
          confirm: true,
          loading: false
        }
      ],
      formItems: [
        {
          label: 'Target Attribute Build Type',
          key: 'targetAttrBuildType',
          type: 'select',
          options: [
            {
              label: 'BuildFromSrc',
              value: 'BUILT_FROM_SRC'
            },
            {
              label: 'BuildFromZero',
              value: 'BUILT_FROM_ZERO'
            }
          ]
        },
        {
          label: 'Target Attribute Name',
          key: 'targetAttrName'
        },
        {
          label: 'Target Attribute Status',
          key: 'targetAttrStatus',
          type: 'select',
          options: [
            {
              label: 'Normal',
              value: 'NORMAL'
            },
            {
              label: 'Eliminated',
              value: 'ELIMINATED'
            }
          ]
        },
        {
          label: 'Create Time',
          key: 'create_time',
          type: 'range-picker'
        }
      ],
      queryLoading: false,
      exportLoading: false,
      columns: [
        // {
        //   title: 'No.',
        //   key: 'serial',
        //   dataIndex: 'serial',
        //   scopedSlots: { customRender: 'serial' }
        // },
        {
          title: 'Target Attribute Build Type',
          key: 'target_attr_build_type',
          dataIndex: 'target_attr_build_type',
          width: 150
        },
        {
          title: 'Target Attribute UID',
          key: 'target_attr_uid',
          dataIndex: 'target_attr_uid',
          width: 150
        },
        {
          title: 'Target Attribute Name',
          key: 'target_attr_name',
          dataIndex: 'target_attr_name',
          width: 200
        },
        {
          title: 'Target Attribute Description',
          key: 'target_attr_description',
          dataIndex: 'target_attr_description',
          width: 200
        },
        {
          title: 'Target Attribute Status',
          key: 'target_attr_status',
          dataIndex: 'target_attr_status',
          width: 100
        },
        {
          title: 'In Which Version Target Attribute Was Created',
          key: 'target_attr_created_version',
          dataIndex: 'target_attr_created_version',
          width: 120
        },
        {
          title: 'In Which Version Target Attribute Was Eliminated',
          key: 'target_attr_eliminated_version',
          dataIndex: 'target_attr_eliminated_version',
          width: 120
        },
        {
          title: 'Source Attribute UID',
          key: 'src_attr_uid',
          dataIndex: 'src_attr_uid',
          width: 150
        },
        {
          title: 'Source Attribute Name',
          key: 'src_attr_name',
          dataIndex: 'src_attr_name',
          width: 200
        },
        {
          title: 'Source Attribute Description',
          key: 'src_attr_description',
          dataIndex: 'src_attr_description',
          width: 200
        },
        {
          title: 'Source Attribute Status',
          key: 'src_attr_status',
          dataIndex: 'src_attr_status',
          width: 100
        },
        {
          title: 'In Which Version Source Attribute Was Created',
          key: 'src_attr_created_version',
          dataIndex: 'src_attr_created_version',
          width: 120
        },
        {
          title: 'In Which Version Source Attribute Was Eliminated',
          key: 'src_attr_eliminated_version',
          dataIndex: 'src_attr_eliminated_version',
          width: 120
        },
        {
          title: 'DS UID',
          key: 'ds_uid',
          dataIndex: 'ds_uid',
          width: 250
        },
        {
          title: 'DS Name',
          key: 'ds_name',
          dataIndex: 'ds_name',
          width: 150
        },
        {
          title: 'Mapping Status',
          key: 'mapping_status',
          dataIndex: 'mapping_status',
          width: 100
        },
        {
          title: 'Create Time',
          key: 'create_time',
          dataIndex: 'create_time',
          width: 150
        },
        {
          title: 'Create By',
          key: 'create_by',
          dataIndex: 'create_by',
          width: 150
        },
        {
          title: 'Last Update Time',
          key: 'last_update_time',
          dataIndex: 'last_update_time',
          width: 150
        },
        {
          title: 'Last Update By',
          key: 'last_update_by',
          dataIndex: 'last_update_by',
          width: 150
        },
        {
          title: 'Operation',
          key: 'operation',
          dataIndex: 'operation',
          fixed: 'right',
          scopedSlots: { customRender: 'operation' },
          width: 120
        }
      ],
      selectedDateRange: []
    }
  },
  computed: {
    scroll () {
      return {
        x: this.columns.length > 6 ? 'max-content' : false
      }
    }
  },
  created () {
    this.loadTableData()
  },
  methods: {
    firstLetterUpper,
    handleTableChange (pagination) {
      this.pagination.current = pagination.current
      this.loadTableData()
    },
    async loadTableData () {
      this.queryLoading = true
      const resp = await this.$api
        .getTargetAtrrs({
          page: this.pagination.current - 1,
          size: this.pagination.pageSize,
          ...this.params
        })
        .finally(() => (this.queryLoading = false))
      if (!resp || !resp.object) return
      this.tableData = resp.object
      // this.pagination.total = resp.object.count
    },
    reset () {
      this.params = {}
      this.loadTableData()
      this.selectedDateRange = []
    },
    datePickChange () {
      if (this.selectedDateRange.length === 2) {
        const [start, end] = this.selectedDateRange
        this.params.createTimeFrom = start.format('YYYY-MM-DD')
        this.params.createTimeTo = end.format('YYYY-MM-DD')
      } else {
        this.params.createTimeFrom = undefined
        this.params.createTimeTo = undefined
      }
      this.loadTableData()
    },
    async changeStatus (item, button, status = 'NORMAL') {
      button.loading = true
      await this.$api.changeTargetStatus(item.target_attr_uid, status).finally(() => {
        button.loading = false
      })
      this.loadTableData()
    },
    async retain (item, button) {
      this.changeStatus(item, button)
    },
    async eliminate (item, button) {
      this.changeStatus(item, button, 'ELIMINATED')
    },
    async handleClick (item, button) {
      this[button.operationType](item, button)
    },
    async delete (item, button) {
      button.loading = true
      await this.$api.deleteTargetAtrr(item.target_attr_uid).finally(() => {
        button.loading = false
      })
      this.loadTableData()
    },
    edit (item, button) {
      this.activeItem = item
      this.editModalVisible = true
    }
  }
}
</script>

<style lang="less" scoped>
.search-btn-group {
  text-align: right;
  padding: 0;
  position: absolute;
  bottom: 0;
  right: 0;
}
/deep/ .ant-form-item {
  margin-bottom: 0;
}
.ant-form-vertical /deep/ .ant-form-item {
  padding-bottom: 0;
}
.filter-box {
  overflow: hidden;
  position: relative;
}
/deep/ .toolbar-box {
  border-top: 1px solid #e8e8e8;
}
/deep/ .toolbar-box {
  padding: 10px 0;
}
</style>
