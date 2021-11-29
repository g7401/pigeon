import Vue from 'vue'
import VueDraggableResizable from 'vue-draggable-resizable'

Vue.component('vue-draggable-resizable', VueDraggableResizable)

export default {
  data () {
    return {
    }
  },
  created () {},
  methods: {
    getComponents (columns, scroll) {
      const draggingMap = {}
      columns.forEach(col => {
        draggingMap[col.key] = col.width
      })
      const draggingState = Vue.observable(draggingMap)
      const resizeableTitle = (h, props, children) => {
        let thDom = null
        const { key, ...restProps } = props
        const col = columns.find(col => {
          const k = col.dataIndex || col.key
          return k === key
        })
        if (!col.width || key === 'operation') {
          return <th {...restProps}>{children}</th>
        }
        const onDrag = x => {
          draggingState[key] = 0
          if (scroll) {
            scroll.x += x - col.width
          }
          col.width = Math.max(x, 1)
        }

        const onDragstop = () => {
          draggingState[key] = thDom.getBoundingClientRect().width
        }
        return (
          <th {...restProps} v-ant-ref={r => (thDom = r)} width={col.width} class="resize-table-th">
            {children}
            <vue-draggable-resizable
              key={col.key}
              class="table-draggable-handle"
              w={10}
              x={draggingState[key] || col.width}
              z={1}
              axis="x"
              draggable={true}
              resizable={false}
              onDragging={onDrag}
              onDragstop={onDragstop}
            />
          </th>
        )
      }
      return {
        header: {
          cell: resizeableTitle
        }
      }
    }
  }
}
