
# Big Chat Brasil - Backend

Este é o backend do projeto **Big Chat Brasil**, ambiente totalmente dockerizado para facilitar o setup e execução da aplicação.

---

## Tecnologias Utilizadas

- Java 17  
- Spring Boot 3.8  
- Maven 3.9  
- PostgreSQL 16  
- Docker + Docker Compose  

---

## Como Rodar o Projeto

Este projeto usa **Docker Compose** para subir a aplicação e o banco de dados já configurados.

### Pré-requisitos

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

### Passo a Passo

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/big-chat-brasil.git
   cd big-chat-brasil
   ```

2. Execute o Docker Compose:
   ```bash
   docker-compose up --build
   ```

