/*require.config({
    paths: {
        'jquery': 'http://code.jquery.com/jquery-2.1.0.min.js'
    }
});*/


require(["viewController"], function(v) {
       function loadPage(page)
        {
        switch (page) {
            case "LOGIN":
                v.applyView(page);
                break;
            case "REGISTRATION":
                break;
        }
    }
});

/*
    function loadPage(page) {

        switch (page) {
            case 'LOGIN':
                applyView(loginForm);
                break;
            case 'REGISTRATION':

                break;
        }
    }
*/
