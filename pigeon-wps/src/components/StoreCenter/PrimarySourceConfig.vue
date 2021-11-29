<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <a-row
      type="flex"
      justify="end"
    >
      <a-button
        type="primary"
        :loading="saveLoading"
        @click="savePrimaySource"
        icon="save"
      >Save</a-button>
    </a-row>
    <a-form-model
      :label-col="{ span: 24 }"
      layout="vertical"
    >
      <a-row :gutter="24">
        <a-col :span="9">
          <a-form-model-item label="Select Primary Source" required>
            <a-select
              :options="primaryDsOptions"
              v-model="selectedPrimaryDsUid"
              @change="handleDsChange"
            ></a-select>
          </a-form-model-item>
        </a-col>
        <a-col :span="9">
          <a-form-model-item label="Select Identity Attribute(s) for Selected Primary Source" required>
            <a-select
              v-model="selectedAttrs"
              :options="attrsOptions"
              mode="multiple"
            ></a-select>
          </a-form-model-item>
        </a-col>
      </a-row>
    </a-form-model>
  </a-card>
</template>

<script>
export default {
  props: {
    epProcessDefUid: {
      type: String,
      default: undefined
    }
  },
  data () {
    return {
      availPrimaryDsList: [],
      selectedPrimaryDsUid: '',
      selectedAttrs: [],
      saveLoading: false
    }
  },
  created () {
    this.getPrimaySource()
  },
  computed: {
    primaryDsOptions () {
      return this.availPrimaryDsList.map(x => ({
        label: x.name,
        value: x.uid
      }))
    },
    attrsOptions () {
      if (!this.selectedPrimaryDsUid) return []
      const activePrimayDs = this.availPrimaryDsList.find(x => x.uid === this.selectedPrimaryDsUid)
      if (!activePrimayDs) return []
      return activePrimayDs
        .children.map(x => ({
          label: x.name,
          value: x.uid
        }))
    }
  },
  methods: {
    handleDsChange () {
      this.selectedAttrs = []
    },
    async getPrimaySource () {
      const resp = await this.$api.getEbPrimarySrc(this.epProcessDefUid)
      if (!resp || !resp.object) return
      this.availPrimaryDsList = resp.object.avail_primary_ds_and_attrs
      this.selectedPrimaryDsUid = resp.object.primary_ds_uid
      this.selectedAttrs = resp.object.list_of_identity_attr_uid_of_primary_ds || []
    },
    async savePrimaySource () {
      this.saveLoading = true
      await this.$api.saveEbPrimarySrc(this.epProcessDefUid, this.selectedPrimaryDsUid, this.selectedAttrs).finally(() => {
        this.saveLoading = false
      })
      this.$emit('saved')
    }
  }
}
</script>

<style>
</style>
