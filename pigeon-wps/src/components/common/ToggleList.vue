<template>
  <span class="toggle-list-component">
    <slot :isCollapse="isCollapse">
      <div v-if="list && list.length && showIcon" class="toggle-btn" @click="toggle">
        {{ isCollapse ? '展开' : '收起' }}
        <a-icon
          class="toogle-icon"
          type="down"
          :class="{reverse: !isCollapse}"
        />
      </div>
    </slot>
  </span>
</template>

<script>
export default {
  props: {
    list: {
      type: Array,
      default () {
        return []
      }
    },
    showLenth: {
      type: Number,
      default: 4
    },
    collapse: {
      type: Boolean,
      default: true
    },
    collapseStatus: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      isCollapse: false,
      showIcon: true,
      isFinishInit: false
    }
  },
  watch: {
    list: {
      immediate: true,
      handler (newVal, oldVal) {
        if (this.isFinishInit) {
          this.updateList()
        } else {
          this.init()
          this.isFinishInit = true
        }
      }
    },
    collapseStatus (val) {
      this.isCollapse = val
    }
  },
  methods: {
    init () {
      this.isCollapse = false
      this.showIcon = true
      if (!this.list || !this.list.length) return
      if (this.list.length <= this.showLenth) {
        this.showIcon = false
        this.$emit('change', this.list, this.isCollapse)
        return
      }
      if (this.collapse) {
        this.toggle()
      } else {
        this.updateList()
      }
    },
    toggle () {
      this.isCollapse = !this.isCollapse
      this.updateList()
    },
    updateList () {
      const list = this.isCollapse ? this.list.slice(0, this.showLenth) : this.list
      this.$emit('change', list, this.isCollapse)
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .toggle-btn {
  display: inline-block;
  // width: 20px;
  // height: 20px;
  font-size: 12px;
  // line-height: 20px;
  color: #1890ff;
  cursor: pointer;
}
.toogle-icon {
  transition: transform 0.24s;
}
.reverse {
  transform: rotate(180deg);
}
</style>
