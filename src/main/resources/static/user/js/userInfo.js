$(function () {
    let menuIsActive = false;
    let isFirstRefreshRequest = true;

    let userInfo = {
        login: "",
        email: "",
        roles: [],
        firstName: "",
        lastName: "",
        age: 0,
        sex: "",
        warn: "",
        icon: 0,
        refreshToken: ""
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

    let initializathionUserInfo = function () {
        let request = {
            refreshToken: localStorage.getItem("protected-chat-refreshToken")
        }
        $.ajax({
            url: '/user/info',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(request),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (respons) {
                if (respons.length == 0) {
                    return
                }
                userInfo.login = respons.login;
                userInfo.email = respons.email;
                userInfo.roles = respons.roles;
                userInfo.firstName = respons.firstName;
                userInfo.lastName = respons.lastName;
                userInfo.age = respons.age;
                userInfo.sex = respons.sex;
                if (respons.warn) {
                    userInfo.warn = respons.warn;
                }
                userInfo.icon = respons.icon;
                updateDataOnPage();
            },
            error: function (xhr, status, error) {
                checkError(xhr, initializathionUserInfo, []);
            }
        });
    }

    let updateDataOnPage = function () {
        $('.field-login').text(userInfo.login);
        let rolesList = $('<ul class="rolles-list"></ul>');
        if (userInfo.roles.length != 0) {
            for (i in userInfo.roles) {
                let role = $('<li class="role-item">' + userInfo.roles[i] + '</li>');
                rolesList.append(role);
            }
            $('.roles').html("");
            $('.roles').text("My roles:");
            $('.roles').append(rolesList);
        }
        $('#input-field-email').val(userInfo.email);
        $('#input-field-first-name').val(userInfo.firstName);
        $('#input-field-last-name').val(userInfo.lastName);
        $('#input-field-age').val(userInfo.age);
        $('#input-field-sex').val(userInfo.sex);
        if (!userInfo.warn) {
            $('.warn-message').css('display', 'block')
            $('.warn-message').text(userInfo.warn);
        }
    }

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

    let activateUpdateDataButton = function(){
        $('.update-data-button').on('click', function () {
            userInfo.email = $("#input-field-email").val();
            userInfo.firstName = $("#input-field-first-name").val();
            userInfo.lastName = $("#input-field-last-name").val();
            userInfo.age = $("#input-field-age").val();
            userInfo.sex = $("#input-field-sex").val();
            userInfo.refreshToken = localStorage.getItem("protected-chat-refreshToken");
            updateDataRequest();
        })
    }

    let updateDataRequest = function(){
        $.ajax({
            url: '/user/update',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(userInfo),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                alert(response.message);
            },
            error: function (xhr, status, error) {
                checkError(xhr, updateDataRequest, []);
            }
        });
    }

    let activateLogoutBotton = function () {
        $('.logout-button').on('click', function () {
            logoutUser();
        })
    }

    let logoutUser = function () {
        $.ajax({
            url: '/user/logout',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            success: function (response) {
                userData.id = 0;
                userData.login = "";
                userData.email = "";
                userData.roles = [];
                let userData_serialized = JSON.stringify(userData);
                window.localStorage.setItem("protected-chat-user-data", userData_serialized);
                window.localStorage.setItem("protected-chat-token", "");
                alert(response.message);
                window.location.href = "../../index.html";
            },
            error: function (xhr, status, error) {
                checkError(xhr, logoutUser, []);
            }
        });
    }




    // ACTIVATION



    initializathionHeader();
    initializathionUserInfo();
    activateUpdateDataButton();
    activateLogoutBotton();

})