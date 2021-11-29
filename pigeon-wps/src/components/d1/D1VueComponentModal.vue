<template>
  <div>
    <a-modal
      :title="config.title"
      :ok-text="config.okText"
      :cancel-text="config.cancelText"
      :visible="value"
      :confirm-loading="config.confirmLoading"
      :width="config.width"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <d1-vue-component
        v-if="value"
        :options="options"
        @handleTableRowClick="handleSelect"
      ></d1-vue-component>
      <div class="detail">
        <slot></slot>
      </div>
    </a-modal>
  </div>
</template>
<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
export default {
  components: { d1VueComponent },
  props: {
    value: {
      type: Boolean,
      default () {
        return false
      }
    },
    modelConfig: {
      type: Object,
      default () {
        return this.baseConfig
      }
    },
    options: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      selectedItem: {},
      baseConfig: {
        title: '',
        okText: 'Confirm',
        cancelText: 'Cancel',
        width: 1000,
        confirmLoading: false,
        handleCancel: () => {},
        handleOk: () => {}
      },
      config: {}
    }
  },
  created () {
    this.config = Object.assign(this.baseConfig, this.modelConfig)
  },
  methods: {
    handleSelect (item) {
      this.selectedItem = item
      console.log('item', item)
      this.$emit('selecting', item)
    },
    handleOk (e) {
      this.$emit('input', false)
      this.$emit('selected', this.selectedItem)
      this.config.handleOk()
    },
    handleCancel (e) {
      this.$emit('input', false)
      this.config.handleCancel()
    }
  }
}
</script>
