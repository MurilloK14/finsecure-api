# ========================================================
# ESTÁGIO 1: Builder (Compilação e Empacotamento)
# ========================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# 1. Copia primeiro o wrapper e o pom.xml (Aproveitamento de Cache do Docker)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Garante permissão de execução no wrapper do Maven
RUN chmod +x mvnw

# Baixa as dependências offline (se o pom.xml não mudou, o Docker usa o cache aqui!)
RUN ./mvnw dependency:go-offline -B

# 2. Copia o código-fonte e gera o .jar da aplicação
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ========================================================
# ESTÁGIO 2: Runtime (Ambiente Final Mínimo e Seguro)
# ========================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Regra de Ouro de AppSec: Criar usuário não-root (princípio do menor privilégio)
RUN addgroup -S finsecuregroup && adduser -S finsecureuser -G finsecuregroup

# Copia EXCLUSIVAMENTE o .jar do Estágio 1 (descarta todo o código-fonte e o Maven)
COPY --from=builder /build/target/*.jar app.jar

# Define as permissões para o usuário da aplicação
RUN chown -R finsecureuser:finsecuregroup /app
USER finsecureuser

# Expõe a porta 8080
EXPOSE 8080

# Inicia a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]