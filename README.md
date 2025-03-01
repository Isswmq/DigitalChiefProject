# Тестовое задание v2 - Поиск Elasticsearch/Spring

## Описание проекта

Данный проект представляет собой Spring Boot приложение с интеграцией PostgreSQL и Elasticsearch, в котором реализован поиск по товарам (продуктам) и их дочерним сущностям (SKU). Приложение поддерживает загрузку данных из базы данных в Elasticsearch, а также поиск продуктов по атрибутам, включая имена и описания продуктов и SKU.

### Основные возможности:
- Схема БД: таблицы продуктов и SKU со связью один ко многим.
- Поиск по Elasticsearch с фильтрацией по различным атрибутам продукта и SKU.
- API для управления загрузкой данных в Elasticsearch и поиска.
- Возможность фильтрации продуктов по дополнительным атрибутам SKU (`availableFrom` и `stockQuantity`) через флаг конфигурации.

## Требования

Для запуска проекта вам понадобятся:
- **Java 17** (или выше)
- **Docker** (для контейнеризации)
- **Docker Compose** (для управления несколькими контейнерами)

## Запуск проекта

### Шаг 1: Сборка проекта
Для сборки проекта используйте Gradle:

```bash
./gradlew build
```
Это действие скомпилирует проект, и создаст исполняемый JAR-файл. Gradle также автоматически загрузит все необходимые зависимости.

### Шаг 2: Сборка Docker-образа
После сборки проекта, вам необходимо создать Docker-образ приложения. Для этого выполните следующую команду:

```bash
docker build -t app-backend .
```
Эта команда создаст Docker-образ вашего Spring Boot приложения, которое затем будет использоваться для запуска в контейнере.

### Шаг 3: Запуск проекта с помощью Docker Compose
После того как образ будет успешно создан, запустите проект с помощью Docker Compose:

```bash
docker-compose up -d
```
Команда docker-compose up -d запустит все необходимые сервисы в фоновом режиме:

- **PostgreSQL — для хранения данных о продуктах и их SKU.**
- **Elasticsearch — для поиска данных.**
- **Spring Boot приложение — которое соединяется с базой данных и Elasticsearch, выполняет загрузку данных и обработку запросов поиска.**

### Шаг 4: Инициализация базы данных

При запуске контейнера PostgreSQL с помощью Docker Compose, автоматически будет выполнен SQL-скрипт `init.sql`. Этот скрипт выполняет следующие действия:
- Создаёт необходимые таблицы для сущностей продукта и SKU в базе данных.
- Заполняет таблицы тестовыми данными, что позволяет сразу использовать их для загрузки в Elasticsearch и выполнения поисковых запросов.

Файл `init.sql` содержит инструкции для создания структуры базы данных и вставки примерно 20 продуктов и 50 связанных SKU с различными значениями атрибутов.

Таким образом, при первом старте контейнера PostgreSQL все данные будут автоматически созданы и подготовлены для использования в приложении.

### Шаг 5: Использование API

После запуска всех контейнеров и успешного поднятия приложения, вы можете воспользоваться следующими API для работы с продуктами и поисковыми функциями:

#### 1. Загрузка данных из базы данных в Elasticsearch

**Метод:** `GET`

**URL:** `http://localhost:8080/api/v1/reindex`

**Описание:** Этот API вызывает службу загрузки всех данных из PostgreSQL в Elasticsearch. Каждый запуск API загружает данные в индекс Elasticsearch, включая продукты и их связанные SKU.

API также поддерживает необязательный параметр `date`, принимающий дату в формате ISO (`YYYY-MM-DD`). При указании даты в параметре запроса, загружаются только те продукты, у которых связанные SKU были добавлены после этой даты.

Пример запроса без даты:

```bash
curl -X GET http://localhost:8080/api/v1/reindex
```

Пример запроса c датой:

```bash
curl -X GET http://localhost:8080/api/v1/reindex?date=2020-02-02
```


Copy code
#### 2. Поиск продуктов по ключевому слову

**Метод:** `GET`

**URL:** `http://localhost:8080/api/v1/search`

**Описание:** Этот API позволяет осуществлять поиск продуктов по ключевому слову, которое ищется в атрибутах продукта и связанных SKU.

**Параметры запроса:**

- `keyword` (обязательный): Ключевое слово для поиска. Оно участвует в поиске по имени и описанию продукта, а также по атрибутам SKU.

**Пример запроса:**

```bash
curl -X GET "http://localhost:8080/api/v1/search?keyword=Headphones"
```
