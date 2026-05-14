let distrDir
let userDir
let backupDir
let createBackupSubdir
let createChangelog
let fileEncoding

window.addEventListener('load', () => {
    const pending = sessionStorage.getItem('pendingSettings');
    if (pending) {
        const data = JSON.parse(pending);

        document.getElementById('distrInput').value = data.distrDir;
        document.getElementById('userInput').value = data.userDir;
        document.getElementById('backupInput').value = data.backupDir;
        document.getElementById('createBackupSubdirInput').checked = data.createBackupSubdir;
        document.getElementById('createChangelogInput').checked = data.createChangelog;
        document.getElementById('fileEncodingInput').value = data.fileEncoding;

        sessionStorage.removeItem('pendingSettings');
    }
});

function showToast(message, isError = true) {
        const toast = document.createElement('div');
        toast.className = `toast ${!isError ? 'success' : ''}`;
        toast.innerHTML = `
            <span>${isError ? '⚠️' : '✅'} ${message}</span>
            <button onclick="this.parentElement.remove()" style="background:none;border:none;color:white;cursor:pointer;font-size:18px;">×</button>
        `;

        toastContainer.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) toast.remove();
        }, 5000);
    }

function sendSettings() {
    distrDir = document.getElementById("distrInput").value
    userDir = document.getElementById("userInput").value
    backupDir = document.getElementById("backupInput").value
    createBackupSubdir = document.getElementById("createBackupSubdirInput").checked
    createChangelog = document.getElementById("createChangelogInput").checked
    fileEncoding = document.getElementById("fileEncodingInput").value

    fetch("/startpage/get", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                distrDir: distrDir,
                userDir: userDir,
                backupDir: backupDir,
                createBackupSubdir: createBackupSubdir,
                createChangelog: createChangelog,
                fileEncoding: fileEncoding
            })
        })
        .then(response => response.json())
        .then( result => {
            if (!result.success) {
                showToast(result.cause)
            }
            else if (result.success && result.redirectUrl) {
                sessionStorage.setItem('pendingSettings', JSON.stringify({
                    distrDir: document.getElementById("distrInput").value,
                    userDir: document.getElementById("userInput").value,
                    backupDir: document.getElementById("backupInput").value,
                    createBackupSubdir: document.getElementById("createBackupSubdirInput").checked,
                    createChangelog: document.getElementById("createChangelogInput").checked,
                    fileEncoding: document.getElementById("fileEncodingInput").value

                }));
                window.location.href = result.redirectUrl;
            } else {
                showToast("Неожиданный ответ от сервера")
            }
        }
    )
}