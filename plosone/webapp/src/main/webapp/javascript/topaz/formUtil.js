topaz.formUtil = new Object();

topaz.formUtil = {
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
  
  checkFieldStrLength: function ( fieldObj, maxLength ) {
    if(fieldObj.value && fieldObj.value.length > maxLength) {
      alert("Your comment exceeds the allowable limit of " + maxLength + " characters by " + (fieldObj.value.length - maxLength) + " characters.");
      fieldObj.focus();
      return 0;
    }
    else {
      return -1;
    }
  },
  
  disableFormFields: function (formObj) {
    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type != 'hidden') {
        formObj.elements[i].disabled = true;
      } 
    }
  },
  
  enableFormFields: function (formObj) {
    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type != 'hidden') {
        formObj.elements[i].disabled = false;
      } 
    }
  },
  
  addItemInArray: function (array, item) {
    var foundItem = false;
    for (var i=0; i<array.length; i++) {
      alert("array[" + i + "] = " + array[i] + "\n" +
            "item = " + item);
      if (array[i] == item) 
        foundItem = true;
    }
    
    alert("foundItem = " + foundItem);
    
    if (!foundItem)
      array.push(item);
  },
  
  isItemInArray: function (array, item) {
    var foundItem = false;
    for (var i=0; i<array.length; i++) {
      if (array[i] == item) 
        foundItem = true;
    }
    
    if (foundItem)
      return true;
    else
      return false;
  },
  
  selectAllCheckboxes: function (srcObj, targetCheckboxObj) {
    if (srcObj.checked) {
      for (var i=0; i<targetCheckboxObj.length; i++) {
        targetCheckboxObj[i].checked = true;
      }
    }
    else {
      for (var i=0; i<targetCheckboxObj.length; i++) {
        targetCheckboxObj[i].checked = false;
      }
    }
  }
}
