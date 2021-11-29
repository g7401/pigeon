import { getQueryParameters } from '../util'

export const createPlanInstance = () => {
  return {
    success: true,
    object: {
      plan_instance_id: 'test'
    }
  }
}

export const getSalemanList = () => {
  return {
    success: true,
    object: [
      {
        label: '张三',
        value: 'zhangsan'
      },
      {
        label: '李四',
        value: 'lisi'
      },
      {
        label: '王五',
        value: 'wangwu'
      }
    ]
  }
}

export const getDealerCascade = () => {
  return {
    success: true,
    object: [
      {
        value: '1',
        label: '业务西区',
        children: [
          {
            value: 1,
            label: '四川省',
            children: [
              {
                value: 1,
                label: '成都',
                children: [
                  {
                    value: 1,
                    label: '成都市',
                    children: [
                      { value: 1, label: '郫都区万祺食品经营部' },
                      { value: 2, label: '邛崃市友邻合益兴商贸有限公司' },
                      { value: 3, label: '红心和食品经营部' },
                      { value: 4, label: '崇州市崇阳睿驰小食品经营部' },
                      { value: 5, label: '成都圣运商贸有限公司' },
                      { value: 6, label: '温江兴温泉副食经营部' },
                      { value: 7, label: '成都宸晨丰汇商贸有限公司' },
                      { value: 8, label: '都江堰市大伟食品经营部' },
                      { value: 9, label: '成都心连鑫贸易有限公司' },
                      { value: 10, label: '雪峰食品配送中心' },
                      { value: 11, label: '成都道为商贸有限公司' }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      },
      {
        value: '2',
        label: '大区1',
        children: [
          {
            value: '1',
            label: '省份1',
            children: [
              {
                value: '1',
                label: '城市群1',
                children: [
                  {
                    value: '1',
                    label: '地级市1',
                    children: [
                      {
                        value: '1',
                        label: '经销商1',
                        children: []
                      },
                      {
                        value: '2',
                        label: '经销商2'
                      }
                    ]
                  },
                  {
                    value: '2',
                    label: '地级市2'
                  }
                ]
              },
              {
                value: '2',
                label: '城市群2'
              }
            ]
          },
          {
            value: '2',
            label: '省份2'
          }
        ]
      }
    ]
  }
}

export const getMapStoreList = (options) => {
  const markDataMap = {
    area: [
      {
        store_code: '53460634',
        store_name: '好嘟嘟饼屋',
        old_store_area: '1',
        new_store_area: '2',
        longitude: '104.0785709',
        latitude: '30.68624792'
      },
      {
        store_code: '53460637',
        store_name: '心怡干杂',
        old_store_area: '5',
        new_store_area: '4',
        longitude: '104.0861089',
        latitude: '30.68595419'
      },
      {
        store_code: '53460631',
        store_name: '彭士博干杂',
        old_store_area: '2',
        new_store_area: '5',
        longitude: '104.0874346',
        latitude: '30.68535579'
      }
    ],
    eight_week: [
      {
        store_code: '53460634',
        store_name: '好嘟嘟饼屋',
        old_week_number: '1',
        new_week_number: '1',
        longitude: '104.0785709',
        latitude: '30.68624792'
      },
      {
        store_code: '534612312',
        store_name: '好嘟嘟饼屋',
        old_week_number: '4',
        new_week_number: '4',
        longitude: '104.0785710',
        latitude: '30.68624795'
      },
      {
        store_code: '53460637',
        store_name: '心怡干杂',
        old_week_number: '3',
        new_week_number: '6',
        longitude: '104.0861089',
        latitude: '30.68595419'
      },
      {
        store_code: '53460631',
        store_name: '彭士博干杂',
        old_week_number: '1',
        new_week_number: '2',
        longitude: '104.0874346',
        latitude: '30.68535579'
      }
    ],
    per_week: [
      {
        store_code: '53460634',
        store_name: '好嘟嘟饼屋',
        old_day_number: '4',
        new_day_number: '12',
        longitude: '104.0785709',
        latitude: '30.68624792'
      },
      {
        store_code: '53460637',
        store_name: '心怡干杂',
        old_day_number: '3',
        new_day_number: '6',
        longitude: '104.0861089',
        latitude: '30.68595419'
      },
      {
        store_code: '53460631',
        store_name: '彭士博干杂',
        old_day_number: '1',
        new_day_number: '2',
        longitude: '104.0874346',
        latitude: '30.68535579'
      }
    ],
    per_day: [
      {
        store_code: '53460634',
        store_name: '好嘟嘟饼屋',
        old_route_number: '4',
        new_route_number: '1',
        longitude: '104.0785709',
        latitude: '30.68624792'
      },
      {
        store_code: '53460637',
        store_name: '心怡干杂',
        old_route_number: '3',
        new_route_number: '3',
        longitude: '104.0861089',
        latitude: '30.68595419'
      },
      {
        store_code: '53460631',
        store_name: '彭士博干杂',
        old_route_number: '1',
        new_route_number: '2',
        longitude: '104.0874346',
        latitude: '30.68535579'
      }
    ]
  }
  const queryParameters = getQueryParameters(options)
  return {
    success: true,
    object: markDataMap[queryParameters.type]
  }
}

export const getPlanInstance = () => {
  return {
    'success': true,
    'object': {
        'last_step': 'attr',
        'step_status': 'process',
        'base_attr': {
            'dealer': ['2', '1', '1', '1', '1'],
            'start_date': '2020-10-01',
            'end_date': '2020-12-01',
            'work_days': ['2020-10-01', '2020-10-02', '2020-10-03']
        },
        'optimize_attr': {
            'visit_way': 'walk',
            'optimize_type': 'work_load',
            'visit_time_range': [10, 80],
            'visit_store_num_range': [5, 8],
            'target_saleman_num_mode': 'update_num',
            'target_saleman_num': 10
        }
    }
}
}

export const confirmPlan = () => {
  return {
    success: true
  }
}

export const savePlanAttrs = () => {
  return {
    success: true
  }
}

export const saveLastStep = () => {
  return {
    success: true
  }
}

export const startCompute = {
  success: true,
  object: {
    step_task_id: '123456'
  }
}

export const getComputeStatus = () => {
  return {
    success: true,
    object: {
      status: 'finish'
    }
  }
}
