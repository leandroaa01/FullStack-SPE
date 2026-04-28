
## Executar com Docker
### Script Helper (Modo Fácil)

O projeto inclui um script helper para facilitar o gerenciamento dos containers Docker:

```bash
# Dar permissão de execução
chmod +x docker-helper.sh

# Ver todos os comandos disponíveis
./docker-helper.sh help

# Iniciar toda a aplicação
./docker-helper.sh start

# Ver logs da aplicação
./docker-helper.sh logs-app

# Reconstruir após mudanças no código
./docker-helper.sh rebuild

# Verificar status
./docker-helper.sh status

# Acessar o MySQL
./docker-helper.sh shell-db
```
### Opção Docker Compose (Recomendado)

O projeto inclui configuração Docker Compose para executar tanto o MySQL quanto a aplicação Spring Boot em containers.

#### Subir toda a aplicação (MySQL + Spring Boot):

```bash
# Construir e iniciar todos os serviços
docker-compose up -d

# Ver logs em tempo real
docker-compose logs -f

# Ver logs apenas da aplicação
docker-compose logs -f app

# Ver logs apenas do MySQL
docker-compose logs -f mysql
```

#### Parar e remover containers:

```bash
# Parar os serviços
docker-compose down

# Parar e remover volumes (⚠️ apaga dados do banco)
docker-compose down -v
```

#### Reconstruir a aplicação após mudanças no código:

```bash
# Reconstruir e reiniciar apenas a aplicação
docker-compose up -d --build app

# Reconstruir tudo
docker-compose up -d --build
```

### Opção: Executar apenas MySQL no Docker

Se preferir executar apenas o MySQL no Docker e a aplicação localmente:

```bash
# Subir apenas o MySQL
docker-compose up -d mysql

# Executar a aplicação localmente
./mvnw spring-boot:run
```

### Comandos Docker Úteis

#### Gerenciamento de Containers:

```bash
# Listar containers rodando
docker ps

# Listar todos os containers (incluindo parados)
docker ps -a

# Ver status dos serviços
docker-compose ps

# Parar um serviço específico
docker-compose stop app
docker-compose stop mysql

# Iniciar um serviço específico
docker-compose start app
docker-compose start mysql

# Reiniciar um serviço
docker-compose restart app

# Remover um container específico
docker rm spe-backend
docker rm spe-mysql
```

#### Logs e Debug:

```bash
# Ver logs de um serviço
docker-compose logs app
docker-compose logs mysql

# Ver últimas 50 linhas dos logs
docker-compose logs --tail=50 app

# Acompanhar logs em tempo real
docker-compose logs -f app

# Acessar o shell do container da aplicação
docker exec -it spe-backend sh



#### Gerenciamento de Imagens:

```bash
# Listar imagens Docker
docker images

# Remover imagem da aplicação
docker rmi bpp-spe-backend-app

# Remover imagens não utilizadas
docker image prune -a

# Build manual da imagem (sem docker-compose)
docker build -t spe-backend:latest .
```

#### Gerenciamento de Volumes:

```bash
# Listar volumes
docker volume ls

# Inspecionar volume do MySQL
docker volume inspect bpp-spe-backend_mysql_data

# Remover volume específico (⚠️ apaga dados)
docker volume rm bpp-spe-backend_mysql_data

# Remover todos os volumes não utilizados
docker volume prune
```

#### Gerenciamento de Redes:

```bash
# Listar redes Docker
docker network ls

# Inspecionar a rede da aplicação
docker network inspect bpp-spe-backend_spe-network

# Ver quais containers estão na rede
docker network inspect bpp-spe-backend_spe-network --format='{{range .Containers}}{{.Name}} {{end}}'
```

#### Limpeza Geral:

```bash
# Remover tudo que não está em uso (containers, redes, imagens, cache)
docker system prune -a

# Remover tudo incluindo volumes (⚠️ CUIDADO: apaga dados)
docker system prune -a --volumes

# Ver espaço usado pelo Docker
docker system df
```

#### Monitoramento:

```bash
# Ver uso de recursos dos containers
docker stats

# Ver uso de recursos de um container específico
docker stats spe-backend

# Ver informações detalhadas de um container
docker inspect spe-backend

# Ver saúde dos containers
docker ps --format "table {{.Names}}\t{{.Status}}"
```
