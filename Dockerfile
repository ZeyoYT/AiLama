# Use a base image with Java 19 installed
FROM amazoncorretto:21.0.3 AS build

# Copy the source code to the container
COPY . /app

# Set the working directory in the container
WORKDIR /app

# Expose any required ports (if needed)
# EXPOSE 8080

# Set the environment variables from Docker environment variables
# If the Docker environment variable is not provided, use the default value from .env.example
ARG TOKEN
ENV TOKEN=${TOKEN}

ARG OLLAMA_URL
ENV OLLAMA_URL=${OLLAMA_URL}
ARG OLLAMA_PORT
ENV OLLAMA_PORT=${OLLAMA_PORT:-11434}
ARG OLLAMA_MODEL
ENV OLLAMA_MODEL=${OLLAMA_MODEL}
ARG OLLAMA_EMBEDDING_MODEL
ENV OLLAMA_EMBEDDING_MODEL=${OLLAMA_EMBEDDING_MODEL}
ARG OLLAMA_CHAT_MEMORY_UPTO
ENV OLLAMA_CHAT_MEMORY_UPTO=${OLLAMA_CHAT_MEMORY_UPTO:-1}

ARG DEV_ID
ENV DEV_ID=${DEV_ID}
ARG WHITELISTED_USERS
ENV WHITELISTED_USERS=${WHITELISTED_USERS}

# Check if any required environment variables are missing
RUN test -n "$TOKEN" && \
    test -n "$OLLAMA_URL" && \
    test -n "$OLLAMA_PORT" && \
    test -n "$OLLAMA_MODEL" && \
    test -n "$OLLAMA_EMBEDDING_MODEL" && \
    test -n "$DEV_ID" || \
    { echo "One or more required environment variables are missing."; exit 1; }

RUN echo "TOKEN=$TOKEN" > .env \
    && echo "OLLAMA_URL=$OLLAMA_URL" >> .env \
    && echo "OLLAMA_PORT=$OLLAMA_PORT" >> .env \
    && echo "OLLAMA_MODEL=$OLLAMA_MODEL" >> .env \
    && echo "OLLAMA_EMBEDDING_MODEL=$OLLAMA_EMBEDDING_MODEL" >> .env \
    && echo "OLLAMA_CHAT_MEMORY_UPTO=$OLLAMA_CHAT_MEMORY_UPTO" >> .env \
    && echo "DEV_ID=$DEV_ID" >> .env

# Add WHITELISTED_USERS to .env if provided
RUN if [ -n "$WHITELISTED_USERS" ]; then echo "WHITELISTED_USERS=$WHITELISTED_USERS" >> .env; fi

# Ask for SearXNG URL , PORT and ENGINES and dont add if not provided
ARG SEARXNG_URL
ENV SEARXNG_URL=${SEARXNG_URL}
ARG SEARXNG_PORT
ENV SEARXNG_PORT=${SEARXNG_PORT}
ARG SEARXNG_ENGINES
ENV SEARXNG_ENGINES=${SEARXNG_ENGINES}

# add searxng to .env if provided
RUN if [ -n "$SEARXNG_URL" ]; then echo "SEARXNG_URL=$SEARXNG_URL" >> .env; fi
RUN if [ -n "$SEARXNG_PORT" ]; then echo "SEARXNG_PORT=$SEARXNG_PORT" >> .env; fi
RUN if [ -n "$SEARXNG_ENGINES" ]; then echo "SEARXNG_ENGINES=$SEARXNG_ENGINES" >> .env; fi

# Build the application using Gradle
RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew
RUN ./gradlew build

# Use a base image with Java 19 installed
FROM amazoncorretto:21.0.3

# Set the working directory in the container
WORKDIR /app

# Copy the build result from the previous stage
COPY --from=build /app/build/shadow/AiLama-1.0-all.jar /app/AiLama-1.0-all.jar

# Run the application
CMD ["java", "-jar", "AiLama-1.0-all.jar"]