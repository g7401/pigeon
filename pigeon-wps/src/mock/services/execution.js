export const getImportDiTaskInstance = {
  'err_code': null,
  'err_message': null,
  'object': {
    'basic': {
      'di_task_instance_id': '42374016-640d-4689-b064-d8f6c9f6f70e',
      'di_task_instance_created_by': null,
      'di_task_instance_executed_by': {
        'di_processor_id': '44de8bac-3f65-4415-8b51-dc5ec507020d',
        'name': 'MssqlserverImportProcessor',
        'version': '1.0.0'
      },
      'di_task_instance_status': 'DONE',
      'di_task_instance_create_time': '2020-05-18 21:10:12',
      'di_task_instance_started_time': '2020-05-18 21:10:12',
      'di_task_instance_done_time': '2020-05-18 21:10:12',
      'di_task_instance_duration_in_millis': 0,
      'di_task_instance_failed_time': null,
      'di_task_instance_failure_reason': null
    },
    'data_file_id': '44de8bac-3f65-4415-8b51-dc5ec507020w',
    'data_file_name': null,
    'data_file_size_in_bytes': null
  },
  'success': true
}

export const getExtractDiTaskInstance = {
  'err_code': null,
  'err_message': null,
  'object': {
    'basic': {
      'di_task_instance_id': '0bb57f4d-a8ab-4da3-acf3-e9e23993cfd6',
      'di_task_instance_created_by': null,
      'di_task_instance_executed_by': {
        'di_processor_id': 'be16fd9e-b102-4c62-aed7-addba202fa23',
        'name': 'SimpleExcelExtractProcessor',
        'version': '1.0.0'
      },
      'di_task_instance_status': 'DONE',
      'di_task_instance_create_time': '2020-05-18 21:10:12',
      'di_task_instance_started_time': '2020-05-18 21:10:12',
      'di_task_instance_done_time': '2020-05-18 21:10:13',
      'di_task_instance_duration_in_millis': 1000,
      'di_task_instance_failed_time': null,
      'di_task_instance_failure_reason': null
    },
    'output_tables': [
      {
        'table_name': 'process_value',
        'table_comment': null,
        'fields': [
          {
            'table_column_name': 'id',
            'table_column_comment': '主键',
            'table_column_type': 'LONG',
            'table_column_type_attributes': null,
            'nullable': true,
            'unique': false,
            'unsigned': true,
            'query': true,
            'list': true
          },
          {
            'table_column_name': 'last_update_time',
            'table_column_comment': '更新时间',
            'table_column_type': 'DATETIME',
            'table_column_type_attributes': null,
            'nullable': true,
            'unique': false,
            'unsigned': true,
            'query': true,
            'list': true
          },
          {
            'table_column_name': 'process_value',
            'table_column_comment': '值',
            'table_column_type': 'DECIMAL',
            'table_column_type_attributes': '10,2',
            'nullable': true,
            'unique': false,
            'unsigned': true,
            'query': true,
            'list': true
          }
        ]
      }
    ]
  },
  'success': true
}

export const getTransformDiTaskInstance = {
  'err_code': null,
  'err_message': null,
  'object': {
    'units': [
      {
        'basic': {
          'di_task_instance_id': 'e054eb82-9e92-4740-8157-084a1400ad66',
          'di_task_instance_created_by': null,
          'di_task_instance_executed_by': {
            'di_processor_id': '71568232-53a3-47a0-894d-989c23c70cf3',
            'name': 'SqlTransformProcessor',
            'version': '1.0.0'
          },
          'di_task_instance_status': 'DONE',
          'di_task_instance_create_time': '2020-05-18 21:10:13',
          'di_task_instance_started_time': '2020-05-18 21:10:13',
          'di_task_instance_done_time': '2020-05-18 21:10:13',
          'di_task_instance_duration_in_millis': 0,
          'di_task_instance_failed_time': null,
          'di_task_instance_failure_reason': null
        },
        'output_tables': [
          {
            'table_name': 'di_otr_process_value_copy1',
            'table_comment': null,
            'fields': [
              {
                'table_column_name': 'id',
                'table_column_comment': 'COL 1',
                'table_column_type': 'LONG',
                'table_column_type_attributes': null,
                'nullable': true,
                'unique': true,
                'unsigned': true,
                'query': true,
                'list': true
              },
              {
                'table_column_name': 'process_value',
                'table_column_comment': 'COL 2',
                'table_column_type': 'TEXT',
                'table_column_type_attributes': '128',
                'nullable': true,
                'unique': true,
                'unsigned': true,
                'query': true,
                'list': true
              },
              {
                'table_column_name': 'last_update_time',
                'table_column_comment': 'COL 3',
                'table_column_type': null,
                'table_column_type_attributes': null,
                'nullable': false,
                'unique': true,
                'unsigned': true,
                'query': true,
                'list': true
              }
            ]
          },
          {
            'table_name': 'di_otr_process_value_copy3',
            'table_comment': null,
            'fields': [
              {
                'table_column_name': 'name',
                'table_column_comment': 'mingzi',
                'table_column_type': 'LONG',
                'table_column_type_attributes': null,
                'nullable': true,
                'unique': true,
                'unsigned': true,
                'query': true,
                'list': true
              },
              {
                'table_column_name': 'age',
                'table_column_comment': 'nianling',
                'table_column_type': 'TEXT',
                'table_column_type_attributes': '128',
                'nullable': true,
                'unique': true,
                'unsigned': true,
                'query': true,
                'list': true
              },
              {
                'table_column_name': 'last_update_time',
                'table_column_comment': 'gengxinshijian',
                'table_column_type': null,
                'table_column_type_attributes': null,
                'nullable': false,
                'unique': true,
                'unsigned': true,
                'query': true,
                'list': true
              }
            ]
          }
        ]
      },
      {
        'basic': {
          'di_task_instance_id': 'e054eb82-9e92-4740-8157-084a1400ad65',
          'di_task_instance_created_by': null,
          'di_task_instance_executed_by': {
            'di_processor_id': '71568232-53a3-47a0-894d-989c23c70cf3',
            'name': 'SqlTransformProcessor',
            'version': '1.0.0'
          },
          'di_task_instance_status': 'DONE',
          'di_task_instance_create_time': '2020-05-18 21:10:13',
          'di_task_instance_started_time': '2020-05-18 21:10:13',
          'di_task_instance_done_time': '2020-05-18 21:10:13',
          'di_task_instance_duration_in_millis': 0,
          'di_task_instance_failed_time': null,
          'di_task_instance_failure_reason': null
        },
        'output_tables': [
          {
            'table_name': 'di_otr_process_value_copy2',
            'table_comment': null,
            'fields': [
              {
                'table_column_name': 'id',
                'table_column_comment': 'COL 1',
                'table_column_type': 'LONG',
                'table_column_type_attributes': null,
                'nullable': true,
                'unique': true,
                'unsigned': true,
                'query': false,
                'list': true
              },
              {
                'table_column_name': 'process_value',
                'table_column_comment': 'COL 2',
                'table_column_type': 'TEXT',
                'table_column_type_attributes': '128',
                'nullable': true,
                'unique': true,
                'unsigned': true,
                'query': true,
                'list': true
              },
              {
                'table_column_name': 'last_update_time',
                'table_column_comment': 'COL 3',
                'table_column_type': null,
                'table_column_type_attributes': null,
                'nullable': false,
                'unique': true,
                'unsigned': true,
                'query': true,
                'list': true
              }
            ]
          }
        ]
      }
    ]
  },
  'success': true
}

export const getPublishDiTaskInstance = {
  'err_code': null,
  'err_message': null,
  'object': {
    'basic': {
      'di_task_instance_id': 'c8c6d1bc-e726-4f8c-8414-8ffa2a69b362',
      'di_task_instance_created_by': null,
      'di_task_instance_executed_by': {
        'di_processor_id': 'ec024cd2-5d0a-469e-9d81-e1f0361ee43d',
        'name': 'SimplePublishProcessor',
        'version': '1.0.0'
      },
      'di_task_instance_status': 'DONE',
      'di_task_instance_create_time': '2020-05-18 21:10:13',
      'di_task_instance_started_time': '2020-05-18 21:10:13',
      'di_task_instance_done_time': '2020-05-18 21:10:13',
      'di_task_instance_duration_in_millis': 0,
      'di_task_instance_failed_time': null,
      'di_task_instance_failure_reason': null
    },
    'output_tables': null
  },
  'success': true
}

export const createProcessInstance = {
  'err_code': null,
  'err_message': null,
  'object': {
    'id': 117,
    'ds_group': 'JD',
    'ds_name': 'TEST1',
    'di_process_definition_id': 'e3099876-6583-4345-bca1-84cc955ccd1c',
    'di_process_instance_id': '28d6b12f-9b30-4c47-8037-6fc0d6c9ac27',
    'di_process_instance_status': 'WAITING',
    'import_started_time': null,
    'import_done_time': null,
    'import_failed_time': null,
    'extract_started_time': null,
    'extract_done_time': null,
    'extract_failed_time': null,
    'transform_started_time': null,
    'transform_done_time': null,
    'transform_failed_time': null,
    'publish_started_time': null,
    'publish_done_time': null,
    'publish_failed_time': null,
    'create_time': '2020-05-22 17:47:45',
    'create_by': null,
    'last_update_time': '2020-05-22 17:47:45',
    'last_update_by': null
  },
  'success': true
}
