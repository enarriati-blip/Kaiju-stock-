# Kaiju Stock

Aplicación móvil Android para la **gestión y trazabilidad de inventario de una tienda de ropa** (caso Kaiju). Permite consultar el catálogo, registrar entradas, salidas y ajustes de stock, y recibir avisos cuando un producto llega a su stock mínimo.

Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC), Evaluación Parcial 2. Es la base para la Evaluación Final Transversal (EFT).

## Integrantes

| Nombre completo | Correo |
|---|---|
| Enzo Vittorio Arriati Valdivia| en.arriati@duocuc.cl |

> Trabajo individual: el proyecto lo desarrolla un único integrante.

## Tecnologías

- Kotlin y Jetpack Compose con **Material 3**
- Arquitectura **MVVM**
- Navigation Compose
- **Room** (SQLite) con KSP
- StateFlow / SharedFlow
- Mínimo Android 8.0 (API 26)

## Funcionalidades implementadas

- **Inicio de sesión** con tres perfiles de prueba (Administrador, Vendedor y Encargado de inventario).
- **Catálogo** con búsqueda por nombre o código, filtro por categoría y marca visual de stock bajo.
- **Detalle de producto** con sus datos, foto e historial de movimientos.
- **Formulario de producto** (crear y editar) con validación por campo.
- **Formulario de movimiento** (entrada, salida o ajuste) con validación de reglas de negocio.
- **Alertas** con los productos cuyo stock es menor o igual al mínimo.
- **Menú inferior según el perfil**: el Vendedor no ve Alertas.
- **Persistencia local con Room**: los datos se mantienen al cerrar la app.
- **Datos de prueba ficticios**: 11 productos y 3 usuarios, cargados solo la primera vez.

### Reglas de negocio

| Código | Regla |
|---|---|
| RN-01 | El stock nunca puede quedar negativo. |
| RN-02 | El precio debe ser mayor que 0 y el stock mínimo no puede ser negativo. |
| RN-03 | El código de producto es único. |
| RN-04 | Un ajuste de stock requiere un motivo. |
| RN-05 | Los movimientos no se modifican; guardan fecha, tipo, cantidad y usuario. |
| RN-06 | Hay alerta cuando el stock es menor o igual al mínimo. |

### Gestión de estado

El estado vive en los ViewModels dentro de `StateFlow`. Las pantallas lo observan con `collectAsState()` y se redibujan solas. Los eventos de una sola vez (mensajes y avisos de stock bajo) usan `SharedFlow`.

### Recursos nativos

1. **Notificaciones** (`nativo/NotificadorStock.kt`). Cuando un movimiento deja un producto en stock mínimo o menos, se muestra una notificación. Se pide el permiso `POST_NOTIFICATIONS` en tiempo de ejecución y, si se niega, la app sigue funcionando con un aviso en pantalla.
2. **Cámara** (`nativo/GestorFotos.kt`). Permite fotografiar cada producto desde su detalle. Se pide el permiso `CAMERA` en tiempo de ejecución. La foto se guarda en el almacenamiento privado de la app y la cámara escribe mediante un `FileProvider` con permiso temporal. La ruta se guarda en Room, así que la foto persiste.

### Animaciones

- El color del indicador de stock cambia gradualmente al pasar a stock bajo.
- El número de stock hace una transición al registrar un movimiento.
- Las pantallas cambian con fundido y un leve desplazamiento.
- El aviso de alertas del catálogo aparece y desaparece según haya productos en alerta, y el menú muestra un globo con la cantidad.

## Estructura del proyecto (MVVM)

```
app/src/main/java/.../
├── model/        Entidades y estados de formulario (Producto, Movimiento, Usuario)
├── database/     Room: DAO, base de datos (versión 2 con migración) y convertidores
├── repository/   InventarioRepository: único que conoce el DAO; datos de prueba
├── viewmodel/    InventarioViewModel y SesionViewModel: estado y reglas de negocio
├── ui/           Pantallas, componentes reutilizables y tema Material 3
├── navigation/   Rutas y NavHost
└── nativo/       Notificaciones y cámara
```

La base de datos pasó de la versión 1 a la 2 (campo `imagenRuta`) con una `Migration`, para no perder datos al actualizar.

## Instrucciones para configurar y ejecutar

**Requisitos:** Android Studio (versión reciente), JDK 17 o superior, conexión a internet la primera vez y un dispositivo o emulador con Android 8.0 (API 26) o superior.

1. Clonar el repositorio:
   ```
   git clone https://github.com/enarriati-blip/Kaiju-stock-.git
   ```
2. Abrir en Android Studio la carpeta que contiene `app/` y `settings.gradle.kts` (*File → Open*).
3. Esperar a que termine el **Gradle Sync**.
4. Conectar una tablet o teléfono con depuración USB, o crear un emulador desde *Device Manager*. Para probar la cámara, el emulador debe tener la cámara configurada (por ejemplo *VirtualScene*).
5. Presionar **Run**.
6. En el login, elegir un usuario de prueba.
7. Aceptar los permisos de **notificaciones** y **cámara** cuando la app los pida.

### Cómo probar los recursos nativos

- **Notificación:** en el catálogo elegir un producto cercano a su mínimo y registrar una salida que lo deje en stock mínimo o menos.
- **Cámara:** abrir el detalle de un producto y tocar *Tomar foto del producto*. Al cerrar y volver a abrir la app, la foto sigue ahí.

### Pruebas unitarias

```
./gradlew testDebugUnitTest
```
Verifican las reglas RN-01 a RN-06 y la persistencia usando un DAO falso en memoria.

## Decisiones de diseño

- **MVVM con Repository:** el ViewModel no conoce Room; si cambia el origen de datos, solo cambia el Repository.
- **Validación en el ViewModel:** así se prueba sin emulador y la pantalla solo muestra los errores.
- **Fotos en almacenamiento privado:** no se pide permiso de almacenamiento y otras apps no pueden leerlas.
- **Datos ficticios:** la app no usa datos de personas reales.
