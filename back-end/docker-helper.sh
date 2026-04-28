#!/bin/bash

# Script helper para gerenciar a aplicação SPE com Docker

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funções auxiliares
print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Menu de ajuda
show_help() {
    cat << EOF
${GREEN}SPE - Sistema de Ponto Eletrônico${NC}
Script helper para gerenciar a aplicação com Docker

${YELLOW}Uso:${NC} ./docker-helper.sh [comando]

${YELLOW}Comandos disponíveis:${NC}

  ${GREEN}start${NC}           Inicia todos os serviços (MySQL + App)
  ${GREEN}start-db${NC}        Inicia apenas o MySQL
  ${GREEN}start-app${NC}       Inicia apenas a aplicação
  
  ${GREEN}stop${NC}            Para todos os serviços
  ${GREEN}stop-db${NC}         Para apenas o MySQL
  ${GREEN}stop-app${NC}        Para apenas a aplicação
  
  ${GREEN}restart${NC}         Reinicia todos os serviços
  ${GREEN}restart-app${NC}     Reinicia apenas a aplicação
  
  ${GREEN}logs${NC}            Visualiza logs de todos os serviços
  ${GREEN}logs-app${NC}        Visualiza logs da aplicação
  ${GREEN}logs-db${NC}         Visualiza logs do MySQL
  
  ${GREEN}build${NC}           Reconstrói a aplicação
  ${GREEN}rebuild${NC}         Reconstrói e reinicia todos os serviços
  
  ${GREEN}status${NC}          Verifica status dos serviços
  ${GREEN}ps${NC}              Lista containers rodando
  
  ${GREEN}shell-app${NC}       Acessa o shell do container da aplicação
  ${GREEN}shell-db${NC}        Acessa o MySQL via CLI
  
  ${GREEN}clean${NC}           Para serviços e remove containers
  ${GREEN}clean-all${NC}       Remove tudo (containers, volumes, imagens)
  
  ${GREEN}test${NC}            Executa os testes
  ${GREEN}health${NC}          Verifica saúde da aplicação

  ${GREEN}help${NC}            Mostra esta mensagem
  
${YELLOW}Exemplos:${NC}
  ./docker-helper.sh start        # Inicia toda a aplicação
  ./docker-helper.sh logs-app     # Visualiza logs da aplicação
  ./docker-helper.sh rebuild      # Reconstrói e reinicia tudo

EOF
}

# Comandos
case "$1" in
    start)
        print_info "Iniciando todos os serviços..."
        docker-compose up -d
        print_success "Serviços iniciados!"
        print_info "Aplicação: http://localhost:8080"
        print_info "Swagger: http://localhost:8080/swagger-ui/index.html"
        ;;
    
    start-db)
        print_info "Iniciando MySQL..."
        docker-compose up -d mysql
        print_success "MySQL iniciado!"
        ;;
    
    start-app)
        print_info "Iniciando aplicação..."
        docker-compose up -d app
        print_success "Aplicação iniciada!"
        ;;
    
    stop)
        print_info "Parando todos os serviços..."
        docker-compose stop
        print_success "Serviços parados!"
        ;;
    
    stop-db)
        print_info "Parando MySQL..."
        docker-compose stop mysql
        print_success "MySQL parado!"
        ;;
    
    stop-app)
        print_info "Parando aplicação..."
        docker-compose stop app
        print_success "Aplicação parada!"
        ;;
    
    restart)
        print_info "Reiniciando todos os serviços..."
        docker-compose restart
        print_success "Serviços reiniciados!"
        ;;
    
    restart-app)
        print_info "Reiniciando aplicação..."
        docker-compose restart app
        print_success "Aplicação reiniciada!"
        ;;
    
    logs)
        print_info "Visualizando logs (Ctrl+C para sair)..."
        docker-compose logs -f
        ;;
    
    logs-app)
        print_info "Visualizando logs da aplicação (Ctrl+C para sair)..."
        docker-compose logs -f app
        ;;
    
    logs-db)
        print_info "Visualizando logs do MySQL (Ctrl+C para sair)..."
        docker-compose logs -f mysql
        ;;
    
    build)
        print_info "Reconstruindo aplicação..."
        docker-compose build app
        print_success "Build concluído!"
        ;;
    
    rebuild)
        print_info "Reconstruindo e reiniciando todos os serviços..."
        docker-compose down
        docker-compose up -d --build
        print_success "Serviços reconstruídos e iniciados!"
        ;;
    
    status)
        print_info "Status dos serviços:"
        docker-compose ps
        ;;
    
    ps)
        print_info "Containers rodando:"
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        ;;
    
    shell-app)
        print_info "Acessando shell do container da aplicação..."
        docker exec -it spe-backend sh
        ;;
    
    
    clean)
        print_warning "Removendo containers..."
        docker-compose down
        print_success "Containers removidos!"
        ;;
    
    clean-all)
        print_warning "⚠️  ATENÇÃO: Isto irá remover containers, volumes e dados do banco!"
        read -p "Tem certeza? (s/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Ss]$ ]]; then
            docker-compose down -v
            docker rmi bpp-spe-backend-app 2>/dev/null || true
            print_success "Tudo removido!"
        else
            print_info "Operação cancelada."
        fi
        ;;
    
    test)
        print_info "Executando testes..."
        ./mvnw test
        ;;
    
    health)
        print_info "Verificando saúde da aplicação..."
        response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
        if [ "$response" = "200" ]; then
            print_success "Aplicação está saudável! (HTTP $response)"
        else
            print_error "Aplicação pode estar com problemas (HTTP $response)"
        fi
        ;;
    
    help|--help|-h|"")
        show_help
        ;;
    
    *)
        print_error "Comando desconhecido: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
