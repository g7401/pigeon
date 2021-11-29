<template>
  <div>
    <h3 class="title">List of Input Tables Defined in the Transform Processor</h3>
    <a-form-model :label-col="{span: 2}" :wrapper-col="{span: 8}" label-align="left" :form="dataProperties" style="margin-bottom: 30px">
      <a-form-model-item
        v-for="(inputTable, index) in dataProperties.inputTables"
        :key="inputTable.name"
        label="Input Table"
        style="margin-bottom:0"
      >
        <a-input
          v-model="inputTable.name"
          :placeholder="index + 1 +'st input table defined in the transform processor'"
          @change="change"
        />
      </a-form-model-item>
    </a-form-model>
    <h3 class="title">List of Output Tables Defined in the Transform Processor</h3>
    <a-tabs type="card">
      <a-tab-pane
        v-for="table in dataProperties.tables"
        type="card"
        :key="table.name"
        :tab="table.name"
      >
        <a-table
          :pagination="false"
          :bordered="true"
          :columns="columns"
          :data-source="table.data"
        >
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
      columns: [
        {
          title: 'No.',
          key: 'serial',
          dataIndex: 'serial'
        },
        {
          title: 'Table Col. Name',
          key: 'table_col_name',
          dataIndex: 'table_col_name'
        },
        {
          title: 'Table Col. Alias',
          key: 'table_col_alias',
          dataIndex: 'table_col_alias'
        },
        {
          title: 'Table Col. Comment',
          key: 'table_col_comment',
          dataIndex: 'table_col_comment'
        },
        {
          title: 'Type',
          key: 'type',
          dataIndex: 'type'
        },
        {
          title: 'Type Attributes',
          key: 'type_attributes',
          dataIndex: 'type_attributes'
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
        inputTables: [
          {
            name: 'inputTable1'
          },
          {
            name: 'inputTable2'
          },
          {
            name: 'inputTable3'
          }
        ],
        tables: [
          {
            name: 'table1',
            data: [
              { serial: 1, query: true, list: false },
              { serial: 2, query: false, list: true }
            ]
          },
          {
            name: 'table2',
            data: []
          },
          {
            name: 'table3',
            data: []
          }
        ]
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
