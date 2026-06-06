FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Install common development utilities.
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*
