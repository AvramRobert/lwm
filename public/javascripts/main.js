require.config({
    paths: {
        'knockout': 'http://knockoutjs.com/downloads/knockout-3.1.0',
        'jquery': 'http://code.jquery.com/jquery-2.1.0.min'
    }
});


require(["viewController"], function(vc) {
    //loadPage("LOGIN");

       function loadPage(page) {
        switch (page) {
            case "LOGIN":
                vc.applyViewTo('body',login);
                break;
            case "REGISTRATION":
                vc.applyViewTo('body', register);
                break;
        }
    }
});

