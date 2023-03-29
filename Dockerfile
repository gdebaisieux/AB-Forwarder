FROM openjdk:17-buster AS compile_step

WORKDIR /tmp/compiles
COPY . .
RUN ls -la

RUN chmod a+x mvnw
RUN ./mvnw clean package -DskipTests
RUN mv target/*.jar compiled.jar

FROM openjdk:17-slim-buster as run_image

WORKDIR /opt/runner
COPY --from=compile_step /tmp/compiles/compiled.jar .

ENTRYPOINT ["java", "-jar","-XX:+UseContainerSupport","-XX:MinRAMPercentage=75", "-XX:MaxRAMPercentage=75", "compiled.jar", "--spring.config.location=/opt/runner/config/application.properties"]