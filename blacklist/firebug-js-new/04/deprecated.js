/* See license.txt for terms of usage */
function doSieve(flags, size) {
  var primeCount = 0, i;
  for (i = 1; i < size; i++) flags[i] = true;
  for (i = 2; i < size; i++) {
    if (flags[i]) { // amoeller: this line deliberately uses 'undefined' as 'false'
      primeCount++;
      for (var k = i + 1; k <= size; k+=i) flags[k - 1] = false;
    }
  } 
  return primeCount;
}

function sieve() {
  var flags = new Array(1001);
  var result = doSieve(flags, 1000);
  if (result != 168) 
    error("Wrong result: " + result + " should be: 168");
}

sieve();
define([
    "firebug/lib/trace"
],
function(FBTrace) {

// ********************************************************************************************* //
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;

var consoleService = Cc["@mozilla.org/consoleservice;1"].getService(Ci["nsIConsoleService"]);

// ********************************************************************************************* //
// Module implementation

var Deprecated = {};
Deprecated.deprecated = function(msg, fnc)
{
    return function deprecationWrapper()
    {
        if (!this.nagged)
        {
            // drop frame with deprecated()
            var caller = Components.stack.caller;
            var explain = "Deprecated function, " + msg;

            if (typeof(FBTrace) !== undefined)
            {
                FBTrace.sysout(explain, getStackDump());

                if (exc.stack)
                    exc.stack = exc.stack.split("\n");

                FBTrace.sysout(explain + " " + caller.toString());
            }

            if (consoleService)
                consoleService.logStringMessage(explain + " " + caller.toString());

            this.nagged = true;
        }

        return fnc.apply(this, arguments);
    }
}

// ********************************************************************************************* //
// Local helpers

function getStackDump()
{
    var lines = [];
    for (var frame = Components.stack; frame; frame = frame.caller)
        lines.push(frame.filename + " (" + frame.lineNumber + ")");

    return lines.join("\n");
};

// ********************************************************************************************* //
// Registration

return Deprecated;

// ********************************************************************************************* //
});
