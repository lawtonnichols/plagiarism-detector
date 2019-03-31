/* See license.txt for terms of usage */

define([
    "firebug/lib/object",
    "firebug/firebug"
],
function(Obj, Firebug) {

// ************************************************************************************************
// Trace Module
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
/**
 * @module Use Firebug.TraceModule to register/unregister a trace listener that can be
 * used to customize look and feel of log messages in Tracing Console.
 *
 * Firebug.TraceModule.addListener - appends a tracing listener.
 * Firebug.TraceModule.removeListener - removes a tracing listener.
 */
Firebug.TraceModule = Obj.extend(Firebug.Module,
{
    dispatchName: "traceModule",
});

return Firebug.TraceModule;

// ************************************************************************************************
});
