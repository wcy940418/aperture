var gMap, eventMarker, eventContent, userId;
var participatorsPerLine = 5;

$(function () {
    getUserId();
});

function getUserId() {
    $.get({
        url: document.location.origin + "/api/sign_in",
        success:loadEvent
    })

}

function loadEvent(data) {
    userId = data.user_id;
    var pathName = document.location.pathname.split("/");
    var numOnlyReg = /^\d+$/;
    var reg = new RegExp(numOnlyReg);
    var eventId = pathName[pathName.length - 1];
    if (pathName.length >= 3 && reg.test(eventId)) {
        $.get({
            url: document.location.origin + "/api/event?event_id=" + eventId,
            success: eventLoaded
        })
    }
}

function eventLoaded(data) {
    eventContent = data;
    if (data.hasOwnProperty('lon') && data.hasOwnProperty("lat")) {
        $("#map").addClass("map");
        gMap = new google.maps.Map(document.getElementById('map'), {
            center: {lat: data.lat, lng: data.lon},
            zoom: 6
        });
        eventMarker = new google.maps.Marker({
            position:{lat: data.lat, lng: data.lon},
            map:gMap,
            clickable:true
        });
    } else {
        $("#map-section").remove();
    }
    if (data.hasOwnProperty("limitation")) {
        $("#availability").html(Math.max(0, data.limitation - data.participators_num) + " / " + data.limitation);
    } else {
        $("#availability").html("Unlimitated");
    }
    $("#host").html(data.host_name);
    $("#time-created").html(data.time_created);
    $("#title").html(data.title);
    if (data.hasOwnProperty("time_happened")) {
        $("#time-happened").html(data.time_happened);
    } else {
        $("#info-section .glyphicon-flag").remove();
    }
    if (data.hasOwnProperty("description")) {
        $("#description").html(data.description);
    } else {
        $("#description-section").remove()
    }
    if (data.host_id === userId) {
        $("#rsvp-btn").addClass("disabled").removeClass("btn-primary").addClass("btn-default").html("Hosted");
        $.get({
            url: document.location.origin + "/api/register?event_id=" + data.event_id,
            success: participatorsLoaded
        });
    } else if (data.hasOwnProperty("limitation") && data.participators_num >= data.limitation){
        $("#rsvp-btn").addClass("disabled").removeClass("btn-primary").addClass("btn-default").html("Unavailable");
    } else if (data.participated) {
        $("#rsvp-btn").addClass("disabled").removeClass("btn-primary").addClass("btn-default").html("Reserved");
    } else {
        $("#rsvp-btn").click(function () {
            $("#rsvp-btn").addClass("disabled");
            $.post({
                url: document.location.origin + "/api/register",
                data: JSON.stringify({event_id:eventContent.event_id}),
                success: registerSucceed
            });
        });
    }
    $(".loader").remove();
    $(".container").css("display", "block");
}

function participatorsLoaded(data) {
    $("#main-container").append("<div class='row'><div id='participators'" +
        " class='col-sm-12'></div></div>");
    if (data.participators.length === 0) {
        $("#participators").append("<div class='no-participators'>No participators</div>");
    } else {
        $("#participators").append("<div>Your event has following participators:</div>");
        $("#participators").append("<table class='table table-bordered'></table>");
        for (var participator in data.participators) {
            if (participator % participatorsPerLine === 0) {
                $("#participators table").append("<tr></tr>");
            }
            $("#participators table tr").eq(Math.floor(participator / participatorsPerLine)).append(
                "<td class='table-bordered'>" +
                data.participators[participator].name +
                "</td>");
        }
    }
}

function registerSucceed(data) {
    $("#rsvp-btn").off().removeClass("btn-primary").addClass("btn-success").html("Reserved");
}