export const getEbPrimarySrc = {
  err_code: 'string',
  err_message: 'string',
  is_success: true,
  object: {
    avail_primary_ds_and_attrs: [
      {
        children: [
          {
            name: 'name1',
            uid: 'uid_1'
          },
          {
            name: 'name2',
            uid: 'uid_2'
          }
        ],
        name: 'ds 1',
        uid: 'ds_uid_1'
      },
      {
        children: [
          {
            name: 'name3',
            uid: 'uid_3'
          },
          {
            name: 'name4',
            uid: 'uid_4'
          }
        ],
        name: 'ds 2',
        uid: 'ds_uid_2'
      }
    ],
    eb_process_def_uid: 'uid_555',
    list_of_identity_attr_uid_of_primary_ds: ['uid_3'],
    primary_ds_uid: 'ds_uid_2'
  },
  success: true
}

export const getEbSecondarySrc = {
  err_code: 'string',
  err_message: 'string',
  is_success: true,
  object: {
    available_attrs_of_primary_ds: [
      {
        name: 'primary_ds_attr1',
        uid: 'primary_ds_attr_uid1'
      },
      {
        name: 'primary_ds_attr2',
        uid: 'primary_ds_attr_uid2'
      }
    ],
    primary_ds_uid: 'string',
    secondary_ds_ias: [
      {
        secondary_ds_name: 'ds 1',
        secondary_ds_uid: 'secondary_ds_uid_1',
        available_attrs_of_secondary_ds: [
          {
            name: 'secondary attr1',
            uid: 'attr_uid1'
          },
          {
            name: 'secondary attr2',
            uid: 'attr_uid2'
          }
        ],
        mapping_units: [
          {
            attr_uid_of_secondary_ds: 'attr_uid2',
            mapping_to_attr_uid_of_primary_ds: 'primary_ds_attr_uid2'
          }
        ]
      },
      {
        secondary_ds_name: 'ds 2',
        secondary_ds_uid: 'secondary_ds_uid_2',
        available_attrs_of_secondary_ds: [
          {
            name: 'secondary attr3',
            uid: 'attr_uid3'
          },
          {
            name: 'secondary attr4',
            uid: 'attr_uid4'
          }
        ],
        mapping_units: []
      }
    ]
  },
  success: true
}

export const getEbSchedule = {
  err_code: 'string',
  err_message: 'string',
  is_success: true,
  object: {
    eb_process_def_uid: 'string',
    enabled: true,
    schedule_type: 'ADHOC',
    schedule_type_ext_details: 'cron expressions if PERIODIC'
  },
  success: true
}

export const getTargetAttrsConfigs = {
  err_code: 'string',
  err_message: 'string',
  is_success: true,
  object: [
    {
      attr_build_strategy: 'DIRECT_COPY',
      attr_mapping_type: 'ONE_TO_ONE',
      configured: true,
      eb_process_def_uid: 'string',
      eb_task_def_uid: 'string',
      target_attr_name: 'attr name 1',
      target_attr_uid: 'attr_uid_1'
    },
    {
      attr_build_strategy: 'DIRECT_COPY',
      attr_mapping_type: 'MANY_TO_MANY',
      configured: true,
      eb_process_def_uid: 'eb_process_def_uid1',
      eb_task_def_uid: 'eb_task_def_uid1',
      target_attr_name: 'attr name 2',
      target_attr_uid: 'attr_uid_2'
    },
    {
      attr_build_strategy: 'SINGLE_SOURCE_OF_TRUTH',
      attr_mapping_type: 'MANY_TO_MANY',
      configured: false,
      eb_process_def_uid: 'eb_process_def_uid1',
      eb_task_def_uid: 'eb_task_def_uid1',
      target_attr_name: 'attr name 3',
      target_attr_uid: 'attr_uid_3'
    }
  ],
  success: true
}

export const getEbTask = {
  err_code: 'string',
  err_message: 'string',
  is_success: true,
  object: {
    attr_build_strategy: 'DIRECT_COPY',
    eb_task_def_uid: 'eb_task_def_uid1',
    processor_data_props: '{}',
    processor_static_data_props: '{}',
    processor_uid: 'processor_uid1',
    target_attr_uid: 'target_attr_uid1'
  },
  success: true
}

export const getEbTaskSrcAttrs = function () {
  return {
    err_code: 'string',
    err_message: 'string',
    is_success: true,
    object: [
      {
        create_by: 'string',
        create_time: '2020-09-04T10:23:41.929Z',
        ds_name: 'data source1',
        ds_uid: 'string',
        entity_uid: 'string',
        last_update_by: 'string',
        last_update_time: '2020-09-04T10:23:41.929Z',
        mapping_created_version: 'string',
        mapping_eliminated_version: 'string',
        mapping_status: 'NORMAL',
        src_attr_created_version: 'string',
        src_attr_description: 'string',
        src_attr_eliminated_version: 'string',
        src_attr_name: 'srouce attr name1',
        src_attr_status: 'NORMAL',
        src_attr_uid: 'string',
        target_attr_build_type: 'BUILT_FROM_ZERO',
        target_attr_created_version: 'string',
        target_attr_description: 'string',
        target_attr_eliminated_version: 'string',
        target_attr_name: 'string',
        target_attr_status: 'NORMAL',
        target_attr_uid: 'string'
      },
      {
        create_by: 'string',
        create_time: '2020-09-04T10:23:41.929Z',
        ds_name: 'data source2',
        ds_uid: 'string',
        entity_uid: 'string',
        last_update_by: 'string',
        last_update_time: '2020-09-04T10:23:41.929Z',
        mapping_created_version: 'string',
        mapping_eliminated_version: 'string',
        mapping_status: 'NORMAL',
        src_attr_created_version: 'string',
        src_attr_description: 'string',
        src_attr_eliminated_version: 'string',
        src_attr_name: 'srouce attr name2',
        src_attr_status: 'NORMAL',
        src_attr_uid: 'string',
        target_attr_build_type: 'BUILT_FROM_ZERO',
        target_attr_created_version: 'string',
        target_attr_description: 'string',
        target_attr_eliminated_version: 'string',
        target_attr_name: 'string',
        target_attr_status: 'NORMAL',
        target_attr_uid: 'string'
      }
    ],
    success: true
  }
}

export const getEbTaskTargetAttrs = function () {
  return {
    err_code: 'string',
    err_message: 'string',
    is_success: true,
    object: [
      {
        create_by: 'string',
        create_time: '2020-09-04T10:26:41.074Z',
        entity_uid: 'string',
        last_update_by: 'string',
        last_update_time: '2020-09-04T10:26:41.074Z',
        target_attr_build_type: 'BUILT_FROM_ZERO',
        target_attr_created_version: 'string',
        target_attr_description: 'string',
        target_attr_eliminated_version: 'string',
        target_attr_name: 'target attr name1',
        target_attr_status: 'NORMAL',
        target_attr_uid: 'string'
      },
      {
        create_by: 'string',
        create_time: '2020-09-04T10:26:41.074Z',
        entity_uid: 'string',
        last_update_by: 'string',
        last_update_time: '2020-09-04T10:26:41.074Z',
        target_attr_build_type: 'BUILT_FROM_ZERO',
        target_attr_created_version: 'string',
        target_attr_description: 'string',
        target_attr_eliminated_version: 'string',
        target_attr_name: 'target attr name2',
        target_attr_status: 'NORMAL',
        target_attr_uid: 'string'
      }
    ],
    success: true
  }
}

export const getEntityQueryData = {
  object: {
    total: 20,
    content: [
      {
        attr_100: '1',
        attr_101: '5',
        attr_102: '9',
        attr_103: '13'
      },
      {
        attr_100: '2',
        attr_101: '6',
        attr_102: '10',
        attr_103: '14'
      },
      {
        attr_100: '3',
        attr_101: '7',
        attr_102: '11',
        attr_103: '15'
      },
      {
        attr_100: '4',
        attr_101: '8',
        attr_102: '12',
        attr_103: '16'
      },
      {
        attr_100: 'a',
        attr_101: 'A',
        attr_102: 'a1',
        attr_103: 'A1'
      },
      {
        attr_100: 'b',
        attr_101: 'B',
        attr_102: 'b1',
        attr_103: 'B1'
      },
      {
        attr_100: 'c',
        attr_101: 'C',
        attr_102: 'c1',
        attr_103: 'C1'
      },
      {
        attr_100: 'd',
        attr_101: 'D',
        attr_102: 'd1',
        attr_103: 'D1'
      },
      {
        attr_100: 'e',
        attr_101: 'E',
        attr_102: 'e1',
        attr_103: 'E1'
      },
      {
        attr_100: 'f',
        attr_101: 'F',
        attr_102: 'f1',
        attr_103: 'F1'
      },
      {
        attr_100: 'g',
        attr_101: 'G',
        attr_102: 'g1',
        attr_103: 'G1'
      },
      {
        attr_100: 'h',
        attr_101: 'H',
        attr_102: 'h1',
        attr_103: 'H1'
      },
      {
        attr_100: 'i',
        attr_101: 'I',
        attr_102: 'i1',
        attr_103: 'I1'
      },
      {
        attr_100: 'j',
        attr_101: 'J',
        attr_102: 'j1',
        attr_103: 'J1'
      },
      {
        attr_100: 'k',
        attr_101: 'K',
        attr_102: 'k1',
        attr_103: 'K1'
      },
      {
        attr_100: 'l',
        attr_101: 'L',
        attr_102: 'l1',
        attr_103: 'L1'
      },
      {
        attr_100: 'm',
        attr_101: 'M',
        attr_102: 'm1',
        attr_103: 'M1'
      },
      {
        attr_100: 'n',
        attr_101: 'N',
        attr_102: 'n1',
        attr_103: 'N1'
      },
      {
        attr_100: 'o',
        attr_101: 'O',
        attr_102: 'o1',
        attr_103: 'O1'
      },
      {
        attr_100: 'p',
        attr_101: 'P',
        attr_102: 'p1',
        attr_103: 'P1'
      }
    ],
    count_page: 1,
    size: 20,
    current_page: 0
  },
  success: true,
  is_success: true,
  err_code: null,
  err_message: null
}

export const getEntityQuerySetting = {
  object: {
    form: [
      {
        uid: 'attr_100',
        name: 'store_code',
        description: '门店编码',
        query_filter_type: 'PRECISE_MATCH',
        query: true,
        list: true
      },
      {
        uid: 'attr_101',
        name: 'store_id',
        description: '门店ID',
        query_filter_type: 'RANGE_MATCH',
        query: true,
        list: true
      },
      {
        uid: 'attr_103',
        name: 'store_description',
        description: '门店描述',
        query_filter_type: 'FUZZY_MATCH',
        query: true,
        list: true
      }
    ],
    table: [
      {
        uid: 'attr_100',
        name: 'store_code',
        description: '门店编码',
        query_filter_type: 'PRECISE_MATCH',
        query: true,
        list: true
      },
      {
        uid: 'attr_101',
        name: 'store_id',
        description: '门店ID',
        query_filter_type: 'RANGE_MATCH',
        query: true,
        list: true
      },
      {
        uid: 'attr_102',
        name: 'store_name',
        description: '门店名称',
        query_filter_type: 'FUZZY_MATCH',
        query: null,
        list: true
      },
      {
        uid: 'attr_103',
        name: 'store_description',
        description: '门店描述',
        query_filter_type: 'FUZZY_MATCH',
        query: true,
        list: true
      }
    ]
  },
  success: true,
  is_success: true,
  err_code: null,
  err_message: null
}

export const getEbExecList = {
  err_code: 'string',
  err_message: 'string',
  is_success: true,
  object: {
    content: [
      {
        create_by: 'string',
        create_time: '2020-09-08T10:03:37.667Z',
        eb_process_def_name: 'string',
        eb_process_def_uid: 'string',
        eb_process_inst_status: 'WAITING',
        eb_process_inst_uid: 'string',
        entity_name: 'string',
        entity_uid: 'string',
        last_update_by: 'string',
        last_update_time: '2020-09-08T10:03:37.667Z'
      }
    ],
    count_page: 0,
    current_page: 0,
    size: 0,
    total: 0
  },
  success: true
}

export const getEbExecutionDetail = {
  err_code: 'string',
  err_message: 'string',
  is_success: true,
  object: {
    create_time: '2020-09-08T10:04:24.784Z',
    df_uid: 'string',
    ds_name: 'string',
    ds_uid: 'string'
  },
  success: true
}

export const getSrcTargetAtrrs = {
  err_code: 'string',
  err_message: 'string',
  is_success: true,
  object: Array.from({ length: 100 }).map((x, i) => ({
    create_by: 'string',
    create_time: '2020-10-29T07:24:12.908Z',
    ds_name: 'string',
    ds_uid: Math.random().toString(),
    entity_uid: 'string',
    last_update_by: 'string',
    last_update_time: '2020-10-29T07:24:12.908Z',
    mapping_created_version: 'string',
    mapping_eliminated_version: 'string',
    mapping_status: 'NORMAL',
    src_attr_created_version: 'string',
    src_attr_description: 'string',
    src_attr_eliminated_version: 'string',
    src_attr_name: 'string',
    src_attr_status: 'NORMAL',
    src_attr_uid: 'string',
    target_attr_build_type: 'BUILT_FROM_ZERO',
    target_attr_created_version: 'string',
    target_attr_description: 'string',
    target_attr_eliminated_version: 'string',
    target_attr_mapping_type: 'ONE_TO_ONE',
    target_attr_name: Math.random().toString(),
    target_attr_status: 'NORMAL',
    target_attr_uid: 'string'
  })),
  success: true
}

export const getUomDsOptions = {
  err_code: 'string',
  err_message: 'string',
  is_success: true,
  object: {
    options: Array.from({ length: 10 }).map((x, i) => ({
      name: Math.random().toString(),
      uid: i
    })),
    ds_uid: 3
  },
  success: true
}
