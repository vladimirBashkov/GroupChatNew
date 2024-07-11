$(function () {
    let userData = {
        id: 0,
        login: "",
        email: "",
        roles: []
    }

    let newApplication = {
        login: "",
        password: "",
        email: "",
        message: ""
    }

    let activateForm = function () {
        $('.login-form').on('submit', function (event) {
            event.preventDefault();
            auth();
        })
    }

    let activateButtonLogin = function () {
        $('#button-login').on('click', function (event) {
            auth();
        })
    }

    let auth = function () {
        let login = $("#input-field-login").val();
        if (login) {
            let password = $("#input-field-password").val();
            if (!password) {
                return
            }
            let messageData = {
                login: login,
                password: password
            }
            authRequest(messageData);
        }
    }

    let authRequest = function (messageData) {
        $.ajax({
            url: '/auth/signin',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(messageData),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                if (response) {
                    userData.id = response.id;
                    userData.login = response.login;
                    userData.email = response.email;
                    userData.roles = response.roles;
                    let userData_serialized = JSON.stringify(userData);
                    window.localStorage.setItem("protected-chat-user-data", userData_serialized);
                    window.localStorage.setItem("protected-chat-token", response.token);
                    window.localStorage.setItem("protected-chat-refreshToken", response.refreshToken);
                    setTimeout(() => {
                        window.location.href = '../user/chat/chat.html';
                    }, 1500);
                }
            },
            error: function (xhr, status, error) {
                showError(xhr);
            },
        });
    }

    let activateEye = function () {
        $('#eyeIcon').on('click', function () {
            let password = document.getElementById('input-field-password')
            if (password.type == "password") {
                password.type = "text";
                $("#eyeIcon").attr("src", "../welcome/img/eye-open.png");
            } else {
                password.type = "password";
                $("#eyeIcon").attr("src", "../welcome/img/eye-closed.png");
            }
        })
    }


    let activateButtonRegister = function () {
        $('#button-register').on('click', function () {
            $('.email-area').css("display", "flex");
            $('#button-start-register').css("display", "inline-block");
            $('#button-start-register').css("visibility", "visible");
            $('#button-start-register').css("pointer-events", "auto");
            $('#button-login').css("display", "none");
            $('#button-register').css("display", "none");
        })
    }

    let activateButtonSendApplication = function () {
        $('#button-start-register').on('click', function (event) {
            event.preventDefault();
            newApplication.login = $("#input-field-login").val();
            newApplication.email = $("#input-field-email").val();
            newApplication.password = sanitize($("#input-field-password").val());
            if (!newApplication.login) {
                return;
            }
            if (!checkLogin(newApplication.login)) {
                alert('Please use only letters, numbers, and _ - symbol in yoyr login. Length not less then 3 symbols')
                return;
            }
            if (!newApplication.email) {
                return;
            }
            if (!newApplication.password) {
                return;
            }
            if (!checkPassword(newApplication.password)) {
                alert('Please use only letters, numbers, and !@#\$%\^\&*\)\(+=._- symbols in yoyr login. Length not less then 6 symbols')
                return;
            }
            newApplication.message = sanitize(prompt("Enter a message for the administrator to speed up authorization", "Nothing"));
            postApplication();
        })
    }

    let postApplication = function () {
        $.ajax({
            url: '/auth/application',
            type: 'POST',
            data: JSON.stringify(newApplication),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                if (response.length == 0) {
                    return
                }
                alert(response.message);
                $('.email-area').css("display", "none");
                $('#button-start-register').css("display", "none");
                $('#button-login').css("display", "inline-block");

            },
            error: function (xhr, status, error) {
                showError(xhr);
            },
        });
    }

    let showError = function(xhr){
        $('.error-area').css("display", "flex");
        $('.error-message').text(JSON.parse(xhr.responseText).message);
    }

    let checkLogin = function (text) {
        const regex = /^[a-zA-Z0-9_-]{3,}$/g;
        const found = text.match(regex);
        if (found) {
            return true
        } else return false;
    }

    let checkPassword = function (text) {
        var regex = /^[a-zA-Z0-9!@#\$%\^\&*\)\(+=._-]{6,}$/g;
        const found = text.match(regex);
        if (found) {
            return true
        } else return false;
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

    activateEye();
    activateButtonRegister();
    activateForm();
    activateButtonLogin();
    activateButtonSendApplication();
})