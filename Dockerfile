FROM gradle:7.5.1-jdk17 as builder

WORKDIR /app

COPY ["build.gradle", "gradlew", "./"]
COPY gradle gradle
RUN chmod +x gradlew
RUN ./gradlew downloadRepos

COPY . .
RUN chmod +x gradlew
RUN ./gradlew installDist

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache ca-certificates

WORKDIR /app
COPY --from=builder /app .

EXPOSE 7070
ENTRYPOINT ["/app/build/install/cart-service/bin/CartService"]

