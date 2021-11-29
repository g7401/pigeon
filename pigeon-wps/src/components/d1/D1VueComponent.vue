
<template>
  <!-- eslint-disable -->
  <div
    class="d1-vue-component"
    :class="{'d1-vue-component-selection': showHlightCurrentRow}"
  >
    <FieldChooseModal v-model="fieldChooseModalVisible" :fields="choosedFields" @selected="handleSelectVisibleFields"></FieldChooseModal>
    <AlternativeFieldModal v-model="alternativeFieldModalVisible" :fields="groupByFields" :choosedFields="choosedFields" @selected="handleSetGroupBy"></AlternativeFieldModal>
    <template v-if="infoVisible">
      <a-collapse
        class="mg-b-5"
        v-if="pageSettingData.df_name"
        expandIconPosition="right"
        :activeKey="pageSettingData.df_description && '1'"
      >
        <a-collapse-panel
          key="1"
          :header="pageSettingData.df_name"
        >
          <p v-if="pageSettingData.df_description" v-html="pageSettingData.df_description"></p>
          <span v-else>N/A</span>
        </a-collapse-panel>
      </a-collapse>
    </template>
    <a-icon
      class="tree-loading-icon"
      type="loading"
      v-if="confLoading"
    />
    <div
      v-if="showForm && pageSettingData.form.length"
      class="search-box"
      :class="{splitline: showToolbarButtonList}"
    >
      <a-form-model
        ref="queryForm"
        layout="vertical"
        :model="pageSettingData"
        :labal-col="{ span: 5 }"
        v-if="queryVisible"
      >
        <a-row
          :gutter="0"
          class="form-row"
        >
          <a-col
            class="query-form-box"
            :xs="24"
            :sm="24"
            :md="24"
          :lg="24"
            :xl="24"
            :xxl="24"
          >
          <a-row :gutter="10">
            <template v-for="(item, index) in visibleForm">
              <a-col
                class="form-item-col"
                v-if="item.form_element_type === formType.CASCADER || item.form_element_type === formType.CASCADER_MULTIPLE"
                :xs="12"
                :sm="12"
                :md="12"
                :lg="8"
                :xl="6"
                :xxl="4"
              >
                <a-form-item
                  :label="item.dictionary_category_name"
                >
                <VirtualTreeSelect :title="'选择' + item.dictionary_category_name" :multiple="item.form_element_type === formType.CASCADER_MULTIPLE" :nodeMethod="nodeMethod" :extraData="{dictionary_category_uid: item.dictionary_category_uid}" v-model="item.field_default_values" :defaultSelectedOptions="item.default_field_optional_values"></VirtualTreeSelect>
                </a-form-item>
              </a-col>
              <a-col
                class="form-item-col"
                v-else-if="item.form_element_type === formType.DATE_RANGE || item.form_element_type === formType.TIMESTAMP_RANGE"
                :xs="12"
                :sm="12"
                :md="12"
                :lg="8"
                :xl="6"
                :xxl="4"
              >
                <!-- DATE RANGE -->
                <a-form-item
                  v-if="item.form_element_type === formType.DATE_RANGE || item.form_element_type === formType.TIMESTAMP_RANGE"
                  :label="item.field_label"
                >
                  <a-range-picker size="small" v-model="item.field_default_values" :showTime="item.form_element_type === formType.TIMESTAMP_RANGE" @change="paramsChange" />
                </a-form-item>
              </a-col>
              <a-col
                class="form-item-col"
                v-else-if="[formType.DATE_SINGLE, formType.TIME_SINGLE, formType.TIMESTAMP_SINGLE].includes(item.form_element_type)"
                :key="item.field_label"
                :xs="12"
                :sm="12"
                :md="12"
                :lg="8"
                :xl="6"
                :xxl="4"
              >
                <a-form-item
                  v-if="item.form_element_type === formType.DATE_SINGLE || item.form_element_type === formType.TIMESTAMP_SINGLE"
                  :label="item.field_label"
                >
                  <a-date-picker
                    size="small"
                    allowClear
                    :showTime="item.form_element_type === formType.TIMESTAMP_SINGLE"
                    :placeholder="item.form_element_type === formType.TIMESTAMP_SINGLE ? '请选择日期时间' : '请选择日期'"
                    v-model="item.field_default_values"
                    @change="paramsChange"
                  ></a-date-picker>
                </a-form-item>
                <a-form-item
                  v-if="item.form_element_type === formType.TIME_SINGLE"
                  :label="item.field_label"
                >
                  <a-time-picker size="small" placeholder="选择时间" allowClear v-model="item.field_default_values" @change="paramsChange" />
                </a-form-item>
              </a-col>
              <a-col
                class="form-item-col"
                :xs="12"
                :sm="12"
                :md="12"
                :lg="8"
                :xl="6"
                :xxl="4"
                :key="item.field_label"
                v-else-if="item.form_element_type === formType.TIME_RANGE"
              >
                <a-form-item
                  :label="item.field_label"
                >
                  <a-row :gutter="24">
                    <a-col
                      :xs="12"
                      :sm="12"
                      :md="12"
                      :lg="12"
                      class="time-range date-line"
                    >
                      <a-time-picker size="small" placeholder="选择时间" allowClear v-model="item.field_value__start__" @change="paramsChange" />
                    </a-col>
                    <a-col
                      :xs="12"
                      :sm="12"
                      :md="12"
                      :lg="12"
                      class="time-range"
                    >
                      <a-time-picker size="small" placeholder="选择时间" allowClear v-model="item.field_value__end__" @change="paramsChange" />
                    </a-col>
                  </a-row>
                </a-form-item>
              </a-col>
              <!-- number range -->
              <a-col
                class="form-item-col"
                :xs="12"
                :sm="12"
                :md="12"
                :lg="8"
                :xl="6"
                :xxl="4"
                :key="item.field_label"
                v-else-if="item.form_element_type === formType.NUMBER_RANGE"
              >
                <a-form-item
                  :label="item.field_label"
                >
                  <a-row :gutter="24">
                    <a-col
                      :xs="12"
                      :sm="12"
                      :md="12"
                      :lg="12"
                      class="numberInputRange date-line"
                    >
                      <a-input-number
                        size="small"
                        v-model="item.field_value__start__"
                        @change="paramsChange"
                      ></a-input-number>
                    </a-col>
                    <a-col
                      :xs="12"
                      :sm="12"
                      :md="12"
                      :lg="12"
                      class="numberInputRange"
                    >
                      <a-input-number
                        size="small"
                        v-model="item.field_value__end__"
                        @change="paramsChange"
                      ></a-input-number>
                    </a-col>
                  </a-row>
                </a-form-item>
              </a-col>

              <a-col
                class="form-item-col"
                :key="item.field_label"
                :xs="12"
                :sm="12"
                :md="12"
                :lg="8"
                :xl="6"
                :xxl="4"
                v-else
              >
                <a-form-item
                  v-if="[formType.ASSOCIATING_SINGLE, formType.ASSOCIATING_MULTIPLE].includes(item.form_element_type)"
                  :label="item.field_label"
                >
                  <a-select
                    size="small"
                    allowClear
                    v-model="item.field_default_values"
                    :mode="item.form_element_type === formType.ASSOCIATING_MULTIPLE ? 'multiple' : undefined"
                    show-search
                    :default-active-first-option="false"
                    :show-arrow="false"
                    :filter-option="false"
                    :not-found-content="null"
                    :maxTagCount="5"
                    :maxTagTextLength="20"
                    @change="paramsChange"
                    @search="handleSelectSearch($event, item)"
                  >
                    <template v-if="item.isSearch">
                      <a-select-option
                        v-for="option in item.field_optional_values.filter(x => x.option_label.indexOf(searchValueMap[item.field_name]) !== -1)"
                        :key="option.option_label"
                        :value="option.option_value"
                      >
                        {{ option.option_label }}
                      </a-select-option>
                    </template>
                    <template v-else>
                      <a-select-option
                        v-for="option in item.default_field_optional_values"
                        :key="option.label"
                        :value="option.value"
                      >
                        {{ option.label }}
                      </a-select-option>
                    </template>
                  </a-select>
                </a-form-item>

                <!--下拉列表单选-->
                <a-form-item
                  v-if="item.form_element_type === formType.DROP_DOWN_LIST_SINGLE"
                  :label="item.field_label"
                >
                  <a-select
                    size="small"
                    v-model="item.field_default_values"
                    @change="paramsChange"
                    allowClear
                  >
                    <a-select-option
                      v-for="option in item.field_optional_values"
                      :key="option.option_label"
                      :value="option.option_value"
                    >{{ option.option_label }}</a-select-option>
                  </a-select>
                </a-form-item>
                <!--下拉列表多选-->
                <a-form-item
                  v-if="item.form_element_type === formType.DROP_DOWN_LIST_MULTIPLE"
                  :label="item.field_label"
                >
                  <a-select
                    size="small"
                    v-model="item.field_default_values"
                    mode="multiple"
                    :maxTagCount="5"
                    :maxTagTextLength="20"
                    allowClear
                    @change="paramsChange"
                  >
                    <!--多选加个multiple-->
                    <a-select-option
                      v-for="option in item.field_optional_values"
                      :key="option.option_label"
                      :value="option.option_value"
                    >{{ option.option_label }}</a-select-option>
                  </a-select>
                </a-form-item>
                <!-- text -->
                <a-form-item
                  :key="item.field_label"
                  v-if="textFormType[item.form_element_type]"
                  :label="item.field_label"
                  prop="field_value"
                  :ref="'text'+index"
                >
                  <!--prop校验-->
                  <a-input
                    size="small"
                    v-model.trim="item.field_default_values"
                    allowClear
                    @change="paramsChange"
                    @keyup.enter="handleQuery"
                  ></a-input>

                </a-form-item>
                <!-- 数字输入框 -->
                <!-- <a-form-item
                  v-if="item.form_element_type === formType.NUMBER"
                  :label="item.field_label"
                >
                  {{ item.field_default_values }}
                  <a-input-number
                    v-model.trim="item.field_default_values"
                    @change="paramsChange"
                    size="small"
                  ></a-input-number>
                </a-form-item> -->
              </a-col>
            </template>
            </a-row>
          </a-col>
          <!-- <a-col
            :xs="0"
            :sm="0"
            :md="0"
            :lg="0"
            :xl="5"
            :xxl="5"
          >
          <toggle-list
            class="pull-right"
            :list="pageSettingData.form"
            :collapseStatus="isFormCollapse"
            :showLenth="sliceLength"
            @change="handleToggleChange"
          ></toggle-list>
          <div class="search-btn-group">
            <a-button
              type="primary"
              :loading="dataLoading"
              icon="search"
              @click="handleQuery"
            >
              查询
            </a-button>
            <a-button
              :loading="dataLoading"
              icon="reload"
              @click="handleResetForm"
            >
              重置
            </a-button>
          </div>
          </a-col> -->
        </a-row>
        <a-row class="sm-screen-search-btn-box" :gutter="0">
          <a-col
            :xs="24"
            :sm="24"
            :md="24"
            :lg="24"
            :xl="24"
            :xxl="24"
            style="min-height: 32px"
          >
          <div class="search-btn-group">
            <toggle-list
              class="mg-r-10"
              :list="pageSettingData.form"
              :collapse="isFormCollapse"
              :showLenth="sliceLength"
              @change="handleToggleChange"
            ></toggle-list>
            <a-button
              type="primary"
              size="small"
              :loading="dataLoading"
              icon="search"
              @click="handleQuery"
            >
              查询
            </a-button>
            <a-button
              size="small"
              :loading="dataLoading"
              icon="reload"
              @click="handleResetForm"
            >
              重置
            </a-button>
          </div>
          </a-col>
        </a-row>
      </a-form-model>
    </div>

    <div class="data-region">
      <!-- toolbar -->
      <div
        class="tool-bar"
        v-if="!confLoading && showToolbarButtonList"
      >
        <a-row
          :gutter="0"
          style="margin:0"
        >
          <a-col
            :span="16"
            style="padding:0"
          >
            <a-button
              v-if="showSelectColumn"
              type="primary"
            >Select Column</a-button>
            <a-tooltip title="UTF8 with BOM 字符编码">
              <a-button
                class="mg-r-10"
                v-if="showExportButton"
                :loading="loadingDownloadMap['csv']"
                type="primary"
                :size="toolbarBtnSize"
                @click="showExportModal('csv')"
                :disabled="exportDisabled('csv')"
                icon="download"
              >
                导出CSV
              </a-button>
            </a-tooltip>
            <a-tooltip title="UTF8 with BOM 字符编码">
              <a-button
                class="mg-r-10"
                v-if="showExportButton"
                :loading="loadingDownloadMap['csvAndCompress']"
                type="primary"
                :size="toolbarBtnSize"
                @click="showExportModal('csvAndCompress')"
                :disabled="exportDisabled('csvAndCompress')"
                icon="download"
              >
                导出CSV并压缩
              </a-button>
            </a-tooltip>
            <a-tooltip title="最大100万行">
              <a-button
                v-if="showExportButton"
                :loading="loadingDownloadMap['excel']"
                type="primary"
                :size="toolbarBtnSize"
                @click="showExportModal('excel')"
                :disabled="exportDisabled('excel')"
                icon="download"
              >
                导出 Excel
              </a-button>
            </a-tooltip>
          </a-col>
          <a-col :span="8"
          >
            <div class="toolbar-btn-group">
              <a-button
                v-for="(button, index) in toolbarButtonList"
                :key="index"
                :type="button.type"
                :size="toolbarBtnSize"
                @click="handleGeneralButtonClick(button.name)"
              >
                <a-icon
                  v-if="button.icon"
                  :type="button.icon"
                />{{button.label}}
              </a-button>
              <a-tooltip placement="left" title="备选和选择字段不能同时使用" v-if="groupByVisible || fieldChooseVisible">
                <a-button type="primary" :size="toolbarBtnSize" v-if="groupByVisible" @click="showAlternativeModal" :ghost="!!extraQueryData.group_by" :icon="extraQueryData.group_by ? 'check-square' : ''">备选</a-button>
                <a-button type="primary" :size="toolbarBtnSize" v-if="fieldChooseVisible" @click="showFieldChooseModal" :ghost="!!extraQueryData.required_return_field_names" :icon="extraQueryData.required_return_field_names ? 'check-square' : ''">选择字段</a-button>
              </a-tooltip>
              <a-button
                v-if="showDeleteButton"
                type="danger"
                @click="handleRemove"
              >
                <i
                  class="fa fa-trash"
                  aria-hidden="true"
                ></i>
                Delete
              </a-button>
            </div>
          </a-col>
        </a-row>
      </div>

      <vxe-grid
        v-if="gridVisible"
        :empty-text="isRunQuery ? '暂无数据' : '暂未查询'"
        :ref="randomRef"
        :max-height="maxTableHeight"
        :scroll-x="{gt: 15}"
        class="mini-scrollbar"
        :class="{'row--selectable': showHlightCurrentRow, 'more-small-table': tableSize === 'mini'}"
        show-overflow
        show-header-overflow
        :data="tableData"
        :loading="dataLoading"
        :columns="columns"
        :size="tableSize"
        :seq-config="{startIndex: (pagination.currentPage - 1) * pagination.pageSize}"
        :pager-config="showPagination ? pagination:  false"
        :highlight-current-row="showHlightCurrentRow"
        :sort-config="{trigger: 'cell', defaultSort: defaultSort, multiple: true}"
        auto-resize
        @page-change="handleTableChange"
        @current-change="currentChangeEvent"
        @checkbox-all="selectChangeEvent"
        @checkbox-change="selectChangeEvent"
        @sort-change="sortChangeEvent"
      >
        <template
          v-for="table in pageSettingData.table"
          :slot="table.field_name"
          slot-scope="{ row }"
        >
          <a
            :key="table.field_name"
            :title="valueMap(row[table.field_name], table.field_name)"
            v-if="isIncludetableCellDataLinkField(table.field_name)"
            @click.stop="handleCustomtableCellDataLink(row, table.field_name)"
          >{{ valueMap(row[table.field_name], table.field_name) }}</a>
          <span
            v-else
            :key="table.field_name"
            :title="valueMap(row[table.field_name], table.field_name)"
          >
            {{ valueMap(row[table.field_name], table.field_name) }}
          </span>
        </template>
        <template
          v-for="column in options.extraColumns || []"
          :slot="column.field"
          slot-scope="{ row, rowIndex }"
        >
          <slot :name="column.field" :row="rawTableData[rowIndex]"></slot>
        </template>
        <template v-slot:operation="{ row, rowIndex }">
          <a-button-group>
            <template v-for="buttionItem in tableOperationButtonList">
              <template v-if="!buttionItem.show || buttionItem.show(rawTableData[rowIndex])">
                <a-popconfirm
                  :key="buttionItem.name"
                  title="confirm deletion？"
                  cancel-text="No"
                  ok-text="Yes"
                  @confirm="handleTableDefinitionButtonClick(rawTableData[rowIndex], buttionItem.name)"
                  v-if="buttionItem.label == 'Delete'"
                >
                  <a-button
                    :key="buttionItem.name"
                    :type="buttionItem.type"
                    :size="row[buttionItem.name] && row[buttionItem.name].size || 'small'"
                    :disabled="row[buttionItem.name] && row[buttionItem.name].disabled"
                    :loading="row[buttionItem.name] && row[buttionItem.name].loading"
                    :ghost="row[buttionItem.name] && row[buttionItem.name].ghost"
                  >{{ buttionItem.label }}</a-button>
                </a-popconfirm>
                <a-button
                  :key="buttionItem.name"
                  :type="buttionItem.type"
                  @click.native="handleTableDefinitionButtonClick(rawTableData[rowIndex], buttionItem.name)"
                  :size="row[buttionItem.name] && row[buttionItem.name].size || 'small'"
                  :disabled="row[buttionItem.name] && row[buttionItem.name].disabled"
                  :loading="row[buttionItem.name] && row[buttionItem.name].loading"
                  :ghost="row[buttionItem.name] && row[buttionItem.name].ghost"
                  v-else
                >{{ buttionItem.label }}</a-button>
              </template>
            </template>
          </a-button-group>
        </template>
      </vxe-grid>
      <!-- 数据可视化 -->
      <div class="chart-box mg-t-10" v-if="advanced.enabled_graph">
        <a-checkbox v-model="graphVisible">启用图形功能</a-checkbox>
        <a-tabs v-model="activeChartTab" v-if="graphVisible">
          <a-tab-pane :tab="chart.label" :key="chart.key" v-for="chart in charts">
            <a-row class="mg-t-10 mg-b-10" type="flex" justify="space-between">
              <a-col :span="8">
                <a-form-model :ref="chart.key + 'ChartForm'" labelAlign="left" :rules="chartRules" :colon="false" :labelCol="{ span: 4 }" :wrapperCol="{ span: 12 }" :model="activeChart.options">
                  <template v-if="activeChartTab === 'pie'">
                    <a-form-model-item label="图例" required prop="category">
                      <a-select v-model="activeChart.options.category" :options="dimensionFieldsOptions" allowClear></a-select>
                    </a-form-model-item>
                    <a-form-model-item label="值" required prop="y"> 
                      <a-select v-model="activeChart.options.y" :options="numberChoosedFieldsOptions" allowClear></a-select>
                    </a-form-model-item>
                  </template>
                  <template v-else>
                    <a-form-model-item label="X轴" required prop="x">
                      <a-select v-model="activeChart.options.x" :options="dimensionFieldsOptions" allowClear></a-select>
                    </a-form-model-item>
                    <a-form-model-item label="Y轴" required prop="y">
                      <a-select v-model="activeChart.options.y" :options="numberChoosedFieldsOptions" allowClear></a-select>
                    </a-form-model-item>
                    <a-form-model-item label="图例" prop="category">
                      <a-select v-model="activeChart.options.category" :options="dimensionFieldsOptions" allowClear></a-select>
                    </a-form-model-item>
                  </template>
                </a-form-model>
              </a-col>
              <a-button class="load-chart-btn" type="primary" :loading="chartLoading" @click="renderChart">加载图形</a-button>
            </a-row>
            <a-card class="chart-area" :class="{'null-box': !chartDataMap[activeChartTab].length}" bordered size="small">
              <G2Charts :ref="chart.key" :type="chart.key" :data="chartDataMap[activeChartTab]" :options="chart.options"></G2Charts>
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
  </div>
</template>

<script>
/* eslint-disable */
import Vue from 'vue'
import moment from 'moment'
import '@/core/vxe-table'
import cloneDeep from 'lodash.clonedeep'
import { QueryFormType, selectFormType, textFormType, cascaderType } from '@/assets/d1/map'
import { api as d1Api } from '@/api/d1'

import ToggleList from '@/components/common/ToggleList'
import FakeCascader from '@/components/common/FakeCascader'
import VirtualTreeSelect from '@/components/common/VirtualTreeSelect'
import FieldChooseModal from '@/components/d1/FieldChooseModal'
import AlternativeFieldModal from '@/components/d1/AlternativeFieldModal'

// import G2Charts from '@/components/common/G2Charts'
import { TreeSelect } from 'ant-design-vue'
const SHOW_PARENT = TreeSelect.SHOW_PARENT
export default {
  components: { FakeCascader, ToggleList, FieldChooseModal, AlternativeFieldModal, VirtualTreeSelect },
  props: {
    apiType: {
      type: String,
      default: 'deployment'
    },
    options: {
      type: Object,
      required: true,
      default: () => {}
    },
    errorMsg: {
      type: String,
      required: false,
      default: () => ''
    },
    hideFullScreenPreview: {
      type: String,
      required: false,
      default: () => ''
    }
  },
  data() {
    return {
      SHOW_PARENT,
      isRunQuery: false,
      joinStr: '>',
      chartLoading: false,
      chartDataMap: {
        column: [],
        line: [],
        pie: []
      },
      activeChartTab: 'column',
      charts: [
        {
          label: '柱状图',
          key: 'column',
          options: {}
        },
        {
          label: '折线图',
          key: 'line',
          options: {}
        },
        {
          label: '饼图',
          key: 'pie',
          options: {}
        }
      ],
      isFormCollapse: true,
      exportTimeoutId: null,
      infoVisible: false,
      loadingDownloadMap: {
        'excel': false,
        'csv': false,
        'csvAndCompress': false
      },
      groupByFields: [],
      choosedFields: [],
      extraQueryData: {
      },
      textFormType,
      toolbarBtnSize: undefined,
      graphVisible: false,
      groupByVisible: false,
      fieldChooseVisible: false,
      alternativeFieldModalVisible: false,
      fieldChooseModalVisible: false,
      searchValueMap: {},
      gridVisible: false,
      sortArr: [],
      queryVisible: true,
      maxTableHeight: '400px',
      randomRef: Math.random().toString(),
      visibleForm: [],
      columns: [],
      scroll: {},
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
        align: 'right',
        // pageSizes: [10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 500, 1000],
        pageSizes: [10, 20, 50, 100],
        layouts: ['Total', 'PrevPage', 'JumpNumber', 'NextPage', 'FullJump', 'Sizes'],
        border: true
      },
      dataFacetKey: '',
      d1ClientBaseUrl: '',
      d1CoreBaseUrl: '',
      queryUrl: '/df/content/query', // 查询url
      exportUrl: '/df/async_export_jobs/excel', // 导出url
      exportCsvUrl: '/df/async_export_jobs/csv',
      exportCsvAndCompressUrl: '/df/async_export_jobs/csv_and_compress',
      exportResultUrl: '/df/async_export_jobs',
      downloadUrl: d1Api.download,
      deleteUrl: '', // 删除url
      uploadUrl: '', // 上传url
      groupByFieldsUrl: '/df/conf/table/group_by_fields',
      chooseFieldsUrl: '/df/conf/table',
      formTableUrl: '/df/conf',
      formType: QueryFormType,
      tableSize: 'mini',
      formSize: 'small',
      showUploadButton: false, // 显示上传按钮
      showExportButton: false, // 显示导出按钮
      showImportButton: false, // 显示import按钮
      showDeleteButton: false, // 显示删除按钮
      showDerrorButton: false, // 显示错误信息按钮
      errorVisible: false, // 显示错误信息
      showForm: true, // 显示form
      showTableSelection: false, // 显示表格checkbox
      showTableOperationButton: false, // 显示表格操作列按钮
      showToolbarButtonList: false, // 显示工具栏按钮
      enabledDefaultQuery: true, // 加载完form后立即执行查询
      tableOperationButtonList: [
        // {
        //   label: '按鈕', // 按钮显示的名称
        //   type: 'success', // 按钮的类型,默认空,支持  primary，success，info，warning，danger
        //   name: 'test', // 用户点击时返回的组件
        //   disabled: false,
        //   loading: false
        // }
      ],
      toolbarButtonList: [
        // {
        //   label: '', // 按钮显示的名称
        //   type: '', // 按钮的类型,默认空,支持  primary，success，info，warning，danger
        //   name: '', // 用户点击时返回的组件
        //   elColWidth: 2 // 按钮的占位
        // }
      ],
      loadFormTableOnCreate: true, // 是否初始化完成以后立即加载form
      sliceLength: 12, // collapse时显示的数量
      showDownloadButton: false,
      toolbarPlaceholder: [], // 界面显示描述
      asyncExport: false, // 是否异步导出
      dataLoading: false,
      confLoading: false,
      loadingImport: false, // import按钮的loading
      confirmToRemoveDialogVisible: false,
      editDialogVisible: false,
      totalRecord: 0,
      tableData: [],
      rawTableData: [],
      pageSettingData: {
        form: [],
        table: [],
        advanced: {}
      },
      specialParam: {},
      formData: {},
      pageSettingDataFormClone: {},
      showHlightCurrentRow: false,
      showSelectColumn: false,
      showPagination: true,
      confirmToAnalyzeDialogVisible: false,
      tableDataFormatter: false,
      tableDataLoading: false,
      loadingPrompt: '',
      tableCellDataLink: {}, // 表格数据定义的点击事件,详细格式参照tableCellDataLinksDemo
      tableCellDataLinksDemo: {
        needCustomProcess: false, // 如果设置为ture,请监听handleTableValueClick函数
        fieldProperty: [
          {
            field_name: '', // 需要增加a标签的字段名
            field_value: [''], // a标签显示的限制值
            dialogDisplaysValueFromFields: '' // 对话框的说明来源于字段
          }
        ]
      },
      basicCascadeInform: {},
      fieldNameCascade: {},
      specialFormTypeChoiceNullOrEmpty: {
        optionalValue: undefined,
        optionalLabel: 'None'
      },
      tableDataConvvert: {},
      mulitSelect: false, // 默认不显示表格展示的列数
      seloptions: [],
      multipSel: [],
      serialNumber: false // 表格是否添加序号列
    }
  },
  computed: {
    hasExportLoading () {
      return Object.values(this.loadingDownloadMap).some(x => x)
    },
    multipleType () {
      return [this.formType.DROP_DOWN_LIST_MULTIPLE, this.formType.ASSOCIATING_MULTIPLE, this.formType.DATE_RANGE, this.formType.TIME_RANGE, this.formType.TIMESTAMP_RANGE, this.formType.NUMBER_RANGE, this.formType.CASCADER_MULTIPLE]
    },
    defaultSort () {
      return this.sortArr.map(x => {
        const [field, order] = x.split(',')
        return {
          field,
          order: order.toLowerCase()
        }
      })
    },
    advanced () {
      return this.pageSettingData.advanced || {}
    },
    serialNumberWidth() {
      if (!this.serialNumber) return 0
      return this.options.serialNumberWidth || 50
    },
    selection() {
      return this.selectedRowKeys.map(index => this.tableData[index])
    },
    baseUrl() {
      const base = this.d1ClientBaseUrl ? this.d1ClientBaseUrl : this.d1CoreBaseUrl
      return `${base}/${this.apiType}`
    },
    detailParam() {
      const dateFormat = 'YYYY-MM-DD'
      const timeFormat = 'HH:mm:ss'
      const datetimeFormat = 'YYYY-MM-DD HH:mm:ss'
      const formatMap = {
        [this.formType.DATE_SINGLE]: dateFormat,
        [this.formType.DATE_RANGE]: dateFormat,
        [this.formType.TIME_SINGLE]: timeFormat,
        [this.formType.TIME_RANGE]: timeFormat,
        [this.formType.TIMESTAMP_RANGE]: datetimeFormat,
        [this.formType.TIMESTAMP_SINGLE]: datetimeFormat
      }
      let params = {}
      // const setCascaderField = (value, fieldChain) => {
      //   const chainValues = value.split(this.joinStr)
      //   fieldChain.forEach((fieldName, index) => {
      //     if (index < chainValues.length) {
      //       this.setParams(params, fieldName, chainValues[index])
      //     }
      //   })
      // }
      for (let i = 0; i < this.pageSettingData.form.length; i++) {
        const item = this.pageSettingData.form[i] // 每一个参数
        // 几个Range的判断
        if (item.field_hierarchy_structure && item.form_element_type === this.formType.CASCADER) {
          // const fieldChain = this.getFieldNameChain(item.field_hierarchy_structure)
          // fieldChain.unshift(item.field_name)
          // if (fieldChain && item.field_default_values) {
          //   setCascaderField(item.field_default_values, fieldChain)
          // }
          if (item.field_default_values) {
            this.setParams(params, item.dictionary_category_name, item.field_default_values)
          }
        } else if (item.field_hierarchy_structure && item.form_element_type === this.formType.CASCADER_MULTIPLE) {
          if (item.field_default_values) {
            item.field_default_values.forEach(value => {
              this.setParams(params, item.dictionary_category_name, value)
            })
          }
        } else if ([this.formType.DATE_RANGE, this.formType.TIMESTAMP_RANGE].includes(item.form_element_type)) {
          if (item.field_default_values[0] && item.field_default_values[1]) {
            const value = [item.field_default_values[0].format(formatMap[item.form_element_type]), item.field_default_values[1].format(formatMap[item.form_element_type])]
            this.setParams(params, item.field_name, value)
          }
        } else if (item.form_element_type === this.formType.TIME_RANGE) {
          if (item.field_value__start__ && item.field_value__end__) {
            this.setParams(params, item.field_name, [item.field_value__start__.format(formatMap[item.form_element_type]), item.field_value__end__.format(formatMap[item.form_element_type])])
          }
        } else if (item.form_element_type === this.formType.NUMBER_RANGE) {
          if (item.field_value__start__ && item.field_value__end__) {
            this.setParams(params, item.field_name, [item.field_value__start__, item.field_value__end__])
          }
        }
        // 单个时间插件
        else if ([this.formType.TIME_SINGLE, this.formType.DATE_SINGLE, this.formType.TIMESTAMP_SINGLE].includes(item.form_element_type)) {
          // 判断form表字段中的类型
          // 处理report date
          if (item.field_default_values) {
            const value = item.field_default_values.format(formatMap[item.form_element_type]) // 返回日期类型格式的值 输入的值 2019-06-14
            this.setParams(params, item.field_name, value)
          }
        } else if (typeof item.field_default_values === 'object') {
          // 输入类型为object
          const specialparamValues = []
          if (item.field_default_values) {
            this.specialParam[item.field_name] = specialparamValues // 放入数组
            for (let j = 0; j < item.field_default_values.length; j++) {
              const value = item.field_default_values[j] // 输入的值

              if (value != null) {
                this.setParams(params, item.field_name, value)
              }
            }
          }
        } else if (typeof item.field_default_values === 'string' || typeof item.field_default_values === 'number') {
          if (item.field_default_values !== '') {
            this.setParams(params, item.field_name, item.field_default_values)
          }
        }
      }

      params['sort'] = this.sortArr
      if (this.options.preFilters) {
        params = {
          ...this.options.preFilters,
          ...params
        }
      }
      return params
    },
    exportDialogTip() {
      let tip = ''
      if (this.isRunQuery) {
        tip = `确定导出${this.totalRecord}条记录？`
      } else {
        tip = '确定导出？'
      }
      return tip
    },
    definitionTableButtonWidth() {
      if (this.options.operationWidth) {
        return this.options.operationWidth
      }
      if (!this.tableOperationButtonList || !this.tableOperationButtonList.length) return 0
      return this.tableOperationButtonList.reduce((acc, item) => {
        return acc + (item.width || 150)
      }, 0)
    },
    defultSelectRow() {
      if (this.tableData == null || this.tableData.length === 0) {
        return null
      }
      return this.tableData[0]
    },
    selectedFieldNames () {
      return [this.activeChart.options.category, this.activeChart.options.x, this.activeChart.options.y].filter(x => x)
    },
    dimensionFieldsOptions () {
      return this.choosedFields.filter(x => x.role === 'DIMENSION').map(x => ({
        label: x.field_label,
        value: x.field_name,
        disabled: this.selectedFieldNames.includes(x.field_name)
      }))
    },
    numberChoosedFieldsOptions () {
      return this.choosedFields.filter(x => x.role === 'KPI').map(x => ({
        label: x.field_label,
        value: x.field_name,
        disabled: this.selectedFieldNames.includes(x.field_name)
      }))
    },
    activeChart () {
      return this.charts.find(x => x.key === this.activeChartTab)
    },
    chartRules () {
      if (this.activeChartTab === 'pie') {
        return {
          y: [{
            required: true,
            message: 'Y轴是必选项'
          }],
          category: [{
            required: true,
            message: '图例是必选项'
          }]
        }
      } else {
        return {
          x: [{
            required: true,
            message: 'X轴是必选项'
          }],
          y: [{
            required: true,
            message: 'Y轴是必选项'
          }]
        }
      }
    }
  },
  watch: {
    options: {
      handler(val) {
        this.seloptions = val.seloptions
        this.multipSel = val.multipSel
      },
      deep: true
    }
  },
  beforeDestroy () {
    if (this.exportTimeoutId) {
      clearTimeout(this.exportTimeoutId)
    }
  },
  async created() {
    this.initOptions(this.options)
    if (this.loadFormTableOnCreate) {
      await this.loadFormTableSetting()
    }
    const exportTask = this.$ls.get('export_task')
    if (exportTask && exportTask.dfk === this.dataFacetKey) {
      this.loadExportSummary(exportTask.taskId, exportTask.type)
    }
    if (this.graphVisible) {
      this.loadChooseFields()
    }
  },
  methods: {
    exportDisabled (type) {
      return !this.loadingDownloadMap[type] && this.hasExportLoading || this.isRunQuery && !this.totalRecord
    },
    loadChartComponent () {
      Vue.component('G2Charts', () => import(/* webpackChunkName: "G2Charts" */'@/components/common/G2Charts'))
    },
    async getChartData (kpiField, dimensionFields) {
      const result = await this.$api.prefix(this.d1CoreBaseUrl).getGraphData(this.dataFacetKey, kpiField, dimensionFields, this.detailParam)
      return result || []
    },
    async renderChart () {
      let isValid
      this.$refs[`${this.activeChartTab}ChartForm`][0].validate(valid => {
        isValid = valid
      })
      if (!isValid) return
      const dimensionFields = this.activeChartTab === 'pie' ? [this.activeChart.options.category] : [this.activeChart.options.x, this.activeChart.options.category]
      const type = this.activeChartTab
      this.chartLoading = true
      const result = await this.getChartData(this.activeChart.options.y, dimensionFields).finally(() => {
        this.chartLoading = false
      })
      if (!result.length) {
        return this.$message.info('未加载到图表数据')
      }
      this.chartDataMap[type] = result
      this.$nextTick(() => {
        this.$refs[this.activeChartTab][0].render()
      })
    },
    refreshTable () {
      this.$nextTick(() => {
        this.$refs[this.randomRef].refreshColumn(true)
      })
    },
    handleToggleChange (list, isCollapse) {
      this.visibleForm = list
      // this.isFormCollapse = isCollapse
    },
    cascaderFilter (inputValue, path) {
      return path.some(option => option.label.toLowerCase().indexOf(inputValue.toLowerCase()) > -1);
    },
    getFieldNameChain (obj, chain = []) {
      if (obj.field_name) {
        chain.push(obj.field_name)
      }
      if (obj.child) {
        this.getFieldNameChain(obj.child, chain)
      }
      return chain
    },
    generateTreeData (items = [], parent) {
      const data = items.slice()
      data.forEach(item => {
        item.option_label = item.description
        item.option_value = [parent?.option_value, item.name].filter(x => x).join(this.joinStr)
        // item.raw_value = item.name
        if (item.children) this.generateTreeData(item.children, item)
      })
      return data
    },
    nodeMethod (item, parent) {
      item.value = [parent?.value, item.name].filter(x => x).join(this.joinStr)
    },
    refreshSort () {
      setTimeout(() => {
        this.$refs[this.randomRef].sort(this.defaultSort)
      }, 100)
    },
    handleSetGroupBy (groupByData) {
      this.extraQueryData.group_by = groupByData
      this.extraQueryData.required_return_field_names = undefined
      this.columns = this.getColumns(this.pageSettingData.table)
      this.refreshSort()
      this.pagination.currentPage = 1
      this.runQuery()
    },
    handleSelectVisibleFields (fieldNames) {
      this.extraQueryData.required_return_field_names = fieldNames
      if (fieldNames.length === 0) {
        this.extraQueryData.required_return_field_names = undefined
      }
      this.extraQueryData.group_by = undefined
      this.columns = this.getColumns(this.pageSettingData.table)
      this.refreshSort()
      this.runQuery()
    },
    showAlternativeModal () {
      this.alternativeFieldModalVisible = true
      if (!this.choosedFields.length) {
        this.loadChooseFields()
      }
      if (!this.groupByFields.length) {
        this.loadGroupByFields()
      }
    },
    showFieldChooseModal () {
      this.fieldChooseModalVisible = true
      if (!this.choosedFields.length) {
        this.loadChooseFields()
      }
    },
    handleSelectSearch (value, item) {
      this.$set(item, 'isSearch', true)
      this.$set(this.searchValueMap, item.field_name, value || undefined)
    },
    getSortArr () {
      return this.pageSettingData.table
        .filter(x => x.sort_direction)
        .map(x => `${x.field_name},${x.sort_direction}`)
    },
    sortChangeEvent ({ sortList }) {
      if (this.dataLoading) return
      this.sortArr = sortList.map(x => `${x.property},${x.order.toUpperCase()}`)
      this.runQuery()
    },
    valueMap(val, columnName) {
      if (this.options.valueMap) {
        const map = this.options.valueMap[columnName]
        if (map) {
          if (typeof map === 'function') {
            return map(val)
          } else {
            return map[val]
          }
        }
      }
      return val
    },
    paramsChange() {
      if (this.options.watchForm) {
        const params = { ...this.detailParam }
        delete params.sort
        this.$emit('paramsChange', params)
      }
    },
    fullUrl(api) {
      if (!api) return
      if (api.startsWith('http')) {
        return api
      } else {
        return this.baseUrl + api
      }
    },
    getColumns(rawColumns) {
      const selectColumn = {
        type: 'checkbox',
        width: 60,
        align: 'center'
      }
      const serialColumn = {
        title: 'No.',
        type: 'seq',
        width: this.serialNumberWidth,
        fixed: 'left'
      }
      const operationColumn = {
        title: 'Operation',
        field: 'operation',
        slots: { default: 'operation' },
        width: this.definitionTableButtonWidth,
        fixed: 'right'
      }
      rawColumns = rawColumns.sort((a, b) => a.list_element_sequence - b.list_element_sequence)
      if (this.extraQueryData.group_by) {
        if (this.extraQueryData.group_by.field_processing_type === 'RETURN_ONLY_SELECTED_FIELDS' && this.extraQueryData.group_by.selected_group_by_field_names) {
          const kpiColumns = rawColumns.filter(x => x.role === 'KPI')
          rawColumns = rawColumns.filter(x => this.extraQueryData.group_by.selected_group_by_field_names.includes(x.field_name)).sort((a, b) => this.extraQueryData.group_by.selected_group_by_field_names.findIndex(y => y === a.field_name) - this.extraQueryData.group_by.selected_group_by_field_names.findIndex(y => y === b.field_name))
          rawColumns.push(...kpiColumns)
        } else if (this.extraQueryData.group_by.field_processing_type === 'ADVANCED' && this.extraQueryData.group_by.result_fields) {
          rawColumns = rawColumns.filter(x => this.extraQueryData.group_by.result_fields.filter(x => !x.new_field_label).map(x => x.field_name).includes(x.field_name))
          rawColumns.push(...this.extraQueryData.group_by.result_fields.filter(x => x.new_field_label).map(x => ({
            field_label: x.new_field_label,
            field_name: x.new_field_label,
            list_element_width: 150,
            is_new_label: true
          })))
        }
      }
      let columns = rawColumns.map((x, i) => ({
        title: x.field_label,
        field: x.field_name,
        // width: x.list_element_width || undefined,
        minWidth: x.list_element_width || 150,
        sortable: this.extraQueryData.group_by ? false : !!x.sort_direction,
        params: {
          description: x.field_description
        },
        slots: x.is_new_label ? null : { 
          default: x.field_name,
          header: ({ column }) => {
            let tooltip = ''
            if (column.params.description) {
              tooltip = <a-tooltip class="header-tooltip mg-r-5"><template slot="title">{column.params.description}</template><a-icon type="info-circle" /></a-tooltip>
            }
            return [
               <span>{tooltip}{column.title}</span>
            ]
          }
        }
      }))
      if (this.options.hideColumns) {
        columns = columns.filter(x => !this.options.hideColumns.includes(x.field))
      }
      if (this.extraQueryData.required_return_field_names) {
        columns = columns.filter(x => this.extraQueryData.required_return_field_names.includes(x.field))
      }
      if (Array.isArray(this.options.extraColumns)) {
        columns.push(...this.options.extraColumns.map(x => ({
          slots: {
            default: x.field
          },
          ...x
        })))
      }
      if (this.serialNumber) {
        columns.unshift(serialColumn)
      }
      if (this.showTableSelection) {
        columns.unshift(selectColumn)
      }
      if (this.showTableOperationButton) {
        columns.push(operationColumn)
      }
      // 处理列浮动
      columns.forEach((column, index) => {
        if (this.advanced.enabled_freeze_left_columns && index < this.advanced.inclusive_left_columns) {
          column.fixed = 'left'
        }
        if (this.advanced.enabled_freeze_right_columns && columns.length - 1 - index < this.advanced.inclusive_right_columns) {
          column.fixed = 'right'
        }
      })
      return columns
    },
    async loadChooseFields () {
      const result = await this.$api.prefix(this.d1CoreBaseUrl).getChooseFields(this.dataFacetKey)
      this.choosedFields = result || []
    },
    async loadGroupByFields () {
      const result = await this.$api.prefix(this.d1CoreBaseUrl).getGroupByFields(this.dataFacetKey)
      this.groupByFields = result || []
    },
    async loadFormTableSetting() {
      this.confLoading = true
      const result = await this.$api.prefix(this.d1CoreBaseUrl).getFormTableSettings(this.dataFacetKey).finally(
        () => (this.confLoading = false)
      )
      if (!result) return
      const form = (result.form || []).sort((a, b) => a.form_element_sequence - b.form_element_sequence)
      // 删除root层级
      form.forEach(x => {
        if (x.field_optional_values) {
          x.field_optional_values = this.generateTreeData(x.field_optional_values.children)
        } else if (x.field_hierarchy_content) {
          x.field_optional_values = this.generateTreeData(x.field_hierarchy_content.children)
        }
      })
      this.pageSettingData = result
      this.initOptions(this.options)
      this.pageSettingData.table = this.pageSettingData.table || []
      this.sortArr = this.getSortArr()
      this.columns = this.getColumns(this.pageSettingData.table)
      // null or empty初始化

      if (this.options.handleForm) {
        this.pageSettingData.form = this.options.handleForm(form)
      }

      this.pageSettingData.form = this.handleDefaultVal(form)

      this.pageSettingData.form = this.handleNullOrEmpty(form)

      // 级联初始化
      // this.pageSettingData.form = this.handleCascade(form)

      this.pageSettingDataFormClone = cloneDeep(this.pageSettingData.form)
      // TOFIXED
      this.$emit('completeLoadForm', this.pageSettingData)
      this.gridVisible = true
      if (this.enabledDefaultQuery) {
        this.runQuery()
      }
    },
    currentChangeEvent({ row }) {
      if (!this.showHlightCurrentRow) return
      this.selectRow(row)
    },
    selectRow(record) {
      if (record) {
        this.$refs[this.randomRef].setCurrentRow(record)
      } else {
        this.$refs[this.randomRef].clearCurrentRow()
      }
      this.$emit('handleTableRowClick', record)
    },
    handParams(params) {
      Object.keys(params).forEach(key => {
        if (Array.isArray(params[key])) {
          params[key] = params[key].join(',')
        }
      })
      return params
    },
    async runQuery(queryTotal = true) {
      this.dataLoading = true
      this.tableData = []
      this.rawTableData = []
      const params = { ...this.detailParam }
      if (this.showPagination) {
        params.page = this.pagination.currentPage - 1
        params.size = this.pagination.pageSize
      }
      let reqBody = null
      if (Object.keys(this.extraQueryData).length) {
        reqBody = this.extraQueryData
      }
      const result = await this.$api.prefix(this.d1ClientBaseUrl || this.d1CoreBaseUrl).queryTable(this.dataFacetKey, params, reqBody).finally(() => {
        this.dataLoading = false
      })
      this.isRunQuery = true
      // if (!result) {
      //   return
      // }
      const tableData = result.content
      this.rawTableData = cloneDeep(tableData)
      if (queryTotal && params.page === 0) {
        this.totalRecord = result.total_elements
        this.pagination.total = this.totalRecord
      }
      if (tableData != null && tableData.length) {
        // 有数据
        for (let i = 0; i < tableData.length; i++) {
          const item = tableData[i]

          // 如果值是true  or  false 会无法显示,需要处理一下
          const itemKeys = Object.keys(item) // 转成对象
          for (let j = 0; j < itemKeys.length; j++) {
            item[itemKeys[j]] = item[itemKeys[j]] == null ? '' : item[itemKeys[j]] + ''
          }
        }
      }
      if (this.tableDataFormatter && tableData && tableData.length > 0) {
        this.$emit('handleTableDataFormatter', tableData, this.componentName)
      } else {
        this.tableData = tableData
      }

      if (this.showHlightCurrentRow) {
        this.selectRow(this.defultSelectRow)
      }
      this.$nextTick(() => {
        this.dataLoading = false
      })
    },
    async exportTableData(type) {
      const urlMap = {
        'excel': 'exportExcelTableData',
        'csv': 'exportCsvTableData',
        'csvAndCompress': 'exportCsvAndCompressTableData'
      }
      this.loadingDownloadMap[type] = true
      const result = await this.$api.prefix(this.d1ClientBaseUrl || this.d1CoreBaseUrl)[urlMap[type]](this.dataFacetKey, this.detailParam, this.extraQueryData).finally(
        err => {
          this.loadingDownloadMap[type] = false
        }
      )
      // 判断是否是异步导出
      if (this.asyncExport) {
        this.loadExportSummary(result.uid, type)
      }
    },
    async loadExportSummary(taskId, type, requestCount = 0) {
      // if (requestCount === 5) return
      this.loadingDownloadMap[type] = true
      this.$ls.set('export_task', {
        taskId: taskId,
        type: type,
        dfk: this.dataFacetKey
      })
      const result = await this.$api.prefix(this.d1ClientBaseUrl || this.d1CoreBaseUrl).getExportResult(taskId)
      // requestCount++
      if (result.status === 'FAILED') {
        this.loadingDownloadMap[type] = false
        const textMap = {
          'excel': 'Excel',
          'csv': 'CSV',
          'csvAndCompress': 'CSV并压缩'
        }
        this.$message.info(`导出 ${textMap[type]} 失败`)
        this.$ls.remove('export_task')
      } else if (result && result.shared_file_id) {
        window.location.href = `${this.downloadUrl}?file_id=${result.shared_file_id}`
        this.loadingDownloadMap[type] = false
        this.$ls.remove('export_task')
      } else {
        this.exportTimeoutId = setTimeout(() => {
          this.loadExportSummary(taskId, type, requestCount)
        }, 5000)
      }
    },
    setParams(params, key, value) {
      if (params[key]) {
        if (params[key] instanceof Array) {
          params[key].push(value)
        } else {
          const values = []
          values.push(params[key])
          values.push(value)
          params[key] = values
        }
      } else {
        params[key] = value
      }
      return params
    },
    handleQuery() {
      this.pagination.currentPage = 1
      this.runQuery()
    },
    handleResetForm() {
      const pageSettingDataFormCloneInit = cloneDeep(this.pageSettingDataFormClone)
      this.pageSettingData.form = pageSettingDataFormCloneInit
      this.pagination.currentPage = 1
      this.runQuery()
    },
    handleError(err, file, fileList) {
      this.dataLoading = false
    },
    handleBeforeUpload(file) {
      if (file.size > 50 * 1024 * 1024) {
        this.$message.warning('file exceeds size limit')
        return false
      }
      this.dataLoading = true
    },
    handleTableChange({ currentPage, pageSize }) {
      if (pageSize !== this.pagination.pageSize) {
        this.pagination.currentPage = 1
      } else {
        this.pagination.currentPage = currentPage
      }
      this.pagination.pageSize = pageSize
      this.runQuery(false)
    },
    refresh() {
      this.runQuery()
    },
    handleRemove() {
      if (this.isSelectionValid()) {
        this.confirmToRemoveDialogVisible = true
      }
    },
    handleError() {},
    confirmToRemove() {
      const payload = {}
      const ids = []
      this.dataLoading = true

      for (var i = 0; i < this.selection.length; i++) {
        ids.push(this.selection[i].id)
      }

      payload.ids = ids

      this.$http
        .delete(this.fullUrl(this.deleteUrl), payload)
        .then(resp => {
          const data = resp.data
          this.dataLoading = false
          this.confirmToRemoveDialogVisible = false
          this.$message.success('Successful operation')
          this.pagination.currentPage = 1
          this.runQuery()
        })
        .catch(error => {
          this.dataLoading = false
          this.confirmToRemoveDialogVisible = false
        })
    },
    showExportModal(type = 'excel') {
      this.$confirm({
        title: '导出数据',
        content: this.exportDialogTip,
        onOk: () => {
          this.exportTableData(type)
        },
        onCancel() {}
      })
    },
    selectChangeEvent({ checked, records }) {
      this.$emit('selectRows', records)
    },
    handleSelect(selection, row) {},
    handleSelectAll(selection) {},
    isSelectionValid() {
      if (this.selection.length === 0) {
        this.$message.warning('Select record please')
        return false
      } else {
        return true
      }
    },
    initOptions(options) {
      // 初始化组件
      if (this.advanced.enabled_vertical_scrolling) {
        this.maxTableHeight = `${this.advanced.vertical_scrolling_height_threshold}px`
      }
      this.graphVisible = options.graphVisible !== undefined ? options.graphVisible : this.advanced.enabled_graph
      this.graphVisible && this.loadChartComponent()
      this.toolbarBtnSize = options.toolbarBtnSize || this.toolbarBtnSize
      this.groupByVisible = options.groupByVisible !== undefined ? options.groupByVisible : this.advanced.enabled_group_by
      this.fieldChooseVisible = options.fieldChooseVisible !== undefined ? options.fieldChooseVisible : this.fieldChooseVisible
      this.dataFacetKey = options.dataFacetKey || this.dataFacetKey
      this.d1CoreBaseUrl = options.d1CoreBaseUrl || this.d1CoreBaseUrl
      this.d1ClientBaseUrl = options.d1ClientBaseUrl !== undefined ? options.d1ClientBaseUrl : this.d1ClientBaseUrl
      this.formTableUrl = options.formTableUrl || this.formTableUrl
      this.queryUrl = options.queryUrl !== undefined ? options.queryUrl : this.queryUrl // 表格的查询url
      this.importUrl = options.importUrl !== undefined ? options.importUrl : this.importUrl // 导入文件的url
      this.exportUrl = options.exportUrl || this.exportUrl // 导出文件的url
      this.uploadUrl = options.uploadUrl !== undefined ? options.uploadUrl : this.uploadUrl // 导出文件的url
      this.deleteUrl = options.deleteUrl !== undefined ? options.deleteUrl : this.deleteUrl // 删除功能
      this.exportResultUrl = options.exportResultUrl || this.exportResultUrl
      this.downloadUrl = options.downloadUrl || this.downloadUrl
      this.options.preFilters = this.options.preFilters || {}
      
      this.formSize = this.options.formSize || this.formSize
      this.tableOperationButtonList = options.tableOperationButtonList || this.tableOperationButtonList // 表格内按钮组
      this.tableSize = this.options.tableSize || this.tableSize
      if (this.tableOperationButtonList.length && this.tableSize === 'mini') {
        this.tableSize = 'small'
      }
      this.loadFormTableOnCreate =
        options.loadFormTableOnCreate !== undefined ? options.loadFormTableOnCreate : this.loadFormTableOnCreate
      this.tableDataConvvert = options.tableDataConvvert !== undefined ? options.tableDataConvvert : this.tableDataConvvert

      this.pageSettingData = options.pageSettingData !== undefined ? options.pageSettingData : this.pageSettingData
      this.showUploadButton = options.showUploadButton !== undefined ? options.showUploadButton : this.showUploadButton
      // 表格复选框
      this.showTableSelection =
        options.showTableSelection !== undefined ? options: this.advanced.enabled_column_checkbox
      this.showTableOperationButton = options.showTableOperationButton !== undefined ? options.showTableOperationButton : this.advanced.enabled_column_operation
      this.showExportButton = options.showExportButton !== undefined ? options.showExportButton : this.advanced.export_type && this.advanced.export_type !== 'NO'
      this.showImportButton = options.showImportButton !== undefined ? options.showImportButton : this.showImportButton
      this.showDerrorButton = options.showDerrorButton !== undefined ? options.showDerrorButton : this.showDerrorButton
      this.showPagination = options.showPagination != undefined ? options.showPagination : this.advanced.enabled_pagination
      if (this.showPagination) {
        this.pagination.pageSize = options.pageSize || this.advanced.default_page_size || this.pagination.pageSize
      }
      this.showSelectColumn = options.showSelectColumn !== undefined ? options.showSelectColumn : this.showSelectColumn
      this.showForm = options.showForm !== undefined ? options.showForm : this.advanced.query_type && this.advanced.query_type !== 'NO'
      this.showToolbarButtonList =
        options.showToolbarButtonList !== undefined ? options.showToolbarButtonList : this.showToolbarButtonList
      this.tableDataFormatter =
        options.tableDataFormatter !== undefined ? options.tableDataFormatter : this.tableDataFormatter
      this.toolbarButtonList = options.toolbarButtonList !== undefined ? options.toolbarButtonList : this.toolbarButtonList
      this.toolbarPlaceholder =
        options.toolbarPlaceholder !== undefined ? options.toolbarPlaceholder : this.toolbarPlaceholder
      this.loadingPrompt = options.loadingPrompt !== undefined ? options.loadingPrompt : this.loadingPrompt
      this.tableCellDataLink = options.tableCellDataLink !== undefined ? options.tableCellDataLink : this.tableCellDataLink
      // 定制服务
      this.asyncExport = options.asyncExport !== undefined ? options.asyncExport : this.advanced.export_type === 'ASYNCHRONOUS' // 是否异步导出
      this.enabledDefaultQuery =
        options.enabledDefaultQuery !== undefined
          ? options.enabledDefaultQuery
          : this.advanced.enabled_default_query
      this.isFormCollapse = options.enabledFormFolding !== undefined ? options.enabledFormFolding : this.advanced.enabled_form_folding
      this.infoVisible = this.options.infoVisible !== undefined ? this.options.infoVisible : this.advanced.enabled_df_name_description
      this.sliceLength = options.sliceLength || this.sliceLength
      // 拷贝值，给reset使用
      // 显示表格的当前行
      this.showHlightCurrentRow =
        options.showHlightCurrentRow !== undefined ? options.showHlightCurrentRow : this.showHlightCurrentRow
      this.mulitSelect = options.mulitSelect !== undefined ? options.mulitSelect : this.mulitSelect /// /默认不显示表格展示的列数
      this.serialNumber = options.serialNumber !== undefined ? options.serialNumber : this.advanced.enabled_column_no /// /默认不显示表格展示的列数
      this.seloptions = options.seloptions !== undefined ? options.seloptions : this.seloptions // 表格右上角多选下拉框
      this.multipSel = options.multipSel !== undefined ? options.multipSel : this.multipSel // 表格右上角选中数据
      // 表格左下角注释
      if (!this.loadFormTableOnCreate) {
        this.pageSettingDataFormClone = cloneDeep(this.pageSettingData.form)
      }
      if (this.hideFullScreenPreview == '1') {
        this.toolbarButtonList = {}
      }
    },
    handleAdd() {
      this.$emit('handleAdd')
    },
    handleEdit(row) {
      this.$emit('handleEdit', row)
    },
    handleTableDefinitionButtonClick(row, name, index) {
      // 直接返回调用者组件处理
      this.$emit('onTableOperationButtonClick', row, name)
    },
    handleGeneralButtonClick(name) {
      this.$emit('onToolbarButtonClick', name)
    },
    setTableData(tableData) {
      this.tableData = tableData
    },
    clearTableData() {
      this.tableData = [{}]
      this.totalRecord = 0
    },
    inArray(arr, str) {
      let i = arr.length
      while (i--) {
        if (arr[i] === str) {
          return true
        }
      }
      return false
    },
    setPageSettingData(pageSettingData) {
      this.pageSettingData = pageSettingData
      this.pageSettingDataFormClone = cloneDeep(this.pageSettingData.form)
    },
    setDataLoading(status) {
      this.dataLoading = status
    },
    showTrendChart(row) {
      this.$emit('showTrendChart', row)
    },
    handleDelete(row) {
      this.$emit('handleDelete', row)
    },
    setTableDataLoading(status) {
      this.tableDataLoading = status
    },
    isIncludetableCellDataLinkField(fieldKey) {
      const fieldList = this.tableCellDataLink
      if (fieldList && fieldList.length) {
        let i = fieldList.length
        while (i--) {
          if (fieldList[i].field_name === fieldKey) {
            return true
          }
        }
      }
      return false
    },
    handleCustomtableCellDataLink(row, fieldKey) {
      const fieldList = this.tableCellDataLink
      this.$emit('handleTableValueClick', row, fieldKey)
    },
    setDataFacetKey(dataFacetKey) {
      this.dataFacetKey = dataFacetKey
    },
    handleNullOrEmpty(form) {
      for (let i = 0; i < form.length; i++) {
        const formItem = form[i]
        if (
          selectFormType[formItem.form_element_type] && !cascaderType[formItem.form_element_type] && ![this.formType.DROP_DOWN_LIST_MULTIPLE, this.formType.ASSOCIATING_MULTIPLE].includes(formItem.form_element_type)
        ) {
          if (formItem.field_optional_values) {
            formItem.field_optional_values.splice(0, 0, {
              option_label: this.specialFormTypeChoiceNullOrEmpty.optionalLabel,
              option_value: this.specialFormTypeChoiceNullOrEmpty.optionalValue
            })
          } else {
            formItem.field_optional_values = []
            formItem.field_optional_values.push({
              option_label: this.specialFormTypeChoiceNullOrEmpty.optionalLabel,
              option_value: this.specialFormTypeChoiceNullOrEmpty.optionalValue
            })
          }
        }
      }
      return form
    },
    // 后台为json化 数组字符串
    handleDefaultVal(form) {
      for (let i = 0; i < form.length; i++) {
        const formItem = form[i]
        const fieldValArr = formItem.field_default_values?.map(x => x.value)
        if (fieldValArr) {
          formItem.default_field_optional_values = formItem.field_default_values
        }
        if (fieldValArr) {
          if (this.multipleType.includes(formItem.form_element_type)) {
            if (
              formItem.form_element_type == this.formType.TIME_RANGE ||
              formItem.form_element_type == this.formType.NUMBER_RANGE
            ) {
              let startValue = fieldValArr[0]
              let endValue = fieldValArr[1]
              if (formItem.form_element_type == this.formType.TIME_RANGE) {
                startValue = moment(`2020-12-10 ${startValue}`)
                endValue = moment(`2020-12-10 ${endValue}`)
              }
              if (fieldValArr) {
                if (fieldValArr[0]) {
                  // formItem.field_value__start__ = startValue
                  this.$set(formItem, 'field_value__start__', startValue)
                }
                if (fieldValArr[1]) {
                  // formItem.field_value__end__ = endValue
                  this.$set(formItem, 'field_value__end__', endValue)
                }
              }
            } else if (formItem.form_element_type == this.formType.DATE_RANGE || formItem.form_element_type == this.formType.TIMESTAMP_RANGE) {
                formItem.field_default_values = [moment(fieldValArr[0]), moment(fieldValArr[1])]
            } else {
              formItem.field_default_values = fieldValArr
            }
          } else if (formItem.form_element_type === this.formType.DATE_SINGLE || formItem.form_element_type === this.formType.TIMESTAMP_SINGLE || formItem.form_element_type === this.formType.TIME_SINGLE) {
            if (formItem.form_element_type === this.formType.TIME_SINGLE) {
              formItem.field_default_values = moment(`2020-12-10 ${fieldValArr[0]}`)
            } else {
              formItem.field_default_values = moment(fieldValArr[0])
            }
          } else {
            formItem.field_default_values = fieldValArr[0]
          }
        } else {
          this.handleMisformVal(formItem)
        }
      }
      return form
    },
    handleMisformVal(formItem) {
      if (this.multipleType.includes(formItem.form_element_type)) {
        formItem.field_default_values = []
      } else {
        formItem.field_default_values = ''
      }
    },
    getSelection() {
      return this.selection
    },
    // 表格右上角下拉展示显示的列数
    mulitChange(event, val) {
      // 如果event为true【展示】，false【隐藏】
      this.$emit('mulitChange', [event, val])
    }
  }
}
</script>

<style lang="less">
@import '~@/components/index.less';
.d1-vue-component .active-row {
  background: @primary-color;
  cursor: default;
}
</style>

<style lang="less" scoped>
.btn-gap {
  margin-right: 5px;
}
.sm-screen-search-btn-box {
  display: flex;
}
.search-btn-group {
  text-align: right;
  padding: 0;
  position: absolute;
  bottom: 8px;
  right: 0px;
  button {
    margin-right: 10px;
    &:last-of-type {
      margin-right: 0;
    }
  }
  .ant-btn {
    padding: 0 10px;
  }
}
.tool-bar {
  padding: 8px 0px;
}
.splitline {
  border-bottom: 1px solid #e8e8e8;
}
/deep/ .search-box {
  overflow: hidden;
}
// /deep/ .ant-collapse-header {
//   margin: 10px 0;
// }
.d1-vue-component-selection /deep/ .ant-table-row {
  cursor: pointer;
}
/deep/ .ant-collapse {
  background: #ffffff;
}
.ant-form-vertical .ant-form-item {
  padding-bottom: 0;
}
.ant-form-item {
  margin-bottom: 0;
}
// /deep/ .toggle-row {
//   margin-bottom: 15px;
// }
/deep/ .date-line:after {
  position: absolute;
  width: 24px;
  content: '~';
  line-height: 24px;
  text-align: center;
  float: left;
}

/deep/ .ant-calendar-picker, /deep/ .ant-time-picker {
  width: 100% !important;
}
/deep/ .ant-input-number {
  width: 100%;
}
/deep/ .ant-form-item-label label {
  width: 100%;
  display: inline-block;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}
/deep/ .header-tooltip {
  color: #2db7f5;
}
/deep/ .toolbar-btn-group {
  float: right;
  .ant-btn {
    margin-right: 10px;
    &:last-of-type {
      margin-right: 0;
    }
  }
}
/deep/ .ant-select-selection {
  border-top-width: 1px;
}
// /deep/ .form-item-col {
//   height: 74px;
// }
/deep/ .query-form-box .ant-form-item-label {
  padding: 0;
  line-height: 1;
  label {
    font-size: 12px;
    line-height: 1.3;
  }
}
/deep/ .form-row {
  display: flex;
}

/deep/ .ant-collapse > .ant-collapse-item > .ant-collapse-header {
  padding: 5px 10px;
}
/deep/ .ant-collapse-content > .ant-collapse-content-box {
  padding: 5px 10px;
}
/deep/ .chart-box .ant-form-item {
  margin-bottom: 10px;
  padding-bottom: 0px;
  &:last-child {
    margin-bottom: 0px;
  }
  .ant-form-item-label {
    line-height: 32px;
    label {
      line-height: 1.1;
    }
  }
  .ant-form-item-control {
    line-height: 32px;
  }
}
/deep/ .load-chart-btn {
  align-self: flex-end;
}
/deep/ .chart-area .ant-card-body {
  min-height: 400px;
  position: relative;
}
/deep/ .chart-area.null-box .ant-card-body::after {
    content: 'Chart Area';
    background: #f8f8f9;
    line-height: 400px;
    font-size: 32px;
    height: auto;
    display: block;
    text-align: center;
}

/deep/ .ant-select-selection-selected-value, /deep/ .ant-input, /deep/ .ant-select-selection__choice__content, /deep/ .ant-input-number-input, /deep/ .ant-cascader-picker-label, /deep/ .ant-select-search__field__placeholder {
  font-size: 12px;
}
/deep/ .query-form-box {
  margin-bottom: 8px;
}
/deep/ .ant-btn-sm {
  font-size: 12px;
}
/deep/ .ant-calendar-picker-input.ant-input {
  line-height: 24px;
}
</style>
