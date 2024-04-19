# Use a base image with Java 19 installed
FROM openjdk:19-jdk AS build

# Copy the source code to the container
COPY . /app

# Set the working directory in the container
WORKDIR /app

# Expose any required ports (if needed)
# EXPOSE 8080

# Set the environment variables from Docker environment variables
# If the Docker environment variable is not provided, use the default value from .env.example
ARG TOKEN
ENV TOKEN=${TOKEN:-your_actual_bot_token}
ARG OLLAMA_URL
ENV OLLAMA_URL=${OLLAMA_URL:-https://your_ollama_api_url}
ARG OLLAMA_PORT
ENV OLLAMA_PORT=${OLLAMA_PORT:-11434}
ARG OLLAMA_MODEL
ENV OLLAMA_MODEL=${OLLAMA_MODEL:-my_model_name}
ARG OLLAMA_EMBEDDING_MODEL
ENV OLLAMA_EMBEDDING_MODEL=${OLLAMA_EMBEDDING_MODEL:-embedding_model}
ARG DEV_ID
ENV DEV_ID=${DEV_ID:-your_discord_user_id}
ARG WHITELISTED_USERS
ENV WHITELISTED_USERS=${WHITELISTED_USERS:-user_id_1,user_id_2,...}

# Check if any required environment variables are missing
RUN test -n "$TOKEN" && \
    test -n "$OLLAMA_URL" && \
    test -n "$OLLAMA_PORT" && \
    test -n "$OLLAMA_MODEL" && \
    test -n "$OLLAMA_EMBEDDING_MODEL" && \
    test -n "$DEV_ID" && \
    test -n "$WHITELISTED_USERS" || \
    { echo "One or more required environment variables are missing."; exit 1; }

RUN echo "TOKEN=$TOKEN" > .env \
    && echo "OLLAMA_URL=$OLLAMA_URL" >> .env \
    && echo "OLLAMA_PORT=$OLLAMA_PORT" >> .env \
    && echo "OLLAMA_MODEL=$OLLAMA_MODEL" >> .env \
    && echo "OLLAMA_EMBEDDING_MODEL=$OLLAMA_EMBEDDING_MODEL" >> .env \
    && echo "DEV_ID=$DEV_ID" >> .env \
    && echo "WHITELISTED_USERS=$WHITELISTED_USERS" >> .env

# Build the application using Gradle
RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew
RUN ./gradlew build

# Use a base image with Java 19 installed
FROM openjdk:19-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the build result from the previous stage
COPY --from=build /app/build/shadow/AiLama-1.0-all.jar /app/AiLama-1.0-all.jar

# Run the application
CMD ["java", "-jar", "AiLama-1.0-all.jar"]