FROM eclipse-temurin:17-jre
WORKDIR /app
# copy jar đã build sẵn
COPY target/config-service-*.jar app.jar
EXPOSE 8888
# tuỳ chọn: bật GC logging / heap size qua JAVA_OPTS khi run
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
