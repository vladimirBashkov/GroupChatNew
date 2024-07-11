$(function () {
    let menuIsActive = false;
    let joinMenuIsActive = false;
    let usersMenuIsActive = false;
    let isFirstRefreshRequest = true;
    let errorUpMessage = 'Error on server. Please update page.';

    let selectedRequest = {
        joinId: 0,
        login: ""
    }

    let usersRoles = ['USER', 'OLD', 'SIGNOR', 'ADMIN'];   //if change hier, then change in string -> 78,365,394,404,481
    let userRoleIsActive = false;
    let oldRoleIsActive = false;
    let signorRoleIsActive = false;
    let adminRoleIsActive = false;

    let selectedUserForRolesUpdate = {
        id: 0,
        login: "",
        roles: [],
    }

    let selectedUserForBlockAndDelete = {
        id: 0,
        login: "",
        blockTime: 0,
        reasonOfBlocking: "",
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

    let showError = function (xhr) {
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

        $('.user-join-request-list-menu-botton').on('click', function () {
            const hamburger = document.querySelector(".user-join-request-list-menu-botton")
            let area = $('.user-join-request-list-area.head-menu');
            if (joinMenuIsActive) {
                area.css('display', 'none');
                joinMenuIsActive = false;
                hamburger.classList.toggle('active');
            } else {
                area.css('display', 'block');
                joinMenuIsActive = true;
                hamburger.classList.toggle('active');
            }
        })

        $('.users-list-menu-botton').on('click', function () {
            const hamburger = document.querySelector(".users-list-menu-botton")
            let area = $('.users-list-area.head-menu');
            if (usersMenuIsActive) {
                area.css('display', 'none');
                usersMenuIsActive = false;
                hamburger.classList.toggle('active');
            } else {
                area.css('display', 'block');
                usersMenuIsActive = true;
                hamburger.classList.toggle('active');
            }
        })
    }



    //   ADD NEW USER



    let getUserJoinRequestItemFromEntity = function (connectEntity) {
        let li = $('<li class="user-join-request-item"></li>');
        let userJoinRequesItemLogin = $('<div class="user-join-request-item-login">' + connectEntity.login + '</div>');
        let userJoinRequesItemId = $('<div class="user-join-request-item-id">' + connectEntity.id + '</div>');
        let userJoinRequesItemMail = $('<div class="user-join-request-item-email">' + connectEntity.email + '</div>');
        let userJoinRequesItemMessage = $('<div class="user-join-request-item-message">' + connectEntity.message + '</div>');
        li.append(userJoinRequesItemLogin);
        li.append(userJoinRequesItemId);
        li.append(userJoinRequesItemMail);
        li.append(userJoinRequesItemMessage);
        return li;
    }

    let getUserJoinRequests = function () {
        $.ajax({
            url: '/admin/admin/join-request',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            success: function (response) {
                fillingUserJoinRequestList(response, 'head-menu');
                fillingUserJoinRequestList(response, 'basic-list');

                $('.user-join-request-item').on('click', function () {
                    selectedRequest.joinId = parseInt($(this).children('.user-join-request-item-id').text());
                    selectedRequest.login = $(this).children('.user-join-request-item-login').text();
                    $('.message-from-user-join-request-area').empty();
                    $('.message-from-user-join-request-area').text($(this).children('.user-join-request-item-message').text());
                    $('.email-from-user-join-request-area').empty();
                    $('.email-from-user-join-request-area').text($(this).children('.user-join-request-item-email').text());
                })
            },
            error: function (xhr, status, error) {
                checkError(xhr, getUserJoinRequests, [])
            }
        });
    }

    let fillingUserJoinRequestList = function (response, area) {
        $('.user-join-request-list.' + area).empty();
        for (i in response) {
            let li = getUserJoinRequestItemFromEntity(response[i]);
            $('.user-join-request-list.' + area).append(li);
        }
    }

    let activateAddNewUserButton = function () {
        $('.add-new-user-button').on('click', addUser);
    }

    let addUser = function () {
        $.ajax({
            url: '/admin/admin/add-user',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(selectedRequest),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                alert(response.message)
                selectedRequest.joinId = 0;
                selectedRequest.login = "";
                $('.message-from-user-join-request-area').empty();
                $('.email-from-user-join-request-area').empty();
                getUserJoinRequests();
            },
            error: function (xhr, status, error) {
                checkError(xhr, addUser, []);
            }
        });
    }

    let activateDeleteRequestButton = function () {
        $('.delete-request-button').on('click', deleteUserJoinRequest);
    }

    let deleteUserJoinRequest = function () {
        let confirmDelete = confirm("Are you sure you want to delete the request? User - " + selectedRequest.login);
        if (confirmDelete) {
            deleteRequest();
        }
    }

    let deleteRequest = function () {
        $.ajax({
            url: '/admin/admin/delete-request',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify(selectedRequest),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                alert(response.message);
                $('.message-from-user-join-request-area').empty();
                $('.email-from-user-join-request-area').empty();
                getUserJoinRequests();
            },
            error: function (xhr, status, error) {
                checkError(xhr, deleteRequest, []);
            }
        });
    }



    // WORK WITH USERS 


    
    let getUsersList = function () {
        $.ajax({
            url: '/admin/signorConsole/user',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            success: function (respons) {
                fillingUsersList(respons, 'head-menu');
                activateUsersListHeadMenu();
                fillingUsersList(respons, 'update-roles');
                activateUsersListUpdateRoles();
                fillingUsersList(respons, 'block-and-delete');
                activateUsersListBlockAndDelete();
            },
            error: function (xhr, status, error) {
                checkError(xhr, getUsersList, []);
            }
        });
    }

    let fillingUsersList = function (respons, area) {
        $('.users-list.' + area).empty();
        for (i in respons) {
            let li = getUserFromEntity(respons[i], area);
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

    let activateUsersListHeadMenu = function () {
        $('.user-item.head-menu').on('click', function () {
            selectedUserForRolesUpdate.id = parseInt($(this).children('.user-item-id').text());
            selectedUserForRolesUpdate.login = $(this).children('.user-item-login').text();

            let userItemRoles = $(this).children('.user-item-roles');
            let userItemRolesList = userItemRoles.children('.user-item-roles-list');
            let rolesList = userItemRolesList.find('.user-item-role-item');

            userRoleIsActive = false;
            oldRoleIsActive = false;
            signorRoleIsActive = false;
            adminRoleIsActive = false;

            rolesList.each(function () {
                let textRole = $(this).text();
                if (textRole === "USER") {
                    userRoleIsActive = true;
                }
                if (textRole === "OLD") {
                    oldRoleIsActive = true;
                }
                if (textRole === "SIGNOR") {
                    signorRoleIsActive = true;
                }
                if (textRole === "ADMIN") {
                    adminRoleIsActive = true;
                }
            })

            activateCurrentUserRoles();
            let warnUlUR = $('.user-warning-list.update-roles');
            let warnUlBAD = $('.user-warning-list.block-and-delete');
            getUserWarnings(selectedUserForRolesUpdate.id, [warnUlUR, warnUlBAD]);

            selectedUserForBlockAndDelete.id = parseInt($(this).children('.user-item-id').text());
            selectedUserForBlockAndDelete.login = $(this).children('.user-item-login').text();
            selectedUserForBlockAndDelete.blockTime = parseInt($(this).children('.user-item-block-time').text());
            selectedUserForBlockAndDelete.reasonOfBlocking = $(this).children('.user-item-reason-of-blocking').text();

            getUserBlock();
            changeSaveUpdateRolesUserButton(selectedUserForRolesUpdate.login);
            changeBlockUserButton(selectedUserForBlockAndDelete.login);
            changeUnblockUserButton(selectedUserForBlockAndDelete.login);
            changeDeleteUserButton(selectedUserForBlockAndDelete.login);
        })
    }



    //UPDATE ROLES



    let initRoles = function () {
        let roles = $('.user-roles-list-update');
        roles.empty();
        for (i in usersRoles) {
            let role = $('<li class="user-role-' + usersRoles[i] + '-update">' + usersRoles[i] + '</li>');
            roles.append(role);
        }
    }

    let activateUsersListUpdateRoles = function () {
        $('.user-item.update-roles').on('click', function () {
            selectedUserForRolesUpdate.id = parseInt($(this).children('.user-item-id').text());
            selectedUserForRolesUpdate.login = $(this).children('.user-item-login').text();

            let userItemRoles = $(this).children('.user-item-roles');
            let userItemRolesList = userItemRoles.children('.user-item-roles-list');
            let rolesList = userItemRolesList.find('.user-item-role-item');

            userRoleIsActive = false;
            oldRoleIsActive = false;
            signorRoleIsActive = false;
            adminRoleIsActive = false;

            rolesList.each(function () {
                let textRole = $(this).text();
                if (textRole === "USER") {
                    userRoleIsActive = true;
                }
                if (textRole === "OLD") {
                    oldRoleIsActive = true;
                }
                if (textRole === "SIGNOR") {
                    signorRoleIsActive = true;
                }
                if (textRole === "ADMIN") {
                    adminRoleIsActive = true;
                }
            })

            activateCurrentUserRoles();
            let warnUl = $('.user-warning-list.update-roles');
            getUserWarnings(selectedUserForRolesUpdate.id, [warnUl]);
            changeSaveUpdateRolesUserButton(selectedUserForRolesUpdate.login);
        })
    }

    let activateCurrentUserRoles = function () {  //add in description
        let roleUser = $('.user-role-USER-update');
        let roleOld = $('.user-role-OLD-update');
        let roleSignore = $('.user-role-SIGNOR-update');
        let roleAdmin = $('.user-role-ADMIN-update');
        getCurrentUserRoles(roleUser, userRoleIsActive);
        getCurrentUserRoles(roleOld, oldRoleIsActive);
        getCurrentUserRoles(roleSignore, signorRoleIsActive);
        getCurrentUserRoles(roleAdmin, adminRoleIsActive);
    }

    let activateRolesList = function () {
        let roleUser = $('.user-role-USER-update');
        let roleOld = $('.user-role-OLD-update');
        let roleSignore = $('.user-role-SIGNOR-update');
        let roleAdmin = $('.user-role-ADMIN-update');

        roleUser.on('click', function () {
            userRoleIsActive = !userRoleIsActive;
            getCurrentUserRoles(roleUser, userRoleIsActive);
        })

        roleOld.on('click', function () {
            oldRoleIsActive = !oldRoleIsActive;
            getCurrentUserRoles(roleOld, oldRoleIsActive);
        })

        roleSignore.on('click', function () {
            signorRoleIsActive = !signorRoleIsActive;
            getCurrentUserRoles(roleSignore, signorRoleIsActive);
        })

        roleAdmin.on('click', function () {
            adminRoleIsActive = !adminRoleIsActive;
            getCurrentUserRoles(roleAdmin, adminRoleIsActive);
        })
    }

    let getCurrentUserRoles = function (roleElement, isActive) {
        if (isActive) {
            roleElement.css("background-color", "rgba(255, 0, 0, 0.9)");
        } else {
            roleElement.css("background-color", "rgba(255, 0, 0, 0.05)");
        }
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
                for (i in warningLists) {
                    fillingWarning(warningLists[i], response);
                }
            },
            error: function (xhr, status, error) {
                checkError(xhr, getUserWarnings, [id, warningLists]);
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

    let changeSaveUpdateRolesUserButton = function (username) {
        $('.update-roles-user-button').text('SAVE CHANGES FOR ' + username);
    }

    let activateUpdateRolesUserbutton = function () {
        $('.update-roles-user-button').on('click', function () {
            let currentUserRoles = [];
            if (userRoleIsActive) {
                currentUserRoles.push("USER");
            }
            if (oldRoleIsActive) {
                currentUserRoles.push("OLD");
            }
            if (signorRoleIsActive) {
                currentUserRoles.push("SIGNOR");
            }
            if (adminRoleIsActive) {
                currentUserRoles.push("ADMIN");
            }
            if (currentUserRoles.length < 1) {
                alert('Roles are empty')
                return;
            }
            updateRoles(currentUserRoles);
        })
    }

    let updateRoles = function (roles) {
        $.ajax({
            url: '/admin/admin/user/roles',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify({ id: selectedUserForRolesUpdate.id, roles: roles }),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (respons) {
                alert(respons.message)
                getUsersList();
            },
            error: function (xhr, status, error) {
                checkError(xhr, updateRoles, []);
            }
        });
    }



    // BLOCK AND DELETE



    let activateUsersListBlockAndDelete = function () {
        $('.user-item.block-and-delete').on('click', function () {
            selectedUserForBlockAndDelete.id = parseInt($(this).children('.user-item-id').text());
            selectedUserForBlockAndDelete.login = $(this).children('.user-item-login').text();
            selectedUserForBlockAndDelete.blockTime = parseInt($(this).children('.user-item-block-time').text());
            selectedUserForBlockAndDelete.reasonOfBlocking = $(this).children('.user-item-reason-of-blocking').text();

            let warnUl = $('.user-warning-list.block-and-delete');
            getUserWarnings(selectedUserForBlockAndDelete.id, [warnUl]);

            getUserBlock();
            changeBlockUserButton(selectedUserForBlockAndDelete.login);
            changeUnblockUserButton(selectedUserForBlockAndDelete.login);
            changeDeleteUserButton(selectedUserForBlockAndDelete.login);
        })
    }

    let getUserBlock = function () {
        $.ajax({
            url: '/admin/signorConsole/user/block',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: { id: selectedUserForBlockAndDelete.id },
            success: function (response) {
                $('.input-block-time').val(response.blockTime);
                $('#textarea-block-reason').val(response.reason);
            },
            error: function (xhr, status, error) {
                checkError(xhr, getUserBlock, []);
            }
        });
    }

    let changeBlockUserButton = function (login) {
        $('.block-user-button').text('BLOCK ' + login);
    }

    let changeUnblockUserButton = function (login) {
        $('.unblock-user-button').text('UNBLOCK ' + login);
    }

    let changeDeleteUserButton = function (login) {
        $('.delete-user-button').text('DELETE ' + login);
    }

    let activateBlockUserButton = function () {
        $('.block-user-button').on('click', blockUser);
    }

    let blockUser = function () {
        let blockTimeCounter = parseInt($('.input-block-time').val());
        let blockTimeReason = replaceEnter(sanitize($('#textarea-block-reason').val()));
        selectedUserForBlockAndDelete.blockTime = blockTimeCounter;
        selectedUserForBlockAndDelete.reasonOfBlocking = blockTimeReason;
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
            data: JSON.stringify(selectedUserForBlockAndDelete),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (respons) {
                alert(respons.message)
                getUsersList();
            },
            error: function (xhr, status, error) {
                checkError(xhr, blockRequest, []);
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
            data: JSON.stringify({ id: selectedUserForBlockAndDelete.id }),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (respons) {
                alert(respons.message)
                getUsersList();
            },
            error: function (xhr, status, error) {
                checkError(xhr, unblockUserRequest, []);
            }
        });
    }

    let activateDeleteUserButton = function () {
        $('.delete-user-button').on('click', deleteUserRequest);
    }

    let deleteUserRequest = function () {
        $.ajax({
            url: '/admin/admin/user/delete',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Bearer " + localStorage.getItem("protected-chat-token"));
            },
            data: JSON.stringify({ id: selectedUserForBlockAndDelete.id }),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (response) {
                alert(response.message)
                getUsersList();
            },
            error: function (xhr, status, error) {
                checkError(xhr, deleteUserRequest, []);
            }
        });
    }





    // ACTIVATE

    initializathionHeader();
    activateBurgers();
    getUserJoinRequests();
    activateAddNewUserButton();
    activateDeleteRequestButton();

    getUsersList();
    initRoles();
    activateRolesList();
    activateUpdateRolesUserbutton();
    activateBlockUserButton();
    activateUnblockUserButton();
    activateDeleteUserButton();
})