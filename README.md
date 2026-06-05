# AWS Image Pipeline 🚀

Um pipeline de processamento de imagens automatizado e escalável, utilizando uma arquitetura baseada em eventos na AWS. O sistema permite que usuários façam upload de imagens de forma segura, processem essas imagens de forma assíncrona e recebam uma notificação por e-mail quando o trabalho estiver concluído.

## 🏗️ Arquitetura do Sistema

O fluxo de dados do projeto segue estas etapas:

1.  **Frontend (Web):** O usuário insere seu e-mail e seleciona uma imagem.
2.  **AWS Lambda (Producer):** O frontend solicita uma URL pré-assinada à Lambda para fazer o upload seguro diretamente para o S3 (evitando sobrecarga do servidor).
3.  **Amazon S3 (Entrada):** A imagem original é salva no bucket e dispara um **Evento de Notificação**.
4.  **Amazon SQS (Fila):** O evento do S3 é enfileirado no SQS para garantir resiliência e desacoplamento.
5.  **Amazon EC2 (Consumer):** Um Worker Java em execução na EC2 faz *Long Polling* na fila, baixa a imagem, redimensiona-a e salva a versão processada no bucket de saída.
6.  **Amazon SES (Notificação):** O Consumer gera um link temporário da imagem pronta e envia um e-mail automático ao usuário.

---

## 🛠️ Tecnologias Utilizadas

*   **Frontend:** HTML5, CSS3 (Modern UI), JavaScript (Vanilla), Mermaid.js (Fluxogramas).
*   **Backend (Lambda & EC2):** Java 21, Gradle, AWS SDK v2.
*   **Serviços AWS:** S3, SQS, Lambda, EC2, SES, IAM.

---

## 🚀 Como Iniciar o Projeto

### 1. Configuração da Infraestrutura AWS
Certifique-se de ter o AWS CLI instalado e configurado em sua máquina local.

```bash
# Verificar instalação
aws --version

# Configurar credenciais (Use uma região como sa-east-1)
aws configure
```

### 2. Lambda Producer (`image-producer-lambda`)
Este módulo é responsável por autorizar o upload.
*   **Build:** Execute `./gradlew shadowJar` para gerar o arquivo `.jar`.
*   **Deploy:** Faça o upload do `.jar` para uma função Lambda com permissões de escrita no S3.
*   **Configuração:** Defina a variável de ambiente `S3_BUCKET_NAME` com o nome do seu bucket de entrada.

### 3. Frontend Web (`image-frontend`)
Interface amigável para o usuário.
*   **Configuração:** No arquivo `script.js`, atualize a URL do `fetch` para apontar para a **Function URL** da sua Lambda.
*   **Uso:** Basta abrir o `index.html` em qualquer navegador.

### 4. EC2 Consumer (`image-consumer-ec2`)
O "motor" de processamento que roda 24/7 ou sob demanda.
*   **Build:** Execute `./gradlew build` para gerar o `.jar` executável.
*   **Transferência:** Envie o arquivo para sua instância Linux:
    ```bash
    scp -i "sua-chave.pem" build/libs/seu-app.jar ec2-user@IP_DA_EC2:/home/ec2-user/
    ```
*   **Execução:**
    ```bash
    java -jar seu-app.jar
    ```
    *Certifique-se de que a instância EC2 tenha uma IAM Role com permissões para SQS, S3 e SES.*

---

## 📂 Estrutura de Arquivos

*   `/image-frontend`: Contém a interface web e a lógica de comunicação com a Lambda.
*   `/image-producer-lambda`: Código Java da função Serverless que gera as URLs de upload.
*   `/image-consumer-ec2`: Aplicação Java Worker que processa as mensagens da fila e as imagens.

---

## 🛡️ Segurança e Boas Práticas
*   **URLs Pré-assinadas:** Garantem que o bucket S3 não precise ser público para receber uploads.
*   **IAM Roles:** O projeto utiliza o princípio de menor privilégio para acesso aos recursos.
*   **Variáveis de Ambiente:** Nenhuma chave de acesso (Secret Key) está hardcoded no código; o sistema utiliza o provedor de credenciais padrão da AWS.

---

## ✒️ Autor
Desenvolvido por **Marcus Keller**.
