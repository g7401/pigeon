import Vue from 'vue'

const virtual = Vue.directive('virtual', {
  inserted: function (el, binding, vnode) {
    if (!binding.value.list.length) return
    const listElement = el.querySelector('.ant-table-body')
    // const listItemElement = el.querySelector('.ant-table-row')
    console.log('element: ', listElement.clientHeight, 25)
    const itemHeight = 25
    const listHeight = binding.value.list.length * itemHeight
    const visibleCount = Math.ceil(el.clientHeight / itemHeight)
    binding.value.virtualList.length = 0
    const visibleData = binding.value.list.slice(0, visibleCount)
    binding.value.virtualList.push(...visibleData)
    listElement.onscroll = e => {
      const scrollTop = e.target.scrollTop
      const start = Math.floor(scrollTop / itemHeight)
      // 此时的结束索引
      const end = start + visibleCount
      // 滚动时列表盒子的偏移量
      const startOffset = scrollTop - (scrollTop % itemHeight)
      listElement.style.transform = `translate3d(0, ${startOffset}px, 0)`
      listElement.style.height = listHeight + 'px'
      binding.value.virtualList.length = 0
      const visibleData = binding.value.list.slice(start, Math.min(end, binding.value.list.length))
      binding.value.virtualList.push(...visibleData)
      console.log('scrollTop', scrollTop)
    }
  }
})

export default virtual
