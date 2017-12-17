$(document).ready(function() {
    const pathArray = document.location.pathname.split('/');
    let curUser = 1;

    if (pathArray.length >= 2) {
        const profileID = pathArray[2];
        $.get({
            url: document.location.origin + "/api/profile?to_see_user_id=" + profileID,
            dataType: 'json',
            success: function(data) {
                console.log('profile loaded');
                loadProfile(data);
            }
        });

        $.get({
            url: document.location.origin + '/api/sign_in',
            success: function(data) {
                curUser = data.user_id;
                console.log('curUser '+curUser);
                if (curUser == profileID) {
                    $('#add-friend-btn').hide();
                    $('#send-message-btn').hide();
                    $('#edit-profile-btn').show();
                }
                $.get({
                    url: document.location.origin + "/api/friend?to_see_user_id=" + profileID,
                    dataType: 'json',
                    success: function(data) {
                        let friends = data.friends;
                        if (friends.length > 0) {
                            $.each(friends, function(i, friend) {
                                if (friend.user_id == curUser) {
                                    $('#add-friend-btn').hide();
                                    $('#send-message-btn').show();
                                    $('#profile-set-friendship-btn').show();
                                    $('#edit-profile-btn').hide();
                                }
                                createFriendEntry(friend);
                            });
                        } else {
                            $('<div/>', {'class': 'text-center'}).text('No friend found.').appendTo('.profile-content .friend-list');
                        }
                    }
                });

            }
        });
        $('#send-invitation-btn').click(function() {
            const msg = $('#invitation-message').val();
            const invi = {
                to_user_id: profileID,
                content: msg
            };
            $.post({
                url: document.location.origin + "/api/invitation",
                data: JSON.stringify(invi),
                dataType: 'json',
                success: function() {
                    $('#send-invitation-btn').hide();
                    $('#add-friend-popup .modal-body').text('Invitation sent successfully!');
                }
            });
        });

        $('#send-msg-btn').click(function() {
            const msg = $('#send-message').val();
            const sendMsg = {
                to_user_id: profileID,
                content: msg
            };
            $.post({
                url: document.location.origin + "/api/message",
                data: JSON.stringify(sendMsg),
                dataType: 'json',
                success: function() {
                    $('#send-msg-btn').hide();
                    $('#send-msg-popup .modal-body').text('Message sent successfully!');
                }
            });

        });

        $('#set-friendship-btn').click(function() {
            const friVis = {
                friend_user_id:profileID,
                visibility: $('input[name=fri-visibility]:checked').val()
            };
            console.log(JSON.stringify(friVis));
            $.ajax({
                data: JSON.stringify(friVis),
                type: 'PUT',
                url: document.location.origin + "/api/friend",
                success: function() {
                    $('#set-friendship-popup .modal-body').text("Saved successfully!");
                    $('#set-friendship-btn').hide();
                }
            });
        });

        $('#edit-profile-btn').click(function() {
            $.get({
                url: document.location.origin + "/api/profile?to_see_user_id=" + curUser,
                dataType: 'json',
                success: function (data) {
                    const profile = data;
                    $('#username-change').val(profile.username);
                    $('#email-change').val(profile.email);
                    $('#first-name-change').val(profile.first_name);
                    $('#last-name-change').val(profile.last_name);
                    $('#DOB').val(profile.DOB);
                    $('#country-change').val(profile.country);
                    $('#introduction-change').val(profile.introduction);
                    $('#save-profile-btn').click(function() {
                        let profileChange = {
                            username:$('#username-change').val(),
                            email:$('#email-change').val(),
                            first_name:$('#first-name-change').val(),
                            last_name:$('#last-name-change').val(),
                            DOB:$('#DOB').val(),
                            country:$('#country-change').val(),
                            introduction:$('#introduction-change').val(),
                            gender:$('input[name=gender]:checked').val()
                        };
                        console.log(JSON.stringify(profileChange));
                        $.ajax({
                            url: document.location.origin + "/api/profile",
                            type: 'PUT',
                            data: JSON.stringify(profileChange),
                            success: function () {
                                $('#save-profile-btn').hide();
                                $('#edit-profile-popup .modal-body').text('Changes saved!');
                            },
                            error:function() {
                                console.log(JSON.stringify(profileChange));
                            }

                        });
                    });

                }
            });
        })

    }


    $('#fileupload').fileupload({
        url: document.location.origin + "/api/photo_upload",
        dataType: 'json',
        done: function (e, data) {
            const file = data.result.files[0];
            $('<p/>').text(file.name).appendTo('#files');
            const avatarFile = {
                temp_filename: file.temp_filename
            };
            $('<img/>').attr('src', file.thumbnailUrl).appendTo('#files');
            $.post({
                url: document.location.origin + "/api/avatar",
                data: JSON.stringify(avatarFile),
                success: function () {
                    $('<div/>').text("Avatar uploaded successfully!").appendTo('#files');
                }
            });
        },
        progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .progress-bar').css(
                'width',
                progress + '%'
            );
        }
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');


    function loadProfile(profile) {
        $('.profile-content .avatar').attr('src', profile.avatar_url);
        $('.profile-name').text(profile.first_name + " " + profile.last_name);
        $('.profile-country').text(profile.country);
        $('.profile-intro').text(profile.introduction);
        getPhotos(profile.user_id, 10, 0);
        getEvents(profile.user_id, 10, 0);
        getCollections(profile.user_id, 10, 0);

    }

    function getPhotos(ID, rows, offset) {
        let photos = [];
        let url = document.location.origin + "/api/photo?friend_user_id=" + ID + "&load_rows=" + rows + "&offset=" + offset;
        $.get({
            url: url,
            // data: params,
            dataType: 'json',
            success: function(data) {
                photos = data.photos;
                if (photos.length > 0) {
                    $.each(photos, function(i, photo) {
                        createPhotoEntry(photo);
                    });
                } else {
                    $('<div/>', {'class': 'text-center'}).text('No photo found.').appendTo($('.profile-content .photo-list'));
                }
            }
        });
    }
    function createPhotoEntry(photo) {
        photo.time_uploaded = formatDate(new Date(photo.time_uploaded));
        let entry = $('<div/>', {"class": 'photo-entry'});
        entry.appendTo('.profile-content .photo-list');
        let row = $('<div/>', {"class": 'row'});
        row.appendTo(entry);
        let link = $('<a/>', {
            'href': 'http://localhost:8080/photo/' + photo.photo_id,
            'target': '_blank'
        });
        link.appendTo(row);
        $('<img/>', {
            'class': "photo-thumbnail col-sm-4",
            'src': photo.thumbnail_url
        }).appendTo(link);
        let textCol = $('<div/>', {'class': "col-sm-8"});
        textCol.appendTo(row);
        let timeStamp = $('<p/>', {
            'class': 'timestamp',
            'style': 'float:right'
        });
        timeStamp.text(photo.time_uploaded);
        timeStamp.appendTo(row);
        let photoTitle = $('<h3/>', {'class': 'photo-title'});
        photoTitle.text(photo.title);
        photoTitle.appendTo(textCol);
        let photoDes = $('<div/>', {'class': 'text-content'});
        photoDes.text(photo.description);
        photoDes.appendTo(textCol);
        $('<div/>', {'class': 'line'}).appendTo(entry);
    }
    function getEvents(ID, rows, offset) {
        let events = [];
        let url = document.location.origin + "/api/event?friend_user_id=" + ID + "&load_rows=" + rows + "&offset=" + offset;
        $.get({
            url: url,
            dataType:'json',
            success: function(data) {
                events = data.events;
                if (events.length > 0) {
                    $.each(events, function(i, event) {
                        createEventEntry(event);
                    });
                } else {
                    $('<div/>', {'class': 'text-center'}).text('No event found.').appendTo($('.profile-content .event-list'));
                }
            }

        });
    }
    function createEventEntry(event) {
        event.time_created = formatDate(new Date(event.time_created));
        event.time_happened = formatDate(new Date(event.time_happened));
        const entry = $('<div/>', {"class": 'event-entry list-group-item'});
        entry.appendTo('.event-list');
        const link = $('<a/>', {
            'href': 'http://localhost:8080/event/' + event.event_id,
            'target': '_blank'
        });
        link.appendTo(entry);
        const title = $('<h3/>', {'class': 'event-title'});
        title.text(event.title);
        title.appendTo(link);
        const addr = $('<p/>', {'class': 'address'});
        if (typeof event.city != 'undefined' && typeof event.country != 'undefined') {
            addr.text(event.city + ", " + event.country);
        } else if (typeof event.country != 'undefined') {
            addr.text(event.country);
        } else if (typeof event.city != 'undefined') {
            addr.text(event.city);
        }
        addr.appendTo(entry);
        if (typeof event.time_happened != 'undefined') {
            $('<div/>', {'style':'margin-bottom:10px'}).text(" "+event.time_happened).prepend($('<span/>', {'class':'glyphicon glyphicon-time'})).appendTo(entry);
        }
        $('<div/>',{
            'style': 'margin-bottom:10px'
        }).text(event.description).appendTo(entry);
        const timeStamp = $('<p/>', {
            'class': 'text-right',
        });
        timeStamp.text(event.time_created);
        timeStamp.appendTo(entry);
    }
    function getCollections(ID, rows, offset) {
        let collections = [];
        let url = document.location.origin + "/api/collection?friend_user_id=" + ID + "&load_rows=" + rows + "&offset=" + offset;
        $.get({
            url: url,
            dataType: 'json',
            success: function(data) {
                collections = data.collections;
                if (collections.length > 0) {
                    $.each(collections, function(i, collection) {
                        createCollectionEntry(collection);
                    });
                } else {
                    $('<div/>', {'class': 'text-center'}).text('No collection found.').appendTo($('.profile-content .collection-list'));
                }
            }
        });
    }

    function createCollectionEntry(collection) {
        collection.time_created = formatDate(new Date(collection.time_created));
        const entry = $('<div/>', {"class": 'collection-entry list-group-item'});
        entry.appendTo('.collection-list');
        const link = $('<a/>', {
            'href': 'http://localhost:8080/collection/' + collection.collection_id,
            'target': '_blank'
        });
        link.appendTo(entry);
        const title = $('<h3/>', {'class': 'collection-title'});
        title.text(collection.title);
        title.appendTo(link);
        $('<div/>',{
            'style': 'margin-bottom:10px'
        }).text(collection.description).appendTo(entry);
        const timeStamp = $('<p/>', {
            'class': 'text-right'
        });
        timeStamp.text(collection.time_created);
        timeStamp.appendTo(entry);
    }

    function formatDate(date) {
        let str = date.toString();
        const index = str.indexOf('G');
        if (~index) {
            str = str.substr(0, index);
        }
        return str;
    }

    function createFriendEntry(friend) {
        const entry = $('<div/>', {"class": 'friend-entry list-group-item'});
        entry.appendTo('.friend-list');
        const link = $('<a/>', {
            'href': 'http://localhost:8080/profile/' + friend.user_id,
            'target': '_blank'
        });
        link.appendTo(entry);
        $('<img/>', {
            'src': 'http://localhost:8080/file/avatar/' + friend.user_id + '.png',
            'class': 'avatar text-center',
            'style': 'margin-top:10px'
        }).appendTo(link);
        const name = $('<h4/>', {'class': 'text-center'});
        name.text(friend.first_name + " " + friend.last_name);
        name.appendTo(link);
    }




});