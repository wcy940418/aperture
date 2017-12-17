var searchType, keyWord, resultOffset, resultCount, scope, days, maxDist;

$(function () {
    $("#search-type-list li a").click(function () {
        $("#search-for").html($(this).text());
        $("#search-type").val($(this).prop("id"));
        if ($(this).prop("id") === "photo") {
            $("#event-loc-option").css("visibility", "hidden").css("display", "none");
            $("#photo-option").css("visibility", "visible").css("display", "block");
        } else if ($(this).prop("id") === "event-1"){
            $("#photo-option").css("visibility", "hidden").css("display", "none");
            $("#event-loc-option").css("visibility", "visible").css("display", "block");
        } else {
            $("#photo-option").css("visibility", "hidden").css("display", "block");
            $("#event-loc-option").css("visibility", "hidden").css("display", "none");
        }
    });
    searchType = getParameterByName("type");
    keyWord = getParameterByName("keyword");
    resultOffset = getParameterByName("offset") === null ? 0 : getParameterByName("offset");
    resultCount = getParameterByName("load_rows") === null ? 50 : getParameterByName("load_rows");
    scope = getParameterByName("scope") === null ? "All" : getParameterByName("scope");
    days = getParameterByName("days") === null ? 7 : getParameterByName("days");
    maxDist = getParameterByName("max_dist") === null ? 10.0 : getParameterByName("max_dist");
    resultCount = getParameterByName("load_rows") === null ? 50 : getParameterByName("load_rows");
    if (searchType === "photo") {
        $.get({
            url: document.location.origin + "/api/search?type=photo&keyword=" + keyWord +
            "&offset=" + resultOffset + "&load_rows=" + resultCount + "&scope=" + scope +
            "&days=" + days,
            success: loadPhotos
        });
    } else if (searchType === "collection") {
        $.get({
            url: document.location.origin + "/api/search?type=collection&keyword=" + keyWord +
            "&offset=" + resultOffset + "&load_rows=" + resultCount,
            success: loadCollections
        });
    } else if (searchType === "event-0") {
        $.get({
            url: document.location.origin + "/api/search?type=event&keyword=" + keyWord +
            "&offset=" + resultOffset + "&load_rows=" + resultCount,
            success: loadEvents
        });
    } else if (searchType === "event-1") {
        $.get({
            url: "https://maps.googleapis.com/maps/api/geocode/json?address=" +
            keyWord +
            "&key=AIzaSyAQ-7aCfmolnTq7fw8dGtQN6RMa3wSb-0w",
            success: function (data) {
                if (data.status === "OK") {
                    var geo = data.results[0].geometry.location;
                    $.get({
                        url: document.location.origin + "/api/search?type=event&lon=" + geo.lng + "&lat=" + geo.lat +
                        "&max_distance=" + maxDist + "&offset=" + resultOffset + "&load_rows=" + resultCount,
                        success: loadEvents
                    });
                }
            }
        });
    } else if (searchType === "user") {
        $.get({
            url: document.location.origin + "/api/search?type=user&keyword=" + keyWord +
            "&offset=" + "0" + "&load_rows=" + "50",
            success: loadUsers
        });
    }
});

function loadPhotos(data) {
    if ($(".search-box").hasClass("centered")) {
        $(".search-box").removeClass("centered").addClass("topped");
    }
    $("#result").addClass("photo-wall");
    for (var photo in data.photos) {
        var title = data.photos[photo].hasOwnProperty("title") ? data.photos[photo].title : "Untitled";
        $("#result").append(
            "<a class='photo-tile' href=" + document.location.origin + "/photo/" + data.photos[photo].photo_id + ">" +
                "<img src='" + data.photos[photo].thumbnail_url + "' class='inner-photo'>" +
                "<div class='tile-overlay'>" +
                    "<div class='tile-title'>" + data.photos[photo].title + "</div>" +
                    "<div class='tile-author'>By " + data.photos[photo].uploader_name + "</div>" +
                "</div>" +
            "</a>"
        )
    }
}

function loadCollections(data) {
    if ($(".search-box").hasClass("centered")) {
        $(".search-box").removeClass("centered").addClass("topped");
    }
    $("#result").addClass("item-wall");
    for (var collection in data.collections) {
        if (data.collections[collection].photos.length > 0) {
            var coll = data.collections[collection];
            var title = coll.hasOwnProperty("title") ? coll.title : "Untitled";
            var description = coll.hasOwnProperty("description") ? coll.description : "blank";
            $("#result").append(
                "<a class='item' href=" + coll.collection_url + ">" +
                    "<img src='" + document.location.origin + "/file/thumb/" + coll.photos[0] + ".png' class='item-cover'>" +
                    "<div class='item-title-desc'>" +
                        "<h2>" + title + "</h2>" +
                        "<div>" + description + "</div>" +
                    "</div>" +
                    "<div class='item-meta'>" +
                        "<p><span class='glyphicon glyphicon-user'></span> <span id='author'>" + coll.host_name + "</span></p>" +
                        "<p><span class='glyphicon glyphicon-edit'></span> <span id='time-created'>" + coll.time_created + "</span></p>" +
                    "</div>" +
                "</a>"
            );
        }
    }
}

function loadEvents(data) {
    if ($(".search-box").hasClass("centered")) {
        $(".search-box").removeClass("centered").addClass("topped");
    }
    $("#result").addClass("item-wall");
    for (var event in data.events) {
        var eve = data.events[event];
        var description = eve.hasOwnProperty("description") ? eve.description : "blank";
        var timeHappened = eve.hasOwnProperty("time_happened") ? eve.time_happened : null;
        var timeHappenedHtml = timeHappened === null ? "" : "<p><span class='glyphicon glyphicon-flag'></span> " +
            "<span id='time-created'>" + eve.time_created + "</span></p>";
        $("#result").append(
            "<a class='item' href=" + eve.event_url + ">" +
                "<div class='item-title-desc'>" +
                    "<h2>" + eve.title + "</h2>" +
                    "<div>" + description + "</div>" +
                "</div>" +
                "<div class='item-meta'>" +
                    "<p><span class='glyphicon glyphicon-user'></span> <span id='author'>" + eve.host_name + "</span></p>" +
                    "<p><span class='glyphicon glyphicon-edit'></span> <span id='time-created'>" + eve.time_created + "</span></p>" +
                    timeHappenedHtml +
                "</div>" +
            "</a>"
        );
    }
}

function loadUsers(data) {
    if ($(".search-box").hasClass("centered")) {
        $(".search-box").removeClass("centered").addClass("topped");
    }
    $("#result").addClass("item-wall");
    for (var user in data.users) {
        var profile = data.users[user];
        var introduction = profile.hasOwnProperty("introduction") ? profile.introduction : "blank";
        var country = profile.hasOwnProperty("country") ?
            "<p><span class='glyphicon glyphicon-map-marker'></span> <span id='country'>" + profile.country + "</span></p>" : "";
        var gender = profile.hasOwnProperty("gender") ?
            "<p><span class='glyphicon glyphicon-user'></span> <span id='gender'>" + profile.gender + "</span></p>" : "";
        $("#result").append(
            "<a class='item' href=" + profile.profile_url + ">" +
                "<img src='" + profile.avatar_url + "' class='item-cover'>" +
                "<div class='item-title-desc'>" +
                    "<h2>" + profile.first_name + " " + profile.last_name + "</h2>" +
                    "<div>" + introduction + "</div>" +
                "</div>" +
                "<div class='item-meta'>" + country + gender + "</div>" +
            "</a>"
        );
    }
}

function goSearch() {
    var type = $("#search-type").val();
    var url, scope, days, maxDist;
    var keyWord = $("#search-keyword").val();
    if (type === 'photo') {
        scope = $("#photo-option input[type='radio']:checked").val()
        days = $("#photo-option input[type='number']").val();
        url = document.location.origin + document.location.pathname + "?type=" + type + "&keyword=" + keyWord +
            "&offset=" + "0" + "&load_rows=" + "50" + "&scope=" + scope + "&days=" + days;
        window.location.href = url;
    } else if (type === 'event-0') {
        url = document.location.origin + document.location.pathname + "?type=" + type + "&keyword=" + keyWord +
            "&offset=" + "0" + "&load_rows=" + "50";
        window.location.href = url;
    } else if (type === 'event-1') {
        maxDist = $("#event-loc-option input[type='number']").val();
        url = document.location.origin + document.location.pathname + "?type=" + type + "&keyword=" + keyWord +
            "&offset=" + "0" + "&load_rows=" + "50" + "&max_dist=" + maxDist;
        window.location.href = url;
    } else if (type === 'collection') {
        url = document.location.origin + document.location.pathname + "?type=" + type + "&keyword=" + keyWord +
            "&offset=" + "0" + "&load_rows=" + "50";
        window.location.href = url;
    } else if (type === 'user') {
        url = document.location.origin + document.location.pathname + "?type=" + type + "&keyword=" + keyWord +
            "&offset=" + "0" + "&load_rows=" + "50";;
        window.location.href = url;
    }

}

function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}