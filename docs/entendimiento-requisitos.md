# Entendimiento de la prueba técnica (Inditex / GFT)

Documento de análisis previo al diseño. Recoge lo que exigen los documentos de partida, lo que implican y los puntos que el enunciado deja abiertos. No contiene decisiones de implementación.

---

## 1. Fuentes de requisitos

La prueba no tiene una sola especificación, tiene tres. Atender solo a la primera es el error más habitual.

| Fuente | Qué define realmente |
|---|---|
| Enunciado del ejercicio | El contrato funcional: datos, regla de negocio, endpoint y los 5 test exigidos |
| Recomendaciones del proceso de selección | El contrato no funcional y la rúbrica de evaluación: arquitectura, calidad, tooling y entregables |
| Bloque "Se valorará" del enunciado | El peso de la nota: diseño del servicio, calidad de código y corrección de los tests |

Las recomendaciones del proceso resultan, en la práctica, **más prescriptivas que el propio enunciado**: la arquitectura hexagonal figura allí como obligatoria, no como sugerencia.

---

## 2. El dominio: qué es realmente la tabla PRICES

`PRICES` **no es una lista de precios**. Es un **catálogo de ventanas de validez solapadas** para un mismo producto. Cada columna existe para responder una única pregunta:

> *Para una cadena, un producto y un instante concretos, ¿qué fila manda?*

### 2.1 Anatomía de cada campo

| Campo | Significado profundo |
|---|---|
| `BRAND_ID` | Cadena del grupo (1 = ZARA). Delimita el ámbito de la búsqueda |
| `PRODUCT_ID` | Artículo al que se aplica el precio |
| `START_DATE` / `END_DATE` | Mitad de la clave de selección: la ventana en la que la fila es candidata |
| `PRICE_LIST` | Identificador de la tarifa. Es literalmente "la tarifa a aplicar" que hay que devolver |
| `PRIORITY` | Desambiguador. A mayor valor numérico, mayor precedencia |
| `PRICE` | Precio final de venta (pvp). Ya es final: no se pide ningún cálculo de descuento |
| `CURR` | ISO de la moneda (EUR). Presente en los datos, ausente en los campos de salida exigidos |

### 2.2 Los dos hallazgos que cambian el diseño

**1. La fila 1 es una tarifa base "catch-all" y las filas 2, 3 y 4 son tarifas especiales que la sobrescriben.**

| Fila | `PRICE_LIST` | Ventana | `PRIORITY` | Precio | Papel |
|---|---|---|---|---|---|
| A | 1 | 2020-06-14 00:00 → 2020-12-31 23:59 | 0 | 35.50 | Tarifa base de todo el periodo |
| B | 2 | 2020-06-14 15:00 → 2020-06-14 18:30 | 1 | 25.45 | Tarifa especial (promoción puntual) |
| C | 3 | 2020-06-15 00:00 → 2020-06-15 11:00 | 1 | 30.50 | Tarifa especial (promoción de mañana) |
| D | 4 | 2020-06-15 16:00 → 2020-12-31 23:59 | 1 | 38.95 | Tarifa especial de larga duración |

Es decir: 35.50 de base, con tres ventanas donde aplica otra tarifa. El solape de A con B, C y D es **intencionado**, no un defecto de los datos. Existe precisamente para forzar el desambiguador.

**2. `PRIORITY` no significa "descuento".**

La fila D tiene `PRIORITY` 1 y un precio de 38.95, **más caro** que la base (35.50). `PRIORITY` expresa **precedencia de aplicabilidad**, no rebaja. Una implementación que asumiera "gana el más barato" superaría igualmente los 5 test del enunciado, de modo que la comparación de precios no puede formar parte de la regla de selección.

---

## 3. La regla de negocio, enunciada con precisión

Dado un instante `t` y las claves `brandId` y `productId`:

```
candidatas = { fila ∈ PRICES : fila.brandId    = brandId
                              ∧ fila.productId = productId
                              ∧ fila.startDate ≤ t ≤ fila.endDate }

ganadora   = argmax(candidatas, fila.priority)
```

El resultado devuelve las **fechas de la fila ganadora**, no la fecha de la consulta.

### 3.1 Lo que el enunciado NO especifica

| Punto | Situación |
|---|---|
| Inclusividad de los extremos | No se indica si a las 18:30:00 exactas sigue aplicando la fila B. Los test usan 10:00, 16:00 y 21:00, por lo que **evitan deliberadamente los bordes** |
| Empate de `PRIORITY` | Con estos datos es imposible: las filas 2, 3 y 4 tienen prioridad 1 pero ventanas disjuntas. No se define desempate secundario |
| Resultado vacío | No aparece en el enunciado; sí en las recomendaciones del proceso ("casos de no encontrar precio") |
| Formato de la fecha de entrada | No se especifica |
| Moneda en la respuesta | `CURR` está en los datos pero no en la lista de campos de salida |

---

## 4. Qué mide realmente cada uno de los 5 test

Los cinco instantes no son aleatorios: cada uno cubre un comportamiento distinto del motor de selección.

| Test | Instante | Candidatas | Comportamiento que valida |
|---|---|---|---|
| 1 | 14/06 10:00 | {A} | Camino feliz: solo la tarifa base |
| 2 | 14/06 16:00 | {A, B} | **Solape + prioridad**: gana B (prioridad 1) |
| 3 | 14/06 21:00 | {A} | **Salida por el límite superior**: B terminó a las 18:30, se vuelve a la base |
| 4 | 15/06 10:00 | {A, C} | Solape + prioridad en otro día; D queda excluida porque aún no ha empezado |
| 5 | 16/06 21:00 | {A, D} | La tarifa de larga duración (D) gana a la base (A) |

### 4.1 Resultados esperados

| Test | `price_list` | Precio |
|---|---|---|
| 1 | 1 | 35.50 |
| 2 | 2 | 25.45 |
| 3 | 1 | 35.50 |
| 4 | 3 | 30.50 |
| 5 | 4 | 38.95 |

Estos importes son las respuestas canónicas del ejercicio. Se derivan únicamente de "extremos inclusivos + mayor prioridad", lo que **confirma** la interpretación de la regla.

### 4.2 Cobertura adicional que la suite del proyecto aporta

Los 5 test del enunciado validan cuatro comportamientos: coincidencia única, resolución de solape, expiración de ventana y activación de ventana. **Nunca** validan:

- Los instantes exactos de frontera: 14/06 15:00:00, 14/06 18:30:00, 15/06 11:00:00, 15/06 16:00:00 y 15/06 00:00:00.
- El caso "no hay precio aplicable" (fecha fuera de todas las ventanas).
- Parámetros malformados (fecha no parseable, identificadores ausentes o con formato incorrecto).
- Producto o cadena inexistentes.

La cobertura de esos huecos es lo que distingue una entrega correcta de una entrega fuerte.

---

## 5. La rúbrica del proceso de selección, traducida a criterios verificables

| Línea de la recomendación | Traducción concreta | Cómo se verifica |
|---|---|---|
| Arquitectura hexagonal "obligatoria" | El dominio no conoce Spring, JPA ni HTTP; los puertos se definen dentro y los adaptadores fuera | Búsqueda de `org.springframework` en la capa de dominio: no debe aparecer |
| Código mínimo | Sin abstracciones especulativas ni dependencias sin uso | Cada dependencia del `pom.xml` se usa realmente |
| Nada de código muerto | Sin clases, métodos ni interfaces huérfanos | Análisis estático o lectura manual |
| Tests unitarios | La regla de selección probada en aislamiento, sin arrancar Spring | El test pasa sin levantar contexto |
| Tests funcionales | Las 5 peticiones contra un endpoint HTTP real | Karate sobre el servicio arrancado; se valora por encima del test de integración con MockMvc |
| API first | El contrato se escribe antes del código y el código lo respeta | El `openapi.yaml` existe, genera la interfaz del servidor y coincide con lo publicado por springdoc |
| Gestión de excepciones | Sin precio → 404; parámetros incorrectos → 400, con cuerpo de error estructurado | Test de los caminos de error |
| README | Cómo ejecutar y cómo probar | Existe y es veraz |
| Swagger UI y/o Docker | Documentación interactiva y/o ejecución contenerizada | `springdoc` + `/swagger-ui`, `Dockerfile` multi-etapa |
| Repositorio público | Historial de Git limpio y compartible | Commits claros, sin credenciales ni ruido de IDE |

Peso relativo observado: Swagger y Docker figuran como "altamente recomendable"; los test e2e valen más que los de integración; la arquitectura hexagonal es la **única restricción dura**.

---

## 6. Requisitos implícitos y puntos no especificados

Identificados durante el análisis. El enunciado no los resuelve; su resolución forma parte del diseño y queda documentada en el README.

1. Forma del endpoint: *query plano* frente a *estilo recurso*.
2. Formato de fecha aceptado en la petición.
3. Inclusión o no de la moneda en la respuesta.
4. Código HTTP cuando no hay precio: 404 frente a 200 con cuerpo vacío.
5. `brandId` inexistente: error sintáctico (400) o ausencia de tarifa (404). Modelar un catálogo de marcas o de productos sería código no utilizado.
6. Herramienta de test funcional.
7. Versión de Java: el entorno dispone de JDK 21, que encaja con Spring Boot 3.
8. Naturaleza de la respuesta: la regla garantiza una única tarifa ganadora, por lo que el objeto de respuesta es único y no una lista.

---

## 7. Definición de "terminado"

- Un endpoint REST que resuelve el precio de un `brandId` + `productId` + instante.
- Base de datos H2 en memoria, inicializada con las 4 filas del ejemplo.
- La regla de prioridad decidida en el dominio, no en SQL.
- Salida con producto, cadena, tarifa, ventana de aplicación y precio final.
- Contrato OpenAPI + Swagger UI.
- Empaquetado en Docker.
- Tests unitarios de la regla y tests funcionales de los 5 casos del enunciado más los huecos identificados (fronteras, 404 y 400).
- Gestión de excepciones centralizada y coherente.
- README con instrucciones de ejecución y prueba.
- Estructura hexagonal sin filtración de framework hacia el dominio.

---

## 8. Glosario de traducción

| Enunciado (ES) | Concepto en el dominio | Término técnico (EN) |
|---|---|---|
| Cadena del grupo | Marca / cadena | Brand |
| Tarifa aplicable | Identificador de tarifa | Price list / tariff |
| Precio final de venta (pvp) | Importe final | Final price |
| Fecha de aplicación | Instante de consulta | Application date |
| Desambiguador de aplicación | Precedencia | Priority |
| Moneda | Divisa ISO | Currency |
