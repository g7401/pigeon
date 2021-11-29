<template>
  <a-range-picker v-bind="$attrs" v-on="$listeners" @change="onChange" style="width: 100%"></a-range-picker>
</template>

<script>
export default {
  methods: {
    onChange (dates) {
      if (dates.length !== 2) {
        this.$emit('input', undefined)
      }
      const formatText = this.$attrs.showTime ? 'YYYY-MM-DD h:mm:ss' : 'YYYY-MM-DD'
      const value = [dates[0].format(formatText), dates[1].format(formatText)]
      this.$emit('input', value)
    }
  }
}
</script>

<style>

</style>
