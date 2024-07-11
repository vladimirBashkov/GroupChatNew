$(function () {
    let menuIsActive = false;
    let isAuth = false;
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
        } else if (userData.roles.includes('SIGNOR')) {
            activateChatHeader('#header-signor-console-item');
            activateChatHeader('#header-chat-item');
            activateChatHeader('#header-my-page-item');
        } else if (userData.roles.includes('OLD') || userData.roles.includes('USER')) {
            activateChatHeader('#header-chat-item');
            activateChatHeader('#header-my-page-item');
        }
    }

    let refreshTokenFunction = function () {
        if (!localStorage.getItem("protected-chat-refreshToken")) {
            return false;
        }
        $.ajax({
            url: '/auth/refresh-token',
            type: 'POST',
            data: { refreshToken: localStorage.getItem("protected-chat-refreshToken") },
            statusCode: {
                401: function () {
                    window.location.href = "./auth/auth.html";
                },
                403: function () {
                    window.location.href = "./auth/auth.html";
                }
            },
            success: function (response) {
                window.localStorage.setItem("protected-chat-token", response.accessToken);
                window.localStorage.setItem("protected-chat-refreshToken", response.refreshToken);
                setTimeout(() => {
                    window.location.href = "./user/chat/chat.html";
                }, 500)
            },
            error: function () {
            },
        });
    }

    let activateChatHeader = function (field) {
        $(field).css("visibility", "visible");
        $(field).css("pointer-events", "auto");
    }

    let activateBurgerButton = function () {
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
    }

    let activateInsertButton = function () {
        $('.insert-button').on('click', function () {
            $('.cat-img').attr('src', './welcome/img/cat-top.jpg');
            setTimeout(() => {
                if (localStorage.getItem("protected-chat-refreshToken")) {
                    refreshTokenFunction();
                } else window.location.href = "./auth/auth.html";
            }, 1000)

        });
    }

    initializathionHeader();
    activateBurgerButton();
    activateInsertButton();
})