dojo.provide("topaz.topaz");


var topaz = new Object( );

document.getElementsByTagAndClassName = function(tagName, className) {
  if ( tagName == null )
    tagName = '*';
   
  var children = document.getElementsByTagName(tagName);
  var elements = new Array();
  
  if ( className == null )
    return children;
  
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    var classNames = child.className.split(' ');
    for (var j = 0; j < classNames.length; j++) {
      if (classNames[j] == className) {
        elements.push(child);
        break;
      }
    }
  }

  return elements;
}

document.getElementsByTagAndAttributeName = function(tagName, attributeName) {
  if ( tagName == null )
    tagName = '*';
   
  var children = document.getElementsByTagName(tagName);
  var elements = new Array();
  
  if ( attributeName == null )
    return children;
  
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    if (child.getAttributeNode(attributeName) != null) {
      elements.push(child);
    }
  }

  return elements;
}

/**
 * Extending the String object
 *
 **/
String.trim = function() {
  return this.replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g,"");
}

String.rtrim = function() {
  return this.replace(/\s+$/,"");
}

String.ltrim = function() {
  return this.replace(/^\s+/, "");
}

String.isEmpty = function() {
  return (this == null || this == "");
}

function toggleAnnotation(obj, userType) {
  ldc.show();
  var bugs = document.getElementsByTagAndClassName('a', 'bug');
  
  for (var i=0; i<bugs.length; i++) {
    var classList = new Array();
    classList = bugs[i].className.split(' ');
    for (var n=0; n<classList.length; n++) {
      if (classList[n].match(userType))
        bugs[i].style.display = (bugs[i].style.display == "none") ? "inline" : "none";
    }
  }
  
  if (obj.className.match('collapse')) {
    obj.className = obj.className.replace(/collapse/, "expand");
    obj.innerHTML = "Turn annotations on";
  }
  else {
    obj.className = obj.className.replace(/expand/, "collapse");
    obj.innerHTML = "Turn annotations off";
  }
  
  ldc.hide();
  
  return false;
}

function getAnnotationEl(annotationId) {
  var elements = document.getElementsByTagAndAttributeName('a', 'displayid');
     
  var targetEl
  for (var i=0; i<elements.length; i++) {
    var elDisplay = topaz.domUtil.getDisplayId(elements[i]);
    var displayList = elDisplay.split(',');

    for (var n=0; n<displayList.length; n++) {
      if (displayList[n] == annotationId) {
        targetEl = elements[i];
        return targetEl;
      }
    }
    
  }
  
  return false;
}

var elLocation;
function jumpToAnnotation(annotationId) {
  var targetEl = getAnnotationEl(annotationId);

  //alert("targetEl = " + targetEl.nodeName);
  if (targetEl) {
    elLocation = topaz.domUtil.getCurrentOffset(targetEl);
    window.scrollBy(0, elLocation.top);
  }
}


