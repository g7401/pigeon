<template>
  <a-checkbox v-model="checked" v-bind="$attrs"><slot></slot></a-checkbox>
</template>

<script>
export default {
  props: {
    value: {
      type: [Number, String],
      default: 0
    },
    checkedValue: {
      type: [Number, String],
      default: 1
    },
    uncheckedValue: {
      type: [Number, String],
      default: 0
    }
  },
  data () {
    return {
      checked: false
    }
  },
  created () {
    this.checked = this.value === this.checkedValue
  },
  watch: {
    checked: {
      immediate: true,
      handler () {
        this.$emit('input', this.checked ? this.checkedValue : this.uncheckedValue)
      }
    }
  }
}
</script>
