<script>
import events from '../_util/events'
import { mapMutations } from 'vuex'
export default {
  name: 'MultiTab',
  data () {
    return {
      maxTabNum: 10,
      fullPathList: [],
      pages: [],
      activeKey: '',
      newTabIndex: 0
    }
  },
  created () {
    // bind event
    events
      .$on('open', val => {
        if (!val) {
          throw new Error(`multi-tab: open tab ${val} err`)
        }
        this.activeKey = val
      })
      .$on('close', val => {
        if (!val) {
          this.closeThat(this.activeKey)
          return
        }
        this.closeThat(val)
      })
      .$on('remove', val => {
        this.pureRemove(val)
      })
      .$on('rename', ({ key, name }) => {
        try {
          const item = this.pages.find(item => item.path === key)
          item.meta.customTitle = name
          this.$forceUpdate()
        } catch (e) {}
      })
  },
  methods: {
    ...mapMutations(['addRouteCache', 'removeRouteCache']),
    onEdit (targetKey, action) {
      this[action](targetKey)
    },
    pureRemove (targetKey) {
      const closeingPage = this.pages.find(x => x.fullPath === targetKey)
      if (closeingPage) {
        this.removeRouteCache(closeingPage)
        this.isCacheRoute(this.$route) && this.rawRemoveRouteCache()
      }
      this.pages = this.pages.filter(page => page.fullPath !== targetKey)
      this.fullPathList = this.fullPathList.filter(path => path !== targetKey)
    },
    remove (targetKey) {
      this.pureRemove(targetKey)
      // 判断当前标签是否关闭，若关闭则跳转到最后一个还存在的标签页
      if (!this.fullPathList.includes(this.activeKey)) {
        this.selectedLastPath()
      }
    },
    selectedLastPath () {
      this.activeKey = this.fullPathList[this.fullPathList.length - 1]
    },

    // content menu
    closeThat (e) {
      // 判断是否为最后一个标签页，如果是最后一个，则无法被关闭
      if (this.fullPathList.length > 1) {
        this.remove(e)
      } else {
        this.$message.info('这是最后一个标签了, 无法被关闭')
      }
    },
    closeLeft (e) {
      const currentIndex = this.fullPathList.indexOf(e)
      if (currentIndex > 0) {
        this.fullPathList.forEach((item, index) => {
          if (index < currentIndex) {
            this.remove(item)
          }
        })
      } else {
        this.$message.info('左侧没有标签')
      }
    },
    closeRight (e) {
      const currentIndex = this.fullPathList.indexOf(e)
      if (currentIndex < this.fullPathList.length - 1) {
        this.fullPathList.forEach((item, index) => {
          if (index > currentIndex) {
            this.remove(item)
          }
        })
      } else {
        this.$message.info('右侧没有标签')
      }
    },
    closeAll (e) {
      const currentIndex = this.fullPathList.indexOf(e)
      this.fullPathList.forEach((item, index) => {
        if (index !== currentIndex) {
          this.remove(item)
        }
      })
    },
    closeMenuClick (key, route) {
      this[key](route)
    },
    renderTabPaneMenu (e) {
      return (
        <a-menu
          {...{
            on: {
              click: ({ key, item, domEvent }) => {
                if (key === 'refresh') {
                  this.refresh(e)
                } else {
                  this.closeMenuClick(key, e)
                }
              }
            }
          }}
        >
          <a-menu-item key="refresh">刷新</a-menu-item>
          <a-menu-item key="closeThat">关闭当前标签</a-menu-item>
          <a-menu-item key="closeRight">关闭右侧</a-menu-item>
          <a-menu-item key="closeLeft">关闭左侧</a-menu-item>
          <a-menu-item key="closeAll">关闭全部</a-menu-item>
        </a-menu>
      )
    },
    // render
    renderTabPane (title, keyPath) {
      const menu = this.renderTabPaneMenu(keyPath)

      return (
        <a-dropdown overlay={menu} trigger={['contextmenu']}>
          <span style={{ userSelect: 'none' }}>{title}</span>
        </a-dropdown>
      )
    },
    isCacheRoute (route) {
      return this.$store.getters.isCacheRoute(route)
    },
    addTab (route, index = null) {
      if (this.fullPathList.length >= this.maxTabNum) {
        this.pureRemove(this.fullPathList[0])
      }
      if (index === null) {
        this.fullPathList.push(route.fullPath)
      } else {
        this.fullPathList.splice(index, 0, route.fullPath)
      }
      const page = { path: route.path, fullPath: route.fullPath, name: route.name, meta: { title: route.meta.title } }
      const oldCount = this.pages.filter(page => page.name === route.name).length
      if (oldCount) {
        page.meta.customTitle = `${route.meta.title}(${oldCount + 1})`
      }
      if (index === null) {
        this.pages.push(page)
      } else {
        this.pages.splice(index, 0, page)
      }
      this.addRouteCache(route)
    },
    sliceTab (route, index) {
      const oldPage = this.pages[index]
      this.pureRemove(oldPage.fullPath)
      this.fullPathList.splice(index, 0, route.fullPath)
      this.pages.splice(index, 0, route)
      this.addRouteCache(route)
    },
    refresh (fullPath) {
      const oldPathIndex = this.fullPathList.indexOf(fullPath)
      const page = this.pages.find(x => x.fullPath === fullPath)
      this.pureRemove(fullPath)
      events.$emit('refresh', fullPath)
      this.$nextTick(() => {
        this.addTab(page, oldPathIndex)
      })
    },
    getPageComponent () {
      const vnode = this.$route.matched[this.$route.matched.length - 1].instances.default.$vnode
      return vnode.componentInstance
    },
    rawRemoveRouteCache () {
      const vnode = this.$route.matched[this.$route.matched.length - 1].instances.default.$vnode
      var cache = vnode.parent.componentInstance.cache
      var keys = vnode.parent.componentInstance.keys
      const key = vnode.key
      if (key && cache[key]) {
        if (keys.length) {
          var index = keys.indexOf(key)
          if (index > -1) {
            keys.splice(index, 1)
          }
        }
        delete cache[key]
      }
    }
  },
  watch: {
    $route: {
      handler (newVal) {
        this.activeKey = newVal.fullPath
        if (newVal.meta.noTab) return
        const oldPageIndex = this.pages.findIndex(page => page.name === newVal.name)
        const oldPathIndex = this.fullPathList.indexOf(newVal.fullPath)
        if (newVal.meta.singleTab && oldPageIndex !== -1 && newVal.fullPath !== this.pages[oldPageIndex].fullPath) {
          this.sliceTab(newVal, oldPageIndex)
        } else if (oldPathIndex === -1) {
          this.addTab(newVal)
        } else if (oldPathIndex >= 0 && !this.isCacheRoute(newVal)) {
          this.$nextTick(() => {
            this.addRouteCache(newVal)
          })
        }
      },
      immediate: true
    },
    activeKey: function (newPathKey) {
      this.$router.push({ path: newPathKey })
    }
  },
  render () {
    const {
      onEdit,
      $data: { pages }
    } = this
    const panes = pages.map(page => {
      return (
        <a-tab-pane
          style={{ height: 0 }}
          tab={this.renderTabPane(page.meta.customTitle || page.meta.title, page.fullPath)}
          key={page.fullPath}
          closable={pages.length > 1}
        ></a-tab-pane>
      )
    })

    return (
      <div class="ant-pro-multi-tab">
        <div class="ant-pro-multi-tab-wrapper">
          <a-tabs
            hideAdd
            size="small"
            type="editable-card"
            v-model={this.activeKey}
            tabBarStyle={{ backgroundColor: '#fff', margin: 0, paddingLeft: '16px', paddingTop: '10px' }}
            {...{ on: { edit: onEdit } }}
          >
            {panes}
          </a-tabs>
        </div>
      </div>
    )
  }
}
</script>
