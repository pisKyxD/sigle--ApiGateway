# Sigle-ApiGateway

API Gateway del sistema SIGLE. Es el único punto de entrada del frontend: verifica el token de Firebase, redirige las peticiones al microservicio correspondiente y aplica circuit breakers con Resilience4j para tolerar fallos.

## Stack

- Java 17
- Spring Boot 3.4.0
- Spring Cloud Gateway MVC
- Resilience4j (Circuit Breaker)
- Firebase Admin SDK 9.2.0

## Requisitos

- Java 17+
- Maven 3.9+
- Los microservicios deben estar desplegados/corriendo

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

| Ruta | Destino | Circuit Breaker |
|---|---|---|
| `/api/auth/**` | CoreService | `coreCircuitBreaker` |
| `/api/establecimientos/**` | CoreService | `coreCircuitBreaker` |
| `/api/notificaciones/**` | CoreService | `coreCircuitBreaker` |
| `/api/dashboard/**` | CoreService | `coreCircuitBreaker` |
| `/api/listas/**` | ListasService | `listasCircuitBreaker` |
| `/api/citas/**` | CitasService | `citasCircuitBreaker` |
| `/api/pacientes/**` | PacientesService | `pacientesCircuitBreaker` |

## Resilience4j — Circuit Breaker

Cada ruta está protegida por un circuit breaker independiente. Configuración (por servicio):

| Parámetro | Valor |
|---|---|
| `sliding-window-size` | 5 |
| `failure-rate-threshold` | 50% |
| `wait-duration-in-open-state` | 10s |

Si un microservicio falla más del 50% de las últimas 5 peticiones, el circuito se abre durante 10 segundos y las peticiones se redirigen al fallback correspondiente (`/fallback/core`, `/fallback/citas`, `/fallback/listas`, `/fallback/pacientes`), devolviendo un mensaje claro de "servicio no disponible" en vez de un error 500.

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
│   ├── GatewayConfig.java       # rutas + circuit breakers
│   ├── SecurityConfig.java
│   └── CorsConfig.java
├── fallback/
│   └── FallbackController.java  # respuestas cuando un servicio cae
└── filter/
    └── FirebaseAuthFilter.java
```
