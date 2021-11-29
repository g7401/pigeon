<template>
  <a-card
    class="data-source-structure"
    :bordered="false"
    size="small"
  >
    <BaseFormModal
      v-model="createModalVisible"
      api-name="createDictValue"
      title="Create Dictionary Value"
      :formItems="modalFormItems"
      :extraData="extraData"
      @saved="afterSave"
    ></BaseFormModal>
    <BaseFormModal
      v-model="editModalVisible"
      api-name="updateDictValue"
      title="Edit Dictionary Value"
      :item="activeItem"
      :formItems="modalFormItems"
      :extraFields="['id']"
      @saved="afterSave"
    ></BaseFormModal>
    <a-row>
      <a-col
        :span="mode == 'manage' ? 12 : 24"
        :gutter="0"
      >
        <a-row v-if="mode === 'manage' || mode == 'select'">
          <a-col :span="mode == 'manage' ? 18 : 24">
            <a-select
              class="mg-b-10"
              style="width: 100%"
              v-model="selectedDictCatUid"
              :options="dictCatOptions"
              placeholder="Pleace select Dictionary Category"
              @change="refreshTree"></a-select>
          </a-col>
          <a-col :span="6" v-if="mode === 'manage'">
            <a-button
              class="pull-right"
              type="primary"
              :disabled="!selectedDictCatUid"
              @click="addRootValue()"
            >Add Content</a-button>
          </a-col>
        </a-row>

        <LazyTreeSelect
          v-if="treeVisible"
          ref="tree"
          :operation="mode === 'manage'"
          :mode="mode"
          :replaceFields="replaceFields"
          :extraData="lazyExtraData"
          :nodeMethod="nodeMethod"
          v-bind="$attrs"
          v-on="$listeners"
        >
          <template #operation="{ node }">
            <div
              class="btn-group"
            >
              <template>
                <a-button
                  class="op-btn"
                  size="small"
                  type="primary"
                  ghost
                  @click="edit(node)"
                >Edit</a-button>
                <a-button
                  v-if="!isLastLevel(node)"
                  class="op-btn"
                  size="small"
                  type="primary"
                  @click="addChild(node)"
                >Add Child Content</a-button>
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
                    v-if="!node.children || !node.children.length"
                  >Delete</a-button>
                </a-popconfirm>
              </template>
            </div>
          </template>
        </LazyTreeSelect>
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
// import cloneDeep from 'lodash.clonedeep'
import BaseFormModal from '@/components/common/BaseFormModal'
import { mapGetters } from 'vuex'
import treeMixin from '@/components/common/mixins/treeMixin'
import SearchVirtualTree from '@/components/common/SearchVirtualTree'
import LazyTreeSelect from '@/components/d1/LazyTreeSelect'
export default {
  name: 'DictValue',
  mixins: [treeMixin],
  components: {
    BaseFormModal,
    SearchVirtualTree,
    LazyTreeSelect
  },
  props: {
    mode: {
      type: String,
      default: 'manage'
    },
    categoryId: {
      type: [Number, String],
      default: undefined
    },
    replaceFields: {
      type: Object,
      default () {
        return { children: 'children', title: 'name', key: 'id', value: 'id' }
      }
    },
    nodeMethod: {
      type: Function,
      default: function (item, parent) {
        item.level = parent ? (parent.level + 1) : 1
      }
    }
  },
  data () {
    return {
      treeVisible: false,
      selectedDictCatUid: undefined,
      dictCatOptions: [],
      rawTree: [],
      createModalVisible: false,
      editModalVisible: false,
      activeItem: null,
      extraData: {
        parentId: undefined,
        categoryId: undefined
      },
      modalFormItems: [
        {
          label: 'Value',
          key: 'name',
          required: true
        },
        {
          label: 'Label',
          key: 'description'
        }
      ],
      deleteLoading: false,
      treeLoading: false
    }
  },
  watch: {
    categoryId: {
      immediate: true,
      async handler (newVal) {
        // await this.loadTreeData()

        if (newVal) {
          this.selectedDictCatUid = newVal
          this.refreshTree()
          // this.$nextTick(() => {
          //   this.$refs.tree.expandSelfByKeys([`category${this.categoryId}`])
          // })
        } else {
          await this.getDictCategories()
          this.$store.dispatch('getDomainAndItemTree')
        }
      }
    }
  },
  computed: {
    lazyExtraData () {
      return {
        dictionary_category_uid: this.selectedDictCatUid
      }
    },
    // treeData () {
    //   if (!this.rawTree.length) return []
    //   const rawData = cloneDeep(this.rawTree)
    //   return this.generateTreeData(rawData)
    // },
    dictStructureMap () {
      const map = {}
      if (!this.domainAndItemTree || !this.domainAndItemTree.length) return map
      this.domainAndItemTree[0].children.forEach(domain => {
        map[domain.id] = domain.children ? this.generateList(domain.children) : []
      })
      return map
    },
    ...mapGetters(['domainAndValueTree', 'domainAndItemTree'])
  },
  methods: {
    refreshTree () {
      this.treeVisible = false
      this.$nextTick(() => {
        this.treeVisible = true
      })
    },
    async getDictCategories () {
      const result = await this.$api.getDictCategories()
      this.dictCatOptions = result.map(x => ({
        label: x.name,
        value: x.uid
      }))
      if (this.dictCatOptions.length && !this.selectedDictCatUid) {
        this.selectedDictCatUid = this.dictCatOptions[0].value
        this.refreshTree()
      }
    },
    async afterSave (item) {
      await this.loadTreeData(false)
      // this.$refs.tree && this.$refs.tree.expandParent(x => x.type === 'content' && x.id === item.id)
    },
    isLastLevel (valueItem) {
      const structures = this.dictStructureMap[this.selectedDictCatUid]
      return structures && valueItem.level === structures.length
    },
    addRootValue () {
      const sturctions = this.dictStructureMap[this.selectedDictCatUid]
      if (!sturctions || !sturctions.length) {
        return this.$message.info('请先给改 Domain 添加 Structure')
      }
      this.extraData.parentId = undefined
      this.extraData.categoryId = this.selectedDictCatUid
      this.createModalVisible = true
    },
    addChild (parent) {
      this.extraData.parentId = parent.id
      this.extraData.categoryId = this.selectedDictCatUid
      this.createModalVisible = true
    },
    edit (item) {
      this.activeItem = item
      this.editModalVisible = true
    },
    async loadTreeData (cache = true) {
      this.$refs.tree.init(cache)
      // this.treeLoading = true
      // if (this.categoryId) {
      //   const result = await this.$api.getDictValue(this.categoryId).catch(() => (this.treeLoading = false))
      //   if (result) {
      //     this.rawTree = [result]
      //   } else {
      //     this.rawTree = []
      //   }
      // } else {
      //   this.$store.dispatch('getDomainAndItemTree')
      //   await this.$store.dispatch('getDomainAndValueTree', cache).catch(() => (this.treeLoading = false))
      //   this.rawTree = this.domainAndValueTree
      // }
      // this.treeLoading = false
    },
    // generateTreeData (items, parent) {
    //   const data = items.slice()
    //   data.forEach(item => {
    //     item.key = item.type + item.id
    //     if (item.type === 'content') {
    //       item.level = (parent.level + 1) || 1
    //       item.categoryId = parent.categoryId || parent.id
    //       item.title = item.description || item.name
    //     } else {
    //       item.title = item.name
    //     }
    //     if (item.children) this.generateTreeData(item.children, item)
    //   })
    //   return data
    // },
    async del (node) {
      this.deleteLoading = true
      // const parentKey = this.$refs.tree.getParentKey(node.key)
      await this.$api.deleteDictValue(node.id).finally(() => (this.deleteLoading = false))
      await this.loadTreeData(false)
      // this.$refs.tree.expandSelfByKeys([parentKey])
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
// /deep/ .btn-group {
//   float: right;
//   display: flex;
//   position: relative;
//   top: 50%;
//   transform: translateY(-50%);
// }
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
/deep/ .ant-checkbox-wrapper {
  margin-right: 5px;
}
/deep/ .tree-loading-icon {
  display: inline-block;
  margin-top: 10px;
  margin-left: 6px;
}
</style>
