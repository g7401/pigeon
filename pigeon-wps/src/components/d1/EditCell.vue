<template>
  <div class="editable-cell">
    <div
      class="text-box"
      :class="{disabled: disabled}"
      v-if="!editable"
      @dblclick="toEdit"
    ><span class="placeholder">.</span>{{ $attrs.value }}</div>
    <slot v-else>
      <div
        @mousedown.stop
        @click.stop
      >
        <component
          :is="'A' + type"
          size="small"
          v-bind="$attrs"
          :value="copyValue"
          v-on="inputListeners"
        ></component>
      </div>
    </slot>
  </div>
</template>

<script>
export default {
  props: {
    type: {
      type: String,
      default: 'Input'
    },
    disabled: {
      type: Boolean,
      default: false
    },
    defaultEdit: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    inputListeners: function () {
      var vm = this
      return Object.assign({}, this.$listeners, {
        input: function (event) {
          vm.copyValue = event.target.value
        },
        change: function (event) {
          if (['Select', 'InputNumber'].includes(vm.type)) vm.copyValue = event
          if (vm.type === 'Select') {
            vm.toText()
          }
        },
        pressEnter () {
          if (['Input', 'InputNumber'].includes(vm.type)) vm.toText()
        }
      })
    }
  },
  watch: {
    editable (val) {
      this.$emit('edit', val)
    }
  },
  data () {
    return {
      editable: false,
      copyValue: null
    }
  },
  created () {
    this.copyValue = this.$attrs.value
    if (this.defaultEdit) this.editable = true
  },
  methods: {
    toEdit () {
      if (this.disabled) return
      this.editable = true
      this.watchClick()
    },
    toText () {
      if (this.copyValue !== null) {
        this.$emit('input', this.copyValue)
        this.$emit('change', this.copyValue)
      }
      if (!this.defaultEdit) {
        this.editable = false
      }
    },
    watchClick () {
      let watchFnName
      if (['Input', 'InputNumber'].includes(this.type)) {
        watchFnName = 'mousedown'
      } else {
        watchFnName = 'click'
      }
      document.body.addEventListener(watchFnName, this.toText, { once: true })
    }
  }
}
</script>

<style lang="less" scoped>
.text-box {
  width: 100%;
  text-align: center;
  text-overflow: ellipsis;
  overflow: hidden;
  cursor: pointer;
}
.text-box.disabled {
  cursor: default;
  background-color: #eeeeee;
}
/deep/ .ant-input-number,
/deep/ .ant-select {
  width: 100%;
}
.placeholder {
  visibility: hidden;
}
</style>
