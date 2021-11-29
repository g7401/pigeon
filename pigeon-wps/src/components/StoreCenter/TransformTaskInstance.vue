<template>
  <div class="transform-task-instance">
    <a-steps
      v-model="currentStep"
      type="navigation"
      class="steps-box"
    >
      <a-step
        :status="currentStep === index ? 'process' : 'wait'"
        v-for="(task, index) in taskInstances"
        :key="task.dtr_task_instance_id"
      ></a-step>
    </a-steps>

    <TaskInstanceInfo
      :key="currentTaskInstance.dtr_task_instance_id"
      :taskInstance="currentTaskInstance"
      type="tansform"
      v-if="currentTaskInstance"
    ></TaskInstanceInfo>
  </div>
</template>

<script>
import TaskInstanceInfo from '@/components/DIDefinitions/TaskInstanceInfo'
export default {
  components: { TaskInstanceInfo },
  props: {
    taskInstances: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      currentStep: 0
    }
  },
  computed: {
    currentTaskInstance () {
      return this.taskInstances[this.currentStep]
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .steps-box {
  margin-bottom: 20px;
}
</style>
