define(['jquery'], function($){
   return {
     updateView: function(reference, content) {
        $(reference).html(content);
     }
   }
});