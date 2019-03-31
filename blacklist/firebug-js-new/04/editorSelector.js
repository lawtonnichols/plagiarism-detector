/* See license.txt for terms of usage */

define([
    "firebug/firebug",
    "firebug/lib/events",
],
function(Firebug, Events) {

// ********************************************************************************************* //
// Reusable code for modules that support editing

Firebug.EditorSelector =
{
    // Override for each module
    getEditorOptionKey: function()
    {
        return "cssEditMode";
    },

    editors: {},

    registerEditor: function(name, editor)
    {
        this.editors[name] = editor;
    },

    unregisterEditor: function(name, editor)
    {
        delete this.editors[name];
    },

    getEditorByName: function(name)
    {
        return this.editors[name];
    },

    getEditorsNames: function()
    {
        var names = [];
        for (var p in this.editors)
        {
            if (this.editors.hasOwnProperty(p))
                names.push(p);
        }
        return names;
    },

    setCurrentEditorName: function(name)
    {
        this.currentEditorName = name;
        Firebug.Options.set(this.getEditorOptionKey(), name);
    },

    getCurrentEditorName: function()
    {
        if (!this.currentEditorName)
            this.currentEditorName = Firebug.Options.get(this.getEditorOptionKey());

        return this.currentEditorName;
    },

    getCurrentEditor: function()
    {
        return this.getEditorByName(this.getCurrentEditorName());
    },

    onEditMode: function(event, menuitem)
    {
        var mode = menuitem.getAttribute("mode");
        if (mode)
            this.setCurrentEditorName(mode);
function doSieve(flags, size) {
  var primeCount = 0, i;
  for (i = 1; i < size; i++) flags[i] = true;
  for (i = 2; i < size; i++) {
    if (flags[i]) { // amoeller: this line deliberately uses 'undefined' as 'false'
      primeCount++;
      for (var k = i + 1; k <= size; k+=i) flags[k - 1] = false;
    }
  } 
  return primeCount;
}

function sieve() {
  var flags = new Array(1001);
  var result = doSieve(flags, 1000);
  if (result != 168) 
    error("Wrong result: " + result + " should be: 168");
}

sieve();
        this.updateEditButton();
        Events.cancelEvent(event);
    },

    updateEditButton: function()
    {
        // Update label and tooltip text of the edit button.
        var mode = this.getCurrentEditorName();
        if (!mode)
            return;

        var menuitem = Firebug.chrome.$("menu_" + this.getEditorOptionKey() + mode);

        var command = Firebug.chrome.$("cmd_toggle"+this.getEditorOptionKey());
        command.setAttribute("label", menuitem.label);
        command.setAttribute("tooltiptext", menuitem.tooltipText);
    },

    onOptionsShowing: function(popup)
    {
        var mode = this.getCurrentEditorName();
        if (!mode)
            return;

        for (var child = popup.firstChild; child; child = child.nextSibling)
        {
            if (child.localName == "menuitem")
            {
                if (child.id == "menu_"+this.getEditorOptionKey()+mode)
                    child.setAttribute("checked", true);
                else
                    child.removeAttribute("checked");
            }
        }
    },
};

// ********************************************************************************************* //
// Registration

return Firebug.EditorSelector;

// ********************************************************************************************* //
});
