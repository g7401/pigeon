<template>
  <a-card :bordered="false" size="small">
    <DictValueSelectModal v-if="dictValueSelectModalVisible" v-model="dictValueSelectModalVisible" @select="handleSelectDictValue"></DictValueSelectModal>
    <BaseFormModal
      v-model="createModalVisible"
      ref="createModal"
      api-name="createDefaultValContent"
      title="Create Defaults Value"
      :formItems="modalFormItems"
      :extraData="extraData"
      :tips="tips"
      @saved="afterSave"
    ></BaseFormModal>
    <BaseFormModal
      v-model="editModalVisible"
      ref="editModal"
      api-name="updateDefaultValContent"
      title="Edit Defaults Value"
      :item="activeItem"
      :formItems="modalFormItems"
      :extraFields="['id']"
      :tips="tips"
      @saved="afterSave"
    ></BaseFormModal>
    <a-row>
      <a-col :span="mode == 'manage' ? 12 : 24">
        <SearchTree
          ref="tree"
          mode="disabled"
          searchAction="change"
          :tree-data="treeData"
          :replaceFields="replaceFields"
          :loading="treeLoading"
        >
          <template #after="{ node }">
            <div
              v-if="mode === 'manage' && node.type !== 'ROOT'"
              class="btn-group"
            >
              <a-button
                v-if="node.type === 'category'"
                class="op-btn"
                size="small"
                type="primary"
                @click="addRootValue(node)"
              >Add Child</a-button>
              <template v-else>
                <a-button
                  class="op-btn"
                  size="small"
                  type="primary"
                  ghost
                  @click="edit(node)"
                >Edit</a-button>
                <a-popconfirm
                  title="confirm delete?"
                  cancel-text="No"
                  ok-text="Yes"
                  :loading="deleteLoading"
                  @confirm="deleteDefaultValContent(node)"
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
import SearchTree from '@/components/common/SearchTree'
import BaseFormModal from '@/components/common/BaseFormModal'
import DictValueSelectModal from '@/components/d1Admin/DictValueSelectModal'
import { mapGetters } from 'vuex'
export default {
  name: 'DefaultValContentList',
  components: { SearchTree, BaseFormModal, DictValueSelectModal },
  props: {
    mode: {
      type: String,
      default: 'manage'
    },
    categoryId: {
      type: [Number, String],
      default: undefined
    }
  },
  data () {
    return {
      tips: ['Input value or select value from Dictionary Content'],
      dictValueSelectModalVisible: false,
      replaceFields: { title: 'title', key: 'key', children: 'children' },
      valueTree: [],
      modalFormItems: [
        {
          label: 'Value',
          key: 'name',
          required: true,
          type: 'input-search',
          enterButton: 'Select',
          trigger: 'change',
          on: {
            search: (e, formData) => {
              this.dictValueSelectModalVisible = true
              this.handleSelectDictValue = (value, label) => {
                formData['name'] = value
                formData['description'] = label
                this.createModalVisible && this.$refs.createModal && this.$refs.createModal.validate()
                this.editModalVisible && this.$refs.editModal && this.$refs.editModal.validate()
              }
            }
          }
        },
        {
          label: 'Label',
          key: 'description'
        }
      ],
      activeItem: null,
      createModalVisible: false,
      editModalVisible: false,
      extraData: {
        categoryId: undefined
      },
      deleteLoading: false,
      treeLoading: false
    }
  },
  watch: {
    categoryId: {
      immediate: true,
      async handler (newVal) {
        await this.loadTreeData()
        if (newVal) {
          this.$nextTick(() => {
            this.$refs.tree.expandSelfByKeys([`category${this.categoryId}`])
          })
        }
      }
    }
  },
  computed: {
    treeData () {
      if (!this.valueTree.length) return []
      const rawData = cloneDeep(this.valueTree)
      return this.generateTreeData(rawData)
    },
    ...mapGetters(['domainAndValueTree'])
  },
  methods: {
    handleSelectDictValue () {},
    async afterSave (item) {
      await this.loadTreeData(false)
      this.$refs.tree.expandParent(x => x.type === 'content' && x.id === item.id)
    },
    generateTreeData (items, parent) {
      const data = items.slice()
      data.forEach(item => {
        item.key = item.type + item.id
        if (item.type === 'content') {
          item.categoryId = parent.categoryId || parent.id
          item.title = item.description || item.name
        } else {
          item.title = item.name
        }
        if (item.children) this.generateTreeData(item.children, item)
      })
      return data
    },
    async loadTreeData (cache = true) {
      if (!cache) {
        this.$api.removeCache('getDefaultValContents')
      }
      this.treeLoading = true
      const apiKey = this.categoryId ? 'getDefaultValContent' : 'getDefaultValContents'
      const resp = await this.$api[apiKey](this.categoryId).finally(() => {
        this.treeLoading = false
      })
      this.valueTree = [resp]
    },
    addRootValue (domain) {
      this.extraData.categoryId = domain.id
      this.createModalVisible = true
    },
    edit (item) {
      this.activeItem = item
      this.editModalVisible = true
    },
    async deleteDefaultValContent (node) {
      this.deleteLoading = true
      const parentKey = this.$refs.tree.getParentKey(node.key)
      await this.$api.deleteDefaultValContent(node.id).finally(() => (this.deleteLoading = false))
      await this.loadTreeData(false)
      this.$refs.tree.expandSelfByKeys([parentKey])
    }
  }
}
</script>

<style lang="less" scoped>
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
</style>
