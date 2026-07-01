## checkdev_notification

### Описание проекта

`checkdev_notification` — это микросервис уведомлений, отвечающий за **рассылку сообщений пользователям** через различные каналы:

* **Telegram-бот**
* **внутренняя система сообщений**
* **подписки на темы и категории**

Сервис обеспечивает централизованную доставку уведомлений о событиях (интервью, отклики, утверждения, отказы, обновления) другим участникам платформы **CheckDev**.
Он тесно интегрируется с другими микросервисами экосистемы — такими как `checkdev_auth`, `checkdev_desc`, `checkdev_mock`, `checkdev_generator` — и взаимодействует с ними через REST и Eureka.


### Основные возможности

* Отправка уведомлений пользователям через Telegram и внутренние сообщения.
* Управление подписками на категории и темы (subscribe/unsubscribe).
* Уведомления о событиях интервью, откликов и статусов.
* Генерация текстов сообщений на основе шаблонов (`MessagesGenerator`).
* Поддержка авторизации и регистрации через Telegram-бота.
* Работа с внутренней системой сообщений (Inner Messages).
* Миграции БД с помощью **Liquibase**.
* REST API для интеграции с другими микросервисами.


### Архитектура проекта

```
ru.checkdev.notification/
├── NtfSrv.java                           # Точка входа (Spring Boot)
├── config/
│   └── SecurityConfig.java               # Конфигурация безопасности
├── filter/
│   └── CorsFilter.java                   # CORS-фильтр
├── domain/                               # JPA-сущности
│   ├── Base.java
│   ├── Profile.java
│   ├── InnerMessage.java
│   ├── UserTelegram.java
│   ├── SubscribeCategory.java
│   └── SubscribeTopic.java
├── dto/                                  # Data Transfer Objects
│   ├── FeedbackNotificationDTO.java
│   ├── WisherNotifyDTO.java
│   ├── InterviewNotifyDTO.java
│   ├── CancelInterviewNotificationDTO.java
│   ├── WisherApprovedDTO.java
│   ├── WisherDismissedDTO.java
│   ├── InnerMessageDTO.java
│   ├── ProfileTgDTO.java
│   └── CategoryWithTopicDTO.java
├── repository/                           # Spring Data JPA репозитории
│   ├── InnerMessageRepository.java
│   ├── UserTelegramRepository.java
│   ├── SubscribeCategoryRepository.java
│   └── SubscribeTopicRepository.java
├── service/                              # Бизнес-логика
│   ├── NotificationMessagesService.java
│   ├── NotificationMessage.java
│   ├── NotificationMessageTg.java
│   ├── MessagesGenerator.java
│   ├── SubscribeTopicService.java
│   ├── SubscribeCategoryService.java
│   ├── UserTelegramService.java
│   ├── InnerMessageService.java
│   └── EurekaUriProvider.java
├── telegram/                             # Telegram-интеграция
│   ├── Bot.java
│   ├── TgBot.java
│   ├── TgBootFake.java
│   ├── SessionTg.java
│   ├── TgConfig.java
│   ├── config/
│   │   └── TgConfig.java
│   ├── service/
│   │   ├── TgCall.java
│   │   ├── TgAuthCallWebClint.java
│   │   └── FakeTgCallConsole.java
│   └── action/                           # Telegram-диалоги и команды
│       ├── Action.java
│       ├── SaveInnerMessageAction.java
│       ├── notify/
│       │   ├── NotifyAction.java
│       │   └── UnNotifyAction.java
│       ├── reg/                          # Регистрация пользователей
│       │   ├── RegAskNameAction.java
│       │   ├── RegPutNameAction.java
│       │   ├── RegAskEmailAction.java
│       │   ├── RegPutEmailAction.java
│       │   ├── RegCheckEmailAction.java
│       │   └── RegSaveUserAction.java
│       ├── bind/                         # Привязка Telegram к аккаунту
│       │   ├── BindAccountAction.java
│       │   ├── BindAskEmailAction.java
│       │   ├── BindAskPasswordAction.java
│       │   ├── BindPutPasswordAction.java
│       │   ├── BindPutEmailAction.java
│       │   └── UnbindAccountAction.java
│       ├── check/
│       │   └── CheckAction.java
│       ├── info/
│       │   ├── InfoAction.java
│       │   └── UnKnownRequestAction.java
│       └── forget/
│           └── ForgetAction.java
└── web/                                  # REST-контроллеры
    ├── FeedbackNotificationController.java
    ├── NotificationWisherController.java
    ├── NotificationInterviewController.java
    ├── InnerMessageController.java
    ├── SubscribeTopicController.java
    └── SubscribeCategoriesController.java
```


### Технологический стек

| Компонент            | Назначение                                 |
| -------------------- | ------------------------------------------ |
| **Java 17+**         | Язык реализации                            |
| **Spring Boot**      | Основной фреймворк                         |
| **Spring Security**  | Защита REST API                            |
| **Spring Data JPA**  | Работа с базой данных                      |
| **Liquibase**        | Управление миграциями                      |
| **PostgreSQL**       | Основная база данных                       |
| **Telegram Bot API** | Канал уведомлений                          |
| **Maven**            | Система сборки                             |
| **Jenkins**          | CI/CD пайплайн                             |
| **Eureka Client**    | Регистрация в системе обнаружения сервисов |


### Конфигурация приложения

Пример `src/main/resources/application.properties`:

```properties
spring.application.name=notification
server.port=9014

spring.datasource.url=jdbc:postgresql://localhost:5432/checkdev_notification
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.change-log=classpath:db/db.changelog-master.xml

telegram.bot.token=${BOT_TOKEN}
telegram.bot.username=@CheckDevBot

eureka.client.service-url.defaultZone=http://localhost:9009/eureka
```


### Основные REST API

| Метод                               | Путь                                       | Назначение |
| ----------------------------------- | ------------------------------------------ | ---------- |
| `POST /notification/feedback`       | Отправить уведомление о фидбэке            |            |
| `POST /notification/interview`      | Уведомить о новом или обновлённом интервью |            |
| `POST /notification/wisher`         | Уведомить желающего об изменении статуса   |            |
| `GET /inner-messages`               | Получить внутренние сообщения              |            |
| `POST /subscribe/topic`             | Подписаться на тему                        |            |
| `POST /subscribe/category`          | Подписаться на категорию                   |            |
| `DELETE /unsubscribe/topic/{id}`    | Отписаться от темы                         |            |
| `DELETE /unsubscribe/category/{id}` | Отписаться от категории                    |            |


### Telegram-интеграция

Бот реализован в пакете `telegram` и поддерживает следующие команды:

| Команда        | Назначение                      |
| -------------- | ------------------------------- |
| `/start`       | Начало взаимодействия           |
| `/register`    | Регистрация нового пользователя |
| `/bind`        | Привязка аккаунта               |
| `/check`       | Проверка статуса                |
| `/unsubscribe` | Отписка от уведомлений          |
| `/info`        | Информация о сервисе            |
| `/forget`      | Удаление данных Telegram-сессии |


### Как запустить локально

#### 1. Сборка проекта

```bash
mvn clean package
```

#### 2. Запуск

```bash
java -jar target/checkdev_notification-0.0.1-SNAPSHOT.jar
```

или:

```bash
mvn spring-boot:run
```

#### 3. Доступ

Приложение будет доступно по адресу:
[http://localhost:9014](http://localhost:9014)


### Интеграция с Eureka

Для регистрации в `cd_eureka`:

```properties
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.client.service-url.defaultZone=http://localhost:9009/eureka
```


### Dockerfile (пример)

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/checkdev_notification-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9014
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Сборка:

```bash
docker build -t checkdev-notification .
```

Запуск:

```bash
docker run -p 9014:9014 checkdev-notification
```


### Jenkins (CI/CD)

`Jenkinsfile` содержит типичный pipeline:

1. **Build** — сборка Maven-проекта
2. **Test** — выполнение unit и integration тестов
3. **Deploy** — публикация Docker-образа и деплой
