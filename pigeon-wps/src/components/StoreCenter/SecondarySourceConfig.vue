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
        v-if="secondaryDsIas.length && primaryDsUid"
        type="primary"
        :loading="saveLoading"
        @click="savePrimaySource"
        icon="save"
      >Save</a-button>
    </a-row>
    <h3>Select Identity Attribute(s) for Each Secondary Data Source</h3>
    <div v-if="noSecondarySource">No secondary source</div>
    <a-form-model
      :label-col="{ span: 24 }"
      layout="vertical"
    >

      <a-form-model-item
        v-for="ia in secondaryDsIas"
        :key="ia.secondary_ds_uid"
        :label="ia.secondary_ds_name"
      >
        <a-row
          class="mg-b-5"
          v-for="(unit, index) in ia.mapping_units"
          :gutter="15"
          :key="index"
        >
          <a-col :span="6">
            <a-select
              :options="toOptions(ia.available_attrs_of_secondary_ds)"
              placeholder="Select Identity Attribute"
              v-model="unit.attr_uid_of_secondary_ds"
            ></a-select>
          </a-col>
          <a-col
            :span="4"
            class="center-text"
          >
            mapping to primary source
          </a-col>
          <a-col :span="6">
            <a-select
              :options="toOptions(availableAttrs)"
              placeholder="Select Attribute"
              v-model="unit.mapping_to_attr_uid_of_primary_ds"
            ></a-select>
          </a-col>
          <a-col :span="1">
            <a-button
              icon="minus"
              type="danger"
              ghost
              @click="removeItem(ia.mapping_units, index)"
            ></a-button>
          </a-col>
        </a-row>
        <a-row :gutter="15">
          <a-col :span="6"></a-col>
          <a-col :span="4"></a-col>
          <a-col :span="6"></a-col>
          <a-col :span="1">
            <a-button
              icon="plus"
              type="primary"
              ghost
              @click="addItem(ia.mapping_units)"
            ></a-button>
          </a-col>
        </a-row>
      </a-form-model-item>
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
      availableAttrs: [],
      secondaryDsIas: [],
      saveLoading: false,
      primaryDsUid: '',
      noSecondarySource: false
    }
  },
  created () {
    this.getPrimaySource()
  },
  computed: {},
  methods: {
    addItem (list) {
      list.push({
        attr_uid_of_secondary_ds: undefined,
        mapping_to_attr_uid_of_primary_ds: undefined,
        mapping_to_primary_ds_uid: this.primaryDsUid
      })
    },
    validate () {
      let inValid = true
      this.secondaryDsIas.forEach((ia) => {
        ia.mapping_units.forEach(x => {
          if (!x.attr_uid_of_secondary_ds || !x.mapping_to_attr_uid_of_primary_ds) {
            inValid = false
          }
        })
      })
      if (!inValid) {
        this.$message.info('Operation tips. more info: select is required')
      }
      return inValid
    },
    removeItem (list, index) {
      list.splice(index, 1)
    },
    toOptions (list, label = 'name', value = 'uid') {
      return list.map(x => ({
        label: x[label],
        value: x[value]
      }))
    },
    handleDsChange () {
      this.selectedAttrs = []
    },
    async getPrimaySource () {
      const resp = await this.$api.getEbSecondarySrc(this.epProcessDefUid)
      if (!resp || !resp.object) return
      this.secondaryDsIas = resp.object.secondary_ds_ias
      this.secondaryDsIas.forEach(ia => {
        ia.mapping_units = ia.mapping_units || []
      })
      this.availableAttrs = resp.object.available_attrs_of_primary_ds
      this.primaryDsUid = resp.object.primary_ds_uid
      if (!this.secondaryDsIas || !this.secondaryDsIas.length) {
        this.noSecondarySource = true
      }
    },
    async savePrimaySource () {
      if (!this.validate()) return
      this.saveLoading = true
      const list = this.secondaryDsIas.reduce((acc, ia) => {
        const units = ia.mapping_units.map(x => ({
          identity_attr_uid_of_secondary_ds: x.attr_uid_of_secondary_ds,
          mapping_to_identity_attr_uid_of_primary_ds: x.mapping_to_attr_uid_of_primary_ds,
          mapping_to_primary_ds_uid: x.mapping_to_primary_ds_uid,
          secondary_ds_uid: ia.secondary_ds_uid
        }))
        return acc.concat(units)
      }, [])
      await this.$api.saveEbSecondarySrc(this.epProcessDefUid, list).finally(() => {
        this.saveLoading = false
      })
      this.$emit('saved')
    }
  }
}
</script>

<style lang="less" scoped>
.center-text {
  text-align: center;
  line-height: 32px;
}
</style>
