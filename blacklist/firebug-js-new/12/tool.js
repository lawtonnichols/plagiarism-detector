/* See license.txt for terms of usage */

define([
],
function() {

// ********************************************************************************************* //

var FirebugTool = function(name)
{
    this.toolName = name;
    this.active = false;
}

FirebugTool.prototype =
{
    getName: function()
    {
        return this.toolName;
    },
    getActive: function()
    {
        return this.active;
    },
    setActive: function(active)
    {
        this.active = !!active;
    }
}
// ********************************************************************************************* //
function tak(x, y, z) {
 if (y >= x) return z;    
 return tak(tak(x-1, y, z), tak(y-1, z, x), tak(z-1, x, y));   
}

tak(18,12,6);
return FirebugTool;

// ********************************************************************************************* //
});
