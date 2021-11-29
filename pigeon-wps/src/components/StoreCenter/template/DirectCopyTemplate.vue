<template>
  <div>
    <h3 class="title">Input Attribute(s) Coming From Source Attributes</h3>
    <div
      v-for="attr in srcAttrs"
      :key="attr.ds_uid + attr.src_attr_uid"
    >
      {{ attr.ds_name }}, {{ attr.src_attr_name }}
    </div>
    <span v-if="!loading && !srcAttrs.length">N/A</span>
  </div>
</template>

<script>
export default {
  props: {
    importData: {
      type: Object,
      default () {
        return {}
      }
    },
    extraData: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      dataProperties: {},
      srcAttrs: [],
      loading: false
    }
  },
  created () {
    this.loadSrcAttrs()
    if (this.importData && Object.keys(this.importData).length) {
      Object.assign(this.dataProperties, this.importData)
    }
  },
  methods: {
    change () {
      this.$emit('changeData', this.dataProperties)
    },
    async loadSrcAttrs () {
      this.loading = true
      const resp = await this.$api.getEbTaskSrcAttrs(this.extraData.eb_task_def_uid).finally(() => {
        this.loading = false
      })
      if (!resp || !resp.object) return
      this.srcAttrs = resp.object
    }
  }
}
</script>
