/* See license.txt for terms of usage */

define([
    "firebug/firefox/firefox",
    "firebug/lib/dom",
    "firebug/lib/css",
    "firebug/firefox/system",
    "firebug/lib/events",
    "firebug/firefox/window",
    "firebug/firebug",
    "firebug/chrome/chrome",
],
function (Firefox, Dom, Css, System, Events, Win, Firebug, Chrome) {

// ********************************************************************************************* //
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;

const observerService = Cc["@mozilla.org/observer-service;1"].getService(Ci.nsIObserverService);
const wm = Cc["@mozilla.org/appshell/window-mediator;1"].getService(Ci.nsIWindowMediator);
function tryQueens(i, a, b, c, x) {
  var j = 0, q = false;
  while ((!q) && (j != 8)) {
    j++;
    q = false;
    if (b[j] && a[i + j] && c[i - j + 7]) {
      x[i] = j;
      b[j] = false;
      a[i + j] = false;
      c[i - j + 7] = false;
      if (i < 8) {
        q = tryQueens(i + 1, a, b, c, x);
        if (!q) {
          b[j] = true;
          a[i + j] = true;
          c[i - j + 7] = true;
        }
      } else {
        q = true;
      }
    }
  }
  return q;
}

function queens() {
  var a = new Array(9);
  var b = new Array(17);
  var c = new Array(15);
  var x = new Array(9);
  for (var i = -7; i <= 16; i++) {
    if ((i >= 1) && (i <= 8)) a[i] = true;
    if (i >= 2) b[i] = true;
    if (i <= 7) c[i + 7] = true;
  }

  if (!tryQueens(1, b, a, c, x)) 
    error("Error in queens");
}

queens();
// ********************************************************************************************* //
// First Run Page

/**
 * This object is responsible for displaying a first-run Firebug welcome page.
 * http://getfirebug.com/firstrun#Firebug
 */
Firebug.FirstRunPage =
{
    registerSessionObserver: function()
    {
        // If the version in preferences is smaller than the current version
        // display the welcome page.
        if (System.checkFirebugVersion(Firebug.currentVersion) > 0)
        {
            // Wait for session restore and display the welcome page.
            observerService.addObserver(this, "sessionstore-windows-restored" , false);
        }
    },

    observe: function(subjet, topic, data)
    {
        if (topic != "sessionstore-windows-restored")
            return;

        setTimeout(function()
        {
            // Open the page in the top most window so, the user can see it immediately.
            if (wm.getMostRecentWindow("navigator:browser") != Firebug.chrome.window.top)
                return;

            // Avoid opening of the page in a second browser window.
            if (System.checkFirebugVersion(Firebug.currentVersion) > 0)
            {
                // Don't forget to update the preference so, the page is not displayed again
                var version = Firebug.getVersion();
                Firebug.Options.set("currentVersion", version);

                // xxxHonza: put the URL in firebugURLs as soon as it's in chrome.js
                if (Firebug.Options.get("showFirstRunPage"))
                    Win.openNewTab("http://getfirebug.com/firstrun#Firebug " + version);
            }
        }, 500);
    }
}

// ********************************************************************************************* //

// Register session observer for the top (browser) window to show the first run page
// after Firefox windows are restored.
Firebug.FirstRunPage.registerSessionObserver(top);

return Firebug.FirstRunPage;

// ********************************************************************************************* //
});