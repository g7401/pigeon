import ExcelJS from 'exceljs'
function saveAs (obj, fileName) {
  const tmpa = document.createElement('a')
  tmpa.download = fileName ? fileName + '.xlsx' : new Date().getTime() + '.xlsx'
  tmpa.href = URL.createObjectURL(obj)
  tmpa.click()

  setTimeout(function () {
    URL.revokeObjectURL(obj)
  }, 100)
}

export function exportExcel (exportData) {
  const workbook = new ExcelJS.Workbook()
  workbook.created = new Date()
  workbook.modified = new Date()
  const sheet = workbook.addWorksheet('Sheet1')
  // sheet.properties.defaultRowHeight = 25

  sheet.columns = Object.keys(exportData[0]).map(function (header) {
    return {
      header: header,
      key: header
    }
  })
  sheet.addRows(exportData)
  workbook.xlsx.writeBuffer().then(function (data) {
    saveAs(new Blob([data], { type: 'application/octet-stream' }))
  })
}
