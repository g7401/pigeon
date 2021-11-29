<template>
  <Cascader
    :options="formatedOptions"
    v-bind="$attrs"
    v-on="inputListeners"
  ></Cascader>
</template>

<script>
import { Cascader } from 'ant-design-vue'
export default {
  components: {
    Cascader
  },
  props: {
    options: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      maxLevel: 1
    }
  },
  computed: {
    inputListeners: function () {
      var vm = this
      return Object.assign({}, this.$listeners, {
        change: function (event) {
          vm.$listeners.input(event)
          vm.$listeners.change(event)
        }
      })
    },
    formatedOptions () {
      return this.disabeldOptions(this.options)
    }
  },
  created () {
    this.maxLevel = this.findMaxLevel(this.options)
  },
  methods: {
    findMaxLevel (options, maxLevel = 1) {
      options.forEach(option => {
        if (option.children && option.children.length) {
          maxLevel = this.findMaxLevel(option.children, maxLevel + 1)
        }
      })
      return maxLevel
    },
    disabeldOptions (options, level = 1) {
      options.forEach(option => {
        if (level < this.maxLevel && !option.children) {
          option.disabled = true
        } else if (option.children && option.children.length) {
          option.children = this.disabeldOptions(option.children, level + 1)
        }
      })
      return options
    }
  }
}
</script>

<style>
</style>
