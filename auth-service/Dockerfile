FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/auth-service-*.jar app.jar
EXPOSE 8082
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
