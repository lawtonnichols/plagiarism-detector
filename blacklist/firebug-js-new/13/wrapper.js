/* See license.txt for terms of usage */

define([], function() {

// ********************************************************************************************* //
// Constants

var Ci = Components.interfaces;
var Cc = Components.classes;
var Cu = Components.utils;

var Wrapper = {};

// ********************************************************************************************* //
// Wrappers
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
Wrapper.getContentView = function(object)
{
    if (typeof(object) === 'undefined' || object == null)
        return false;

    // There is an exception when accessing StorageList.wrappedJSObject (which is
    // instance of StorageObsolete)
    if (object instanceof window.StorageList)
        return false;

    return (object.wrappedJSObject);
}

Wrapper.unwrapObject = function(object)
{
    // TODO: We might be able to make this check more authoritative with QueryInterface.
    if (typeof(object) === 'undefined' || object == null)
        return object;

    // There is an exception when accessing StorageList.wrappedJSObject (which is
    // instance of StorageObsolete)
    if (object instanceof window.StorageList)
        return object;

    if (object.wrappedJSObject)
        return object.wrappedJSObject;

    return object;
}

Wrapper.unwrapIValue = function(object, viewChrome)
{
    var unwrapped = object.getWrappedValue();
    if (viewChrome)
        return unwrapped;

    try
    {
        // XPCSafeJSObjectWrapper is not defined in Firefox 4.0
        // this should be the only call to getWrappedValue in firebug
        if (typeof(XPCSafeJSObjectWrapper) != "undefined")
            return XPCSafeJSObjectWrapper(unwrapped);
        else if (typeof(unwrapped) == "object")
            return XPCNativeWrapper.unwrap(unwrapped);
        else
            return unwrapped;
    }
    catch (exc)
    {
        if (FBTrace.DBG_ERRORS)
            FBTrace.sysout("unwrapIValue FAILS for "+object+" cause: "+exc,
                {exc: exc, object: object, unwrapped: unwrapped});
        return unwrapped;
    }
}

Wrapper.unwrapIValueObject = function(scope, viewChrome)
{
    var scopeVars = {};
    var listValue = {value: null}, lengthValue = {value: 0};
    scope.getProperties(listValue, lengthValue);

    for (var i = 0; i < lengthValue.value; ++i)
    {
        var prop = listValue.value[i];
        var name = Wrapper.unwrapIValue(prop.name);
        if (prop.value.jsType === prop.value.TYPE_NULL) // null is an object (!)
            scopeVars[name] = null;
        else
        {
            if (!Wrapper.shouldIgnore(name))
                scopeVars[name] = Wrapper.unwrapIValue(prop.value, viewChrome);
        }
    }
    return scopeVars;
};

// ********************************************************************************************* //

Wrapper.ignoreVars =
{
    "__firebug__": 1,
    "eval": 1,

    // We are forced to ignore Java-related variables, because
    // trying to access them causes browser freeze
    "java": 1,
    "sun": 1,
    "Packages": 1,
    "JavaArray": 1,
    "JavaMember": 1,
    "JavaObject": 1,
    "JavaClass": 1,
    "JavaPackage": 1,
    // internal firebug things XXXjjb todo we should privatize these
    "_firebug": 1,
    "_createFirebugConsole": 1,
    "_FirebugCommandLine": 1,
    "loadFirebugConsole": 1,
};

Wrapper.shouldIgnore = function(name)
{
    return (Wrapper.ignoreVars[name] === 1);
};

// ********************************************************************************************* //

return Wrapper;

// ********************************************************************************************* //
});
