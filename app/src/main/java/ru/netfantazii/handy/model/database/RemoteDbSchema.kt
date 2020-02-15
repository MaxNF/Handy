package ru.netfantazii.handy.model.database

object RemoteDbSchema {
    const val COLLECTION_USERS = "users" // id документа = uid пользователя
    const val USER_SHORT_ID =
        "short_id" // секретный код, генерируется автоматически с помощью cloud function
    const val USER_DEVICE_TOKENS = "device_tokens" // ["token 1", "token 2", "token 3"]

    const val COLLECTION_FRIENDS = "friends" // id документа = uid пользователя
    const val FRIEND_SHORT_ID = "short_id"
    const val FRIEND_NAME = "name"
    const val FRIEND_TIME = "creation_time"
    const val FRIEND_VALID = "valid"
    /*  Секретный код - ключ. Сортировка по дате создания. Valid означает есть ли пользователь
    с таким секретным кодом в бд (проверяется при добавлении и при отправке сообщения). При добавлении
    невалидного ключа просто выдаем ошибку и отменяем добавление. Если вдруг ключ стал не валидным
    и это выяснилось при отправке сообщения - помечаем в бд, но не удаляем автоматически.
     {
        AH3Rsz1 : {
            name : "Маша",
            creation_time : date,
            valid : true
            },
        Os7kSMQ : {
            name : "Саша",
            creation_time : date,
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

    const val COLLECTION_MESSAGES = "messages"
    const val MESSAGE_DATE = "date" // генерируется с помощью cloud function (из контекста)
    const val MESSAGE_FROM_NAME = "from_name" // генерируется с помощью cloud function
    const val MESSAGE_FROM_EMAIL = "from_email" // генерируется с помощью cloud function
    const val MESSAGE_FROM_IMAGE = "from_img" // генерируется с помощью cloud function
    const val MESSAGE_TO_SECRET =
        "to_secret" // секретный код по которому ищем получателя в базе и оттуда вытаскиваем девайс токены
    const val MESSAGE_CATALOG_NAME = "catalog_name" // название каталога для пересылки
    const val MESSAGE_CATALOG_COMMENT = "catalog_comment" // Комментарий к посылаемому каталогу

    const val MESSAGE_CATALOG_CONTENT = "catalog_content"
    const val MESSAGE_GROUP_NAME = "name"
    const val MESSAGE_GROUP_PRODUCTS = "products"
    /*
                   [{name: "Пятерочка"
                   products: ["Лук", "Чеснок", "Сыр"]},

                   {name: "Гастроном"
                   products: ["Хлеб", "Мука", "Яйца"]},

                   {name: "Компьютерный магазин"
                   products: ["Коврик для мыши", "Флешка"]}]
     */

    const val MESSAGE_ID_KEY = "message_id"
}

object CloudFunctions {
    const val REGION_EU_WEST1 = "europe-west1"

    const val UPDATE_USER_AND_TOKEN = "saveDeviceTokenAndGetSecret"
    const val CHANGE_SECRET = "changeAndGetShortId"
    const val DELETE_TOKEN_ON_LOGOUT = "deleteTokenOnLogout"
    const val ADD_CONTACT = "addNewContact"
    const val DELETE_CONTACT = "deleteContact"
    const val UPDATE_CONTACT = "updateContact"

}

object ErrorCodes {
    const val USER_IS_NOT_LOGGED_IN = "001 (user is not logged in)"
    const val MESSAGE_IS_EMPTY = "002 (message is empty)"
    const val FOUND_USER_DUPLICATE = "003 (found user duplicate)"
    const val NO_MESSAGES_SENT = "004 (no messages sent)"
    const val USER_IS_NOT_FOUND = "005 (user is not found)"
    const val INSTANCE_ID_TOKEN_NOT_FOUND = "006 (instance id token not found)"
    const val DATA_PAYLOAD_IS_NULL = "007 (data payload is null)"

}