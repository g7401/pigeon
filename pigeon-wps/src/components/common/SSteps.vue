<template>
  <div class="s-steps">
    <a-row
      class="steps-action"
      type="flex"
      justify="end"
    >
      <a-button
        class="mg-r-5"
        :disabled="disabledLast"
        @click="goLast"
      >上一步</a-button>
      <a-button
        :disabled="disabledNext"
        type="primary"
        @click="goNext"
      >下一步</a-button>
    </a-row>
    <div class="step-box">
      <a-steps
        class="mg-tb-20"
        :current="value"
        :status="currentStep.status"
      >
        <a-step
          v-for="(step, index) in steps"
          :title="step.title"
          :key="step.title"
          :status="step.status"
          :disabled="isStepDisabled(index)"
        ></a-step>
      </a-steps>
      <div
        class="mg-tb-10 btn-gap"
        v-if="visiableButtons.length"
      >
        <a-button
          v-for="button in visiableButtons"
          @click="handleButtonClick(button)"
          :key="button.key"
          :type="button.type || 'primary'"
          :ghost="button.ghost"
          :disabled="button.disabled"
          :loading="button.loading"
        >{{ button.label }}</a-button>
      </div>
      <a-card
        class="step-content"
        size="small"
        :bordered="false"
      >
        <slot
          :name="steps[value].key || value"
          :selfStep="steps[value]"
        ></slot>
      </a-card>
    </div>
  </div>
</template>

<script>
// status wait process finish error
import cloneDeep from 'lodash.clonedeep'
export default {
  props: {
    steps: {
      type: Array,
      default () {
        return []
      }
    },
    initSteps: {
      type: Array,
      default () {
        return []
      }
    },
    opButtons: {
      type: Array,
      default () {
        return []
      }
    },
    value: {
      type: Number,
      default: 0
    }
  },
  data () {
    return {
      oldSteps: null,
      tempSteps: null
    }
  },
  watch: {
    steps: {
      handler (newData) {
        const changeStepIndex = newData.findIndex(
          (newStep, index) => this.oldSteps && this.oldSteps[index].status === 'finish' && newStep.status !== this.oldSteps[index].status
        )
        if (changeStepIndex !== -1 && this.lastChangeStepIndex !== -1 && this.lastChangeStepIndex > changeStepIndex) {
          this.steps.forEach((step, index) => {
            if (index > changeStepIndex && index <= this.lastChangeStepIndex) {
              Object.assign(step, cloneDeep(this.tempSteps[index]))
              this.$emit('clearStatus', index)
            }
          })
        }
        this.oldSteps = cloneDeep(newData)
      },
      deep: true
    },
    'lastStep.status': {
      handler (newVal) {
        // this.$emit('lastStepChange', this.lastStep)
      }
    }
  },
  computed: {
    lastStep () {
      if (this.lastChangeStepIndex === -1) return {}
      return this.steps[this.lastChangeStepIndex]
    },
    lastChangeStepIndex () {
      const index = this.steps
        .slice()
        .reverse()
        .findIndex(step => step.status !== 'wait')
      return index !== -1 ? this.steps.length - 1 - index : index
    },
    visiableButtons () {
      return this.opButtons.filter(button => !button.show || button.show(this.currentStep))
    },
    currentStep () {
      return this.steps[this.value]
    },
    disabledLast () {
      return this.value === 0 || this.isStepDisabled(this.value - 1)
    },
    disabledNext () {
      return this.value === this.steps.length - 1 || this.isStepDisabled(this.value + 1)
    }
  },
  created () {
    this.init()
  },
  methods: {
    resetStep (step) {
      const stepIndex = this.steps.findIndex(x => x.key === step.key)
      const originStep = cloneDeep(this.tempSteps[stepIndex])
      Object.assign(step, originStep)
    },
    confirmReset (fn) {
      this.$confirm({
        title: '警告',
        content: `操作后会导致后面步骤重置，确定要操作吗?`,
        okText: '确定',
        okType: 'danger',
        cancelText: '取消',
        onOk () {
          fn()
        },
        onCancel () {
          console.log('Cancel')
        }
      })
    },
    init () {
      this.tempSteps = cloneDeep(this.initSteps)
    },
    isStepDisabled (current) {
      const lastStep = this.steps[current - 1]
      return lastStep && lastStep.status !== 'finish'
    },
    stepChange (current) {
      if (this.isStepDisabled(current)) return
      this.$emit('input', current)
    },
    goNext () {
      this.stepChange(this.value + 1)
    },
    goLast () {
      this.stepChange(this.value - 1)
    },
    handleButtonClick (button) {
      const step = this.steps[this.value]
      const emitClickFn = () => {
        console.log('click button: ', button.key)
        if (button.key === 'resetStep') {
          this.resetStep(step)
        }
        this.$emit('handleOperationClick', button, step)
      }
      if (this.lastChangeStepIndex !== -1 && this.value < this.lastChangeStepIndex && button.isConfirm) {
        this.confirmReset(emitClickFn)
      } else {
        emitClickFn()
      }
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .ant-steps-item-active .ant-steps-item-icon {
  border: 3px solid #40a9ff;
}
/deep/ .ant-steps-item-wait .ant-steps-item-icon {
  color: #40a9ff;
}
/deep/ .ant-steps-item-wait.ant-steps-item-active .ant-steps-item-icon {
  background-color: #e6f7ff;
}
</style>
