<template>
  <span>
    <a-select
      ref="select"
      size="small"
      :value="value"
      :mode="multiple ? 'multiple' : 'default'"
      :options="selectedOptions"
      allowClear
      :showSearch="false"
      @dropdownVisibleChange="handleFocus"
      @change="handleSelect"
    >
      <div
        slot="dropdownRender"
        @mousedown="e => e.preventDefault()"
      >

      </div>
    </a-select>
    <a-modal :title="title" v-model="modalVisible" @ok="handleOk" @cancel="handleCancel">
      <LazyTreeSelect
        v-if="modalVisible"
        ref="vTree"
        :height="height"
        :apiName="apiName"
        :searchApiName="searchApiName"
        :replaceFields="replaceFields"
        :mode="multiple ? 'checkbox' : 'radio'"
        :defaultSelectedKeys="defaultSelectedValues"
        :defaultExtraData="{ selected_dictionary_content_uid: defaultSelectedUids }"
        @checkboxChange="checkboxChange"
        @radioChange="radioChange"
        v-bind="$attrs"
        v-on="$listeners"
      >
      </LazyTreeSelect>
    </a-modal>
  </span>
</template>

<script>
import LazyTreeSelect from '@/components/d1/LazyTreeSelect'
import cloneDeep from 'lodash.clonedeep'
export default {
  components: { LazyTreeSelect },
  data () {
    return {
      modalVisible: false,
      oldValue: null,
      tempValue: null,
      selectedOptions: []
    }
  },
  props: {
    value: {
      type: [Array, String],
      default: null
    },
    multiple: {
      type: Boolean,
      default: false
    },
    replaceFields: {
      type: Object,
      default () {
        return { title: 'name', key: 'value', value: 'value', children: 'children' }
      }
    },
    apiName: {
      type: String,
      default: 'getUserDictContentByParent'
    },
    searchApiName: {
      type: String,
      default: 'getUserDictContentByLabel'
    },
    height: {
      type: Number,
      default: 400
    },
    title: {
      type: String,
      default: 'Tree Select'
    },
    defaultSelectedOptions: {
      type: Array,
      default () {
        return []
      }
    }
  },
  created () {
    this.selectedOptions = cloneDeep(this.defaultSelectedOptions)
  },
  computed: {
    defaultSelectedValues () {
      return this.selectedOptions.map(x => x.value)
    },
    defaultSelectedUids () {
      return this.selectedOptions.map(x => x.uid)
    }
    // selectedRowOptions () {
    //   if (!this.value) return []
    //   if (this.multiple) {
    //     return this.value.map(x => ({
    //     label: x,
    //     value: x
    //   }))
    //   } else {
    //     return [{
    //       label: this.value,
    //       value: this.value
    //     }]
    //   }
    // }
    // defaultExpandKeys () {
    //   if (this.multiple) {
    //     return this.tempValue
    //   } else {
    //     return [this.tempValue]
    //   }
    // }
  },
  methods: {
    handleFocus () {
      this.modalVisible = true
      if (this.value) {
        this.oldValue = cloneDeep(this.value)
        this.tempValue = cloneDeep(this.value)
      }
    },
    checkboxChange (tempValue, row, checked) {
      this.tempValue = tempValue
      const oldIndex = this.selectedOptions.findIndex(x => x.value === row[this.replaceFields.value])
      if (checked && oldIndex === -1) {
        this.selectedOptions.push({
          label: row[this.replaceFields.title],
          value: row[this.replaceFields.value],
          uid: row.uid
        })
      }
      if (!checked && oldIndex !== -1) {
        this.selectedOptions.splice(oldIndex, oldIndex)
      }
    },
    radioChange (tempValue, row) {
      this.tempValue = tempValue
      this.selectedOptions = [{
        label: row[this.replaceFields.title],
        value: row[this.replaceFields.value],
        uid: row.uid
      }]
    },
    handleSelect (value) {
      if (this.multiple && !value.length) {
        this.$emit('input', [])
      } else if (!this.multiple && !value) {
        this.$emit('input', '')
      }
    },
    handleCancel () {
      this.$emit('input', cloneDeep(this.oldValue))
    },
    handleOk () {
      this.modalVisible = false
      if (this.tempValue) {
        this.$emit('input', this.tempValue)
      }
    }
  }
}
</script>

<style>
</style>
