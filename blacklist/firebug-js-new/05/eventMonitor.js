/* See license.txt for terms of usage */

define([
    "firebug/lib/object",
    "firebug/firebug",
    "firebug/lib/trace",
    "firebug/lib/events",
],
function(Obj, Firebug, FBTrace, Events) {

// ********************************************************************************************* //
// EventMonitor Module
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
Firebug.EventMonitor = Obj.extend(Firebug.Module,
{
    dispatchName: "eventMonitor",

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
    // Module

    destroyContext: function(context, persistedState)
    {
        // Clean up all existing monitors.
        var eventsMonitored = context.eventsMonitored;
        if (eventsMonitored)
        {
            for (var i=0; i<eventsMonitored.length; ++i)
            {
                var m = eventsMonitored[i];

                if (!m.type)
                    Events.detachAllListeners(m.object, context.onMonitorEvent, context);
                else
                    Events.removeEventListener(m.object, m.type, context.onMonitorEvent, false);
            }
        }
    },

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
    // Event Monitor

    toggleMonitorEvents: function(object, type, state, context)
    {
        if (state)
            this.unmonitorEvents(object, type, context);
        else
            this.monitorEvents(object, type, context);
    },

    monitorEvents: function(object, type, context)
    {
        if (!this.areEventsMonitored(object, type, context) && object &&
            object.addEventListener)
        {
            if (!context.onMonitorEvent)
                context.onMonitorEvent = function(event) { Firebug.Console.log(event, context); };

            if (!context.eventsMonitored)
                context.eventsMonitored = [];

            context.eventsMonitored.push({object: object, type: type});

            if (!type)
                Events.attachAllListeners(object, context.onMonitorEvent, context);
            else
                Events.addEventListener(object, type, context.onMonitorEvent, false);
        }
    },

    unmonitorEvents: function(object, type, context)
    {
        var eventsMonitored = context.eventsMonitored;

        for (var i=0; i<eventsMonitored.length; ++i)
        {
            if (eventsMonitored[i].object == object && eventsMonitored[i].type == type)
            {
                eventsMonitored.splice(i, 1);

                if (!type)
                    Events.detachAllListeners(object, context.onMonitorEvent, context);
                else
                    Events.removeEventListener(object, type, context.onMonitorEvent, false);
                break;
            }
        }
    },

    areEventsMonitored: function(object, type, context)
    {
        var eventsMonitored = context.eventsMonitored;
        if (eventsMonitored)
        {
            for (var i = 0; i < eventsMonitored.length; ++i)
            {
                if (eventsMonitored[i].object == object && eventsMonitored[i].type == type)
                    return true;
            }
        }

        return false;
    }
});

// ********************************************************************************************* //
// Registration & Export

Firebug.registerModule(Firebug.EventMonitor);

return Firebug.EventMonitor;

// ********************************************************************************************* //
});
