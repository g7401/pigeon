export const getUserDataFacets = {
  object: {
    content: Array.from({ length: 20 }).map((v, i) => ({
      name: `Data Facet Name${i + 1}`,
      key: `di_process_definition_dfk${i || ''}`,
      description: 'description '.repeat(i + 1)
    })),
    total: 20
  }
}

export const getFormTableSettings = {
  form: [
    {
      view_field_label: 'Ds Group',
      db_field_name: 'ds_group',
      form_field_query_type: 'EXACT_MATCHING_TEXT',
      field_optional_value_list: null,
      field_cascade_optional_value_list: null,
      field_cascade_child_field_name: null,
      field_value: '',
      form_field_sequence: 2
    },
    {
      view_field_label: 'Ds Name',
      db_field_name: 'ds_name',
      form_field_query_type: 'EXACT_MATCHING_TEXT',
      field_optional_value_list: null,
      field_cascade_optional_value_list: null,
      field_cascade_child_field_name: null,
      field_value: '',
      form_field_sequence: 3
    },
    {
      view_field_label: 'Import Started Time',
      db_field_name: 'import_started_time',
      form_field_query_type: 'EXACT_MATCHING_TEXT',
      field_optional_value_list: null,
      field_cascade_optional_value_list: null,
      field_cascade_child_field_name: null,
      field_value: '',
      form_field_sequence: 7
    }
  ],
  table: [
    {
      view_field_label: 'Ds Group',
      db_field_name: 'ds_group',
      table_field_sequence: 2,
      table_field_column_width: 350,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Ds Name',
      db_field_name: 'ds_name',
      table_field_sequence: 3,
      table_field_column_width: 350,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Di Process Definition Id',
      db_field_name: 'di_process_definition_id',
      table_field_sequence: 4,
      table_field_column_width: 350,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Di Process Instance Id',
      db_field_name: 'di_process_instance_id',
      table_field_sequence: 5,
      table_field_column_width: 350,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Di Process Instance Status',
      db_field_name: 'di_process_instance_status',
      table_field_sequence: 6,
      table_field_column_width: 100,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Import Started Time',
      db_field_name: 'import_started_time',
      table_field_sequence: 7,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Import Done Time',
      db_field_name: 'import_done_time',
      table_field_sequence: 8,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Import Failed Time',
      db_field_name: 'import_failed_time',
      table_field_sequence: 9,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Extract Started Time',
      db_field_name: 'extract_started_time',
      table_field_sequence: 10,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Extract Done Time',
      db_field_name: 'extract_done_time',
      table_field_sequence: 11,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Extract Failed Time',
      db_field_name: 'extract_failed_time',
      table_field_sequence: 12,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Transform Started Time',
      db_field_name: 'transform_started_time',
      table_field_sequence: 13,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Transform Done Time',
      db_field_name: 'transform_done_time',
      table_field_sequence: 14,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Transform Failed Time',
      db_field_name: 'transform_failed_time',
      table_field_sequence: 15,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Publish Started Time',
      db_field_name: 'publish_started_time',
      table_field_sequence: 16,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Publish Done Time',
      db_field_name: 'publish_done_time',
      table_field_sequence: 17,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Publish Failed Time',
      db_field_name: 'publish_failed_time',
      table_field_sequence: 18,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Create Time',
      db_field_name: 'create_time',
      table_field_sequence: 19,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Create By',
      db_field_name: 'create_by',
      table_field_sequence: 20,
      table_field_column_width: 350,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Last Update Time',
      db_field_name: 'last_update_time',
      table_field_sequence: 21,
      table_field_column_width: 150,
      children: null,
      table_field_order_by: 'NONE'
    },
    {
      view_field_label: 'Last Update By',
      db_field_name: 'last_update_by',
      table_field_sequence: 22,
      table_field_column_width: 350,
      children: null,
      table_field_order_by: 'NONE'
    }
  ]
}

export const queryData = {
  err_code: null,
  err_message: null,
  object: {
    total: 117,
    content: [
      {
        id: 7,
        ds_group: 'JD',
        ds_name: 'TEST1',
        di_process_definition_id: 'e3099876-6583-4345-bca1-84cc955ccd1c',
        di_process_instance_id: 'f41fb2b8-a8f6-421e-b843-4a1ec87b34cf',
        di_process_instance_status: 2,
        import_started_time: '2020-05-19 17:38:19',
        import_done_time: '2020-05-19 17:38:19',
        import_failed_time: null,
        extract_started_time: null,
        extract_done_time: null,
        extract_failed_time: null,
        transform_started_time: null,
        transform_done_time: null,
        transform_failed_time: null,
        publish_started_time: null,
        publish_done_time: null,
        publish_failed_time: null,
        create_time: '2020-05-19 16:58:30',
        create_by: null,
        last_update_time: '2020-05-19 17:38:19',
        last_update_by: null,
        is_deleted: null
      },
      {
        id: 6,
        ds_group: 'JD',
        ds_name: 'TEST1',
        di_process_definition_id: 'e3099876-6583-4345-bca1-84cc955ccd1c',
        di_process_instance_id: 'c655b3d4-e01c-48c6-a18f-2d09d8e4d703',
        di_process_instance_status: 2,
        import_started_time: '2020-05-19 17:37:49',
        import_done_time: '2020-05-19 17:37:49',
        import_failed_time: null,
        extract_started_time: null,
        extract_done_time: null,
        extract_failed_time: null,
        transform_started_time: null,
        transform_done_time: null,
        transform_failed_time: null,
        publish_started_time: null,
        publish_done_time: null,
        publish_failed_time: null,
        create_time: '2020-05-19 16:58:00',
        create_by: null,
        last_update_time: '2020-05-19 17:37:49',
        last_update_by: null,
        is_deleted: null
      },
      {
        id: 5,
        ds_group: 'JD',
        ds_name: 'TEST1',
        di_process_definition_id: 'e3099876-6583-4345-bca1-84cc955ccd1c',
        di_process_instance_id: '1552395a-fb67-4b1d-aa05-c0585a12ec51',
        di_process_instance_status: 2,
        import_started_time: '2020-05-19 17:37:19',
        import_done_time: '2020-05-19 17:37:19',
        import_failed_time: null,
        extract_started_time: null,
        extract_done_time: null,
        extract_failed_time: null,
        transform_started_time: null,
        transform_done_time: null,
        transform_failed_time: null,
        publish_started_time: null,
        publish_done_time: null,
        publish_failed_time: null,
        create_time: '2020-05-19 16:57:30',
        create_by: null,
        last_update_time: '2020-05-19 17:37:19',
        last_update_by: null,
        is_deleted: null
      },
      {
        id: 4,
        ds_group: 'JD',
        ds_name: 'TEST1',
        di_process_definition_id: 'e3099876-6583-4345-bca1-84cc955ccd1c',
        di_process_instance_id: 'e5cd545d-fa9a-4856-9c54-0542c0702c46',
        di_process_instance_status: 2,
        import_started_time: '2020-05-19 17:36:49',
        import_done_time: '2020-05-19 17:36:49',
        import_failed_time: null,
        extract_started_time: null,
        extract_done_time: null,
        extract_failed_time: null,
        transform_started_time: null,
        transform_done_time: null,
        transform_failed_time: null,
        publish_started_time: null,
        publish_done_time: null,
        publish_failed_time: null,
        create_time: '2020-05-19 16:57:00',
        create_by: null,
        last_update_time: '2020-05-19 17:36:49',
        last_update_by: null,
        is_deleted: null
      },
      {
        id: 3,
        ds_group: 'JD',
        ds_name: 'TEST1',
        di_process_definition_id: 'e3099876-6583-4345-bca1-84cc955ccd1c',
        di_process_instance_id: 'cf90b875-91c9-4839-abd6-225c01033f56',
        di_process_instance_status: 3,
        import_started_time: '2020-05-19 17:36:15',
        import_done_time: '2020-05-19 17:36:18',
        import_failed_time: null,
        extract_started_time: '2020-05-19 17:36:18',
        extract_done_time: '2020-05-19 17:36:18',
        extract_failed_time: null,
        transform_started_time: '2020-05-19 17:36:18',
        transform_done_time: '2020-05-19 17:36:19',
        transform_failed_time: null,
        publish_started_time: '2020-05-19 17:36:19',
        publish_done_time: null,
        publish_failed_time: '2020-05-19 17:36:19',
        create_time: '2020-05-19 16:56:30',
        create_by: null,
        last_update_time: '2020-05-19 17:36:19',
        last_update_by: null,
        is_deleted: null
      },
      {
        id: 2,
        ds_group: 'JD',
        ds_name: 'TEST1',
        di_process_definition_id: 'e3099876-6583-4345-bca1-84cc955ccd1c',
        di_process_instance_id: '46d747b7-5bf5-4ec9-a397-2ac4fe059e93',
        di_process_instance_status: 2,
        import_started_time: '2020-05-18 21:10:12',
        import_done_time: '2020-05-18 21:10:12',
        import_failed_time: null,
        extract_started_time: '2020-05-18 21:10:12',
        extract_done_time: '2020-05-18 21:10:13',
        extract_failed_time: null,
        transform_started_time: '2020-05-18 21:10:13',
        transform_done_time: '2020-05-18 21:10:13',
        transform_failed_time: null,
        publish_started_time: '2020-05-18 21:10:13',
        publish_done_time: '2020-05-18 21:10:13',
        publish_failed_time: null,
        create_time: '2020-05-18 21:09:47',
        create_by: null,
        last_update_time: '2020-05-18 21:10:13',
        last_update_by: null,
        is_deleted: null
      },
      {
        id: 1,
        ds_group: 'JD',
        ds_name: 'TEST1',
        di_process_definition_id: 'e3099876-6583-4345-bca1-84cc955ccd1c',
        di_process_instance_id: 'a83fc603-a128-4782-9a05-b769432b7517',
        di_process_instance_status: 2,
        import_started_time: '2020-05-18 21:08:42',
        import_done_time: '2020-05-18 21:08:42',
        import_failed_time: null,
        extract_started_time: null,
        extract_done_time: null,
        extract_failed_time: null,
        transform_started_time: null,
        transform_done_time: null,
        transform_failed_time: null,
        publish_started_time: null,
        publish_done_time: null,
        publish_failed_time: null,
        create_time: '2020-05-18 21:08:33',
        create_by: null,
        last_update_time: '2020-05-18 21:08:42',
        last_update_by: null,
        is_deleted: null
      }
    ],
    total_elements: 117
  },
  success: true
}
