$(function () {
    let menuIsActive = false;
    let usersMenuForWorkIsActive = false;
    let usersMenuForNewChatIsActive = false;
    let isFirstRefreshRequest = true;
    let errorUpMessage = 'Error on server. Please update page.';

    let selectedUserForBlock = {
        id: 0,
        login: "",
        blockTime: 0,
        reasonOfBlocking: "",
    }

    let userToChat = {
        id: 0,
        login: ""
    }

    let usersListToChat = [];

    let newChatRequest = {
        chatName: "",
        usersList: []
    }



    // BASIC FUNCTION



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
            },
        });
    }

    let showError = function(xhr){
        alert(JSON.parse(xhr.responseText).message);
    }

    let activateBurgers = function () {
        $('.btn-reset.burger').on('click', function () {
            const hamburger = document.querySelector(".btn-reset.burger")
            if (menuIsActive) {
                $('nav').css('display', 'none');
                menuIsActive = false;
                hamburger.classList.toggle('active')
            } else {
                $('nav').css('display', 'flex');
                menuIsActive = true;
                hamburger.classList.toggle('active')
            }
        })

        $('.work-with-users-list-menu-botton').on('click', function () {
            const hamburger = document.querySelector(".work-with-users-list-menu-botton")
            let area = $('.users-list-area.work-with-users');
            if (usersMenuForWorkIsActive) {
                area.css('display', 'none');
                usersMenuForWorkIsActive = false;
                hamburger.classList.toggle('active');
            } else {
                area.css('display', 'flex');
                usersMenuForWorkIsActive = true;
                hamburger.classList.toggle('active');
            }
        })

        $('.create-new-chat-list-menu-botton').on('click', function () {
            const hamburger = document.querySelector(".create-new-chat-list-menu-botton")
            let area = $('.users-list-area.create-new-chat');
            if (usersMenuForNewChatIsActive) {
                area.css('display', 'none');
                usersMenuForNewChatIsActive = false;
                hamburger.classList.toggle('active');
            } else {
                area.css('display', 'flex');
                usersMenuForNewChatIsActive = true;
                hamburger.classList.toggle('active');
            }
        })
    }



    // BLOCK AND DELETE



    let getUsersList = function () {
        $.ajax({
            url: '/admin/signorConsole/user',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            success: function (response) {
                fillingUsersList(response, 'work-with-users');
                activateUsersListBlockAndDelete();
                fillingUsersList(response, 'create-new-chat');
                activateUsersListCreateNewChat();
            },
            error: function (xhr, status, error) {
                checkError(xhr, getUsersList, [])
            }
        });
    }

    let fillingUsersList = function (response, area) {
        $('.users-list.' + area).empty();
        for (i in response) {
            let li = getUserFromEntity(response[i], area);
            $('.users-list.' + area).append(li);
        }
    }

    let getUserFromEntity = function (connectEntity, area) {
        let li = $('<li class="user-item ' + area + '"></li>');
        let userItemLogin = $('<div class="user-item-login">' + connectEntity.login + '</div>');
        let userItemId = $('<div class="user-item-id">' + connectEntity.id + '</div>');
        let userItemMail = $('<div class="user-item-email">' + connectEntity.email + '</div>');
        let userItemRoles = $('<div class="user-item-roles"></div>');
        let userItemRolesList = $('<ul class="user-item-roles-list"></ul>');
        for (i in connectEntity.roles) {
            let role = $('<li class="user-item-role-item">' + connectEntity.roles[i] + '</li>');
            userItemRolesList.append(role);
        }
        userItemRoles.append(userItemRolesList);
        let userItemFirstName = $('<div class="user-item-first-name">' + connectEntity.firstName + '</div>');
        let userItemLastName = $('<div class="user-item-last-name">' + connectEntity.lastName + '</div>');
        let userItemSex = $('<div class="user-item-sex">' + connectEntity.sex + '</div>');
        let userItemBlockTime = $('<div class="user-item-block-time">' + connectEntity.blockTime + '</div>');
        let userItemReasonOfBlocking = $('<div class="user-item-reason-of-blocking">' + connectEntity.reasonOfBlocking + '</div>');
        let userItemStartOfBlocking = $('<div class="user-item-time-start-of-blocking">' + connectEntity.startOfBlocking + '</div>');
        li.append(userItemLogin);
        li.append(userItemId);
        li.append(userItemMail);
        li.append(userItemRoles);
        li.append(userItemFirstName);
        li.append(userItemLastName);
        li.append(userItemSex);
        li.append(userItemBlockTime);
        li.append(userItemReasonOfBlocking);
        li.append(userItemStartOfBlocking);
        return li;
    }

    let activateUsersListBlockAndDelete = function () {
        $('.user-item.work-with-users').on('click', function () {
            selectedUserForBlock.id = parseInt($(this).children('.user-item-id').text());
            selectedUserForBlock.login = $(this).children('.user-item-login').text();
            selectedUserForBlock.blockTime = parseInt($(this).children('.user-item-block-time').text());
            selectedUserForBlock.reasonOfBlocking = $(this).children('.user-item-reason-of-blocking').text();
            let warnUl = $('.user-warning-list');
            getUserWarnings(selectedUserForBlock.id, warnUl);
            getUserBlock();
            changeBlockUserButton(selectedUserForBlock.login);
            changeUnblockUserButton(selectedUserForBlock.login);
        })
    }

    let getUserWarnings = function (id, warningLists) {
        $.ajax({
            url: '/admin/signorConsole/user/warnings',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify({ id: id }),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                fillingWarning(warningLists, response);
            },
            error: function (xhr, status, error) {
                checkError(xhr, getUserWarnings, [id, warningLists])
            }
        });
    }

    let fillingWarning = function (list, response) {
        list.empty();
        if (response.length == 0) {
            list.css("background-color", "rgba(255, 0, 0, 0)");
        } else {
            list.css("background-color", "rgba(255, 0, 0, 0.5)");
        }
        for (i in response) {
            let warningElement = $('<li class="user-warning-item">' + response[i] + '</li>')
            list.append(warningElement);
        }
    }

    let getUserBlock = function () {
        $.ajax({
            url: '/admin/signorConsole/user/block',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: { id: selectedUserForBlock.id },
            success: function (response) {
                $('.input-block-time').val(response.blockTime);
                $('#textarea-block-reason').val(response.reason);
            },
            error: function (xhr, status, error) {
                checkError(xhr, getUserBlock, [])
            }
        });
    }

    let changeBlockUserButton = function (login) {
        $('.block-user-button').text('BLOCK ' + login);
    }

    let changeUnblockUserButton = function (login) {
        $('.unblock-user-button').text('UNBLOCK ' + login);
    }

    let activateBlockUserButton = function () {
        $('.block-user-button').on('click', blockUser);
    }

    let blockUser = function () {
        let blockTimeCounter = parseInt($('.input-block-time').val());
        let blockTimeReason = replaceEnter(sanitize($('#textarea-block-reason').val()));
        selectedUserForBlock.blockTime = blockTimeCounter;
        selectedUserForBlock.reasonOfBlocking = blockTimeReason;
        blockRequest();
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

    let blockRequest = function () {
        $.ajax({
            url: '/admin/signorConsole/user/block',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(selectedUserForBlock),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                alert(response.message)
                getUsersList();
            },
            error: function (xhr, status, error) {
                checkError(xhr, blockRequest, [])
            }
        });
    }

    let activateUnblockUserButton = function () {
        $('.unblock-user-button').on('click', unblockUserRequest);
    }

    let unblockUserRequest = function () {
        $.ajax({
            url: '/admin/signorConsole/user/unblock',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify({ id: selectedUserForBlock.id }),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                alert(response.message)
                getUsersList();
            },
            error: function (xhr, status, error) {
                checkError(xhr, unblockUserRequest, [])
            }
        });
    }



    // CREEATE NEW BIG CHAT



    let activateUsersListCreateNewChat = function () {
        $('.user-item.create-new-chat').on('click', function () {
            if ($(this).hasClass("active")) {
                let id = parseInt($(this).children('.user-item-id').text());
                console.log(id);
                usersListToChat = usersListToChat.filter(function (userInList) {
                    return userInList.id !== id;
                })
                $(this).removeClass("active");
            } else {
                let newUserToChat = Object.assign({}, userToChat);
                newUserToChat.id = parseInt($(this).children('.user-item-id').text());
                newUserToChat.login = $(this).children('.user-item-login').text();
                usersListToChat.push(newUserToChat);
                $(this).addClass("active");
            }
            refreshUsersToNewChatList();

        })
    }

    let refreshUsersToNewChatList = function () {
        let usersToNewChatList = $('.users-to-new-chat-list');
        usersToNewChatList.empty();
        for (i in usersListToChat) {
            let li = $('<li class="user-item"></li>');
            let userItemLogin = $('<div class="user-item-login">' + usersListToChat[i].login + '</div>');
            li.append(userItemLogin);
            usersToNewChatList.append(li);
        }
    }

    let activateCreateNewChatButton = function () {
        $('.create-new-chat-button').on('click', createNewChat);
    }

    let createNewChat = function () {
        if (usersListToChat.length < 3) {
            alert("too few members!");
            $('.users-to-new-chat-area').css('border', '5px solid rgb(255, 0, 0)');
            return;
        }

        $('.users-to-new-chat-area').css('border', '0');
        let newChatName = $('.input-name-new-chat').val();
        if (!(newChatName)) {
            alert("Write name");
            $('.input-name-new-chat').css('border', '5px solid rgb(255, 0, 0)');
            return
        }
        $('.input-name-new-chat').css('border', '0');
        newChatRequest.chatName = newChatName;
        newChatRequest.usersList = usersListToChat;
        createNewChatRequest();
    }

    let createNewChatRequest = function () {
        $.ajax({
            url: '/admin/signorConsole/chat',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(newChatRequest),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                alert(response.message);
                getUsersList();
            },
            error: function (xhr, status, error) {
                checkError(xhr, createNewChatRequest, [])
            }
        });
    }



    // ACTIVATION

    initializathionHeader();
    activateBurgers();
    getUsersList();
    activateBlockUserButton();
    activateUnblockUserButton();
    activateCreateNewChatButton();

})