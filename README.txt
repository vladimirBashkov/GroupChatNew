MY PROTECTED SIMPLE CHAT

Эта программа микро месседжера для обмена сообщениями.
Для локального запуска в IntelliJ IDEA просто раскомментируйте в файле:
/src/main/resources/application.yml часть с "for localhost start"
и заскомментируйте часть с "for docker server start"
Также нужно поменять свои настройки пользователя для MySQL:
    username: root-(Свой пользователь)
    password: password-(Свой пароль для пользователя)
    url: jdbc:mysql://localhost:3306/mypsch_db
и создать в MySQL БД с названием: mypsch_db
Ну или же просто запускаем через Docker-compose через комманду:
    docker-compose up
в терминале (иногда приходится дважды запускать контейнер server изза раннего старта, несмотря на ожидания запуска MySQL)


Для полного запуска на сервере инструкция будет чуть позже :)



