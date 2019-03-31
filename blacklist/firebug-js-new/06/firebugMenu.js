/* See license.txt for terms of usage */

define([
    "firebug/lib/object",
    "firebug/firebug",
    "firebug/firefox/firefox",
    "firebug/lib/css",
    "firebug/lib/array",
],
function(Obj, Firebug, Firefox, Css, Arr) {

// ********************************************************************************************* //
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;

// ********************************************************************************************* //
// Module Implementation

Firebug.FirebugMenu = Obj.extend(Firebug.Module,
{
    dispatchName: "firebugMenu",

    initializeUI: function()
    {
        Firebug.Module.initializeUI.apply(this, arguments);

        // Put Firebug version on all "About" menu items. This men item appears in
        // Firefox Tools menu (Firefox UI) as well as Firebug Icon menu (Firebug UI)
        this.updateAboutMenu(document);
        this.updateAboutMenu(top.document);

        // Initialize Firebug Tools, Web Developer and Firebug Icon menus.
        var firebugMenuPopup = Firebug.chrome.$("fbFirebugMenuPopup");

        // If 'Web Developer' menu is available (introduced in Firefox 6)
        // Remove the old entry in Tools menu.
        if (Firefox.getElementById("menu_webDeveloper_firebug"))
        {
            var menuFirebug = Firefox.getElementById("menu_firebug");
            if (menuFirebug)
                menuFirebug.parentNode.removeChild(menuFirebug);
        }

        // Initialize content of Firebug menu at various places.
        this.initializeMenu(Firefox.getElementById("menu_webDeveloper_firebug"), firebugMenuPopup);
        this.initializeMenu(Firefox.getElementById("menu_firebug"), firebugMenuPopup);
        this.initializeMenu(Firefox.getElementById("appmenu_firebug"), firebugMenuPopup);
        this.initializeMenu(Firebug.chrome.$("fbFirebugMenu"), firebugMenuPopup);
    },

    /**
     * Append version info to all "About" menu items.
     * @param {Object} doc The scope document where to look for XUL menu elements.
     */
    updateAboutMenu: function(doc)
    {
        var version = Firebug.getVersion();
        if (version)
        {
            var nodes = doc.querySelectorAll(".firebugAbout");
            nodes = Arr.cloneArray(nodes);
            for (var i=0; i<nodes.length; i++)
            {
                var node = nodes[i];
                var aboutLabel = node.getAttribute("label");
                node.setAttribute("label", aboutLabel + " " + version);
                Css.removeClass(node, "firebugAbout");
            }
        }
    },

    /**
     * Insert Firebug menu into specified location in the UI. Firebug menu appears
     * at several location depending on Firefox version and/or application (e.g. SeaMonkey)
     */
    initializeMenu: function(parentMenu, popupMenu)
    {
        if (!parentMenu)
            return;

        if (parentMenu.getAttribute("initialized"))
            return;
function tryQueens(i, a, b, c, x) {
  var j = 0, q = false;
  while ((!q) && (j != 8)) {
    j++;
    q = false;
    if (b[j] && a[i + j] && c[i - j + 7]) {
      x[i] = j;
      b[j] = false;
      a[i + j] = false;
      c[i - j + 7] = false;
      if (i < 8) {
        q = tryQueens(i + 1, a, b, c, x);
        if (!q) {
          b[j] = true;
          a[i + j] = true;
          c[i - j + 7] = true;
        }
      } else {
        q = true;
      }
    }
  }
  return q;
}

function queens() {
  var a = new Array(9);
  var b = new Array(17);
  var c = new Array(15);
  var x = new Array(9);
  for (var i = -7; i <= 16; i++) {
    if ((i >= 1) && (i <= 8)) a[i] = true;
    if (i >= 2) b[i] = true;
    if (i <= 7) c[i + 7] = true;
  }

  if (!tryQueens(1, b, a, c, x)) 
    error("Error in queens");
}

queens();
        parentMenu.appendChild(popupMenu.cloneNode(true));
        parentMenu.setAttribute("initialized", "true");
    },
});

// ********************************************************************************************* //
// Registration

Firebug.registerModule(Firebug.FirebugMenu);

// ********************************************************************************************* //

return Firebug.FirebugMenu;
});
