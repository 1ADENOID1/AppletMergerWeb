fetch("/fileSelect/get")
    .then(response => {
        return response.json().then(result => {
            return { response, result }
        })
    })
    .then(({ response, result }) => {
        let parentFileDiv = document.getElementById("fileListDiv")
        parentFileDiv.innerHTML = ''

        if (response.status === 200) {

            for (let i in result) {
                let newFile = document.createElement("input")
                newFile.type = "button"
                newFile.value = result[i]
                newFile.className = "file-btn"
                newFile.onclick = function() { getFileFields(this) }
                parentFileDiv.appendChild(newFile)
            }

        } else if (response.status === 500) {

            let errorLabel = document.createElement("label")

            if (result.status == "FileReadError") {
                errorLabel.innerText = result.cause

            } else if (result.status == "SessionReadError") {
                errorLabel.innerText = result.cause + ". Нажмите Назад и попробуйте снова"

            } else {
                errorLabel.innerText = "Неожиданная ошибка сервера"
            }

            parentFileDiv.appendChild(errorLabel)
        }
    })

function getFileFields(button) {
    let filePath = button.value
    fetch(`/fileSelect/getFields?filePath=${encodeURIComponent(filePath)}`)
        .then(response => {
                      return response.json().then(result => {
                          return { response, result }
                      })
        })
        .then(({ response, result }) => {

            let parentFileFieldsDiv = document.getElementById("jsonFields")
            parentFileFieldsDiv.innerHTML = ''

            if (response.status === 200) {

                sessionStorage.setItem("currentFile", filePath)
                let headerLabel = document.getElementById("jsonFilesHeader")
                headerLabel.innerHTML = ''
                headerLabel.innerText = "Выбран файл: " + filePath

                Object.entries(result).forEach(([key, value]) => {

                    let keyValueDiv = document.createElement("div")
                    keyValueDiv.className = "file-fields-group"

                    let keyLabel = document.createElement("label")
                    keyLabel.innerText = key.replaceAll('█', " -> ")
                    keyValueDiv.appendChild(keyLabel)

                    let valueInput = document.createElement("input")
                    valueInput.value = value
                    valueInput.id = key
                    keyValueDiv.appendChild(valueInput)

                    parentFileFieldsDiv.appendChild(keyValueDiv)
                });

            } else if (response.status === 500) {

                let errorLabel = document.createElement("label")

                if (result.status == "FileFieldsReadError") {
                    errorLabel.innerText = result.cause + ". Нажмите Назад и попробуйте снова"
                } else {
                    errorLabel.innerText = "Неожиданная ошибка сервера"
                }

                parentFileFieldsDiv.appendChild(errorLabel)
            }
        })
}

function saveFileFields() {
    let fileName = sessionStorage.getItem("currentFile")
    console.log(fileName)
    if (fileName == null) {
        //TODO: Как-то оповестить юзверя что файл он не выбрал
        return;
    }

    let fieldsParentDiv = document.getElementById("jsonFields")
    let fieldInputs = fieldsParentDiv.querySelectorAll("input")

    let fieldsMap = new Map()
    fieldInputs.forEach( field => {
        if (field.id != null) {
            fieldsMap.set(field.id, field.value)
        }

    })
    console.log(fieldsMap)

    fetch(`/fileSelect/setFields?name=${encodeURIComponent(fileName)}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(Object.fromEntries(fieldsMap))
    })
}

function saveFiles() {
    if (confirm("Данное действие приведёт к записи изменений в файлы. Вы уверены, что хотите продолжить?")) {
        fetch("fileSelect/save")
        .then(response => {
            return response.json().then(result => {
                return { response, result }
            })
        })
        .then(({ response, result }) => {
            if (response.status === 200) {
                window.location.href = "/"
            }
            if (response.status === 500) {
                showToast("Произошла ошибка при сохранении")
            }
        })
    }
}

function returnBack() {
    if (confirm("Данное действие приведёт к потере изменений. Вы уверены, что хотите продолжить?")) {

        window.location.href = "/"
    }
}