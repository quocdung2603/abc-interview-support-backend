FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/discovery-service-*.jar app.jar
EXPOSE 8761
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
