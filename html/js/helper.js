$.fn.serializeObject = function() {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

function submitForm(e) {
  	  	                                  
		$.ajax( {
        method: "POST",
        contentType: "application/json",                
        url: "/api",   
        data: JSON.stringify($(e).serializeObject()) 
      } )
		  .done(function(result) {		  	
          var resultTXT = JSON.stringify(JSON.parse(result)); 
		    $(e).find('.result').html( resultTXT );
		  })
		  .fail(function() {
		    alert( "error" );
		  })
		  .always(function() {		    
		  });
      
      return false;
}
