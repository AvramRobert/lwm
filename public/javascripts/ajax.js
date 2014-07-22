function ajaxRequest(url, type, cType, data, funct) {
    var contentType = (cType != null)? cType: "application/x-www-form-urlencoded";
    $.ajax({
        url: url,
        type: type,
        contentType: contentType + '; charset=UTF-8',
        data: JSON.stringify(data),
        success: function (message) {
            funct(message);
        },
        error: function (error) {
            funct(error);
        }
    });
}
