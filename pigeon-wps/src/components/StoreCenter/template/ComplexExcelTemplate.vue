<template>
  <div class="complex-excel-processor" v-if="dataProperties.tables && dataProperties.tables.length">
    <h3 class="title">Output Table(s)</h3>
    <a-tabs v-model="activeTab" type="card">
      <a-tab-pane
        v-for="(table, index) in dataProperties.tables"
        type="card"
        :key="index"
        :tab="table.tableName"
      >
        <div
          class="table-form"
          :style="{ 'margin-bottom': '10px' }"
        >
          <a-form-model
            :colon="false"
            :model="table"
            style="display: flex"
          >
            <a-form-model-item
              required
              label="Table Name"
              :label-col="{span: 6}"
              :wrapper-col="{span: 6}"
              style="width: 350px"
            >
              <a-input
                @change="nameChange(table)"
                v-model="table.tableName"
              />
            </a-form-model-item>
            <a-form-model-item
              required
              label="Table Alias"
              :label-col="{span: 6}"
              :wrapper-col="{span: 6}"
              style="width: 350px"
            >
              <a-input
                @change="change"
                v-model="table.tableAlias"
              />
            </a-form-model-item>
            <a-form-model-item
              label="Table Comment"
              style="flex: auto;margin-left:20px"
              :label-col="{span: 3}"
              :wrapper-col="{span: 21}"
            >
              <a-input
                @change="change"
                v-model="table.tableComment"
              />
            </a-form-model-item>
          </a-form-model>
        </div>
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
          title: 'Type Name',
          key: 'tableColumnType',
          dataIndex: 'tableColumnType'
        },
        {
          title: 'Type Size',
          key: 'typeSize',
          dataIndex: 'typeSize',
          scopedSlots: { customRender: 'typeSize' },
          width: 150
        },
        {
          title: 'Type Format',
          key: 'typeFormat',
          dataIndex: 'typeFormat',
          scopedSlots: { customRender: 'typeFormat' },
          width: 150
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
    nameChange (table) {
      if (!/^di_oex_.*/.test(table.tableName)) {
        table.tableName = 'di_oex_'
      }
      this.change()
    },
    change () {
      this.$emit('changeData', this.dataProperties)
    }
  }
}
</script>
