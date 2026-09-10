Feature: Esqueleto ambulante de la API de precios

  Background:
    * url baseUrl

  Scenario: el contrato OpenAPI se publica con el servicio en marcha
    Given path 'openapi.yaml'
    When method get
    Then status 200

  Scenario: la documentacion del servicio responde
    Given path 'v3/api-docs'
    When method get
    Then status 200
