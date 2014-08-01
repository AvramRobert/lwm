define(['jquery'], function ($) {
    return {
        ajaxRequest: function (url, type, cType, data, funct) {
            var contentType = (cType != null) ? cType : "application/x-www-login-urlencoded";
            $.ajax({
                url: url,
                type: type,
                contentType: contentType + '; charset=UTF-8',
                data: JSON.stringify(data),
                success: function (message, textStatus, request) {
                    if(funct != null) {
                        funct(request.getResponseHeader("access"),textStatus, message);
                    }
                },
                error: function (error) {
                    funct(error);
                }
            });
        }
    }});
