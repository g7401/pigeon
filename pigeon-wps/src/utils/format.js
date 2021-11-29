import { isObj } from '@/components/_util/util'
// import { api } from '@/api/d1'

export function bool (val) {
  if (val === 0 || val === '0' || val === false) {
    return 'No'
  } else if (val === 1 || val === '1' || val === true) {
    return 'Yes'
  }
  return val
}

export function object (val) {
  if (isObj(val)) {
    return Object.entries(val)
      .map(arr => arr.join(': '))
      .join(', ')
  }
  return val
}

export function time (duration) {
  if (!duration) {
    return null
  }
  if (duration < 1000) {
    return `${duration} ms`
  }
  return `${Math.floor(duration / 1000)} s`
}

export function fileSize (val) {
  if (!val) return val
  if (val >= 1024) {
    return `${Math.floor(val / 1024)} KB`
  } else {
    return `${val} B`
  }
}

export function link (id) {
  if (!id) return id
  if (this.$api.config.download.startsWith('http')) {
    return `${this.$api.config.download}?file_id=${id}`
  }
  return `${window.location.protocol}//${window.location.host}${this.$api.config.download}?file_id=${id}`
}
