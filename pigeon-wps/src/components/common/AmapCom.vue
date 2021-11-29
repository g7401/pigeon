<template>
  <div
    class="map-container"
    :id="domId"
  ></div>
</template>

<script>
import AMapLoader from '@amap/amap-jsapi-loader'
const baseOptions = {
  center: [116.397428, 39.90923],
  zoom: 13,
  resizeEnable: true
}
const baseApiOptions = {
  key: '1daa352f6249bd2860c16474a78dca42',
  version: '1.4.15'
}
export default {
  name: 'AmapCom',
  props: {
    options: {
      type: Object,
      default () {
        return { ...baseOptions }
      }
    },
    apiOptions: {
      type: Object,
      default () {
        return { ...baseApiOptions }
      }
    }
  },
  data () {
    return {
      domId: `amap-${Math.random()}`
    }
  },
  mounted () {
    this.initMap()
  },
  methods: {
    initMap () {
      const options = Object.assign({}, baseOptions, this.options)
      const apiOptions = Object.assign({}, baseApiOptions, this.apiOptions)
      AMapLoader.load(apiOptions)
        .then(AMap => {
          const map = new AMap.Map(this.domId, options)
          this.$emit('init', map)
          this.$once('hook:beforeDestroy', () => {
            map.destroy()
          })
        })
        .catch(e => {
          console.log(e)
        })
    }
  }
}
</script>

<style scoped>
.map-container {
  height: 500px;
  width: 100%;
}
</style>
