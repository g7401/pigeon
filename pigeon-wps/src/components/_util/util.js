/**
 * components util
 */

/**
 * 清理空值，对象
 * @param children
 * @returns {*[]}
 */
export function filterEmpty (children = []) {
  return children.filter(c => c.tag || (c.text && c.text.trim() !== ''))
}

/**
 * 获取字符串长度，英文字符 长度1，中文字符长度2
 * @param {*} str
 */
export const getStrFullLength = (str = '') =>
  str.split('').reduce((pre, cur) => {
    const charCode = cur.charCodeAt(0)
    if (charCode >= 0 && charCode <= 128) {
      return pre + 1
    }
    return pre + 2
  }, 0)

/**
 * 截取字符串，根据 maxLength 截取后返回
 * @param {*} str
 * @param {*} maxLength
 */
export const cutStrByFullLength = (str = '', maxLength) => {
  let showLength = 0
  if (getStrFullLength(str) <= maxLength) {
    return str
  }
  const formatStr = str.split('').reduce((pre, cur) => {
    const charCode = cur.charCodeAt(0)
    if (charCode >= 0 && charCode <= 128) {
      showLength += 1
    } else {
      showLength += 2
    }
    if (showLength <= maxLength) {
      return pre + cur
    }
    return pre
  }, '')
  return `${formatStr}...`
}

export function firstLetterUpper (str = '') {
  return str ? str[0].toUpperCase() + str.slice(1) : str
}

export function toHump (name) {
  return name.replace(/_(\w)/g, (all, letter) => {
    return letter.toUpperCase()
  })
}

export function isObj (i) {
  return Object.prototype.toString.call(i) === '[object Object]'
}

export function humpObj (obj) {
  if (!isObj(obj)) {
    return obj
  }
  const newObj = {}
  Object.keys(obj).forEach(key => {
    const item = obj[key]

    if (Array.isArray(item)) {
      newObj[toHump(key)] = item.map(i => humpObj(i))
    } else if (isObj(item)) {
      newObj[toHump(key)] = humpObj(item)
    } else {
      newObj[toHump(key)] = item
    }
  })
  return newObj
}
