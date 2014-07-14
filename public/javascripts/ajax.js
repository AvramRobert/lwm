function ajaxRequest(url, type, contentType, data) {
    $.ajax({
        url: url,
        type: type,
        contentType: contentType + '; charset=UTF-8',
        data: JSON.stringify(data),
        success: function (data) {
            alert('YEA');
        },
        error: function () {
            alert('Something went wrong');
        }
    });
}