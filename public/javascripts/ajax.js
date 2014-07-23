define(['jquery'], function ($) {
    return {
        ajaxRequest: function (url, type, cType, data, funct) {
            var contentType = (cType != null) ? cType : "application/x-www-login-urlencoded";
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
    }});
