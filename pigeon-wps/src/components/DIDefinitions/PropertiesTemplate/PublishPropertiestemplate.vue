<template>
  <div>
    <a-table
      style="margin-left: -10px"
      v-if="tables"
      :pagination="false"
      :bordered="true"
      :columns="columns"
      :data-source="tables"
    >
      <template
        slot="publish"
        slot-scope="text, record"
      >
        <a-checkbox
          v-model="record.publish"
          @change="change"
        ></a-checkbox>
      </template>
    </a-table>
  </div>
</template>

<script>
/* eslint-disable */
export default {
  props: {
    importData: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  data() {
    return {
      columns: [
        {
          title: 'Available Tables To Publish',
          key: 'source_table_name',
          dataIndex: 'source_table_name'
        },
        {
          title: 'Publish',
          key: 'publish',
          dataIndex: 'publish',
          scopedSlots: { customRender: 'publish' }
        }
      ],
      dataProperties: { items: [] },
      oldTables: [],
      tableMap: {},
      tables: []
    }
  },
  created() {
    console.log('this.importData: ', this.importData)
    if (this.importData && Object.keys(this.importData).length) {
      Object.assign(this.dataProperties, this.importData)
    }
    this.handlePublishField()
  },
  methods: {
    change() {
      this.dataProperties.items = this.tables
        .filter(item => item.publish)
        .map(item => ({
          source_table_name: item.source_table_name,
          target_table_name: item.target_data_table.table_name
        }))
      this.dataProperties.tables = this.dataProperties.items
        .map(item => this.tableMap[item.source_table_name])
        .filter(x => x)
      this.$emit('changeData', this.dataProperties)
    },
    async getAailableTables() {
      if (!this.$route.query.definition_id) return {}
      const params = {
        di_process_definition_id: this.$route.query.definition_id
      }
      const resp = await this.$http.get('/kapok-dis/master/definitions/tables_available_to_publish', { params })
      if (!resp || !resp.success || !resp.object) return []

      return resp.object.items
    },
    generateTablesAndMap(tables) {
      const oldTableNames = this.dataProperties.items ? this.dataProperties.items.map(x => x.source_table_name) : []
      const newTableNames = tables.map(table => table.source_table_name)
      tables.forEach(table => {
        table.publish = oldTableNames.includes(table.source_table_name)
      })
      this.dataProperties.items.forEach((item, index) => {
        if (!newTableNames.includes(item.source_table_name)) {
          const oldTable = this.dataProperties.tables ? this.dataProperties.tables[index] : null
          tables.unshift({ source_table_name: item.source_table_name, target_data_table: oldTable, publish: true })
        }
      })
      tables.forEach(table => {
        this.tableMap[table.source_table_name] = table.target_data_table
      })
      this.tables = tables
    },
    async handlePublishField() {
      const tables = await this.getAailableTables()
      this.generateTablesAndMap(tables)
      this.change()
    }
  }
}
</script>
