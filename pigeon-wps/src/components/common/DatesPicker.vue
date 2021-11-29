<template>
  <div class="dates-picker calendar-box">
    <a-calendar
      v-model="displayDate"
      :defaultValue="now"
      v-bind="$attrs"
      :validRange="$attrs.validRange.length ? $attrs.validRange : placeholderValidRange"
      @select="handleSelectDate"
    >
      <template
        slot="dateCellRender"
        slot-scope="cellValue"
      >
        <div
          class="work-day-mark"
          v-if="isWorkDay(cellValue)"
        ></div>
      </template>
    </a-calendar>
  </div>
</template>

<script>
import moment from 'moment'
import { Calendar } from 'ant-design-vue'
export default {
  name: 'DatesPicker',
  components: { ACalendar: Calendar },
  props: {
    value: {
      type: Array,
      default () {
        return []
      }
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      placeholderValidRange: [moment('1971-01-01'), moment('1971-01-01')],
      now: moment(),
      displayDate: moment()
    }
  },
  watch: {
    '$attrs.validRange': {
      handler (value) {
        if (value && value.length) {
          this.displayDate = value[0]
        }
      },
      immediate: true
    }
  },
  methods: {
    isWorkDay (moment) {
      return this.value.includes(moment.format('YYYY-MM-DD'))
    },
    handleSelectDate (moment) {
      if (this.disabled) return
      const selectedDates = [...this.value]
      const date = moment.format('YYYY-MM-DD')
      const index = selectedDates.indexOf(date)
      if (index !== -1) {
        selectedDates.splice(index, 1)
      } else {
        selectedDates.push(date)
      }
      this.$emit('input', selectedDates)
      this.$emit('change', selectedDates)
    }
  }
}
</script>
