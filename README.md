
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
- [Command Example](#command-example)
    - [Ask a Query](#ask-a-query--)
    - [Asking Different Model on the Fly](#asking-different-model-on-the-fly--)
    - [Asking Query in a User only Visible Message](#asking-query-in-a-user-only-visible-message---the-command-would-be-visible-to-the-user-who-executed-it---)
    - [Asking Query and Providing a Website URL For Context](#asking-query-and-providing-a-website-url-for-context--)

<br>

## Features

- Simple To Use
- Web Search Capabilities using SearXNG
- Web Document for RAG Support using URL
- can be used anywhere in discord where user installable apps are supported
- you can run you query by a different model on the fly using the model's field ( example below )
- URL RAG supported, where you can provide a url in the URL Option and the bot will take the Website as a context

<br>

## Prerequisite

- Ollama 0.28 or Greater ( [Download Here](https://ollama.com/download) )
- Java 19 or Greater
- Gradle 7.6 or Greater ( [Download Here](https://gradle.org/) )
- A Discord App ( make __[New](https://discord.com/developers/applications)__ app from discord developer portal )
- Optional : SearXNG for Web Search ( [Setup Here](https://github.com/searxng/searxng?tab=readme-ov-file#setup) ) ( [AiLama Guide Here](#searxng-guide) )

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
5. Because its a unreleased feature, i didnt find any proper command framework, so a workaround manual command registration code was used ( will be fixed in future release )
6. The Ai does not have a memory of previous conversation (would be added in future)
7. Its my first time making a docker image, so there might be some issues with it
8. First Response might be slow because of the model loading

<br>

## Limitations

1. The Response will varay from model to model
2. URL RAG might not work with smaller models that support less context window
3. Only supports Ollama right now
4. No Memory for previous conversations
5. Multiple Response Requests are not supported

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

2. Restart the Bot and Now you can use the Web Search FeatureS='SEARCH_ENGINE_HERE, SEARCH_ENGINE_HERE, ...'


<br>

## Command Example

### Ask a Query :-
```
/ai ask: <query here>
```

Example :-
```
/ai ask:Why is the Sky Blue
```

<br>

### Asking Different Model on the Fly :-
```
/ai ask: <query> model: <model_name_with_version>
```

Example :-
```
/ai ask:Why is the Sky Blue model:gemma:latest
```

<br>

### Asking Query in a User only Visible Message <br> ( the command would be visible to the user who executed it ) :-
```
/ai ask: <query> ephemeral: <Options : True | False>
```

Example :-
```
/ai ask:Why is the Sky Blue ephemeral:True
```

<br>

### Asking Query and Providing a Website URL For Context :-
```
/ai ask: <query> url: <url>
```

Example :-
```
/ai ask:Why is the Sky Blue url:https://www.space.com/why-is-the-sky-blue
```

<br>

### Asking Query using Web Search ( Requires SearXNG ) :-
```
/ai ask: <query> web: <Options : True | False>
```

Example :-
```
/ai ask:What is the Time web:True
```
