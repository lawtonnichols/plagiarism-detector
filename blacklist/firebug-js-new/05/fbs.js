/* See license.txt for terms of usage */

define([], function() {

// ********************************************************************************************* //
// Constants

var FBS = {};

// ********************************************************************************************* //
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
try
{
    // xxxHonza: The entire firebug-service implementation should be here
    Components.utils["import"]("resource://firebug/firebug-service.js", FBS);
}
catch (err)
{
}

// ********************************************************************************************* //

return FBS.fbs;

// ********************************************************************************************* //
});
