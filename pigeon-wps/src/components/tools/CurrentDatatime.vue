<template>
  <div class="datetime">
    {{ datetime | moment }}
  </div>
</template>

<script>
export default {
  data () {
    return {
      datetime: new Date(),
      intervalID: null
    }
  },
  created () {
    this.intervalID = setInterval(() => {
      this.datetime = new Date()
    }, 1000)
  },
  beforeDestroy () {
    clearInterval(this.intervalID)
  }
}
</script>

<style scoped>
</style>
