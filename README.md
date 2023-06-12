# Simple Message API
[![Code license (GPL v3.0)](https://img.shields.io/badge/code%20license-GPL%20v3.0-green.svg?style=flat-square)](https://github.com/TheRealKabo/MPP/blob/master/LICENSE)

**Please note: This project is still in very early development and many features are still missing :(**

This is a simple reactive api that can be used as a backend for a messenger. The api is build using spring boot web flux
and a postgres database. For authentication jwt bearer tokens are used. The whole project can be run in a docker container. See below for more information.

## 🔨 How to use 

**Step 1**: Clone the repo by using `git clone`

**Step 2** (Optional but **highly recommended**): Change the database credentials and the jwt secret key in the 
`application.yml` file located at `src/main/resources`. Be sure to use a secret key with at least 256 bits.
If you want to use docker, you need to change the database credentials in the `docker-compose.yml` accordingly.

**Step 3**: To start the database, api and api-docs in a docker container just run `docker compose up` in the root directory of the repo.
If you want to start the api without docker you need to first start your database by either running `docker compose up postgres` or using your own postgres database.
To start the api without docker, just run `./gradlew bootRun`.

## 🔧 Getting started
The application comes with api docs. You can access swagger ui at [http://localhost:8080/api-docs.html](http://localhost:8080/api-docs.html).
Every api endpoint is listed and explained there.

To start, you first need to create a new user account. To achieve this, just send a post request including the username and password to the `/auth` endpoint:
```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/auth' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "myusername",
  "password": "supersecurepassword"
}'
```

The just use the `/auth/login` endpoint to login to your new account:
```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/auth/login' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "myusername",
  "password": "supersecurepassword"
}'
```
You will get your jwt access token as a response. Use it to access all the other protected endpoints.
For example, to retrieve information about the current user, use:
```bash
curl -X 'GET' \
  'http://localhost:8080/api/v1/me' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer replacethiswithyouraccesstoken'
```
