require.config({
    paths: {
        'knockout': 'http://knockoutjs.com/downloads/knockout-3.1.0',
        'jquery': 'http://code.jquery.com/jquery-2.1.0.min',
        'login': 'templates/login.scala.html'
    }
});


require(["viewController", "bindings", "text!login"], function(vc,b,login) {
    loadPage("LOGIN");

       function loadPage(page) {
        switch (page) {
            case "LOGIN":
                vc.applyView(login);
                break;
            case "REGISTRATION":
                break;
        }
    }
});

