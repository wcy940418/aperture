$(function() {
    var pathName = document.location.pathname.split("/");
    var numOnlyReg = /^\d+$/;
    var reg = new RegExp(numOnlyReg);
    var photoId = pathName[pathName.length - 1]
    if (pathName.length >= 3 && reg.test(photoId)) {
        $.get({
            url: document.location.origin + "/api/photo?photo_id=" + photoId,
            success: photoLoaded
        })
    }
});

var isMetaSideBarExtended = false;
var photoId = null;
var userId = null;
var likers = null;
var collections = null;
var addedCollectionIds = [];
var collectionDividerAdded = false;
var gMap = null;
var marker = null;

function toggleMetaSideBar() {
    if (!isMetaSideBarExtended) {
        isMetaSideBarExtended = true;
        $("#expand-meta-btn").html("&#9660");
        $("#meta-overlay").css("height", "200px").css("opacity", 1.0);
    } else {
        isMetaSideBarExtended = false;
        $("#expand-meta-btn").html("&#9650");
        $("#meta-overlay").css("height", "0px").css("opacity", 0.0);
    }
}

function photoLoaded(data) {
    $("#image-viewer").removeClass("loader")
        .addClass("full-image")
        .css("background-image", 'url("' + data.full_url + '")');
    console.log(data.full_url);
    gMap = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 40.69, lng: -95.35},
        zoom: 4
    });
    marker = new google.maps.Marker({
        position:{lat: 40.69, lng: -95.35},
        map:gMap,
        clickable:true
    });
    if (data.hasOwnProperty("lon") && data.hasOwnProperty("lat")) {
        $("#map").css("display", "block");
        gMap.panTo({lat:data.lat, lng:data.lon});
        marker.setPosition({lat:data.lat, lng:data.lon});
    } else {
        $("#map").css("display", "none");
    }
    $("#overlay").css("display", "block");
    $("#photo-title").html(data.title);
    $("#photo-description").html(data.description);
    $("#author").html(data.photographer);
    $("#time-captured").html(data.time_captured);
    $("#time-uploaded").html(data.time_uploaded);
    $("#category").html(data.category);
    photoId = data.photo_id;
    getUserId();
}

function getLikers(photo) {
    $.get({
        url: document.location.origin + "/api/like?photo_id=" + String(photo),
        success: function (data) {
            likers = data.likers;
            processLikeButton(userId, likers);
        }
    });
}

function getUserId() {
    $.get({
        url: document.location.origin + "/api/sign_in",
        success: function(data) {
            userId = data.user_id;
            getLikers(photoId);
            $("#bookmark").css("cursor", "pointer");
            $("#bookmark").click(function () {
                $("#collection-modal").modal("show");
                loadCollections(userId);
            });
        }
    });
}

function processLikeButton(userId, likers) {
    for (var i = 0; i < likers.length; ++i) {
        if(likers[i].user_id === userId) {
            $("#like").css("color", "red");
            $("#likes").html(likers.length);
            return;
        }
    }
    $("#like").click(function(){
        likePhoto(photoId);
    }).css("cursor", "pointer");
    $("#likes").html(likers.length);
}

function likePhoto(photo) {
    $.post({
        url: document.location.origin + "/api/like",
        data: JSON.stringify({photo_id:photo}),
        success: function() {
            $("#like").css("color","red").off().css("cursor", "auto");
            $("#likes").html(likers.length + 1);
        }
    })
}

function loadCollections(userId) {
    $("#submit-add").removeClass("btn-success").removeClass("disabled").addClass("btn-primary").html("Add");
    $("#collection-selection").html("Add to..." + " <span class=\"caret\"></span>").val("");
    $.get({
        url: document.location.origin + "/api/collection?friend_user_id=" + String(userId) + "&load_rows=99&offset=0",
        success: collectionsLoaded
    });
}

function collectionsLoaded(data) {
    collections = data.collections;
    addedCollectionIds = [];
    for (var x in collections) {
        var photos = collections[x].photos;
        for (var photo in photos) {
            if (photos[photo] === photoId) {
                addedCollectionIds.push(collections[x].collection_id);
            }
        }
    }
    loadCollectionManager(addedCollectionIds);
}

function clearCollectionManager() {
    $("#collections li:not(:last)").remove();
    collectionDividerAdded = false;
}

function loadCollectionManager(collectionsContainPhoto) {
    $("#collection-manager").removeClass("loader").addClass("dropdown");
    $("#add-new-collection").click(addNewCollection);
    clearCollectionManager();
    for (var idx in collections) {
        if (collectionsContainPhoto.indexOf(collections[idx].collection_id) === -1) {
            if (!collectionDividerAdded) {
                $("<li role=\"separator\" class=\"divider\"></li>").prependTo("#collections");
                collectionDividerAdded = true;
            }
            $("<li><a href='#'>" + collections[idx].title + "</a></li>").prependTo("#collections");
        }
    }
    $("#collection-selection").css("display", "block");
    $("#collections li a").click(function () {
        $("#collection-selection").html($(this).text() + " <span class=\"caret\"></span>").val($(this).text());
    });
    $("#submit-add").click(function () {
        addToCollection(photoId);
    });
}

function isBlank(str) {
    return (!str || /^\s*$/.test(str));
}

function addNewCollection() {
    var title = $("#new-collection-name").val();
    if (!isBlank(title)) {
        $.post({
            url: document.location.origin + "/api/collection",
            data: JSON.stringify({
                title: title,
                visibility:"Public",
                description:null
            }),
            success: newCollectionAdded
        });
    }
}

function newCollectionAdded(data) {
    loadCollections(userId);
}

function addToCollection(photoId) {
    var collectionName = $("#collection-selection").val();
    for (var x in collections) {
        if (collections[x].title === collectionName) {
            $("#submit-add").addClass("disabled");
            $.ajax({
                url: document.location.origin + "/api/collection",
                type:'PUT',
                data:JSON.stringify({
                    collection_id:collections[x].collection_id,
                    edit_type:"photos",
                    add:[photoId],
                    del:[]
                }),
                success: addedToCollection
            });
        }
    }
}

function addedToCollection() {
    $("#submit-add").removeClass("btn-primary").addClass("btn-success").html("Added");
    setTimeout(function () {
        $("#collection-modal").modal("hide");
    }, 1000);
}

function delFromCollection(photoId) {

}