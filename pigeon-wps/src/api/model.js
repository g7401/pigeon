export const api = {
  prepareTrainingSet: '/lotus-mts/model_training/prepare_training_set',
  trainTask: '/lotus-mts/model_training/train_task',
  trainCommitTask: '/lotus-mts/model_training/commit_task',
  preparePredictionSet: '/lotus-mts/model_prediction/prepare_prediction_set',
  predictTask: '/lotus-mts/model_prediction/predict_task',
  predictCommitTask: '/lotus-mts/model_prediction/commit_task',
  modelDefinition: '/lotus-mbs/model_definition/id',
  createModelTraining: '/lotus-mms/modelling_master/training_process_instances',
  createModelServing: '/lotus-mms/modelling_master/serving_process_instances',

  initialUploadFeatureDatasets: '/kapok-dis/fusion/initial_upload_feature_datasets',
  initialUploadReportingDatasets: '/kapok-dis/fusion/initial_upload_reporting_datasets'
}

const config = {
  createModelServing: {
    url: api.createModelServing,
    method: 'post',
    dataFields: [{
      key: 'model_id'
    }, {
      key: 'data_set_filter_map'
    }, {
      key: 'app_key',
      default: 'd4b30f9a-0647-43e8-b139-d759f8243cb4'
    }]
  },
  createModelTraining: {
    url: api.createModelTraining,
    method: 'post',
    dataFields: [{
      key: 'model_id'
    }, {
      key: 'data_set_filter_map'
    }, {
      key: 'app_key',
      default: 'd4b30f9a-0647-43e8-b139-d759f8243cb4'
    }]
  },
  getModelDefinition: {
    url: api.modelDefinition,
    method: 'get',
    paramsFields: ['model_id'],
    mock: true
  },
  getPrepareTrainingSet: {
    url: api.prepareTrainingSet,
    method: 'get',
    paramsFields: ['model_training_process_instance_id']
  },
  getTrainTask: {
    url: api.trainTask,
    method: 'get',
    paramsFields: ['model_training_process_instance_id']
  },
  getTrainCommitTask: {
    url: api.trainCommitTask,
    method: 'get',
    paramsFields: ['model_training_process_instance_id']
  },
  getPreparePredictionSet: {
    url: api.preparePredictionSet,
    method: 'get',
    paramsFields: ['model_prediction_process_instance_id']
  },
  getPredictTask: {
    url: api.predictTask,
    method: 'get',
    paramsFields: ['model_prediction_process_instance_id']
  },
  getPredictCommitTask: {
    url: api.predictCommitTask,
    method: 'get',
    paramsFields: ['model_prediction_process_instance_id']
  },

  // fusion
  initialUploadFeatureDatasets: {
    url: api.initialUploadFeatureDatasets,
    method: 'post',
    dataFields: ['data_properties', 'ds_name', 'file_type', 'file_id']
  },
  initialUploadReportingDatasets: {
    url: api.initialUploadReportingDatasets,
    method: 'post',
    dataFields: ['data_properties', 'ds_name', 'file_type', 'file_id']
  }
}

export default config
