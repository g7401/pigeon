<template>
  <a-card
    size="small"
    class="form-table-setting"
  >
    <a-modal v-if="isTest" title="Import Configuration" :footer="false" v-model="importModalVisible">
      <a-textarea :value="importConfigStr" :rows="10" @change="handleImportChange"></a-textarea>
      <a-button class="mg-t-10" :disabled="!importConfigStr" @click="importConfig">Import</a-button>
    </a-modal>
    <DefaultValChooseModal v-if="defaultValStrategyVisible && activeField" :categoryId="d" v-model="defaultValStrategyVisible" @select="handleSelectedDefaultVal"></DefaultValChooseModal>
    <DictChooseModal
      v-if="dictModalVisible"
      v-model="dictModalVisible"
      :categoryId="activeDictCategoryId"
      @select="handleSelectedDict"
    ></DictChooseModal>
    <CopyConfigModal
      v-model="copyModalVisible"
      :uid="uid"
      @select="copyDataFacet"
    ></CopyConfigModal>
    <ResourceConfigModal
      v-if="permissionModalVisible && activeField"
      v-model="permissionModalVisible"
      :uid="activeField.resource_structure_uid"
      @select="handleSelectPermission"></ResourceConfigModal>
    <a-row
      class="btn-row"
      type="flex"
      justify="space-between"
    >
      <a-breadcrumb v-if="crumbs.length">
        <a-breadcrumb-item
          v-for="crumb in crumbs"
          :key="crumb"
        >{{ crumb }}</a-breadcrumb-item>
      </a-breadcrumb>
      <div class="btn-group">
        <a-button
          type="primary"
          ghost
          @click="preview"
        >Preview Page</a-button>
        <a-tooltip title="Reload the latest fields of the table or view on which the Data Facet depends.">
          <a-popconfirm
            placement="bottom"
            title="confirm refresh?"
            cancel-text="No"
            ok-text="Yes"
            @confirm="refresh"
          >
            <a-button
              type="danger"
              ghost
              :loading="refreshLoading"
            >Refresh Fields</a-button>
          </a-popconfirm>
        </a-tooltip>
        <a-button
          type="primary"
          :loading="saveLoading"
          @click="save"
        >Save All Configuration</a-button>
      </div>
    </a-row>
    <a-tabs size="small" class="config-tabs" v-model="activeTab" @change="tabChange">
      <div slot="tabBarExtraContent">
        <div>
          <a-button
            v-if="isTest"
            class="mg-r-5"
            size="small"
            v-clipboard:copy="configStr"
            v-clipboard:success="onCopySuccess"
            v-clipboard:error="onCopyError">Copy</a-button>
          <a-button v-if="isTest" class="mg-r-5" size="small" @click="importModalVisible = true">Import</a-button>
          <a-button
            type="primary"
            size="small"
            @click="showCopyConfigModal"
          >Copy Configuration From Another Data Facet</a-button>
        </div>
      </div>
      <a-tab-pane key="basic" tab="Basic Configuration">
        <div class="table-box" :class="{'zoom-box': isMaximized}">
          <vxe-grid
            ref="confTable"
            max-height="100%"
            auto-resize
            class="more-small-table mini-scrollbar"
            show-overflow
            show-header-overflow
            :data="tableData"
            :loading="dataLoading"
            :columns="columns"
            size="small"
            keep-source
            :sort-config="{ multiple: false, trigger: 'cell' }"
            :edit-config="{ trigger: 'click', mode: 'cell', showIcon: false, showStatus: true, activeMethod: activeMethod }"
            highlight-cell
            @edit-closed="handleEditFinish"
            @sort-change="handleSortChange"
            :cell-class-name="cellClassName"
            @zoom="handleZoomChange"
          >
            <template v-slot:top>
              <a-button class="zoom-btn" size="small" @click="clickZoom">
                <a-icon :type="$refs.confTable && isMaximized ? 'fullscreen-exit' : 'fullscreen'" />
              </a-button>
            </template>
          </vxe-grid>
        </div>
        <p class="tip-text mg-t-10">page, size, sort, access_token, client_id and username are reserved words, they cannot be used as field names form elements.</p>
      </a-tab-pane>
      <a-tab-pane key="advanced" tab="Advanced Configuration">
        <div class="advanced-config">
          <a-row
            :gutter="10"
            class="mg-b-10"
          >
            <a-col
              :xs="12"
              :sm="12"
              :md="12"
              :lg="12"
              :xl="12"
              :xxl="10">
              <a-checkbox v-model="config.advanced.enabled_pagination">Enable Pagination with default page size</a-checkbox>
              <a-select
                v-model="config.advanced.default_page_size"
                size="small"
                :options="pageSizeOptions"
              ></a-select>
            </a-col>
            <a-col
              :xs="12"
              :sm="12"
              :md="12"
              :lg="12"
              :xl="12"
              :xxl="10">
              <a-checkbox v-model="config.advanced.enabled_vertical_scrolling">Enable Vertical Scrolling Height Threshold (px)</a-checkbox>
              <a-input-number
                size="small"
                :min="0"
                :max="2000"
                v-model="config.advanced.vertical_scrolling_height_threshold"
              ></a-input-number>
            </a-col>
          </a-row>

          <a-row
            :gutter="10"
            class="mg-b-10"
          >
            <a-col
              :xs="12"
              :sm="12"
              :md="12"
              :lg="12"
              :xl="12"
              :xxl="10">
              <a-checkbox v-model="config.advanced.enabled_freeze_left_columns">Freeze Columns (left to right) from 1 to</a-checkbox>
              <a-input-number
                v-model="config.advanced.inclusive_left_columns"
                size="small"
                :min="0"
                :max="1000"
              ></a-input-number>
            </a-col>
            <a-col
              :xs="12"
              :sm="12"
              :md="12"
              :lg="12"
              :xl="12"
              :xxl="10">
              <a-checkbox v-model="config.advanced.enabled_freeze_right_columns">Freeze Columns (right to left ) from 1 to </a-checkbox>
              <a-input-number
                v-model="config.advanced.inclusive_right_columns"
                size="small"
                :min="0"
                :max="1000"
              ></a-input-number>
            </a-col>
          </a-row>

          <a-row
            :gutter="10"
            class="mg-b-10"
          >
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox class="mg-r-10" v-model="config.advanced.enabled_column_no">Enable Column No.</a-checkbox>
            </a-col>
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox class="mg-r-10" v-model="config.advanced.enabled_column_checkbox">Enable Column Check Box</a-checkbox>
            </a-col>
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox class="mg-r-10" v-model="config.advanced.enabled_column_operation">Enable Column Operation</a-checkbox>
            </a-col>
          </a-row>

          <a-radio-group v-model="config.advanced.export_type">
            <a-row class="mg-b-10" :gutter="10">
              <a-col
                :xs="8"
                :sm="8"
                :md="8"
                :lg="8"
                :xl="8"
                :xxl="6">
                <a-radio value="ASYNCHRONOUS">
                  Asynchronous Export
                </a-radio>
              </a-col>
              <a-col
                :xs="8"
                :sm="8"
                :md="8"
                :lg="8"
                :xl="8"
                :xxl="6">
                <a-radio value="NO">
                  No Need to Export
                </a-radio>
              </a-col>
            </a-row>
          </a-radio-group>
          <a-row
            :gutter="10"
            class="mg-b-10"
            v-if="config.advanced.export_type === 'ASYNCHRONOUS'"
          >
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox v-model="config.advanced.enabled_export_csv_remove_comma">Enable Remove Comma While Export CSV</a-checkbox>
            </a-col>
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox v-model="config.advanced.enabled_export_csv_remove_line_break">Enable Remove Line Break While Export CSV</a-checkbox>
            </a-col>
          </a-row>
          <a-radio-group v-model="config.advanced.query_type">
            <a-row class="mg-b-10" :gutter="10">
              <a-col
                :xs="8"
                :sm="8"
                :md="8"
                :lg="8"
                :xl="8"
                :xxl="6">
                <a-radio value="SYNCHRONOUS">
                  Synchronous Query
                </a-radio>
              </a-col>
              <a-col
                :xs="8"
                :sm="8"
                :md="8"
                :lg="8"
                :xl="8"
                :xxl="6">
                <a-radio value="NO">
                  No Need to Query
                </a-radio>
              </a-col>
            </a-row>
          </a-radio-group>

          <a-row
            :gutter="10"
            class="mg-b-10"
          >
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox v-model="config.advanced.enabled_group_by">Enable Group By</a-checkbox>
            </a-col>
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox v-model="config.advanced.enabled_graph">Enable Graph</a-checkbox>
            </a-col>
          </a-row>
          <a-row
            :gutter="10"
            class="mg-b-10"
          >
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox v-model="config.advanced.enabled_default_query">Enable Default Query</a-checkbox>
            </a-col>
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox v-model="config.advanced.enabled_form_folding">Enable Form Folding</a-checkbox>
            </a-col>
            <a-col
              :xs="8"
              :sm="8"
              :md="8"
              :lg="8"
              :xl="8"
              :xxl="6">
              <a-checkbox v-model="config.advanced.enabled_df_name_description">Enable DF Name Description</a-checkbox>
            </a-col>
          </a-row>
        </div>
      </a-tab-pane>
      <a-tab-pane key="index" tab="Index(es) for Reference">
        <p class="tip-text">When configuring query items, you can refer to the index definition. <a-button class="mg-l-20" size="small" :loading="indexLoading" @click="getIndexs">Refresh</a-button></p>
        <a-row :gutter="50">
          <a-col
            :xs="9"
            :sm="9"
            :md="9"
            :lg="8"
            :xl="5"
            :xxl="5">
            <h4>list of Index</h4>
            <vxe-grid
              ref="indexList"
              class="more-small-table mini-scrollbar row--selectable"
              height="297"
              show-overflow
              :data="indexList"
              :columns="indexListColumns"
              :show-header="false"
              size="mini"
              highlight-hover-row
              highlight-current-row
              auto-resize
              @current-change="currentChangeEvent"
            >
            </vxe-grid>
          </a-col>
          <a-col
            :xs="14"
            :sm="14"
            :md="14"
            :lg="12"
            :xl="8"
            :xxl="8"
            v-if="activeIndex"
          >
            <h4>Details of Selected Index</h4>
            <a-row class="mg-b-20">
              <a-checkbox v-model="activeIndex.unique" disabled>Unique</a-checkbox>
            </a-row>
            <span class="mg-b-5">Index Columns</span>
            <vxe-grid
              class="more-small-table mini-scrollbar"
              height="233"
              show-overflow
              show-header-overflow
              :data="activeIndex.columns"
              :columns="indexColumns"
              size="mini"
              auto-resize
            >
            </vxe-grid>
          </a-col>
        </a-row>
      </a-tab-pane>
    </a-tabs>

    <!-- <h3 class="title mg-t-20">Advanced Configuration</h3> -->
    <!-- <h3 class="title mg-t-20">Index(es) for Reference</h3> -->
    <!-- <h3 class="title mg-t-20">表头分组配置</h3>
    <a-row>
      <a-button
        type="primary"
        ghost
        @click="addColumnGroup()"
      >增加表头分组</a-button>
    </a-row>
    <vxe-grid
      v-if="customiseColumns.length"
      class="costomise-header mini-scrollbar hide-body mg-t-10"
      show-overflow
      show-header-overflow
      :data="[]"
      :columns="customiseColumns"
      size="medium"
      ref="customiseGrid"
    >
      <template v-slot:empty>
      </template>
    </vxe-grid> -->
  </a-card>
</template>

<script>
import '@/core/vxe-table'
import cloneDeep from 'lodash.clonedeep'
import { QueryFormType, selectFormType, cascaderType, dateEelmentType, timeEelmentType, dateTimeEelmentType } from '@/assets/d1/map'
import DictChooseModal from '@/components/d1Admin/DictChooseModal'
import DefaultValChooseModal from '@/components/d1Admin/DefaultValChooseModal'
import BuildStrategyModal from '@/components/d1Admin/BuildStrategyModal'
import CopyConfigModal from '@/components/d1Admin/CopyConfigModal'
import ResourceConfigModal from '@/components/d1Admin/ResourceConfigModal'
import EditCell from '@/components/d1/EditCell'
const orderByOptions = [
  { label: 'None', value: undefined },
  { label: 'DESC', value: 'DESC' },
  { label: 'ASC', value: 'ASC' }
]

const roleOptions = [
  { label: 'None', value: undefined },
  { label: 'DIMENSION', value: 'DIMENSION' },
  { label: 'KPI', value: 'KPI' }
]

const typeOptions = [...Object.keys(QueryFormType).map(x => ({
  label: x,
  value: x
}))]

const aggregationTypeOptions = [
  {
    label: 'AVG',
    value: 'AVG'
  },
  {
    label: 'COUNT',
    value: 'COUNT'
  },
  {
    label: 'MAX',
    value: 'MAX'
  },
  {
    label: 'MIN',
    value: 'MIN'
  },
  {
    label: 'SUM',
    value: 'SUM'
  }
]

export default {
  components: { DictChooseModal, DefaultValChooseModal, BuildStrategyModal, CopyConfigModal, ResourceConfigModal, EditCell },
  props: {
    uid: {
      type: [Number, String],
      default: null
    },
    crumbs: {
      type: Array,
      default () {
        return []
      }
    },
    mode: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      activeTab: 'basic',
      importConfigStr: null,
      importModalVisible: false,
      isMaximized: false,
      permissionModalVisible: false,
      config: {
        advanced: {},
        basic: {
          fields: []
        }
      },
      activeDictCategoryId: null,
      copyModalVisible: false,
      copyData: [],
      dataLoading: false,
      saveLoading: false,
      refreshLoading: false,
      dictModalVisible: false,
      activeField: null,
      // chlidFieldOptions: null,
      defaultValStrategyVisible: false,
      pageSizeOptions: [
        {
          label: '10',
          value: 10
        },
        {
          label: '20',
          value: 20
        },
        {
          label: '50',
          value: 50
        },
        {
          label: '100',
          value: 100
        }
      ],
      indexList: [],
      indexLoading: false,
      indexListColumns: [{
        title: 'Name',
        field: 'name'
      }],
      indexColumns: [{
        title: 'Column Sequence',
        field: 'sequence'
      }, {
        title: 'Table Column',
        field: 'column_name'
      }],
      activeIndex: null,
      customiseColumns: [],
      activeSort: { field: 'list_element_sequence', order: 'asc' },
      formKeys: ['enabled_as_form_element', 'form_element_sequence', 'form_element_type', 'child_field_name', 'defaults_category_uid', 'dictionary_category_uid'],
      disabledEditColumns: ['seq', 'field_name', 'field_type', 'field_description']
    }
  },
  computed: {
    configStr () {
      if (!this.isTest) return null
      return JSON.stringify(this.config)
    },
    isTest () {
      return this.mode === 'test'
    },
    tableData () {
      return this.config.basic.fields
    },
    columns () {
      return [
        {
          type: 'seq',
          title: 'No.',
          field: 'seq',
          fixed: 'left',
          minWidth: 40
        },
        {
          field: 'field_name',
          title: 'Field Name',
          sortable: true,
          minWidth: 140,
          fixed: 'left'
        },
        {
          field: 'field_type',
          title: 'Field Type',
          fixed: 'left',
          minWidth: 90
        },
        {
          field: 'field_description',
          title: 'Field Description',
          minWidth: 120
        },
        {
          title: 'Field Label',
          field: 'field_label',
          minWidth: 150,
          editRender: { name: 'AInput', autoselect: true },
          slots: {
            header: this.slotsHeader
          }
        },
        {
          title: 'Table',
          align: 'center',
          children: [
            {
              title: 'Show',
              align: 'center',
              field: 'enabled_as_list_element',
              minWidth: 90,
              slots: {
                header: this.slotsHeader,
                default: ({ row, column }) => {
                  return [
                    <a-switch
                      disabled={!row.effective}
                      checked={!!row.enabled_as_list_element}
                      {...{ on: { change: checked => (row.enabled_as_list_element = checked) } }}
                    ></a-switch>
                  ]
                }
              }
            },
            {
              title: 'Sequence',
              align: 'center',
              field: 'list_element_sequence',
              editRender: {
                name: 'AInputNumber',
                props: { min: 1, max: 100 },
                autoselect: true
              },
              slots: {
                header: this.slotsHeader
              },
              minWidth: 110,
              sortable: true
            },
            {
              title: 'Width',
              align: 'center',
              field: 'list_element_width',
              editRender: { name: 'AInputNumber', props: { min: 0, max: 1000 }, autoselect: true },
              slots: {
                header: this.slotsHeader
              },
              minWidth: 75
            },
            {
              title: 'Order By',
              field: 'sort_direction',
              minWidth: 110,
              align: 'center',
              editRender: { name: 'ASelect', options: orderByOptions },
              slots: {
                header: this.slotsHeader
              }
            },
            {
              title: 'Data Permission',
              field: 'resource_structure_uid',
              minWidth: 150,
              slots: {
                header: this.slotsHeader,
                default: ({ row, column }) => {
                  let cell
                  if (row.resource_structure_uid && row.resource_category_name && row.resource_structure_name) {
                    cell = (
                      <span>
                        {row.resource_category_name},{row.resource_structure_name}
                      </span>
                    )
                  } else {
                    cell = <div class="placeholder">.</div>
                  }
                  return [
                    <span
                      {...{ on: { click: () => this.settingPermission(row) } }}
                      class="optional-values-cell"
                      class={{ disabled: !this.isCanSettingPermission(row) }}
                    >
                      {cell}
                    </span>
                  ]
                }
              }
            }
          ]
        },
        {
          title: 'Group',
          align: 'center',
          children: [
            {
              title: 'Role',
              field: 'role',
              minWidth: 130,
              align: 'center',
              editRender: true,
              slots: {
                header: this.slotsHeader,
                default: ({ row, column }) => {
                  let cell
                  if (row.role) {
                    cell = <span>{row.role}</span>
                  }
                  return [
                    <span
                      class={{ disabled: !this.isCanSettingTable(row) }}
                    >
                      {cell}
                    </span>
                  ]
                },
                edit: ({ row, column }) => {
                  return [
                    <a-select
                      size="small"
                      value={row.role}
                      options={this.generateRoleOptions(row)}
                      {... { on: { change: (value) => { row.role = value } } }}
                    ></a-select>
                  ]
                }
              }
            },
            {
              title: 'Aggregation Type',
              field: 'aggregation_type',
              minWidth: 150,
              align: 'center',
              editRender: true,
              slots: {
                header: this.slotsHeader,
                default: ({ row, column }) => {
                  let cell
                  if (row.aggregation_type) {
                    cell = <span>{row.aggregation_type}</span>
                  }
                  return [
                    <span
                      class={{ disabled: !this.isCanSettingAggregationType(row) }}
                    >
                      {cell}
                    </span>
                  ]
                },
                edit: ({ row, column }) => {
                  return [
                    <a-select
                      size="small"
                      value={row.aggregation_type}
                      options={this.generateAggregationTypeOptions(row)}
                      {... { on: { change: (value) => { row.aggregation_type = value } } }}
                    ></a-select>
                  ]
                }
              }
            }
            // {
            //   title: 'Group By',
            //   field: 'enabled_as_group_by',
            //   align: 'center',
            //   minWidth: 100,
            //   slots: {
            //     header: this.slotsHeader,
            //     default: ({ row, column }) => {
            //       return [
            //         <a-switch
            //           disabled={!this.isCanSettingGroupBy(row)}
            //           checked={!!row.enabled_as_group_by}
            //           {...{ on: { change: checked => (row.enabled_as_group_by = checked) } }}
            //         ></a-switch>
            //       ]
            //     }
            //   }
            // }
          ]
        },
        {
          title: 'Filter',
          align: 'center',
          children: [
            {
              title: 'Show',
              align: 'center',
              field: 'enabled_as_form_element',
              minWidth: 90,
              // sortable: true,
              slots: {
                header: this.slotsHeader,
                default: ({ row, column }) => {
                  return [
                    <a-switch
                      disabled={!row.effective || this.isChildField(row)}
                      checked={!!row.enabled_as_form_element}
                      {...{ on: { change: (checked) => this.formEnableChange(checked, row) } }}
                    ></a-switch>
                  ]
                }
              }
            },
            {
              title: 'Sequence',
              align: 'center',
              field: 'form_element_sequence',
              minWidth: 110,
              editRender: { name: 'AInputNumber', props: { min: 1, max: 100 }, autoselect: true },
              slots: {
                header: this.slotsHeader
              },
              sortable: true
            },
            {
              title: 'Element Type',
              field: 'form_element_type',
              minWidth: 220,
              editRender: true,
              slots: {
                header: this.slotsHeader,
                default: ({ row, column }) => {
                  let cell
                  if (row.form_element_type) {
                    cell = <span>{row.form_element_type}</span>
                  }
                  return [
                    <span
                      class={{ disabled: !this.isCanSettingForm(row) }}
                    >
                      {cell}
                    </span>
                  ]
                },
                edit: ({ row, column }) => {
                  return [
                    <a-select
                      size="small"
                      value={row.form_element_type}
                      show-search
                      options={this.generateFormElementOptions(row)}
                      {... { on: { change: (value) => this.handleFormElementChange(value, row) } }}
                    ></a-select>
                  ]
                }
              }
            },
            {
              title: 'Child Field',
              field: 'child_field_name',
              minWidth: 150,
              editRender: true,
              slots: {
                header: this.slotsHeader,
                default: ({ row, column }) => {
                  let cell
                  if (row.child_field_name) {
                    cell = <span>{row.child_field_name}</span>
                  }
                  return [
                    <span
                      class={{ disabled: !this.isCanSettingChildField(row) }}
                    >
                      {cell}
                    </span>
                  ]
                },
                edit: ({ row, column }) => {
                  return [
                    <a-select
                      size="small"
                      value={row.child_field_name}
                      show-search
                      options={this.generateChlidFieldOptions(row)}
                      {... { on: { change: (value) => this.handleChildFieldChange(value, row) } }}
                    ></a-select>
                  ]
                }
              }
            },
            {
              title: 'Optional Values',
              field: 'dictionary_category_uid',
              minWidth: 150,
              slots: {
                header: this.slotsHeader,
                default: ({ row, column }) => {
                  let cell
                  if (row.dictionary_category_uid && row.dictionary_category_name) {
                    cell = (
                      <span>
                        {row.dictionary_category_name}
                      </span>
                    )
                  } else {
                    cell = <div class="placeholder">.</div>
                  }
                  return [
                    <span
                      {...{ on: { click: () => this.settingDict(row) } }}
                      class="optional-values-cell"
                      class={{ disabled: !this.isCanSettingDict(row) }}
                    >
                      {cell}
                    </span>
                  ]
                }
              }
            },
            {
              title: 'Default Value(s)',
              field: 'defaults_category_uid',
              minWidth: 150,
              slots: {
                header: this.slotsHeader,
                default: ({ row, column }) => {
                  let cell
                  if (row.defaults_category_uid && row.defaults_category_name) {
                    cell = <span>{row.defaults_category_name}</span>
                  } else {
                    cell = <div class="placeholder">.</div>
                  }
                  return [
                    // class={{ disabled: this.isCanSettingDict(row) }}
                    <span
                      {...{ on: { click: () => this.showDefaultValStrategy(row) } }}
                      class={{ disabled: !this.isCanSettingDefaultVal(row) }}
                    >
                      {cell}
                    </span>
                  ]
                }
              }
            }
          ]
        }
      ]
    },
    leftFields () {
      const realColumns = this.getRealColumns(this.customiseColumns)
      return this.tableData.filter(x => x.enabled_as_list_element && (!realColumns.length || !realColumns.some(y => y.field === x.field_name))).sort((a, b) => a - b)
    }
  },
  async created () {
    await this.loadFormTableSetting()
  },
  methods: {
    tabChange (key) {
      if (key === 'index') {
        this.getIndexs()
      } else if (key === 'basic') {
        this.refreshTable()
      }
    },
    onCopySuccess () {
      this.$message.success('copy success')
    },
    onCopyError () {
      this.$message.warning('copy error')
    },
    handleImportChange (e) {
      this.importConfigStr = e.target.value
    },
    importConfig () {
      try {
        this.config = JSON.parse(this.importConfigStr)
        this.importModalVisible = false
      } catch (e) {
        this.$message.warning('format error')
      }
    },
    formEnableChange (checked, row) {
      row.enabled_as_form_element = checked
      this.updateChainField(row, 'enabled_as_form_element')
    },
    generateRoleOptions (field) {
      return roleOptions.map(x => {
        // if (x.value === 'KPI') {
        //   x.disabled = !['INT', 'LONG', 'DECIMAL'].includes(field.field_type)
        // }
        return x
      })
    },
    generateAggregationTypeOptions (field) {
      return aggregationTypeOptions.map(x => ({
        ...x
        // disabled: x.value && field.role === 'DIMENSION' && x.value !== 'COUNT'
      }))
    },
    clickZoom () {
      this.isMaximized = !this.isMaximized
      this.$refs.confTable.zoom()
      this.handleZoomChange()
    },
    handleZoomChange () {
      this.isMaximized = this.$refs.confTable.isMaximized()
      const appDom = this.$root.$el
      if (!appDom) return
      if (this.isMaximized) {
        appDom.style.overflow = 'hidden'
      } else {
        appDom.style.overflow = 'auto'
      }
    },
    refreshTable () {
      this.$nextTick(() => {
        this.$refs.confTable.refreshColumn(true)
      })
    },
    async getIndexs () {
      this.indexLoading = true
      const result = await this.$api.getIndexs(this.uid).finally(() => {
        this.indexLoading = false
      })
      if (result) {
        this.indexList = result
        if (this.indexList.length) {
          this.handleClickRow(this.indexList[0])
        }
      }
    },
    clearFormFieldKeys (field) {
      this.clearFieldKeys(field, this.formKeys)
    },
    clearFieldKeys (field, keys) {
      keys.forEach(key => {
        field[key] = null
      })
    },
    initChildField (childField, parentField) {
      childField.enabled_as_form_element = true
      childField.form_element_type = parentField.form_element_type
    },
    handleChildFieldChange (value, field) {
      const childField = this.tableData.find(x => x.field_name === value)
      if (childField && this.formKeys.some(key => childField[key])) {
        this.clearFormFieldKeys(childField)
        this.initChildField(childField, field)
        field.child_field_name = value
        // this.$confirm({
        //   title: 'Tips',
        //   content: `Form Configuration of Child Field ${value} will be clear, Confirm ?`,
        //   onOk: () => {
        //     this.clearFormFieldKeys(childField)
        //     this.initChildField(childField)
        //     field.child_field_name = value
        //   }
        // })
      } else {
        if (childField) {
          this.initChildField(childField)
        }
        field.child_field_name = value
      }
    },
    handleFormElementChange (value, field) {
      field.form_element_type = value
    },
    isCanUseElement (fieldType, elementType) {
      if (['DATE', 'TIMESTAMP', 'TIME'].includes(fieldType)) {
        if (fieldType === 'DATE' && dateEelmentType[elementType]) return true
        if (fieldType === 'TIME' && timeEelmentType[elementType]) return true
        if (fieldType === 'TIMESTAMP' && dateTimeEelmentType[elementType]) return true
        return false
      } else if (!dateEelmentType[elementType] && !timeEelmentType[elementType] && !dateTimeEelmentType[elementType]) {
        return true
      }
      return false
    },
    generateFormElementOptions (field) {
      return typeOptions.map(x => ({
        disabled: !this.isCanUseElement(field.field_type, x.value),
        ...x
      }))
    },
    handleSortChange ({ sortList }) {
      if (sortList.length) {
        const sortItem = sortList[0]
        this.activeSort = {
          field: sortItem.property,
          order: sortItem.order
        }
      } else {
        this.activeSort = null
      }
    },
    activeMethod ({ row, column }) {
      if (!row.effective) return false

      if (this.isChildField(row) && this.formKeys.includes(column.property) && column.property !== 'child_field_name') {
        return false
      }
      if (column.property === 'child_field_name' && !this.isCanSettingChildField(row)) {
        return false
      }
      if (['list_element_sequence', 'list_element_width', 'sort_direction', 'enabled_as_group_by', 'role'].includes(column.property) && !this.isCanSettingTable(row)) {
        return false
      }
      if (['form_element_sequence', 'form_element_type'].includes(column.property) && !this.isCanSettingForm(row)) {
        return false
      }
      if (column.property === 'aggregation_type' && !this.isCanSettingAggregationType(row)) {
        return false
      }
      return true
    },
    cellClassName ({ row, column }) {
      const disabledCellClass = 'disabled-cell'
      const editableCellClass = 'clickable-cell'
      if (!row.effective) return disabledCellClass
      if (this.disabledEditColumns.includes(column.property)) return disabledCellClass
      if (this.isChildField(row) && this.formKeys.includes(column.property) && column.property !== 'child_field_name') {
        return disabledCellClass
      }
      if (column.property === 'child_field_name' && !this.isCanSettingChildField(row)) {
        return disabledCellClass
      }
      if (['list_element_sequence', 'list_element_width', 'sort_direction', 'enabled_as_group_by', 'role'].includes(column.property) && !this.isCanSettingTable(row)) {
        return disabledCellClass
      }
      if (['form_element_sequence', 'form_element_type'].includes(column.property) && !this.isCanSettingForm(row)) {
        return disabledCellClass
      }
      if (column.property === 'dictionary_category_uid' && !this.isCanSettingDict(row)) {
        return disabledCellClass
      }
      if (column.property === 'defaults_category_uid' && !this.isCanSettingDefaultVal(row)) {
        return disabledCellClass
      }
      if (column.property === 'resource_structure_uid' && !this.isCanSettingPermission(row)) {
        return disabledCellClass
      }
      if (column.property === 'aggregation_type' && !this.isCanSettingAggregationType(row)) {
        return disabledCellClass
      }
      return editableCellClass
    },
    handleEditFinish ({ row, column }) {
      if (this.activeSort && column.property === this.activeSort.field) {
        this.$refs.confTable.sort(this.activeSort.field, this.activeSort.order)
      }
      if (column.property === 'form_element_type') {
        if (!selectFormType[row[column.property]]) {
          row.child_field_name = null
          row.dictionary_category_uid = null
          row.dictionary_category_name = null
        }
        if (this.isCascaderRootField(row)) {
          this.updateChainElementType(row)
        }
      }
      // if (column.property === 'role' && row['role'] !== 'DIMENSION') {
      //   row.enabled_as_group_by = false
      // }
      if (column.property === 'role') {
        if (!row['role'] || row['role'] === 'DIMENSION') {
          row.aggregation_type = null
        } else if (row['role'] === 'KPI' && !row.aggregation_type) {
          row.aggregation_type = 'SUM'
        }
        // else if (row['role'] === 'DIMENSION' && row.aggregation_type && row.aggregation_type !== 'COUNT') {
        //   row.aggregation_type = 'COUNT'
        // }
      }
    },
    updateChainElementType (field) {
      this.updateChainField(field)
    },
    updateChainField (field, key = 'form_element_type') {
      if (!field.child_field_name) return
      const childField = this.tableData.find(x => field.child_field_name === x.field_name)
      if (childField) {
        childField[key] = field[key]
        this.updateChainField(childField, key)
      }
    },
    slotsHeader ({ column }) {
      return [
        <span><template slot="title"></template><a-icon type="edit" /> {column.title}</span>
      ]
    },
    rowClassName ({ row }) {
      if (!row.effective) {
        return 'disabled-row'
      }
    },
    async copyDataFacet (sourceId) {
      this.config.confirmLoading = true
      const result = await this.$api.copyFormTableSetting(sourceId, this.uid).finally(() => {
        this.config.confirmLoading = false
      })
      this.config = result
    },
    showCopyConfigModal () {
      this.copyModalVisible = true
    },
    getRealColumns (columns, realColumns = []) {
      columns.forEach(column => {
        if (column.field) {
          realColumns.push(column)
        } else {
          column.children && this.getRealColumns(column.children, realColumns)
        }
      })
      return realColumns
    },
    getNameSuffix (number) {
      return number ? `${number + 1}` : ''
    },
    rerenderHeader () {
      this.$nextTick(() => {
        this.$refs.customiseGrid && this.$refs.customiseGrid.loadColumn(this.customiseColumns)
      })
    },
    hasLastRealColumn (groupColumn) {
      const lastRealColumn = this.getLastRealColumn()
      return groupColumn.children.length && groupColumn.children[groupColumn.children.length - 1].field === lastRealColumn.field
    },
    generateList (data, list = []) {
      for (let i = 0; i < data.length; i++) {
        const node = data[i]
        list.push(node)
        if (node.children) {
          this.generateList(node.children, list)
        }
      }
      return list
    },
    isLastGroupColumn (groupColumn) {
      const platColumns = this.generateList(this.customiseColumns)
      const allGroupColumns = platColumns.filter(x => !x.field)
      if (allGroupColumns.length === 0) return false
      return groupColumn.title === allGroupColumns[allGroupColumns.length - 1].title
    },
    isFirstNullGroupColumn (groupColumn) {
      const platColumns = this.generateList(this.customiseColumns)
      const allGroupColumns = platColumns.filter(x => !x.field && x.children.length === 0)
      if (allGroupColumns.length === 0) return false
      return groupColumn.title === allGroupColumns[0].title
    },
    addColumnGroup (parent) {
      const baseGroup = {
        title: !parent ? `表头名称${this.getNameSuffix(this.customiseColumns.length)}` : `${parent.title} child ${this.getNameSuffix(parent.children.length)}`,
        align: 'center',
        minWidth: 300,
        children: [],
        slots: {
          header: ({ column, columnIndex }) => {
            const menuItems = this.leftFields.map((x, i) => <a-menu-item key={i} disabled={!!i} {...{ on: { click: () => this.addRealColumn(baseGroup, x) } }}>{x.field_label}</a-menu-item>)
            return [<span>
            <EditCell value={column.title} disabled={!!column.field} {...{ on: { input: (val) => { column.title = val; baseGroup.title = val } } }}></EditCell>
            <div class="add-column-btn-group">
            { !baseGroup.children.some(child => child.field) ? <a-button class="add-column-btn" icon="plus" size="small" {...{ on: { click: () => this.addColumnGroup(baseGroup) } }}>分组</a-button> : '' }
            { (this.hasLastRealColumn(baseGroup) || this.isFirstNullGroupColumn(baseGroup)) && (baseGroup.children.some(child => child.field) || !baseGroup.children.length) ? <a-dropdown>
              <a-button class="add-column-btn" icon="plus" size="small">字段</a-button>
              <a-menu slot="overlay">
                { menuItems }
              </a-menu>
            </a-dropdown> : '' }
            { this.isLastGroupColumn(baseGroup) ? <a-button class="add-column-btn" type="primary" type="danger" size="small" icon="minus" {...{ on: { click: () => { this.removeGroupColumn(parent ? parent.children : this.customiseColumns, baseGroup) } } }}></a-button> : '' }
            </div>
            </span>]
          }
        }
      }
      if (parent) {
        parent.children.push(baseGroup)
      } else {
        this.customiseColumns.push(baseGroup)
      }
      this.rerenderHeader()
    },
    removeGroupColumn (list, groupColumn) {
      this.removeItem(list, groupColumn.label)
      this.rerenderHeader()
    },
    removeRealColumn (list, field) {
      this.removeItem(list, field, 'field')
      this.rerenderHeader()
    },
    removeItem (list, key, keyFieldName = 'label') {
      const index = list.findIndex(x => x[keyFieldName] === key)
      if (index > -1) {
        list.splice(index, 1)
      }
    },
    getLastRealColumn () {
      const realColumns = this.getRealColumns(this.customiseColumns)
      if (realColumns.length === 0) return false
      return realColumns[realColumns.length - 1]
    },
    isLastRealColumn (fieldName) {
      const lastRealColumn = this.getLastRealColumn()
      if (!lastRealColumn) return false
      return lastRealColumn.field === fieldName
    },
    addRealColumn (parent, column) {
      const key = Math.random().toString()
      const baseColumn = {
        title: column.field_label,
        field: column.field_name,
        minWidth: column.list_element_width,
        key,
        slots: {
          header: ({ column }) => {
            return [<span>{column.title}{ this.isLastRealColumn(baseColumn.field) ? <div class="add-column-btn-group"><a-button type="primary" type="danger" size="small" icon="minus" {...{ on: { click: () => { this.removeRealColumn(parent.children, baseColumn.field) } } }}></a-button></div> : '' }</span>]
          }
        }
      }
      parent.children.push(baseColumn)
      this.rerenderHeader()
    },
    currentChangeEvent ({ row }) {
      this.handleClickRow(row)
    },
    handleClickRow (row) {
      this.$refs.indexList.setCurrentRow(row)
      this.activeIndex = row
    },
    showDefaultValStrategy (field) {
      if (!this.isCanSettingDefaultVal(field)) return
      this.activeField = field
      this.defaultValStrategyVisible = true
      this.d = field.defaults_category_uid
    },
    isChildField (field) {
      return this.tableData.some(x => x.enabled_as_form_element && x.child_field_name === field.field_name)
    },
    isCanSettingDefaultVal (field) {
      return field.effective && field.enabled_as_form_element && !this.isChildField(field)
    },
    isCascaderRootField (field) {
      return cascaderType[field.form_element_type] && !this.isChildField(field)
    },
    isCanSettingChildField (field) {
      return field.effective && field.enabled_as_form_element && (this.isCascaderRootField(field) || this.isChildField(field))
    },
    isCanSettingDict (field) {
      return field.effective && field.enabled_as_form_element && !!selectFormType[field.form_element_type] && !this.isChildField(field)
    },
    isCanSettingForm (field) {
      return field.effective && field.enabled_as_form_element
    },
    isCanSettingGroupBy (field) {
      return this.isCanSettingTable(field) && field.role === 'DIMENSION'
    },
    isCanSettingAggregationType (field) {
      return this.isCanSettingTable(field) && field.role === 'KPI'
    },
    isCanSettingTable (field) {
      return field.effective && field.enabled_as_list_element
    },
    isCanSettingPermission (field) {
      return this.isCanSettingTable(field)
    },
    settingDict (field) {
      if (!this.isCanSettingDict(field)) {
        return
      }
      this.activeDictCategoryId = field.dictionary_category_uid
      this.activeField = field
      this.dictModalVisible = true
    },
    settingPermission (field) {
      if (!this.isCanSettingPermission(field)) {
        return
      }
      this.activeField = field
      this.permissionModalVisible = true
    },
    handleSelectedDict (id, name) {
      this.activeField.dictionary_category_uid = id
      this.activeField.dictionary_category_name = name
      this.activeField = null
    },
    handleSelectedDefaultVal (id, name) {
      this.activeField.defaults_category_uid = id
      this.activeField.defaults_category_name = name
      this.activeField = null
    },
    handleSelectPermission (node) {
      this.activeField.resource_structure_uid = node?.uid
      this.activeField.resource_structure_name = node?.name
      this.activeField.resource_category_uid = node?.category.uid
      this.activeField.resource_category_name = node?.category.name
      this.activeField = null
    },
    validate (fields) {
      if (fields.some(x => x.enabled_as_form_element && !x.form_element_type && !this.isChildField(x))) {
        this.$message.warning('Element Type can not be empty if show form')
        return false
      }
      for (var i in fields) {
        const field = fields[i]
        const fieldName = field.field_name
        const queryType = field.form_element_type

        if (this.isCanSettingDict(field)) {
          if (!field.dictionary_category_uid) {
            this.$message.warning(
              fieldName + "'s Element Type is " + queryType + ', Optional Values can not be empty'
            )
            return false
          }
          if (this.isCascaderRootField(field) && !field.child_field_name) {
            this.$message.warning('Chlid Field of Root Cascader Field can not be empty')
            return false
          }
        }
        // else if (rowData.dictionary_category_uid) {
        //   this.$message.warning(fieldName + "'s query Type is " + queryType + ', can not use Optional Values')
        //   return false
        // }
      }
      return true
    },
    generateChlidFieldOptions (row) {
      const usedChildFieldNames = this.tableData.filter(x => x.child_field_name).map(x => x.child_field_name)
      return [{ label: 'None', value: '' }, ...this.tableData.filter(x => !usedChildFieldNames.includes(x.field_name) && x.field_name !== row.field_name).map(item => ({ label: item.field_name, value: item.field_name }))]
    },
    async loadFormTableSetting () {
      this.dataLoading = true
      const result = await this.$api
        .getFormTableSetting(this.uid)
        .finally(() => (this.dataLoading = false))
        .catch(err => {
          if (err.response.status) {
            this.$emit('noFindDfk', this.uid)
          }
        })
      if (!result) return
      this.config = result
      this.copyData = cloneDeep(this.tableData)
      // this.setAllActiveRow(this.tableData)
      // this.chlidFieldOptions = this.generateChlidFieldOptions(this.tableData)
      this.dataLoading = false
    },
    async save () {
      const valid = this.validate(this.tableData)
      if (!valid) return
      this.saveLoading = true
      const requestData = cloneDeep(this.config)
      requestData.basic.fields = requestData.basic.fields.filter(x => x.effective)
      const result = await this.$api
        .saveFormTableSetting(requestData)
        .finally(() => (this.saveLoading = false))
      this.config = result
    },
    async refresh () {
      this.refreshLoading = true
      await this.$api.refreshFormTableSetting(this.uid).finally(() => (this.refreshLoading = false))
      this.loadFormTableSetting()
    },
    preview () {
      const { href } = this.$router.resolve({
        name: 'PreviewTable',
        query: {
          df_key: this.config.df_key
        }
      })
      window.open(href, '_blank')
    },
    revertRow (record) {
      const copyItem = this.copyData.find(x => x.id === record.id)
      Object.assign(record, copyItem)
    }
    // revertTable () {
    //   this.tableData = cloneDeep(this.copyData)
    //   this.$message.success('Operation succeeded')
    // }
  }
}
</script>

<style lang="less" scoped>
/deep/ .btn-row {
  padding-bottom: 5px;
  .ant-btn {
    margin-right: 5px;
    &:last-child {
      margin-right: 0;
    }
  }
}
.optional-values-cell {
  text-overflow: ellipsis;
  overflow: hidden;
}
/deep/ .ant-breadcrumb {
  line-height: 32px;
}
// .disabled {
//   background-color: #eee;
//   display: block;
// }
.table-box {
  height: calc(100vh - 200px);
  position: relative;

  .zoom-btn {
    position: absolute;
    top: 4px;
    right: 4px;
    display: none;
    z-index: 108;
  }
  &:hover .zoom-btn {
    display: block
  }
}
.zoom-box {
  z-index: 107;
}
/deep/ .table-box .ant-input-number,
/deep/ .table-box .ant-select {
  width: 100%;
}
/deep/ .hide-body .vxe-table--body-wrapper {
  height: 10px;
}

/deep/ .editable-cell {
  display: inline-block;
}
/deep/ .add-column-btn {
  margin-left: 5px;
}
/deep/ .add-column-btn-group {
  display: none;
}
/deep/ .vxe-header--column:hover {
  .add-column-btn-group {
    display: block;
    position: absolute;
    left: 10px;
    top: 10px;
  }
}
/deep/ .costomise-header .vxe-header--column {
 font-size: 13px;
 font-weight: 400;
 color: #606266
}
.form-table-setting {
  border-top: 0;
}
/deep/ .disabled-cell {
  background: #fbfbfb;
}
/deep/ .clickable-cell {
  cursor: pointer;
  &:hover {
    box-shadow: inset 0px 0px 0px 2px #409eff;
  }
}
/deep/ .vxe-cell--edit-icon {
  color: #606266;
}
/deep/ .vxe-table.vxe-editable .vxe-body--column.col--dirty:before {
  content: "";
  border-color: transparent #40a9ff transparent transparent;
}
// /deep/ .vxe-cell:after {
//   content: '';
//   display: block;
// }
.title {
  margin-bottom: 5px;
}
/deep/ .advanced-config {
  .ant-input-number, .ant-select {
    width: 60px
  }
  .ant-radio-group {
    width: 100%
  }
}
/deep/ .vxe-toolbar {
  height: 32px;
}
.placeholder {
  visibility: hidden;
}
/deep/ .config-tabs .ant-tabs-bar {
  margin-bottom: 10px;
}
/deep/ .config-tabs.ant-tabs .ant-tabs-extra-content {
    line-height: 35px;
}
</style>
