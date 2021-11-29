<template>
  <HierarchyTable
    treeApiName="getNationalGeography"
    treeByNameApiName="getNationalGeographyByName"
    :isLoad="true"
    :tables="tables"
  ></HierarchyTable>
</template>

<script>
import HierarchyTable from '@/components/StoreCenter/HierarchyTable.vue'
export default {
  name: 'NationalGeography',
  components: { HierarchyTable },
  data () {
    return {
      tables: [
        {
          queryApiName: 'getGeograogyStores',
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
              title: '省名称',
              dataIndex: 'province_name'
            },
            {
              title: '省编码',
              dataIndex: 'province_code'
            },
            {
              title: '地级市名称',
              dataIndex: 'city_name'
            },
            {
              title: '地级市编码',
              dataIndex: 'city_code'
            },
            {
              title: '区县名称',
              dataIndex: 'district_name'
            },
            {
              title: '区县编码',
              dataIndex: 'district_code'
            },
            {
              title: '乡镇街道名称',
              dataIndex: 'township_name'
            },
            {
              title: '乡镇街道编码',
              dataIndex: 'township_code'
            },
            {
              title: '等级分类1',
              dataIndex: 'level_cat_1'
            },
            {
              title: '等级分类2',
              dataIndex: 'level_cat_2'
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
