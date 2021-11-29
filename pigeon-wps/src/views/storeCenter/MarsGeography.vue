<template>
  <HierarchyTable
    treeApiName="getMarsGeography"
    :tables="tables"
  ></HierarchyTable>
</template>

<script>
import HierarchyTable from '@/components/StoreCenter/HierarchyTable.vue'
export default {
  name: 'MarsGeography',
  components: { HierarchyTable },
  data () {
    return {
      tables: [
        {
          queryApiName: 'getMarsGeograogyStores',
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
              title: '大区名称',
              dataIndex: 'large_region_name'
            },
            {
              title: '大区编码',
              dataIndex: 'large_region_code'
            },
            {
              title: '省份名称',
              dataIndex: 'province_name'
            },
            {
              title: '省份编码',
              dataIndex: 'province_code'
            },
            {
              title: '城市群名称',
              dataIndex: 'city_group_name'
            },
            {
              title: '城市群编码',
              dataIndex: 'city_group_code'
            },
            {
              title: '地级市名称',
              dataIndex: 'city_name'
            },
            {
              title: '地级市编码',
              dataIndex: 'city_code'
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
