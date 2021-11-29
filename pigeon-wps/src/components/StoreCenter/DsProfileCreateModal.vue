<template>
  <div class="data-source-form">
    <DsTypeProfileCreateModal v-if="createDsTypeProfileVisible && item && processorUid" v-model="createDsTypeProfileVisible" :item="item" :processorUid="processorUid" @saved="handleCreate"></DsTypeProfileCreateModal>
    <a-modal
      :title="title"
      :ok-text="config.okText"
      :cancel-text="config.cancelText"
      :visible="value"
      :confirm-loading="config.confirmLoading"
      :width="config.width"
      @ok="handleOk"
      @cancel="close"
    >
      <template>
        <a-form-model
          :model="formData"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
          :colon="false"
          labelAlign="left"
        >

          <a-form-model-item
            label="Connection Profile"
            prop="name"
            :autoLink="false"
          >
            <a-select :options="toOptions(ProfileList)" v-model="formData.connection_profile_uid" @change="handleChange"></a-select> <a-button class="mg-l-10" type="primary" @click="createDsTypeProfileVisible = true">Or Create Connection Profile</a-button>
          </a-form-model-item>
        </a-form-model>
        <a-card size="small" v-if="processorUid">
          <Processor ref="processor2" v-if="processorVisible && processorUid && dataProps" :uid="processorUid" :dataProps="dataProps" :disabled="true"></Processor>
        </a-card>
      </template>
    </a-modal>
  </div>
</template>

<script>
import Processor from '@/components/StoreCenter/Processor'
import DsTypeProfileCreateModal from '@/components/StoreCenter/DsTypeProfileCreateModal'
export default {
  components: { Processor, DsTypeProfileCreateModal },
  props: {
    item: {
      type: Object,
      default () {
        return {}
      }
    },
    processorUid: {
      type: String,
      default: ''
    },
    value: {
      type: Boolean,
      default: false
    },
    dsUid: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      activeDataProps: '{}',
      config: {
        okText: 'Save',
        cancelText: 'Cancel',
        width: 800,
        confirmLoading: false
      },
      labelCol: { span: 5 },
      wrapperCol: { span: 18 },
      formData: {
        connection_profile_uid: '',
        ds_uid: ''
      },
      ProfileList: [],
      dataPropsMap: {},
      createDsTypeProfileVisible: false,
      processorVisible: true
    }
  },
  computed: {
    title () {
      return 'Create Data Source Connection Profile'
    },
    dataProps () {
      const profile = this.ProfileList.find(x => x.uid === this.formData.connection_profile_uid)
      if (!profile) return
      return profile.processor_data_props
    }
  },
  async created () {
    this.loadConnectionProfiles()
  },
  methods: {
    toOptions (list) {
      return list.map(x => ({
                label: x.name,
                value: x.uid
              }))
    },
    handleChange () {
      console.log('change')
      this.processorVisible = false
      setTimeout(() => {
        this.processorVisible = true
      }, 100)
    },
    async loadConnectionProfiles () {
      const resp = await this.$api.getDsTypeProfileList(this.item.ds_type_uid)
      if (resp && resp.object) {
        this.ProfileList = resp.object
        // resp.object.forEach(item => {
        //   this.dataPropsMap[item.uid] = item.processor_data_props
        // })
        if (!this.formData.connection_profile_uid && resp.object.length) {
          this.formData.connection_profile_uid = resp.object[0].uid
        }
      }
    },
    handleCreate (data) {
      this.ProfileList.push(data)
      this.formData.connection_profile_uid = data.uid
    },
    save () {
      return this.$api.createDsProfile(this.dsUid, this.formData.connection_profile_uid)
    },
    async handleOk () {
      // if (!this.formData.name || !/^[0-9a-zA-Z_]{1,}$/.test(this.formData.name)) {
      //   this.$message.warning('data source name must be a combination of letters,underline or numbers')
      //   return
      // }
      this.config.confirmLoading = true
      this.save()
        .then(() => {
          this.$emit('saved')
          this.close()
        })
        .finally(() => {
          this.config.confirmLoading = false
        })
    },
    close () {
      this.$emit('close')
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .ant-select {
  width: 250px;
}
// .op-box {
//   position: absolute;
//   right: 0;
//   top: 0;
// }
</style>
