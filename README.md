
# AiLama
A User Installable Discord App, made using JDA-5 ( java discord api 5 ), LangChain4J, and using Ollama to provide Ai Response anywhere in discord
<br>
<br>
Please Read : [Important Things to Consider](#important-things-to-consider-before-using-the-project)

<br>

## Table of Contents

- [Features](#features)
- [Prerequisite](#prerequisite)
- [How-To](#how-to)
- [Whitelisting Other People to use the App](#whitelisting-other-people-to-use-the-app)
- [Important Things to Consider before Using the Project](#important-things-to-consider-before-using-the-project)
- [Limitations](#limitations)
- [Run Using Docker](#run-using-docker)
    - [Using Docker Compose](#docker-compose-file-)
    - [Manual Method](#manual-method--)
- [Build Docker Image Your self](#build-docker-image-your-self)
- [SearXNG Guide](#searxng-guide)
    - [Setting SearXNG](#setting-searxng--)
    - [Adding Environment Variables to AiLama](#adding-environment-variables-to-ailama--)
- [List of Commands](#list-of-commands--)

<br>


## Features

- Simple To Use
- Web Search Capabilities using SearXNG
- Web Document for RAG Support using URL
- Function Calling on AI Command with DOT Notation ( example below )
- Normal Document Support
- can be used anywhere in discord where user installable apps are supported
- you can run you query by a different model on the fly using the model's field ( example below )

<br>

## Prerequisite

### Ollama :

_Ollama is an open-source app that lets you run, create, and share large language models locally with a command-line interface_

- Ollama 0.28 or Greater ( [Download Here](https://ollama.com/download) )
- Ai Model for Ollama ( [Download Here](https://ollama.com/models) ) [ [llama3](https://ollama.com/library/llama3) Preferred ]

<br>

**_There are Two ways you can use AiLama, by using Docker Image or by Building the Project and Running it_**

### Compile and Run
- Java 19 or Greater
- Gradle 7.6 or Greater ( [Download Here](https://gradle.org/) )

### Run without Compiling
- Docker ( [Download Here](https://www.docker.com/) )

<br>

### Discord App
- A Discord Bot/App ( make __[New](https://discord.com/developers/applications)__ app from discord developer portal )

<br>

## Optional Prerequisite

_These are Optional Prerequisite, you can use the bot without them, but to get access to more features its recommended to install the following programs_

### Web Search
- SearXNG for Web Search ( [Setup Here](https://github.com/searxng/searxng?tab=readme-ov-file#setup) ) ( [Guide by AiLama Here](#searxng-guide) )

### Image Generation
- Automatic1111 for Image Generation ( [ Download Here ](https://github.com/AUTOMATIC1111/stable-diffusion-webui) )

<br>

## How-To

1. First Clone this Repo

```
https://github.com/ZeyoYT/AiLama.git
```

2. Change Directory

```
cd AiLama
```

3. Create Dot Env File ( check [.env.example](https://github.com/ZeyoYT/AiLama/blob/master/.env.example) ) and Fill Details
```
mv ./.env.example ./.env
```

5. Build Project
```
./gradlew build
```

<br>
<br>

After Building the project, a new Jar File would be made in `AiLama/build/shadow/`, that jar file can now be used to run the bot using the following command
```
java -jar ./<jar file name here>.jar
```

<br>

## How to Make a new Discord Installable App and Install it

1. Go to `https://discord.com/developers/applications`
2. Click 'New Application' button
3. Give it a Name and agree to TOS and Developer Policy
4. Go to Bot and Un-Select Public Bot and Enable Presence Intent & Server Members Intent
5. Click on Reset Token and Reset the bot's token
6. Copy the Shown Token and Paste it in the `.env` file
7. Save the changes
8. Go to Installation and Un-Select Guild Install, and Select User Install
9. in Install Link select `Discord Provided Link`, then copy the link and open it
10. Authorize the App

<br>
<br>

## Whitelisting Other People to use the App

<br>

_Using the Authorization Link, you can allow others to install the app, but to actually use the app you need to whitelist them_

<br>

In the Dot Env File ( .env ), there is a field called `WHITELISTED_USERS`, it must be shown something like this

```dotenv
WHITELISTED_USERS='WHITELISTED_USER_ID_HERE, WHITELISTED_USER_ID_HERE, ...'
```

<br>

in this you need to add there User ID's inside a single quote seperated by coma ( , ), for Example
```dotenv
WHITELISTED_USERS='426802118683262976, 259214353931304963, 848561799132741652'
```

<br>

**Note:** After Authorization of app, they might need to restart discord for the app to show up, and the app must be running, or else the command wont be registerd

<br>

## Important Things to Consider before Using the Project

1. This is a side project, i will try my best to maintain it
2. You Need to run the Bot to use the command and keep it running
3. Code Quality is Low because i am still new with Ai and Lang Chain
4. This project uses a unreleased feature of JDA-5 that Supports User Installable Apps
5. Because it's a unreleased feature, i didn't find any proper command framework, so a workaround manual command registration code was used ( will be fixed in future release )
6. The Ai does not have a memory of previous conversation (would be added in future)
7. It's my first time making a docker image, so there might be some issues with it
8. First Response might be slow because of the model loading

<br>

## Limitations

1. The Response will vary from model to model
2. URL RAG might not work with smaller models that support less context window
3. Only supports Ollama right now

<br>

## Run Using Docker 

_Docker Image is Available at [Docker Hub](https://hub.docker.com/r/zeyoog/ailama)_

<br>

### Using Docker Compose :-
#### Docker Compose File :
```yaml
version: '3.8'

services:
  app:
    image: ailama:latest
    environment:
      - TOKEN=<YOUR_BOT_TOKEN>
      - OLLAMA_URL=<YOUR_OLLAMA_URL>
      - OLLAMA_PORT=<YOUR_OLLAMA_PORT>
      - OLLAMA_MODEL=<YOUR_OLLAMA_MODEL>
      - OLLAMA_EMBEDDING_MODEL=<YOUR_OLLAMA_EMBEDDING_MODEL>
      - DEV_ID=<YOUR_DISCORD_ID>
      # Optional Parameters :-
      # Write inside single quotes and the user ids separated by commas, if you dont want to use this feature, just write '' (empty string enclosed by single quotes)
      - WHITELISTED_USERS='<user_id1>,<user_id2>,<user_id3> ... <user_idN>'
      - SEARXNG_URL=<YOUR_SEARXNG_URL>
      - SEARXNG_PORT=<YOUR_SEARXNG_PORT>
      - SEARXNG_ENGINES=<YOUR_SEARXNG_ENGINES>
      - AUTOMATIC1111_URL=<YOUR_AUTOMATIC1111_URL>
      - AUTOMATIC1111_PORT=<YOUR_AUTOMATIC1111_PORT>
      - AUTOMATIC1111_STEPS=<STEPS_FOR_IMAGE_GENERATION>
      - AUTOMATIC1111_SAMPLER_NAME=<SAMPLER_NAME>
      - AUTOMATIC1111_SCHEDULER_TYPE=<SCHEDULER_TYPE>
```
#### Run Command :
```
docker-compose up -d
```

<br>

### Manual Method :-

#### Pull Command :
```
docker pull zeyoog/ailama
```

#### Run Command :
```
docker run -e "TOKEN=<bot token>" -e OLLAMA_URL=<ollama_host_url> -e OLLAMA_PORT=<ollama_port> -e OLLAMA_MODEL=<ollama_model> -e OLLAMA_EMBEDDING_MODEL=<ollama_embedding_model> -e DEV_ID=<developer_id> -e "WHITELISTED_USERS='<whitelisted_user_id>, <whitelisted_user_id>'" ailama:latest
```

<br>

## Build Docker Image Your self

_After Building you can also use the docker compose to run the bot_

<br>

__1. Clone Repo and Change Directory :__
```
git clone https://github.com/zeyoyt/AiLama.git

cd AiLama
```

__2. Run Docker Build Command :__
```
docker build -t ailama:latest .
```

__3. Run Docker Container :__
```
docker run -e "TOKEN=<bot token>" -e OLLAMA_URL=<ollama_host_url> -e OLLAMA_PORT=<ollama_port> -e OLLAMA_MODEL=<ollama_model> -e OLLAMA_EMBEDDING_MODEL=<ollama_embedding_model> -e DEV_ID=<developer_id> -e "WHITELISTED_USERS='<whitelisted_user_id>, <whitelisted_user_id>'" ailama:latest
```

<br>

## SearXNG Guide

_WARNING :- Using SearXNG you are scrapping results of different search engines and it might get you blocked from that search engine as its against there **TOS**_

<br>

### Setting SearXNG :-

1. First Install SearXNG ( [Setup Here](https://github.com/searxng/searxng?tab=readme-ov-file#setup) )
2. After Setting SearXNG, Open the `settings.yml` file ( [Guide Here](https://docs.searxng.org/admin/settings/index.html) )
3. Find ` Search: ` ( [Guide Here](https://docs.searxng.org/admin/settings/settings_search.html) )
4. Add ` json ` to ` Formats ` like :-

```yaml
# In settings.yml

search:
  formats:
    - html
    - json
```
<br>

### Adding Environment Variables to AiLama :-

1. Add the Following Environment Variables to the `.env` file

```dotenv
SEARXNG_URL=<SEARXNG_HOST_URL>
SEARXNG_PORT=<SEARXNG_PORT>
SEARXNG_ENGINES=<SEARXNG_ENGINES>
```

2. Restart the Bot and Now you can use the Web Search Feature

<br>


## List of Commands :-
- /ai
- /web
- /document
- /image

<br>

### For More Information on Commands and Usage, Check the [Wiki](https://github.com/zeyoyt/AiLama/wiki)