/* See license.txt for terms of usage */

define([
    "firebug/lib/object",
    "firebug/firebug",
    "firebug/firefox/firefox",
    "firebug/lib/locale",
    "firebug/lib/events",
    "firebug/lib/dom",
    "firebug/lib/options",
],
function(Obj, Firebug, Firefox, Locale, Events, Dom, Options) {

// ********************************************************************************************* //
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;

var appInfo = Cc["@mozilla.org/xre/app-info;1"].getService(Ci.nsIXULAppInfo);
var versionChecker = Cc["@mozilla.org/xpcom/version-comparator;1"].getService(Ci.nsIVersionComparator);

// ********************************************************************************************* //
// Module Implementation

/**
 * @module StartButton module represents the UI entry point to Firebug. This "start buttton"
 * formerly known as "the status bar icon" is automatically appended into Firefox toolbar
 * (since Firefox 4).
 *
 * Start button is associated with a menu (fbStatusContextMenu) that contains basic actions
 * such as panel activation and also indicates whether Firebug is activated/deactivated for
 * the current page (by changing its color).
 */
Firebug.StartButton = Obj.extend(Firebug.Module,
/** @lends Firebug.StartButton */
{
    dispatchName: "startButton",

    initializeUI: function()
    {
        Firebug.Module.initializeUI.apply(this, arguments);

        // Associate a popup-menu with the start button (the same as it's
        // used for the obsolete status bar icon.
        var startButton = Firefox.getElementById("firebug-button");
        if (startButton)
        {
            var popup = Firefox.getElementById("fbStatusContextMenu");
            startButton.appendChild(popup.cloneNode(true));

            // In case of Firefox 4+ the button is a bit different.
            if (appInfo.name == "Firefox" && versionChecker.compare(appInfo.version, "4.0*") >= 0)
                startButton.setAttribute("firefox", "4");
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
            // Put Firebug version in tooltip.
            var version = Firebug.getVersion();
            if (version)
            {
                var fbStatusBar = Firefox.getElementById("fbStatusBar");
                if (fbStatusBar)
                    fbStatusBar.setAttribute("tooltiptext", "Firebug " + version);
            }
        }

        this.updateStatusIcon();

        if (FBTrace.DBG_INITIALIZE)
            FBTrace.sysout("Startbutton initializeUI "+startButton);
    },

    addOnLoadListener: function(win)
    {
        this.browserWin = win;

        this.onLoadBinding = Obj.bind(this.onLoad, this);
        Events.addEventListener(this.browserWin, "load", this.onLoadBinding, false);
    },

    onLoad: function()
    {
        Events.removeEventListener(this.browserWin, "load", this.onLoadBinding, false);

        try
        {
            this.appendToToolbar();
        }
        catch (e)
        {
            if (FBTrace.DBG_ERRORS)
                FBTrace.sysout("startButton; onLoad.appendToToolbar EXCEPTION " + e, e);
        }
    },

    shutdown: function()
    {
    },

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //

    /**
     * Appends Firebug start button into Firefox toolbar automatically after installation.
     * The button is appended only once so, if the user removes it, it isn't appended again.
     */
    appendToToolbar: function()
    {
        if (Options.get("toolbarCustomizationDone"))
            return;

        Options.set("toolbarCustomizationDone", true);

        // Get the current navigation bar button set (a string of button IDs) and append
        // ID of the Firebug start button into it.
        var startButtonId = "firebug-button";
        var navBarId = "nav-bar";

        // xxxHonza: do not use Firefox.getElementById, it depends on Firebug.chrome
        // that doesn't have to be set (loaded) yet. 
        //var navBar = Firefox.getElementById(navBarId);
        var navBar = top.document.getElementById(navBarId);
        var currentSet = navBar.currentSet;

        if (FBTrace.DBG_INITIALIZE)
            FBTrace.sysout("Startbutton; curSet (before modification): " + currentSet);

        // Append only if the button is not already there.
        var curSet = currentSet.split(",");
        if (curSet.indexOf(startButtonId) == -1)
        {
            navBar.insertItem(startButtonId);
            navBar.setAttribute("currentset", navBar.currentSet);
            navBar.ownerDocument.persist("nav-bar", "currentset");

            // Check whether insertItem really works
            var curSet = navBar.currentSet.split(",");
            if (curSet.indexOf(startButtonId) == -1)
            {
                FBTrace.sysout("Startbutton; navBar.insertItem doesn't work", curSet);
            }

            if (FBTrace.DBG_INITIALIZE)
                FBTrace.sysout("Startbutton; curSet (after modification): " + navBar.currentSet);

            try
            {
                // The current global scope is not browser.xul.
                top.BrowserToolboxCustomizeDone(true);
            }
            catch (e)
            {
                if (FBTrace.DBG_ERRORS)
                    FBTrace.sysout("startButton; appendToToolbar EXCEPTION " + e, e);
            }
        }

        // Don't forget to show the navigation bar - just in case it's hidden.
        Dom.collapse(navBar, false);
        document.persist(navBarId, "collapsed");
    },

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
    // Support for the status bar

    /**
     * The status bar icon is hidden by default since Firebug 1.8 (Firefox 4).
     * The toolbar icon (start button) is the preferred Firebug entry point.
     */
    updateStatusIcon: function()
    {
        var show = Options.get("showStatusIcon");
        var statusBar = Firefox.getElementById("fbStatusBar");
        if (statusBar)
            Dom.collapse(statusBar, !show);
    },

    updateOption: function(name, value)
    {
        if (name === "showStatusIcon")
            this.updateStatusIcon();
    },

    onClickStatusText: function(context, event)
    {
        if (event.button != 0)
            return;

        if (!context || !context.errorCount)
            return;

        var panel = Firebug.chrome.getSelectedPanel();
        if (panel && panel.name != "console")
        {
            Firebug.chrome.selectPanel("console");
            Events.cancelEvent(event);
        }
    },

    onClickStatusIcon: function(context, event)
    {
        if (event.button != 0)
            return;
        else if (Events.isControl(event))
            Firebug.toggleDetachBar(true);
        else if (context && context.errorCount)
            Firebug.toggleBar(undefined, "console");
        else
            Firebug.toggleBar();
    },

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
    // Error count

    showCount: function(errorCount)
    {
        var statusBar = Firefox.getElementById("fbStatusBar");
        var statusText = Firefox.getElementById("fbStatusText");

        if (!statusBar)
            return;

        var firebugButton = Firefox.getElementById("firebug-button");
        if (errorCount && Firebug.showErrorCount)
        {
            statusBar.setAttribute("showErrors", "true")
            statusText.setAttribute("value", Locale.$STRP("plural.Error_Count2", [errorCount]));

            if (firebugButton)
            {
                firebugButton.setAttribute("showErrors", "true");
                firebugButton.setAttribute("errorCount", errorCount);
            }
        }
        else
        {
            statusBar.removeAttribute("showErrors");
            statusText.setAttribute("value", "");

            if (firebugButton)
            {
                firebugButton.removeAttribute("showErrors");

                // Use '0' so, the horizontal space for the number is still allocated.
                // The button will cause re-layout if there is more than 9 errors.
                firebugButton.setAttribute("errorCount", "0");
            }
        }
    },

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
    // Tooltip

    resetTooltip: function()
    {
        var tooltip = "Firebug " + Firebug.getVersion();
        tooltip += "\n" + this.getEnablementStatus();

        if (Firebug.getSuspended())
        {
            tooltip += "\n" + this.getSuspended();
        }
        else
        {
            tooltip += "\n" + Locale.$STRP("plural.Total_Firebugs2",
                [Firebug.TabWatcher.contexts.length]);
        }

        if (Firebug.allPagesActivation == "on")
        {
            var label = Locale.$STR("enablement.on");
            tooltip += "\n"+label+" "+Locale.$STR("enablement.for all pages");
        }
        // else allPagesActivation == "none" we don't show it.

        tooltip += "\n" + Locale.$STR(Firebug.getPlacement());

        var firebugStatus = Firefox.getElementById("firebugStatus");
        if (!firebugStatus)
            return;

        firebugStatus.setAttribute("tooltiptext", tooltip);

        // The start button is colorful only if there is a context
        var active = Firebug.currentContext ? "true" : "false";
        firebugStatus.setAttribute("firebugActive", active);

        if (FBTrace.DBG_TOOLTIP)
            FBTrace.sysout("resetTooltip called: firebug active: " + active);
    },

    getEnablementStatus: function()
    {
        var strOn = Locale.$STR("enablement.on");
        var strOff = Locale.$STR("enablement.off");

        var status = "";
        var firebugStatus = Firefox.getElementById("firebugStatus");

        if (!firebugStatus)
            return;

        if (firebugStatus.getAttribute("console") == "on")
            status += "Console: " + strOn + ",";
        else
            status += "Console: " + strOff + ",";

        if (firebugStatus.getAttribute("script") == "on")
            status += " Script: " + strOn;
        else
            status += " Script: " + strOff + "";

        if (firebugStatus.getAttribute("net") == "on")
            status += " Net: " + strOn + ",";
        else
            status += " Net: " + strOff + ",";

        return status;
    },

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
    // Activation

    getSuspended: function()
    {
        var suspendMarker = Firefox.getElementById("firebugStatus");
        if (suspendMarker && suspendMarker.hasAttribute("suspended"))
            return suspendMarker.getAttribute("suspended");

        return null;
    },

    setSuspended: function(value)
    {
        var suspendMarker = Firefox.getElementById("firebugStatus");

        if (FBTrace.DBG_ACTIVATION)
            FBTrace.sysout("Firebug.setSuspended to " + value + ". Browser: " +
                Firebug.chrome.window.document.title);

        if (value == "suspended")
            suspendMarker.setAttribute("suspended", value);
        else
            suspendMarker.removeAttribute("suspended");

        this.resetTooltip();
    }
});

// ********************************************************************************************* //
// Registration

// Firebug start button must be appended when the top window (browser) is loaded.
Firebug.StartButton.addOnLoadListener(top);

Firebug.registerModule(Firebug.StartButton);

// ********************************************************************************************* //

return Firebug.StartButton;
});
