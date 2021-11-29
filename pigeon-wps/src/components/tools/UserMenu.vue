<template>
  <div class="user-wrapper">
    <div class="content-box">
      <!-- <a href="https://pro.loacg.com/docs/getting-started" target="_blank">
        <span class="action">
          <a-icon type="question-circle-o"></a-icon>
        </span>
      </a> -->
      <!-- <notice-icon class="action"/> -->
      <a-modal v-if="apiType === 'deployment'" title="Account Profile" v-model="infoModalVisible" :footer="null">
        <KeyValueInfo :options="options" :obj="userInfo"></KeyValueInfo>
      </a-modal>
      <a-modal :width="960" v-model="offlineModalVisible" :footer="null">
        <template #title>离线中心 <span class="header-tip"> <a-icon type="info-circle" /> 只保存7天内的50条离线记录</span></template>
        <OfflineCenter v-if="offlineModalVisible"></OfflineCenter>
      </a-modal>
      <span class="offline-btn action" @click="offlineModalVisible = true">离线中心</span>
      <a-dropdown v-if="apiType === 'deployment'">
        <span class="action ant-dropdown-link user-dropdown-menu">
          <!-- <a-avatar class="avatar" size="small" :src="avatar"/> -->
          <span>{{ nickname }}</span>
        </span>
        <a-menu slot="overlay" class="user-dropdown-menu-wrapper">
          <a-menu-item key="0">
            <a href="javascript:;" @click="infoModalVisible = true">
              <a-icon type="user"/>
              <!-- <span>Account Profile</span> -->
              <span>Account Profile</span>
            </a>
          </a-menu-item>
          <a-menu-divider/>
          <a-menu-item key="1">
            <a href="javascript:;" @click="handleLogout">
              <a-icon type="logout"/>
              <!-- <span>Sign Out</span> -->
              <span>Sign Out</span>
            </a>
          </a-menu-item>
        </a-menu>
      </a-dropdown>
      <div class="no-dropdown-username action" v-if="apiType === 'openapi'">{{ nickname }}</div>
    </div>
  </div>
</template>

<script>
import NoticeIcon from '@/components/NoticeIcon'
import KeyValueInfo from '@/components/common/KeyValueInfo'
import OfflineCenter from '@/views/OfflineCenter'
import { mapActions, mapGetters } from 'vuex'

export default {
  name: 'UserMenu',
  components: {
    NoticeIcon,
    KeyValueInfo,
    OfflineCenter
  },
  data () {
    return {
      infoModalVisible: false,
      offlineModalVisible: false,
      options: [
        {
          label: 'Username',
          key: 'username'
        },
        {
          label: 'Email Address',
          key: 'email_address'
        },
        {
          label: 'Phone',
          key: 'phone'
        },
        // {
        //   label: 'Real Name',
        //   key: 'real_name'
        // },
        {
          label: 'Sign Up At',
          key: 'create_timestamp'
        }
      ]
    }
  },
  computed: {
    ...mapGetters(['nickname', 'avatar', 'userInfo', 'apiType'])
  },
  methods: {
    ...mapActions(['Logout']),
    handleLogout () {
      this.$confirm({
        title: '提示',
        content: '真的要注销登录吗 ?',
        onOk: () => {
          return this.Logout(this.nickname).then(() => {
            setTimeout(() => {
              const loginPage = this.$router.resolve({
                name: 'login'
              })
              window.location.replace(loginPage.href)
            }, 16)
          }).catch(err => {
            this.$message.error({
              title: '错误',
              description: err.message
            })
          })
        },
        onCancel () {
        }
      })
    }
  }
}
</script>

<style lang="less" scoped>
.header-tip {
  font-size: 13px;
  margin-left: 20px;
  color: #606266;
  .anticon {
    color: #1890ff;
  }
}
</style>
