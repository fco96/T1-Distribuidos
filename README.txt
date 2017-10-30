Integrantes
Nombre: Francisco Olivares
Correo: francisco.olivars.14@sansano.usm.cl
Rol: 201473575-8

La clave de las 3 máquinas es:Distribuidos96

La máquina dist67 tiene todo el contenido relacionado con el cliente
La máquina dist68 tiene todo el contenido relacionado con el servidor de distrito
La máquina dist69 tiene todo el contenido relacionado con el servidor central

Instrucciones de ejecución:
Entre a la única carpeta que existe en la máquina y ejecute el makefile escribiendo
'make' en la terminal, una vez compilados los archivos corra los .class con el comando
'java <nombre del .class>' y se iniciará la ejecución

Se debe de ejecutar el ServerCentral, después el ServerDistrito y finalmente Cliente 

Información adicional
El servidor central siempre inicia 2 sockets en los puertos 8080 y 8081 con la finalidad de atender
peticiones y garantizar el estado distribuido de los id, respectivamente.

El servidor de distrito requiere 1 parámetro adicional, este es el ip del servidor central. Este
parámetro se pide para coordinar los id de titan como se había mencionado anteriormente.

En las 3 máquinas se deshabilitó el firewall para permitir la comunicación de los procesos con
el comando 'systemctl stop firewalld.service'

