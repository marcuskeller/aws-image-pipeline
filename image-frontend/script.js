document.addEventListener('DOMContentLoaded', async () => {
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
    
const mermaidDiv = document.querySelector('.mermaid');
if (mermaidDiv) {
    try {
        const { default: mermaid } = await import('https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs');
        mermaid.initialize({ startOnLoad: false, theme: 'neutral' });
        await mermaid.run();
    } catch (e) {
        console.error("Erro ao carregar Mermaid:", e);
    }
}
});

async function upload() {
    const emailInput = document.getElementById('email');
    const fileInput = document.getElementById('file');
    const email = emailInput.value;
    const file = fileInput.files[0];
    const uploadBtn = document.querySelector('button[onclick="upload()"]');
    const originalBtnContent = uploadBtn.innerHTML;

    if (!email) {
        alert("Por favor, insira o seu e-mail!");
        emailInput.focus();
        return;
    }

    if (!file) {
        alert("Por favor, selecione uma imagem!");
        // Como o input file está escondido, focamos na drop-zone que é o label dele
        document.querySelector('.drop-zone').scrollIntoView({ behavior: 'smooth' });
        return;
    }

    // Iniciar estado de carregamento
    uploadBtn.disabled = true;
    uploadBtn.innerHTML = 'Enviando... <i class="fa-solid fa-spinner fa-spin"></i>';

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
            console.error('Erro S3:', errorText);
            alert(`Erro ao enviar para o S3: ${uploadResponse.status} - ${uploadResponse.statusText}`);
        }
    } catch (error) {
        console.error('Erro no processo:', error);
        alert('Ocorreu um erro no processo: ' + error.message);
    } finally {
        // Restaurar botão
        uploadBtn.disabled = false;
        uploadBtn.innerHTML = originalBtnContent;
    }
}