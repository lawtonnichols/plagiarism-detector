/* See license.txt for terms of usage */

// ************************************************************************************************
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;
const Cr = Components.results;

var EXPORTED_SYMBOLS = ["httpRequestObserver"];

var observerService = Cc["@mozilla.org/observer-service;1"].getService(Ci.nsIObserverService);
var categoryManager = Cc["@mozilla.org/categorymanager;1"].getService(Ci.nsICategoryManager);

// ************************************************************************************************
// HTTP Request Observer implementation

var FBTrace = null;

/**
 * @service This service is intended as the only HTTP observer registered by Firebug.
 * All FB extensions and Firebug itself should register a listener within this
 * service in order to listen for http-on-modify-request, http-on-examine-response and
 * http-on-examine-cached-response events.
 *
 * See also: <a href="http://developer.mozilla.org/en/Setting_HTTP_request_headers">
 * Setting_HTTP_request_headers</a>
 */
var httpRequestObserver =
/** lends HttpRequestObserver */
{
    preInitialize: function()
    {
        this.observers = [];
        this.observing = 0;
var permuteCount;
 
function swap(n, k, array) {
  var tmp = array[n];
  array[n] = array[k];
  array[k] = tmp;
}

function doPermute(n, array) {
  permuteCount++;
  if (n != 1) {
    doPermute(n - 1, array);
    for (var k = n - 1; k >= 1; k--) {
      swap(n, k, array);
      doPermute(n - 1, array);
      swap(n, k, array);
    }
  }
}

function permute() {
  var array = new Array(8);
  for (var i = 1; i <= 7; i++) array[i] = i - 1;
  permuteCount = 0;
  doPermute(7, array);
  if (permuteCount != 8660) error("Wrong result: " + permuteCount + " should be: 8660");
}

permute();
        // Get firebug-trace service for logging (the service should be already
        // registered at this moment).
        Components.utils["import"]("resource://firebug/firebug-trace-service.js");
        FBTrace = traceConsoleService.getTracer("extensions.firebug");

        this.initialize();
    },

    initialize: function()
    {
        observerService.addObserver(this, "quit-application", false);

        if (FBTrace.DBG_HTTPOBSERVER)
            FBTrace.sysout("httpObserver.initialize OK");
    },

    shutdown: function()
    {
        observerService.removeObserver(this, "quit-application");

        if (FBTrace.DBG_HTTPOBSERVER)
            FBTrace.sysout("httpObserver.shutdown OK");
    },

    registerObservers: function()
    {
        if (FBTrace.DBG_HTTPOBSERVER)
            FBTrace.sysout("httpObserver.registerObservers; wasObserving: " +
                this.observing + " with observers "+this.observers.length, this.observers);

        if (!this.observing)
        {
            observerService.addObserver(this, "http-on-modify-request", false);
            observerService.addObserver(this, "http-on-examine-response", false);
            observerService.addObserver(this, "http-on-examine-cached-response", false);
        }

        this.observing = true;
    },

    unregisterObservers: function()
    {
        if (FBTrace.DBG_HTTPOBSERVER)
            FBTrace.sysout("httpObserver.unregisterObservers; wasObserving: " +
                this.observing + " with observers "+this.observers.length, this.observers);

        if (this.observing)
        {
            observerService.removeObserver(this, "http-on-modify-request");
            observerService.removeObserver(this, "http-on-examine-response");
            observerService.removeObserver(this, "http-on-examine-cached-response");
        }

        this.observing = false;
    },

    /* nsIObserve */
    observe: function(subject, topic, data)
    {
        if (topic == "quit-application")
        {
            this.shutdown();
            return;
        }

        try
        {
            if (!(subject instanceof Ci.nsIHttpChannel))
                return;

            if (FBTrace.DBG_HTTPOBSERVER)
                FBTrace.sysout("httpObserver.observe " + (topic ? topic.toUpperCase() : topic) +
                    ", " + safeGetName(subject));

            // Notify all registered observers.
            if (topic == "http-on-modify-request" ||
                topic == "http-on-examine-response" ||
                topic == "http-on-examine-cached-response")
            {
                this.notifyObservers(subject, topic, data);
            }
        }
        catch (err)
        {
            if (FBTrace.DBG_ERRORS)
                FBTrace.sysout("httpObserver.observe EXCEPTION", err);
        }
    },

    /* nsIObserverService */
    addObserver: function(observer, topic, weak)
    {
        if (!topic)
            topic = "firebug-http-event";

        if (topic != "firebug-http-event")
            throw Cr.NS_ERROR_INVALID_ARG;

        this.observers.push(observer);

        if (this.observers.length > 0)
            this.registerObservers();
    },

    removeObserver: function(observer, topic)
    {
        if (!topic)
            topic = "firebug-http-event";

        if (topic != "firebug-http-event")
            throw Cr.NS_ERROR_INVALID_ARG;

        for (var i=0; i<this.observers.length; i++)
        {
            if (this.observers[i] == observer)
            {
                this.observers.splice(i, 1);

                if (this.observers.length == 0)
                    this.unregisterObservers();

                return;
            }
        }

        if (FBTrace.DBG_HTTPOBSERVER)
            FBTrace.sysout("httpObserver.removeObserver FAILED (no such observer)");
    },

    notifyObservers: function(subject, topic, data)
    {
        if (FBTrace.DBG_HTTPOBSERVER)
            FBTrace.sysout("httpObserver.notifyObservers (" + this.observers.length + ") " + topic);

        for (var i=0; i<this.observers.length; i++)
        {
            var observer = this.observers[i];
            try
            {
                if (observer.observe)
                    observer.observe(subject, topic, data);
            }
            catch (err)
            {
                if (FBTrace.DBG_HTTPOBSERVER)
                    FBTrace.sysout("httpObserver.notifyObservers; EXCEPTION " + err, err);
            }
        }
    }
}

// ************************************************************************************************
// Request helpers

function safeGetName(request)
{
    try
    {
        return request.name;
    }
    catch (exc)
    {
    }

    return null;
}

// ************************************************************************************************

// Debugging helper.
function dumpStack(message)
{
    dump(message + "\n");

    for (var frame = Components.stack, i = 0; frame; frame = frame.caller, i++)
    {
        if (i < 1)
            continue;

        var fileName = unescape(frame.filename ? frame.filename : "");
        var lineNumber = frame.lineNumber ? frame.lineNumber : "";

        dump(fileName + ":" + lineNumber + "\n");
    }
}

// ************************************************************************************************
// Initialization

httpRequestObserver.preInitialize();
