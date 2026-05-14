fetch("/fileSelect/get")
    .then(response => {
        return response.json().then(result => {
            return { response, result }
        })
    })
    .then(({ response, result }) => {
        if (response.status === 200) {
            console.log(result)
            let parentFileDiv = document.getElementById("fileListDiv")
            parentFileDiv.innerHTML = ''
            for (let i in result) {
                let newFile = document.createElement("input")
                newFile.type = "button"
                newFile.value = result[i]
                newFile.className = "file-btn"
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



function returnBack() {
    if (confirm("Данное действие приведёт к потере изменений. Вы уверены, что хотите продолжить?")) {

        window.location.href = "/"
    }
}