<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <RelationGraph class="mg-b-10" :nodes="nodes" :edges="edges"></RelationGraph>
    <a-tabs
      v-model="activeTab"
    >
      <a-tab-pane
        v-for="tab in tabs"
        :key="tab.key"
        :tab="tab.tab"
      >
        <component v-if="activeTab === tab.key" :is="tab.key + 'Config'" :epProcessDefUid="epProcessDefUid" @saved="handleSaved"></component>
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script>
import PrimarySourceConfig from '@/components/StoreCenter/PrimarySourceConfig'
import SecondarySourceConfig from '@/components/StoreCenter/SecondarySourceConfig'
import EbScheduleConfig from '@/components/StoreCenter/EbScheduleConfig'
import AttributeMappingConfig from '@/components/StoreCenter/AttributeMappingConfig'
import RelationGraph from '@/components/common/RelationGraph'

export default {
  name: 'AttributeConfig',
  components: { PrimarySourceConfig, SecondarySourceConfig, EbScheduleConfig, AttributeMappingConfig, RelationGraph },
  props: {
    epProcessDefUid: {
      type: String,
      default: undefined
    }
  },
  data () {
    return {
      activeTab: 'PrimarySource',
      tabs: [
        {
          tab: 'Primary Source & Identity Attribute',
          key: 'PrimarySource'
        },
        {
          tab: 'Secondary Source & Identity Attribute',
          key: 'SecondarySource'
        },
        {
          tab: 'Attribute Mapping',
          key: 'AttributeMapping'
        },
        {
          tab: 'Schedule',
          key: 'EbSchedule'
        }
      ],
      nodes: [],
      edges: []
    }
  },
  created () {
    this.loadGraphData()
  },
  methods: {
    async loadGraphData () {
      const resp = await this.$api.getEbProcessDefImg(this.epProcessDefUid)
      if (!resp || !resp.object) return
      const { nodes, edges } = resp.object
      this.nodes = nodes
      this.edges = edges
    },
    handleSaved () {
      this.loadGraphData()
    }
  }
}
</script>

<style>
</style>
