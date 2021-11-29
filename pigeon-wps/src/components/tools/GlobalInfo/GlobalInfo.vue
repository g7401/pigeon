<template>
  <div class="global-info" v-if="infos.length">
    <a-alert
      v-for="(info, index) in infos"
      :key="info.message"
      :type="info.type"
      :message="info.message"
      banner
      closable
      show-icon
      :afterClose="() => handleClose(index)"
      :style="{margin: '0 -12px'}"
    />
  </div>
</template>

<script>
import events from '@/components/_util/events'
import { polling } from '@/utils//util'
export default {
  name: 'GlobalInfo',
  data () {
    return {
      infos: [],
      intervalID: null,
      displayTime: 60 * 1000,
      interval: 3 * 60 * 1000,
      maxLength: 3
    }
  },
  created () {
    this.intervalID = setInterval(() => {
      this.shiftInfo()
    }, this.displayTime)
    this.loadInfo = polling(this.loadInfo, this.interval)
    this.loadInfo()
    events.$on('showAlert', info => {
      this.addInfo(info)
    })
  },
  methods: {
    shiftInfo () {
      this.infos.length && this.infos.shift()
    },
    addInfo ({ message, type = 'info' }) {
      this.infos.push({
        message,
        type
      })
      if (this.infos.length > this.maxLength) this.shiftInfo()
    },
    async loadInfo () {
      const result = await this.$api.getSystemMessage()
      this.addInfo(result)
    },
    handleClose (index) {
      this.infos.splice(index, 1)
    }
  },
  beforeDestroy () {
    clearInterval(this.intervalID)
  }
}
</script>

<style>
</style>
