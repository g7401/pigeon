<template>
  <div class="complex-excel-processor" v-if="dataProperties.tables && dataProperties.tables.length">
    <h3 class="title">Definition of Output Table(s)</h3>
    <a-tabs v-model="activeTab" type="card">
      <a-tab-pane
        v-for="(table, index) in dataProperties.tables"
        type="card"
        :key="index"
        :tab="table.tableName"
      >
        <a-table
          :pagination="false"
          :bordered="true"
          :columns="columns"
          :data-source="table.fields"
        >
          <template
            slot="serial"
            slot-scope="text, record, i"
          >
            {{ i + 1 }}
          </template>
          <template
            slot="query"
            slot-scope="text, record"
          >
            <a-checkbox
              v-model="record.query"
              @change="change"
            ></a-checkbox>
          </template>
          <template
            slot="list"
            slot-scope="text, record"
          >
            <a-checkbox
              v-model="record.list"
              @change="change"
            ></a-checkbox>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
export default {
  props: {
    importData: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      activeTab: 0,
      columns: [
        {
          title: 'No.',
          key: 'serial',
          dataIndex: 'serial',
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: 'Table Col. Name',
          key: 'tableColumnName',
          dataIndex: 'tableColumnName'
        },
        {
          title: 'Table Col. Alias',
          key: 'tableColumnAlias',
          dataIndex: 'tableColumnAlias'
        },
        {
          title: 'Table Col. Comment',
          key: 'tableColumnComment',
          dataIndex: 'tableColumnComment'
        },
        {
          title: 'Type',
          key: 'tableColumnType',
          dataIndex: 'tableColumnType'
        },
        {
          title: 'Type Attributes',
          key: 'tableColumnTypeAttributes',
          dataIndex: 'tableColumnTypeAttributes'
        },
        {
          title: 'Query',
          key: 'query',
          dataIndex: 'query',
          scopedSlots: { customRender: 'query' }
        },
        {
          title: 'List',
          key: 'list',
          dataIndex: 'list',
          scopedSlots: { customRender: 'list' }
        }
      ],
      dataProperties: {
        tables: []
      }
    }
  },
  created () {
    console.log('this.importData: ', this.importData)
    if (this.importData && Object.keys(this.importData).length) {
      Object.assign(this.dataProperties, this.importData)
    }
  },
  methods: {
    change () {
      this.$emit('changeData', this.dataProperties)
    }
  }
}
</script>
