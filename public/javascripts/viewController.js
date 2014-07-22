/*require.config({
    paths: {
        'knockout': 'http://knockoutjs.com/downloads/knockout-3.1.0.js'
    }
});*/

define(function(){
    //var viewModel = {contents: k.ko.observable(), body: k.ko.observable()};
    return {
        applyView: function (view) {
            //viewModel.body(view);
            //k.ko.applyBindings(viewModel);
            alert(view);
        },

        applyViewTo: function (variable, view) {
            switch (variable) {
                case 'body':
                    //viewModel.body(view);
                    break;
                case 'contents':
                    //viewModel.contents(view);
                    break;
            }
            //k.ko.applyBindings(viewModel);
        }
    }
});
/*
var viewModel = {
    contents: ko.observable(),
    body: ko.observable()
};

function applyView(view) {
        viewModel.body(view);
        ko.applyBindings(viewModel);
    }

function applyViewTo(variable, view) {
    switch(variable) {
        case 'body':
            viewModel.body(view);
            break;
        case 'contents':
            viewModel.contents(view);
            break;
    }
    ko.applyBindings(viewModel);
}
*/

