# Carga (WEB/POS)

- ## Endpoint

  1. Recibe:
      - `rut` -> Rut del cliente
      - `newPrepaidTx` -> Codigo de moneda, monto y id unico de trx
      - `codCom` -> Codigo de comercio donde se realiza la carga

  2. Busca usuario por RUT en API Users.
      - La busqueda se realizara mediante la interfeaz del EJB de Users.

  3. Valida el nivel del usuario:
      - `N=0` -> Cliente no tiene prepago, no existe, bloqueado.
      - `N=1` -> Cliente completo el proceso de registro de prepago.
      - `N=2`-> 

  4. Llamar al `CDT` con los parametros:
      - `Id del movimiento` -> Se obtiene de `NewPrepaidTx`.
      - `Cta del usuario` -> (concatenar `'Prepago_' + rut del usuario`).
      - `Monto` -> Se obtiene de `NewPrepaidTx`.

  5. Evaluar respuesta del `CDT`, en caso de error y la carga se realizo por `TEF`, se inicia proceso de devolucion y se responde con el error respectivo.

  6. Calcular monto cargado y comisiones.

  7. Dejar mensaje en `ActiveMQ` para procesar la carga en Tecnocom.

  8. Responder con:
      - `Id de solicitud de carga`
      - `Monto cargado`
      - `Comisiones`


- ## Proceso asincrono de Carga (Apache Camel)

  1. Recibe:
      - `newPrepaidTx`
      - `user`
      - `codCom`

  2. Verificar si la tarjeta existe.
      - Si la tarjeta no existe, se llama al servicio de `Emision de Tarjeta`.

  3. Busca numero de `Pan` y `Contrato`.

  4. Buscar codigo de entidad.

  5. Calcular el tipo de factura.

  6. Llamar al servicio `Inclusion de movimientos` de Tecnocom.

  7. Evaluar respuesta de `Tecnocom`.
      - En caso de error No Reintentable, iniciar proceso de devolucion y reversar carga en el `CDT`.
      - En caso de error Reintentable, dejar nuevamente en la cola `ActiveMQ`.

  8. Guardar informacion retornada por `Tecnocom`.
      - `numextcta`
      - `nummovext`
      - `clamon`

  9. Enviar confirmacion de carga en el `CDT`.

  10. Si es 1era carga, llamar al servicio `Inclusion de movimientos` de Tecnocom para incluir la comision de emision de tarjeta.
