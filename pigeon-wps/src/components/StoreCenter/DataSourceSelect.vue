<template>
  <div>
    <a-icon
      type="loading"
      v-if="getLoading"
    ></a-icon>
    <template v-else>
      <a-row
        type="flex"
        justify="end"
      >
        <a-button
          type="primary"
          :loading="saveLoading"
          @click="save"
        >Save</a-button>
      </a-row>
      <a-row>
        <a-col :span="10">
          <base-form
            ref="form"
            :key="setting.key"
            :formItems="setting.formItems"
            :item="setting.formItem"
            v-model="setting.formData"
            v-for="setting in formSettings"
          ></base-form>
        </a-col>
      </a-row>
    </template>
  </div>
</template>

<script>
import BaseForm from '@/components/common/BaseForm'
export default {
  components: { BaseForm },
  props: {
    settings: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      defaultLabel: '选择一个来源',
      formSettings: [],
      saveLoading: false,
      getLoading: false
    }
  },
  created () {
    this.init()
  },
  methods: {
    init () {
      this.settings.forEach(x => {
        this.formSettings.push({
          getApi: x.getApi,
          saveApi: x.saveApi,
          type: x.type,
          formItems: [
            {
              label: x.label || this.defaultLabel,
              key: 'ds_uid',
              type: 'select',
              options: [],
              required: true
            }
          ],
          formData: {
            ds_uid: undefined
          },
          formItem: {
            ds_uid: undefined
          }
        })
      })
      this.getLoading = true
      Promise.all(this.formSettings.map(x => this.$api[x.getApi](x.type))).then(resps => {
        resps.forEach((resp, i) => {
          this.formSettings[i].formItems[0].options = resp.object.options.map(x => ({
            label: x.name,
            value: x.uid
          }))
          this.formSettings[i].formItem.ds_uid = resp.object.ds_uid
        })
      }).finally(() => {
        this.getLoading = false
      })
    },
    async save () {
      for (let i = 0; i < this.$refs.form.length; i++) {
        if (this.$refs.form[i].validate() === false) {
          return
        }
      }
      this.saveLoading = true
      Promise.all(this.formSettings.map(x => this.$api[x.saveApi](x.type, x.formData))).finally(result => {
        this.saveLoading = false
      })
    }
  }
}
</script>

<style>
</style>
