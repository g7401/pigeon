<template>
  <HierarchyTable
    treeApiName="getStoreChannel"
    :tables="tables"
  ></HierarchyTable>
</template>

<script>
import HierarchyTable from '@/components/StoreCenter/HierarchyTable.vue'
export default {
  name: 'StoreChannel',
  components: { HierarchyTable },
  data () {
    return {
      tables: [
        {
          queryApiName: 'getChannelStores',
          formItems: [
            {
              label: '门店ID',
              key: 'store_id'
            },
            {
              label: '门店编码',
              key: 'store_code'
            },
            {
              label: '门店名称',
              key: 'store_name'
            }
          ],
          columns: [
            {
              title: 'No.',
              dataIndex: 'serial',
              scopedSlots: { customRender: 'serial' }
            },
            {
              title: '门店ID',
              dataIndex: 'store_id'
            },
            {
              title: '门店编码',
              dataIndex: 'store_code'
            },
            {
              title: '门店名称',
              dataIndex: 'store_name'
            },
            {
              title: '一级门店类型名称',
              dataIndex: 'channel_name1'
            },
            {
              title: '一级门店类型编码',
              dataIndex: 'channel_code1'
            },
            {
              title: '二级门店类型名称',
              dataIndex: 'channel_name2'
            },
            {
              title: '二级门店类型编码',
              dataIndex: 'channel_code2'
            },
            {
              title: '三级门店类型名称',
              dataIndex: 'channel_name3'
            },
            {
              title: '三级门店类型编码',
              dataIndex: 'channel_code3'
            }
          ]
        }
      ]
    }
  }
}
</script>

<style>
</style>
