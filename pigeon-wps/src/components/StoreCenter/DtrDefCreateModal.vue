<template>
  <div class="data-source-form">
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
        >
          <a-form-model-item
            label="Data Target"
            required
          >
            <DtTreeSelect v-model="formData.dtUid" :disabled="!!dtrDefUid"></DtTreeSelect>
          </a-form-model-item>
          <a-form-model-item
            label="Name"
            prop="name"
            required
          >
            <a-input
              v-model="formData.name"
              placeholder="name"
            />
          </a-form-model-item>

          <a-form-model-item
            label="Description"
            prop="description"
          >
            <a-textarea
              :maxLength="200"
              :autoSize="{ minRows: 3, maxRows: 10 }"
              v-model="formData.description"
            ></a-textarea>
          </a-form-model-item>
          <a-form-model-item
            label="Control Flow"
            required
          >
            <a-button
              icon="plus"
              ghost
              type="primary"
              @click="addNode"
            ></a-button>
          </a-form-model-item>
        </a-form-model>

        <draggable
          class="steps-box ant-steps ant-steps-horizontal ant-steps-small ant-steps-label-horizontal ant-steps-navigation"
          v-if="nodes.length"
          v-model="nodes"
        >
          <div
            title="draggable to sort"
            class="ant-steps-item ant-steps-item-process ant-steps-item-active"
            v-for="(item, index) in nodes"
            :key="item.name"
          >
            <div class="ant-steps-item-container">
              <div class="ant-steps-item-tail"></div>
              <div class="ant-steps-item-icon">
                <span class="ant-steps-icon">{{ index + 1 }}</span>
                <a-icon
                  title="delete current processor"
                  type="close-circle"
                  @click="removeNode(index)"
                />
              </div>
              <div class="ant-steps-item-content">
                <div class="ant-steps-item-title">
                  <EditCell v-model="item.name"></EditCell>
                </div>
              </div>
            </div>
          </div>
        </draggable>

      </template>
    </a-modal>
  </div>
</template>

<script>
import draggable from 'vuedraggable'
import EditCell from '@/components/d1/EditCell'
import DtTreeSelect from '@/components/StoreCenter/DtTreeSelect'
export default {
  components: { draggable, EditCell, DtTreeSelect },
  props: {
    dtrDefUid: {
      type: String,
      default: ''
    },
    value: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      config: {
        okText: 'Save',
        cancelText: 'Cancel',
        width: 900,
        confirmLoading: false
      },
      labelCol: { span: 6 },
      wrapperCol: { span: 18 },
      formData: {
        name: '',
        description: '',
        // eslint-disable-next-line no-undef
        dtUid: undefined
      },
      nodes: []
    }
  },
  computed: {
    title () {
      const base = 'DTR Process Def'
      const prefix = this.dtrDefUid ? 'Update ' : 'Create '
      return prefix + base
    }
  },
  async created () {
    if (this.dtrDefUid) {
      this.loadItem(this.dtrDefUid)
    }
  },
  methods: {
    addNode () {
      this.nodes.push({
        name: `Transform Processor ${this.nodes.length + 1}`,
        description: ''
      })
    },
    removeNode (index) {
      this.nodes.splice(index)
    },
    async loadItem (dtrDefUid) {
      const resp = await this.$api.getDtrDefDetail(dtrDefUid)
      if (!resp || !resp.object) return
      this.formData.name = resp.object.name
      this.formData.description = resp.object.description
      this.formData.dtUid = resp.object.dt_uid
      this.nodes = resp.object.list_of_dtr_task_def.map(x => ({
        name: x.name,
        description: x.description,
        uid: x.uid
      }))
    },
    save () {
      if (this.dtrDefUid) {
        return this.$api.updateDtrProcessDef(this.dtrDefUid, this.formData.name, this.formData.description, this.nodes)
      } else {
        return this.$api.createDtrProcessDef(
          this.formData.dtUid,
          this.formData.name,
          this.formData.description,
          this.nodes
        )
      }
    },
    async handleOk () {
      // if (!this.formData.name || !/^[0-9a-zA-Z_]{1,}$/.test(this.formData.name)) {
      //   this.$message.warning('data source name must be a combination of letters,underline or numbers')
      //   return
      // }
      if (!this.dtrDefUid && !this.formData.dtUid) {
        this.$message.warning('Data target is required')
        return
      }
      if (!this.formData.name || this.formData.name.length > 50) {
        this.$message.warning("DTR Process Def name's length should not exceed 50 ")
        return
      }
      if (!this.nodes.length) {
        this.$message.warning('Control Flow is required')
        return
      }
      this.config.confirmLoading = true
      this.save()
        .then(() => {
          // this.$message.success('Operation succeeded')
          this.$emit('saved')
          this.close()
        })
        .finally(err => {
          console.log('err: ', err)
          // this.$message.error('Operation failed')
          this.config.confirmLoading = false
        })
    },
    close () {
      this.$emit('closed')
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="less" scoped>
.ant-steps-navigation .ant-steps-item:before {
  background: transparent;
}
.ant-steps-item {
  cursor: pointer;
  min-width: 250px;
}
/deep/ .anticon-close-circle {
  visibility: hidden;
  color: rgb(245, 34, 45);
  font-size: 20px;
  position: absolute;
  top: 1px;
  left: 1px;
}
.ant-steps-item:hover .anticon-close-circle {
  visibility: visible;
}
.steps-box {
  overflow: auto;
}
.ant-steps-item-title {
  overflow: visible;
}
.ant-steps-item-icon {
  position: relative;
}
</style>
