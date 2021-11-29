<template>
  <div
    class="toggle-height"
  >
    <div
      class="toggle-content"
      :style="{height: contentHeight}"
    >
      <slot></slot>
      <div
        class="gradient-area"
        @click="toggle"
        v-if="isCollapse"
        :style="{height: gradientHeight + 'px'}"
      ></div>
    </div>
    <div
      class="toggle-box"
      @click="toggle"
    >
      <slot
        name="icon"
        :isCollapse="isCollapse"
      >
        <a-icon
          class="toggle-btn"
          type="down"
          :class="{reverse: !isCollapse}"
        />
      </slot>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    showLength: {
      type: Number,
      default: 1
    },
    gradientHeight: {
      type: Number,
      default: 50
    }
  },
  data () {
    return {
      isCollapse: true,
      showIcon: true,
      collapseHeight: 0
    }
  },
  computed: {
    contentHeight () {
      if (this.isCollapse) {
        return this.collapseHeight + this.gradientHeight + 'px'
      }
      return 'auto'
    }
  },
  created () {
    if (this.$slots.default.length <= this.showLength) {
      this.showIcon = false
    }
  },
  mounted () {
    this.collapseHeight = this.$slots.default
      .slice(0, this.showLength)
      .reduce((acc, vnode) => acc + vnode.elm.offsetHeight, 0)
  },
  methods: {
    toggle () {
      this.isCollapse = !this.isCollapse
    }
  }
}
</script>

<style lang="less" scoped>
.toggle-btn {
  transition: transform 0.24s;
  display: inline-block;
}
.reverse {
  transform: rotate(180deg);
}
.toggle-content {
  overflow: hidden;
  position: relative;
}
.gradient-area {
  position: absolute;
  bottom: 0;
  width: 100%;
  background: linear-gradient(rgba(255, 255, 255, 0.1), white);
  cursor: pointer;
}
.toggle-box {
  height: 25px;
  line-height: 25px;
  text-align: center;
  cursor: pointer;
}
</style>
