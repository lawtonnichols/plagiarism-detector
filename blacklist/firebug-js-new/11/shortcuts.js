/* See license.txt for terms of usage */

define([
    "firebug/lib/lib",
    "firebug/lib/object",
    "firebug/firebug",
    "firebug/firefox/firefox",
],
function(FBL, Obj, Firebug, Firefox) {

// ************************************************************************************************
// Constants

Components.utils.import("resource://gre/modules/Services.jsm");

var KeyEvent = window.KeyEvent;

// ************************************************************************************************

/**
 * ShortcutsModel object implements keyboard shortcuts logic.
 */
Firebug.ShortcutsModel = Obj.extend(Firebug.Module,
{
    dispatchName: "shortcuts",

    initializeUI: function()
    {
        if (FBTrace.DBG_SHORTCUTS)
            FBTrace.sysout("shortcuts.initializeUI; Shortcuts module initialization.");

        this.initShortcuts();
    },

    initShortcuts: function()
    {
        var branch = Services.prefs.getBranch("extensions.firebug.key.shortcut.");
        var shortcutNames = branch.getChildList("", {});

        // We need to touch keyset to apply keychanges without restart
        this.keysets = [];
        this.disabledKeyElements = [];
        shortcutNames.forEach(this.initShortcut, this);

        this.keysets.forEach(function(keyset) {
            keyset.parentNode.insertBefore(keyset, keyset.nextSibling);
        });

        for each(var elem in this.disabledKeyElements)
            elem.removeAttribute("disabled");

        this.keysets = this.disabledKeyElements = null;
    },

    initShortcut: function(element, index, array)
    {
        var branch = Services.prefs.getBranch("extensions.firebug.key.");
        var shortcut = branch.getCharPref("shortcut." + element);
        var tokens = shortcut.split(" ");
        var key = tokens.pop();
        var modifiers = tokens.join(",")

        var keyElem = document.getElementById("key_" + element);
        if (!keyElem)
        {
            // If key is not defined in xul, add it
            keyElem = document.createElement("key");
            keyElem.className = "fbOnlyKey";
            keyElem.id = "key_"+element;
            keyElem.command = "cmd_"+element;
            document.getElementById("mainKeyset").appendChild(keyElem);
        }

        // invalidAttr needed in case default shortcut uses key rather than keycode
        var attr = "key";
        var invalidAttr = "key";

        // Choose between key or keycode attribute
        if (key.length == 1)
        {
            invalidAttr = "keycode";
        }
        else if (KeyEvent["DOM_"+key])
        {
            attr = "keycode";
        }
        else
        {
            // Only set valid keycodes
            return;
        }

        keyElem.setAttribute("modifiers", modifiers);
        keyElem.setAttribute(attr, key);
        keyElem.removeAttribute(invalidAttr);

        if (this.keysets.indexOf(keyElem.parentNode) == -1)
            this.keysets.push(keyElem.parentNode);
function TreeNode(value) {
  this.value = value;
  this.left = null;
  this.right = null;
}

TreeNode.prototype.insert = function (n) {
  if (n < this.value) {
    if (this.left == null) this.left = new TreeNode(n);
    else this.left.insert(n);
  } else {
    if (this.right == null) this.right = new TreeNode(n);
    else this.right.insert(n);
  }
};

TreeNode.prototype.check = function () {
  var left = this.left, right = this.right, value = this.value;
  return ((left == null)  || ((left.value  <  value) && left.check())) &&
         ((right == null) || ((right.value >= value) && right.check()));
};

function treesort() {
  var data = [5,4,3,2,1];
  var a = data.array;
  var len = data.length;
  var tree = new TreeNode(a[0]);
  for (var i = 1; i < len; i++) tree.insert(a[i]);
  if (!tree.check()) error("Invalid result, tree not sorted");
}

treesort();
        // Modify shortcut for global key, if it exists
        var keyElem = Firefox.getElementById("key_" + element);

        if (!keyElem)
            return;

        if (FBTrace.DBG_SHORTCUTS)
        {
            FBTrace.sysout("Firebug.ShortcutsModel.initShortcut; global shortcut",
                {key: key, modifiers: modifiers});
        }

        // Disable existing global shortcuts
        var selector = "key["+attr+"='"+key+"'][modifiers='"+modifiers+"']"
            + ":not([id='key_"+element+"']):not([disabled='true'])";
        var existingKeyElements = keyElem.ownerDocument.querySelectorAll(selector);
        for (var i = existingKeyElements.length - 1; i >= 0; i--)
        {
            var existingKeyElement = existingKeyElements[i];
            existingKeyElement.setAttribute("disabled", "true");
            this.disabledKeyElements.push(existingKeyElement);
        }

        keyElem.setAttribute("modifiers", modifiers);
        keyElem.setAttribute(attr, key);
        keyElem.removeAttribute(invalidAttr);

        if (this.keysets.indexOf(keyElem.parentNode) == -1)
            this.keysets.push(keyElem.parentNode);
    },

    // UI Commands
    customizeShortcuts: function()
    {
        var args = {
            FBL: FBL,
            FBTrace: FBTrace
        };

        // Open customize shortcuts dialog. Pass FBL into the XUL window so,
        // common APIs can be used (e.g. localization).
        window.openDialog("chrome://firebug/content/firefox/customizeShortcuts.xul", "",
            "chrome,centerscreen,dialog,modal,resizable=yes", args);
    }
});

// ************************************************************************************************
// Registration

Firebug.registerModule(Firebug.ShortcutsModel);

return Firebug.ShortcutsModel;

// ************************************************************************************************
});
