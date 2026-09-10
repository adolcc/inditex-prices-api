Feature: Consulta del precio aplicable

  Background:
    * url baseUrl
    * path 'api/v1/prices'

  Scenario Outline: devuelve la tarifa que cubre el instante y tiene mayor prioridad
    Given param brandId = 1
    And param productId = 35455
    And param applicationDate = '<fecha>'
    When method get
    Then status 200
    And match response.productId == 35455
    And match response.brandId == 1
    And match response.priceList == <tarifa>
    And match response.price == <precio>

    Examples:
      | fecha               | tarifa | precio |
      | 2020-06-14T10:00:00 | 1      | 35.50  |
      | 2020-06-14T16:00:00 | 2      | 25.45  |
      | 2020-06-14T21:00:00 | 1      | 35.50  |
      | 2020-06-15T10:00:00 | 3      | 30.50  |
      | 2020-06-16T21:00:00 | 4      | 38.95  |

  Scenario Outline: incluye los extremos de la ventana de validez
    Given param brandId = 1
    And param productId = 35455
    And param applicationDate = '<fecha>'
    When method get
    Then status 200
    And match response.priceList == <tarifa>

    Examples:
      | fecha               | tarifa |
      | 2020-06-14T15:00:00 | 2      |
      | 2020-06-14T18:30:00 | 2      |
      | 2020-06-14T18:30:01 | 1      |
      | 2020-06-15T11:00:00 | 3      |
      | 2020-06-15T11:00:01 | 1      |
      | 2020-06-15T16:00:00 | 4      |

  Scenario: devuelve la ventana de validez y la divisa de la tarifa aplicada
    Given param brandId = 1
    And param productId = 35455
    And param applicationDate = '2020-06-14T16:00:00'
    When method get
    Then status 200
    And match response.startDate == '2020-06-14T15:00:00'
    And match response.endDate == '2020-06-14T18:30:00'
    And match response.currency == 'EUR'

  Scenario: informa de la ausencia de tarifa para el instante consultado
    Given param brandId = 1
    And param productId = 35455
    And param applicationDate = '2021-01-01T00:00:00'
    When method get
    Then status 404
    And match response.code == 'PRICE_NOT_FOUND'

  Scenario: informa de la ausencia de tarifa para un producto sin tarifas
    Given param brandId = 1
    And param productId = 12345
    And param applicationDate = '2020-06-14T10:00:00'
    When method get
    Then status 404
    And match response.code == 'PRICE_NOT_FOUND'

  Scenario: informa de la falta de un parámetro obligatorio
    Given param brandId = 1
    And param productId = 35455
    When method get
    Then status 400
    And match response.code == 'INVALID_PARAMETERS'

  Scenario: informa de un parámetro con formato incorrecto
    Given param brandId = 1
    And param productId = 35455
    And param applicationDate = 'no-es-una-fecha'
    When method get
    Then status 400
    And match response.code == 'INVALID_PARAMETERS'
