<template>
  <div class="template-list" v-show="show">
    <div class="template-fliter-bar">
      <a-input
        placeholder="搜索模板"
        v-model="searchText"
        class="btn-gutter"
        style="width:340px"
        @change="filterTemplate"
      ></a-input>
      <a-button icon="search" @click="queryMore">查询</a-button>
    </div>
    <a-list
      item-layout="vertical"
      :pagination="{ pageSize: 4 }"
      :data-source="templateList"
      style="padding-right:140px"
    >
      <a-list-item slot="renderItem" key="item.templateId" slot-scope="item">
        <span slot="extra">
          <a-icon
            v-if="item.chart_type === 'bar'"
            type="bar-chart"
            class="ant-pagination-item-active chart-icon-size"
          />
          <a-icon
            v-if="item.chart_type === 'line'"
            type="line-chart"
            class="ant-pagination-item-active chart-icon-size"
          />
          <a-icon
            v-if="item.chart_type === 'pie'"
            type="pie-chart"
            class="ant-pagination-item-active chart-icon-size"
          />
          <a-icon
            v-if="item.chart_type === 'scatter'"
            type="dot-chart"
            class="ant-pagination-item-active chart-icon-size"
          />
        </span>
        <a-list-item-meta :description="item.data_source_name">
          <a slot="title" @click="loadTemplate(item.id)">{{ item.name }}</a>
        </a-list-item-meta>
      </a-list-item>
    </a-list>
  </div>
</template>

<script>
import { get } from '../api/api'
export default {
  name: 'TemplateList',
  data () {
    return {
      templateList: [],
      allTemplateList: [],
      searchText: ''
    }
  },
  props: {
    show: {
      type: Boolean,
      required: true,
      default: false
    }
  },
  methods: {
    filterTemplate (e) {
      if (!e.target.value) {
        this.templateList = [...this.allTemplateList]
      } else {
        this.templateList = [
          ...this.allTemplateList.filter(
            list => list.name.indexOf(e.target.value) >= 0 || list.data_source_name.indexOf(e.target.value) >= 0
          )
        ]
      }
    },
    async queryMore () {
      if (!this.searchText) return
      const response = await get('query/templates/' + this.searchText)
      if (response.success) {
        this.$message.success('查询成功')
        this.allTemplateList = [...this.allTemplateList, ...response.obj]
        this.templateList = [...response.obj]
      }
    },
    async loadTemplate (id) {
      const response = await get('query/template/' + id)
      if (response.success) {
        this.$emit('loadTemplate', response.obj)
      }
    },
    addTemplate (template) {
      this.templateList.unshift(template)
      this.allTemplateList.unshift(template)
    }
  },
  async mounted () {
    const response = await get('query/templates/')
    if (response.success) {
      this.templateList = [...response.obj]
      this.allTemplateList = [...response.obj]
    }
  }
}
</script>
