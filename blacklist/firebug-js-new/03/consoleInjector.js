/* See license.txt for terms of usage */

define([
    "firebug/lib/object",
    "firebug/firebug",
    "firebug/chrome/reps",
    "firebug/lib/locale",
    "firebug/lib/events",
    "firebug/lib/url",
    "firebug/js/stackFrame",
    "firebug/firefox/window",
    "firebug/console/console",
    "firebug/lib/array",
    "firebug/console/consoleExposed",
    "firebug/console/errors",
],
function(Obj, Firebug, FirebugReps, Locale, Events, Url, StackFrame, Win, Console, Arr) {

// ********************************************************************************************* //
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;
const Cu = Components.utils;

// ********************************************************************************************* //
// Console Injector

Firebug.Console.injector =
{
    isAttached: function(context, win)
    {
        var handler = this.getConsoleHandler(context, win);

        if (FBTrace.DBG_CONSOLE)
            FBTrace.sysout("Console.isAttached "+handler+" in context "+context.getName()+
                " and win "+Win.safeGetWindowLocation(win), handler);

        return handler;
    },

    attachIfNeeded: function(context, win)
    {
        if (this.isAttached(context, win))
            return true;

        if (FBTrace.DBG_CONSOLE)
            FBTrace.sysout("Console.attachIfNeeded found isAttached false " +
                Win.safeGetWindowLocation(win));

        this.attachConsoleInjector(context, win);
        this.addConsoleListener(context, win);

        Firebug.Console.clearReloadWarning(context);

        var attached =  this.isAttached(context, win);
        if (attached)
            Events.dispatch(Firebug.Console.fbListeners, "onConsoleInjected", [context, win]);

        return attached;
    },

    attachConsoleInjector: function(context, win)
    {
        // Get the 'console' object (this comes from chrome scope).
        var console = Firebug.ConsoleExposed.createFirebugConsole(context, win);

        // Do not expose the chrome object as is but, rather do a wrapper, see below.
        //win.wrappedJSObject.console = console;
        //return;

        // Construct a script string that defines a function. This function returns
        // an object that wraps every 'console' method. This function will be evaluated
        // in a window content sandbox and return a wrapper for the 'console' object.
        // Note that this wrapper appends an additional frame that shouldn't be displayed
        // to the user.
        var expr = "(function(x) { return {\n";
        for (var p in console)
        {
            var func = console[p];
            if (typeof(func) == "function")
            {
                expr += p + ": function() { return Function.apply.call(x." + p +
                    ", x, arguments); },\n";
            }
        }
        expr += "};})";

        // Evaluate the function in the window sandbox/scope and execute. The return value
        // is a wrapper for the 'console' object.
        var sandbox = Cu.Sandbox(win);
        var getConsoleWrapper = Cu.evalInSandbox(expr, sandbox);
        win.wrappedJSObject.console = getConsoleWrapper(console);

        if (FBTrace.DBG_CONSOLE)
            FBTrace.sysout("console.attachConsoleInjector; Firebug console attached to: " +
                context.getName());
    },

    addConsoleListener: function(context, win)
    {
        if (!win)
            win = context.window;

        var handler = this.getConsoleHandler(context, win);
        if (handler)
            return;

        var handler = createConsoleHandler(context, win);

        // Initialize Firebug token
        win.document.setUserData("firebug-Token", handler.token, null);

        this.setConsoleHandler(context, win, handler);

        return true;
    },

    getConsoleHandler: function(context, win)
    {
        if (!win.document)
        {
            if (FBTrace.DBG_ERRORS)
            {
                FBTrace.sysout("console.getConsoleHandler; NO DOCUMENT",
                    {win:win, context:context});
            }
            return null;
        }

        var attachedToken = win.document.getUserData("firebug-Token");
        if (context.activeConsoleHandlers)
        {
            for(var i = 0; i < context.activeConsoleHandlers.length; i++)
            {
                if (context.activeConsoleHandlers[i].token === attachedToken)
                    return context.activeConsoleHandlers[i];
            }
        }
    },

    removeConsoleHandler: function(context, win)
    {
        var handler = this.getConsoleHandler(context, win);
        if (handler)
        {
            handler.detach();
            Arr.remove(context.activeConsoleHandlers, handler);

            if (FBTrace.DBG_CONSOLE)
                FBTrace.sysout("consoleInjector.removeConsoleHandler; token " + handler.token +
                    " and  attached handler("+handler.handler_name+") to _firebugConsole in : "+
                    Win.safeGetWindowLocation(win));
        }
    },

    setConsoleHandler: function(context, win, handler)
    {
        if (!context.activeConsoleHandlers)
            context.activeConsoleHandlers = [];

        context.activeConsoleHandlers.push(handler);

        if (FBTrace.DBG_CONSOLE)
            FBTrace.sysout("consoleInjector addConsoleListener set token "+handler.token+
                " and  attached handler("+handler.handler_name+") to _firebugConsole in : "+
                Win.safeGetWindowLocation(win));
    },

    detachConsole: function(context, win)
    {
        if (!win)
            win = context.window;

        this.removeConsoleHandler(context, win);
    },
}

// ********************************************************************************************* //

var total_handlers = 0;
function createConsoleHandler(context, win)
{
    var handler = {};
    handler.console = Firebug.ConsoleExposed.createFirebugConsole(context, win);

    // xxxHonza: these two functions should be automatically overrided, check this out
    // can be probably removed (evaluated and evaluateError).
    console.evaluated = function(result, context)
    {
        if (FBTrace.DBG_CONSOLE)
        {
            FBTrace.sysout("consoleInjector.FirebugConsoleHandler evalutated default called",
                result);
        }

        Firebug.Console.log(result, context);
    };

    console.evaluateError = function(result, context)
    {
        Firebug.Console.log(result, context, "errorMessage");
    };

    handler.detach = function()
    {
        Events.removeEventListener(win.document, 'firebugAppendConsole', this.boundHandler, true);

        if (FBTrace.DBG_CONSOLE)
            FBTrace.sysout("consoleInjector FirebugConsoleHandler removeEventListener "+
                this.handler_name);
    };

    handler.handler_name = ++total_handlers;
    handler.token = Math.random();

    handler.handleEvent = function(event)
    {
        if (FBTrace.DBG_CONSOLE)
            FBTrace.sysout("FirebugConsoleHandler(" + this.handler_name + ") " +
                win.document.getUserData("firebug-methodName") + ", event", event);

        if (!Firebug.CommandLine.CommandHandler.handle(event, this.console, win))
        {
            if (FBTrace.DBG_CONSOLE)
                FBTrace.sysout("FirebugConsoleHandler", this);

            var methodName = win.document.getUserData("firebug-methodName");
            Firebug.Console.log(Locale.$STRF("console.MethodNotSupported", [methodName]));
        }
    };

    handler.setEvaluatedCallback = function( fnOfResult )
    {
        this.console.evaluated = fnOfResult;
    };

    handler.setEvaluateErrorCallback = function( fnOfResultAndContext )
    {
        this.console.evaluateError = fnOfResultAndContext;
    };

    handler.win = win;
    handler.context = context;

    // When raised on our injected element, callback to Firebug and append to console
    handler.boundHandler = Obj.bind(handler.handleEvent, handler);

    // capturing
    Events.addEventListener(win.document, "firebugAppendConsole", handler.boundHandler, true);

    if (FBTrace.DBG_CONSOLE)
        FBTrace.sysout("consoleInjector FirebugConsoleHandler addEventListener " +
            handler.handler_name);
var towersPiles, towersMovesDone;

function TowersDisk(size) {
  this.size = size;
  this.next = null;
}

function towersPush(pile, disk) {
  var top = towersPiles[pile];
  if ((top != null) && (disk.size >= top.size))
    error("Cannot put a big disk on a smaller disk");
  disk.next = top;
  towersPiles[pile] = disk;
}

function towersPop(pile) {
  var top = towersPiles[pile];
  if (top == null) error("Attempting to remove a disk from an empty pile");
  towersPiles[pile] = top.next;
  top.next = null;
  return top;
}

function towersMoveTop(from, to) {
  towersPush(to, towersPop(from));
  towersMovesDone++;
}

function towersMove(from, to, disks) {
  if (disks == 1) {
    towersMoveTop(from, to);
  } else {
    var other = 3 - from - to;
    towersMove(from, other, disks - 1);
    towersMoveTop(from, to);
    towersMove(other, to, disks - 1);
  }
}

function towersBuild(pile, disks) {
  for (var i = disks - 1; i >= 0; i--) {
    towersPush(pile, new TowersDisk(i));
  }
}

function towers() {
  towersPiles = [ null, null, null ];
  towersBuild(0, 13);
  towersMovesDone = 0;
  towersMove(0, 1, 13);
  if (towersMovesDone != 8191) 
    error("Error in result: " + towersMovesDone + " should be: 8191");
}

towers();
    return handler;
}

// ********************************************************************************************* //
// Registration

return Firebug.Console.injector;

// ********************************************************************************************* //
});
