# Prices API

Servicio REST que resuelve el precio aplicable a un producto de una cadena en una fecha determinada.

Dada una combinación de cadena, producto e instante, devuelve la tarifa de mayor prioridad cuya ventana de validez contiene ese instante, junto con su importe, su divisa y las fechas de aplicación.

---

## Índice

- [Stack](#stack)
- [Arquitectura](#arquitectura)
- [Ejecución](#ejecución)
- [Pruebas](#pruebas)
- [Documentación de la API](#documentación-de-la-api)
- [Decisiones y supuestos](#decisiones-y-supuestos)
- [Casos cubiertos](#casos-cubiertos)

---

## Stack

| Elemento | Elección |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.4 |
| Persistencia | Spring Data JPA sobre H2 en memoria |
| Contrato | OpenAPI 3, del que se genera la interfaz del servidor |
| Documentación | springdoc-openapi y Swagger UI |
| Pruebas unitarias y de integración | JUnit 5 y AssertJ |
| Pruebas funcionales | Karate 1.4.1 contra el servicio arrancado |
| Empaquetado | Maven Wrapper y Dockerfile multi-etapa |

---

## Arquitectura

Arquitectura hexagonal: el dominio no conoce Spring, JPA ni HTTP, y las dependencias apuntan siempre hacia el interior.

```
src/main/java/com/adolcc/prices
├── domain                      Núcleo del negocio, sin dependencias de framework
│   ├── model                   Price (tarifa) y DateRange (ventana de validez)
│   ├── policy                  ApplicablePriceSelector: la regla de prioridad
│   └── exception               PriceNotFoundException
├── application                 Casos de uso y puertos
│   ├── PriceQuery              Entrada del caso de uso
│   ├── FindApplicablePriceService
│   └── port
│       ├── in                  FindApplicablePriceUseCase
│       └── out                 PriceRepository
└── infrastructure              Adaptadores y composición
    ├── adapter
    │   ├── input/rest          Controlador, mapeo y tratamiento de errores
    │   └── output/persistence  Entidad JPA, repositorio y adaptador
    └── config                  Composición del caso de uso
```

Recorrido de una petición:

```
HTTP -> PriceRestAdapter -> FindApplicablePriceUseCase -> PriceRepository (puerto)
                                                       -> PricePersistenceAdapter -> H2
                       <- ApplicablePriceSelector (decide la tarifa aplicable)
```

El fichero `src/main/resources/static/openapi.yaml` es la fuente de verdad: la interfaz `PricesApi` y los DTO asociados se generan durante la compilación, y el adaptador REST la implementa. La documentación publicada por springdoc corresponde a ese mismo contrato.

---

## Ejecución

### En local

```bash
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8080`.

### Con Docker

```bash
docker build -t inditex-prices-api .
docker run --rm -p 8080:8080 inditex-prices-api
```

La imagen compila el proyecto en una etapa intermedia y ejecuta el artefacto sobre una JRE 21, de modo que no requiere Java ni Maven instalados en la máquina.

### Ejemplo de petición

```bash
curl "http://localhost:8080/api/v1/prices?brandId=1&productId=35455&applicationDate=2020-06-14T16:00:00"
```

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

---

## Pruebas

```bash
./mvnw test
```

| Nivel | Clase | Qué verifica |
|---|---|---|
| Unitario | `ApplicablePriceSelectorTest` | La regla de prioridad sobre un conjunto de tarifas diseñado para descartar reglas alternativas |
| Unitario | `DateRangeTest` | Inclusividad de los extremos de la ventana y su invariante de construcción |
| Unitario | `FindApplicablePriceServiceTest` | Orquestación del caso de uso: filtrado por cadena y producto, y propagación de la ausencia de tarifa |
| Integración | `PricePersistenceAdapterTest` | Consulta y mapeo contra H2 real, y los registros de ejemplo de `data.sql` |
| Funcional | `KarateAcceptanceTest` | El contrato HTTP completo: casos del enunciado, fronteras, 404 y 400 |

Los tests unitarios y de integración no arrancan el contexto web; el test funcional levanta el servicio en un puerto aleatorio y ejecuta los escenarios de Karate definidos en `src/test/resources/features`.

El informe de Karate se genera en `target/karate-reports/karate-summary.html`.

---

## Documentación de la API

| Recurso | Dirección |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| Contrato OpenAPI | `http://localhost:8080/openapi.yaml` |
| Consola H2 | `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:prices`, usuario `sa`, sin contraseña) |

---

## Decisiones y supuestos

Los siguientes puntos no quedan resueltos por el enunciado. Se documentan aquí junto con el criterio aplicado.

**Semántica de los extremos.** Los límites de la ventana de validez son inclusivos por ambos lados: un instante igual a `end_date` sigue perteneciendo a la tarifa. El enunciado no lo especifica, pero los datos de ejemplo lo revelan: la tarifa base termina el `2020-12-31 23:59:59` y no en `2021-01-01 00:00:00`, lo que solo tiene sentido si el extremo superior se incluye.

**Significado de `PRIORITY`.** Expresa precedencia de aplicabilidad, no descuento. La tarifa de mayor prioridad del ejemplo (38.95) es más cara que la base (35.50), de modo que la regla no puede basarse en comparar precios. La selección se resuelve filtrando por ventana y tomando la prioridad máxima.

**Desempate de prioridad.** No se implementa. El enunciado no lo define y con los datos de ejemplo es imposible que ocurra (las tarifas con la misma prioridad tienen ventanas disjuntas). Ante un empate, el resultado no está definido por la regla actual.

**Ubicación de la regla.** La selección de la tarifa aplicable vive en el dominio y no en la consulta SQL. El repositorio recupera las tarifas de la cadena y el producto, y el dominio decide cuál aplica. El motivo es disponer de una única fuente de verdad de la semántica de ventanas y poder probar la regla sin base de datos. Con el volumen de datos del enunciado no se justifica filtrar por fecha en SQL; en un escenario de alto volumen ese filtro sería una optimización, nunca la decisión.

**Distinción entre error y ausencia de tarifa.** Un parámetro ausente o con formato incorrecto devuelve `400`; la inexistencia de tarifa aplicable devuelve `404`. Un `brand_id` o un `product_id` sin tarifas no se considera un error de entrada, sino ausencia de tarifa, por lo que no se modela ningún catálogo de cadenas ni de productos.

**Formato de las fechas.** Se utiliza `LocalDateTime`, sin zona horaria, coherente con los datos del enunciado. El contrato declara `format: date-time` y la generación está configurada para producir el mismo tipo en la interfaz y en los DTO. El parámetro `applicationDate` viaja en formato ISO-8601 con separador `T` y sin desfase horario, por ejemplo `2020-06-14T16:00:00`.

**Moneda.** Se incluye en la respuesta aunque el enunciado no la exija entre los campos de salida, ya que forma parte del precio final.

**Importes.** Se representan con `BigDecimal` y escala de dos decimales.

**Clave primaria de la tabla.** El enunciado no define una clave y permite añadir campos, por lo que la tabla dispone de un identificador sintético autoincremental.

---

## Casos cubiertos

Combinación consultada: `brandId = 1`, `productId = 35455`. Los instantes se indican en el formato exacto que acepta el parámetro `applicationDate`.

| Caso | `applicationDate` | `priceList` | `price` |
|---|---|---|---|
| Test 1 | `2020-06-14T10:00:00` | 1 | 35.50 |
| Test 2 | `2020-06-14T16:00:00` | 2 | 25.45 |
| Test 3 | `2020-06-14T21:00:00` | 1 | 35.50 |
| Test 4 | `2020-06-15T10:00:00` | 3 | 30.50 |
| Test 5 | `2020-06-16T21:00:00` | 4 | 38.95 |

Además de esos cinco casos, la suite funcional cubre los instantes de frontera de cada ventana, la respuesta completa con las fechas de aplicación y la divisa, la ausencia de tarifa por fecha fuera de rango, la ausencia de tarifa para un producto sin registros, la falta de un parámetro obligatorio y un parámetro con formato incorrecto.

El análisis de requisitos que precede al diseño se encuentra en [`docs/entendimiento-requisitos.md`](docs/entendimiento-requisitos.md).
