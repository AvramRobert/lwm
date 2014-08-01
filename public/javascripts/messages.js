/*CLASS: LOGIN*/
define(['viewController', 'ajax'], function(vc, a, r) {
    var error = '<div class=""> <div class=""><label class=""> Not authorized or response took too long!</label></div></div>';
    var success = '<div class=""> <div class=""><label class=""> Successfully logged in! </label></div></div>' ;
    return {
        setAuthMessage: function(header,status, message) {
            if(message.status == 401 || message.status == 500) vc.applyViewTo("contents", error);
            else if(status == "success") {
                alert(header);
                switch (header) {
                    case "r":
                        window.location.href = "/api/registration";
                        break;
                    default:
                        window.location.href = "/api/dashboard"
                        break;
                }
            }
        },

        setDashboard: function(message) {
            if(message.status == 401 || message.status == 500) vc.applyViewTo("contents", error);
            else if(message.status == 200) {
                window.location.href = "/api/dashboard";
            }
        }
    }
});

/*CLASS: REGISTRATION*/


/*CLASS: HOMEPAGE*/
