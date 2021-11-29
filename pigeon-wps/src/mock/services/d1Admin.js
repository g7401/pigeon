export const getResourceStructures = [
  {
    uid: 100,
    name: '玛氏地理',
    description: '玛氏地理Hierarchy结构',
    type: 'category',
    level: 1,
    children: [
      {
        uid: 10001,
        name: '大区',
        description: '大区',
        type: 'region',
        level: 2,
        children: [
          {
            uid: 10002,
            name: '省份',
            description: '省份',
            type: 'province',
            level: 3,
            children: [
              {
                uid: 10003,
                name: '城市群',
                description: '城市群',
                type: 'cityGroup',
                level: 4,
                children: [
                  {
                    uid: 10004,
                    name: '城市',
                    description: '城市',
                    type: 'city',
                    level: 5,
                    children: null
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
]

export const getDataSourceList = [
  ...Array.from({ length: 30 }).map((v, i) => ({
    label: `fake_server${i + 1}`,
    level: 1,
    id: i + 1,
    type: 'server',
    children: [
      {
        label: 'fake database',
        type: 'database',
        level: 2,
        children: [
          {
            label: 'fake schema',
            type: 'schema',
            level: 3,
            children: [
              {
                label: 'fake table',
                type: 'table',
                level: 4,
                children: [
                  {
                    label: 'fake_table_dfk',
                    id: 1,
                    type: 'dfk'
                  }
                ]
              },
              {
                label: 'fake table2',
                type: 'table',
                level: 4,
                children: []
              }
            ]
          },
          {
            label: 'fake schema3',
            type: 'schema',
            level: 3
          }
        ]
      }
    ]
  })),
  {
    label: 'mysql_fake_server',
    level: 1,
    id: 2,
    type: 'server',
    children: [
      {
        label: 'mysql fake database',
        type: 'database',
        level: 2,
        children: [
          {
            label: 'mysql fake table',
            type: 'table',
            level: 4,
            children: [
              {
                label: 'mysql_fake_table_dfk',
                id: 3,
                type: 'dfk'
              }
            ]
          },
          {
            label: 'mysql fake table2',
            type: 'table',
            level: 4,
            children: []
          }
        ]
      }
    ]
  }
]

// export const getDomainAndItemTree = [
//   {
//     name: 'Domain name1',
//     id: 1,
//     type: 'domain',
//     children: [
//       {
//         name: 'item name1.1',
//         id: 1,
//         domain_id: 1,
//         children: [
//           {
//             name: 'item name1.1.1',
//             id: 2,
//             domain_id: 1,
//             children: [
//               {
//                 name: 'item name1.1.1.1',
//                 id: 3,
//                 domain_id: 1
//               }
//             ]
//           }
//         ]
//       }
//     ]
//   },
//   {
//     name: 'Domain name2',
//     type: 'domain',
//     id: 2
//   }
// ]

// export const getDomainAndValueTree = [
//   {
//     name: 'Domain name1',
//     type: 'domain',
//     id: 1,
//     children: [
//       {
//         name: 'item value1.1',
//         id: 1,
//         domain_id: 1,
//         structure_id: 1,
//         children: [
//           {
//             name: 'item value1.1.1',
//             id: 2,
//             domain_id: 1,
//             structure_id: 2,
//             parent_id: 1,
//             children: [
//               {
//                 name: 'item value1.1.1.1',
//                 id: 4,
//                 domain_id: 1,
//                 structure_id: 3,
//                 parent_id: 2
//               }
//             ]
//           },
//           {
//             name: 'item value1.1.2',
//             id: 3,
//             domain_id: 1,
//             structure_id: 2,
//             parent_id: 1
//           }
//         ]
//       },
//       {
//         name: 'item value11.2',
//         id: 5,
//         domain_id: 1,
//         structure_id: 1
//       }
//     ]
//   },
//   {
//     name: 'Domain name2',
//     type: 'domain',
//     id: 2
//   }
// ]

export const testDictStrategy = [
  {
    name: 'item value1.1',
    id: 1,
    domain_id: 1,
    structure_id: 1,
    children: [
      {
        name: 'item value1.1.1',
        id: 2,
        domain_id: 1,
        structure_id: 2,
        parent_id: 1,
        children: [
          {
            name: 'item value1.1.1.1',
            id: 4,
            domain_id: 1,
            structure_id: 3,
            parent_id: 2
          }
        ]
      },
      {
        name: 'item value1.1.2',
        id: 3,
        domain_id: 1,
        structure_id: 2,
        parent_id: 1
      }
    ]
  },
  {
    name: 'item value11.2',
    id: 5,
    domain_id: 1,
    structure_id: 1
  }
]

// export const getFormTableSetting =

export const getDataFacetTags = ['tag1', 'tag2']

export const getJobFaildReson = 'failed reson is: &￥&%&￥&￥……%￥……%&……%*%￥……￥……￥……￥%……%￥&%&%&%&……%&……%&……%'
