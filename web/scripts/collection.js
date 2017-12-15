var isMetaSideBarExtended = false;
var isSlideshowExtended = false;
var collection = null;
var collectionId = null;
var photoId = null;
var userId = null;
var likers = {};
var collections = null;
var addedCollectionIds = {};
var collectionPhotos = {};
var collectionDividerAdded = false;
var gMap = null;
var slideShowSeq = [];

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

function toggleSlideshow() {
    if (!isSlideshowExtended) {
        isSlideshowExtended = true;
        $("#expand-slide-btn").html("&#9650");
        $("#slideshow-overlay").css("height", "200px").css("opacity", 1.0);
    } else {
        isSlideshowExtended = false;
        $("#expand-slide-btn").html("&#9660");
        $("#slideshow-overlay").css("height", "0px").css("opacity", 0.0);
    }
}

$(function() {
    var pathName = document.location.pathname.split("/");
    var numOnlyReg = /^\d+$/;
    var reg = new RegExp(numOnlyReg);
    var collectionId = pathName[pathName.length - 1]
    if (pathName.length >= 3 && reg.test(collectionId)) {
        $.get({
            url: document.location.origin + "/api/collection?collection_id=" + collectionId,
            success: collectionLoaded
        })
    }
});

function collectionLoaded(data) {
    collection = data;
    collectionId = collection.collection_id;
    if (collection.photos.length === 0) {
        $("#image-viewer").removeClass("loader").addClass("error-message")
            .html("<div style='color: #f7f7f7; font-size: 70px;'>This collection is blank</div>");
    } else {
        for (photo in collection.photos) {
            loadPhoto(collection.photos[photo]);
        }
    }
    getUserId();
}

function getUserId() {
    $.get({
        url: document.location.origin + "/api/sign_in",
        success: function(data) {
            userId = data.user_id;
            $("#bookmark").css("cursor", "pointer");
            $("#bookmark").click(function () {
                $("#collection-modal").modal("show");
                loadCollections(userId);
            });
        }
    });
}

function loadPhoto(photoId) {
    $.get({
        url: document.location.origin + "/api/photo?photo_id=" + photoId,
        success: photoLoaded
    })
}

function photoLoaded(data) {
    collectionPhotos[data.photo_id] = data;
    loadSlideshow(data.photo_id);
    if (Object.keys(collectionPhotos).length === 1) {
        loadFirstPhoto(data.photo_id);
    }
}

function loadFirstPhoto(photo_id) {
    $("#image-viewer").removeClass("loader")
        .addClass("full-image")
        .css("background-image", 'url("' + collectionPhotos[photo_id].full_url + '")');
    console.log(collectionPhotos[photo_id].full_url);
    gMap = new google.maps.Map(document.getElementById('map'), {
        center: {lat: -34.397, lng: 150.644},
        zoom: 4
    });
    var marker = new google.maps.Marker({
        position:{lat: -34.397, lng: 150.644},
        map:gMap
    });
    $("#overlay").css("display", "block");
    $("#slideshow").css("display", "block");
    changePhoto(photo_id);
}

function loadSlideshow(photo) {
    $("#slideshow-overlay").append('<img class="thumbnail-tile demo" id="p-' + photo + '" src="' +
        collectionPhotos[photo].thumbnail_url + '" onclick="onThumbClicked(this.id)">');
    slideShowSeq.push("p-" + photo);
}

function onThumbClicked(imgId) {
    var photo_id = imgId.split("-")[1];
    changePhoto(photo_id);
}

function changePhoto(photo_id) {
    $("#image-viewer")
        .css("background-image", 'url("' + collectionPhotos[photo_id].full_url + '")');
    console.log(collectionPhotos[photo_id].full_url);
    $("#photo-title").html(collectionPhotos[photo_id].title);
    $("#photo-description").html(collectionPhotos[photo_id].description);
    $("#author").html(collectionPhotos[photo_id].photographer);
    $("#time-captured").html(collectionPhotos[photo_id].time_captured);
    $("#time-uploaded").html(collectionPhotos[photo_id].time_uploaded);
    $("#category").html(collectionPhotos[photo_id].category);
    $("#slideshow-overlay .active").removeClass("active").addClass("demo");
    $("#p-" + photo_id).removeClass("demo").addClass("active");

    photoId = collectionPhotos[photo_id].photo_id;
    getLikers(photoId);
}

function getLikers(photo) {
    if (likers.hasOwnProperty(photo)) {
        processLikeButton(userId, likers[photo]);
    } else {
        $.get({
            url: document.location.origin + "/api/like?photo_id=" + String(photo),
            success: function (data) {
                likers[photoId] = data.likers;
                processLikeButton(userId, likers[photoId]);
            }
        });
    }
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
    $("#likes").html(likers[photoId].length);
}

function likePhoto(photo) {
    $.post({
        url: document.location.origin + "/api/like",
        data: JSON.stringify({photo_id:photo}),
        success: function() {
            $("#like").css("color","red").off().css("cursor", "auto");
            likers[photoId].push({user_id:userId, name:"me"});
            $("#likes").html(likers[photoId].length);
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
    addedCollectionIds[photoId] = [];
    for (var x in collections) {
        var photos = collections[x].photos;
        for (var photo in photos) {
            if (photos[photo] === photoId) {
                addedCollectionIds[photoId].push(collections[x].collection_id);
            }
        }
    }
    loadCollectionManager(addedCollectionIds[photoId]);
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