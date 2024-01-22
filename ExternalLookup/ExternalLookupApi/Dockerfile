FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
WORKDIR /app

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build

WORKDIR /src
COPY "NuGet-CI.Config" "NuGet.Config"
COPY . .
RUN --mount=type=secret,id=GITHUB_ACTOR \
    --mount=type=secret,id=GITHUB_TOKEN \
    GITHUB_ACTOR="$(cat /run/secrets/GITHUB_ACTOR)" \
    GITHUB_TOKEN="$(cat /run/secrets/GITHUB_TOKEN)" \
    dotnet restore "ExternalLookupAPI.csproj"
RUN dotnet build "ExternalLookupAPI.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "ExternalLookupAPI.csproj" -c Release -o /app/publish

FROM base AS final
ENV COMPlus_EnableDiagnostics=0
# Create a new user with bash and a homedir
RUN useradd -r -u 1001 -m -s /bin/bash dotnetuser
# Set the user for subsequent commands
USER 1001

WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "ExternalLookupAPI.dll", "--no-restore"]