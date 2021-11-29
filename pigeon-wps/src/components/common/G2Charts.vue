<template>
  <div :id="domId"></div>
</template>

<script>
import { Column, Pie, Line } from '@antv/g2plot'
export default {
  props: {
    height: {
      type: Number,
      default: 600
    },
    data: {
      type: Array,
      default () {
        return []
      }
    },
    type: {
      type: String,
      default: 'interval'
    },
    options: {
      type: Object,
      default: null
    }
  },
  data () {
    return {
      domId: `g2-${Math.random()}`,
      chart: null
    }
  },
  methods: {
    render () {
      if (this.chart) {
        this.chart.destroy()
      }
      const map = {
        'column': Column,
        'line': Line,
        'pie': Pie
      }
      const baseConfig = {
        legend: {
          reversed: true
        }
      }
      const chart = new map[this.type](this.domId, Object.assign(baseConfig, this.chartConfig(this.options)))

      this.chart = chart
      chart.render()
    },
    groupBy (data, field, mergeField) {
      const groupMap = {}
      data.forEach(item => {
        const fieldValue = item[field]
        if (groupMap[fieldValue]) {
          groupMap[fieldValue] = groupMap[fieldValue] + item[mergeField]
        } else {
          groupMap[fieldValue] = item[mergeField]
        }
      })
      return Object.keys(groupMap).map(fieldValue => ({
        [field]: fieldValue,
        [mergeField]: groupMap[fieldValue]
      }))
    },
    chartConfig (options) {
      if (this.type === 'column') {
        return {
          xAxis: {
            label: {
              autoRotate: true
            }
          },
          data: this.data,
          isGroup: !!options.category,
          xField: options.x,
          yField: options.y,
          seriesField: options.category
        }
      } else if (this.type === 'pie') {
        const data = this.groupBy(this.data, options.category, options.y)
        return {
          // appendPadding: 10,
          data,
          angleField: options.y,
          colorField: options.category,
          // radius: 0.9,
          label: {
            type: 'inner',
            offset: '-30%',
            content: ({ percent }) => `${(percent * 100).toFixed(0)}%`,
            style: {
              fontSize: 14,
              textAlign: 'center'
            }
          },
          interactions: [{ type: 'element-active' }]
        }
      } else if (this.type === 'line') {
        const xAxis = {
          tickCount: 20,
          label: {
            autoRotate: true
          }
        }
        let data = this.data
        const firstXAxisvalueIndex = this.data.findIndex(item => item[options.x])
        if (this.data.length && firstXAxisvalueIndex !== -1 && /^\d+(\.\d+)?$/.test(this.data[firstXAxisvalueIndex][options.x])) {
          data = this.data.slice().sort((a, b) => a[options.x] - b[options.x])
        } else if (this.data.length && typeof this.data[firstXAxisvalueIndex][options.x] === 'string') {
          data = this.data.slice().sort((a, b) => a[options.x] && b[options.x] && a[options.x].localeCompare(b[options.x]))
        }
        return {
          data,
          xField: options.x,
          yField: options.y,
          seriesField: options.category,
          xAxis
        }
      }
    }
  }
}
</script>

<style>
</style>
