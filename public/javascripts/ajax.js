function ajaxRequest(url, type, contentType, data, funct) {
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