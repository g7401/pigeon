<template>
  <div :style="{position: 'relative', display: 'flow-root'}">
    <div
      ref="list"
      class="infinite-list-container"
      :style="{marginTop: headerHeight + 'px', height: listHeight + 'px', overflow: isScroll ? 'auto' : 'hidden' }"
      @scroll="scrollEvent($event)"
    >
      <div
        class="infinite-list-phantom"
        :style="{ height: totalListHeight + 'px' }"
      ></div>
    </div>
    <div
      class="infinite-list"
      :style="{ transform: getTransform, right: isScroll ? '15px': 0}"
    >
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    list: {
      type: Array,
      default: () => []
    },
    itemHeight: {
      type: Number,
      default: 24.8
    },
    headerHeight: {
      type: Number,
      default: 25.6
    },
    footerHeight: {
      type: Number,
      default: 40.5
    }
  },
  data () {
    return {
      screenHeight: 0,
      // 偏移量
      startOffset: 0,
      // 起始索引
      start: 0
    }
  },
  computed: {
    totalListHeight () {
      return this.list.length * this.itemHeight + 5
    },
    listHeight () {
      return this.visibleCount * this.itemHeight
    },
    isScroll () {
      return this.visibleCount < this.list.length
    },
    // 可显示的列表项数
    visibleCount () {
      return Math.floor((this.screenHeight - this.footerHeight - this.headerHeight) / this.itemHeight)
    },

    // 偏移量对应的style
    getTransform () {
      return `translate3d(0, ${this.startOffset}px, 0)`
    },
    // 获取真实显示列表数据
    visibleData () {
      return this.list.slice(this.start, Math.min(this.end, this.list.length))
    },
    end () {
      return this.start + this.visibleCount
    }
  },
  watch: {
    visibleData () {
      this.$emit('list:update', this.visibleData, this.start)
    }
  },
  mounted () {
    // 页面数据初始化
    this.screenHeight = this.$el.clientHeight
    this.start = 0
    console.log('visibleCount: ', this.visibleCount)
    // 这里的截取结束位置需要根据开始位置和首屏显示的条数来确定
  },
  methods: {
    scrollEvent () {
      // 当前滚动位置
      const scrollTop = this.$refs.list.scrollTop
      // if (scrollTop - this.headerHeight < 0) {
      //   this.start = 0
      //   this.startOffset = 0
      //   return
      // }
      // 此时的开始索引
      this.start = Math.floor((scrollTop) / this.itemHeight)
      // 此时的结束索引
      // 滚动时列表盒子的偏移量
      // this.startOffset = 0 - (scrollTop % this.itemHeight)
    }
  }
}
</script>

<style lang="less" scoped>
.infinite-list-container {
  height: 100%;
  overflow: auto;
  position: relative;
  -webkit-overflow-scrolling: touch;
}
.infinite-list-phantom {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  z-index: -1;
}
.infinite-list {
  left: 0;
  top: 0;
  position: absolute;
  // text-align: center;
}
</style>
