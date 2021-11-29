<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <DsProfileModifyModal
      v-if="modifyVisible && dsProfile && processor"
      v-model="modifyVisible"
      :processorUid="processor.uid"
      :item="dsProfile"
      @saved="handleAfterModify"
    ></DsProfileModifyModal>
    <DsProfileCreateModal
      v-if="createDsProfileVisible && dsProfile && processor"
      v-model="createDsProfileVisible"
      :dsUid="selectedDsUid"
      :processorUid="processor.uid"
      :item="dsProfile"
      @saved="loadDsProfile"
    ></DsProfileCreateModal>
    <a-form
      :label-col="{span: 24}"
      :wrapper-col="{span: 24}"
    >
      <a-form-item label="Data Source">
        <DSTreeSelect v-model="selectedDsUid"></DSTreeSelect>
        <a-button
          class="mg-l-10"
          :disabled="!selectedDsUid"
          @click="loadDsProfile"
          :loading="profileLoading"
        >Load Data Source Connection Profile</a-button>
      </a-form-item>
    </a-form>
    <a-row class="mg-b-10">
      <h3 class="title pull-left" v-if="dsProfile && dsProfile.ds_connection_profile_uid">Connection Profile Name: {{ dsProfile.connection_profile_name }}</h3>
      <div class="op-box pull-right">
        <a-button
          v-if="dsProfile && dsProfile.ds_connection_profile_uid"
          type="primary"
          icon="edit"
          @click="modifyVisible = true"
        >Modify</a-button>
        <a-button
          v-if="dsProfile && !dsProfile.ds_connection_profile_uid && !dsProfile.is_hierarchy"
          type="primary"
          icon="plus-circle"
          class="mg-l-10"
          @click="createDsProfileVisible = true"
        >Create</a-button>
      </div>
    </a-row>
    <a-card
      size="small"
      v-if="dsProfile && dsProfile.ds_connection_profile_uid && processor"
    >
      <Processor
        :disabled="true"
        :pageProps="processor.page_props"
        :dataProps="dsProfile.processor_data_props"
      ></Processor>
    </a-card>
  </a-card>
</template>

<script>
import Processor from '@/components/StoreCenter/Processor'
import DsProfileModifyModal from '@/components/StoreCenter/DsProfileModifyModal'
import DsProfileCreateModal from '@/components/StoreCenter/DsProfileCreateModal'
import DSTreeSelect from '@/components/StoreCenter/DSTreeSelect'

export default {
  name: 'DataSourceConfiguration',
  components: {
    Processor,
    DsProfileModifyModal,
    DsProfileCreateModal,
    DSTreeSelect
  },
  data () {
    return {
      dsTree: [],
      selectedDsUid: undefined,
      dsProfile: null,
      processor: null,
      profileLoading: false,
      modifyVisible: false,
      createDsProfileVisible: false
    }
  },
  created () {},
  methods: {
    async loadDsProfile () {
      this.dsProfile = null
      this.profileLoading = true
      const resp = await this.$api.getDsProfile(this.selectedDsUid).finally(() => {
        this.profileLoading = false
      })
      if (resp && resp.object) {
        this.dsProfile = resp.object
        this.dsProfile.processor_uid && this.loadProcessorDetail(this.dsProfile.processor_uid)
      }
    },
    async loadProcessorDetail (processorUid) {
      this.processor = null
      const resp = await this.$api.getProcessorDetail(processorUid)
      if (resp && resp.object) {
        this.processor = resp.object
      }
    },
    handleAfterModify (formData) {
      this.dsProfile.connection_profile_name = formData.name
      this.dsProfile.processor_data_props = formData.processor_data_props
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .ant-select {
  width: 450px;
  line-height: 32px;
}
.title {
  margin-bottom: 0;
}
// .op-box {
//   position: absolute;
//   right: 0;
//   top: 0;
// }
</style>
