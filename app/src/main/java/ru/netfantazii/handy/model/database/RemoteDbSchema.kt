package ru.netfantazii.handy.model.database

class RemoteDbSchema {
    companion object {
        val COLLECTION_USERS = "users" // id документа = uid пользователя
        val USER_SHORT_ID =
            "short_id" // секретный код, генерируется автоматически с помощью cloud function
        val USER_DEVICE_TOKENS = "device_tokens" // ["token 1", "token 2", "token 3"]

        val COLLECTION_FRIENDS = "friends" // id документа = uid пользователя
        val FRIEND_SHORT_ID = "short_id"
        val FRIEND_NAME = "name"
        val FRIEND_TIME = "creation_time"
        val FRIEND_VALID = "valid"
        /*  Секретный код - ключ. Сортировка по дате создания. Valid означает есть ли пользователь
        с таким секретным кодом в бд (проверяется при добавлении и при отправке сообщения). При добавлении
        невалидного ключа просто выдаем ошибку и отменяем добавление. Если вдруг ключ стал не валидным
        и это выяснилось при отправке сообщения - помечаем в бд, но не удаляем автоматически.
         {
            AH3Rsz1 : {
                name : "Маша",
                creation_time : date
                valid : true
                },
            Os7kSMQ : {
                name : "Саша",
                creation_time : date
                valid : true
                }
          }
         */

        /* Коллекция с сообщениями, которые содержат метаданные и полезную информацию - собственно
        сам пересылаемый каталог. Дата сообщения, имя отправителя и емейл отправителя запрашивается
        у контекста во время работы onCreate клауд функции.
           Контент каталога - это массив с группами. Каждая группа это мапа, в
        которой хранится имя группы и массив продуктов (в формате строк).
           Порядок групп и продуктов не указывается явно, т.к. группы и элементы массива продуктов
        будут идти в том порядке, в котором будут туда добавлены. При парсинге просто возьмем индекс
        элемента и сделаем его явной позиции у POJO.
         */

        val COLLECTION_MESSAGES = "messages"
        val MESSAGE_DATE = "date" // генерируется с помощью cloud function (из контекста)
        val MESSAGE_FROM_NAME = "from_name" // генерируется с помощью cloud function
        val MESSAGE_FROM_EMAIL = "from_email" // генерируется с помощью cloud function
        val MESSAGE_FROM_IMAGE = "from_img" // генерируется с помощью cloud function
        val MESSAGE_TO_SECRET =
            "to_secret" // секретный код по которому ищем получателя в базе и оттуда вытаскиваем девайс токены
        val MESSAGE_CATALOG_NAME = "catalog_name" // название каталога для пересылки
        val MESSAGE_CATALOG_COMMENT = "catalog_comment" // Комментарий к посылаемому каталогу

        val MESSAGE_CATALOG_CONTENT = "catalog_content"
        val MESSAGE_GROUP_NAME = "name"
        val MESSAGE_GROUP_PRODUCTS = "products"
        /*
                       [{name: "Пятерочка"
                       products: ["Лук", "Чеснок", "Сыр"]},

                       {name: "Гастроном"
                       products: ["Хлеб", "Мука", "Яйца"]},

                       {name: "Компьютерный магазин"
                       products: ["Коврик для мыши", "Флешка"]}]
         */

        val MESSAGE_ID_KEY = "message_id"


    }
}

class CloudFunctions {
    companion object{
        val UPDATE_USER_AND_TOKEN = "saveDeviceTokenAndGetSecret"
        val CHANGE_SECRET = "changeAndGetShortId"
        val DELETE_TOKEN = "deleteTokenOnLogout"
        val ADD_CONTACT = "addNewContact"
        val DELETE_CONTACT = "deleteContact"
        val UPDATE_CONTACT = "updateContact"
    }
}