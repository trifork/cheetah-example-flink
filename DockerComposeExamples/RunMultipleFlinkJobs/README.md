# Run multiple Flink jobs

In this docker compose file is an example of how to run multiple jobs in a pipeline. This is given as an example on how to do it, and thus the Flink jobs are simple.

When running a pipeline there is a few things to keep in mind.

## Input and output of the Flink jobs should match

A key factor for the pipeline to work is to make sure the first jobs output is the second jobs input. Or at least making use the pipeline is connected correctly.

## Context matters

Note in the docker compose file, right before build the ```context``` is specified.

```bash
build:
      context: ../path/to/folder
      dockerfile: Dockerfile
```

Sometimes a docker compose file doesn't have a context, but this just implies the directory of the docker compose file is the context, another way of writing that is like below

```bash
build:
      context: .
      dockerfile: Dockerfile
```

The reason this is important to specify when using docker compose files from other directories, is that the docker file might include steps of copying files or folders from the context, thus making the build fail, if the wrong context is used.

## Memory usage

Because each job runs with a job manager and task manager, the memory usage can be a problem if a too big pipeline is constructed. It's advised to only use the jobs necessary to debug the pipeline. To run jobs in the same file, but not all at once, consider using the profiles described in the root README.

## Writing custom test

Like the template is born with a working component test that test the specific job. It's a good idea to write a test either for each step or for the pipeline as a whole. This can be done using the [cheetah.kafka](https://docs.cheetah.trifork.dev/libraries/cheetah-lib-shared-dotnet/articles/Cheetah.Kafka/TestingWithCheetahKafka.html) nuget package.

## Using depends-on

Consider using depends-on, described in the root README. To ensure the pipeline works like expected.
