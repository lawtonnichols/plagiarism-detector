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

Debug.ERROR = function(exc)
{
    if (typeof(FBTrace) !== undefined)
    {
        if (exc.stack)
            exc.stack = exc.stack.split('\n');
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
