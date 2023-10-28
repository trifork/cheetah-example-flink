FROM mcr.microsoft.com/dotnet/runtime:6.0 AS base
WORKDIR /app

FROM mcr.microsoft.com/dotnet/sdk:6.0@sha256:0f4f696537a786fbe9f3f98a0ceccccc054bd1a9b81b64aa06d6acf8ecdf2db5 AS build

WORKDIR /src
COPY "NuGet-CI.Config" "NuGet.Config"
COPY . .
RUN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=secret,id=GITHUB_TOKEN \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    dotnet restore "MergeTwoStreams.ComponentTest.csproj"

USER $APP_UID
ENTRYPOINT ["dotnet","test", "MergeTwoStreams.ComponentTest.csproj", "--no-restore"]