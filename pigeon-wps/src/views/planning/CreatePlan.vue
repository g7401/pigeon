<template>
  <a-card
    :bordered="false"
    size="small"
    class="create-path"
  >
    <SSteps
      ref="steps"
      :steps="steps"
      :initSteps="initSteps"
      :opButtons="opButtons"
      v-model="currentStepIndex"
      @handleOperationClick="handleButton"
      @lastStepChange="debounceHandleLastStepChange"
    >
      <template v-slot:attr="{ selfStep }">
        <div class="base-attr">
          <h3 class="title">基本参数</h3>
          <div class="base-attr-content">
            <a-form-model
              labelAlign="left"
              :label-col="labelCol"
              :wrapper-col="wrapperCol"
              ref="baseAttrForm"
              :model="selfStep.data.baseAttr"
              :rules="baseAttrRules"
            >
              <a-form-model-item
                label="经销商"
                prop="dealer"
                ref="dealer"
                :autoLink="false"
              >
                <s-cascader
                  :options="dealerOptions"
                  expandTrigger="hover"
                  placeholder="请选择经销商"
                  v-model="selfStep.data.baseAttr.dealer"
                  :disabled="isDisabledCascader"
                  @change="() => { $refs.dealer.onFieldChange();handleAttrChange(selfStep) }"
                />
              </a-form-model-item>
              <a-col :span="9">
                <a-form-model-item
                  label="计划日期范围"
                  required
                  :autoLink="false"
                  :validateStatus="planDateRangeValidateStatus"
                  :help="planDateRangeHelpText"
                >
                  <a-range-picker
                    ref="dateRange"
                    :open="rangeOpen"
                    v-model="selfStep.data.selectedDateRange"
                    @openChange="handleOpen"
                    @change="datePickChange(selfStep)"
                    @calendarChange="calendarChange($event, selfStep)"
                  />
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item
                  label="计划日期区间工作日设置"
                  prop="work_days"
                  ref="work_days"
                >
                  <DatesPicker
                    v-model="selfStep.data.baseAttr.work_days"
                    :validRange="selfStep.data.selectedDateRange"
                    :fullscreen="false"
                    :disabled="true"
                    @change="handleSelectWorkDays"
                  ></DatesPicker>
                </a-form-model-item>
              </a-col>
            </a-form-model>
          </div>
        </div>
        <div class="data-source">
          <h3 class="title">数据源</h3>
          <SUpload v-model="selfStep.data.fileId" @uploading="setFileName(selfStep, $event)" :config="{ btnText: 'Import' }"></SUpload>
          <a :href="selfStep.data.fileId" class="download-btn" target="blank" v-if="selfStep.data.fileName && editPlanInstanceId">{{ selfStep.data.fileName }}</a>
          <!-- <div class="data-source-content">
            <a-tabs
              v-model="currentTab"
              size="small"
            >
              <a-tab-pane
                v-for="tab in dataSourceTabs"
                :key="tab.key"
                :tab="tab.label"
              >
                <d1-vue-component
                  :options="getD1Options(tab.key)"
                  :key="tab.key"
                ></d1-vue-component>
              </a-tab-pane>
            </a-tabs>
          </div> -->
        </div>
        <div class="optimize-attr mg-t-20">
          <h3 class="title">优化参数</h3>
          <div class="optimize-attr-content">
            <a-form-model
              :label-col="optimizeLabelCol"
              :wrapper-col="optimizeWrapperCol"
              ref="optimizeAttrForm"
              :model="selfStep.data.optimizeAttr"
            >
              <div class="form-side-box">

                <a-form-model-item
                  label="业务员拜访交通方式"
                  prop="visit_way"
                  ref="visit_way"
                >
                  <a-radio-group
                    v-model="selfStep.data.optimizeAttr.visit_way"
                    :options="visitWayOptions"
                  ></a-radio-group>
                </a-form-model-item>

                <!-- <a-form-model-item
                  label="考虑最近一访"
                  prop="consider_last_visit"
                  ref="consider_last_visit"
                >
                  <a-checkbox v-model="selfStep.data.optimizeAttr.consider_last_visit"></a-checkbox>
                </a-form-model-item> -->

                <!-- <a-form-model-item
                  label="均衡优化倾向"
                  prop="optimize_type"
                  ref="optimize_type"
                >
                  <a-radio-group
                    v-model="selfStep.data.optimizeAttr.optimize_type"
                    :options="optimizeTypeOptions"
                  ></a-radio-group>
                </a-form-model-item> -->
                <!-- <a-form-model-item
                  label="每天在店时长范围"
                  prop="visit_time_range"
                  ref="visit_time_range"
                >
                  <a-slider
                    range
                    :step="1"
                    v-model="selfStep.data.optimizeAttr.visit_time_range"
                  />
                </a-form-model-item> -->
                <!-- <a-form-model-item
                  label="每天带店量范围"
                  prop="visit_store_num_range"
                  ref="visit_store_num_range"
                >
                  <a-slider
                    range
                    :step="1"
                    v-model="selfStep.data.optimizeAttr.visit_store_num_range"
                  />
                </a-form-model-item> -->
              </div>
              <div class="form-side-box">
                <!-- <a-form-model-item
                  label="目标业务员数量"
                  prop="target_saleman_num_mode"
                  ref="target_saleman_num_mode"
                >
                  <a-radio-group
                    v-model="selfStep.data.optimizeAttr.target_saleman_num_mode"
                    :options="targetSalemanModeOptions"
                  ></a-radio-group>
                  <a-input
                    v-if="selfStep.data.optimizeAttr.target_saleman_num_mode === 'update_num'"
                    v-model="selfStep.data.optimizeAttr.target_saleman_num"
                    placeholder="请填写数量"
                  ></a-input>
                </a-form-model-item> -->
                <!-- <a-form-model-item
                  label="片区划分优化策略"
                  prop="optimize_strategy"
                  ref="optimize_strategy"
                >
                  <a-table
                    class="more-small-table"
                    size="small"
                    rowKey="saleman_name"
                    :dataSource="selfStep.data.optimizeAttr.optimize_strategy"
                    :columns="optimizeStrategyColumns"
                    :pagination="false"
                    bordered
                  >
                    <template
                      slot="change_type"
                      slot-scope="text, record"
                    >
                      <a-radio-group
                        v-model="record['change_type']"
                        :options="changeTypeOptions"
                      ></a-radio-group>
                    </template>
                  </a-table>
                </a-form-model-item> -->
              </div>
            </a-form-model>
          </div>
        </div>
      </template>
      <template v-slot:area="{ selfStep }">
        <template v-if="selfStep.status === 'finish'">
          <!-- <a-modal
            title="分配业务员"
            ok-text="确认"
            cancel-text="取消"
            v-model
            :confirm-loading="confirmLoading"
            :width="800"
            @ok="handleOk"
            @cancel="handleCancel"
          >
          </a-modal> -->
          <ToggleHeight
            v-if="selfStep.data.statisticsData"
            :showLength="2"
            :gradientHeight="10"
          >
            <h3 class="title">总体统计</h3>
            <a-table
              class="more-small-table"
              size="small"
              :pagination="false"
              :columns="columnsMap[selfStep.key].totalStatistics"
              :dataSource="[selfStep.data.statisticsData.total || {}]"
              :loading="selfStep.data.statisticsLoading"
            ></a-table>
            <h3 class="title mg-t-10">具体统计</h3>
            <a-table
              class="more-small-table"
              size="small"
              :pagination="false"
              :columns="columnsMap[selfStep.key].specificStatistics"
              :dataSource="selfStep.data.statisticsData.specific"
              :loading="selfStep.data.statisticsLoading"
            ></a-table>
          </ToggleHeight>
          <MarksSelector
            key="area"
            ref="area"
            v-if="selfStep.data.markData"
            v-model="selfStep.data.markData"
            :isSelect="isNoFinish"
            :typeList="markAttrsMap[selfStep.key].typeList"
            :typeField="markAttrsMap[selfStep.key].typeField"
            :nameField="markAttrsMap[selfStep.key].nameField"
            :showFields="markAttrsMap[selfStep.key].showFields"
            @input="handleMarkDataChange(selfStep)"
          >
            <template v-slot:tableButtons>
              <a-button
                @click="restoreMarkData(selfStep)"
                :loading="selfStep.data.restoreMarkLoading"
                type="primary"
                ghost
              >还原成系统输出结果</a-button>
              <a-button
                @click="saveMarkData(selfStep)"
                :loading="selfStep.data.saveMarkLoading"
                type="primary"
              >保存</a-button>
            </template>
          </MarksSelector>
        </template>
      </template>
      <template v-slot:eight_week="{ selfStep }">
        <template v-if="selfStep.status === 'finish'">
          <ToggleHeight
            v-if="selfStep.data.statisticsData"
            :showLength="2"
            :gradientHeight="10"
          >
            <h3 class="title">总体统计</h3>
            <a-table
              class="more-small-table"
              size="small"
              :pagination="false"
              :columns="columnsMap[selfStep.key].totalStatistics"
              :dataSource="[selfStep.data.statisticsData.total || {}]"
              :loading="selfStep.data.statisticsLoading"
            ></a-table>
            <h3 class="title mg-t-10">具体统计</h3>
            <a-table
              class="more-small-table"
              size="small"
              :pagination="false"
              :columns="columnsMap[selfStep.key].specificStatistics"
              :dataSource="selfStep.data.statisticsData.specific"
              :loading="selfStep.data.statisticsLoading"
            ></a-table>
          </ToggleHeight>
          <div class="marks-wrapper">
            <MarksSelector
              key="eight_week"
              ref="eight_week"
              v-if="selfStep.data.markData"
              v-model="selfStep.data.markData"
              :isSelect="isNoFinish"
              :typeList="markAttrsMap[selfStep.key].typeList"
              :typeField="markAttrsMap[selfStep.key].typeField"
              :nameField="markAttrsMap[selfStep.key].nameField"
              :showFields="markAttrsMap[selfStep.key].showFields"
              @input="handleMarkDataChange(selfStep)"
              :collapseActiveKey="selfStep.data.collapseActiveKey"
              @update:collapseActiveKey="selfStep.data.collapseActiveKey = $event"
            >
              <template v-slot:tableButtons>
                <a-button
                  @click="restoreMarkData(selfStep)"
                  :loading="selfStep.data.restoreMarkLoading"
                  type="primary"
                  ghost
                >还原成系统输出结果</a-button>
                <a-button
                  @click="saveMarkData(selfStep)"
                  :loading="selfStep.data.saveMarkLoading"
                  type="primary"
                >保存</a-button>
              </template>
              <template v-slot:collapsePanel>
                <a-collapse-panel
                  key="filter"
                  header="筛选结果"
                >
                  <a-select
                    style="width: 100%"
                    placeholder="选择业务员名称"
                    v-model="selfStep.data.selectedsalemanName"
                    :options="salemanOptions"
                    @change="handleSelectSaleMan(selfStep)"
                  ></a-select>
                </a-collapse-panel>
              </template>
              ></MarksSelector>
          </div>
        </template>
      </template>
      <template v-slot:per_week="{ selfStep }">
        <template v-if="selfStep.status === 'finish'">
          <ToggleHeight
            v-if="selfStep.data.statisticsData"
            :showLength="2"
            :gradientHeight="10"
          >
            <h3 class="title">总体统计</h3>
            <a-table
              class="more-small-table"
              size="small"
              :pagination="false"
              :columns="columnsMap[selfStep.key].totalStatistics"
              :dataSource="[selfStep.data.statisticsData.total || {}]"
              :loading="selfStep.data.statisticsLoading"
            ></a-table>
            <h3 class="title mg-t-10">具体统计</h3>
            <a-table
              class="more-small-table"
              size="small"
              :pagination="false"
              :columns="columnsMap[selfStep.key].specificStatistics"
              :dataSource="selfStep.data.statisticsData.specific"
              :loading="selfStep.data.statisticsLoading"
            ></a-table>
          </ToggleHeight>
          <div class="marks-wrapper">
            <MarksSelector
              key="per_week"
              ref="per_week"
              v-if="selfStep.data.markData"
              v-model="selfStep.data.markData"
              :isSelect="isNoFinish"
              :typeList="markAttrsMap[selfStep.key].typeList"
              :typeField="markAttrsMap[selfStep.key].typeField"
              :nameField="markAttrsMap[selfStep.key].nameField"
              :showFields="markAttrsMap[selfStep.key].showFields"
              @input="handleMarkDataChange(selfStep)"
              :collapseActiveKey="selfStep.data.collapseActiveKey"
              @update:collapseActiveKey="selfStep.data.collapseActiveKey = $event"
            >
              <template v-slot:tableButtons>
                <a-button
                  @click="restoreMarkData(selfStep)"
                  :loading="selfStep.data.restoreMarkLoading"
                  type="primary"
                  ghost
                >还原成系统输出结果</a-button>
                <a-button
                  @click="saveMarkData(selfStep)"
                  :loading="selfStep.data.saveMarkLoading"
                  type="primary"
                >保存</a-button>
              </template>
              <template v-slot:collapsePanel>
                <a-collapse-panel
                  key="filter"
                  header="筛选结果"
                >
                  <a-select
                    style="width: 100%"
                    placeholder="选择业务员名称"
                    v-model="selfStep.data.selectedsalemanName"
                    :options="salemanOptions"
                    @change="handleSelectSaleMan(selfStep)"
                  ></a-select>
                  <AWeekPicker
                    class="mg-t-10"
                    autoFocus
                    v-model="selfStep.data.calendarMoment"
                    @change="handleSelectWeek($event, selfStep)"
                    :disabledDate="handleDisabledDate"
                  ></AWeekPicker>
                  <!-- <a-calendar
                    class="
                    calendar-box
                    mg-t-10"
                    :fullscreen="false"
                    :disabledDate="handleDisabledDate"
                    v-model="selfStep.data.calendarMoment"
                    valueFormat="YYYY-MM-DD"
                    @select="handleSelectWeek($event, selfStep)"
                  >
                    </a-calendar> -->
                </a-collapse-panel>
              </template>
            </MarksSelector>
          </div>
        </template>
      </template>
      <template v-slot:per_day="{ selfStep }">
        <template v-if="selfStep.status === 'finish'">
          <ToggleHeight
            v-if="selfStep.data.statisticsData"
            :showLength="2"
            :gradientHeight="10"
          >
            <h3 class="title">总体统计</h3>
            <a-table
              class="more-small-table"
              size="small"
              :pagination="false"
              :columns="columnsMap[selfStep.key].totalStatistics"
              :dataSource="[selfStep.data.statisticsData.total || {}]"
              :loading="selfStep.data.statisticsLoading"
            ></a-table>
            <h3 class="title mg-t-10">具体统计</h3>
            <a-table
              class="more-small-table"
              size="small"
              :pagination="false"
              :columns="columnsMap[selfStep.key].specificStatistics"
              :dataSource="selfStep.data.statisticsData.specific"
              :loading="selfStep.data.statisticsLoading"
            ></a-table>
          </ToggleHeight>
          <div class="marks-wrapper">
            <MarksSelector
              key="per_day"
              ref="per_day"
              v-if="selfStep.data.markData"
              v-model="selfStep.data.markData"
              :isSelect="false"
              :typeList="markAttrsMap[selfStep.key].typeList"
              :typeField="markAttrsMap[selfStep.key].typeField"
              :nameField="markAttrsMap[selfStep.key].nameField"
              :routeField="markAttrsMap[selfStep.key].routeField"
              :showFields="markAttrsMap[selfStep.key].showFields"
              @input="handleMarkDataChange(selfStep)"
              :collapseActiveKey="selfStep.data.collapseActiveKey"
              @update:collapseActiveKey="selfStep.data.collapseActiveKey = $event"
            >
              <template v-slot:tableButtons>
                <a-button
                  @click="restoreMarkData(selfStep)"
                  :loading="selfStep.data.restoreLoading"
                  type="primary"
                  ghost
                >还原成系统输出结果</a-button>
                <a-button
                  @click="saveMarkData(selfStep)"
                  :loading="selfStep.data.saveMarkLoading"
                  type="primary"
                >保存</a-button>
              </template>
              <template v-slot:collapsePanel>
                <a-collapse-panel
                  key="filter"
                  header="筛选结果"
                >
                  <a-select
                    style="width: 100%"
                    placeholder="选择业务员名称"
                    v-model="selfStep.data.selectedsalemanName"
                    :options="salemanOptions"
                    @change="handleSelectSaleMan(selfStep)"
                  ></a-select>
                  <a-calendar
                    class="calendar-box mg-t-10"
                    :fullscreen="false"
                    :disabledDate="handleDisabledDate"
                    v-model="selfStep.data.calendarMoment"
                    @select="handleSelectPerDayDate($event, selfStep)"
                  >
                  </a-calendar>
                </a-collapse-panel>
              </template>
            </MarksSelector>
          </div>
        </template>
      </template>
      <template v-slot:confirm="{ selfStep }">
        <ToggleHeight
          v-if="selfStep.data.statisticsData"
          :showLength="2"
          :gradientHeight="10"
        >
          <h3 class="title">总体统计</h3>
          <a-table
            class="more-small-table"
            size="small"
            :pagination="false"
            :columns="columnsMap[selfStep.data.selectedStep].totalStatistics"
            :dataSource="[selfStep.data.statisticsData.total || {}]"
            :loading="selfStep.data.statisticsLoading"
          ></a-table>
          <h3 class="title mg-t-10">具体统计</h3>
          <a-table
            class="more-small-table"
            size="small"
            :pagination="false"
            :columns="columnsMap[selfStep.data.selectedStep].specificStatistics"
            :dataSource="selfStep.data.statisticsData.specific"
            :loading="selfStep.data.statisticsLoading"
          ></a-table>
        </ToggleHeight>
        <div class="marks-wrapper">
          <MarksSelector
            key="confirm"
            :isSelect="false"
            v-if="selfStep.data.markData"
            v-model="selfStep.data.markData"
            :typeList="markAttrsMap[selfStep.data.selectedStep].typeList"
            :typeField="markAttrsMap[selfStep.data.selectedStep].typeField"
            :nameField="markAttrsMap[selfStep.data.selectedStep].nameField"
            :showFields="markAttrsMap[selfStep.data.selectedStep].showFields"
            :routeField="markAttrsMap[selfStep.data.selectedStep].routeField"
            :collapseActiveKey="selfStep.data.collapseActiveKey"
            @update:collapseActiveKey="selfStep.data.collapseActiveKey = $event"
          >
            <template v-slot:collapsePanel>
              <a-collapse-panel
                key="filter"
                header="筛选结果"
              >
                <a-radio-group
                  class="select-step-radios"
                  v-model="selfStep.data.selectedStep"
                  :options="stepOptions"
                  @change="loadMarkData(selfStep)"
                ></a-radio-group>
                <a-select
                  class="mg-tb-10"
                  v-if="selfStep.data.selectedStep !== 'area'"
                  style="width: 100%"
                  placeholder="选择业务员名称"
                  v-model="selfStep.data.selectedsalemanName"
                  :options="salemanOptions"
                  @change="handleSelectSaleMan(selfStep)"
                ></a-select>
                <!-- <a-calendar
                  class="calendar-box"
                  v-if="selfStep.data.selectedStep === 'per_week'"
                  :fullscreen="false"
                  :disabledDate="handleDisabledDate"
                  v-model="selfStep.data.calendarMoment"
                  @select="handleSelectWeek($event, selfStep)"
                >
                </a-calendar> -->
                <AWeekPicker
                  v-if="selfStep.data.selectedStep === 'per_week'"
                  v-model="selfStep.data.calendarMoment"
                  @change="handleSelectWeek($event, selfStep)"
                  :disabledDate="handleDisabledDate"
                ></AWeekPicker>
                <a-calendar
                  class="calendar-box"
                  v-if="selfStep.data.selectedStep === 'per_day'"
                  :fullscreen="false"
                  valueFormat="YYYY-MM-DD"
                  v-model="selfStep.data.calendarMoment2"
                  @select="handleSelectPerDayDate($event, selfStep)"
                >
                </a-calendar>
              </a-collapse-panel>
            </template>
          </MarksSelector>
        </div>
      </template>
    </SSteps>
  </a-card>
</template>

<script>
import debounce from 'lodash.debounce'
import cloneDeep from 'lodash.clonedeep'
import moment from 'moment'
import { Calendar, Slider, DatePicker } from 'ant-design-vue'
import SSteps from '@/components/common/SSteps'
import MarksSelector from '@/components/common/MarksSelector'
import ToggleHeight from '@/components/common/ToggleHeight'
import DatesPicker from '@/components/common/DatesPicker'
import SCascader from '@/components/common/SCascader'
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import SUpload from '@/components/common/SUpload'
import { polling } from '@/utils/util'
import D1Api from '@/api/d1'
import { mapGetters } from 'vuex'

const lawHolidays = [
  '2020-01-01',
  '2020-01-24',
  '2020-01-25',
  '2020-01-26',
  '2020-01-27',
  '2020-01-28',
  '2020-01-29',
  '2020-01-30',
  '2020-01-31',
  '2020-02-01',
  '2020-02-02',
  '2020-04-04',
  '2020-04-05',
  '2020-04-06',
  '2020-05-01',
  '2020-05-02',
  '2020-05-03',
  '2020-05-04',
  '2020-05-05',
  '2020-06-25',
  '2020-06-26',
  '2020-06-27',
  '2020-10-01',
  '2020-10-02',
  '2020-10-03',
  '2020-10-04',
  '2020-10-05',
  '2020-10-06',
  '2020-10-07',
  '2020-10-08'
]

const lawWorkDays = ['2020-01-19', '2020-04-26', '2020-05-09', '2020-06-28', '2020-09-27', '2020-10-10']

const visitWayOptions = [
  {
    value: 'walk',
    label: '步行'
  },
  {
    value: 'ride',
    label: '骑行'
  }
]

const optimizeTypeOptions = [
  {
    value: 'work_load',
    label: '工作量（在店时长，在途时长，带店数，店次数）平均优先'
  },
  {
    value: 'distance_center',
    label: '距离集中优先'
  }
]

const targetSalemanModeOptions = [
  {
    value: 'keep',
    label: '维持现状'
  },
  {
    value: 'update_num',
    label: '更新数量'
  }
]

const changeTypeOptions = [
  {
    value: 'keep',
    label: '不动'
  },
  {
    value: 'add',
    label: '增'
  },
  {
    value: 'reduce',
    label: '增'
  },
  {
    value: 'add_and_reduce',
    label: '增和减'
  }
]

const stepOptions = [
  {
    value: 'area',
    label: '片区划分'
  },
  {
    value: 'eight_week',
    label: '8 weeks'
  },
  {
    value: 'per_week',
    label: 'by week'
  },
  {
    value: 'per_day',
    label: 'by day'
  }
]

const markAttrsMap = {
  area: {
    oldTypeField: 'old_store_area',
    typeField: 'new_store_area',
    nameField: 'store_name',
    showFields: [
      { key: 'store_name', label: '门店' },
      { key: 'old_store_area', label: '现片区' },
      { key: 'new_store_area', label: '新片区' }
    ],
    typeList: ['1', '2', '3', '4', '5']
  },
  eight_week: {
    oldTypeField: 'old_week_number',
    typeField: 'new_week_number',
    nameField: 'store_name',
    showFields: [
      { key: 'store_name', label: '门店' },
      { key: 'old_week_number', label: '旧的周序号' },
      { key: 'new_week_number', label: '新的周序号' }
    ],
    typeList: ['1', '2', '3', '4', '5', '6', '7', '8']
  },
  per_week: {
    oldTypeField: 'old_day_number',
    typeField: 'new_day_number',
    nameField: 'store_name',
    showFields: [
      { key: 'store_name', label: '门店' },
      { key: 'old_day_number', label: '旧的天序号' },
      { key: 'new_day_number', label: '新的天序号' }
    ],
    typeList: ['1', '2', '3', '4', '5', '6', '7', '8']
  },
  per_day: {
    oldTypeField: 'old_route_number',
    typeField: 'new_route_number',
    nameField: 'store_name',
    routeField: 'new_route_number',
    showFields: [
      { key: 'store_name', label: '门店' },
      { key: 'old_route_number', label: '旧的路线序号' },
      { key: 'new_route_number', label: '新的路线序号' }
    ],
    typeList: ['1', '2', '3', '4', '5', '6', '7', '8']
  }
}

const optimizeStrategyColumns = [
  {
    key: 'saleman_name',
    dataIndex: 'saleman_name',
    title: '原业务员姓名'
  },
  {
    key: 'change_type',
    title: '覆盖门店范围允许变化类型',
    scopedSlots: { customRender: 'change_type' }
  }
]

const dataSourceTabs = [
  // {
  //   label: '门店主数据',
  //   key: 'store_dfk'
  // },
  // {
  //   label: '业务员门店关联主数据',
  //   key: 'saleman_store_relation_dfk'
  // },
  // {
  //   label: '门店拜访频次主数据',
  //   key: 'store_visit_frequency_dfk'
  // },
  // {
  //   label: '门店在店工作时长主数据',
  //   key: 'store_work_time_dfk'
  // },
  // {
  //   label: '门店拜访记录',
  //   key: 'store_visit_dfk'
  // }
]

const steps = () => [
  {
    title: '参数配置',
    key: 'attr',
    status: 'wait',
    data: {
      baseAttr: {
        dealer: [],
        start_date: '',
        end_date: '',
        work_days: []
      },
      fileId: '',
      optimizeAttr: {
        visit_way: 'walk'
        // consider_last_visit: true,
        // optimize_type: 'work_load',
        // visit_time_range: [],
        // visit_store_num_range: [],
        // target_saleman_num_mode: 'keep',
        // target_saleman_num: undefined,
        // optimize_strategy: []
      },
      selectedDateRange: [],
      collapseActiveKey: 'selected'
    }
  },
  {
    title: '片区划分',
    key: 'area',
    status: 'wait',
    data: {
      markData: null,
      statisticsData: null
    }
  },
  {
    title: '8周划分',
    key: 'eight_week',
    status: 'wait',
    data: {
      markData: null,
      statisticsData: null,
      statisticsLoading: false,
      saveMarkLoading: false,
      restoreMarkLoading: false,
      selectedsalemanName: undefined,
      collapseActiveKey: 'selected'
    }
  },
  {
    title: '每天路线',
    key: 'per_day',
    status: 'wait',
    data: {
      markData: null,
      statisticsData: null,
      statisticsLoading: false,
      saveMarkLoading: false,
      restoreMarkLoading: false,
      selectedsalemanName: undefined,
      selectedDate: null,
      collapseActiveKey: 'selected',
      calendarMoment: moment()
    }
  },
  {
    title: '确认计划',
    key: 'confirm',
    status: 'wait',
    data: {
      markData: null,
      statisticsData: null,
      statisticsLoading: false,
      saveMarkLoading: false,
      restoreMarkLoading: false,
      selectedsalemanName: undefined,
      selectedWeek: null,
      selectedDate: null,
      selectedStep: 'area',
      collapseActiveKey: 'filter',
      calendarMoment: null,
      calendarMoment2: null
    }
  }
]

const adminStep = () => ({
    title: '每周划分',
    key: 'per_week',
    status: 'wait',
    data: {
      markData: null,
      statisticsData: null,
      statisticsLoading: false,
      saveMarkLoading: false,
      restoreMarkLoading: false,
      selectedsalemanName: undefined,
      selectedWeek: null,
      collapseActiveKey: 'selected',
      calendarMoment: moment()
    }
  })

// eslint-disable-next-line no-unused-vars
const columnsMap = {
  area: {
    totalStatistics: [
      {
        key: 'total_area_num',
        dataIndex: 'total_area_num',
        title: '总片区数量'
      },
      {
        key: 'total_store_num',
        dataIndex: 'total_store_num',
        title: '总门店数量'
      },
      // {
      //   key: 'total_week_num',
      //   dataIndex: 'total_week_num',
      //   title: '计划期间总周数'
      // },
      {
        key: 'total_store_visit_num',
        dataIndex: 'total_store_visit_num',
        title: '总访店次数'
      },
      {
        key: 'total_visit_time',
        dataIndex: 'total_visit_time',
        title: '总在店时长'
      },
      {
        key: 'area_average_store_num',
        dataIndex: 'area_average_store_num',
        title: '每片区平均门店数量'
      },
      {
        key: 'area_average_store_visit_num',
        dataIndex: 'area_average_store_visit_num',
        title: '每片区平均访店次数'
      },
      {
        key: 'area_average_visit_time',
        dataIndex: 'area_average_visit_time',
        title: '每片区平均在店时长'
      }
    ],
    specificStatistics: [
      {
        key: 'serail',
        dataIndex: 'serail',
        title: '片区序号'
      },
      {
        key: 'store_num',
        dataIndex: 'store_num',
        title: '门店数量'
      },
      {
        key: 'store_visit_num',
        dataIndex: 'store_visit_num',
        title: '访店次数'
      },
      {
        key: 'visit_time',
        dataIndex: 'visit_time',
        title: '在店时长'
      }
    ]
  },
  eight_week: {
    totalStatistics: [
      {
        key: 'total_week_num',
        dataIndex: 'total_week_num',
        title: '计划期间总周数'
      },
      {
        key: 'total_work_day_num',
        dataIndex: 'total_work_day_num',
        title: '计划期间总工作日数'
      },
      {
        key: 'total_visit_store_num',
        dataIndex: 'total_visit_store_num',
        title: '总带店量'
      },
      {
        key: 'total_store_visit_num',
        dataIndex: 'total_store_visit_num',
        title: '总访店次数'
      },
      {
        key: 'total_visit_time',
        dataIndex: 'total_visit_time',
        title: '总在店时长'
      }
      // {
      //   key: 'everyday_average_visit_store_num',
      //   dataIndex: 'everyday_average_visit_store_num',
      //   title: '平均每天带店量'
      // },
      // {
      //   key: 'everyday_average_store_visit_num',
      //   dataIndex: 'everyday_average_store_visit_num',
      //   title: '平均每天访店次数'
      // },
      // {
      //   key: 'everyday_average_visit_time',
      //   dataIndex: 'everyday_average_visit_time',
      //   title: '平均每天在店时长'
      // }
    ],
    specificStatistics: [
      {
        key: 'serail',
        dataIndex: 'serail',
        title: '周序号'
      },
      {
        key: 'work_day_num',
        dataIndex: 'work_day_num',
        title: '工作日数'
      },
      {
        key: 'visit_store_num',
        dataIndex: 'visit_store_num',
        title: '带店量'
      },
      {
        key: 'store_visit_num',
        dataIndex: 'store_visit_num',
        title: '访店次数'
      },
      {
        key: 'visit_time',
        dataIndex: 'visit_time',
        title: '在店时长'
      },
      {
        key: 'everyday_average_visit_store_num',
        dataIndex: 'everyday_average_visit_store_num',
        title: '平均每天带店量'
      },
      {
        key: 'everyday_average_store_visit_num',
        dataIndex: 'everyday_average_store_visit_num',
        title: '平均每天访店次数'
      },
      {
        key: 'everyday_average_visit_time',
        dataIndex: 'everyday_average_visit_time',
        title: '平均每天在店时长'
      }
    ]
  },
  per_week: {
    totalStatistics: [
      {
        key: 'total_work_day_num',
        dataIndex: 'total_work_day_num',
        title: '总工作日数'
      },
      {
        key: 'total_visit_store_num',
        dataIndex: 'total_visit_store_num',
        title: '总带店量'
      },
      {
        key: 'total_store_visit_num',
        dataIndex: 'total_store_visit_num',
        title: '总访店次数'
      },
      {
        key: 'total_visit_time',
        dataIndex: 'total_visit_time',
        title: '总在店时长'
      },
      {
        key: 'everyday_average_visit_store_num',
        dataIndex: 'everyday_average_visit_store_num',
        title: '平均每天带店量'
      },
      {
        key: 'everyday_average_store_visit_num',
        dataIndex: 'everyday_average_store_visit_num',
        title: '平均每天访店次数'
      },
      {
        key: 'everyday_average_visit_time',
        dataIndex: 'everyday_average_visit_time',
        title: '平均每天在店时长'
      }
    ],
    specificStatistics: [
      {
        key: 'serail',
        dataIndex: 'serail',
        title: '天序号'
      },
      {
        key: 'visit_store_num',
        dataIndex: 'visit_store_num',
        title: '带店量'
      },
      {
        key: 'store_visit_num',
        dataIndex: 'store_visit_num',
        title: '访店次数'
      },
      {
        key: 'visit_time',
        dataIndex: 'visit_time',
        title: '在店时长'
      }
    ]
  },
  per_day: {
    totalStatistics: [
      {
        key: 'total_visit_store_num',
        dataIndex: 'total_visit_store_num',
        title: '总带店量'
      },
      {
        key: 'total_work_time',
        dataIndex: 'total_work_time',
        title: '总工作时长'
      },
      {
        key: 'total_visit_time',
        dataIndex: 'total_visit_time',
        title: '总在店时长'
      },
      {
        key: 'total_on_way_time',
        dataIndex: 'total_on_way_time',
        title: '总在途时长'
      },
      {
        key: 'total_on_way_distance',
        dataIndex: 'total_on_way_distance',
        title: '总在途距离'
      }
    ],
    specificStatistics: [
      {
        key: 'serail',
        dataIndex: 'serail',
        title: '路线序号'
      },
      {
        key: 'store_code',
        dataIndex: 'store_code',
        title: '门店编号'
      },
      {
        key: 'store_name',
        dataIndex: 'store_name',
        title: '门店名称'
      },
      {
        key: 'visit_time',
        dataIndex: 'visit_time',
        title: '在店时长'
      }
      // {
      //   key: 'on_way_time',
      //   dataIndex: 'on_way_time',
      //   title: '在途时长'
      // },
      // {
      //   key: 'on_way_distance',
      //   dataIndex: 'on_way_distance',
      //   title: '在途距离'
      // }
    ]
  }
}

export default {
  name: 'CreatePlan',
  components: {
    SSteps,
    MarksSelector,
    ToggleHeight,
    d1VueComponent,
    ACalendar: Calendar,
    ASlider: Slider,
    DatesPicker,
    SCascader,
    AWeekPicker: DatePicker.WeekPicker,
    SUpload
  },
  props: {
    auth: {
      type: String,
      default: 'user'
    },
    editPlanInstanceId: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      isDisabledCascader: false,
      rangeOpen: false,
      columnsMap,
      markAttrsMap,
      stepOptions,
      changeTypeOptions,
      targetSalemanModeOptions,
      visitWayOptions,
      optimizeTypeOptions,
      optimizeStrategyColumns,
      dataSourceTabs,
      dealerOptions: [],
      planInstanceId: null,
      currentStepIndex: 0,
      steps: steps(),
      initSteps: [],
      opButtons: [
        {
          key: 'startCompute',
          label: '开始计算',
          show: step => {
            return (
              this.isNoFinish &&
              ['area', 'eight_week', 'per_week', 'per_day'].includes(step.key) &&
              (step.status === 'wait' || step.status === 'error')
            )
          }
        },
        {
          key: 'computing',
          label: '正在计算中...',
          loading: true,
          disabled: true,
          show: step => {
            return (
              this.isNoFinish &&
              ['area', 'eight_week', 'per_week', 'per_day'].includes(step.key) &&
              step.status === 'process'
            )
          }
        },
        {
          key: 'reCompute',
          label: '重新计算',
          isConfirm: true,
          show: step => {
            return (
              this.isNoFinish &&
              ['area', 'eight_week', 'per_week', 'per_day'].includes(step.key) &&
              step.status === 'finish'
            )
          }
        },
        {
          key: 'exportResult',
          type: 'default',
          label: '导出结果',
          loading: false,
          show: step => {
            return (
              (['area', 'eight_week', 'per_week', 'per_day'].includes(step.key) && step.status === 'finish') ||
              (step.key === 'confirm' && step.status === 'wait')
            )
          }
        },
        {
          key: 'confirmPlan',
          label: '确认计划',
          loading: false,
          show: step => {
            return step.key === 'confirm' && step.status === 'wait'
          }
        },
        {
          key: 'reConfig',
          label: '重新配置',
          loading: false,
          isConfirm: true,
          show: step => {
            return (
              this.isNoFinish &&
              step.key === 'attr' &&
              step.status === 'finish' &&
              this.steps.slice(1).some(x => x.status === 'process')
            )
          }
        },
        {
          key: 'saveAttrs',
          label: '保存',
          isConfirm: true,
          loading: false,
          show: step => {
            return this.isNoFinish && step.key === 'attr' && step.status !== 'finish'
          }
        }
      ],
      currentTab: 'store_dfk',
      salemanOptions: [],
      planDateRangeValidateStatus: 'validating',
      planDateRangeHelpText: '',
      baseAttrRules: {
        work_days: [
          {
            required: true
          }
        ],
        dealer: [
          {
            required: true
          }
        ]
      },
      labelCol: {
        span: 12
      },
      wrapperCol: {
        span: 18
      },
      optimizeLabelCol: {
        span: 7
      },
      optimizeWrapperCol: {
        span: 17
      },
      planInstanceLoading: false,
      stopPolling: false
    }
  },
  computed: {
    isNoFinish () {
      return this.steps[this.steps.length - 1].status !== 'finish'
    },
    currentStep: {
      get () {
        return this.steps[this.currentStepIndex]
      }
    },
    attrStep () {
      return this.steps[0]
    },
    ...mapGetters(['nickname', 'suppliers'])
  },
  watch: {
    async currentStepIndex () {
      if (
        this.currentStep.key === 'confirm' ||
        (['area', 'eight_week', 'per_week', 'per_day'].includes(this.currentStep.key) &&
          this.currentStep.status === 'finish')
      ) {
        if (this.currentStep.key !== 'attr' && this.currentStep.status === 'finish' && !this.salemanOptions.length) {
          await this.loadSaleManList()
        }
        this.loadMarkData(this.steps[this.currentStepIndex])
      }
      if (this.currentStep.key === 'attr' && !this.dealerOptions.length) {
        await this.loadDealers()
        this.setDefaultDealer()
      }
    }
  },
  async created () {
    if (this.auth === 'admin') {
      this.steps.splice(3, 0, adminStep())
    }
    if (this.editPlanInstanceId) {
      this.planInstanceId = this.editPlanInstanceId
      // await this.loadSaleManList()
      await this.loadPlanInstance(this.editPlanInstanceId)
      if (['area', 'eight_week', 'per_week', 'per_day'].includes(this.currentStep.key) && this.currentStep.status === 'process') {
        this.queryComputeStatus(this.steps[this.currentStepIndex])
      }
    } else {
      await this.createPlanInstance()
    }
    this.initSteps = cloneDeep(this.steps)
    await this.loadDealers()
    this.setDefaultDealer()
    // this.loadMarkData(this.steps[this.currentStepIndex])
  },
  methods: {
    getCrumbs (dataList, supplierName) {
      const city = dataList.find(
        item => item.children && item.children.some(child => child.label === supplierName && child.level === 5)
      )
      const province = dataList.find(
        item => item.children && item.children.some(child => child.label === city.label && child.level === 4)
      )
      const group = dataList.find(
        item => item.children && item.children.some(child => child.label === province.label && child.level === 3)
      )
      const area = dataList.find(
        item => item.children && item.children.some(child => child.label === group.label && child.level === 2)
      )
      return [area.label, group.label, province.label, city.label, supplierName]
    },
    generateList (data, level = 1, dataList = []) {
      for (let i = 0; i < data.length; i++) {
        const node = data[i]
        node.level = level
        dataList.push(node)
        if (node.children) {
          level++
          this.generateList(node.children, level, dataList)
        }
      }
      return dataList
    },
    setDefaultDealer () {
      if (this.suppliers && this.suppliers.length) {
        const list = this.generateList(this.dealerOptions)
        this.steps[0].data.baseAttr.dealer = this.getCrumbs(list, this.suppliers[0])
        this.isDisabledCascader = true
      }
    },
    setFileName (step, file) {
      step.data.fileName = file.name
    },
    debounceHandleLastStepChange: debounce(function (lastStep) { this.handleLastStepChange(lastStep) }, 100),
    handleLastStepChange (lastStep) {
      if (this.planInstanceLoading) return
      if (!this.planInstanceId) return
      this.$api.saveLastStep(this.planInstanceId, lastStep.key, lastStep.status)
    },
    handleSelectWorkDays () {
      const firstWorkDay = this.attrStep.data.baseAttr.work_days[0]
      if (!firstWorkDay) return
      const hasMomentStepKeys = ['per_week', 'per_day', 'confirm']
      this.steps
        .filter(step => hasMomentStepKeys.includes(step.key))
        .forEach(step => {
          step.data.calendarMoment = moment(firstWorkDay)
        })
    },
    handleStepStatus (key, status) {
      const index = this.steps.findIndex(step => step.key === key)
      if (index === -1) return
      this.currentStepIndex = index
      this.steps[index].status = status
      Array.from({ length: index }).forEach((v, i) => {
        this.steps[i].status = 'finish'
      })
    },
    hasFrontNoFinish (step) {
      const stepIndex = this.steps.findIndex(x => x.key === step.key)
      return this.steps.slice(0, stepIndex).some(x => x.status !== 'finish')
    },
    async createPlanInstance () {
      const resp = await this.$api.createPlanInstance()
      if (resp && resp.success) {
        this.planInstanceId = resp.object.plan_instance_id
      }
    },
    async loadPlanInstance (planInstanceId) {
      this.planInstanceLoading = true
      const resp = await this.$api.getPlanInstance(planInstanceId).catch(() => { this.planInstanceLoading = false })
      if (resp && resp.success) {
        if (resp.object.base_attr.dealer && resp.object.base_attr.dealer[0]) {
          this.steps[0].data.baseAttr.dealer = resp.object.base_attr.dealer
        }
        if (resp.object.base_attr.start_date && resp.object.base_attr.end_date) {
          this.steps[0].data.baseAttr.start_date = resp.object.base_attr.start_date
          this.steps[0].data.baseAttr.end_date = resp.object.base_attr.end_date
          this.initDateRange(this.steps[0])
        }
        if (resp.object.base_attr.work_days) {
          this.steps[0].data.baseAttr.work_days = resp.object.base_attr.work_days
        }
        if (resp.object.optimize_attr) {
          this.steps[0].data.optimizeAttr = resp.object.optimize_attr
        }
        this.steps[0].data.fileId = resp.object.file_id
        this.steps[0].data.fileName = resp.object.file_name
        this.handleStepStatus(resp.object.last_step, resp.object.step_status)
        this.$nextTick(() => {
          setTimeout(() => {
            this.planInstanceLoading = false
          }, 100)
        })
      }
    },
    async loadDealers () {
      const resp = await this.$api.getDealerCascade()
      if (resp && resp.success) {
        this.dealerOptions = resp.object
      }
    },
    async loadSaleManList () {
      const resp = await this.$api.getMapChoices(this.planInstanceId, 'area')
      if (resp && resp.success) {
        this.salemanOptions = resp.object.map((val, i) => ({ label: `业务员${i + 1}`, value: val }))
      }
      // this.attrStep.data.optimizeAttr.optimize_strategy = this.salemanOptions.map(option => ({
      //   saleman_name: option.label,
      //   change_type: ''
      // }))
    },
    handleDisabledDate (moment) {
      const date = moment.format('YYYY-MM-DD')
      return !this.attrStep.data.baseAttr.work_days.includes(date)
    },
    handleSelectWeek (moment, step) {
      step.data.selectedWeek = moment.weeks() - this.attrStep.data.selectedDateRange[0].weeks() + 1
      this.loadMarkData(step)
    },
    handleSelectSaleMan (step) {
      this.loadMarkData(step)
    },
    handleSelectPerDayDate (moment, step) {
      step.data.selectedDate = moment.format('YYYY-MM-DD')
      this.loadMarkData(step)
    },
    async restoreMarkData (step) {
      step.data.restoreMarkLoading = true
      step.data.oldMarkData = cloneDeep(step.data.markData)
      step.data.markData = null
      const params = this.generateParams(step)
      const resp = await this.$api.restoreMapStoreList(this.planInstanceId, step.key, params).finally(() => {
        step.data.restoreMarkLoading = false
      })
      if (resp && resp.success) {
        step.data.markData = resp.object
      }
    },
    async saveMarkData (step) {
      const config = this.markAttrsMap[step.key]
      let data
      if (step.data.oldMarkData) {
        data = step.data.markData.filter(x => step.data.oldMarkData.find(old => old.uuid === x.uuid && old[config.typeField] !== x[config.typeField]))
      } else {
        data = step.data.markData.filter(x => x[config.oldTypeField] !== x[config.typeField])
      }
      if (!data.length) {
        this.$message.info('未更改数据，不能保存')
        return
      }
      step.data.saveMarkLoading = true
      await this.$api.saveMapStoreList(this.planInstanceId, step.key, data).finally(() => {
        step.data.saveMarkLoading = false
      })
      this.$refs[step.key].selectedList = []
      step.data.oldMarkData = null
      this.loadMarkData(step)
      // this.loadStatistics(step)
    },
    generateParams (step) {
      const params = {}
      const type = step.key === 'confirm' ? step.data.selectedStep : step.key
      if (['eight_week', 'per_week', 'per_day'].includes(type)) {
        if (!step.data.selectedsalemanName && this.salemanOptions.length) {
          step.data.selectedsalemanName = this.salemanOptions[0].value
        }
        params.saleman = step.data.selectedsalemanName
      }
      if (type === 'per_week') {
        if (step.data.selectedWeek === null) {
          step.data.calendarMoment = this.steps[0].data.selectedDateRange[0]
          step.data.selectedWeek = 1
        }
        params.week_num = step.data.selectedWeek
      }
      if (type === 'per_day') {
        if (step.data.selectedDate === null) {
          step.data.selectedDate = this.attrStep.data.baseAttr.work_days[0]
          if (step.key === 'confirm') {
            step.data.calendarMoment2 = moment(step.data.selectedDate)
          } else {
            step.data.calendarMoment = moment(step.data.selectedDate)
          }
        }
        params.date = step.data.selectedDate
      }
      return params
    },
    async loadMarkData (step) {
      step.data.markData = null
      const type = step.key === 'confirm' ? step.data.selectedStep : step.key
      const params = this.generateParams(step)
      if (step.key === 'confirm' || ['area', 'eight_week', 'per_week', 'per_day'].includes(step.key) && step.status === 'finish') {
        const choiceResp = await this.$api.getMapChoices(this.planInstanceId, type, params)
        if (choiceResp && choiceResp.object) {
          markAttrsMap[type].typeList = choiceResp.object
        }
      }
      this.$loading.show()
      const resp = await this.$api.getMapStoreList(this.planInstanceId, type, params).finally(() => {
        this.$loading.hide()
      })
      if (resp && resp.success) {
        step.data.markData = resp.object
      }
      this.loadStatistics(step)
      // setTimeout(() => {
      //   step.data.markData = markDataMap[type]
      // }, 50)
    },
    getDates (startDate, stopDate) {
      const dateArray = []
      let currentDate = startDate
      while (currentDate <= stopDate) {
        dateArray.push(moment(currentDate))
        currentDate = moment(currentDate).add(1, 'days')
      }
      return dateArray
    },
    getDatesByCount (startDate, count = 40) {
      const dateArray = []
      let currentDate = startDate
      while (dateArray.length < count) {
        const formatDate = currentDate.format('YYYY-MM-DD')
        if ((['1', '2', '3', '4', '5'].includes(currentDate.format('d')) && !lawHolidays.includes(formatDate)) ||
          lawWorkDays.includes(formatDate)) {
            dateArray.push(formatDate)
          }
        currentDate = currentDate.add(1, 'days')
      }
      return dateArray
    },
    getEndDateByCount (startDate, count = 40) {
      const dates = this.getDatesByCount(startDate, count)
      return dates[dates.length - 1]
    },
    initWorkDays (start, end, step) {
      // const dates = this.getDates(start, end)
      // step.data.baseAttr.work_days = this.getDatesByCount(start)
      // dates.forEach(date => {
      //   const formatDate = date.format('YYYY-MM-DD')
      //   if (
      //     (['1', '2', '3', '4', '5'].includes(date.format('d')) && !lawHolidays.includes(formatDate)) ||
      //     lawWorkDays.includes(formatDate)
      //   ) {
      //     step.data.baseAttr.work_days.length < 40 && step.data.baseAttr.work_days.push(formatDate)
      //   }
      // })
      this.$refs.work_days.onFieldChange()
      this.handleSelectWorkDays()
    },
    clearWorkDays (step) {
      step.data.baseAttr.work_days = []
      this.$refs.work_days.onFieldChange()
    },
    initDateRange (step) {
      step.data.selectedDateRange = [moment(step.data.baseAttr.start_date), moment(step.data.baseAttr.end_date)]
    },
    getNextWeek (momentObj, i = 8) {
      const newMomentObj = moment(momentObj.format('YYYY-MM-DD'))
      const weekOfDay = parseInt(newMomentObj.format('E'))
      // const next_monday = momentObj.add((7 - weekOfDay) + 7 * (i - 1) + 1, 'days').format('YYYY-MM-DD')
      const nextSunday = newMomentObj.add((7 - weekOfDay) + 7 * i, 'days').format('YYYY-MM-DD')
      return nextSunday
    },
    handleOpen (open) {
      this.rangeOpen = open
    },
    async calendarChange (dates, step) {
      step.data.selectedDateRange[0] = dates[0]
      // step.data.selectedDateRange[1] = moment(this.getEndDateByCount(dates[0]))
      const resp = await this.$api.getWorkDays(dates[0].format('YYYY-MM-DD'))
      if (resp && resp.success) {
        const workDays = resp.object
        step.data.baseAttr.work_days = workDays
        step.data.selectedDateRange[1] = moment(workDays[workDays.length - 1])
        this.datePickChange(step)
        this.rangeOpen = false
      }
    },
    datePickChange (step) {
      if (step.data.selectedDateRange.length) {
        const [start, end] = step.data.selectedDateRange
        step.data.baseAttr.start_date = start.format('YYYY-MM-DD')
        step.data.baseAttr.end_date = end.format('YYYY-MM-DD')
        this.initWorkDays(start, end, step)
        this.handleAttrChange(step)
      } else {
        this.clearWorkDays(step)
      }
      this.validatePlanDateRange(step)
    },
    getD1Options (dfkey) {
      return {
        dataFacetKey: dfkey,
        pageSize: 10,
        showToolbarButtonList: true,
        asyncExport: true,
        showExportButton: true,
        serialNumber: true,
        openform: true
      }
    },
    async loadStatistics (step) {
      const type = step.key === 'confirm' ? step.data.selectedStep : step.key
      const params = this.generateParams(step)
      step.data.statisticsLoading = true
      // eslint-disable-next-line no-unused-vars
      const resp = await this.$api.getPlanStatistics(this.planInstanceId, type, params).finally(() => {
        step.data.statisticsLoading = false
      })
      if (resp && resp.success) {
        step.data.statisticsData = resp.object
      }
      console.log(`load ${step.key} statistics data`)
    },
    handleMarkDataChange (step) {

    },
    handleAttrChange (step) {
      step.status = 'process'
    },
    validatePlanDateRange (step) {
      let valid
      if (step.data.selectedDateRange.length) {
        this.planDateRangeValidateStatus = 'success'
        this.planDateRangeHelpText = ''
        valid = true
      } else {
        valid = false
        this.planDateRangeValidateStatus = 'error'
        this.planDateRangeHelpText = 'plan date range is required'
        valid = false
      }
      return valid
    },
    async saveAttrs (step, button) {
      let totalValid = true
      this.$refs.baseAttrForm.validate(valid => {
        if (!valid) {
          totalValid = false
        }
      })
      if (!this.validatePlanDateRange(step)) {
        totalValid = false
      }
      if (!step.data.fileId) {
        this.$message.info('请上传数据源')
        totalValid = false
      }
      if (totalValid) {
        button.loading = true
        const data = {
          base_attr: step.data.baseAttr,
          optimize_attr: step.data.optimizeAttr,
          file_id: step.data.fileId,
          file_name: step.data.fileName
        }
        const resp = await this.$api.savePlanAttrs(this.planInstanceId, data).finally(() => {
          button.loading = false
        })
        if (resp && resp.success) {
          step.status = 'finish'
        }
      }
    },
    handleButton (button, step) {
      const btnKey = button.key
      this[btnKey](step, button)
    },
    handlePollingStop (resp) {
      // eslint-disable-next-line camelcase
      return this.stopPolling || ['finish', 'error'].includes(resp?.object?.status)
    },
    handlePollingData (resp, step) {
      if (resp.object.status === 'wait') {
        step.status = 'process'
      } else {
        step.status = resp.object.status
      }
    },
    async queryComputeStatus (step) {
      await polling(this.$api.getComputeStatus, 5000, this.handlePollingStop, (resp) => this.handlePollingData(resp, step))(this.planInstanceId, step.key)
      if (this.hasFrontNoFinish(step)) return
      // step.status = 'finish'
      if (step.status === 'finish') {
        this.loadMarkData(step)
      }
    },
    async startCompute (step) {
      step.status = 'process'
      const resp = await this.$api.startCompute(this.planInstanceId, step.key).catch(() => { step.status = 'error' })
      if (resp.success === true) {
        this.queryComputeStatus(step)
      } else {
        step.status = 'error'
      }
    },
    async exportResult (step, button) {
      console.log(`export ${step.key} Result`)
      button.loading = true
      const resp = await this.$api.exportMapResult(this.planInstanceId, step.key).finally(() => { button.loading = false })
      if (resp.success && resp.object) {
        window.location.href = `${D1Api.download}?file_id=${resp.object}`
      }
    },
    reCompute (step) {
      this.startCompute(step)
    },
    async reConfig (step, button) {
      button.loading = true
      setTimeout(() => {
        step.status = 'process'
        button.loading = false
      }, 1000)
    },
    async confirmPlan (step, button) {
      button.loading = true
      const resp = await this.$api.confirmPlan(this.planInstanceId).finally(() => {
        button.loading = false
      })
      if (resp && resp.success) {
        step.status = 'finish'
      }
      // setTimeout(() => {
      //   step.status = 'finish'
      //   button.loading = false
      // }, 500)
    },
    destroyed () {
      this.stopPolling = true
    }
  }
}
</script>

<style lang="less" scoped>
.title {
  font-weight: 550;
}
/deep/ .ant-fullcalendar-date {
  position: relative;
}
/deep/ .work-day-mark {
  position: absolute;
  top: 0;
  left: 6px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 10px 10px 0 0;
  border-color: #52c41a transparent transparent transparent;
}
/deep/ form .ant-fullcalendar-header .ant-select {
  width: auto;
}
/deep/ .ant-fullcalendar table {
  height: auto;
}
/deep/ .ant-fullcalendar-header {
  height: 32px;
  line-height: 32px;
  padding: 0px 16px 0px 0;
}
/deep/ .ant-fullcalendar-content {
  position: static;
  bottom: auto;
  left: 0;
  width: 100%;
}

.save-btn {
  float: right;
}
.base-attr {
  overflow: hidden;
}
.marks-wrapper {
  position: relative;
  min-height: 80vh;
}
/deep/ .filter-box {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 350px;
  z-index: 1;
  // .ant-select {
  //   width: 100%;
  // }
}
/deep/ .optimize-attr .ant-form-item {
  margin-bottom: 0;
}
.form-side-box {
  float: left;
  width: 50%;
}
/deep/ .select-step-radios .ant-radio-wrapper {
  font-size: 12px;
  &:last-child {
    margin-right: 0;
  }
}
// /deep/ .ant-radio-wrapper {
//   width: 50%;
//   white-space: normal;
//   float: left;
//   margin-right: 0;
// }
/deep/ .ant-calendar-picker {
  width: 100%;
}
.download-btn {
  color: #1890ff;
  margin-left: 10px;
}
</style>
