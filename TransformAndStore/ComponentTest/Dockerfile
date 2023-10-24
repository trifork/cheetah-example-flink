FROM mcr.microsoft.com/dotnet/runtime:6.0 AS base
WORKDIR /app

FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build

WORKDIR /src
COPY "NuGet-CI.Config" "NuGet.Config"
COPY . .
RUN \
    --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=secret,id=GITHUB_TOKEN \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    dotnet restore "TransformAndStore.ComponentTest.csproj"

USER $APP_UID
ENTRYPOINT ["dotnet","test", "TransformAndStore.ComponentTest.csproj", "--no-restore"]