import cloneDeep from 'lodash.clonedeep'
export default {
  data () {
    return {
      treeDataKey: 'treeData'
    }
  },
  computed: {
    dataList () {
      return this.generateList(this[this.treeDataKey])
    }
  },
  methods: {
    // tree 通用函数
    expandSelf (fn) {
      const expandedKeys = this.dataList.filter(item => fn(item)).map(item => item[this.replaceFields.key])
      Object.assign(this, {
        expandedKeys,
        autoExpandParent: !!expandedKeys.length
      })
    },
    expandParent (fn) {
      const expandedKeys = this.dataList.filter(item => fn(item)).map(item => this.getParentKey(item[this.replaceFields.key]))
      Object.assign(this, {
        expandedKeys,
        autoExpandParent: !!expandedKeys.length
      })
    },
    hasDeep (node, fn, deep = 1) {
      if (fn(node)) {
        return deep
      } else if (node[this.replaceFields.children] && node[this.replaceFields.children].length) {
        return node[this.replaceFields.children].some(x => this.hasDeep(x, fn, deep++))
      } else {
        return false
      }
    },
    filterDeep (tree, fn) {
      const copyTree = cloneDeep(tree)
      const newTreeData = copyTree.filter(x => {
        const hasValue = this.hasDeep(x, fn)
        if (x[this.replaceFields.children] && hasValue !== 1) {
          x[this.replaceFields.children] = this.filterDeep(x[this.replaceFields.children], fn)
        }
        return hasValue
      })
      return newTreeData
    },
    getParent (key, dataList) {
      dataList = dataList || this.dataList
      const node = dataList.find(
        item =>
          item[this.replaceFields.children] &&
          item[this.replaceFields.children].some(x => x[this.replaceFields.key] === key)
      )
      return node
    },
    getParentKey (key, dataList) {
      const node = this.getParent(key, dataList)
      return node && node[this.replaceFields.key]
    },
    generateList (data, list = []) {
      for (let i = 0; i < data.length; i++) {
        const node = data[i]
        list.push(node)
        if (node[this.replaceFields.children]) {
          this.generateList(node[this.replaceFields.children], list)
        }
      }
      return list
    },
    getChain (key, needSelf = true, chain = []) {
      if (!chain.length && needSelf) {
        const selfItem = this.dataList.find(x => x[this.replaceFields.key] === key)
        if (selfItem) {
          chain.push(selfItem)
        }
      }
      const lastLevel = this.dataList.find(
        item => item.children && item.children.some(child => child[this.replaceFields.key] === key)
      )
      if (lastLevel) {
        chain.unshift(lastLevel)
        this.getChain(lastLevel[this.replaceFields.key], needSelf, chain)
      }
      return chain
    }
  }
}
