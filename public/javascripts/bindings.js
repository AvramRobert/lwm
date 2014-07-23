/*CLASS: LOGIN*/
define(['viewController'], function(vc) {
    var error = '<div class=""> <div class=""><label class=""> Not authorized or response took too long!</label></div></div>';
    var success = '<div class=""> <div class=""><label class=""> Successfully logged in! </label></div></div>' ;
    return {
        setAuthMessage: function(message) {
            if(message.status == 401 || message.status == 500) vc.applyViewTo("contents", error);
            else if(message.toLowerCase() == 'login successful')vc.applyViewTo("contents", success);
        }
    }
});

/*CLASS: REGISTRATION*/


/*CLASS: HOMEPAGE*/
