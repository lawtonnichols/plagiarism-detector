/* See license.txt for terms of usage */

define([
    "firebug/lib/string",
    "firebug/lib/css",
],
function(Str, Css) {

// ********************************************************************************************* //
// Trace Listener
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
 * Default implementation of a Trace listener. Can be used to customize tracing logs
 * in the console in order to easily distinguish logs.
 */
function TraceListener(prefix, type, removePrefix, stylesheetURL)
{
    this.prefix = prefix;
    this.type = type;
    this.removePrefix = removePrefix;
    this.stylesheetURL = stylesheetURL;
}

TraceListener.prototype =
/** @lends TraceListener */
{
    // Called when console window is loaded.
    onLoadConsole: function(win, rootNode)
    {
        if (this.stylesheetURL)
            Css.appendStylesheet(rootNode.ownerDocument, this.stylesheetURL);
    },

    // Called when a new message is logged in to the trace-console window.
    onDump: function(message)
    {
        var index = message.text.indexOf(this.prefix);
        if (index == 0)
        {
            if (this.removePrefix)
                message.text = message.text.substr(this.prefix.length);

            message.text = Str.trim(message.text);
            message.type = this.type;
        }
    }
};

// ********************************************************************************************* //
// Registration

return TraceListener;

// ********************************************************************************************* //
});
