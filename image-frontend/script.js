// Inicializa o listener do arquivo assim que a página carrega
document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('file');
    const fileNameDisplay = document.getElementById('file-name');

    if (fileInput && fileNameDisplay) {
        fileInput.addEventListener('change', function () {
            if (this.files.length > 0) {
                fileNameDisplay.textContent = "Arquivo: " + this.files[0].name;
            } else {
                fileNameDisplay.textContent = "";
            }
        });
    }
});

async function upload() {
    const email = document.getElementById('email').value;
    const file = document.getElementById('file').files[0];

    if (!email || !file) {
        alert("Preencha o e-mail e selecione a foto!");
        return;
    }

    try {
        const response = await fetch('https://3qiudn6fwwimlrw2ay5x3lskcq0lzsqm.lambda-url.sa-east-1.on.aws/', {
           method: 'POST',
           headers: {'Content-Type': 'application/json'},
           body: JSON.stringify({"user-email": email})
        });

        if (!response.ok) {
            throw new Error(`Erro ao contatar Lambda: ${response.status}`);
        }

        const data = await response.json();

        const presignedUrl = data.uploadUrl || data.url || (typeof data === 'string' ? data : null);

        if (!presignedUrl) {
            throw new Error("Não foi possível encontrar a URL de upload na resposta da Lambda.");
        }

        const uploadResponse = await fetch(presignedUrl, {
            method: 'PUT',
            body: file,
            headers: {
                'x-amz-meta-user-email': email,
                'Content-Type': file.type
            }
        });

        if (uploadResponse.ok) {
            alert('Upload feito! O processamento começou.');
        } else {
            const errorText = await uploadResponse.text();
            alert('Erro ao enviar para o S3. Verifique o console.');
        }
    } catch (error) {
        alert('Ocorreu um erro no processo.');
    }
}