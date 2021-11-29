<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <a-row>
      <a-button
        class="pull-right"
        type="primary"
        @click="save"
        :loading="saveLoading"
        icon="save"
        :disabled="type ==='view'"
      >Save</a-button>
    </a-row>
    <a-row :gutter="20">
      <a-col :span="14">
        <a-row
          class="row-box"
          v-for="(row, index) in rows"
          :key="index"
          :gutter="15"
        >
          <a-col :span="7">
            <a-select
              :disabled="type ==='view'"
              :options="typeOptions"
              v-model="row.dt_src_type"
              @change="handleTypeChange(row)"
            ></a-select>
          </a-col>
          <a-col
            v-if="row.dt_src_type === 'SDS'"
            :span="7"
          >
            <DSTreeSelect
              :disabled="type ==='view'"
              v-model="row.ds_uid"
              placeholder="Choose Data Source"
              @input="handleDataSourceChange(row)"
            ></DSTreeSelect>
          </a-col>
          <a-col
            v-if="row.dt_src_type === 'SDS'"
            :span="7"
          >
            <a-select
              :disabled="type ==='view'"
              :options="sdsMap[row.ds_uid]"
              placeholder="Choose Structured Data Source"
              v-model="row.src_uid"
            ></a-select>
          </a-col>
          <a-col
            :span="7"
            v-if="row.dt_src_type === 'DT'"
          ></a-col>
          <a-col
            v-if="row.dt_src_type === 'DT'"
            :span="7"
          >
            <DtTreeSelect
              :disabled="type ==='view'"
              v-model="row.src_uid"
              placeholder="Choose Data Target"
            ></DtTreeSelect>
          </a-col>
          <a-col :span="2">
            <a-button
              v-if="type !=='view'"
              type="danger"
              ghost
              icon="minus"
              @click="removeRow(index)"
            ></a-button>
          </a-col>
        </a-row>
        <a-row :gutter="15">
          <a-col :span="7"></a-col>
          <a-col :span="7"></a-col>
          <a-col :span="7"></a-col>
          <a-col :span="2">
            <a-button
              v-if="type !=='view'"
              type="primary"
              ghost
              icon="plus"
              @click="addRow"
            ></a-button>
          </a-col>
        </a-row>
      </a-col>
      <a-col :span="10">
        <a-form-model
          ref="form"
          :model="formData"
        >
          <a-form-model-item
            label="Data Target Name"
            prop="name"
            required
          >
            <a-input
              :disabled="type ==='view'"
              v-model="formData.name"
              placeholder="name"
            />
          </a-form-model-item>

          <a-form-model-item
            label="Data Target Description"
            prop="description"
          >
            <a-textarea :maxLength="200" :autoSize="{ minRows: 3, maxRows: 10 }" :disabled="type ==='view'" v-model="formData.description"></a-textarea>
          </a-form-model-item>
        </a-form-model>
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
import DSTreeSelect from '@/components/StoreCenter/DSTreeSelect'
import DtTreeSelect from '@/components/StoreCenter/DtTreeSelect'
export default {
  name: 'EditDataTarget',
  components: { DSTreeSelect, DtTreeSelect },
  props: {
    dtDefUid: {
      type: String,
      default: ''
    },
    parentUid: {
      type: String,
      default: ''
    },
    type: {
      type: String,
      default: 'create'
    }
  },
  data () {
    return {
      formData: {
        name: '',
        description: ''
      },
      sdsMap: {},
      typeOptions: [
        {
          label: 'Data Source',
          value: 'SDS'
        },
        {
          label: 'Data Target',
          value: 'DT'
        }
      ],
      rows: [],
      dtOptions: [],
      saveLoading: false
    }
  },
  created () {
    if (this.dtDefUid) this.loadDef(this.dtDefUid)
  },
  methods: {
    handleDataSourceChange (row) {
      this.loadSds(row.ds_uid)
      row.src_uid = undefined
    },
    async save () {
      if (!this.formData.name) {
        this.$message.warning('Operation failed. More info: data target name is required')
        return
      }
      for (let i = 0; i < this.rows.length; i++) {
        const row = this.rows[i]
        if ((row.dt_src_type === 'SDS' && !row.ds_uid) || !row.src_uid) {
          this.$message.warning('Operation failed. More info: source option is required')
          return
        }
      }

      this.saveLoading = true
      // eslint-disable-next-line no-unused-vars
      let resp
      if (this.dtDefUid) {
        resp = await this.$api
          .updateDataTarget(this.dtDefUid, this.formData.name, this.formData.description, this.rows)
          .finally(x => {
            this.saveLoading = false
          })
      } else {
        resp = await this.$api
          .createDataTarget(this.parentUid, this.formData.name, this.formData.description, this.rows)
          .finally(x => {
            this.saveLoading = false
          })
      }
      this.$multiTab.remove(this.$route.fullPath)
      this.$router.push({
        name: 'DataTargetDef'
      })
    },
    async loadDef (dtUid) {
      const resp = await this.$api.getDtDefDetail(dtUid)
      if (!resp || !resp.object) return
      this.rows = resp.object.list_of_src || []
      this.formData.name = resp.object.name
      this.formData.description = resp.object.description
      this.rows.forEach(x => {
        x.ds_uid && this.loadSds(x.ds_uid)
      })
    },
    async loadSds (dsUid) {
      if (this.sdsMap[dsUid]) {
        return this.sdsMap[dsUid]
      }
      const resp = await this.$api.getDataSourceSds(dsUid)
      if (resp && resp.object) {
        const options = resp.object.map(x => ({
          label: x.name,
          value: x.sds_uid
        }))
        this.$set(this.sdsMap, dsUid, options)
      }
    },
    addRow () {
      this.rows.push({
        dt_src_type: 'SDS',
        src_uid: undefined,
        ds_uid: undefined
      })
    },
    removeRow (index) {
      this.rows.splice(index)
    },
    handleTypeChange (row) {
      row.ds_uid = undefined
      row.src_uid = undefined
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .ant-select {
  width: 100%;
  margin-right: 5px;
}
/deep/ .row-box {
  margin-bottom: 5px;
}
</style>
