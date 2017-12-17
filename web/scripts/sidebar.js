
$(document).ready(function () {
    $( document ).ajaxError(function(event, request, settings, thrownError) {
        $('.alert .alert-content').text(request.status + " " + request.statusText + "\n" + request.responseText);
        $('.alert').show();
        // alert(request.status + " " + request.statusText + "\n" + request.responseText);
    });
    let userID = 1;
    init();

    $('#sidebarCollapse').on('click', function () {
        $('#sidebar').toggleClass('active');
        $(this).toggleClass('active');
    });

    $('.CTAs').on('click', '.login-btn', function() {
        console.log('logging in');
        $('#content').children().hide();
        $('.navbar').show();
        $('.login-form').show();
        $('.login-form button').click(function() {
            login();
        });
    });

    $('.CTAs').on('click', '.logout-btn', function(){
        console.log('logging out..');
        logout();
    });

    $('.signup-btn').click(function() {
        $('#content').children().hide();
        $('.navbar').show();
        $('.signup-form').show();
        $('.signup-form button').click(function() {
            var email = $('.signup-form #email').val();
            var pwd = $('.signup-form #pwd').val();
            var conPwd = $('.signup-form #confirm-pwd').val();
            var userName = $('.signup-form #username').val();
            var firstName = $('.signup-form #first-name').val();
            var lastName = $('.signup-form #last-name').val();
            if (conPwd !== pwd) {
                alert('Passwords don\'t match!');
            } else {
                var signUp = {
                    username: userName,
                    email: email,
                    password: pwd,
                    first_name: firstName,
                    last_name: lastName
                }
                $.post({
                    url: "/api/sign_up",
                    data: JSON.stringify(signUp),
                    dataType: 'json',
                    success: function() {
                        alert("Welcome! Please log in.");
                        $('.login-btn').trigger('click');
                    }
                });

            }
        });
    });

    $('#messages').click(function() {
        $('#message').addClass('active');
        $('#content').children().hide();
        $('.navbar').show();
        $('.message-content').show();
        loadMessage();
    });

    $('#compose-photo').click(function() {
        $('#content').children().hide();
        $('.navbar').show();
        $('#photo-compose-content').show();
        $('#success-message').hide();
    });

    $('#compose-collection').click(function() {
        $('#content').children().hide();
        $('.navbar').show();
        $('#collection-compose-content').show();
        loadCollectionsToEdit();
    });

    $('#compose-event').click(function() {
        $('#content').children().hide();
        $('.navbar').show();
        $('#event-compose-content').show();
        loadeventsToEdit();
    });


    $('#reset-collection-btn').click(function(){
        $('#collection-compose-form')[0].reset();
        $('.save_collection-btn').remove();
        $('#collection-save-success-msg').hide();
        $('#create-collection-btn').show();
    });


    $('#create-event-btn').click(function() {
        // const country = /\S/.test($('#event-country').val()) ? $('#event-country').val() : null;
        // const city = /\S/.test($('#event-city').val()) ? $('#event-city').val() : null;
        // const street = /\S/.test($('#event-street').val()) ? $('#event-street').val() : null;
        // const state = /\S/.test($('#event-state').val()) ? $('#event-state').val() : null;
        // const zip = /\S/.test($('#event-zip').val()) ? $('#event-zip').val() : null;
        const country = $('#event-country').val();
        const city = $('#event-city').val();
        const street = $('#event-street').val();
        const state = $('#event-state').val();
        const zip = $('#event-zip').val();
        const geoUrl = encodeURI('https://maps.googleapis.com/maps/api/geocode/json?address=' +street + " " +
            city + " " + state + " " + country + " " + zip + '&key=AIzaSyBXIpemsf1saPdV3fWvp2jSIC0OU2o9uT8');
        let event = {
            title: $('#event-title').val(),
            country: country,
            state: state,
            city: city,
            street: street,
            zip: zip,
            time_happened: $('#event-time-happened').val(),
            description: $('#event-description').val(),
            lon: null,
            lat: null,
            visibility: $('input[name=event-visibility]:checked').val(),
            limitation: $('#event-limit').val()
        };
        if (country === null && city === null && street === null && state === null ) {
            postEvent(event);
        } else {
            $.get({
                url: geoUrl,
                success: function(data) {
                    if (data.results.length > 0) {
                        const geo = data.results[0].geometry.location;
                        event.lat = geo.lat;
                        event.lon = geo.lng;
                    }
                    postEvent(event);
                }
            });
        }

    });

    $('#photo-upload').fileupload({
        url: document.location.origin + "/api/photo_upload",
        dataType: 'json',
        done: function (e, data) {
            const file = data.result.files[0];
            $('#photo-files').text("");
            $('<img/>').attr('src', file.thumbnailUrl).appendTo('#photo-files');
            $('<p/>').text(file.name).appendTo('#state-text');
            $('<p/>').text('Upload successfully.').appendTo('#state-text');
            $('#upload-photo-btn').click(function() {
                const country = /\S/.test($('#photo-country').val()) ? $('#photo-country').val() : null;
                const city = /\S/.test($('#photo-city').val()) ? $('#photo-city').val() : null;
                const street = /\S/.test($('#photo-street').val()) ? $('#photo-street').val() : null;
                const state = /\S/.test($('#photo-state').val()) ? $('#photo-state').val() : null;
                const zip = /\S/.test($('#photo-zip').val()) ? $('#photo-zip').val() : null;
                const geoUrl = encodeURI('https://maps.googleapis.com/maps/api/geocode/json?address=' +street + " " +
                    city + " " + state + " " + country + " " + zip + '&key=AIzaSyBXIpemsf1saPdV3fWvp2jSIC0OU2o9uT8');
                let photo = {
                    temp_filename: file.temp_filename,
                    title: $('#photo-title').val(),
                    country: country,
                    state: state,
                    city: city,
                    street: street,
                    zip: zip,
                    time_captured: $('#photo-time-captured').val(),
                    description: $('#photo-description').val(),
                    lon: null,
                    lat: null,
                    visibility: $('input[name=photo-visibility]:checked').val(),
                    category: $('#photo-category').val()
                };
                if (country === null && city === null && street === null && state === null ) {

                    postPhoto(photo);
                } else {
                    $.get({
                        url: geoUrl,
                        success: function(data) {
                            if (data.results.length > 0) {
                                const geo = data.results[0].geometry.location;
                                photo.lon = geo.lng;
                                photo.lat = geo.lat;
                            }
                            postPhoto(photo);
                        }
                    });
                }


            });

        },
        progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#photo-progress .progress-bar').css(
                'width',
                progress + '%'
            );
        }
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');

    $('#create-collection-btn').click(function() {
        const newCol = {
            title: $('#collection-title').val(),
            visibility: $('input[name=collection-visibility]:checked').val(),
            description: $('#collection-description').val()
        };
        $.post({
            url: document.location.origin + "/api/collection",
            data: JSON.stringify(newCol),
            success: function() {
                $('#compose-collection').trigger('click');
            }
        });
    });

    $('#reset-event-btn').click(function(){
        $('#event-compose-form')[0].reset();
        $('.save_event-btn').remove();
        $('#event-save-success-msg').hide();
        $('#create-event-btn').show();
    });





    /* api */


    function init() {
        $('#login-logout').attr('class', 'login-btn');
        $('#login-logout').text('Log In');
        $('.signup-btn').show();
        // $('#login-logout').trigger('click');
        $('.sidebar-avatar-name .avatar').attr('src', '../guest.png');
        $('.sidebar-avatar-name .sidebar-name').text('Guest');
        $.get({
            url: document.location.origin + '/api/sign_in',
            success: function(data) {
                userID = data.user_id;
                if (userID !== 1) {

                    $('#login-logout').attr('class', 'logout-btn');
                    $('#login-logout').text('Log Out');
                    $('.signup-btn').hide();
                    // $('#home').trigger('click');
                    getProfile(userID);
                    // $('.sidebar-avatar-name .avatar').attr('src', profile.avatar_url);
                    // $('.sidebar-avatar-name .sidebar-name').text(profile.first_name + " " + profile.last_name);
                }
            }

        });


    }


    function getProfile(userID) {
        var url = document.location.origin + "/api/profile?to_see_user_id=" + userID;
        var profile = {};
        $.get({
            url: url,
            dataType: 'json',
            success: function(data) {
                profile = data;

                var name = profile.first_name + " " + profile.last_name;
                $('.sidebar-avatar-name .avatar').attr('src', profile.avatar_url);
                $('.sidebar-avatar-name a').attr('href', profile.profile_url);
                $('.sidebar-avatar-name .sidebar-name').text(name);
                return profile;
            }
        });


    }

    function login() {
        let emailOrUsername = $('#email-username').val();
        let pwd = $('#pswd').val();
        let rem;
        if ($('#is-rem').val() === "true") {
            rem = true;
        } else {
            rem = false;
        }
        let logIn = {
            username_or_email: emailOrUsername,
            password: pwd,
            remember: rem
        };

        $.post({
            url: document.location.origin + "/api/sign_in",
            data: JSON.stringify(logIn),
            dataType: 'json',
            success: function(data) {
                userID = data.user_id;
                let profile = getProfile(userID);
                // console.log(profile);
                // var name = profile.first_name + " " + profile.last_name;
                // console.log(name);
                // $('.sidebar-avatar-name .avatar').attr('src', profile.avatar_url);
                // $('.sidebar-avatar-name .sidebar-name').text(name);
                $('.signup-btn').hide();
                $('#login-logout').toggleClass('login-btn');
                $('#login-logout').toggleClass('logout-btn');
                $('#login-logout').text('Log Out');
                // $('#home').trigger('click');
                location.reload(true);
            }
        });
    }

    function logout() {
        $.get({
            url: document.location.origin + "/api/sign_out",
            success: function() {
                userID = 1;
                $('.sidebar-avatar-name .avatar').attr('src', "../guest.png");
                $('.sidebar-name').text('Guest');
                $('.signup-btn').show();
                $('#login-logout').toggleClass('login-btn');
                $('#login-logout').toggleClass('logout-btn');
                $('#login-logout').text('Log In');
                $('.login-btn').trigger('click');
                console.log('log out success');
            }
        });
    }

    function createPhotoEntry(photo) {
        $.get({
            url: document.location.origin + "/api/profile?to_see_user_id=" + photo.uploader_id,
            success: function(profile) {
                const name = profile.first_name + " " + profile.last_name;
                console.log(name);
                photo.time_uploaded = new Date(photo.time_uploaded);
                let entry = $('<div/>', {"class": 'photo-entry'});
                entry.appendTo('.photo-list');
                let row = $('<div/>', {"class": 'row'});
                row.appendTo(entry);
                let link = $('<a/>', {'href': 'http://localhost:8080/photo/' + photo.photo_id});
                link.appendTo(row);
                $('<img/>', {
                    'class': "photo-thumbnail col-sm-4",
                    'src': photo.full_url
                }).appendTo(link);
                let textCol = $('<div/>', {'class': "col-sm-8"});
                textCol.appendTo(row);
                let photoTitle = $('<h3/>', {'class': 'photo-title'});
                photoTitle.text(photo.title);
                photoTitle.appendTo(textCol);
                $('<a/>', {
                    'href': document.location.origin + '/profile/' + photo.uploader_id
                }).append($('<div/>').text('by '+ name)).appendTo(textCol);
                let photoDes = $('<div/>', {'class': 'text-content'});
                photoDes.text(photo.description);
                photoDes.appendTo(textCol);
                let timeStamp = $('<p/>', {
                    'class': 'timestamp',
                    'style': 'float:right'
                });
                timeStamp.text(photo.time_uploaded);
                timeStamp.appendTo(textCol);

                $('<div/>', {'class': 'line'}).appendTo(entry);
            }
        });
    }

    function loadMessage() {
        $('#msg-friend-list .msg-friend-entry').hide();
        $('#invis .msg-invitatoin-entry').hide();
        $('#FOF .msg-FOF-entry').hide();
        $('#msg-friend-list div').hide();
        $('#invis div').hide();
        $('#FOF div').hide();
        $.get({
            url: document.location.origin + "/api/friend?to_see_user_id=" + userID,
            dataType: 'json',
            success: function(data) {
                let friends = data.friends;
                if (friends.length > 0) {
                    $.each(friends, function(i, friend) {
                        createFriendEntryInMessage(friend);
                    });
                } else {
                    $('<div/>', {'class': 'text-center'}).text('No friend found.').appendTo('#msg-friend-list');
                }
            }
        });
        $.get({
            url: document.location.origin + "/api/invitation",
            success: function(data) {
                const invis = data.invitations;
                if (invis.length > 0) {
                    $.each(invis, function(i, invitation) {
                        createInvitationEntryInMessage(invitation);
                    });
                } else {
                    $('<div/>', {'class': 'text-center'}).text('No invitation for now.').appendTo('#invis');
                }
            }

        });
        $.get({
            url: document.location.origin + "/api/fof",
            success: function(data) {
                const fofs = data.FOFs;
                if (fofs.length > 0) {
                    $.each(fofs, function(i, fof) {
                        createFOFEntryInMessage(fof);
                    });
                } else {
                    $('<div/>', {'class': 'text-center'}).text('No fof found.').appendTo('#FOF');
                }
            }
        });
    }

    function createFriendEntryInMessage(friend) {
        let frd = $('<li/>', {
            'class': 'msg-friend-entry list-group-item',
            'id': friend.user_id.toString()
        }).append($('<img/>', {
            'src': 'http://localhost:8080/file/avatar/' + friend.user_id + '.png',
            'class': 'avatar'
        })).append($('<h4/>', {
            'class': 'text-center'
        }).text(friend.first_name + " " + friend.last_name)).appendTo('#msg-friend-list');
        frd.on('click', function() {
            enableMessageSend(friend.user_id);
            loadFriendMsg(friend.user_id, 10, 0);
        })
    }

    function enableMessageSend(friendID) {
        $('#msg-send-btn').click(function() {
            const msg = $('#msg-send-message').val();
            const sendMsg = {
                to_user_id: friendID,
                content: msg
            };
            $.post({
                url: document.location.origin + "/api/message",
                data: JSON.stringify(sendMsg),
                dataType: 'json',
                success: function() {
                    $('#msg-send-message').val("");
                    loadFriendMsg(friendID, 10, 0);
                }
            });
        });
    }

    function loadFriendMsg(friendID, rows, offset) {
        $('#no-fri-message').hide();
        $.get({
            url: document.location.origin + "/api/message?friend_user_id=" + friendID + "&load_rows=" + rows + "&offset=" + offset,
            success: function (data) {
                const messages = data.messages;
                $('#msgs .msg-message-entry').remove();
                if (messages.length > 0) {
                    $.each(messages, function(i, msg) {
                        createMsgEntryInMessage(friendID, msg);
                    });
                } else {
                    $('#no-fri-message').show();
                }


            }
        });
    }

    function createMsgEntryInMessage(friendID, msg) {
        let avatarSrc = 'http://localhost:8080/file/avatar/' + friendID + '.png';
        let profileUrl = document.location.origin + "/profile/" + friendID;
        if (msg.to_from === 'to') {
            avatarSrc = 'http://localhost:8080/file/avatar/' + userID + '.png';
            profileUrl = document.location.origin + "/profile/" + userID;
        }
        $('<div/>', {'class': 'msg-message-entry'})
            .append($('<a/>', {
                'href': profileUrl,
                'target': '_blank'
            }).append($('<img/>', {
                'src': avatarSrc,
                'class': 'avatar',
                'style': 'position:absolute;'
            })))
            .append($('<div/>', {'class':'bubble'})
                .text(msg.content).append($('<p/>', {'class': 'timestamp'}).text(formatDate(new Date(msg.time_stamp))))
            ).appendTo('#msgs');
    }

    function createInvitationEntryInMessage(invitation) {
        const entryID = 'invi-' + invitation.message_id;
        const acceptBtnID = 'acpt-' + invitation.message_id;
        const ignoreBtnID = 'ign-' + invitation.message_id;
        $.get({
            url: document.location.origin + "/api/profile?to_see_user_id=" + invitation.from_user_id,
            success: function(data) {
                const profile = data;
                const name = profile.first_name + " " + profile.last_name;
                $('<div/>', {
                    'class': 'msg-invitation-entry list-group-item',
                    'id': entryID
                }).append($('<a/>', {
                    'href': document.location.origin + "/profile/" + profile.user_id,
                    'target': '_blank'
                }).append($('<img/>', {
                    'src': profile.avatar_url,
                    'class': 'avatar'
                })))
                    .append($('<h4/>', {'class': 'from-who'}).text(name))
                    .append($('<div/>', {'class': 'text-content'}).text(invitation.content))
                    .append($('<button/>', {
                        'type': 'button',
                        'class': 'btn',
                        'id': acceptBtnID,
                    }).text('Accept'))
                    .append($('<button/>', {
                        'type': 'button',
                        'class': 'btn',
                        'id': ignoreBtnID
                    }).text('Decline')).appendTo('#invis');
                $('#' + acceptBtnID).click(function() {
                    $.post({
                        data: JSON.stringify({
                            message_id:invitation.message_id,
                            accept: true
                        }),
                        url: document.location.origin + "/api/invitation",
                        success: function () {
                            $('#'+entryID).text(name + "\'s invitation accepted!");
                        }
                    });
                });
                $('#'+ ignoreBtnID).click(function() {
                    $.post({
                        data: JSON.stringify({
                            message_id:invitation.message_id,
                            accept: false
                        }),
                        url: document.location.origin + "/api/invitation",
                        success: function () {
                            $('#'+entryID).text(name + "\'s invitation declined!");
                        }
                    });
                });
            }
        });

    }

    function createFOFEntryInMessage(fof) {
        const entry = $('<li/>', {
            'class': 'msg-FOF-entry list-group-item',
        }).append($('<img/>', {
            'src': 'http://localhost:8080/file/avatar/' + fof.user_id + '.png',
            'class': 'avatar'
        })).append($('<h4/>', {
            'class': 'text-center'
        }).text(fof.first_name + " " + fof.last_name));
        $('<a/>', {
            'href': 'http://localhost:8080/profile/' + fof.user_id
        }).append(entry).appendTo('#FOF');
    }

    function postPhoto(photo) {
        $.post({
            url: document.location.origin + "/api/photo",
            data: JSON.stringify(photo),
            success: function(data) {
                $('#upload-photo-btn').hide();
                $('#upload-more-photo-btn').show();
                $('#success-message').show();
                $('#upload-more-photo-btn').click(function() {
                    location.reload(true);
                });
            }

        });
    }

    function formatDate(date) {
        let str = date.toString();
        const index = str.indexOf('G');
        if (~index) {
            str = str.substr(0, index);
        }
        return str;
    }

    function loadCollectionsToEdit() {
        $('.save_collection-btn').remove();
        $('#no-collection-to-edit').hide();
        $('#collection-list-to-edit .collection-entry').remove();
        $('#collection-save-success-msg').hide();
        $('#reset-collection-btn').trigger('click');
        $.get({
            url: document.location.origin + "/api/collection?friend_user_id=" + userID + "&load_rows=100&offset=0",
            success: function(data) {
                collections = data.collections;
                if (collections.length > 0) {
                    $.each(collections, function(i, collection) {
                        createCollectionToEditEntry(collection);
                    });
                } else {
                    $('#no-collection-to-edit').show();
                }
            }
        });
    }

    function createCollectionToEditEntry(collection) {
        const entry = $('<div/>', {
            'class': 'collection-entry list-group-item text-center'
        }).appendTo('#collection-list-to-edit');
        entry.append($('<a/>', {
            'href': collection.collection_url,
            'target': '_blank'
        }).append($('<h4/>').text(collection.title)))
            .append($('<div/>').text(collection.description));
        const editButton = $('<button/>', {
            'class':'btn',
            'style':'margin-top:20px',
            'id':'edit-'+collection.collection_id
        }).text('Edit');
        editButton.appendTo(entry);
        $('#collection-list-to-edit').on('click', '#edit-'+collection.collection_id, function(){
            console.log(collection);
            $('.save_collection-btn').remove();
            $('#create-collection-btn').hide();
            $('<span/>', {
                'class': 'save_collection-btn',
                'id': 'save-'+collection.collection_id
            }).append($('<button/>', {'class':'btn'}).text('Save'))
                .appendTo('#collection-btn-group');
            $('#save-collection-btn').show();
            $('#collection-title').val(collection.title);
            $('#collection-description').val(collection.description);

        });
        $('#collection-btn-group').on('click',  '#save-'+collection.collection_id, function() {
            const editedCol = {
                collection_id: collection.collection_id,
                edit_type:"metadata",
                title: $('#collection-title').val(),
                visibility: $('input[name=collection-visibility]:checked').val(),
                description: $('#collection-description').val()
            };
            $.ajax({
                url: document.location.origin + "/api/collection",
                type: 'PUT',
                data: JSON.stringify(editedCol),
                success: function() {
                    // $('#collection-save-success-msg').show();
                    $('#compose-collection').trigger('click');
                }
            });
        });
    }

    function loadeventsToEdit() {
        $('.save_event-btn').remove();
        $('#no-event-to-edit').hide();
        $('#event-list-to-edit .event-entry').remove();
        $('#event-save-success-msg').hide();
        $('#reset-event-btn').trigger('click');
        $.get({
            url: document.location.origin + "/api/event?friend_user_id=" + userID + "&load_rows=100&offset=0",
            success: function(data) {
                events = data.events;
                if (events.length > 0) {
                    $.each(events, function(i, event) {
                        createEventToEditEntry(event);
                    });
                } else {
                    $('#no-event-to-edit').show();
                }
            }
        });
    }

    function createEventToEditEntry(event) {
        const entry = $('<div/>', {
            'class': 'event-entry list-group-item text-center'
        }).appendTo('#event-list-to-edit');
        entry.append($('<a/>', {
            'href': event.event_url,
            'target': '_blank'
        }).append($('<h4/>').text(event.title)))
            .append($('<div/>').text(event.description));
        const editButton = $('<button/>', {
            'class':'btn',
            'style':'margin-top:20px',
            'id':'edit-event-'+event.event_id
        }).text('Edit');
        editButton.appendTo(entry);
        $('#event-list-to-edit').on('click', '#edit-event-'+event.event_id, function(){
            console.log(event);
            $('.save_event-btn').remove();
            $('#create-event-btn').hide();
            $('<span/>', {
                'class': 'save_event-btn',
                'id': 'save-event-'+event.event_id
            }).append($('<button/>', {'class':'btn'}).text('Save'))
                .appendTo('#event-btn-group');
            $('#save-event-btn').show();
            $('#event-title').val(event.title);
            $('#event-description').val(event.description);
            $('#event-country').val(event.country);
            $('#event-state').val(event.state);
            $('#event-city').val(event.city);
            $('#event-street').val(event.street);
            $('#event-zip').val(event.zip);
            $('#event-limit').val(event.limitation);
            $('#event-time-happened').val(event.time_happened.split(' ')[0]);

        });
        $('#event-btn-group').on('click',  '#save-event-'+event.event_id, function() {
            const country = /\S/.test($('#event-country').val()) ? $('#event-country').val() : null;
            const city = /\S/.test($('#event-city').val()) ? $('#event-city').val() : null;
            const street = /\S/.test($('#event-street').val()) ? $('#event-street').val() : null;
            const state = /\S/.test($('#event-state').val()) ? $('#event-state').val() : null;
            const zip = /\S/.test($('#event-zip').val()) ? $('#event-zip').val() : null;
            const geoUrl = encodeURI('https://maps.googleapis.com/maps/api/geocode/json?address=' +street + " " +
                city + " " + state + " " + country + " " + zip + '&key=AIzaSyBXIpemsf1saPdV3fWvp2jSIC0OU2o9uT8');
            let editedEvent = {
                event_id: event.event_id,
                title: $('#event-title').val(),
                country: country,
                state: state,
                city: city,
                street: street,
                zip: zip,
                time_happened: $('#event-time-happened').val(),
                description: $('#event-description').val(),
                lon: null,
                lat: null,
                visibility: $('input[name=event-visibility]:checked').val(),
                limitation: $('#event-limit').val()
            };
            if (country === null && city === null && street === null && state === null ) {
                $.ajax({
                    url: document.location.origin + "/api/event",
                    type: 'PUT',
                    data: JSON.stringify(editedEvent),
                    success: function() {
                        $('#compose-event').trigger('click');
                    }
                });
            } else {
                $.get({
                    url: geoUrl,
                    success: function(data) {
                        if (data.results.length > 0) {
                            const geo = data.results[0].geometry.location;
                            event.lat = geo.lat;
                            event.lon = geo.lng;
                        }
                        $.ajax({
                            url: document.location.origin + "/api/event",
                            type: 'PUT',
                            data: JSON.stringify(editedEvent),
                            success: function() {
                                $('#compose-event').trigger('click');
                            }
                        });
                    }
                });
            }

        });
    }

    function postEvent(event) {
        $.post({
            url: document.location.origin + "/api/event",
            data: JSON.stringify(event),
            success: function() {
                $('#compose-event').trigger('click');
            }
        });
    }




});



