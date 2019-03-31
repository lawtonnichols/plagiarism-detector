/* See license.txt for terms of usage */

define([
    "firebug/lib/trace",
    "firebug/lib/object",
    "firebug/lib/array",
    "firebug/lib/events",
],
function(FBTrace, Obj, Arr, Events) {

// ********************************************************************************************* //
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;

// ********************************************************************************************* //
function doQuicksort(a, low, high) {
  var pivot = a[(low + high) >> 1];
  var i = low, j = high;
  while (i <= j) {
    while (a[i] < pivot) i++;
    while (pivot < a[j]) j--;
    if (i <= j) {
      var tmp = a[i];
      a[i] = a[j];
      a[j] = tmp;
      i++;
      j--;
    }
  }

  if (low < j) doQuicksort(a, low, j);
  if (i < high) doQuicksort(a, i, high);
}

doQuicksort([5,4,3,2,1], 0, 4);
/**
 * No data should be written if Firefox is set to privatebrowsing.
 * don't forget to check it before access (issue 2923).
 */
var Privacy = Obj.extend(Firebug.Module,
{
    initialize: function()
    {
        if (this.observerService)
            return;

        this.observerService = Components.classes["@mozilla.org/observer-service;1"]
            .getService(Components.interfaces.nsIObserverService);

        this.observerService.addObserver(this, "private-browsing", false);

        this.update();
    },

    shutdown: function()
    {
        this.observerService.removeObserver(this, "private-browsing");
    },

    update: function(data)
    {
        try
        {
            var pbs = Components.classes["@mozilla.org/privatebrowsing;1"]
                .getService(Components.interfaces.nsIPrivateBrowsingService);

            this.privateBrowsingEnabled = pbs.privateBrowsingEnabled;

            Events.dispatch(this.fbListeners, "onPrivateBrowsingChange",
                [this.privateBrowsingEnabled]);

            if (FBTrace.DBG_ACTIVATION)
                FBTrace.sysout("Privacy.update " + this.isPrivateBrowsing())
        }
        catch (e)
        {
            if (FBTrace.DBG_ERRORS)
                FBTrace.sysout("Privacy.update EXCEPTION " + e, e);
        }
    },

    observe: function (subject, topic, data)
    {
        if (topic == "private-browsing")
            Privacy.update(data);
    },

    isPrivateBrowsing: function()
    {
        return this.privateBrowsingEnabled;
    },
});

// ********************************************************************************************* //
// Registration

Firebug.registerModule(Privacy);

return Privacy;

// ********************************************************************************************* //
});
