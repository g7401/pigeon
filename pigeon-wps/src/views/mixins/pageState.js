import debounce from 'lodash.debounce'

export default {
  data () {
    return {
      firstUpdate: true,
      unWatch: null,
      pageName: null
    }
  },
  activated () {
    this.pageName = this.$route.name
    if (!this.$currentPage) return
    this.unWatch = this.$store.watch((state, getters) => getters.getPageState(this.pageName), this.$debounceHandleWatch(), { deep: true, immediate: true })
  },
  deactivated () {
    this.unWatch && this.unWatch()
  },
  destroyed () {
    this.unWatch && this.unWatch()
  },
  computed: {
    $currentPage () {
      return this.$store && this.$store.getters.getPage(this.pageName)
    },
    $pageState () {
      return this.$store.getters.getPageState(this.pageName)
    }
  },
  methods: {
    $debounceHandleWatch () {
      return debounce(this.$handleWatch, 100)
    },
    $handleWatch (state) {
      console.log('handleWatch:', state, this.firstUpdate, this.$currentPage)
      if (!state) return
      if (!this.firstUpdate && (!this.$store.getters.canUpdatePage(this.pageName))) return
      this.$updatePage(state)
      this.$store.commit('setPageUpdated', this.pageName)
      this.firstUpdate = false
    },
    $updatePage (state) {
      if (!this.$currentPage) return
      const updateFn = this[`$${this.$currentPage.updateType}Update`]
      updateFn && updateFn(state)
    },
    $queryParamsUpdate (state) {
      let isEqual
      if (Object.keys(this.$route.query).length !== Object.keys(state).length) {
        isEqual = false
      } else {
        isEqual = true
        Object.keys(this.$route.query).forEach(key => {
          if (this.$route.query[key] !== state[key]) {
            isEqual = false
          }
        })
      }
      if (isEqual) return
      this.$router.replace({
        ...this.$route,
        name: this.pageName,
        query: {
          ...this.$route.query,
          ...state
        }
      })
    }
    // $comDataUpdate (state) {
    //   Object.assign(this.$data, this.$options.data(), state)
    // }
  }
}
