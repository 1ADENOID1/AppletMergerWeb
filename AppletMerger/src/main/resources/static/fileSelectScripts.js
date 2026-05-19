fetch("/fileSelect/get")
    .then(response => {
        return response.json().then(result => {
            return { response, result }
        })
    })
    .then(({ response, result }) => {
        if (response.status === 200) {
            let parentFileDiv = document.getElementById("fileListDiv")
            parentFileDiv.innerHTML = ''
            for (let i in result) {
                let newFile = document.createElement("input")
                newFile.type = "button"
                newFile.value = result[i]
                newFile.className = "file-btn"
                newFile.onclick = function() { getFileFields(this) }
                parentFileDiv.appendChild(newFile)
            }
        } else if (response.status === 500) {
            let parentFileDiv = document.getElementById("fileListDiv")
            parentFileDiv.innerHTML = ''
            if (result.status == "FileReadError") {
                let errorLabel = document.createElement("label")
                errorLabel.innerText = result.cause
                parentFileDiv.appendChild(errorLabel)
            } else if (result.status == "SessionReadError") {
                let errorLabel = document.createElement("label")
                errorLabel.innerText = result.cause + ". Нажмите Назад и попробуйте снова"
                parentFileDiv.appendChild(errorLabel)
            }
        }
    })

function getFileFields(button) {
    let filePath = button.value
    fetch(`/fileSelect/getFields?filePath=${encodeURIComponent(filePath)}`)
        .then(response => response.json())
        .then(answer => {
            let parentFileFieldsDiv = document.getElementById("jsonFields")
            parentFileFieldsDiv.innerHTML = ''
            console.log(answer)
            Object.entries(answer).forEach(([key, value]) => {

                let keyValueDiv = document.createElement("div")
                keyValueDiv.className = "form-group"

                let keyLabel = document.createElement("label")
                keyLabel.innerText = key.replaceAll('█', " -> ")
                keyValueDiv.appendChild(keyLabel)

                let valueInput = document.createElement("input")
                valueInput.value = value
                keyValueDiv.appendChild(valueInput)

                parentFileFieldsDiv.appendChild(keyValueDiv)
            });
        })
}

function returnBack() {
    if (confirm("Данное действие приведёт к потере изменений. Вы уверены, что хотите продолжить?")) {

        window.location.href = "/"
    }
}