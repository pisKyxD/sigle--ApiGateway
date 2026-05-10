# Sigle-ApiGateway

API Gateway del sistema SIGLE. Es el único punto de entrada del frontend, se encarga de verificar el token de Firebase y redirigir las peticiones al microservicio correspondiente.

## Stack

- Java 17
- Spring Boot 3.4.0
- Spring Cloud Gateway MVC
- Firebase Admin SDK 9.2.0

## Requisitos

- Java 17+
- Maven 3.9+
- Los microservicios deben estar corriendo

## Instalación

```bash
mvn clean package -DskipTests
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

Disponible en `http://localhost:8090`

## Docker

```bash
docker build -t sigle-api-gateway .
docker run -p 8090:10000 sigle-api-gateway
```

## Rutas

| Ruta | Destino |
|---|---|
| `/api/auth/**` | CoreService :8080 |
| `/api/establecimientos/**` | CoreService :8080 |
| `/api/notificaciones/**` | CoreService :8080 |
| `/api/dashboard/**` | CoreService :8080 |
| `/api/listas/**` | ListasService :8081 |
| `/api/citas/**` | CitasService :8082 |
| `/api/pacientes/**` | PacientesService :8083 |

## Autenticación

Todas las rutas `/api/**` requieren el header:

```
Authorization: Bearer <token Firebase>
```

Las peticiones OPTIONS pasan sin verificación (preflight CORS). Si el token es inválido devuelve 401.

## Health

```
GET http://localhost:8090/actuator/health
```

## Estructura

```
src/main/java/ApiGateway/
├── config/
│   ├── FirebaseConfig.java
│   ├── GatewayConfig.java
│   ├── SecurityConfig.java
│   └── CorsConfig.java
└── filter/
    └── FirebaseAuthFilter.java
```
