<template>
  <a-layout class="d1-admin-view">
    <a-layout-sider
      class="sider"
      theme="light"
      :collapsible="true"
      v-model="collapsed"
      :collapsedWidth="30"
      breakpoint="lg"
      :style="{ 'overflow-y': collapsed ? 'hidden' : 'auto', 'overflow-x': 'hidden', height: 'auto', maxHeight: 'auto', position: 'relative', left: 0 }"
      :class="{'no-collapse-sider': !collapsed}"
      :width="siderWidth + 'px'"
      :trigger="null"
    >
      <a-row v-show="structureVisible" class="pd-5 top-fix">
        <a-button
          style="width: 100%"
          @click="collapsed = true"
          type="primary"
          ghost
        >Collapse</a-button>
      </a-row>

      <a-icon
        class="expand-button"
        type="double-right"
        v-show="collapsed"
        @click="collapsed = false"
      />
      <DataSourceStructure
        v-show="structureVisible"
        :selectedKey="activeSelectedKey"
        @showFormTableSetting="handleShowSetting"
        @crumbs:update="updateCrumbs"
      ></DataSourceStructure>
      <!-- <vue-draggable-resizable
        class="draggable-handle"
        :w="10"
        :z="1"
        :x="siderWidth - 10"
        axis="x"
        :draggable="true"
        :resizable="false"
        :onDrag="onDrag"
      >
      </vue-draggable-resizable> -->

    </a-layout-sider>
    <!-- <a-layout :style="{ marginLeft: collapsed ? '30px' : (siderWidth + 'px'), height: '100vh', overflow: 'auto' }"> -->
    <!-- <a-layout :style="{ height: '100vh', overflow: 'auto' }"> -->
    <a-layout :style="{ overflow: 'hidden' }">
      <a-layout-content
        class="configuration-area"
        :class="{'is-initial': !activeTab}"
      >
        <a-tabs
          class="data-facet-tabs"
          v-if="activeTab"
          v-model="activeTab"
          hideAdd
          type="editable-card"
          size="small"
          @edit="onEdit"
          @change="handleTabChange"
        >
          <a-tab-pane
            v-for="item in dfItems"
            :key="item.id"
          >
            <template #tab>
              <a-dropdown :trigger="['contextmenu']">
                <span>{{ item.name }}</span>
                <a-menu slot="overlay">
                  <a-menu-item @click="refresh(item.id)">
                    Refresh
                  </a-menu-item>
                </a-menu>
              </a-dropdown>
            </template>
            <FormTableSetting
              :ref="item.id"
              :crumbs="crumbs"
              :uid="item.id"
              v-bind="$attrs"
              @noFindDfk="pureDelete"
            ></FormTableSetting>
          </a-tab-pane>
        </a-tabs>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script>
import VueDraggableResizable from 'vue-draggable-resizable'
import DataSourceStructure from '@/components/d1/DataSourceStructure'
import FormTableSetting from '@/components/d1/FormTableSetting'
import Vue from 'vue'
export default {
  components: { VueDraggableResizable, DataSourceStructure, FormTableSetting },
  data () {
    return {
      activeTab: null,
      dfItems: [],
      crumbs: [],
      collapsed: false,
      siderWidth: 360,
      structureVisible: true
    }
  },
  watch: {
    collapsed (val) {
      if (val) {
        this.structureVisible = false
      } else {
        setTimeout(() => {
          this.structureVisible = true
        }, 250)
      }
    }
  },
  computed: {
    activeSelectedKey () {
      if (!this.activeTab) return
      const activeItem = this.dfItems.find(x => x.id === this.activeTab)
      if (!activeItem) return
      return activeItem.selectedKey
    }
  },
  methods: {
    refresh (key) {
      this.$refs[key] && this.$refs[key][0].loadFormTableSetting()
    },
    handleTabChange () {
      this.$refs[this.activeTab][0].refreshTable()
    },
    onDrag (x) {
      if (x < 320 || x > 500) {
        return false
      }
      this.siderWidth = x + 10
    },
    handleShowSetting ({ crumbs, id, name, selectedKey }) {
      window.scrollTo(0, 0)
      this.crumbs = crumbs
      this.activeTab = id
      const index = this.dfItems.findIndex(x => x.id === id)
      if (index === -1) {
        this.dfItems.push({ id, name, selectedKey })
      } else {
        this.$refs[this.activeTab][0].refreshTable()
      }
    },
    updateCrumbs (crumbs) {
      this.crumbs = crumbs
    },
    pureDelete (id) {
      Vue.ls.remove('CURRENT_DF_ID')
      const index = this.dfItems.findIndex(x => x.id === id)
      this.dfItems.splice(index, 1)
      this.activeTab = null
    },
    deleteDfk (id) {
      const index = this.dfItems.findIndex(x => x.id === id)
      if (index !== -1 && this.dfItems.length > 1) {
        this.dfItems.splice(index, 1)
        this.activeTab = this.dfItems[this.dfItems.length - 1].id
      } else {
        this.$message.info("can't close the last tab")
      }
    },
    onEdit (key, action) {
      if (action === 'remove') {
        this.deleteDfk(key)
      }
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .data-facet-tabs>.ant-tabs-bar {
  margin-bottom: 0;
}
/deep/ .configuration-area.is-initial::after {
  content: 'Data Facet Configuration Area';
  font-size: 30px;
  height: 100vh;
  line-height: 100vh;
  text-align: center;
  display: block;
}
.draggable-handle {
  height: 100% !important;
  bottom: 0;
  cursor: col-resize;
  touch-action: none;
  position: absolute;
}
/deep/ .expand-button {
  height: 100%;
  width: 100%;
  color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
}
/deep/ .top-fix {
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
}
/deep/ .ant-tabs-tab {
  font-size: 13px;
}
/deep/ .no-collapse-sider {
  max-height: calc(100vh - 68px);
}
</style>
