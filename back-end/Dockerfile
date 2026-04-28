# Multi-stage build para otimizar a imagem

# Estágio 1: Build da aplicação
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiar arquivos do Maven Wrapper
COPY mvnw .
COPY .mvn .mvn

# Copiar pom.xml e baixar dependências (camada cacheável)
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte e compilar
COPY src src
RUN ./mvnw package -DskipTests

# Estágio 2: Imagem final leve
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR da etapa de build
COPY --from=build /app/target/*.jar app.jar

# Expor porta
EXPOSE 8080

# Executar aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
