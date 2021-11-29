<template>
  <a-descriptions
    :title="config.title"
    :column="mainConfig.column"
    :layout="mainConfig.layout"
    :size="mainConfig.size"
    :bordered="mainConfig.bordered"
  >
    <span v-if="mainConfig.none">N/A</span>
    <a-descriptions-item
      v-for="option in options"
      :key="option.key"
      :label="option.label"
    >
      <slot
        :name="option.key"
        :val="obj[option.key]"
      >
        <template v-if="option.type === 'link'">
          <template v-if="obj[option.key]">
            <a
              :href="link(obj[option.key])"
              target="_blank"
            >
              <Ellipsis
                :length="mainConfig.length"
                tooltip
              >{{ link(obj[option.key]) }}</Ellipsis>
            </a>
          </template>
          <template v-else>{{ mainConfig.nullStr }}</template>
        </template>
        <template v-else>
          <Ellipsis
            :length="mainConfig.length"
            tooltip
          >{{ show(obj[option.key], option.type) || mainConfig.nullStr }}</Ellipsis>
          <span
            class="unit"
            v-if="obj[option.key] && option.unit"
          >{{ option.unit }}</span>
        </template>

      </slot>
    </a-descriptions-item>
  </a-descriptions>
</template>

<script>
import Ellipsis from '@/components/Ellipsis'
import * as format from '@/utils/format'
const baseConfig = {
  title: '',
  column: 1,
  layout: 'horizontal',
  size: 'small',
  length: 100,
  nullStr: '',
  bordered: false
}
export default {
  components: { Ellipsis },
  props: {
    config: {
      type: Object,
      default () {
        return { ...baseConfig }
      }
    },
    options: {
      type: Array,
      default () {
        return []
      }
    },
    obj: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      mainConfig: {}
    }
  },
  created () {
    this.mainConfig = { ...baseConfig, ...this.config }
  },
  methods: {
    show (val, type) {
      const formatFn = this[`${type}`]
      if (!type || !formatFn) return val
      return formatFn(val)
    },
    ...format
  }
}
</script>

<style lang="less" scoped>
@import '~@/components/index.less';
.unit {
  color: @primary-color;
  font-size: 12px;
  margin-left: 2px;
  font-weight: 500;
}
</style>
