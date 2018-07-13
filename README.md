# RediSpring Music
This project is a Spring Boot app that can be run locally with a Redis database and then deployed to Pivotal Application Service (PAS) without any code changes.

## Architecture
The [RediSpring Music app](https://github.com/Redislabs-Solution-Architects/redispring-music) is a 3-tier web application built with Angular JS for the frontend, Spring Boot for the controller, and Redis for the backend.

### Domain Model
We have a single class representing a music album:
```java
@Data
@RedisHash("album")
public class Album {
	@Id
	String id;
	String title;
	@Indexed
	String artist;
	String year;
	String genre;
	String cover;
}
```
The @RedisHash annotation tells Spring Data Redis that this class is mapped to Redis hashes. @Id is used to indicate which field to use as the key. The @Indexed annotation allows for secondary indexing based on Redis sets.

### Repository
The AlbumRepository interface is a CRUD repository for objects of type Albums, using keys of type String:
```java
public interface AlbumRepository extends CrudRepository<Album, String> { }
```

### REST Controller
The AlbumController class exposes endpoints on root `/albums` that only call the CRUD repository in order to be able to perform inserts, reads, updates, and deletes on our album domain.
```java
@RestController
@RequestMapping(value = "/albums")
public class AlbumController {

	@Autowired
	private AlbumRepository repository;

	@RequestMapping(method = RequestMethod.GET)
	public Iterable<Album> albums() {
		return repository.findAll();
	}
  
  ....
```

## Pre-requisites

### Redis

You need a Redis or Redis Enterprise database running locally.

### Java JDK 8 and Maven

On MacOS:
```shell
brew tap caskroom/versions
brew cask install java8
brew install maven
```

## Running the demo locally

0. Clone the repository:
```
git clone https://github.com/Redislabs-Solution-Architects/redispring-music.git
```

1. Configure the Redis database connection
If you need to specify host and port for your Redis database, add these entries to a `application.properties` file at the root of the project:
```
spring.redis.host=myhost
spring.redis.port=myport
```

2. Run the app
```
mvn spring-boot:run 
```

3. Add an album through the REST API:
```
curl -i -X PUT -H "Content-Type:application/json" -d "{  \"id\": \"1\",  \"title\": \"The Royal Scam\", \"artist\": \"Steely Dan\", \"genre\": \"Rock\", \"year\": \"1976\", \"cover\": \"https://bit.ly/2NNT4nQ\" }" http://localhost:8080/albums
```

4. View the albums in the UI
In a browser go to  http://localhost:8080

5. Use `redis-cli` to inspect the entries created
```
redis-cli keys *
redis-cli type album
redis-cli smembers album
redis-cli hgetall album:1
```

6. Enable Actuator endpoints

Add the following entries to `application.properties`:
```
management.health.redis.enabled=true
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```
Restart the app and inspect these endpoints:
```
http://localhost:8080/actuator/beans
http://localhost:8080/actuator/configprops
http://localhost:8080/actuator/health
```
*What Redis client is the app using?*  
*What connection parameters is the app using?*  
*How can we change these (e.g. Redis port number)?*


## Deploy the app to Pivotal Application Service

### Build and deploy the app

When running on PAS and if a Redis service is bound to the app, the connection strings and credentials needed to use the service will be extracted from the Cloud Foundry environment.

* Install the cf CLI by following the [instructions](https://docs.run.pivotal.io/cf-cli/install-go-cli.html) for your operating system
* Log in to a PCF instance using one of the following API URLs:
  * https://api.sys.sa1.pcf.redis.ninja
  * https://api.sys.sa2.pcf.redis.ninja
```
cf login --skip-ssl-validation -a https://api.sys.sa1.pcf.redis.ninja -u <username> -p <password> -o sa -s dev
```
  
Once you are logged in, build the application and push it to PAS using these commands:
```
mvn clean install
cf push --no-start
```
The no-start parameter is used to prevent the application from starting because it is not bound to a service yet.

The application is pushed using settings in the provided `manifest.yml` file:
```yaml
applications:
- name: redispring-music
  memory: 1024M
  path: target/redispring-music-0.0.1-SNAPSHOT.jar
```

### Create and bind a service

These steps can be used to bind a service that is managed by the platform: 

```shell
# view the services available
cf services
# bind the service instance to the application
cf bind-service <app name> <service name>
# restart the application so the new service is detected
cf start redispring-music
```

The output from the last command will show the URL that has been assigned to the application.

### Change bound services

To test the application with different services, you can simply stop the app, unbind a service, bind a different database service, and start the app:

```shell
cf unbind-service <app name> <service name>
cf bind-service <app name> <service name>
cf restart
```


### Apps Manager
Access Apps Manager using your browser:
* For SA1: https://apps.sys.sa1.pcf.redis.ninja
* For SA2: https://apps.sys.sa1.pcf.redis.ninja

Inspect application parameters, including environment variables.
*Which environment variable contains the Redis Enterprise access info?*
 
### Scale the app
Scale the application so that you have 3 instances of the app running:
* Use the cf CLI
* or Apps Manager

Access the app multiple times then inspect the app logs (cf CLI or Apps Manager).
*What kind of routing is taking place for your multiple requests?*   
Requests to the app are now handled in a round-robin fashion across these 3 instances. If one of these instances were to fail, the requests would be rerouted to the remaining instances without any interruption of service.
