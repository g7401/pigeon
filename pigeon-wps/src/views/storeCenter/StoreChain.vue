<template>
  <HierarchyTable
    treeApiName="getStoreChain"
    treeByNameApiName="getStoreChainByName"
    :isLoad="true"
    :treeHeight="3"
    :tables="tables"
  ></HierarchyTable>
</template>

<script>
import HierarchyTable from '@/components/StoreCenter/HierarchyTable.vue'
export default {
  name: 'StoreChain',
  components: { HierarchyTable },
  data () {
    return {
      tables: [
        {
          queryApiName: 'getChains',
          columns: [
            {
              title: 'No.',
              dataIndex: 'serial',
              scopedSlots: { customRender: 'serial' }
            },
            {
              title: '客户连锁名称',
              dataIndex: 'chain_name'
            },
            {
              title: '客户连锁编码',
              dataIndex: 'chain_code'
            },
            {
              title: '全国总部名称',
              dataIndex: 'national_headquarters_name'
            },
            {
              title: '全国总部编码',
              dataIndex: 'national_headquarters_code'
            },
            {
              title: '城市总部名称',
              dataIndex: 'city_headquarters_name'
            },
            {
              title: '城市总部编码',
              dataIndex: 'city_headquarters_code'
            },
            {
              title: '渠道名称',
              dataIndex: 'channel_name'
            },
            {
              title: '渠道编码',
              dataIndex: 'channel_code'
            },
            {
              title: 'KA类型名称',
              dataIndex: 'ka_type_name'
            },
            {
              title: 'KA类型编码',
              dataIndex: 'ka_type_code'
            },
            {
              title: '供货类型名称',
              dataIndex: 'supply_type_name'
            },
            {
              title: '供货类型编码',
              dataIndex: 'supply_type_code'
            },
            {
              title: '连锁品牌名称',
              dataIndex: 'chain_brand_name'
            },
            {
              title: '连锁品牌编码',
              dataIndex: 'chain_brand_code'
            },
            {
              title: '箭牌供应商名称',
              dataIndex: 'wrigley_supplier_name'
            },
            {
              title: '箭牌供应商编码',
              dataIndex: 'wrigley_supplier_code'
            },
            {
              title: '巧克力供应商名称',
              dataIndex: 'chocolate_supplier_name'
            },
            {
              title: '巧克力供应商编码',
              dataIndex: 'chocolate_supplier_code'
            }
          ]
        },
        {
          queryApiName: 'getChainStores',
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
              title: '客户连锁名称',
              dataIndex: 'chain_name'
            },
            {
              title: '客户连锁编码',
              dataIndex: 'chain_code'
            },
            {
              title: '全国总部名称',
              dataIndex: 'national_headquarters_name'
            },
            {
              title: '全国总部编码',
              dataIndex: 'national_headquarters_code'
            },
            {
              title: '城市总部名称',
              dataIndex: 'city_headquarters_name'
            },
            {
              title: '城市总部编码',
              dataIndex: 'city_headquarters_code'
            },
            {
              title: '渠道名称',
              dataIndex: 'channel_name'
            },
            {
              title: '渠道编码',
              dataIndex: 'channel_code'
            },
            {
              title: 'KA类型名称',
              dataIndex: 'ka_type_name'
            },
            {
              title: 'KA类型编码',
              dataIndex: 'ka_type_code'
            },
            {
              title: '供货类型名称',
              dataIndex: 'supply_type_name'
            },
            {
              title: '供货类型编码',
              dataIndex: 'supply_type_code'
            },
            {
              title: '连锁品牌名称',
              dataIndex: 'chain_brand_name'
            },
            {
              title: '连锁品牌编码',
              dataIndex: 'chain_brand_code'
            },
            {
              title: '箭牌供应商名称',
              dataIndex: 'wrigley_supplier_name'
            },
            {
              title: '箭牌供应商编码',
              dataIndex: 'wrigley_supplier_code'
            },
            {
              title: '巧克力供应商名称',
              dataIndex: 'chocolate_supplier_name'
            },
            {
              title: '巧克力供应商编码',
              dataIndex: 'chocolate_supplier_code'
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
