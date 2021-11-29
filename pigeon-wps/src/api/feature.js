export const api = {
  subscriptionTables: '/lotus-fss/feature/store/subscription_management'
}

const config = {
  getSubscriptionTables: {
    url: api.subscriptionTables,
    method: 'get'
  },
  saveSubscritionTables: {
    url: api.subscriptionTables,
    method: 'post'
  }
}

export default config
