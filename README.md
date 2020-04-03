**Telegram bot task**
-
**Requirements**
* maven
* java 1.8
* docker 

**Start app**

 start database(_task/_ directory):
- docker-compose up

 start webservices(in _task/telegram-city-bot/_ directory)
- mvn clean install
- mvn spring-boot: run

start bot(in _task/bot/_ directiory)
- mvn clean install
- mvn spring-boot: run

test bot
- [follow bot link]( https://t.me/CityNotesBot)

Give me feedback :)


