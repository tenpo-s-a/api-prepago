# Emision de Tarjeta (Alta rapida de cliente)

- ## Endpoint

  1. Llamar al servicio `Alta rapida` de Tecnocom.
      - Datos de entrada:
          - `nombre` -> Nombre del cliente; 
          - `apellid1` -> Apellido paterno.
          - `apellid2` -> Apellido materno.
          - `numdoc` -> Numero de documento.
          - `tipdoc` -> Tipo de documento (RUT).

  2. Almacenar los datos:
      - `contrato` -> Numero de contrato del cliente. 

  3. LLamar al servicio `Datos tarjeta` de Tecnocom.
      - Datos de entrada:
          - `contrato` -> Numero de contrato del cliente.

  4. Almacenar los datos:
      - `pan` -> Numero de la tarjeta.
      - `fecexp` -> Fecha de expiracion de la tarjeta.
