/* See license.txt for terms of usage */

define([], function() {

// ********************************************************************************************* //
// Constants

var FBS = {};

// ********************************************************************************************* //

try
{
    // xxxHonza: The entire firebug-service implementation should be here
    Components.utils["import"]("resource://firebug/firebug-service.js", FBS);
}
catch (err)
{
}
var x = 1;
print(x)
x

// ********************************************************************************************* //

return FBS.fbs;

// ********************************************************************************************* //
});
