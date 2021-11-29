<template>
  <a-cascader
    :value="chainValues"
    :fieldNames="fieldNames"
    :options="options"
    v-bind="$attrs"
    v-on="inputListeners"
  ></a-cascader>
</template>

<script>
import treeMixin from '@/components/common/mixins/treeMixin'
export default {
  mixins: [treeMixin],
  props: {
    value: {
      type: [String, Number],
      default: null
    },
    options: {
      type: Array,
      default () {
        return []
      }
    },
    fieldNames: {
      type: Object,
      default () {
        return { children: 'children', label: 'label', value: 'value' }
      }
    }
  },
  data () {
    return {
    }
  },
  computed: {
    treeData () {
      return this.options
    },
    replaceFields () {
      const map = {
        children: this.fieldNames.children
      }
      map.title = this.fieldNames.label
      map.key = this.fieldNames.value
      return map
    },
    chainValues () {
      if (this.value) {
        const chain = this.getChain(this.value)
        return chain.map(x => x[this.fieldNames.value])
      } else {
        return []
      }
    },
    inputListeners: function () {
      var vm = this
      return Object.assign({}, this.$listeners, {
        change: function (event) {
          const lastValue = event && event.length ? event[event.length - 1] : null
          vm.$listeners.input(lastValue)
          vm.$listeners.change(lastValue)
        }
      })
    }
  }
}
</script>

<style>
</style>
