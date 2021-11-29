<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <h3 class="title">Select Attribute Source(s)</h3>
    <a-row
      class="attribute-select-box"
      :gutter="15"
    >
      <a-col :span="5">
        <div>Data Sources</div>
        <a-card
          size="small"
          class="data-source-box"
        >
          <a-tree
            :tree-data="dataSources"
            :selectedKeys="selectedDsUids"
            :replaceFields="{children:'children', key: 'uid' }"
            :defaultSelectedKeys="defaultSelectedKeys"
            @select="handleSelect"
          ></a-tree>
        </a-card>
      </a-col>
      <a-col :span="19">
        <div>Attributes of Selected Data Source</div>
        <vxe-table
          ref="xTable"
          class="more-small-table mini-scrollbar"
          row-id="src_attr_uid"
          :data="dsAttrs"
          :loading="dsAttrsLoading"
          size="small"
          max-height="500"
          highlight-hover-row
          :checkbox-config="{checkRowKeys: selectedRowKeys}"
          @checkbox-all="selectChangeEvent"
          @checkbox-change="selectChangeEvent"
        >
          <vxe-table-column type="checkbox" width="60" fixed="left" align="center"></vxe-table-column>
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
              v-if="column.dataIndex === 'target_attr_name'"
              v-slot="{ row }"
            >
              <a-input
                size="small"
                v-if="isSelect(row)"
                v-model="row.target_attr_name"
                @input="handleInputChange(row, 'target_attr_name', $event.target.value)"
              ></a-input>
              <span v-else>
                {{ row['target_attr_name'] }}
              </span>
            </template>
            <template
              v-else-if="column.dataIndex === 'target_attr_description'"
              v-slot="{ row }"
            >
              <a-input
                v-if="isSelect(row)"
                size="small"
                v-model="row.target_attr_description"
                @input="handleInputChange(row, 'target_attr_description', $event.target.value)"
              ></a-input>
              <span v-else>
                {{ row['target_attr_description'] }}
              </span>
            </template>
          </vxe-table-column>
        </vxe-table>
        <!-- <a-table
          class="more-small-table"
          size="small"
          :bordered="true"
          :columns="columns"
          :data-source="dsAttrs"
          :loading="dsAttrsLoading"
          rowKey="src_attr_uid"
          :pagination="false"
          :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
          :scroll="{ x: 1200, y: '60vh' }"
        >
          <template
            slot="serial"
            slot-scope="text, record, index"
          >
            {{ index + 1 }}
          </template>
          <template
            slot="target_attr_name"
            slot-scope="text, record"
          >
            <a-input
              size="small"
              v-if="isSelect(record)"
              v-model="record.target_attr_name"
              @input="handleInputChange(record, 'target_attr_name', $event.target.value)"
            ></a-input>
            <span v-else>
              {{ text }}
            </span>
          </template>
          <template
            slot="target_attr_description"
            slot-scope="text, record"
          >
            <a-input
              v-if="isSelect(record)"
              size="small"
              v-model="record.target_attr_description"
              @input="handleInputChange(record, 'target_attr_description', $event.target.value)"
            ></a-input>
            <span v-else>
              {{ text }}
            </span>
          </template>
        </a-table> -->
      </a-col>
    </a-row>
    <h3 class="title">Confirm All Selected Attribute(s)</h3>
    <div class="selected-attributes">
      <a-row
        class="mg-b-10"
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
                    allowClear
                    v-model.trim="params[formItem.key]"
                    :options="formItem.options"
                    v-if="formItem.type === 'select'"
                    :placeholder="formItem.placeholder"
                  ></a-select>
                  <a-input
                    v-model.trim="params[formItem.key]"
                    :placeholder="formItem.placeholder"
                    :allowClear="true"
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
              :loading="confirmLoading"
              icon="check"
              @click="saveTargetAttrs"
            >Confirm</a-button>
            <a-button
              class="mg-l-5"
              icon="undo"
              :loading="queryLoading"
              @click="revert"
            >Revert</a-button>
          </div>
        </a-col>
      </a-row>
      <!-- <a-table
        v-if="targetTableVisible"
        :bordered="true"
        class="more-small-table"
        size="small"
        :rowKey="(record) => record.ds_uid + record.src_attr_uid"
        :data-source="filterTargetAttrs"
        :columns="targetColumns"
        :loading="queryLoading"
        :scroll="{x: 1800, y: '60vh'}"
        :pagination="false"
      >
        <template
          slot="serial"
          slot-scope="text, record, index"
        >
          {{ index + 1 }}
        </template>
      </a-table> -->
      <vxe-table
        ref="xTable2"
        class="more-small-table mini-scrollbar"
        :data="filterTargetAttrs"
        :loading="queryLoading"
        size="mini"
        max-height="600"
      >
        <vxe-table-column
          type="seq"
          title="No."
          width="60"
          fixed="left"
        ></vxe-table-column>
        <vxe-table-column
          v-for="column in targetColumns"
          :key="column.dataIndex"
          :field="column.dataIndex"
          :title="column.title"
          :width="column.width"
          :min-width="column.minWidth"
          :fixed="column.fixed"
        ></vxe-table-column>
      </vxe-table>
    </div>
  </a-card>
</template>

<script>
import '@/core/vxe-table'
const getSpanRowNum = (row, list, columnKey, temp) => {
  if (list.length === 1) return 1
  let spanRow = 0
  if (row[columnKey].toLowerCase() !== temp[columnKey]?.toLowerCase()) {
    temp[columnKey] = row[columnKey].toLowerCase()
    list.forEach(x => {
      if (x[columnKey].toLowerCase() === temp[columnKey]?.toLowerCase()) {
        spanRow++
      }
    })
  }
  return spanRow
}
// function getCustomRender (listData, columnkey = 'target_attr_name') {
//   const temp = {}
//   return (value, row, index) => {
//     const obj = {
//       children: value,
//       attrs: {
//         rowSpan: getSpanRowNum(row, listData, columnkey, temp)
//       }
//     }
//     return obj
//   }
// }
export default {
  name: 'AttributeSelect',
  data () {
    return {
      selectedDsUids: [],
      defaultSelectedKeys: [],
      selectedRowKeys: [],
      dataSources: [],
      dsAttrsMap: {},
      targetAttrs: [],
      // targetAttrs: [],
      activeDs: null,
      dsAttrsLoading: false,
      queryLoading: false,
      confirmLoading: false,
      columns: [
        // {
        //   title: 'No.',
        //   key: 'serial',
        //   dataIndex: 'serial',
        //   scopedSlots: { customRender: 'serial' },
        //   fixed: 'left',
        //   width: 60
        // },
        {
          title: 'Source Attribute UID',
          key: 'src_attr_uid',
          dataIndex: 'src_attr_uid',
          width: 100
        },
        {
          title: 'Source Attribute Name',
          key: 'src_attr_name',
          dataIndex: 'src_attr_name',
          width: 170,
          ellipsis: true
        },
        {
          title: 'Source Attribute Description',
          key: 'src_attr_description',
          dataIndex: 'src_attr_description',
          width: 170,
          ellipsis: true
        },
        {
          title: 'Source Attribute Status',
          key: 'src_attr_status',
          dataIndex: 'src_attr_status',
          minWidth: 100
        },
        {
          title: 'In Which Version Source Attribute Was Created',
          key: 'src_attr_created_version',
          dataIndex: 'src_attr_created_version',
          width: 110
        },
        {
          title: 'In Which Version Source Attribute Was Eliminated',
          key: 'src_attr_eliminated_version',
          dataIndex: 'src_attr_eliminated_version',
          width: 110
        },
        {
          title: 'Target Attribute Name',
          key: 'target_attr_name',
          dataIndex: 'target_attr_name',
          scopedSlots: { customRender: 'target_attr_name' },
          fixed: 'right',
          width: 150
        },
        {
          title: 'Target Attribute Description',
          key: 'target_attr_description',
          dataIndex: 'target_attr_description',
          scopedSlots: { customRender: 'target_attr_description' },
          fixed: 'right',
          width: 150
        }
      ],
      targetColumns: [],
      targetTableVisible: true,
      params: {
        target_attr_name: undefined,
        source_attr_name: undefined,
        ds_uid: undefined
      },
      formItems: [
        {
          label: 'Target Attribute Name',
          key: 'target_attr_name'
        },
        {
          label: 'Source Attribute Name',
          key: 'src_attr_name'
        },
        {
          label: 'Data Source',
          key: 'ds_uid',
          type: 'select',
          placeholder: 'Choose Data Source',
          options: []
        }
      ]
    }
  },
  async activated () {
    await this.loadInUseDS()
    this.loadTargetAttrs()
  },
  watch: {
    filterTargetAttrs: {
      deep: true,
      immediate: true,
      handler () {
        this.targetColumns = this.getTargetColumns()
        const mergeCells = this.getMergeCells()
        this.$nextTick(() => {
          this.$refs.xTable2.setMergeCells(mergeCells)
        })
        // this.targetTableVisible = false
        // this.$nextTick(() => {
        //   this.targetTableVisible = true
        // })
      }
    }
  },
  computed: {
    filterTargetAttrs () {
      return this.targetAttrs
        .filter(x => {
          let isShow = true
          this.formItems.forEach(item => {
            if (this.params[item.key] && x[item.key]?.toLowerCase().indexOf(this.params[item.key]?.toLowerCase()) === -1) {
              isShow = false
            }
          })
          return isShow
        })
        .sort((a, b) => {
          return ('' + a.target_attr_name).localeCompare('' + b.target_attr_name)
        })
    },
    dsAttrs () {
      if (!this.selectedDsUids.length) {
        return []
      } else {
        return this.dsAttrsMap[this.selectedDsUids[0]] || []
      }
    }
  },
  methods: {
    selectChangeEvent ({ checked, records }) {
      this.onSelectChange(records.map(x => x.src_attr_uid))
    },
    getMergeCells () {
      const temp = {}
      return this.filterTargetAttrs
        .map((x, i) => ({
          row: i,
          col: 1,
          rowspan: getSpanRowNum(this.filterTargetAttrs[i], this.filterTargetAttrs, 'target_attr_name', temp),
          colspan: 1
        }))
        .filter(x => x.rowspan > 1)
    },
    getTargetColumns () {
      const columns = [
        // {
        //   title: 'No.',
        //   key: 'serial',
        //   dataIndex: 'serial',
        //   scopedSlots: { customRender: 'serial' },
        //   fixed: 'left',
        //   width: 60
        // },
        {
          title: 'Target Attribute Name',
          key: 'target_attr_name',
          dataIndex: 'target_attr_name',
          // sorter: (a, b) => {
          //   return ('' + a.target_attr_name).localeCompare('' + b.target_attr_name)
          // },
          // defaultSortOrder: 'ascend',
          // sortDirections: [],
          // customRender: getCustomRender(this.filterTargetAttrs),
          fixed: 'left',
          width: 190
        },
        {
          title: 'Target Attribute UID',
          key: 'target_attr_uid',
          dataIndex: 'target_attr_uid',
          fixed: 'left',
          width: 100
        },
        {
          title: 'Target Attribute Description',
          key: 'target_attr_description',
          dataIndex: 'target_attr_description',
          width: 200
        },
        {
          title: 'Source Attribute UID',
          key: 'src_attr_uid',
          dataIndex: 'src_attr_uid',
          width: 100
        },
        {
          title: 'Source Attribute Name',
          key: 'src_attr_name',
          dataIndex: 'src_attr_name',
          width: 170
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
          width: 110
        },
        {
          title: 'In Which Version Source Attribute Was Eliminated',
          key: 'src_attr_eliminated_version',
          dataIndex: 'src_attr_eliminated_version',
          width: 110
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
        }
      ]
      return columns
    },
    isSelect (row) {
      return this.selectedRowKeys.includes(row.src_attr_uid)
    },
    handleInputChange (record, attrName, value) {
      const targetAttr = this.targetAttrs.find(
        x => x.src_attr_uid === record.src_attr_uid && x.ds_uid === this.activeDs.uid
      )
      if (targetAttr) {
        targetAttr[attrName] = value
      }
    },
    pushAttrs (selectedRowKeys) {
      selectedRowKeys.forEach(uid => {
        const oldIndex = this.targetAttrs.findIndex(x => x.src_attr_uid === uid && x.ds_uid === this.activeDs.uid)
        if (oldIndex === -1) {
          const dsAttr = this.dsAttrs.find(y => y.src_attr_uid === uid)
          if (dsAttr) {
            dsAttr.target_attr_name = dsAttr.target_attr_name || dsAttr.src_attr_name
            dsAttr.target_attr_description = dsAttr.target_attr_description || dsAttr.src_attr_description
            const newDsAttr = Object.assign({}, dsAttr)
            dsAttr.in_use = true
            newDsAttr.ds_name = this.activeDs.name
            newDsAttr.ds_uid = this.activeDs.uid
            this.targetAttrs.push(newDsAttr)
          }
        }
      })
    },
    removeAttrs (selectedRowKeys) {
      const removeKeys = this.selectedRowKeys.filter(key => !selectedRowKeys.includes(key))
      removeKeys.forEach(uid => {
        const oldIndex = this.targetAttrs.findIndex(x => x.src_attr_uid === uid && x.ds_uid === this.activeDs.uid)
        if (oldIndex !== -1) {
          const dsAttr = this.dsAttrs.find(y => y.src_attr_uid === uid)
          if (dsAttr) {
            dsAttr.in_use = false
          }
          this.targetAttrs.splice(oldIndex, 1)
        }
      })
    },
    onSelectChange (selectedRowKeys) {
      if (this.dsAttrsLoading) return
      this.pushAttrs(selectedRowKeys)
      this.removeAttrs(selectedRowKeys)
      this.selectedRowKeys = selectedRowKeys
    },
    handleSelect (selectedDsUids) {
      if (!selectedDsUids.length) return
      this.selectedDsUids = selectedDsUids
      this.activeDs = this.dataSources.find(x => x.uid === this.selectedDsUids[0])
      this.loadDsAttrs(this.selectedDsUids[0])
    },
    async loadInUseDS () {
      const resp = await this.$api.getInUseDS()
      if (!resp || !resp.object) return
      resp.object.forEach(x => {
        x.title = `${x.name}`
      })
      this.dataSources = resp.object
      if (this.dataSources.length) {
        this.formItems[2].options = this.dataSources.map(x => ({
          label: x.name,
          value: x.uid
        }))
        this.handleSelect([this.dataSources[0].uid])
      }
    },
    async loadDsAttrs (dsUid) {
      if (!dsUid) return
      if (!this.dsAttrsMap[dsUid]) {
        this.dsAttrsLoading = true
        const resp = await this.$api.getDSAtrrs(dsUid).finally(() => {
          this.dsAttrsLoading = false
        })
        if (!resp) return
        // this.dsAttrsMap[dsUid] = resp.object || []
        this.$set(this.dsAttrsMap, dsUid, resp.object || [])
      }
      const selectedRows = this.dsAttrs.filter(x => x.in_use)
      this.$refs.xTable.setCheckboxRow(selectedRows, true)
      this.selectedRowKeys = selectedRows.map(x => x.src_attr_uid)
    },
    async loadTargetAttrs () {
      this.targetAttrs = []
      this.queryLoading = true
      const resp = await this.$api.getSrcTargetAtrrs().finally(() => {
        this.queryLoading = false
      })
      if (!resp || !resp.object) return
      this.targetAttrs = resp.object || []
    },
    async saveTargetAttrs () {
      this.confirmLoading = true
      await this.$api.saveSrcTargetAtrrs(this.targetAttrs).finally(() => {
        this.confirmLoading = false
      })
      this.loadTargetAttrs()
    },
    revert () {
      this.loadTargetAttrs()
      if (this.dataSources.length) {
        this.handleSelect([this.dataSources[0].uid])
      }
    }
  }
}
</script>

<style lang="less" scoped>
.attribute-select-box {
  margin-bottom: 20px;
}
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
/deep/ .data-source-box {
  overflow-y: auto;
  max-height: 60vh;
}
/deep/ .ant-tree li span.ant-tree-switcher {
  display: none;
}
/deep/ .ant-table-tbody > tr.ant-table-row-selected td {
  background: #fff;
}
/deep/ .ant-tree-icon-hide {
  overflow: hidden;
}
/deep/ .ant-input-sm {
  height: 24px;
  padding: 1px 7px;
}
/deep/ .ant-input {
  font-size: 12px;
}
</style>
