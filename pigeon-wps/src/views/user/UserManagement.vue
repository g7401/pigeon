<template>
  <a-card
    :bordered="false"
    size="small"
  >
    <BaseFormModal
      v-model="createVisiable"
      api-name="createUser"
      title="Create User"
      :formItems="modalFormItems"
      @saved="afterSaved"
    ></BaseFormModal>
    <BaseFormModal
      v-model="editVisiable"
      api-name="updateUser"
      title="Edit User"
      :item="activeItem"
      :formItems="modalFormItems"
      :extraFields="['uid']"
      @saved="afterSaved"
    ></BaseFormModal>
    <d1-vue-component
      ref="d1Com"
      :options="options"
      @onToolbarButtonClick="handleToolbar"
      @onTableOperationButtonClick="handleOperation"
    >
    </d1-vue-component>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent'
import BaseFormModal from '@/components/common/BaseFormModal'
export default {
  name: 'UserManagement',
  components: {
    d1VueComponent,
    BaseFormModal
  },
  data () {
    return {
      detailLoading: false,
      createVisiable: false,
      editVisiable: false,
      activeItem: null,
      options: {
        infoVisible: false,
        dataFacetKey: 'pvz_user_dfk',
        hideColumns: ['uid'],
        showToolbarButtonList: true,
        showTableOperationButton: true,
        operationWidth: 100,
        toolbarButtonList: [
          {
            label: 'Create User',
            type: 'primary',
            name: 'showCreateModal',
            elColWidth: 3
          }
        ],
        tableOperationButtonList: [
          {
            label: 'Edit',
            type: 'primary',
            name: 'showEditModal'
          }
          // {
          //   label: 'Delete',
          //   type: 'danger',
          //   name: 'del'
          // }
        ]
      }
    }
  },
  computed: {
    modalFormItems () {
      return [
        {
          label: 'Enabled',
          key: 'enabled',
          default: true,
          type: 'checkbox'
        },
        {
          label: 'Username',
          key: 'username',
          required: true
        },
        {
          label: 'Password',
          key: 'password',
          type: 'input-password',
          show: () => this.createVisiable
        },
        {
          label: 'Role',
          key: 'role',
          type: 'select',
          options: [{
            label: 'USER',
            value: 'USER'
          }, {
            label: 'DEVELOPER',
            value: 'DEVELOPER'
          }, {
            label: 'ADMIN',
            value: 'ADMIN'
          }]
        }
      ]
    }
  },
  methods: {
    afterSaved () {
      this.activeItem = null
      this.reload()
    },
    reload () {
      this.$refs.d1Com.runQuery()
    },
    showCreateModal () {
      this.createVisiable = true
    },
    showEditModal (item) {
      this.activeItem = item
      this.editVisiable = true
    },
    async del (item) {
      await this.$api.deleteUser(item.username)
      this.reload()
    },
    handleOperation (item, operationName) {
      this[operationName](item)
    },
    handleToolbar (toolbarName) {
      this[toolbarName]()
    }
  }
}
</script>

<style lang="less" scoped>
</style>
