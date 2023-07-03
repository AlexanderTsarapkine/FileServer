Requires:
- Java 20
- mvn 3.9.3

To run the program run the following in the fileServer directory:
- ./mvnw spring-boot:run

View health of application
- curl localhost:8080/actuator/health

Remotely shutdown application (not enabled):
    curl -X POST localhost:8080/actuator/shutdown

    There is also an /actuator/shutdown endpoint, but, by default, it is visible only through JMX. To enable it as an HTTP endpoint http://docs.spring.io/spring-boot/docs/2.5.0/reference/htmlsingle/#production-ready-endpoints-enabling-endpoints, add management.endpoint.shutdown.enabled=true to your application.properties file and expose it with management.endpoints.web.exposure.include=health,info,shutdown. However, you probably should not enable the shutdown endpoint for a publicly available application.

Resources:
    https://spring.io/guides/gs/spring-boot/
    https://spring.io/guides/gs/securing-web/
