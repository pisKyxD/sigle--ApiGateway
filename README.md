# Sigle RedNorte — Eureka Server

Servidor de *service discovery* para el ecosistema de microservicios de **Sigle RedNorte**. Cada microservicio (CoreService, CitasService, ListasService, PacientesService) se registra aquí al arrancar, y el `ApiGateway` lo consulta para saber a qué instancia enrutar cada petición — sin tener que hardcodear URLs entre servicios.

## Stack

- Java 17
- Spring Boot 4.1.0
- Spring Cloud 2025.1.2 (`spring-cloud-starter-netflix-eureka-server`)

## Servicios que se registran acá

| Servicio | Repositorio |
|---|---|
| API Gateway | `sigle--ApiGateway` |
| Core Service (auth, roles, establecimientos, dashboard) | `sigle-CoreService` |
| Citas Service | `sigle-CitasService` |
| Listas Service (listas de espera, pacientes) | `sigle-ListasService` |
| Pacientes Service (notificaciones, email) | `sigle-PacientesService` |

## Cómo correr en local

```bash
./mvnw spring-boot:run
```

Por defecto levanta en el puerto `8761`. El panel de Eureka queda disponible en:
http://localhost:8761

Ahí puedes ver qué servicios están registrados y su estado (`UP`, `DOWN`, etc.) en tiempo real.

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `PORT` | `8761` | Puerto en el que escucha el servidor |
| `EUREKA_INSTANCE_HOSTNAME` | `localhost` | Hostname público con el que este Eureka se anuncia a sí mismo. **En despliegues remotos (Render, etc.) debe ser el dominio público real**, no `localhost` |

## Configuración relevante (`application.yml`)

```yaml
eureka:
  client:
    register-with-eureka: false   # Eureka no se registra a sí mismo
    fetch-registry: false         # ni descarga el registro de otro Eureka
  server:
    enable-self-preservation: false
```

`enable-self-preservation: false` hace que Eureka elimine instancias que dejaron de mandar heartbeat, en vez de asumir que es un problema de red y conservarlas igual. Para este proyecto (pocos servicios, entorno con reinicios frecuentes en Render) conviene así — si algún día hay muchos más servicios y la red es inestable, vale la pena revisar si conviene reactivarlo.

## Despliegue en Render

Este servicio corre en Render en:
https://sigle-eurekaserver.onrender.com

Variables a configurar en el dashboard de Render:
EUREKA_INSTANCE_HOSTNAME=sigle-eurekaserver.onrender.com

**Importante para los otros microservicios:** Render solo expone tráfico público por HTTPS en el puerto `443` — el puerto interno de cada app (el que asigna `PORT`) no es alcanzable desde afuera. Por eso, cualquier servicio que se registre acá estando desplegado en Render debe anunciarse con el puerto seguro `443` en vez de su puerto interno, o el `ApiGateway` no va a poder enrutarle tráfico (se cae por timeout). Los microservicios Node (`CoreService`, `CitasService`, `ListasService`, `PacientesService`) ya tienen esta lógica resuelta en su `src/config/eureka.js`.

## Docker

```bash
docker build -t sigle-eureka-server .
docker run -p 8761:10000 -e PORT=10000 sigle-eureka-server
```

## Notas

- Este servicio no tiene lógica de negocio ni base de datos — es infraestructura pura.
- Debe ser el **primer** servicio en levantar (o al menos, estar arriba) antes que el resto, ya que todos dependen de él para descubrirse entre sí.y corrigeme el de api gateway?Ejecutó 4 comandosEjecutó 4 comandosCon todo esto confirmado contra el código real, aquí está la versión corregida, lista para pegar directo en GitHub (reemplaza el README.md completo):
markdown# Sigle-ApiGateway

API Gateway del sistema SIGLE. Es el único punto de entrada del frontend: verifica el token de Firebase, redirige las peticiones al microservicio correspondiente (vía Eureka + Load Balancer), aplica circuit breakers con Resilience4j para tolerar fallos, y limita la tasa de peticiones por IP.

## Stack

- Java 17
- Spring Boot 3.4.0
- Spring Cloud Gateway MVC (`spring-cloud-starter-gateway-server-webmvc` 4.3.4)
- Spring Cloud 2024.0.1 (Eureka Client + LoadBalancer)
- Resilience4j 2.2.0 (Circuit Breaker)
- Firebase Admin SDK 9.2.0

## Requisitos

- Java 17+
- Maven 3.9+
- EurekaServer corriendo (este Gateway depende de él para descubrir los demás servicios)
- Los microservicios deben estar desplegados/corriendo y registrados en Eureka

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `PORT` | `9000` | Puerto en el que escucha el Gateway |
| `EUREKA_URI` | `http://localhost:8761/eureka/` | URL del EurekaServer |
| `FIREBASE_SERVICE_ACCOUNT` | — | JSON de la cuenta de servicio de Firebase, usado para validar los tokens de las peticiones |

## Instalación

```bash
mvn clean package -DskipTests
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

Disponible en `http://localhost:9000`

## Docker

```bash
docker build -t sigle-api-gateway .
docker run -p 9000:10000 -e PORT=10000 sigle-api-gateway
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

Cada ruta está protegida por un circuit breaker independiente (`coreCircuitBreaker`, `citasCircuitBreaker`, `listasCircuitBreaker`, `pacientesCircuitBreaker`), todos con la misma configuración:

| Parámetro | Valor |
|---|---|
| `sliding-window-size` | 10 |
| `failure-rate-threshold` | 50% |
| `wait-duration-in-open-state` | 30s |
| `permitted-number-of-calls-in-half-open-state` | 3 |

Si un microservicio falla más del 50% de las últimas 10 peticiones, el circuito se abre durante 30 segundos y las peticiones se redirigen al fallback correspondiente (`/fallback/core`, `/fallback/citas`, `/fallback/listas`, `/fallback/pacientes`), devolviendo un 503 con un mensaje claro de "servicio no disponible" en vez de un error 500 o un timeout largo. Pasado ese tiempo, deja pasar 3 peticiones de prueba (medio abierto) para ver si el servicio ya se recuperó.

## Límite de peticiones (rate limiting)

Todas las rutas `/api/**` están limitadas a **30 peticiones por minuto por IP**. Al superarlo, responde `429 Too Many Requests`:
```json
{"error": "Too Many Requests", "mensaje": "Demasiadas peticiones. Intenta en 1 minuto."}
```
Este límite se calcula con `request.getRemoteAddr()`. Si el Gateway corre detrás de un proxy/balanceador que oculta la IP real del cliente (como puede pasar en algunos despliegues cloud), todas las peticiones podrían verse con la misma IP y compartir el mismo límite entre usuarios distintos — vale la pena confirmarlo en el entorno de despliegue actual.

## Autenticación

Todas las rutas `/api/**` requieren el header:
Authorization: Bearer <token Firebase>

El token se valida contra el proyecto de Firebase configurado en `FIREBASE_SERVICE_ACCOUNT`. Las peticiones `OPTIONS` (preflight CORS) pasan sin verificación. Si el token es inválido o falta, devuelve `401`.

CORS está abierto a cualquier origen (`allowedOrigins("*")`) para los métodos `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`.

## Estructura
src/main/java/ApiGateway/
├── config/
│   ├── FirebaseConfig.java      # inicializa Firebase Admin SDK
│   ├── GatewayConfig.java       # rutas + load balancing hacia Eureka
│   ├── SecurityConfig.java      # Spring Security (stateless, sin sesiones)
│   └── CorsConfig.java          # CORS + rate limiting por IP
├── fallback/
│   └── FallbackController.java  # respuestas cuando un servicio cae
└── filter/
└── FirebaseAuthFilter.java  # valida el Bearer token en cada request
