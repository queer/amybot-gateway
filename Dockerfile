FROM openjdk:8-jre

ENTRYPOINT ["/usr/bin/java", "-Xmx1G", "-jar", "/usr/share/amybot/gateway.jar"]

COPY target/gateway-*.jar /usr/share/amybot/gateway.jar
