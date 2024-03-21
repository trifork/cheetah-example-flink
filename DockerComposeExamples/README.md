# Docker compose examples

This folder contains examples on how to use docker compose to run Flink jobs.

## Prerequisites

It is assumed that the software described in the [local development](https://docs.cheetah.trifork.dev/getting-started/local-development.html) section of getting started is done. At least `Docker Engine` and `docker-compose` is installed.

### Disclaimer

Docker is a huge subject, and these examples aren't meant to cover everything possible. To read more about docker visit their [documentation](https://docs.docker.com/).

## Getting started

The docker compose files used in these examples are from the projects found in this repository as well. To find more information about the jobs, look in the individual folder to see their docker filer and look at the code.

When running multiple [services](https://docs.docker.com/compose/compose-file/05-services/) in a docker compose file, there are some things to keep in mind.

- Service

- Networks

- Depends_on

- Profiles

### Service

The first part of a docker compose file should be the following:

```bash
services:
  name-of-service:
    build: .
```

Note that ```services``` should only be specified once, not for every service in the docker compose file.

### Networks

The network top level element is for configuring a network the services use to connect to each other. This network will be set up as a network on the docker engine, which means services from other docker compose files can also use the network, and interact to services even though they aren't in the same file, as long as they share the same network.

As an example the ```cheetah development infrastructure``` uses the network ```cheetah-infrastructure``` as does the examples in this repository. That allows the services in this repository to use the services in the ```cheetah development infrastructure```.

This is the way to set a global networks for the entire file.

```bash
networks:
  default:
    name: "cheetah-infrastructure"
    external: true
```

Note this should be declared as a top level statement, on the file itself, not for each service.

### Depends_on

For some services, it is required that another service is started, this could a frontend needs to wait for an backend API to start up before it will work. To make sure these services starts in the correct order, use ```depends_on```.

By using ```depends_on``` on a service, it requires the other service to run before it starts up. Below is an example.

```bash
services:
  first-service:
    build: .
  depends_on:
      - second-service

  second-service:
    build: .
```

In this example ```first-service``` starts after ```second-service``` is started.

### Profiles

A docker compose file can start up many docker contains, but this might not always be the intentions. To specify which services should start together, and when, the property ```profile``` can be used. By declaring a profile, that ensures that services will only start when that profile is used. Example below.

```bash
services:
  name-of-service:
    build: .
    profiles: 
      - debug
```

To run a service with a given profile use the following command

```bash
docker compose --profile debug up
```
