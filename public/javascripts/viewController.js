var viewModel = {
    contents: ko.observable(),
    body: ko.observable()
};

function applyView(view) {
    viewModel.body(view);
    ko.applyBindings(viewModel) ;
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
define(function() {
    return {
        applyView: function(view) {
            viewModel.body(view);
            ko.applyBindings(viewModel);
        }
    }
});


