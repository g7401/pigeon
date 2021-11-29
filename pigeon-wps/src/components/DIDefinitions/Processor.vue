<template>
  <div>
    <div class="overview content-box">
      <h3 class="detail-title">· Overview</h3>
      <div class="indent">
        <KeyValueInfo
          v-if="processor"
          key="processor"
          :config="{bordered: true}"
          :options="overviewOptions"
          :obj="processor"
        ></KeyValueInfo>
        <slot name="overview"></slot>
      </div>
    </div>
    <div class="description content-box">
      <h3 class="detail-title">· Description</h3>
      <div class="indent">
        <p v-if="processor">
          {{ processor.description }}
        </p>
        <slot name="description"></slot>
      </div>

    </div>
    <div class="properties content-box">
      <h3 class="detail-title">· Properties</h3>
      <div class="indent">
        <component
          ref="com"
          v-if="processor.page_properties && processor.page_properties !== '{}'"
          :is="comName"
          :importData="dataProperties"
          @changeData="$emit('changeData', $event)"
        ></component>
        <p v-else>no properties</p>
        <slot name="properties"></slot>
      </div>
    </div>
  </div>
</template>

<script>
/* eslint-disable */
import Vue from 'vue'
import KeyValueInfo from '@/components/common/KeyValueInfo'

export default {
  components: { KeyValueInfo },
  props: {
    processor: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  data() {
    return {
      comName: 'properties',
      overviewOptions: [
        { label: 'Name', key: 'name' },
        { label: 'Version', key: 'version' },
        { label: 'Owner', key: 'owner' },
        { label: 'Entry Class Name', key: 'entry_class_name' }
      ]
    }
  },
  computed: {
    dataProperties() {
      if (!this.processor && !this.processor.data_properties) return {}
      if (typeof this.processor.data_properties === 'string') {
        return JSON.parse(this.processor.data_properties)
      }
      return this.processor.data_properties
    }
  },
  watch: {
    'processor.page_properties': {
      immediate: true,
      handler() {
        this.generateComponent(this.processor.page_properties)
      }
    }
  },
  methods: {
    async generateComponent(pageProperties) {
      Vue.component(this.comName, async resolve => {
        // if (!pageProperties) {
        //   const resp = await this.$http('http://10.0.0.11:8000/test.js')
        //   pageProperties = resp
        // }
        const factory = new Function(`return ${pageProperties}`)
        resolve(factory())
      })
    },
    validate () {
      if (!this.$refs.com || !this.$refs.com.validate) return
      return this.$refs.com.validate()
    }
  }
}
</script>

<style scoped>
.content-box {
  margin-bottom: 30px;
}
.indent {
  padding-left: 10px;
}
</style>