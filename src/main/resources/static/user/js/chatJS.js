$(function () {
    let menuIsActive = false;
    let chatMenuIsActive = false;
    let usersMenuIsActive = false;
    let isFirstRefreshRequest = true;
    let isServerError = false;
    let errorUpMessage = 'Error on server. Please update page.';

    let selectedChat = 0;
    let itSomeNew = false;
    let chats = [];
    let chat = {
        id: 0,
        chatName: "",
        length: 0,
        newMessageCount: 0
    }

    let messageData = {
        refreshToken: "",
        message: "",
        chatId: 0,
        replyByMessageId: 0
    }

    let userData = {
        id: 0,
        login: "",
        email: "",
        roles: []
    }

    let initializathionHeader = function () {
        let userDataFromLocalStorage = JSON.parse(localStorage.getItem("protected-chat-user-data"));
        if (userDataFromLocalStorage === null) {
            return;
        }
        userData = userDataFromLocalStorage;

        if (userData.roles.includes('ADMIN')) {
            activateChatHeader('#header-admin-console-item');
            activateChatHeader('#header-signor-console-item');
            activateChatHeader('#header-chat-item');
            activateChatHeader('#header-my-page-item');
            return;
        } else if (userData.roles.includes('SIGNOR')) {
            activateChatHeader('#header-signor-console-item');
            activateChatHeader('#header-chat-item');
            activateChatHeader('#header-my-page-item');
            return;
        } else if (userData.roles.includes('OLD') || userData.roles.includes('USER')) {
            activateChatHeader('#header-chat-item');
            activateChatHeader('#header-my-page-item');
        }
    }

    let activateChatHeader = function (field) {
        $(field).css("visibility", "visible");
        $(field).css("pointer-events", "auto");
    }

    let checkError = function(xhr , someFunction, param){
        if (JSON.parse(xhr.responseText).status == 401) {
            tryAgain(someFunction, param);
        } else {
            showError(xhr);
        }
    }

    let tryAgain = function (someFunction, param) {
        if (isFirstRefreshRequest) {
            isFirstRefreshRequest = false;
            refreshTokenFunction();
            setTimeout(() => {
                someFunction.apply(this, param);
            }, 1500);
        } else {
            alert(errorUpMessage);
            isServerError = true;
            isFirstRefreshRequest = true;
        }
    }

    let refreshTokenFunction = function () {
        if (!localStorage.getItem("protected-chat-refreshToken")) {
            return;
        }
        $.ajax({
            url: '/auth/refresh-token',
            type: 'POST',
            data: { refreshToken: localStorage.getItem("protected-chat-refreshToken") },
            statusCode: {
                403: function (xhr, status, error) {
                    window.location.href = "../../auth/auth.html";
                }
            },
            success: function (response) {
                window.localStorage.setItem("protected-chat-token", response.accessToken);
                window.localStorage.setItem("protected-chat-refreshToken", response.refreshToken);
            }
        });
    }

    let showError = function(xhr){
        alert(JSON.parse(xhr.responseText).message);
    }

    let initializathionBurgers = function () {
        $('.btn-reset.burger').on('click', function () {
            const hamburger = document.querySelector(".btn-reset.burger")
            if (menuIsActive) {
                $('nav').css('display', 'none');
                menuIsActive = false;
                hamburger.classList.toggle('active');
            } else {
                $('nav').css('display', 'flex');
                menuIsActive = true;
                hamburger.classList.toggle('active');
            }
        })

        $('.chats-menu-botton').on('click', function () {
            const hamburger = document.querySelector(".chats-menu-botton")
            if (chatMenuIsActive) {
                $('.chat-list-area').css('display', 'none');
                chatMenuIsActive = false;
                hamburger.classList.toggle('active');
            } else {
                $('.chat-list-area').css('display', 'block');
                chatMenuIsActive = true;
                hamburger.classList.toggle('active');
            }
        })

        $('.users-menu-botton').on('click', function () {
            const hamburger = document.querySelector(".users-menu-botton")
            if (usersMenuIsActive) {
                $('.users-list-area').css('display', 'none');
                usersMenuIsActive = false;
                hamburger.classList.toggle('active');
            } else {
                $('.users-list-area').css('display', 'block');
                usersMenuIsActive = true;
                hamburger.classList.toggle('active');
            }
        })
    }



    //   CHATS BLOCK


    let getChatsFromLocalStorage = function () {
        if (localStorage.getItem("protected-chat-chats-list")) {
            itSomeNew = true;
            let userChatList = JSON.parse(localStorage.getItem("protected-chat-chats-list"));
            chats = userChatList;
        }
    }

    let getChatLiFromEntity = function (chatEntity) {
        let chatLi = $('<li class="chat-list-item"></li>');
        let chatItem = $('<div class="chat-item"></div>');
        let chatItemId = $('<div class="chat-item-id">' + chatEntity.id + '</div>');
        let chatItemName = $('<div class="chat-item-name">' + chatEntity.chatName + '</div>');
        chatItem.append(chatItemId);
        chatItem.append(chatItemName);
        if (chatEntity.id === selectedChat || chatEntity.newMessageCount == 0) {

        } else {
            let chatItemCounter = $('<div class="chat-item-counter">' + chatEntity.newMessageCount + '</div>');
            chatItem.append(chatItemCounter);
        }
        chatLi.append(chatItem);
        return chatLi;
    }

    let getChats = function () {
        $.ajax({
            url: '/chat/chat',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: { id: userData.id },
            success: function (response) {
                checkChats(response);
            },
            error: function (xhr, status, error) {
                checkError(xhr, getChats, []);
            }
        });
    }

    let checkChats = function (response) {
        let correctChatList = JSON.parse(JSON.stringify(chats));
        console.log(correctChatList);
        for (i in chats) {
            let chatIsExist = false;
            for (j in response) {
                if (response[j].id === chats[i].id) {
                    chatIsExist = true;
                }
            }
            if (!chatIsExist) {

                console.log(chats[i].id);
                correctChatList = correctChatList.filter(function (chat) {
                    return chat.id !== chats[i].id;
                })
                itSomeNew = true;
            }
        }
        chats = correctChatList;

        for (k in response) {
            let newChat = response[k];
            if (chats.length > 0) {
                if (!foundChat(newChat)) {
                    addNewChatToChats(newChat);
                }
            } else addNewChatToChats(newChat);
        }
        console.log(chats);
    }

    let foundChat = function (newChat) {
        let chatIsFound = false;
        for (i in chats) {
            if (newChat.id == chats[i].id) {
                chatIsFound = true;
                if (newChat.chatName.localeCompare(chats[i].chatName) != 0) {
                    chats[i].chatName = newChat.chatName;
                    itSomeNew = true;
                }
                if (newChat.length != chats[i].length) {
                    chats[i].newMessageCount = newChat.length - chats[i].length;
                    itSomeNew = true;
                }
            }
        }
        return chatIsFound;
    }

    let addNewChatToChats = function (newChat) {
        let newChatToList = Object.assign({}, chat);
        newChatToList.id = newChat.id;
        newChatToList.chatName = newChat.chatName;
        newChatToList.length = newChat.length;
        newChatToList.newMessageCount = newChat.length;
        chats.push(newChatToList);
        itSomeNew = true;
    }

    let updateChatList = function () {
        if (itSomeNew) {
            $('.chat-list').empty();
            for (i in chats) {
                let chatItem = getChatLiFromEntity(chats[i]);
                $('.chat-list').append(chatItem);
            }

            $('.chat-item').on('click', function () {
                let newSelectedChat = parseInt($(this).children('.chat-item-id:first').text());
                if (selectedChat == newSelectedChat) {
                    return
                }
                selectedChat = newSelectedChat;
                messageData.chatId = newSelectedChat;
                messageData.replyByMessageId = 0;
                initChatMessagesFields();
                updateMessages();
                getChatUsers();
                updateChatList();
            })
            itSomeNew = false;
        }
    }



    //  MESSAGE BLOCK




    let getMessageFromEntity = function (messageEntity) {
        let message
        let messageHead
        let messageHeadLogin
        let messageId = "message-" + messageEntity.id;
        if (parseInt(messageEntity.userId) == userData.id) {
            message = $('<div class="message  my-message" id="' + messageId + '"></div>');
            messageHead = $('<div class="message-head my-message"></div>');
            messageHeadLogin = $('<div class="message-head-login my-message"></div>');
        } else {
            message = $('<div class="message" id="' + messageId + '"></div>');
            messageHead = $('<div class="message-head"></div>');
            messageHeadLogin = $('<div class="message-head-login">' + messageEntity.login + '</div>');
        }
        let messageHeadTime = $('<div class="message-head-time">' + messageEntity.time + '</div>');
        let messageHeadId = $('<div class="message-head-id">&#8470-' + messageEntity.id + '</div>')
        messageHead.append(messageHeadLogin);
        messageHead.append(messageHeadTime);
        messageHead.append(messageHeadId);
        let messageBody = $('<div class="message-body"></div>');
        let messageBodyText = $('<div class="message-body-text">' + splitBigWords(messageEntity.text) + '</div>')
        let messageBodyIdData = $('<div class="message-body-id-data">' + messageEntity.id + '</div>')
        let messageBodyDeleteBotton = $('<button type="button" class="message-body-delete-botton">Delete</button>');
        if (userData.roles.includes('ADMIN') || userData.id == messageEntity.userId) {
            messageBodyDeleteBotton.css('display', 'inline-block');
        }
        let messageBodyResponseBotton = $('<button type="button" class="message-body-response-botton">Send response</button>');
        messageBody.append(messageBodyText);
        messageBody.append(messageBodyIdData);
        messageBody.append(messageBodyDeleteBotton);
        messageBody.append(messageBodyResponseBotton);
        message.append(messageHead);
        message.append(messageBody);
        if (messageEntity.replyByMessageId != 0) {
            let messageFooter = $('<div class="message-footer-replay-by"></div>');
            let messageFooterText = $('<a href="#message-' + messageEntity.replyByMessageId + '">Response by: &#8470-' + messageEntity.replyByMessageId + '</a>');
            messageFooter.append(messageFooterText);
            message.append(messageFooter);
        }
        return message;
    }

    let checkUpdateMessages = function () {
        for (let i = 0; i < chats.length; i++) {
            if (chats[i].id === selectedChat) {
                if (chats[i].newMessageCount != 0) {
                    updateMessages(chats[i].id);
                    itSomeNew = true;
                }
            }
        }
    }

    let updateMessages = function (chatId) {
        $.ajax({
            url: '/chat/message',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: { id: userData.id, chatId: selectedChat },
            success: function (response) {
                for (let j = 0; j < response.length; j++) {
                    let messageId = "message-" + response[j].id;
                    if ($('#' + messageId).length) {

                    } else {
                        let messageItem = getMessageFromEntity(response[j]);
                        $('.chat-messages-fields').append(messageItem);
                        initMessage("#" + messageId + " ");
                    }
                }
                $('.chat-messages-fields').scrollTop($('.chat-messages-fields')[0].scrollHeight);
                const findChat = chats.find(chat => chat.id === chatId);
                findChat.length = response.length;
                findChat.newMessageCount = 0;
                let chats_serialized = JSON.stringify(chats);
                window.localStorage.setItem("protected-chat-chats-list", chats_serialized);

            },
            error: function (xhr, status, error) {
                checkError(xhr, updateMessages, [chatId]);
            }
        });
    }

    let initChatMessagesFields = function (response) {
        $('.chat-messages-fields').empty();
        $('.new-message-form').css('display', 'flex');
    }

    let initMessage = function (messageId) {
        $(messageId + ".message-body").on({
            click: function () {
                $(this).children('.message-body-response-botton').css('left', '75%')
                $(this).children('.message-body-delete-botton').css('left', '75%')
            },
            mouseleave: function () {
                $(this).children('.message-body-response-botton').css('left', '1000px')
                $(this).children('.message-body-delete-botton').css('left', '1000px')
            }
        })
        $(messageId + '.message-body-response-botton').on('click', function () {
            messageData.replyByMessageId = parseInt($(this).parent().children('.message-body-id-data').text());
        })
        $(messageId + '.message-body-delete-botton').on('click', function () {
            let messageIdToDelete = parseInt($(this).parent().children('.message-body-id-data').text());
            deleteMessage(messageIdToDelete);
        })
    }

    let deleteMessage = function (messgeId) {
        let requestDel = {
            refreshToken: localStorage.getItem("protected-chat-refreshToken"),
            messageId: messgeId
        }
        $.ajax({
            url: '/chat/delete-message',
            type: 'DELETE',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(requestDel),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (respons) {
                initChatMessagesFields();
                updateMessages(); //think
            },
            error: function (xhr, status, error) {
                checkError(xhr, deleteMessage, [messgeId])
            }
        });
    }

    let activateNewMessageForm = function () {
        $('.new-message-form').on('submit', function (event) {
            event.preventDefault();
            messageData.refreshToken = localStorage.getItem("protected-chat-refreshToken");
            messageData.message = replaceEnter(sanitize($('.input-text').val()));
            if (messageData.message) {
                sendMessage();
            }
        })
    }

    let sendMessage = function () {
        if (messageData.chatId == 0) {
            return;
        }
        $.ajax({
            url: '/chat/message',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(messageData),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (respons) {
                $('.input-text').val("");
                messageData.replyByMessageId = 0;
            },
            error: function (xhr, status, error) {
                checkError(xhr, sendMessage, [])
            }
        });
    }

    let sanitize = function (string) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#x27;',
            "/": '&#x2F;',
        };
        const reg = /[&<>"'/]/ig;
        return string.replace(reg, (match) => (map[match]));
    }

    let replaceEnter = function (string) {
        return string.replace(/(?:\r\n|\r|\n)/g, '<br>');
    }

    let splitBigWords = function (string) {
        const splittedText = string.split(/[ ]/);
        if (splittedText.length > 1) {
            let finalText = "";
            for (let i = 0; i < splittedText.length; i++) {
                finalText = finalText + splitWord(splittedText[i]) + " ";
            }
            return finalText;
        } else {
            return splitWord(string);
        }
    }

    let splitWord = function (string) {
        let width = window.innerWidth;
        let splitedSize;
        if (width < 530) {
            splitedSize = 15;
        } else if (width < 800) {
            splitedSize = 20;
        } else if (width < 1200) {
            splitedSize = 30;
        } else {
            splitedSize = 40;
        }

        if (string.length > splitedSize) {
            let splitCount = string.length / splitedSize;
            let newString = "";
            let i = 0
            for (i; i < splitCount; i++) {
                let step = string.substring(((0 + i) * splitedSize), ((1 + i) * splitedSize));
                newString = newString.concat(" ", step);
            }
            let endString = string.substring(((1 + i) * splitedSize));
            newString = newString.concat(" ", endString);
            return newString;
        } else return string;

    }



    //   USERS LIST




    let getUserItemFromEntity = function (userEntity) {
        let userLiItem = $('<li class="users-list-item"></li>');
        let userItem = $('<div class="users-item"></div>');
        let userItemLogin = $('<div class="users-item-login">' + userEntity.login + '</div>');
        let userItemUserInfo = $('<div class="user-info"></div>');
        let userItemUserInfoId = $('<div class="users-item-id">' + userEntity.id + '</div>');
        let userItemUserInfoFirstName = $('<div class="users-item-first-name">Name: ' + userEntity.firstName + '</div>');
        let userItemUserInfoSex = $('<div class="users-item-sex">Sex: ' + userEntity.sex + '</div>');
        let userItemUserInfoAge = $('<div class="users-item-age">Age: ' + userEntity.age + '</div>');
        let writeToButton = $('<button class="write-to-user-button">Write to ' + userEntity.login + '</button>');
        let sendWarnButton = $('<button class="send-warn-to-user-button">Send Warn</button>');
        let deleteUserButton = $('<button class="delete-user-button">Dellet</button>');

        userItemUserInfo.append(userItemUserInfoId);
        userItemUserInfo.append(userItemUserInfoFirstName);
        userItemUserInfo.append(userItemUserInfoSex);
        userItemUserInfo.append(userItemUserInfoAge);
        if (userData.roles.includes('ADMIN')) {
            writeToButton.css('display', 'inline-block');
            sendWarnButton.css('display', 'inline-block');
            deleteUserButton.css('display', 'inline-block')
        } else if (userData.roles.includes('SIGNOR')) {
            writeToButton.css('display', 'inline-block');
            sendWarnButton.css('display', 'inline-block');
        } else if (userData.roles.includes('OLD')) {
            writeToButton.css('display', 'inline-block');
        }
        userItemUserInfo.append(writeToButton);
        userItemUserInfo.append(sendWarnButton);
        userItemUserInfo.append(deleteUserButton);
        userItem.append(userItemLogin);
        userItem.append(userItemUserInfo);
        userLiItem.append(userItem);
        return userLiItem;
    }

    let getChatUsers = function () {
        $.ajax({
            url: '/chat/user',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: { id: userData.id, chatId: selectedChat },
            success: function (respons) {
                $('.users-list').empty();
                for (i in respons) {
                    let useerLiItem = getUserItemFromEntity(respons[i]);
                    $('.users-list').append(useerLiItem);
                }

                $(".users-list-item").on({
                    click: function () {
                        $(this).children('.users-item').children('.user-info').css('left', '50%');
                    },
                    mouseleave: function () {
                        $(this).children('.users-item').children('.user-info').css('left', '1000px');
                    }
                })

                $('.write-to-user-button').on('click', function () {
                    let userId = parseInt($(this).parent().children('.users-item-id').text());
                    createNewChat(userId);
                })

                $('.send-warn-to-user-button').on('click', function () {
                    let userId = parseInt($(this).parent().children('.users-item-id').text());
                    sendWarnToUser(userId);
                })

                $('.delete-user-button').on('click', function () {
                    let userId = parseInt($(this).parent().children('.users-item-id').text());
                    deletUserFromChat(userId);
                })
            },
            error: function (xhr, status, error) {
                checkError(xhr, getChatUsers, []);
            }
        });
    }

    let createNewChat = function (userId) {
        $.ajax({
            url: '/chat/create-new-chat',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: { id: userData.id, toUserId: userId },
            success: function (response) {
                getChats();
            },
            error: function (xhr, status, error) {
                checkError(xhr, createNewChat, [userId]);
            }
        });
    }

    let sendWarnToUser = function (userId) {
        let warnMessage = prompt("Write you warning:", "First WARNING!!!");
        let requestCreateNewWarn = {
            userId: userId,
            warnMessage: warnMessage
        }
        sendWarn(requestCreateNewWarn);
    }

    let sendWarn = function (requestCreateNewWarn) {
        $.ajax({
            url: '/chat/create-new-warn',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(requestCreateNewWarn),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                alert(response.message)
            },
            error: function (xhr, status, error) {
                checkError(xhr, sendWarn, [requestCreateNewWarn])
            }
        });
    }

    let deletUserFromChat = function (userId) {
        $.ajax({
            url: '/chat/delete-user',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: { id: userData.id, userId: userId, chatId: selectedChat },
            success: function (respons) {
                alert(respons.message);
                getChatUsers();
            },
            error: function (xhr, status, error) {
                checkError(xhr, deletUserFromChat, [userId])
            }
        });
    }


    initializathionHeader();
    initializathionBurgers();
    getChatsFromLocalStorage();
    activateNewMessageForm();
    getChats();

    setInterval(() => {
        if (!isServerError) {
            getChats();
        }
    }, 5000);

    setInterval(updateChatList, 1000);

    setInterval(() => {
        if (!isServerError && selectedChat != 0) {
            checkUpdateMessages();
        }
    }, 1000);

})