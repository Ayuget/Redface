/**
* Scrolls to the bottom of the page
*/
function scrollToBottom() {
    window.scrollTo(0,document.body.scrollHeight);
}

/**
* Scrolls to a particular element
* @param id element id
*/
function scrollToElement(id) {
    var elem = document.getElementById(id);
    var x = 0;
    var y = 0;

    while (elem != null) {
        x += elem.offsetLeft;
        y += elem.offsetTop;
        elem = elem.offsetParent;
    }

    window.scrollTo(x, y);
}

/**
* Toggles inner child spoiler visibility
*/
function toggleSpoiler(obj){
	var div = obj.getElementsByTagName('div');
	if (div[0]) {
		if (div[0].style.visibility == "visible") {
			div[0].style.visibility = 'hidden';
		}
		else if (div[0].style.visibility == "hidden" || !div[0].style.visibility) {
			div[0].style.visibility = 'visible';
		}
	}
}

/**
* Deal with the clicked URL by calling the Java callback defined
* in the Javascript interface of the webview.
*
* Stops the event propagation to avoid issues with spoilers
*/
function handleUrl(event, postId, url) {
    event = event || window.event;
    Android.handleUrl(postId, url);

    event.stopPropagation();
}

/**
* Toggles overflow menu for a certain post
* @param id post id
*/
function toggleOverflowMenu(id){
	var overflowList = document.getElementById(id);
	if (overflowList != null) {
		if (overflowList.style.display == "block") {
			overflowList.style.display = 'none';
		}
		else if (overflowList.style.display == "none" || !overflowList.style.display) {
		    closeAllOverflowMenus();
			overflowList.style.display = 'block';
		}
	}
}

function closeAllOverflowMenus() {
    var menus = document.getElementsByClassName("post-overflow-icons");
    var i;
    for (i = 0; i < menus.length; i++) {
        menus[i].style.display = "none";
    }
}

function favoritePost(postId) {
    Android.markPostAsFavorite(postId);
    toggleOverflowMenu(postId);
}

/**
 *
 * Kindly provided by : http://codepen.io/fronterweb/pen/jcwgx
 *
 * Helper function, that allows to attach multiple events to selected objects
 * @param {[object]}   el       [selected element or elements]
 * @param {[type]}   events   [DOM object events like click or touch]
 * @param {Function} callback [Callback method]
 */
var addMulitListener = function(el, events, callback) {
  // Split all events to array
  var e = events.split(' ');

    // Loop trough all elements
    Array.prototype.forEach.call(el, function(element, i) {
      // Loop trought all events and add event listeners to each
      Array.prototype.forEach.call(e, function(event, i) {
        element.addEventListener(event, callback, false);
      });
    });
};

document.addEventListener("DOMContentLoaded", function(event) {
    /**
     * This function is adding ripple effect to elements
     * @param  {[object]} e [DOM objects, that should apply ripple effect]
     * @return {[null]}   [description]
     */
    addMulitListener(document.querySelectorAll('[material]'), 'click touchstart', function(e) {
        var ripple = this.querySelector('.ripple');
        var eventType = e.type;
        /**
         * Ripple
         */
        if(ripple == null) {
          // Create ripple
          ripple = document.createElement('span');
          ripple.classList.add('ripple');

          // Prepend ripple to element
          this.insertBefore(ripple, this.firstChild);

          // Set ripple size
          if(!ripple.offsetHeight && !ripple.offsetWidth) {
            var size = Math.max(e.target.offsetWidth, e.target.offsetHeight);
            ripple.style.width = size + 'px';
            ripple.style.height = size + 'px';
          }

        }

        // Remove animation effect
        ripple.classList.remove('animate');

        // get click coordinates by event type
        if(eventType == 'click') {
          var x = e.pageX;
          var y = e.pageY;
        } else if(eventType == 'touchstart') {
          var x = e.changedTouches[0].pageX;
          var y = e.changedTouches[0].pageY;
        }
        x = x - this.offsetLeft - ripple.offsetWidth / 2;
        y = y - this.offsetTop - ripple.offsetHeight / 2;

        // set new ripple position by click or touch position
        ripple.style.top = y + 'px';
        ripple.style.left = x + 'px';
        ripple.classList.add('animate');
    });
});
