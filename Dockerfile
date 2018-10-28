FROM java:8-jre
MAINTAINER Dmitriy Shevchenko <d.t.shevchenko@gmail.com>

ADD ./target/sensor-simulator.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/sensor-simulator.jar"]


EXPOSE 8100