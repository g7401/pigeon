<template>
  <a-card
    class="data-source-structure"
    :bordered="false"
    size="small"
  >
    <BuildStrategyModal v-if="strategyModalVisiable" v-model="strategyModalVisiable" :extraData="extraData"></BuildStrategyModal>
    <BaseFormModal
      v-model="createDomainModalVisible"
      api-name="createDictDomain"
      title="Create Dictionary Category"
      :formItems="modalFormItems"
      @saved="afterSaveCat"
    ></BaseFormModal>
    <BaseFormModal
      v-model="editDomainModalVisible"
      api-name="updateDictDomain"
      title="Edit Dictionary Category"
      :formItems="modalFormItems"
      :item="activeDomainItem"
      :extraFields="['id']"
      @saved="afterSaveCat"
    ></BaseFormModal>
    <BaseFormModal
      v-model="createModalVisible"
      api-name="createDictItem"
      title="Create Dictionary Structure"
      :formItems="modalFormItems"
      :extraData="{parentId: parentId, categoryId: domainId}"
      @saved="handleSavedStructure"
    ></BaseFormModal>
    <BaseFormModal
      v-model="editModalVisible"
      api-name="updateDictItem"
      title="Edit Dictionary Structure"
      :formItems="modalFormItems"
      :item="activeItem"
      :extraFields="['id']"
      @saved="handleSavedStructure"
    ></BaseFormModal>
    <template>
      <a-row class="mg-b-10" v-if="mode === 'manage' && !treeLoading">
        <a-button
          @click="createDomain()"
          type="primary"
        >Create Dictionary Category</a-button>
      </a-row>
    </template>
    <a-row>
      <a-col
        :span="mode == 'manage' ? 12 : 24"
        :gutter="0"
      >
        <SearchTree
          mode="select"
          ref="tree"
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
              <template v-if="node.type === categoryKey">
                <a-button
                  class="op-btn"
                  size="small"
                  type="primary"
                  ghost
                  @click="editDomain(node)"
                >Edit</a-button>
                <a-button
                  class="op-btn"
                  size="small"
                  type="primary"
                  v-if="!node.children || !node.children.length"
                  @click="addRootItem(node)"
                >Add Structure</a-button>
                <a-button
                  class="op-btn"
                  size="small"
                  type="primary"
                  @click="showStrategyModal(node)"
                >Manage Build Strategy</a-button>
                <a-popconfirm
                  title="confirm delete?"
                  cancel-text="No"
                  ok-text="Yes"
                  :loading="node.loading"
                  @confirm="delCat(node)"
                >
                  <a-button
                    v-if="!node.children || !node.children.length"
                    class="op-btn"
                    size="small"
                    type="danger"
                  >Delete</a-button>
                </a-popconfirm>
              </template>
              <template v-else>
                <a-button
                  class="op-btn"
                  size="small"
                  type="primary"
                  ghost
                  @click="edit(node)"
                >Edit</a-button>
                <a-button
                  v-if="!node.children || node.children.length === 0"
                  class="op-btn"
                  size="small"
                  type="primary"
                  @click="addChild(node)"
                >Add Child Structure</a-button>
                <a-popconfirm
                  title="confirm delete?"
                  cancel-text="No"
                  ok-text="Yes"
                  :loading="node.loading"
                  @confirm="del(node)"
                >
                  <a-button
                    class="op-btn"
                    size="small"
                    type="danger"
                    v-if="!node.children || !node.children.length"
                  >Delete</a-button>
                </a-popconfirm>
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
import { mapGetters } from 'vuex'
import treeMixin from '@/components/common/mixins/treeMixin'
import SearchTree from '@/components/common/SearchTree'
export default {
  name: 'DictDef',
  mixins: [treeMixin],
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
      categoryKey: 'category',
      strategyModalVisiable: false,
      extraData: {
        dictionary_category_uid: null
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
      treeLoading: false,
      replaceFields: { children: 'children', title: 'name', key: 'key' },
      selectedKeys: []
    }
  },
  async created () {
    if (this.categoryId) {
      this.selectedKeys = [`category${this.categoryId}`]
    }
    await this.loadTreeData()
  },
  computed: {
    treeData () {
      return this.generateTreeData(cloneDeep(this.domainAndItemTree))
    },
    ...mapGetters(['domainAndItemTree'])
  },
  methods: {
    afterSaveCat () {
      this.loadTreeData(false)
      this.$api.removeCache('getDomainAndValueTree')
    },
    async handleSavedStructure (item) {
      await this.loadTreeData(false)
      this.$refs.tree.expandParent(x => x.type === 'structure' && x.id === item.id)
    },
    showStrategyModal (cat) {
      if (!cat.children || !cat.children.length) {
        return this.$message.info('Operation failed. More info: Please create Structure for Dictionary Category first')
      }
      this.extraData.dictionary_category_uid = cat.id
      this.strategyModalVisiable = true
    },
    createDomain () {
      this.createDomainModalVisible = true
    },

    addRootItem (domain) {
      this.parentId = undefined
      this.domainId = domain.id
      this.createModalVisible = true
    },
    addChild (parent) {
      this.parentId = parent.id
      this.domainId = parent.categoryId
      this.createModalVisible = true
    },
    editDomain (item) {
      this.activeDomainItem = item
      this.editDomainModalVisible = true
    },
    edit (item) {
      this.activeItem = item
      this.editModalVisible = true
    },
    async loadTreeData (cache = true) {
      this.treeLoading = true
      await this.$store.dispatch('getDomainAndItemTree', cache).finally(() => (this.treeLoading = false))
    },
    generateTreeData (items, parent) {
      const data = items.slice()
      data.forEach(item => {
        item.loading = false
        item.selectable = this.mode === 'select' && item.type === this.categoryKey
        if (item.selectable) {
          item.class = 'selectable-node'
        } else {
          item.class = 'disabled-node'
        }
        item.key = item.type + item.id
        if (item.type === 'structure') {
          item.categoryId = parent.categoryId || parent.id
        }
        if (item.children) this.generateTreeData(item.children, item)
      })
      return data
    },
    async del (structure) {
      structure.loading = true
      const parentKey = this.$refs.tree.getParentKey(structure.key)
      await this.$api.deleteDictItem(structure.id).finally(() => (structure.loading = false))
      await this.loadTreeData(false)
      this.$refs.tree.expandSelfByKeys([parentKey])
    },
    async delCat (cat) {
      cat.loading = true
      const parentKey = this.$refs.tree.getParentKey(cat.key)
      await this.$api.deleteDictDomain(cat.id).finally(() => (cat.loading = false))
      await this.loadTreeData(false)
      this.$refs.tree.expandSelfByKeys([parentKey])
      this.$api.removeCache('getDomainAndValueTree')
    },
    handleSelect (selectedKeys, { node }) {
      if (selectedKeys.length === 0) return
      this.selectedKeys = selectedKeys
      if (node.dataRef.type === this.categoryKey) {
        this.$emit('select', node.dataRef)
      }
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
