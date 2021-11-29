const deepType = ['object', 'array']

// 应对没有接口文档的情况下，前后端并行开发，字段命名不统一的问题。
// 应对后端同类型接口（例如列表分页查询）字段命名不统一导致请求内嵌的组件不能直接用的问题，例如同样是分页查询接口，总条目数有的叫 total 有的叫 total_count。
// 将后端的命名字段从前端业务逻辑中抽离出来，后端服务重构时，不需要修改业务逻辑代码，只需要在配置文件里修改映射关系。
// 前端写接口Gateway,有利于维护,不需要依赖后端服务,通过Gateway就可以知道某个接口会返回什么数据。
// 通过Gateway定义json可以生成mock数据。
// 可以对数据进行预处理，比如将字符串'2020-11-26'处理成moment对象。

class Gateway {
  constructor (defines, multiple = false, pure = false) {
    this.pure = pure
    if (multiple) {
      if (defines.response) {
        this.responseDefine = defines.response
      }
      if (defines.reqBody) {
        this.reqBodyDefine = defines.reqBody
      }
      if (defines.reqParams) {
        this.reqParamsDefine = defines.reqParams
      }
    } else {
      this.responseDefine = defines
    }
  }

  _reverseJsonDefine (define) {
    if (define.type === 'array' && define.items && define.items.properties) {
      const newDefine = { ...define }
      newDefine.items = { ...define.items }
      newDefine.items.properties = this._reverseJsonDefine(
        define.items.properties
      )
      return newDefine
    }
    if (define.type === 'object') {
      define = define.properties
    }
    const reverseJsonDefine = {}
    Object.keys(define).forEach(key => {
      const propertyDefine = define[key]
      const propertyKey = propertyDefine.property || key
      reverseJsonDefine[propertyKey] = { ...propertyDefine, property: key }
      if (propertyDefine.properties) {
        reverseJsonDefine[propertyKey].properties = this._reverseJsonDefine(
          propertyDefine.properties
        )
      } else if (propertyDefine.items && propertyDefine.items.properties) {
        reverseJsonDefine[propertyKey].items = { ...propertyDefine.items }
        reverseJsonDefine[propertyKey].items.properties = this._reverseJsonDefine(
          propertyDefine.items.properties
        )
      }
    })
    return reverseJsonDefine
  }

  _getDeepData (data, property) {
    let temp = data
    const chain = property.split('.')
    for (let i = 0; i < chain.length; i++) {
      if (temp === undefined || temp === null) {
        return
      }
      temp = temp[chain[i]]
    }
    return temp
  }

  toObjFront (objData, properties) {
    const newData = {}
    const jsonDefineKeys = Object.keys(properties)
    const rawPropertyKeys = jsonDefineKeys.map(x => properties[x].property ? properties[x].property.split('.')[0] : x)
    if (!this.pure) {
      Object.keys(objData).filter(key => !rawPropertyKeys.includes(key)).forEach(key => {
        newData[key] = objData[key]
      })
    }
    jsonDefineKeys.forEach(key => {
      const propertyDefine = properties[key]
      const chain = key.split('.')
      const lastKey = chain[chain.length - 1]
      let temp = newData
      if (chain.length > 1) {
        chain.slice(0, -1).map(x => {
          temp[x] = temp[x] || {}
          temp = temp[x]
        })
      }
      const propertyData = this._getDeepData(objData, propertyDefine.property || key)
      if (propertyData === undefined || propertyData === null) {
        temp[lastKey] = propertyDefine.defaultValue
        if (propertyDefine.type === 'object') {
          temp[lastKey] = {}
        } else if (propertyDefine.type === 'array') {
          temp[lastKey] = []
        }
        return
      }
      if (!deepType.includes(propertyDefine.type)) {
        if (propertyDefine.formater) {
          temp[lastKey] = propertyDefine.formater(propertyData)
        } else {
          temp[lastKey] = propertyData
        }
      } else if (propertyDefine.type === 'object' && propertyDefine.properties) {
        temp[lastKey] = this.toObjFront(propertyData, propertyDefine.properties)
      } else if (propertyDefine.type === 'array') {
        if (propertyDefine.items === 'tree') {
          temp[lastKey] = propertyData.map(x => this.toObjFront(x, properties))
        } else {
          temp[lastKey] = this.toArrayFront(propertyData, propertyDefine.items)
        }
      } else {
        temp[lastKey] = propertyData
      }
    })
    return newData
  }

  toArrayFront (arrData, items) {
    if (!arrData) return arrData
    if (!deepType.includes(items.type)) {
      if (items.formater) {
        return arrData.map(x => items.formater(x))
      } else {
        return arrData
      }
    } else if (items.type === 'object') {
      return arrData.map(x => this.toObjFront(x, items.properties))
    } else if (items.type === 'array') {
      return arrData.map(x => this.toArrayFront(x, items.items))
    }
  }

  toFront (ajaxData, define) {
    define = define || this.responseDefine
    if (!define) return ajaxData
    if (!ajaxData) return ajaxData
    if (define.type === 'array') {
      return this.toArrayFront(ajaxData, define.items)
    } else if (define.type === 'object') {
      return this.toObjFront(ajaxData, define.properties)
    } else {
      return this.toObjFront(ajaxData, define)
    }
  }

  toBack (frontData, type = 'params') {
    let define
    if (type === 'params') {
      define = this.reqParamsDefine
    } else if (type === 'body') {
      define = this.reqBodyDefine
    }
    if (!define) return frontData
    const reverseDefine = this._reverseJsonDefine(define)
    return this.toFront(frontData, reverseDefine)
  }

  _getMockValue (propertyDefine) {
    const Mock = require('mockjs2')
    const mockPlaceholderMap = {
      'boolean': 'boolean',
      'string': "string('lower', 30)",
      'number': 'integer(0, 10000)',
      'integer': 'integer'
    }
    const mockPlaceholder = propertyDefine.mockPlaceholder || mockPlaceholderMap[propertyDefine.type]
    return Mock.mock(`@${mockPlaceholder}`)
  }

  _getObjectMockData (properties, deep = 1) {
    const mockData = {}
    Object.keys(properties).forEach(key => {
      const propertyDefine = properties[key]
      const property = propertyDefine.property || key
      if (!deepType.includes(propertyDefine.type)) {
        mockData[property] = propertyDefine.defaultValue || this._getMockValue(propertyDefine)
      } else if (propertyDefine.type === 'object' && propertyDefine.properties) {
        mockData[property] = this._getObjectMockData(propertyDefine.properties)
      } else if (propertyDefine.type === 'array' && propertyDefine.items) {
        let define
        if (propertyDefine.items === 'tree') {
          define = {
            type: 'object',
            properties
          }
          deep++
        } else {
          define = propertyDefine.items
        }
        if (deep > 3) {
          mockData[property] = []
          return
        }
        mockData[property] = this._getArrayMockData(define, propertyDefine.mockCount, deep)
      }
    })
    return mockData
  }

  _getArrayMockData (items, count = 3, deep = 1) {
    if (!deepType.includes(items.type)) {
      return Array.from({ length: count }).map(x => this._getMockValue(items))
    } else if (items.type === 'object' && items.properties) {
      return Array.from({ length: count }).map(x => this._getObjectMockData(items.properties, deep))
    } else if (items.type === 'array' && items.items) {
      return Array.from({ length: count }).map(x => this._getArrayMockData(items.items, items.mockCount))
    }
  }

  getMockData () {
    const define = this.responseDefine
    if (!define) return
    if (define.type === 'array') {
      return this._getArrayMockData(define.items, define.mockCount)
    } else if (define.type === 'object') {
      return this._getObjectMockData(define.properties)
    } else {
      return this._getObjectMockData(define)
    }
  }
}

// 树形例子
// {
//   title: {
//     type: String,
//     property: 'label'
//   },
//   key: {
//     type: String,
//     property: 'id'
//   },
//   children: {
//     type: Array,
//     property: 'children',
//     defaultValue: [],
//     items: 'tree'
//   }
// }

export default Gateway
