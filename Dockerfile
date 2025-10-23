FROM gradle:9-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM amazoncorretto:25.0.1-alpine
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/ktgen.jar /app/ktgen.jar
ENTRYPOINT ["java","-jar","/app/ktgen.jar"]