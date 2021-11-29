
const formatName = (name) => {
  const firstLetter = name[0]
  if (name && /[a-z]/.test(firstLetter)) {
    name = firstLetter.toUpperCase() + name.substr(1)
  }
  return name
}

const getIncludeName = (route) => {
  return formatName(route.meta.componentName || route.name)
}

const addInclude = (state, name) => {
  if (!name) return
  name = formatName(name)
  state.include.push(name)
  // if (state.include.indexOf(name) === -1) {
  //   state.include.push(name)
  // }
}
const removeInclude = (state, name) => {
  const index = state.include.findIndex(x => x.toLowerCase() === name.toLowerCase())
  index !== -1 && state.include.splice(index, 1)
}

export default {
  state: {
    include: ['BlankLayout', 'RouteView']
  },
  mutations: {
    ADD_INCLUDE: addInclude,
    REMOVE_INCLUDE: removeInclude,
    REMOVE_INCLUDES: (state, names) => {
      names = names.map(x => x.toLowerCase())
      const include = state.include.filter(x => !names.includes(x.toLowerCase()))
      state.include = include
    },
    addRouteCache: (state, route) => {
      const name = getIncludeName(route)
      addInclude(state, name)
    },
    removeRouteCache: (state, route) => {
      const name = getIncludeName(route)
      removeInclude(state, name)
    }
  },
  getters: {
    isCacheRoute: (state) => (route) => {
      const name = getIncludeName(route)
      return state.include.includes(name)
    }
  }
}
