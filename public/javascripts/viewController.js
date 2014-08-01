define(['knockout', 'modelView'], function(k, mv){
    var viewModel = {body: k.observable(), contents: k.observable()};
    return {
        applyViewValues: function (view) {
            viewModel.body(view);
            mv.updateView('body', view);
            k.applyBindings(viewModel);
        },

        applyViewTo: function (variable, view) {
            switch (variable) {
                case 'body':
                    viewModel.body(view);
                    mv.updateView('body', view);
                    break;
                case 'contents':
                    viewModel.contents(view);
                    mv.updateView('#contents', view);
                    break;
            }

        }
    }
});