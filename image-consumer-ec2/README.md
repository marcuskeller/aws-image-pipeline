## Configurando o Lambda

Neste processo o Lambda é essencial, pois ele irá gerar uma URL assinada com o email do remetente e devolverá para a aplicação web,
a aplicação então irá salvar esta imagem diretamente no seu S3(Serviço de Bucket) este serviço irá emitir um evento para 
o SQS(Serviço de filas) fazendo assim o pedido ser salvo na fila.

[image-producer-lambda](https://github.com/marcuskeller/image-producer-lambda)

## Image Consumer EC2 

O EC2 vai ler a fila do SQS e ver qual é a próxima imagem a ser processada, com isso irá pegar esta imagem, redimensionar o tamanho e vai marcar esta imagem como 
processada no S3 e irá enviar um email para o remetente com a imagem redimensionada.


## Configurando a AWS CLI no seu ambiente local

Para acessar a sua instância pelo seu prompt precisamos configurar o setup do seu ambiente local. Dito isso abaixo deixo 
um guia explicando o passo a passo.


### Instale o AWS CLI na sua máquina
Para que este processo de certo, primeiro você precisa instalar o AWS CLI na sua máquina local:

[AWS CLI DOWNLOAD- OFICIAL](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)

Para verificar se a instalação funcionou insira esse comando:
```
aws --version
```

Se aparecer algo como `aws-cli/2.x.x` , a instalação foi um sucesso.

## Configuração de Credenciais
### Obtenhas as chaves:

Vá ao console da AWS, clique no seu nome de usuário (topo direito) -> **Security Credentials**.

Vá em **Access Keys** e crie uma nova. Copie o **Access Key ID** e o **Secret Access Key**.

### Execute o comando de configuração:
No seu terminal, digite:
```
aws configure
```

### Insira os dados

```
AWS ID de chave de acesso: Cole o ID que você copiou.

AWS chave secreta de acesso: Cole a chave secreta.

Nome da região padrão: sa-east-1 (ou a região onde seu bucket/fila estão).

Formato de saida padrão: json (recomendado).
```

### Teste
Para garantir que está tudo funcionando, tente listar os seus buckets S3:
```
aws s3 ls
```

### Iniciando EC2
Para iniciar a sua instância da AWS abra o seu cmd e insira o seguinte comando:
```
ssh -i "...\key-pair.pem" ec2-user@seu_IP_publico_ec2
```

### Inserindo arquivo .Jar no seu EC2
```
scp -i "...\key-pair.pem" ...\seu-app.jar ec2-user@54.xx.xx.xx:/home/ec2-user/
```