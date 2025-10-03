# 📌 CrediYa - Plataforma de Gestión de Solicitudes de Préstamos

**CrediYa** es una plataforma que digitaliza y optimiza el proceso de solicitud de préstamos personales, eliminando la necesidad de trámites presenciales. El sistema permite a los clientes enviar solicitudes en línea, valida automáticamente su capacidad de endeudamiento y brinda a los asesores herramientas para revisar, aprobar o rechazar las solicitudes.

---

## ✨ Características Principales

- **Gestión de usuarios**: Registro de solicitantes con datos personales validados.
- **Autenticación y roles**: Acceso seguro mediante JWT, con permisos diferenciados para **administradores, asesores y clientes**.
- **Solicitud de préstamos**: Los clientes pueden registrar solicitudes con monto, plazo y tipo de préstamo.
- **Validación automática**: El sistema evalúa la **capacidad de endeudamiento** y aprueba/rechaza solicitudes de manera automática.
- **Revisión manual**: Los asesores pueden ver un listado de solicitudes y tomar la decisión final.
- **Notificaciones automáticas**: Envío de correos a solicitantes con el estado final de su crédito.
- **Reportes en tiempo real**: Panel para visualizar cantidad y monto total de préstamos aprobados.
- **Reportes programados**: Envío diario de métricas de rendimiento al administrador.
- **Contenerización y despliegue**: Microservicios empaquetados en Docker, orquestados con Docker Compose y desplegados en AWS (ECS + ALB + API + Gateway + RDS + DynamoDB).

---

## 🎥 Demo

👉 [Ver demo en YouTube](https://youtu.be/tu-video-id)

En este video se muestra el flujo completo:

- Registro y autenticación con JWT.
- Creación de una solicitud de préstamo.
- Validación automática y revisión manual por asesores.
- Notificación por correo al cliente.
- Panel de reportes en tiempo real.

---

## 🏗 Arquitectura

El sistema sigue **Clean Architecture** con separación clara de capas (dominio, aplicación, infraestructura).

- **Framework principal**: Spring WebFlux (programación reactiva).
- **Microservicios principales**:
  - **Autenticación** → Manejo de usuarios, login y roles.
  - **Solicitudes** → Gestión de solicitudes de préstamo y estados.
  - **Reportes** → Estadísticas y reportes de negocio.

Cada uno de los microservicios fueron hechos en base a [Scaffold of Clean Architecture](https://github.com/bancolombia/scaffold-clean-architecture)

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

**Arquitectura en AWS (Despliegue en la nube)**

![Arquitectura AWS](https://ik.imagekit.io/dwlmxvv6k/git/CrediYaInfraestructura.png)

---

## 🛠 Tecnologías

- **Backend**: Java + Spring WebFlux + Clean Architecture
- **Seguridad**: Spring Security + JWT

* **Persistencia**:
  - Relacional: PostgreSQL / RDS
  - NoSQL: DynamoDB
* **Mensajería**: Amazon SQS
* **Notificaciones**: Amazon SES
* **Infraestructura y monitoreo**:
  - Docker + Docker Compose
  - AWS ECS Fargate + ALB + API Gateway + CloudWatch
  - AWS Secret Manager para credenciales seguras
  - Spring Boot Actuator (health checks, métricas)
* **Documentación**: Swagger (OpenAPI 3)
* **Pruebas unitarias y cobertura**: JUnit 5, Mockito, JaCoCo
* **Spring Profiles**: Configuración flexible para entornos `local`, `docker`, `prod`.

---

## 🔌 Endpoints Clave

```plaintext
# Autenticación
POST /auth/api/v1/users    → Crea un usuario.
POST /auth/api/v1/login    → Genera token JWT.

# Solicitudes
POST /request/api/v1/requests  → Crea una solicitud de prestamo.
PUT  /request/api/v1/requests  → Actualiza el estado de solicitud de prestamo.
GET  /request/api/v1/requests  → Lista las solicitudes paginadas.

# Reportes
GET /reporting/api/v1/reports    → Lista reportes.

# Documentación
GET /auth/swagger-ui/index.html
GET /request/swagger-ui/index.html
GET /reporting/swagger-ui/index.html
```

---

## 🧪 Ejecución de Pruebas

```shell
# Generar reporte de JaCoCo
./gradlew jacocoMergedReport
```

---

## 🧑‍💻 Autor

Stefano Fabricio Rodriguez Avalos

[LinkedIn](https://www.linkedin.com/in/stefanofabriciorodriguezavalos)
