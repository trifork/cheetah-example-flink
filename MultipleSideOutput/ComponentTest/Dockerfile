FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build

WORKDIR /src
RUN chown -R app:app /src
COPY "NuGet-CI.Config" "NuGet.Config"
COPY . .

USER app
RUN \
    --mount=type=secret,id=GITHUB_ACTOR,uid=$APP_UID \
    --mount=type=secret,id=GITHUB_TOKEN,uid=$APP_UID \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    dotnet restore "MultipleSideOutputExample.ComponentTest.csproj"

## Only here for readability and convention
USER app 

ENTRYPOINT ["dotnet","test", "MultipleSideOutputExample.ComponentTest.csproj", "--no-restore"]