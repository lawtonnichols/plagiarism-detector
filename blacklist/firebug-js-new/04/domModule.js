/* See license.txt for terms of usage */

define([
    "firebug/lib/object",
    "firebug/firebug",
    "firebug/dom/domBreakpointGroup",
],
function(Obj, Firebug, DOMBreakpointGroup) {

// ********************************************************************************************* //
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;

// ********************************************************************************************* //
// DOM Module

Firebug.DOMModule = Obj.extend(Firebug.Module,
{
    dispatchName: "domModule",

    initialize: function(prefDomain, prefNames)
    {
        Firebug.Module.initialize.apply(this, arguments);

        if (Firebug.Debugger)
            Firebug.connection.addListener(this.DebuggerListener);
    },

    shutdown: function()
    {
        Firebug.Module.shutdown.apply(this, arguments);

        if (Firebug.Debugger)
            Firebug.connection.removeListener(this.DebuggerListener);
    },

    initContext: function(context, persistedState)
    {
        Firebug.Module.initContext.apply(this, arguments);
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
        context.dom = {breakpoints: new DOMBreakpointGroup()};
    },

    loadedContext: function(context, persistedState)
    {
        context.dom.breakpoints.load(context);
    },

    destroyContext: function(context, persistedState)
    {
        Firebug.Module.destroyContext.apply(this, arguments);

        context.dom.breakpoints.store(context);
    },
});

// ********************************************************************************************* //

Firebug.DOMModule.DebuggerListener =
{
    getBreakpoints: function(context, groups)
    {
        if (!context.dom.breakpoints.isEmpty())
            groups.push(context.dom.breakpoints);
    }
};

// ********************************************************************************************* //
// Registration

Firebug.registerModule(Firebug.DOMModule);

return Firebug.DOMModule;

// ********************************************************************************************* //
});

