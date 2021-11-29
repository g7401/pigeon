import Vue from 'vue'
function setValue (field, value) {
  const fieldArr = field.split('.')
  fieldArr.reduce((obj, item, i) => {
    if (fieldArr.length - 1 === i) {
      Vue.set(obj, item, value)
    }
    return obj[item]
  }, this)
}

function loadingDecorator (promise, field) {
  return function () {
    field ? setValue.call(this, field, true) : Vue.prototype.$loading.show()
    return promise.apply(this, arguments).finally(() => {
      field ? setValue.call(this, field, false) : Vue.prototype.$loading.hide()
    })
  }
}

export default {
  install (Vue) {
    Vue.prototype.$loadingDecorator = loadingDecorator
  }
}
