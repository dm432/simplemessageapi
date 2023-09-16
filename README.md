# Simple Message API
[![Code license (GPL v3.0)](https://img.shields.io/badge/code%20license-GPL%20v3.0-green.svg?style=flat-square)](https://github.com/dm432/simplemessageapi/blob/master/LICENSE)

This is a simple reactive api that can be used as a backend for a messenger. The api is build using SpringBoot WebFlux
and a Postgres database. For authentication bearer JWTs are used. The whole project can be run inside a Kubernetes cluster.

If you want to build a messenger or something similar, feel free to clone this repo and change anything you like. 🧑🏻‍🔬

## ✨ Features
- Bearer Token Authentication 🔒
- Paginated API Responses 📄
- Can be run inside a Kuberneter Cluster (see below) 🖥️
- Uses Spring WebFlux and Kotlin Coroutines for the best performance 🚀
- Unit Tests for almost everything (Coverage > 90%) ✅

## 🔨 Start the application 

### Method 1 (Kubernetes Cluster):
#### Prerequisites:
- [Docker Engine](https://github.com/moby/moby) 
- A [Kubernetes](https://github.com/kubernetes/kubernetes) Cluster (or [Minikube](https://github.com/kubernetes/minikube))
- Kubernetes command-line tool (kubectl)

#### Steps:
- **Step 1**: Clone the repo by using `git clone`.
- **Step 2**: Change the secrets (located at `k8s/secret.yaml`) and (optional) the config (located at `k8s/config.yaml`). 
Keep in mind that the secrets need to be base64 encoded. You can do this by using `echo -n 'text_to_encode' | base64` for example.
The JWT secret key needs to be at least 256 bits strong.
- **Step 3** (only required if you are using Minikube): To access the API, you need to expose the load balancer service. To do this, just open another shell and run `minikube tunnel` there.
- **Step 4**: Apply all templates to your cluster by running `kubectl apply -f k8s/config.yaml`, `kubectl apply -f k8s/secret.yaml` and `kubectl apply -f k8s/deployment.yaml`.

The API should now be running and accessible at http://YOUR_EXTERNAL_IP:8080. Note: You can get the external ip of your cluster with `kubectl get services`.
 
### Method 2 (Development Setup):
#### Prerequisites:
- JDK 17 LTS 
- [Docker Engine](https://github.com/moby/moby) and [Docker Compose](https://github.com/docker/compose)

#### Method 2a (Run the database and the application in Docker containers):
- **Step 1**: Clone the repo by using `git clone`.
- **Step 2**: Change the secrets and the configuration for the postgres database and the application in the `docker-compose.yml` file.
Note that the JWT secret key needs to be at least 256 bits strong.
- **Step 3**: Start the containers by using `docker compose up`.


#### Method 2b (Run only the database inside the Docker container and start the application with Gradle):
- **Step 1**: Clone the repo by using `git clone`.
- **Step 2**: Change the secrets and the configuration for the postgres database in the `docker-compose.yml` file.
- **Step 3**: Start the database by running `docker compose up postgres`.
- **Step 4**: Start the application with `POSTGRES_HOST=value POSTGRES_DB=value POSTGRES_USER=value POSTGRES_PASSWORD=value JWT_SECRET_KEY=value JWT_VALIDITY_DURATION=value ./gradlew bootRun`.
Make sure that you replace all environment variable values with the actual values that you defined in the `docker-compose.yml` file. Note that the JWT secret key needs to be at least 256 bits strong. 

If you have completed one of these methods, the API should be running and accessible at [http://localhost:8080](http://localhost:8080).

## 🔧 How to use
The application comes with api-docs. You can access Swagger UI at [http://localhost:8080/api-docs.html](http://localhost:8080/api-docs.html) or (depending on your run method) at http://YOUR_EXTERNAL_IP:8080/api-docs.html .
Every API endpoint is listed and explained there.

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

The just use the `/auth/login` endpoint to log in to your new account:
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
You will get your JWT as a response. Use it to access all the other protected endpoints.
For example, to retrieve information about the current user, use:
```bash
curl -X 'GET' \
  'http://localhost:8080/api/v1/me' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer replacethiswithyouraccesstoken'
```
