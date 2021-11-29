<template>
  <a-card
    :bordered="false"
    size="small"
  >
    <a-modal v-if="inviteModalVisible" v-model="inviteModalVisible" :width="350" :footer="null" title="Invite User">
      <a-select
        show-search
        v-model="inviteUsername"
        placeholder="Search username"
        :default-active-first-option="false"
        :show-arrow="false"
        :filter-option="false"
        :not-found-content="null"
        :options="usernamesOptions"
        @search="queryUser"
      >
      </a-select>
      <a-row class="mg-t-10" style="text-align: center">
        <a-button :disabled="!inviteUsername" type="primary" @click="addUser">Select username above</a-button>
      </a-row>
    </a-modal>
    <a-modal
      v-if="accessModalVisible"
      :title="'Manage Access: ' + activeItem.app_name"
      :width="800"
      v-model="accessModalVisible"
      :footer="null"
    >
      <h1 class="title">App has access to which Data Facets</h1>
      <a-row>
        <a-button class="pull-right" type="primary" @click="saveDfAccess" :disabled="saveDfDisabled" :loading="dfAccessSaveLoading">Save</a-button>
      </a-row>
      <a-row>
        <a-col :span="12">
          <a-select
            class="mg-b-10"
            placeholder="Filter By tag"
            mode="multiple"
            :options="tagOptions"
            v-model="selectTags"
            allowClear
          ></a-select>
        </a-col>
      </a-row>
      <a-row>
        <a-col :span="12">
          <a-input
            class="mg-b-10"
            v-model="searchText"
            placeholder="Filter by df uid or df key or df name"
            allowClear
          ></a-input>
        </a-col>
      </a-row>
      <a-row
        class="mg-b-10"
      >
        <a-checkbox v-model="isShowCheckedOnly">Show checked only</a-checkbox>
      </a-row>
      <vxe-grid
        :height="400"
        show-overflow
        show-header-overflow
        class="more-small-table"
        :data="filterDfTableData"
        :loading="dfAccessQueryLoading"
        :columns="dfColumns"
        :show-header="true"
        size="mini"
        :checkbox-config="{labelField: 'checkbox', checkField: 'selected', halfField: 'indeterminate'}"
      >
        <template #tag="{ row }">
          <a-tag v-for="tag in row.tags || []" :key="tag">{{ tag }}</a-tag>
        </template>
      </vxe-grid>
      <h1 class="mg-t-20 title">Who has access to this App</h1>
      <a-row>
        <a-button class="pull-right" type="primary" @click="saveUserAccess" :disabled="saveUserDisabled" :loading="userAccessSaveLoading">Save</a-button>
      </a-row>
      <a-row>
        <a-col :span="12">
          <a-input
            class="mg-b-10"
            v-model="userSearchText"
            placeholder="Filter by username"
          ></a-input>
        </a-col>
      </a-row>
      <a-row
        class="mg-b-10"
      >
        <a-button type="primary" size="small" @click="inviteModalVisible = true">Invite User</a-button>
      </a-row>
      <a-row>
        <a-col :span="12">
          <vxe-grid
            ref="userTable"
            :height="400"
            show-overflow
            show-header-overflow
            class="more-small-table"
            :data="filterUserTableData"
            :loading="userAccessQueryLoading"
            :columns="userColumns"
            :show-header="false"
            size="mini"
          >
            <template #operation="{ row }">
              <a-button type="danger" ghost size="small" @click="removeUser(row)">Remove</a-button>
            </template>
          </vxe-grid>
        </a-col>
      </a-row>
    </a-modal>
    <AppModal
      key="create"
      v-model="createVisiable"
      v-if="createVisiable"
      @saved="afterSaved"
    ></AppModal>
    <AppModal
      key="edit"
      v-model="editVisiable"
      v-if="editVisiable"
      title="Edit App"
      apiName="updateApp"
      :item="activeItem"
      @saved="afterSaved"
    ></AppModal>
    <base-vxe-grid
      ref="appTable"
      max-height="600"
      :formItems="formItems"
      :columns="columns"
      queryApiName="queryApp"
      :operationWidth="250"
      :toolbarButtonList="toolbarButtonList"
      :operationButtonList="operationButtonList"
      @onOperationButtonClick="handleOperation"
      @onToolbarButtonClick="handleToolbar"
      operationButtonClick
    ></base-vxe-grid>
  </a-card>
</template>

<script>
import debounce from 'lodash.debounce'
import BaseVxeGrid from '@/components/common/BaseVxeGrid'
import AppModal from '@/components/App/appModal'

export default {
  name: 'AppManagement',
  components: {
    BaseVxeGrid,
    AppModal
  },
  data () {
    return {
      // modal
      inviteUsername: undefined,
      usernamesOptions: [],
      inviteModalVisible: false,
      userAccessSaveLoading: false,
      userAccessQueryLoading: false,
      userSearchText: '',
      userTableData: [],
      userColumns: [
        {
          title: 'username',
          field: 'username'
        },
        {
          title: 'operation',
          field: 'operation',
          slots: {
            default: 'operation'
          }
        }
      ],
      isShowCheckedOnly: false,
      searchText: '',
      tagOptions: [],
      selectTags: [],
      dfTableData: [],
      selectedDfUids: [],
      dfAccessSaveLoading: false,
      dfAccessQueryLoading: false,
      dfColumns: [
        {
          title: ' ',
          field: 'checkbox',
          type: 'checkbox',
          minWidth: 40,
          align: 'center'
        },
        {
          title: 'Df Uid',
          field: 'df_uid',
          minWidth: 50
        },
        {
          title: 'Df Key',
          field: 'df_key',
          minWidth: 150
        },
        {
          title: 'Df Name',
          field: 'df_name',
          minWidth: 150
        },
        {
          title: 'Create Time',
          field: 'create_timestamp',
          minWidth: 150
        },
        {
          title: 'Tag',
          field: 'tag',
          minWidth: 170,
          slots: {
            default: 'tag'
          }
        }
      ],
      // modal
      accessModalVisible: false,
      detailLoading: false,
      createVisiable: false,
      editVisiable: false,
      activeItem: null,
      formItems: [
        {
          label: 'App Name',
          key: 'app_name'
        }
      ],
      columns: [
        {
          title: 'App Name',
          field: 'app_name',
          minWidth: 200
        },
        {
          title: 'App Key',
          field: 'app_key',
          minWidth: 230
        },
        {
          title: 'App Secret',
          field: 'app_secret',
          minWidth: 230
        },
        {
          title: 'Enabled',
          field: 'enabled',
          minWidth: 80
        },
        {
          title: 'Create Time',
          field: 'create_timestamp',
          minWidth: 150
        },
        {
          title: 'Create By',
          field: 'create_by',
          minWidth: 150
        },
        {
          title: 'Last Update Time',
          field: 'last_update_timestamp',
          minWidth: 150
        },
        {
          title: 'Last Update By',
          field: 'last_update_by',
          minWidth: 150
        }
      ],
      toolbarButtonList: [
        {
          label: 'Create App',
          type: 'primary',
          key: 'showCreateModal'
        }
      ],
      operationButtonList: [
        {
          label: 'Edit',
          type: 'primary',
          key: 'showEditModal'
        },
        {
          label: 'Delete',
          type: 'danger',
          key: 'delApp',
          confirm: true
        },
        {
          label: 'Manage Access',
          type: 'primary',
          ghost: true,
          key: 'showManageAccessModal'
        }
      ]
    }
  },
  computed: {
    filterUserTableData () {
      let items = this.userTableData
      if (this.userSearchText) {
        items = items.filter(x => x.username.indexOf(this.userSearchText) !== -1)
      }
      return items
    },
    filterDfTableData () {
      let items = this.dfTableData
      if (this.selectTags.length) {
        items = items.filter(x => x.tags && x.tags.some(x => this.selectTags.includes(x)))
      }
      if (this.searchText) {
        items = items.filter(x => [x.df_uid, x.df_key, x.df_name].join().indexOf(this.searchText) !== -1)
      }
      if (this.isShowCheckedOnly) {
        items = items.filter(x => x.selected)
      }
      return items
    },
    saveDfDisabled () {
      return !this.dfTableData.some(x => x.selected)
    },
    saveUserDisabled () {
      return !this.userTableData.length
    }
  },
  created () {
    this.queryUser = debounce(this.queryUser, 500)
    this.getDataFacetTags()
  },
  methods: {
    addUser () {
      if (this.userTableData.some(x => x.username === this.inviteUsername)) {
        return this.$message.info('Operation failed. More info: This Username is exist')
      }
      this.inviteModalVisible = false
      this.userTableData.push({
        username: this.inviteUsername
      })
      this.inviteUsername = undefined
    },
    async queryUser (text) {
      if (text) {
        const result = await this.$api.queryUser(text)
        this.usernamesOptions = result.map(x => ({
          label: x,
          value: x
        }))
      } else {
        this.usernamesOptions = []
      }
    },
    removeUser (user) {
      this.userTableData = this.userTableData.filter(x => x.username !== user.username)
    },
    async saveUserAccess () {
      this.userAccessSaveLoading = true
      const usernames = this.userTableData.map(x => x.username)
      this.$api.saveUserAccess(this.activeItem.uid, usernames).finally((x) => {
        this.userAccessSaveLoading = false
      })
    },
    async getUserAccess (uid) {
      this.userTableData = []
      this.userAccessQueryLoading = true
      const usernames = await this.$api.getUserAccess(uid).finally(() => {
        this.userAccessQueryLoading = false
      })
      this.userTableData = usernames.map(x => ({ username: x }))
    },
    async saveDfAccess () {
      this.dfAccessSaveLoading = true
      const selectedDfUids = this.dfTableData.filter(x => x.selected).map(x => x.df_uid)
      this.$api.saveDfAccess(this.activeItem.uid, selectedDfUids).finally((x) => {
        this.dfAccessSaveLoading = false
      })
    },
    async getDfAccess (uid) {
      this.dfTableData = []
      this.dfAccessQueryLoading = true
      this.dfTableData = await this.$api.getDfAccess(uid).finally(() => {
        this.dfAccessQueryLoading = false
      })
    },
    async getDataFacetTags () {
      const result = await this.$api.getAllTags()
      this.tagOptions = result.map(x => ({
        label: x,
        value: x
      }))
    },
    afterSaved () {
      this.$refs.appTable.loadTableData()
    },
    showCreateModal (item) {
      this.createVisiable = true
    },
    showManageAccessModal (item) {
      this.getDfAccess(item.uid)
      this.getUserAccess(item.uid)
      this.activeItem = item
      this.accessModalVisible = true
    },
    async showEditModal (item) {
      this.activeItem = item
      this.editVisiable = true
    },
    async delApp (item) {
      await this.$api.delApp(item.uid)
      this.$refs.appTable.loadTableData()
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
.title {
  text-align: center;
  font-size: 20px;
}
/deep/ .active-row {
  background: #bae7ff;
  cursor: default;
}
/deep/ .selectable-row {
  cursor: pointer;
}
/deep/ .ant-select, /deep/ .ant-input {
  width: 100%;
}
</style>
