<template>
  <a-card
    size="small"
    class=""
  >
    <a-row
      type="flex"
      justify="end"
      class="op-row"
    >
      <a-button
        type="primary"
        :loading="loading"
        @click="submit"
      >Confirm</a-button>
    </a-row>
    <a-transfer
      class="transfer-box"
      :data-source="tables"
      :titles="['List of Tables Published by Data Pipeline', 'List of Subscribed']"
      :listStyle="{width: '40%'}"
      :target-keys="targetKeys"
      :selected-keys="selectedKeys"
      :render="renderFn"
      :disabled="disabled"
      @change="handleChange"
      @selectChange="handleSelectChange"
    />
  </a-card>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
export default {
  name: 'SubscriptionManagement',
  data () {
    return {
      targetKeys: [],
      selectedKeys: [],
      disabled: false,
      tables: [],
      loading: false
    }
  },
  created () {
    this.getTables()
  },
  methods: {
    handleChange (nextTargetKeys, direction, moveKeys) {
      this.targetKeys = nextTargetKeys
      this.tables.forEach(table => {
        if (this.targetKeys.includes(table.table_name)) {
          table.is_choose = true
        } else {
          table.is_choose = false
        }
      })
    },
    handleSelectChange (sourceSelectedKeys, targetSelectedKeys) {
      this.selectedKeys = [...sourceSelectedKeys, ...targetSelectedKeys]
    },
    renderFn (item) {
      if (item.table_comment) {
        return `${item.table_name}, ${item.table_comment}`
      }
      return `${item.table_name}`
    },
    async submit () {
      const tables = cloneDeep(this.tables)
      tables.forEach(table => {
        delete table.key
      })
      this.loading = true
      await this.$api.saveSubscritionTables(tables)
      this.loading = false
    },
    async getTables () {
      const resp = await this.$api.getSubscriptionTables()
      if (!resp || !resp.object) return
      this.tables = resp.object
      this.handleTables(this.tables)
    },
    handleTables (tables) {
      tables.forEach(table => {
        table.key = table.table_name
      })
      this.targetKeys = tables.filter(x => x.is_choose).map(x => x.key)
    }
  }
}
</script>

<style lang="less" scoped>
.transfer-box {
  width: 100%;
}
/deep/ .op-row {
  margin-bottom: 10px;
  .ant-btn {
    margin-left: 5px;
  }
}
</style>
