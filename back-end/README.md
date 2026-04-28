# Sistema de Ponto Eletrônico (SPE) - Backend

Sistema de Ponto Eletrônico desenvolvido em Java com Spring Boot para gerenciamento de ponto de bolsistas e técnicos.

## Descrição do Projeto

O SPE é um sistema backend para controle de ponto eletrônico que permite o registro de entrada e saída de bolsistas, com autenticação baseada em JWT e diferentes níveis de permissão (ADMIN, BOLSISTA, TECNICO).

### Funcionalidades Principais

- **Autenticação JWT**: Sistema de login seguro com tokens JWT
- **Gerenciamento de Usuários**: Registro e autenticação de usuários
- **Controle de Ponto**: Registro de entrada e saída de bolsistas
- **Cálculo Automático de Horas**: Sistema calcula automaticamente as horas trabalhadas
- **Visualização de Pontos**: Bolsistas podem visualizar seu histórico de pontos
- **Diferentes Perfis**: Suporte para BOLSISTA e TECNICO

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Security** (Autenticação e Autorização)
- **Spring Data JPA** (Persistência de dados)
- **MySQL** (Banco de dados)
- **JWT** (JSON Web Tokens) via auth0-java-jwt
- **Lombok** (Redução de código boilerplate)
- **Maven** (Gerenciamento de dependências)
- **Docker & Docker Compose** (Containerização)

## Pré-requisitos

Antes de executar o projeto, certifique-se de ter instalado:

- **Java JDK 17** ou superior
- **Maven 3.6+** (ou use o Maven Wrapper incluído)
- **MySQL 8.0+** (ou use Docker)
- **Git** (para clonar o repositório)
- **Docker & Docker Compose** (opcional, mas recomendado)

## Configuração do Ambiente

### 1. Clonar o Repositório

```bash
git clone https://gitlab.com/si-dimap/spe/back-end.git
cd back-end
```

### 2. Configurar o Banco de Dados MySQL

#### 2.1. Criar o Banco de Dados

Acesse o MySQL e execute:

```sql
CREATE DATABASE SPE;
```

#### 2.2 crie  um ``.env`` e use o .```env.example``` para inserir os dados no ``.env``:
```sql
BD_NAME= VALUE_HERE
BD_USER=VALUE_HERE
BD_PASSWORD=VALUE_HERE
BD_URL=VALUE_HERE
KEY_SECRET=VALUE_HERE 
URL_FRONT=VALUE_HERE
```

##  Como Executar o Projeto

### Opção 1: Usando Maven Wrapper (Recomendado)

#### No Linux/Mac:
```bash
./mvnw clean install
./mvnw spring-boot:run
```

#### No Windows:
```cmd
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

### Opção 2: Usando Maven Instalado

```bash
mvn clean install
mvn spring-boot:run
```

### Opção 3: Executar o JAR

```bash
mvn clean package
java -jar target/spe-0.0.1-SNAPSHOT.jar
```

O servidor será iniciado em: `http://localhost:8080`

## Contribuidores

- Projeto desenvolvido para o DIMAP/UFRN

---

**&copy; Desenvolvido com ☕ e Spring Boot**
