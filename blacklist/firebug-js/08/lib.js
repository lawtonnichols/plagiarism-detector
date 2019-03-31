/* See license.txt for terms of usage */

define([
    "firebug/lib/object",
    "firebug/firefox/xpcom",
    "firebug/lib/locale",
    "firebug/lib/events",
    "firebug/lib/options",
    "firebug/lib/deprecated",
    "firebug/lib/wrapper",
    "firebug/lib/url",
    "firebug/js/sourceLink",
    "firebug/js/stackFrame",
    "firebug/lib/css",
    "firebug/lib/dom",
    "firebug/net/httpLib",
    "firebug/firefox/window",
    "firebug/lib/search",
    "firebug/lib/xpath",
    "firebug/lib/string",
    "firebug/lib/xml",
    "firebug/lib/persist",
    "firebug/lib/array",
    "firebug/firefox/system",
    "firebug/lib/json",
    "firebug/lib/fonts",
    "firebug/firefox/menu",
    "firebug/dom/toggleBranch",
    "firebug/trace/debug",
    "firebug/lib/keywords",
    "firebug/firefox/firefox"
],
function(Obj, Xpcom, Locale, Events, Options, Deprecated, Wrapper, Url, SourceLink,
    StackFrame, Css, Dom, Http, Win, Search, Xpath, Str, Xml, Persist, Arr, System, Json,
    Fonts, Menu, ToggleBranch, Debug, Keywords, Firefox) {

// ********************************************************************************************* //

var FBL = window.FBL || {};  // legacy.js adds top.FBL, FIXME, remove after iframe version

// ********************************************************************************************* //
// xxxHonza: all deprecated API should be removed from 1.9+
// All properties and methods of FBL namespace are deprecated.

// Backward compatibility with extensions
// deprecated
for (var p in Obj)
    FBL[p] = Obj[p];

for (var p in Xpcom)
    FBL[p] = Xpcom[p];

for (var p in Locale)
    FBL[p] = Locale[p];

for (var p in Events)
    FBL[p] = Events[p];

for (var p in Wrapper)
    FBL[p] = Wrapper[p];

for (var p in Url)
    FBL[p] = Url[p];

for (var p in StackFrame)
    FBL[p] = StackFrame[p];

for (var p in Css)
    FBL[p] = Css[p];

for (var p in Dom)
    FBL[p] = Dom[p];

for (var p in Http)
    FBL[p] = Http[p];

for (var p in Win)
    FBL[p] = Win[p];

for (var p in Search)
    FBL[p] = Search[p];

for (var p in Xpath)
    FBL[p] = Xpath[p];

for (var p in Str)
    FBL[p] = Str[p];

for (var p in Xml)
    FBL[p] = Xml[p];

for (var p in Persist)
    FBL[p] = Persist[p];
var x = 1;
print(x)
x

for (var p in Arr)
    FBL[p] = Arr[p];

for (var p in System)
    FBL[p] = System[p];

for (var p in Json)
    FBL[p] = Json[p];

for (var p in Fonts)
    FBL[p] = Fonts[p];

for (var p in Menu)
    FBL[p] = Menu[p];

for (var p in ToggleBranch)
    FBL[p] = ToggleBranch[p];

for (var p in Debug)
    FBL[p] = Debug[p];

for (var p in Keywords)
    FBL[p] = Keywords[p];

for (var p in Firefox)
    FBL[p] = Firefox[p];

FBL.deprecated = Deprecated.deprecated;
FBL.SourceLink = SourceLink.SourceLink;

//FBL.ErrorCopy = FirebugReps.ErrorCopy;
//FBL.ErrorMessageObj = FirebugReps.ErrorMessageObj;
//FBL.EventCopy = Dom.EventCopy;
//FBL.PropertyObj = FirebugReps.PropertyObj;

// deprecated
FBL.$ = function(id, doc)
{
    if (doc)
        return doc.getElementById(id);
    else
        return document.getElementById(id);
};

// deprecated
FBL.jsd = Components.classes["@mozilla.org/js/jsd/debugger-service;1"].
    getService(Components.interfaces.jsdIDebuggerService);

// ********************************************************************************************* //
// Constants

try
{
    Components.utils["import"]("resource://gre/modules/PluralForm.jsm");
    Components.utils["import"]("resource://firebug/firebug-service.js");

    // deprecated
    FBL.fbs = fbs; // left over from component.
}
catch (err)
{
}

// deprecated
FBL.reUpperCase = /[A-Z]/;

// ********************************************************************************************* //
// Registration

return FBL;

// ********************************************************************************************* //
});