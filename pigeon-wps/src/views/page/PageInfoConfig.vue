<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <a-tabs v-model="activeTab">
      <a-tab-pane
        tab="Basic"
        key="Basic"
      >
        <a-row class="mg-b-10">
          <a-button
            class="pull-right"
            type="primary"
            :loading="loading"
            :disabled="disabled"
            @click="savePageInfo"
          >Save</a-button>
        </a-row>
        <a-row>
          <a-col
            :xs="24"
            :sm="22"
            :md="20"
            :lg="18"
            :xl="14"
            :xxl="10">
            <BaseForm
              :item="pageInfo"
              v-model="formData"
              :formItems="formItems"
            ></BaseForm>
          </a-col>
        </a-row>
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script>
import BaseForm from '@/components/common/BaseForm'
import { mapGetters } from 'vuex'
export default {
  name: 'PageInfoConfig',
  components: {
    BaseForm
  },
  data () {
    return {
      activeTab: 'Basic',
      loading: false,
      formItems: [
          {
            label: 'Terms of Service Url',
            key: 'terms_of_service_url'
          },
          {
            label: 'Privacy Policy Url',
            key: 'privacy_policy_url'
          },
          {
            label: 'Release Version',
            key: 'release_version'
          },
          {
            label: 'Vendor Name',
            key: 'vendor_name'
          }
      ],
      formData: {}
    }
  },
  computed: {
    disabled () {
      return !this.formData.terms_of_service_url || !this.formData.privacy_policy_url || !this.formData.release_version || !this.formData.vendor_name
    },
    ...mapGetters(['pageInfo'])
  },
  methods: {
    async savePageInfo () {
      this.loading = true
      this.$store.dispatch('savePageInfo', this.formData).finally(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style>
</style>
