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
        icon="redo"
        @click="reset"
      >Reset</a-button>
      <a-button
        class="mg-l-5"
        type="primary"
        :loading="loading"
        icon="save"
        @click="submit"
      >Save</a-button>
    </a-row>
    <a-transfer
      v-if="loaded"
      class="transfer-box"
      :data-source="tables"
      :titles="['Available Data Sources', 'Selected Data Sources']"
      :listStyle="{width: '40%'}"
      :target-keys="targetKeys"
      :selected-keys="selectedKeys"
      :render="renderFn"
      :disabled="disabled"
      @change="handleChange"
      @selectChange="handleSelectChange"
    />
    <a-icon v-else type="loading" />
  </a-card>
</template>

<script>
export default {
  name: 'StoreSourceTranfer',
  data () {
    return {
      targetKeys: [],
      selectedKeys: [],
      disabled: false,
      tables: [],
      loading: false,
      loaded: false
    }
  },
  created () {
    Promise.all([this.getTables(), this.getInUseDS()]).finally((result) => {
      this.loaded = true
    })
  },
  methods: {
    reset () {
      this.getTables()
      this.getInUseDS()
    },
    handleChange (nextTargetKeys, direction, moveKeys) {
      this.targetKeys = nextTargetKeys
    },
    handleSelectChange (sourceSelectedKeys, targetSelectedKeys) {
      this.selectedKeys = [...sourceSelectedKeys, ...targetSelectedKeys]
    },
    renderFn (item) {
      if (item.description) {
        return `${item.name}, ${item.description}`
      }
      return `${item.name}`
    },
    async submit () {
      const targetList = this.tables.filter(table => {
        return this.targetKeys.includes(table.uid)
      })
      this.loading = true
      await this.$api.saveInUseDS(targetList).finally(x => {
        this.loading = false
      })
    },
    async getTables () {
      const resp = await this.$api.getAvailableDS()
      if (!resp || !resp.object) return
      this.handleTables(resp.object)
      this.tables = resp.object
    },
    async getInUseDS () {
      const resp = await this.$api.getInUseDS()
      if (!resp || !resp.object) return
      this.targetKeys = resp.object.map(x => x.uid) || []
    },
    handleTables (tables) {
      tables.forEach(table => {
        table.key = table.uid
        table.title = table.name
      })
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
/deep/ .ant-transfer-list {
  height: 60vh;
}
</style>
