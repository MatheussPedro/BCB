
# Big Chat Brasil - Backend

Este é o backend do projeto **Big Chat Brasil**, ambiente totalmente dockerizado para facilitar o setup.

---

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.8
- Maven 3.9
- PostgreSQL 16
- Docker + Docker Compose

---

## Links

Documentação:
https://app.swaggerhub.com/apis/matheus-112/BCB/1.0.0

Documentação 2 opção: https://drive.google.com/file/d/1y7G8zQo0ynIwmSaWD8lpFb6DR0DHjDqu/view?usp=sharing

Caso prefira pode importar o JSON de API no postman: https://drive.google.com/file/d/1y7G8zQo0ynIwmSaWD8lpFb6DR0DHjDqu/view?usp=sharing

---

## Como Rodar

Este método já sobe o PostgreSQL e a aplicação configurados.

### Pré-requisitos

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

### Passo a passo

1. Clone o projeto:
   ```bash
   git clone git@github.com:MatheussPedro/BCB.git
   cd BCB
   ```

2. Inicie com:
   ```bash
   docker-compose up
   ```

---

## Sobre o banco

Pode acessar via Docker com:

```bash
docker exec -it bcb-postgres bash
```

Logar como usuário já dentro do container:

```bash
psql -U bcb_u -d bcb
```

Abaixo está a estrutura das tabelas do banco de dados utilizado no projeto **Big Chat Brasil**:

---

### Tabela: `clientes`

| Coluna         | Tipo                  | Obrigatório | Observações                                        |
|----------------|-----------------------|-------------|---------------------------------------------------|
| id             | bigint                | Sim         | Chave primária                                    |
| nome           | varchar(255)          | Sim         | Nome do cliente                                   |
| documento      | varchar(20)           | Sim         | Único, CPF ou CNPJ dependendo do tipo_documento   |
| tipo_documento | varchar(4)            | Sim         | Ex: CPF, CNPJ                                     |
| plano          | varchar(10)           | Sim         | Ex: FREE, BASIC, PREMIUM                          |
| status         | boolean               | Sim         | true = ativo, false = inativo                     |
| limite         | double precision      | Sim         | Limite de mensagens para clientes pós-pagos       |
| saldo          | double precision      | Não         | Valor para clientes pré-pagos                     |

Relacionamentos:
- `clientes.id` é referenciado por várias tabelas: `mensagens`, `fila`, `conversas`, `transactions`

---

### Tabela: `conversas`

| Coluna               | Tipo                        | Obrigatório | Observações                                 |
|----------------------|-----------------------------|-------------|----------------------------------------------|
| id                   | bigint                      | Sim         | Chave primária                               |
| client_id            | bigint                      | Sim         | FK para `clientes(id)`                       |
| recipient_id         | bigint                      | Sim         | FK para `clientes(id)`                       |
| last_message_time    | timestamp                   | Não         | Última mensagem registrada                    |
| last_message_content | varchar(255)                | Não         | Texto da última mensagem                      |
| recipient_name       | varchar(255)                | Não         | Nome do destinatário                         |
| unread_count         | integer                     | Não         | Total de mensagens não lidas                  |

Relacionamentos:
- Cada conversa conecta um cliente com um destinatário.
- `conversas.id` é referenciado por `mensagens`

---

### Tabela: `fila`

| Coluna       | Tipo                        | Obrigatório | Observações                        |
|--------------|-----------------------------|-------------|-----------------------------------|
| id           | bigint                      | Sim         | Chave primária                    |
| client_id    | bigint                      | Sim         | FK para `clientes(id)`           |
| recipient_id | bigint                      | Sim         | FK para `clientes(id)`           |
| timestamp    | timestamp                   | Sim         | Data/hora da fila                 |
| priority     | varchar(255)                | Sim         | Prioridade da mensagem            |
| text         | text                        | Sim         | Conteúdo da mensagem              |
| type         | varchar(255)                | Sim         | Tipo da mensagem (ex: texto, midia)|

---

### Tabela: `mensagens`

| Coluna         | Tipo                        | Obrigatório | Observações                        |
|----------------|-----------------------------|-------------|-----------------------------------|
| id             | bigint                      | Sim         | Chave primária                    |
| client_id      | bigint                      | Sim         | FK para `clientes(id)`           |
| recipient_id   | bigint                      | Sim         | FK para `clientes(id)`           |
| conversation_id| bigint                      | Não         | FK para `conversas(id)`          |
| data_hora      | timestamp                   | Sim         | Data/hora da mensagem             |
| texto          | varchar(255)                | Sim         | Texto da mensagem                 |
| status         | varchar(255)                | Sim         | Ex: ENVIADA, ENTREGUE, LIDA       |
| prioridade     | varchar(255)                | Sim         | Prioridade (ex: alta, normal)     |
| tipo           | varchar(255)                | Sim         | Tipo da mensagem (ex: texto, midia)|
| custo          | double precision            | Sim         | Custo da mensagem para o cliente  |

---

### Tabela: `transactions`

| Coluna                    | Tipo                | Obrigatório | Observações                                  |
|---------------------------|---------------------|-------------|---------------------------------------------|
| id                        | bigint              | Sim         | Chave primária                              |
| client_id                 | bigint              | Não         | FK para `clientes(id)`                      |
| timestamp                 | timestamp           | Não         | Data da transação                           |
| amount                    | double precision    | Sim         | Valor da transação (positivo ou negativo)   |
| description               | varchar(255)        | Não         | Descrição (ex: recarga, cobrança, ajuste)   |
| type                      | varchar(255)        | Não         | Tipo de transação                           |
| balance_after_transaction | double precision    | Não         | Saldo do cliente após a transação           |
| limit_after_transaction   | double precision    | Não         | Limite do cliente após a transação          |