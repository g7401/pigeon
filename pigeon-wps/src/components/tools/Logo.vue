<template>
  <div class="logo">
    <router-link :to="{name: iconRouteName}">
      <img src="@/assets/logo.png" :class="{'mg-l-10': showTitle}" alt="mars">
      <h1 class="logo-title" v-if="showTitle">{{ title }}</h1>
    </router-link>
  </div>
</template>

<script>
import LogoSvg from '@/assets/logo.svg?inline'
import { mapGetters } from 'vuex'
export default {
  name: 'Logo',
  components: {
    LogoSvg
  },
  props: {
    title: {
      type: String,
      default: process.env.VUE_APP_NAME,
      required: false
    },
    showTitle: {
      type: Boolean,
      default: true,
      required: false
    }
  },
  computed: {
    ...mapGetters(['apiType']),
    iconRouteName () {
      return this.apiType === 'openapi' ? 'OpenApiTable' : 'home'
    }
  }
}
</script>

<style lang="less" scoped>
.logo-title {
  margin-bottom: 0;
}
/deep/ .router-link-active {
  display: flex;
  align-items: center;
  height: 100%;
}
</style>
