#Customer

Microservicio eccargado de la *Gestion de clientes* y de la comunicacion con otros servicios a traves de *ActiveMQ* 

##Tecnologias utilizadas 
**java 17**
**Spring Boot 3.5.5**
**Spring Data JPA**
**MySQL**
**Spring Security + JWT**
**ActiveMQ*
**Resilience4j**
**Redis**
*Lombok**

Estructura del proyecto 
El proyrcto esta organizado en paquetes siguiendo buenas practicas:
com.mycompany.customer
├── config         # Configuraciones (JMS, seguridad, cache, etc.)
├── controller     # Controladores REST
├── command        # commands CQRS
├── dto            # Objetos de transferencia (requests/responses)
├── entity         # Entidades JPA 
├── event          # Eventos de dominio o integracion
├── exception      # Excepciones personalizadas
├── handler        # Manejadores globales (ControllerAdvice)
├── jms            # Productores/consumidores de mensajes ActiveMQ
├── mapper         # Mappers
├── query          # query CQRS 
├── repository     # Interfaces JPA / Spring Data
├── security       # Configuración y filtros de seguridad 
└── service        # Lógica de negocio 


Ejecucion local

1- Clonar repositorio
bash
git clone https://github.com/tu-usuario/customer.git
cd customer

2- Configurar la base de datos En *application.properties* 

3- Levantar ActiveMQ

Resiliencia
Este servicio utiliza Resilience4j para implementar:
*Circuit Breaker
*Retries
*Timeouts
De esta forma, el sistema sigue funcionando aunque otros servicios esten caidos.




