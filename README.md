
# AiLama
A User Installable Discord App, made using JDA-5 ( java discord api 5 ), LangChain4J, and using Ollama to provide Ai Response anywhere in discord
<br>
<br>
Please Read : [Important Things to Consider](https://github.com/ZeyoYT/AiLama/tree/master?tab=readme-ov-file#important-things-to-consider-before-using-the-project)

<br>

## Features

- Simple To Use
- can be used anywhere in discord where user installable apps are supported
- you can run you query by a different model on the fly using the model's field ( example below )
- URL RAG supported, where you can provide a url in the URL Option and the bot will take the Website as a context

<br>

## Prerequisite

- Ollama 0.28 or Greater ( [Download Here](https://ollama.com/download) )
- Java 19 or Greater
- Gradle 7.6 or Greater ( [Download Here](https://gradle.org/) )
- A Discord App ( make __[New](https://discord.com/developers/applications)__ app from discord developer portal )

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

## White-Listing Other People to use the App

<br>

_Using the Authorization Link, you can allow others to install the app, but to actually use the app you need to whitelist them_

<br>

In the Dot Env File ( .env ), there is a field called `WHITELISTED_USERS`, it must be shown something like this

```
WHITELISTED_USERS='WHITELISTED_USER_ID_HERE, WHITELISTED_USER_ID_HERE, ...'
```

<br>

in this you need to add there User ID's inside a single quote seperated by coma ( , ), for Example
```
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

<br>

## Limitations 

1. The Response will varay from model to model
2. URL RAG might not work with smaller models that support less context window
3. Only supports Ollama right now

<br>

## Command Example :

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
