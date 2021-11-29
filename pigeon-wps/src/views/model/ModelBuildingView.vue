<template>
  <div class="model-building-view">
    <a-tabs v-model="activeTab">
      <a-tab-pane
        v-for="tab in tabs"
        :tab="tab.tab"
        :key="tab.key"
      >
        <component :is="tab.key"></component>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
import ModelTypeView from '@/views/model/ModelTypeView'
import ModelManagement from '@/views/model/ModelManagement'
export default {
  components: { modeltype: ModelTypeView, modelmanagement: ModelManagement },
  name: 'ModelBuilding',
  data () {
    return {
      activeTab: 'modeltype',
      tabs: [
        {
          tab: 'Building Model Type',
          key: 'modeltype'
        },
        {
          tab: 'Building Model',
          key: 'modelmanagement'
        }
      ]
    }
  },
  props: {
    type: {
      type: String,
      default: 'modeltype'
    }
  },
  watch: {
    type: {
      handler (value) {
        console.log('test', value)
        this.activeTab = value
      },
      immediate: true
    }
  }
}
</script>

<style>
</style>
