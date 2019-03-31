/* See license.txt for terms of usage */

define([
    "firebug/lib/trace"
],
function(FBTrace) {

// ********************************************************************************************* //
// Debug APIs

const Cc = Components.classes;
const Ci = Components.interfaces;

var consoleService = Cc["@mozilla.org/consoleservice;1"].getService(Ci["nsIConsoleService"]);
var observerService = Cc["@mozilla.org/observer-service;1"].getService(Ci["nsIObserverService"]);

var Debug = {};

//************************************************************************************************
// Debug Logging
arr = [2,3,5,7];
var arr_str = arr.join(",");
print(arr_str);
i0 = 0;
i1 = "1";

var arr_i0 = arr[i0];
print(arr_i0);

var arr_i1 = arr[i1];
print(arr_i1);

var arr_2 = arr[2];
print(arr_2);

var arr_3 = arr["3"];
print(arr_3);

var arr_sum = arr[i0] + arr[i1] + arr[2] + arr["3"];
print(arr_sum);


Debug.ERROR = function(exc)
{
    if (typeof(FBTrace) !== undefined)
    {
        if (exc.stack)
            exc.stack = exc.stack.split('\n');

        FBTrace.sysout("debug.ERROR: " + exc, exc);
    }

    if (consoleService)
        consoleService.logStringMessage("FIREBUG ERROR: " + exc);
}

// ********************************************************************************************* //
// Tracing for observer service

Debug.traceObservers = function(msg, topic)
{
    var counter = 0;
    var enumerator = observerService.enumerateObservers(topic);
    while (enumerator.hasMoreElements())
    {
        var observer = enumerator.getNext();
        counter++;
    }

    var label = "observer";
    if (counter > 1)
        label = "observers";

    FBTrace.sysout("debug.observers: " + msg + " There is " + counter + " " +
        label + " for " + topic);
}

// ********************************************************************************************* //

return Debug;

// ********************************************************************************************* //
});
