<template>
  <div>
    <AmapCom @init="handleInit"></AmapCom>
  </div>
</template>

<script>
import AmapCom from './AmapCom'
export default {
  components: {
    AmapCom
  },
  props: {
    data: {
      type: Array,
      default () {
        return [
          {
            lnglat: [116.405285, 39.904989], // 点标记位置
            name: 'beijing',
            id: 1,
            style: 0
          }
        ]
      }
    },
    styleObjects: {
      type: Array,
      default () {
        return []
      }
    },
    watchEvents: {
      type: Array,
      default () {
        return ['click', 'mouseout', 'mouseover']
      }
    },
    showFields: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {}
  },
  created () {},
  methods: {
    computeCenter (lnglatArr) {
      const totalLngLat = lnglatArr.reduce(
        (acc, item) => {
          acc[0] = acc[0] + Number(item[0])
          acc[1] = acc[1] + Number(item[1])
          return acc
        },
        [0, 0]
      )
      return [totalLngLat[0] / lnglatArr.length, totalLngLat[1] / lnglatArr.length]
    },
    handleInit (map) {
      const lnglatArr = this.data.map(x => x.lnglat)
      const center = this.computeCenter(lnglatArr)
      map.setCenter(center)
      const styleObjects = this.styleObjects.map(item => ({
        url: item.url,
        size: new AMap.Size(...item.size),
        anchor: new AMap.Pixel(...item.anchor)
      }))
      const massMarks = new AMap.MassMarks(this.data, {
        zIndex: 5, // 海量点图层叠加的顺序
        zooms: [3, 19], // 在指定地图缩放级别范围内展示海量点图层
        cursor: 'pointer',
        style: styleObjects // 设置样式对象
      })
      this.$watch(
        'data',
        newData => {
          massMarks.setData(newData)
        },
        { deep: true }
      )
      // 将海量点添加至地图实例
      massMarks.setMap(map)
      this.watchEvents.forEach(eventName => {
        massMarks.on(eventName, e => this.$emit(eventName, e))
      })
      this.$once('hook:beforeDestroy', () => {
        this.watchEvents.forEach(eventName => {
          massMarks.off(eventName, e => this.$emit(eventName, e))
        })
        massMarks.clear()
      })
      this.$emit('init', massMarks, map)
      this.initLabel(massMarks, map)
    },
    watchAmapInstance (instance, eventName, fn) {
      instance.on(eventName, fn)
      this.$once('hook:beforeDestroy', () => {
        instance.off(eventName, fn)
      })
    },
    initLabel (massMarks, map) {
      var marker = new AMap.Marker({ content: ' ', map: map })
      this.watchAmapInstance(massMarks, 'mouseover', e => {
        marker.setPosition(e.data.lnglat)
        const labelText = this.showFields.map(field => `${field.label}: ${e.data.originData[field.key]}`).join(', ')
        marker.setLabel({
          content: `<div class="ant-tooltip ant-slider-tooltip ant-tooltip-placement-top" style="transform: translateX(-50%)"><div class="ant-tooltip-content"><div class="ant-tooltip-arrow"></div><div class="ant-tooltip-inner">${labelText}</div></div></div>`,
          offset: new AMap.Pixel(10, -25), // 设置文本标注偏移量
          direction: 'right' // 设置文本标注方位
        })
      })
      this.watchAmapInstance(massMarks, 'mouseout', function () {
        marker.setLabel(null)
      })
    }
  }
}
</script>

<style lang="less">
.amap-marker-label {
  border: 0;
  background-color: transparent;
  .ant-tooltip {
    max-width: fit-content;
  }
}
</style>
<style lang="less" scoped>
.map-container {
  min-height: 600px;
  width: 100%;
}
</style>
