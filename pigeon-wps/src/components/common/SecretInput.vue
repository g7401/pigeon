<template>
  <a-row :gutter="10">
    <a-col :span="18">
      <a-input :value="$attrs.value" v-bind="$attrs" v-on="inputListeners"></a-input>
    </a-col>
    <a-col :span="6">
      <a-button @click="generate" :disabled="$attrs.disabled">Generate</a-button>
    </a-col>
  </a-row>
</template>

<script>
import md5 from 'md5'
export default {
  computed: {
    inputListeners: function () {
      var vm = this
      return Object.assign({}, this.$listeners, {
        input: function (event) {
          vm.$emit('input', event.target.value)
        }
      })
    }
  },
  methods: {
    generate () {
      const value = md5(Math.random()).toLowerCase()
      this.$emit('input', value)
    }
  }
}
</script>

<style lang="less" scoped>
.ant-btn {
  width: 100%
}
</style>
