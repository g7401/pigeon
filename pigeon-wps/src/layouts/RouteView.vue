<script>
export default {
  name: 'RouteView',
  props: {
    keepAlive: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      isRouterAlive: true
    }
  },
  created () {
    const watchFn = (fullPath) => {
      if (fullPath === this.$route.fullPath) {
        this.refresh()
      }
    }
    this.$multiTab.instance.$on('refresh', watchFn)
    this.$once('hook:beforeDestroy', function () {
      this.$multiTab.instance.$off('refresh', watchFn)
    })
  },
  methods: {
    refresh () {
      this.isRouterAlive = false
      this.$nextTick(function () {
        this.isRouterAlive = true
      })
    }
  },
  render () {
    const {
      isRouterAlive,
      $route: { fullPath, meta },
      $store: { getters }
    } = this
    const inKeep = isRouterAlive ? (
      <keep-alive include={getters.include}>
        <router-view ref="view" key={fullPath} />
      </keep-alive>
    ) : (
      <keep-alive include={getters.include}>
      </keep-alive>
    )
    const notKeep = <router-view ref="view" />
    // 这里增加了 multiTab 的判断，当开启了 multiTab 时
    // 应当全部组件皆缓存，否则会导致切换页面后页面还原成原始状态
    // 若确实不需要，可改为 return meta.keepAlive ? inKeep : notKeep
    if (!getters.multiTab && !meta.keepAlive) {
      return notKeep
    }
    return this.keepAlive || getters.multiTab || meta.keepAlive ? inKeep : notKeep
  }
}
</script>
