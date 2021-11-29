<template>
  <a-card
    class="data-source-structure"
    :bordered="false"
    size="small"
  >
    <BuildStrategyModal v-if="strategyModalVisiable" v-model="strategyModalVisiable" :extraData="extraData" type="defaultVal"></BuildStrategyModal>
    <BaseFormModal
      v-model="createDomainModalVisible"
      api-name="createDefaultValCategory"
      title="Create Defaults Category"
      :formItems="modalFormItems"
      @saved="afterSaveCategory"
    ></BaseFormModal>
    <BaseFormModal
      v-model="editDomainModalVisible"
      api-name="updateDefaultValCategory"
      title="Edit Defaults Category"
      :formItems="modalFormItems"
      :item="activeDomainItem"
      :extraFields="['id']"
      @saved="afterSaveCategory"
    ></BaseFormModal>
    <template>
      <a-row class="mg-b-10" v-if="mode === 'manage' && !treeLoading">
        <a-button
          @click="createDomain()"
          type="primary"
        >Create Defaults Category</a-button>
      </a-row>
    </template>
    <a-row>
      <a-col :span="mode == 'manage' ? 12 : 24" :gutter="0">
        <SearchTree
          mode="select"
          searchAction="change"
          :tree-data="treeData"
          :replaceFields="replaceFields"
          :loading="treeLoading"
          :selectedKeys="selectedKeys"
          @select="handleSelect"
        >
          <template #after="{ node }">
            <div
              class="btn-group"
              v-if="mode === 'manage' && node.type !== 'ROOT'"
            >
              <template>
                <a-button
                  class="op-btn"
                  size="small"
                  type="primary"
                  ghost
                  @click="editDomain(node)"
                >Edit</a-button>
                <a-popconfirm
                  title="confirm delete?"
                  cancel-text="No"
                  ok-text="Yes"
                  :loading="deleteLoading"
                  @confirm="del(node)"
                >
                  <a-button
                    class="op-btn"
                    size="small"
                    type="danger"
                  >Delete</a-button>
                </a-popconfirm>
                <a-button
                  class="op-btn"
                  size="small"
                  type="primary"
                  @click="showStrategyModal(node)"
                >Manage Build Strategy</a-button>
              </template>
            </div>
          </template>
        </SearchTree>
      </a-col>
    </a-row>

  </a-card>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import BaseFormModal from '@/components/common/BaseFormModal'
import BuildStrategyModal from '@/components/d1Admin/BuildStrategyModal'
import SearchTree from '@/components/common/SearchTree'
import { mapGetters } from 'vuex'
export default {
  name: 'DefaultValCategoryDef',
  components: {
    BaseFormModal,
    BuildStrategyModal,
    SearchTree
  },
  props: {
    mode: {
      type: String,
      default: 'manage'
    },
    categoryId: {
      type: [Number, String],
      default: null
    }
  },
  data () {
    return {
      selectedKeys: [],
      strategyModalVisiable: false,
      extraData: {
        defaults_category_uid: null
      },
      createModalVisible: false,
      editModalVisible: false,
      activeItem: null,
      createDomainModalVisible: false,
      editDomainModalVisible: false,
      activeDomainItem: null,

      parentId: null,
      domainId: null,
      modalFormItems: [
        {
          label: 'Name',
          key: 'name',
          required: true
        },
        {
          label: 'Description',
          key: 'description',
          type: 'textarea',
          rows: 4,
          maxLength: 100
        }
      ],
      deleteLoading: false,
      treeLoading: false,
      replaceFields: { children: 'children', title: 'name', key: 'id' }
    }
  },
  async created () {
    if (this.categoryId) {
      this.selectedKeys = [this.categoryId]
    }
    await this.loadTreeData()
  },
  computed: {
    treeData () {
      return this.generateTreeData(cloneDeep(this.defaultValCategories))
    },
    ...mapGetters(['defaultValCategories'])
  },
  methods: {
    afterSaveCategory () {
      this.$api.removeCache('getDefaultValContents')
      this.loadTreeData()
    },
    showStrategyModal (domain) {
      this.extraData.defaults_category_uid = domain.id
      this.strategyModalVisiable = true
    },
    createDomain () {
      this.createDomainModalVisible = true
    },
    editDomain (item) {
      this.activeDomainItem = item
      this.editDomainModalVisible = true
    },
    async loadTreeData () {
      this.treeLoading = true
      await this.$store.dispatch('getDefaultValCategories', false).finally(() => (this.treeLoading = false))
      this.expandedKeys = [this.treeData[0].id]
    },
    generateTreeData (items, parent) {
      const data = items.slice()
      data.forEach(item => {
        item.selectable = this.mode === 'select' && item.type === 'category'
        if (item.selectable) {
          item.class = 'selectable-node'
        } else {
          item.class = 'disabled-node'
        }
        if (item.children) this.generateTreeData(item.children, item)
      })
      return data
    },
    async del (dataSource) {
      this.deleteLoading = true
      await this.$api.deleteDefaultValCategory(dataSource.id).finally(() => (this.deleteLoading = false))
      this.loadTreeData()
      this.$api.removeCache('getDefaultValContents')
    },
    handleSelect (selectedKeys, { node }) {
      if (!selectedKeys.length) return
      this.selectedKeys = selectedKeys
      this.$emit('select', node.dataRef)
    }
  }
}
</script>

<style lang="less" scoped>
// /deep/ .ant-tree {
//   width: 60%;
// }
/deep/ .op-btn {
  // width: 18px;
  height: 18px;
  margin-left: 5px;
  font-size: 12px;
  line-height: 12px;
}
/deep/ .btn-group {
  float: right;
  display: flex;
  position: relative;
  top: 50%;
  transform: translateY(-50%);
}
/deep/ .top-operation {
  margin: 5px 6px;
  .top-right-btns {
    display: flex;
    justify-content: flex-end;
    align-items: center;
  }
}
/deep/ .only-show-dfk {
  margin: 0 6px;
  font-size: 12px;
}
/deep/ .ant-tree li span.ant-tree-iconEle {
  display: inline-block;
  width: 18px;
  height: 24px;
  margin: 0;
  line-height: 24px;
  text-align: center;
  vertical-align: top;
  border: 0 none;
  outline: none;
  cursor: pointer;
}
/deep/ .ant-tree li .ant-tree-node-content-wrapper {
  padding-left: 0;
}
/deep/ .ant-tree li ul {
  margin: 0;
  padding: 0 0 0 10px;
}
/deep/ .ant-layout-sider-collapsed {
  overflow-x: hidden;
}
.text-ellipsis {
  width: 55%;
  display: inline-block;
  text-overflow: ellipsis;
  overflow: hidden;
}
/deep/ .ant-checkbox-wrapper {
  margin-right: 5px;
}
/deep/ .tree-loading-icon {
  display: inline-block;
  margin-top: 10px;
  margin-left: 6px;
}
/deep/ .selectable-node {
  font-weight: 550;
}
</style>
