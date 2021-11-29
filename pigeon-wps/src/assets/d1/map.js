export const cascaderType = {
  CASCADER: 'CASCADER',
  CASCADER_MULTIPLE: 'CASCADER_MULTIPLE'
}

export const selectFormType = {
  DROP_DOWN_LIST_SINGLE: 'DROP_DOWN_LIST_SINGLE',
  DROP_DOWN_LIST_MULTIPLE: 'DROP_DOWN_LIST_MULTIPLE',
  ASSOCIATING_SINGLE: 'ASSOCIATING_SINGLE',
  ASSOCIATING_MULTIPLE: 'ASSOCIATING_MULTIPLE',
  ...cascaderType
}

export const textFormType = {
  FULL_FUZZY_TEXT: 'FULL_FUZZY_TEXT',
  LEFT_FUZZY_TEXT: 'LEFT_FUZZY_TEXT',
  RIGHT_FUZZY_TEXT: 'RIGHT_FUZZY_TEXT',
  EXACT_TEXT: 'EXACT_TEXT'
}
export const dateEelmentType = {
  DATE_RANGE: 'DATE_RANGE',
  DATE_SINGLE: 'DATE_SINGLE'
}
export const timeEelmentType = {
  TIME_RANGE: 'TIME_RANGE',
  TIME_SINGLE: 'TIME_SINGLE'
}
export const dateTimeEelmentType = {
  TIMESTAMP_RANGE: 'TIMESTAMP_RANGE',
  TIMESTAMP_SINGLE: 'TIMESTAMP_SINGLE'
}
export const QueryFormType = {
  // 文本
  ...textFormType,
  /** 下拉选择 */
  ...selectFormType,
  /** 日期时间 */
  ...dateTimeEelmentType,
  ...dateEelmentType,
  ...timeEelmentType,
  /** 数字类型 */
  NUMBER_RANGE: 'NUMBER_RANGE'
}
