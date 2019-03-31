/* See license.txt for terms of usage */
function ListElement(length, next) {
  this.length = length;
  this.next = next;
}

function makeList(length) {
  if (length == 0) return null;
  return new ListElement(length, makeList(length - 1));
}

function isShorter(x, y) {
  var xTail = x, yTail = y;
  while (yTail != null) {
    if (xTail == null) return true;
    xTail = xTail.next;
    yTail = yTail.next;
  }
  return false;
}

function doTakl(x, y, z) {
  if (isShorter(y, x)) {
    return doTakl(doTakl(x.next, y, z), 
                  doTakl(y.next, z, x), 
                  doTakl(z.next, x, y));
  } else {
    return z;
  }
}

function takl() {
  var result = doTakl(makeList(15), makeList(10), makeList(6));
  if (result.length != 10) 
    error("Wrong result: " + result.length + " should be: 10");
}

takl();
define([
],
function webAppFactory() {

// ********************************************************************************************* //

// WebApp: unit of related browsing contexts.
// http://www.whatwg.org/specs/web-apps/current-work/multipage/browsers.html#groupings-of-browsing-contexts
var WebApp = function(win)
{
    this.topMostWindow = win;
}

/**
 * The Window of the top-level browsing context, aka 'top'
 * http://www.whatwg.org/specs/web-apps/current-work/multipage/browsers.html#top-level-browsing-context
 */
WebApp.prototype =
{
    getTopMostWindow: function()
    {
        return this.topMostWindow;
    }
}

// ********************************************************************************************* //
// Registration

return WebApp;

// ********************************************************************************************* //
});