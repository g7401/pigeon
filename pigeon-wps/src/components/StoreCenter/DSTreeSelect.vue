<template>
  <CacheTreeSelect v-bind="$attrs" v-on="$listeners"></CacheTreeSelect>
</template>

<script>
import CacheTreeSelect from '@/components/StoreCenter/CacheTreeSelect'
export default {
  components: {
    CacheTreeSelect
  }
}
</script>
