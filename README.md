# 🚀 GhostFollow v1.0.0 - Lanzamiento Oficial 👻

¡Bienvenidos a la primera versión estable de **GhostFollow**! 
Esta aplicación nace con un objetivo claro: devolver a los usuarios el control sobre su comunidad de Instagram, permitiéndoles descubrir quién los ha dejado de seguir, quiénes son sus verdaderos fans y cómo evoluciona su cuenta, todo ello sin comprometer su privacidad ni su contraseña.

## ✨ Funcionalidades Principales

### 1. Panel de Relaciones (El Salseo)
* 😡 **Traidores:** Detecta al instante los usuarios a los que sigues pero no te siguen de vuelta.
* 🤩 **Fans:** Descubre a los usuarios que te siguen fielmente aunque tú no los sigas.
* 🤝 **Mutuals:** Visualiza a tus amigos y conexiones bidireccionales.

### 2. Analizador de Archivos Oficiales
* Procesamiento rápido y local de archivos `.zip` exportados directamente desde Instagram.
* Sistema de comparativas: descubre exactamente cuántos y qué seguidores has ganado o perdido desde tu último análisis.

### 3. Seguidores Avanzados
* Lista completa de seguidores con buscador en tiempo real.
* Filtros avanzados por fecha: averigua quién te empezó a seguir antes o después de un día concreto.
* Paginación infinita optimizada para grandes volúmenes de datos.

### 4. Sistema de Alertas por Email 🔔
* Notificaciones automáticas directas a tu bandeja de entrada con un resumen de los seguidores ganados y perdidos tras cada análisis.

### 5. Historial Evolutivo 📈
* Registro histórico de todos tus análisis para que puedas ver la curva de crecimiento (o pérdida) de tu cuenta a lo largo del tiempo.

---

## 🔒 Seguridad y Privacidad (Nuestra Prioridad)
* **Zero-Password Policy:** Jamás pedimos las credenciales de Instagram del usuario. Trabajamos exclusivamente con el archivo oficial de exportación de datos de Meta.
* **Autenticación Segura:** Sistema de login y registro protegido con JWT (JSON Web Tokens) y contraseñas encriptadas.
* **Recuperación de cuentas:** Flujo completo de recuperación de contraseña mediante envío de códigos de 6 dígitos al correo electrónico (OTP).

---

## 📱 Interfaz y Experiencia de Usuario (UI/UX)
* **Diseño "Glassmorphism" Premium:** UI moderna con efectos de cristal templado, transparencias y gradientes dinámicos.
* **Onboarding Inteligente:** Muro de bienvenida que guía a los nuevos usuarios a subir su primer archivo antes de desbloquear el panel (cero fricción).
* **Rendimiento a 60 FPS:** Animaciones de fondo aceleradas por hardware (GPU) mediante `translate3d` y `will-change` para garantizar un scroll fluido en cualquier dispositivo móvil.
* **100% Responsive:** Interfaz adaptada al milímetro para smartphones, con tarjetas compactas y control de desbordamiento de texto.

---

## 🛠️ Stack Tecnológico
* **Frontend:** Vue 3 (Composition API), TypeScript, Vite, CSS puro (sin librerías pesadas).
* **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA.
* **Base de Datos:** PostgreSQL / MySQL.
