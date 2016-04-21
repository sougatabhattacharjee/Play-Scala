# Play-Scala project

## Summary

* Scala 2.11.7
* Play 2.4
* JDBC
* anorm (inputs via PreparedStatement API)
* MySQL database 
* sbt
* specs2
* curl (to check HTTP request)
* Postman (to check HTTP request)
* [TODOs left](#todos)

## Running
on port 9000

## Check
``curl -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: 36fe2236-8655-ffef-77a6-13bd4a21e59c" -d '{
"title":"Volks", 
"fuel":"Diesel",
"price":1000,
"new": false,
"mileage":23,
"firstRegistration":"2012-03-04"
} ``

####Run -> "http://localhost:9000/caradv"

An used car advert is created. Should return ``200 OK``. If /boo is requested then 404 is returned.

#### Check Get
``curl -X GET -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: b071d976-e53e-b077-9c33-407621add812" 'http://localhost:9000/caradv'``

Return all the cars with ``200 OK``

``curl -X GET -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: eea3574f-5f99-01c3-2e17-185e39127870" 'http://localhost:9000/caradv/0fe4ec90-9186-47cc-990d-e68a0ac261ba'``
Return one car with ``200 OK``

## REST Service

Base for all HTTP requests is ``/caradv``.

### Model

JSON following parameters:

* **id** (_required_): **int** or **uuid**;
* **title** (_required_): **string**, e.g. _"Audi A4 Avant"_
* **fuel** (_required_): **fuel** type **string**, either ``Diesel`` or ``Gasoline``
* **price** (_required_): **integer**
* **new** (_required_): **boolean**, indicates if car is new or used
* **mileage** (_optional_): **integer** 
* **first registration** (_optional_): **date** without time.format ``YYYY-MM-DD``, e.g. 2014-03-24

## Services Allowed

### List all adverts
*Request*: ``GET /caradv?sort=<field>``

``sort`` could be any of the JSON fields, by default sorted by id
*Response*: 200 OK 

### Get one advert by id

*Request*: ``GET /caradv/{id}``.

*Response*: 200 OK with JSON or 404 Not Found

### Create an advert

*Request*:  ``POST /caradv``

*Response*: 201 Created with resource details

### Update by id

*Request*: ``PUT /caradv/{id}``.

*Response*: 200 if successful, 400 if JSON is malformed, 404 if advert is not found

### Delete by id

*Request*: ``DELETE /caradv/{id}``

*Response*: 204 Success, nothing to return or 404 Not found (as we don't track that resource was deleted)

Request check the existence. If it does not exist then it should return Not Found. 


## TODOs

* Switch from MySQL to Dynamo database
* UUID generation to verify uniqueness
* Test application with dummy data test cases
* Limitation (pagination) getting all adverts. Currently Data is not big, applicable for big amout of data
* Sort column parameter types, asce or desc
* Mock tests
* Deploying to Heroku or any other services
