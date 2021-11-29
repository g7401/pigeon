<template>
  <CacheTreeSelect v-bind="$attrs" v-on="$listeners" treeName="dtTree" actionName="getDataTargetTree"></CacheTreeSelect>
</template>

<script>
import CacheTreeSelect from '@/components/StoreCenter/CacheTreeSelect'
export default {
  components: {
    CacheTreeSelect
  }
}
</script>
