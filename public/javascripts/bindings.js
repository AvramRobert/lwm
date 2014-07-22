/*CLASS: LOGIN*/
var error = '<div class=""> <div class=""><label class=""> Not authorized or response took too long!</label></div></div>' ;
var success = '<div class=""> <div class=""><label class=""> Successfully logged in! </label></div></div>' ;
var loginForm = '<form id="loginForm" class="" role="" data-bind="html: contents">' +
    '<div id="usernameContainer" class="">' +
    '<label class="">Username: </label>' +
    '<div class=""><input type="text" id="username" placeholder="Username" data-bind="">' +
    '</div>' +
    '<div id="passwordContainer" class=""><label class="">Password: </label>' +
    '<div class=""><input type="password" id="pass" placeholder="Password" data-bind=""></div></div>' +
    '<div id="submit" class="">' +
    '<div class="">' +
    '<input type="submit" id="search" value="Log In">' +
    '</div></div></form><script type="text/javascript">' +
    '$ (document).ready(function() {' +
    '$ ("#loginForm").submit(function(event) {' +
    'event.preventDefault();' +
    'var u = $ ( "#username" ).val();' +
    'var p = $ ("#pass").val();' +
    'ajaxRequest("/sessions", "POST", "application/json", { user : u, password : p }, setAuthMessage) ' +
    ';});});</script>';

function setAuthMessage(message) {
if(message.status == 401 || message.status == 500) applyView(error);
else if(message.toLowerCase() == 'login successful') applyView(success);
}


/*CLASS: REGISTRATION*/


/*CLASS: HOMEPAGE*/
