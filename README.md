# Sistema de Ponto Eletrônico (SPE)

Sistema web full stack para controle de ponto eletrônico de bolsistas e técnicos, com área administrativa, autenticação por perfil e registro de horas.

O repositório está organizado em dois projetos:

- `back-end`: API em Java 21 com Spring Boot 3.5.6
- `front-end`: aplicação Angular 17

## Visão Geral

O SPE foi desenvolvido para apoiar o gerenciamento de ponto de bolsistas e técnicos, centralizando o acesso às informações de frequência, justificativas e dados administrativos.

### Funcionalidades

- Login com autenticação por token JWT
- Perfis de acesso para `ADMIN` e  `BOLSISTA`
- Registro e consulta de pontos
- Visualização de dados do bolsista
- Área administrativa para consulta e gestão de registros
- Geração de documentos em PDF no backend

## Tecnologias

### Backend

- Java 21
- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- MySQL 8
- JWT com `java-jwt`
- Lombok
- Maven
- Docker e Docker Compose

### Frontend

- Angular 17
- TypeScript
- Bootstrap 5
- RxJS

## Estrutura do Projeto

```text
back-end/
front-end/
README.md
```

## Pré-requisitos

- Java 21 ou superior
- Node.js 18+ e npm
- Maven 3.6+ ou Maven Wrapper
- MySQL 8, caso não use Docker
- Docker e Docker Compose, se preferir executar tudo em contêineres

## Configuração do Backend

O backend lê as credenciais e parâmetros do banco por variáveis de ambiente.

Configure pelo menos estas variáveis:

- `BD_NAME`
- `BD_USER`
- `BD_PASSWORD`
- `BD_URL`
- `KEY_SECRET`
- `URL_FRONT`

Exemplo:

```bash
BD_NAME=spe
BD_USER=root
BD_PASSWORD=senha
BD_URL=jdbc:mysql://localhost:3306/spe?useSSL=false&serverTimezone=UTC
KEY_SECRET=uma-chave-secreta
URL_FRONT=http://localhost:4200
```

## Como Executar

### Backend local

```bash
cd back-end
./mvnw clean install
./mvnw spring-boot:run
```

Se preferir Maven instalado:

```bash
cd back-end
mvn clean install
mvn spring-boot:run
```

O backend sobe em `http://localhost:8080`.

### Frontend local

```bash
cd front-end
npm install
npm start
```

O frontend sobe em `http://localhost:4200`.

### Docker

No backend existe `docker-compose.yml` com MySQL e a aplicação Spring Boot.

```bash
cd back-end
docker-compose up -d --build
```

Para parar:

```bash
docker-compose down
```

Também há um helper script para facilitar o gerenciamento:

```bash
chmod +x docker-helper.sh
./docker-helper.sh help
./docker-helper.sh start
./docker-helper.sh logs-app
./docker-helper.sh rebuild
```

## Rotas do Frontend

- `/home`
- `/login`
- `/meus-dados`
- `/admin`
- `/admin/bolsista/:id`

## APIs e Documentação

O backend utiliza Springdoc OpenAPI. Quando a aplicação estiver ativa, a documentação normalmente fica disponível em:

- `/swagger-ui/index.html`
- `/v3/api-docs`

## Testes

### Frontend

```bash
cd front-end
npm test
```

### Backend

O projeto backend inclui a infraestrutura do JaCoCo e suporte a testes de segurança, mas os testes automatizados estão parcialmente configurados no estado atual do repositório.

## Observações

- O frontend depende do backend ativo para autenticação e carregamento dos dados.
- Se usar Docker para o banco, verifique se a variável `BD_URL` aponta para o host correto do MySQL.
- O backend foi configurado para atualizar o schema automaticamente com `spring.jpa.hibernate.ddl-auto=update`.

## Licença

Consulte o arquivo [LICENSE](LICENSE) para os termos de uso do projeto.
