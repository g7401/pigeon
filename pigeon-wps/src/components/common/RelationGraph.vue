<template>
  <a-card size="small">
    <div
      :id="domId"
      class="graph-box"
    ></div>
  </a-card>
</template>

<script>
import G6 from '@antv/g6'
import cloneDeep from 'lodash.clonedeep'
// eslint-disable-next-line no-unused-vars
import { getStrFullLength } from '@/components/_util/util'
const nodeSettingMap = {
  main: {
    size: [40, 20],
    type: 'rect',
    style: {
      lineWidth: 1,
      stroke: '#1890ff',
      fill: '#73caf7'
    },
    labelCfg: {
      refY: 5,
      style: {
        fontSize: 4,
        fill: '#096dd9'
      }
    }
  },
  other: {
    size: [30, 15],
    style: {
      lineWidth: 1,
      stroke: '#91d5ff',
      fill: '#e6f7ff'
    },
    labelCfg: {
      style: {
        fontSize: 5,
        fill: '#1890ff'
      },
      offest: 10
    }
  },
  finish: {
    size: [40, 20],
    style: {
      fill: '#f6ffed',
      stroke: '#b7eb8f',
      lineWidth: 1
    },
    labelCfg: {
      style: {
        fontSize: 5,
        fill: '#52c41a'
      }
    }
  }
}

const edgeSettingMap = {
  dash: {
    size: 0.5,
    type: 'line',
    color: '#e2e2e2',
    style: {
      lineDash: [5, 1],
      endArrow: true,
      radius: 20,
      fill: '#e2e2e2'
    },
    labelCfg: {
      refY: 5,
      style: {
        lineWidth: 2,
        fontSize: 5
      }
    }
  }
}

export default {
  props: {
    nodes: {
      type: Array,
      default () {
        return []
      }
    },
    edges: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      domId: `relation-graph-${Math.random()
        .toString()
        .slice(2)}`,
      graph: null,
      mainIds: [],
      baseTextWidth: 9
    }
  },
  watch: {
    nodes: {
      handler () {
        this.graph && this.renderGraph()
      },
      deep: true
    },
    edges: {
      handler () {
        this.graph && this.renderGraph()
      },
      deep: true
    }
  },
  created () {},
  mounted () {
    this.renderGraph()
  },
  methods: {
    getG6Nodes () {
      this.mainIds = []
      const nodes = cloneDeep(this.nodes)
      const mainNodes = []
      const otherNodes = []
      nodes.forEach(node => {
        if (node.node_type) {
          Object.assign(node, cloneDeep(nodeSettingMap[node.node_type] || {}))
          const strLength = getStrFullLength(node.label)
          if (strLength > this.baseTextWidth) {
            // node.size[0] = node.size[0] + ((strLength - this.baseTextWidth) * node.labelCfg.style.fontSize)
            node.size[0] = node.size[0] + ((strLength - this.baseTextWidth) * 3)
          }
          if (['main', 'finish'].includes(node.node_type)) {
            mainNodes.push(node)
            this.mainIds.push(node.id)
          } else {
            otherNodes.push(node)
          }
          // if (node.label && node.label.length > 20) {
          //   if (!node.labelCfg) {
          //     node.labelCfg = {}
          //   }
          //   node.labelCfg.position = 'bottom'
          //   node.labelCfg.style = {
          //     fontSize: 4
          //   }
          // }
          delete node.node_type
        }
      })
      return mainNodes.concat(otherNodes)
    },
    getG6Edges () {
      const edges = cloneDeep(this.edges)
      const mainEdges = []
      const otherEdges = []
      edges.forEach(edge => {
        if (edge.edge_type) {
          Object.assign(edge, cloneDeep(edgeSettingMap[edge.edge_type]))
          delete edge.edge_type
        }
        if (this.mainIds.includes(edge.source) && this.mainIds.includes(edge.target)) {
          mainEdges.push(edge)
        } else {
          otherEdges.push(edge)
        }
      })
      return mainEdges.concat(otherEdges)
    },
    renderGraph () {
      this.graph && this.graph.destroy()
      const width = document.getElementById(this.domId).clientWidth || 1200
      const height = document.getElementById(this.domId).clientHeight || 300
      // const minimap = new G6.Minimap()
      const graph = new G6.Graph({
        // plugins: [minimap],
        modes: {
          default: ['drag-canvas', 'zoom-canvas', 'drag-node'] // 允许拖拽画布、放缩画布、拖拽节点
        },
        container: this.domId,
        width,
        height,
        fitView: true,
        fitViewPadding: 80,
        // minZoom: 10,
        // maxZoom: 8,
        renderer: 'canvas',
        layout: {
          type: 'dagre',
          rankdir: 'LR',
          align: 'UL',
          controlPoints: true,
          nodesepFunc: () => 1,
          ranksepFunc: () => 1
        },
        defaultNode: nodeSettingMap['main'],
        defaultEdge: {
          type: 'line',
          size: 1,
          color: '#314659',
          style: {
            endArrow: true,
            radius: 20
          },
          labelCfg: {
            style: {
              lineWidth: 2,
              fontSize: 5
            }
          }
        }
      })
      this.graph = graph
      // 读取数据
      const nodes = this.getG6Nodes()
      const edges = this.getG6Edges()
      const data = {
        nodes,
        edges
      }
      graph.data(cloneDeep(data))
      graph.render()
    }
  },
  beforeDestroy () {
    this.graph && this.graph.destroy()
  }
}
</script>

<style>
</style>
