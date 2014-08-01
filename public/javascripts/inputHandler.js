define(function() {
  return {
      checkInput: function(input) {
          var ok = true;
          for(var j in input) {
              if(input.hasOwnProperty(j) && input[j].trim() == '') {
                  $('#'+j ).css({'background-color': 'crimson'});
                  ok = false;
              }
          }
          return ok;
      }
  }
});