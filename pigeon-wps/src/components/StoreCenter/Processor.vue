<template>
  <div>
    <component
      ref="com"
      v-if="totalPageProps && totalPageProps !== '{}'"
      :is="comName"
      :importData="dataProperties"
      :disabled="disabled"
      v-bind="$attrs"
      @changeData="$emit('changeData', $event)"
    ></component>
    <span v-else>N/A</span>
  </div>
</template>

<script>
/* eslint-disable */
import Vue from 'vue'

export default {
  props: {
    uid: {
      type: String,
      default: ''
    },
    pageProps: {
      type: String,
      default() {
        return ''
      }
    },
    dataProps: {
      type: [String, Object],
      default() {
        return '{}'
      }
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      comName: 'storeCenterProcessor',
      loadedPageProps: ''
    }
  },
  computed: {
    dataProperties() {
      if ((!this.totalPageProps || this.totalPageProps == '{}') && !this.dataProps) return {}
      if (typeof this.dataProps === 'string') {
        return JSON.parse(this.dataProps)
      }
      return this.dataProps
    },
    totalPageProps () {
      return this.uid ? this.loadedPageProps : this.pageProps
    }
  },
  watch: {
    totalPageProps: {
      immediate: true,
      handler(value) {
        this.generateComponent(value)
      }
    },
    'uid': {
      immediate: true,
      handler () {
        this.uid && this.loadProcessorDetail(this.uid)
      }
    }
  },
  methods: {
    async loadProcessorDetail(processorUid) {
      const resp = await this.$api.getProcessorDetail(processorUid)
      if (resp && resp.object) {
        this.loadedPageProps = resp.object.page_props
      }
    },
    async generateComponent(pageProperties) {
      Vue.component(this.comName, async resolve => {
        const factory = new Function(`return ${pageProperties}`)
        resolve(factory())
      })
    },
    validate() {
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