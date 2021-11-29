<template>
  <a-card size="small" :bordered="false">
    <CommonTable queryApiName="getSpecificationData"></CommonTable>
  </a-card>
</template>

<script>
import CommonTable from '@/components/StoreCenter/CommonTable.vue'
// import { mapGetters } from 'vuex'
export default {
  name: 'StoreQuery',
  components: { CommonTable },
  data () {
    return {
    }
  },
  created () {
  },
  methods: {
  }
}
</script>

<style>
</style>
