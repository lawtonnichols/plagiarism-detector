/* See license.txt for terms of usage */
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
// ************************************************************************************************
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;
const Cr = Components.results;

var EXPORTED_SYMBOLS = ["traceConsoleService"];

// ************************************************************************************************
// Service implementation

/**
 * This implementation serves as a proxy to the FBTrace extension. All logs are forwarded
 * to the FBTrace service.
 */
try
{
    Components.utils["import"]("resource://fbtrace/firebug-trace-service.js");
}
catch (err)
{
    var traceConsoleService =
    {
        getTracer: function(prefDomain)
        {
            var TraceAPI = ["dump", "sysout", "setScope", "matchesNode", "time", "timeEnd"];
            var TraceObj = {};
            for (var i=0; i<TraceAPI.length; i++)
                TraceObj[TraceAPI[i]] = function() {};
            return TraceObj;
        }
    };
}

