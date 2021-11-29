<template>
  <a-card size="small">
    <div class="marks-selector">
      <MassMarks
        v-if="markData.length"
        :data="markData"
        :styleObjects="styleObjects"
        :showFields="showFields"
        @click="handleMarkClick"
        @init="handleInit"
      ></MassMarks>
      <a-collapse
        class="collapse-box"
        expandIconPosition="right"
        :activeKey="collapseActiveKey"
        @change="handleCollapseChange"
        accordion
      >
        <slot name="collapsePanel"></slot>
        <a-collapse-panel
          v-if="isSelect && tableVisiable"
          key="selected"
          header="调整结果"
        >
          <p>已选中{{ getFieldLabel(nameField) }}：</p>
          <a-table
            class="more-small-table"
            size="small"
            :rowKey="idField"
            :dataSource="tableDataSource"
            :columns="columns"
            :pagination="pagination"
            :border="true"
            @change="handleTableChange"
          >
            <template
              v-for="column in columns"
              :slot="column.key"
              slot-scope="text, record, index"
            >
              <EditCell
                v-if="column.key === typeField"
                :key="column.key + index"
                :options="typeOptions"
                :defaultEdit="true"
                type="Select"
                v-model="record[column.key]"
                @change="handleChange"
              ></EditCell>
              <template v-else>{{ getValueLabel(column.key, record[column.key]) }}</template>
              <!-- <EditCell
                v-else
                :key="column.key"
                v-model="record[column.key]"
              ></EditCell> -->
            </template>
            <template
              slot="operation"
              slot-scope="text, record, index"
            >
              <a-button
                @click="remove(index)"
                type="danger"
                ghost
                size="small"
                icon="delete"
              ></a-button>
            </template>
          </a-table>
          <div class="btn-gap mg-t-10">
            <slot name="tableButtons"></slot>
          </div>
        </a-collapse-panel>
      </a-collapse>
      <div class="op-box btn-gap">
        <a-button
          v-if="isSelect"
          class="square-select-btn"
          size="small"
          @click="$emit('squareSelectBtnClick')"
        >{{ isSquareSelect ? '关闭框选' : '开启框选' }}</a-button>

        <a-button
          v-if="isSelect"
          size="small"
          @click="changeListVisiable = !changeListVisiable"
        >批量更改</a-button>
        <a-button
          v-if="statusList.length"
          size="small"
          @click="returnLastStep"
        >上一步</a-button>
        <a-button
          v-if="showExport"
          size="small"
          @click="exportExcel"
        >导出</a-button>
        <!-- <a-button
          v-if="tableDataSource.length"
          size="small"
          @click="tableVisiable=!tableVisiable"
        >{{ tableVisiable ? '隐藏' : '显示' }}</a-button> -->
      </div>
      <a-card
        class="select-box"
        size="small"
        v-if="changeListVisiable"
      >
        <template slot="title">
          更改 <span style="color: #1890ff">{{ getFieldLabel(typeField) }}</span>:
        </template>
        <a-list
          item-layout="horizontal"
          :data-source="typeList"
          size="small"
          :split="false"
        >
          <a-list-item
            :class="{'active-item': text === selectedType}"
            slot="renderItem"
            slot-scope="text, index"
          >
            <a-list-item-meta @click="selectType(text)">
              <div slot="title">{{ getValueLabel(typeField, text) }}</div>
              <a-avatar
                slot="avatar"
                :src="getTypeIconUrl(index)"
              />
            </a-list-item-meta>
          </a-list-item>
        </a-list>
        <div class="btn-box mg-t-10">
          <a-button
            type="primary"
            :disabled="selectedType == null || !selectedList.length"
            @click="changeType"
          >确认</a-button>
        </div>
      </a-card>
    </div>
  </a-card>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import { exportExcel } from '@/utils/excel'
import MassMarks from './MassMarks'
import EditCell from '@/components/d1/EditCell'

const optionsToMap = (options, keyField = 'key', labelField = 'label') => {
  const map = {}
  options.forEach(option => {
    map[option[keyField]] = option[labelField]
  })
  return map
}

export default {
  components: { MassMarks, EditCell },
  data () {
    return {
      selectedList: [],
      markData: [],
      statusList: [],
      maxCacheStatusLength: 10,
      tableVisiable: true,
      changeListVisiable: false,
      isSquareSelect: false,
      selectedType: null,
      pagination: {
        pageSize: 10,
        current: 1,
        total: 0,
        showTotal: total => `已选 ${total} 个`
      },
      routeOverlays: []
    }
  },
  props: {
    value: {
      type: Array,
      default () {
        return []
      }
    },
    showFields: {
      type: Array,
      default () {
        return []
      }
    },
    idField: {
      type: String,
      default: 'uuid'
    },
    nameField: {
      type: String,
      default: 'store_name'
    },
    typeField: {
      type: String,
      default: ''
    },
    typeList: {
      type: Array,
      default () {
        return []
      }
    },
    lngField: {
      type: String,
      default: 'longitude'
    },
    latField: {
      type: String,
      default: 'latitude'
    },
    routeField: {
      type: String,
      default: ''
    },
    isSelect: {
      type: Boolean,
      default: true
    },
    collapseActiveKey: {
      type: String,
      default: 'selected'
    },
    showExport: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    fieldLabelMap () {
      return optionsToMap(this.showFields)
    },
    typeOptions () {
      return this.typeList.map(x => ({
        label: this.getValueLabel(this.typeField, x),
        value: x
      }))
    },
    styleList () {
      return this.typeList.concat(['selected'])
    },
    styleObjects () {
      const selectedObject = {
        url: `/markers/selected.png`,
        size: [25, 25],
        anchor: [5, 5]
      }
      return this.typeList
        .map((x, i) => ({
          url: this.getTypeIconUrl(i),
          size: [25, 25],
          anchor: [5, 5]
        }))
        .concat([selectedObject])
    },
    columns () {
      return this.showFields
        .map(x => ({
          title: x.label,
          dataIndex: x.key,
          key: x.key,
          scopedSlots: { customRender: x.key },
          editable: true
        }))
        .concat([
          {
            title: '操作',
            dataIndex: 'operation',
            key: 'operation',
            fixed: 'right',
            scopedSlots: { customRender: 'operation' }
          }
        ])
    },
    tableDataSource () {
      return this.selectedList
        .map(item => {
          return item.originData
        })
        .slice(
          (this.pagination.current - 1) * this.pagination.pageSize,
          this.pagination.current * this.pagination.pageSize
        )
    },
    selectedListCompute () {
      return this.selectedList
    }
  },
  created () {
    // this.typeList = this.getTypeList()
    this.init()
  },
  watch: {
    selectedList (newList) {
      this.pagination.total = newList.length
      this.pagination.current = Math.ceil(newList.length / this.pagination.pageSize)
      this.$emit(
        'selected',
        newList.map(x => x.originData)
      )
    }
  },
  methods: {
    getValueLabel (fieldKey, value) {
      const field = this.showFields.find(field => field.key === fieldKey)
      if (!field || !field.valueMap) return value
      return field.valueMap[value] || value
    },
    getFieldLabel (fieldKey) {
      return this.fieldLabelMap[fieldKey] || fieldKey
    },
    handleCollapseChange (event) {
      this.$emit('update:collapseActiveKey', event)
    },
    handleChange () {
      const data = this.getData()
      this.$emit('input', data)
    },
    // getTypeList () {
    //   if (!this.typeField) return [0]
    //   return [
    //     ...new Set(
    //       this.data.map(x => {
    //         return x[this.typeField]
    //       })
    //     )
    //   ]
    // },
    getData () {
      return this.markData.length ? this.markData.map(x => x.originData) : this.value
    },
    initType () {
      this.markData = this.formatData(this.getData())
    },
    init () {
      this.initType()
      this.selectedList = []
    },
    selectType (type) {
      this.selectedType = type
    },
    changeType () {
      this.saveLastStatus()
      this.selectedList.forEach(item => {
        item.originData[this.typeField] = this.selectedType
      })
      this.init()
      this.handleChange()
    },
    getTypeIconUrl (i) {
      return `/markers/mass${i}.png`
    },
    exportExcel () {
      exportExcel(this.getData())
    },
    watchAmapInstance (instance, eventName, fn) {
      instance.on(eventName, fn)
      this.$once('hook:beforeDestroy', () => {
        instance.off(eventName, fn)
      })
    },
    handleTableChange (pagination, filters, sorter) {
      Object.assign(this.pagination, pagination)
    },
    returnLastStep () {
      const { oldSelectedList, oldMarkData } = this.statusList.pop()
      this.selectedList = Object.assign([], oldSelectedList)
      this.markData = Object.assign([], this.markData, oldMarkData)
    },
    toggleSqureSelect (mouseTool) {
      if (!this.isSquareSelect) {
        mouseTool.rectangle({
          fillColor: 'transparent',
          strokeColor: '#1890ff',
          strokeStyle: 'dashed'
          // 同Polygon的Option设置
        })
      } else {
        mouseTool.close(false)
      }
      this.isSquareSelect = !this.isSquareSelect
    },
    handleInit (massMarks, map) {
      const overlays = []
      AMap.plugin(['AMap.MouseTool'], () => {
        const mouseTool = new AMap.MouseTool(map)
        const handleClickFn = () => this.toggleSqureSelect(mouseTool)
        this.$on('squareSelectBtnClick', handleClickFn)
        this.$once('hook:beforeDestroy', () => {
          this.$off('squareSelectBtnClick', handleClickFn)
        })
        this.watchAmapInstance(mouseTool, 'draw', e => {
          map.remove(overlays)
          overlays.push(e.obj)
          const bounds = e.obj.getBounds()
          const allMarkers = massMarks.getData()
          let overlayPath = []
          if (e.obj.CLASS_NAME === 'AMap.Marker') {
            overlayPath.push(e.obj.getPosition())
          } else {
            const southWest = bounds.getSouthWest()
            const northEast = bounds.getNorthEast()
            if (southWest.equals(northEast)) {
              return
            }
            overlayPath = e.obj.getPath()
          }
          const selectItems = []

          allMarkers.forEach(marker => {
            const point = marker.lnglat
            const isPointInRing = AMap.GeometryUtil.isPointInRing(point, overlayPath)
            isPointInRing && selectItems.push(marker)
          })
          selectItems.length && this.addList(selectItems)
        })
      })
      this.routeField &&
        AMap.plugin(['AMap.Walking'], () => {
          this.$watch(
            'value',
            newData => {
              map.remove(this.routeOverlays)
              const lngLatList = this.getRouteLngLatList(newData)
              this.routePlan(map, lngLatList)
            },
            { deep: true, immediate: true }
          )
        })
      this.watchAmapInstance(map, 'mouseup', () => {
        this.changeListVisiable = false
        setTimeout(function () {
          map.remove(overlays)
        })
      })
    },
    getRouteLngLatList (items) {
      return items
        .slice()
        .sort((x, y) => x[this.routeField] - y[this.routeField])
        .map(x => [x[this.lngField], x[this.latField]])
    },
    routePlan (map, lngLatList) {
      const colors = ['#ffeeee', '#B22222', '#FFD700', '#7CFC00']
      for (let i = 0; i < lngLatList.length - 1; i++) {
        const walkOption = {
          map: map,
          hideMarkers: true,
          outlineColor: colors[0],
          autoFitView: true
        }
        const walking = new AMap.Walking(walkOption)
        this.routeOverlays.push(walking)
        walking.search(lngLatList[i], lngLatList[i + 1], (status, result) => {})
      }
    },
    getInitStyle (item) {
      const index = this.typeList.indexOf(item[this.typeField])
      return index !== -1 ? index : 0
    },
    formatData (data) {
      const newMarkData = cloneDeep(data)
      return newMarkData.map(item => ({
        lnglat: [item[this.lngField], item[this.latField]],
        name: item[this.nameField],
        id: item[this.idField],
        originData: item,
        style: this.getInitStyle(item)
      }))
    },
    add (item) {
      this.saveLastStatus()
      this.selectedList.push(item)
      item.style = this.styleList.indexOf('selected')
    },
    addList (items) {
      const waitItems = items.filter(
        item => this.selectedList.findIndex(selectedItem => selectedItem.id === item.id) === -1
      )
      waitItems.length && this.saveLastStatus()
      this.selectedList = this.selectedList.concat(waitItems)
      waitItems.forEach(item => {
        item.style = this.styleList.indexOf('selected')
      })
    },
    remove (index) {
      this.saveLastStatus()
      this.selectedList[index].style = this.getInitStyle(this.selectedList[index].originData)
      this.selectedList.splice(index, 1)
    },
    saveLastStatus () {
      const status = {}
      status.oldMarkData = cloneDeep(this.markData)
      status.oldSelectedList = status.oldMarkData.filter(
        mark => this.selectedList.findIndex(selectedItem => selectedItem.id === mark.id) !== -1
      )
      this.statusList.push(status)
      if (this.statusList.length >= this.maxCacheStatusLength) {
        this.statusList.shift()
      }
    },
    toggle (item) {
      const index = this.selectedList.findIndex(selectedItem => selectedItem.id === item.id)
      if (index === -1) {
        this.add(item)
      } else {
        this.remove(index)
      }
    },
    handleMarkClick (e) {
      if (!this.isSelect) return
      this.toggle(e.data)
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .selected-table-box {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 30%;
}
/deep/ .op-box {
  position: absolute;
  top: 5px;
  left: 5px;
}
.marks-selector {
  position: relative;
}
/deep/ .collapse-box {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 30%;
}
/deep/ .select-box {
  position: absolute;
  bottom: 20px;
  left: 5px;
  width: 200px;
  overflow: auto;
  .ant-list {
    height: 400px;
    overflow: auto;
  }
  .ant-list-sm .ant-list-item {
    padding-top: 4px;
    padding-bottom: 4px;
  }
  .ant-list-item-meta-title {
    line-height: 32px;
    margin-bottom: 0;
    font-size: 12px;
  }
}
/deep/ .ant-list-item {
  cursor: pointer;
}
/deep/ .active-item,
/deep/ .ant-list-item:hover {
  background: #e6f7ff;
}
.btn-box {
  position: sticky;
  bottom: 0;
  background: #fff;
  text-align: center;
}
/deep/ .ant-btn-sm.ant-btn > .anticon {
  line-height: 24px;
}
/deep/ .ant-collapse-icon-position-right > .ant-collapse-item > .ant-collapse-header {
  padding: 5px 10px;
}
/deep/ .ant-collapse-content > .ant-collapse-content-box {
  padding: 10px;
}
/deep/ .ant-card-head {
  background-color: #fafafa;
}
.panel {
  position: fixed;
  background-color: white;
  max-height: 90%;
  overflow-y: auto;
  top: 10px;
  right: 10px;
  width: 280px;
}
.panel .amap-call {
  background-color: #009cf9;
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
}
.panel .amap-lib-driving {
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
  overflow: hidden;
}
</style>
