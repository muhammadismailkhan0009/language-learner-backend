# syntax=docker/dockerfile:1.7

# ---- Stage 1: Build with Maven (cacheable) ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only what affects dependency resolution first
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
RUN chmod +x ./mvnw

# Warm dependency cache (prunable with `docker buildx prune`)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -Pprod -DskipTests dependency:go-offline

# Now add the sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -Pprod -DskipTests package

# ---- Stage 2: Runtime (tiny) ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Copy only the built artifact
COPY --from=build /app/target/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
