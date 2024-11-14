# Working with Avro

## Json to Avro schema

<https://konbert.com/convert/json/to/avro> > put files in this folder.

Add namespace:

```json
 "namespace": "cheetah.example.model.avrorecord",
```

## Autogenerate java classes

```sh
docker run -it --rm -v "${PWD}:/code" -v "${PWD}/.m2:/root/.m2" -w /code -e GITHUB_ACTOR -e GITHUB_TOKEN maven:3.8.6-openjdk-11 mvn compile
```

## Generate c# classes

```sh
docker run -it --rm -v "${PWD}:/code" -w /code mcr.microsoft.com/dotnet/sdk:7.0 bash -c "dotnet tool install -g Apache.Avro.Tools --version 1.11.2 && export PATH="$PATH:/root/.dotnet/tools" && avrogen -s src/main/avro/cheetah/example/InputEventAvro.avsc ComponentTest/Models/"
```