FROM eclipse-temurin:17 as build
WORKDIR /app
COPY . .
RUN ./gradlew clean build
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/build/libs/simplemessageapi-0.0.1-SNAPSHOT.jar /app/simplemessageapi-0.0.1-SNAPSHOT.jar
CMD ["java", "-jar", "simplemessageapi-0.0.1-SNAPSHOT.jar"]