var formUtil = new Object();

formUtil = {
  textCues: {
  	on: function ( formEl, textCue ) {
    if (formEl.value == "")
    	formEl.value = textCue;
	  },
	  
	  off: function ( formEl, textCue ) {
	    if (formEl.value == textCue)
	    	formEl.value = "";
	  },
	  
	  reset: function ( formEl, textCue ){
	    formEl.value = textCue;
	  }
  },
  
  toggleFieldsByClassname: function ( toggleClassOn, toggleClassOff ) {
    var targetElOn = document.getElementsByTagAndClassName(null, toggleClassOn);
    var targetElOff = document.getElementsByTagAndClassName(null, toggleClassOff);

    for (var i=0; i<targetElOn.length; i++) {
      targetElOn[i].style.display = "block";
    }

    for (var i=0; i<targetElOff.length; i++) {
      targetElOff[i].style.display = "none";
    }
  },
  
  checkFieldStrLength: function ( fieldObj ) {
    if(fieldObj.value && fieldObj.value.length > formConfig.commentMaxLen) {
      alert("Your comment exceeds the allowable limit of " + formConfig.commentMaxLen + " characters by " + (fieldObj.value.length - formConfig.commentMaxLen) + " characters.");
      fieldObj.focus();
      return 0;
    }
    else {
      return -1;
    }
  }
}
