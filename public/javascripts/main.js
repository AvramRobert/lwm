require(["viewController"], function(v) {
loadPage("LOGIN");

    function loadPage(page) {
            switch(page) {
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
