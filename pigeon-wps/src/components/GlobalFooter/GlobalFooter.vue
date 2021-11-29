<template>
  <div class="footer">
    <div class="links">
      <a
        :href="pageInfo.terms_of_service_url"
        target="_blank"
      >Terms of Service</a>
      <a
        :href="pageInfo.privacy_policy_url"
        target="_blank"
      >Privacy Policy</a>
    </div>
    <div class="copyright">
      {{ pageInfo.release_version }}
      <a-icon type="copyright" /> <span>{{ pageInfo.vendor_name }}</span>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
export default {
  name: 'GlobalFooter',
  data () {
    return {}
  },
  computed: {
    ...mapGetters(['pageInfo'])
  },
  created () {
    this.$store.dispatch('GetPageInfo')
  }
}
</script>

<style lang="less" scoped>
.footer {
  padding: 0 16px;
  margin: 48px 0 24px;
  text-align: center;

  .links {
    margin-bottom: 8px;

    a {
      color: rgba(0, 0, 0, 0.45);

      &:hover {
        color: rgba(0, 0, 0, 0.65);
      }

      &:not(:last-child) {
        margin-right: 20px;
      }
    }
  }
  .copyright {
    color: rgba(0, 0, 0, 0.45);
    font-size: 14px;
  }
}
</style>
