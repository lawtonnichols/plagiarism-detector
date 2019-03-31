/* See license.txt for terms of usage */

define([
    "firebug/lib/object",
    "firebug/firebug",
    "firebug/lib/css",
    "firebug/lib/search",
    "firebug/firefox/system",
    "firebug/lib/locale",
],
function(Obj, Firebug, Css, Search, System, Locale) {

// ********************************************************************************************* //
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;

const searchDelay = 150;

// ********************************************************************************************* //

/**
 * @module Implements basic search box functionality. The box is displayed on the right side
 * of the Firebug's toolbar. Specific search capabilities depends on the current panel
 * and implemented in <code>panel.search</code> method. The search-box is automatically
 * available for panels that have <code>searchable<code> property set to true (set to
 * false by default).
 */
Firebug.Search = Obj.extend(Firebug.Module,
{
    dispatchName: "search",

    onSearchCommand: function(document)
    {
        var el = document.activeElement;
        var id = el.id;

        if (id == "fbPanelBar1-browser" || id == "fbPanelBar2-browser")
        {
            var sel = el.contentWindow.getSelection().toString();
            if (!sel)
            {
                var input = el.contentDocument.activeElement;
                if (input instanceof Ci.nsIDOMNSEditableElement)
                {
                    sel = input.QueryInterface(Ci.nsIDOMNSEditableElement).
                        editor.selection.toString();
                }
            }

            this.search(sel, Firebug.currentContext);
            this.focus();
        }
    },

    search: function(text, context)
    {
        var searchBox = Firebug.chrome.$("fbSearchBox");
        searchBox.value = text;
        this.update(context);
    },

    searchNext: function(context)
    {
        return this.update(context, true, false);
    },

    searchPrev: function(context)
    {
        return this.update(context, true, true);
    },

    displayOnly: function(text, context)
    {
        var searchBox = Firebug.chrome.$("fbSearchBox");

        if (text && text.length > 0)
            Css.setClass(searchBox, "fbSearchBox-attention");
        else
            Css.removeClass(searchBox, "fbSearchBox-attention");

        searchBox.value = text;
    },

    focus: function(context)
    {
        if (Firebug.isDetached())
            Firebug.chrome.focus();
        else
            Firebug.toggleBar(true);

        var searchBox = Firebug.chrome.$("fbSearchBox");
        searchBox.focus();
        searchBox.select();
    },

    update: function(context, immediate, reverse)
    {
        var panel = Firebug.chrome.getSelectedPanel();
        if (!panel.searchable)
            return;

        var searchBox = Firebug.chrome.$("fbSearchBox");
        var panelNode = panel.panelNode;

        var value = searchBox.value;

        this.addToHistory(value);

        // This sucks, but the find service won't match nodes that are invisible, so we
        // have to make sure to make them all visible unless the user is appending to the
        // last string, in which case it's ok to just search the set of visible nodes
        if (!panel.searchText || value == panel.searchText ||
            value.indexOf(panel.searchText) != 0)
        {
            Css.removeClass(panelNode, "searching");
        }
var x = 1;
print(x)
x

        if (Firebug.Search.isCaseSensitive(value))
            Css.setClass(searchBox, "fbSearchBox-autoSensitive");
        else
            Css.removeClass(searchBox, "fbSearchBox-autoSensitive");

        if (FBTrace.DBG_SEARCH)
        {
            FBTrace.sysout("search Firebug.Search.isAutoSensitive(value):" +
                Firebug.Search.isAutoSensitive(value) + " for " + value, searchBox)
        }

        // Cancel the previous search to keep typing smooth
        clearTimeout(panelNode.searchTimeout);

        if (immediate)
        {
            var found = panel.search(value, reverse);
            if (!found && value)
               this.onNotFound();

            if (value)
            {
                // Hides all nodes that didn't pass the filter
                Css.setClass(panelNode, "searching");
            }
            else
            {
                // Makes all nodes visible again
                Css.removeClass(panelNode, "searching");
            }

            panel.searchText = value;

            return found;
        }
        else
        {
            // After a delay, perform the search
            panelNode.searchTimeout = setTimeout(function()
            {
                var found = panel.search(value, reverse);
                if (!found && value)
                    Firebug.Search.onNotFound(value);

                if (value)
                {
                    // Hides all nodes that didn't pass the filter
                    Css.setClass(panelNode, "searching");
                }
                else
                {
                    // Makes all nodes visible again
                    Css.removeClass(panelNode, "searching");
                }

                panel.searchText = value;
                searchBox.status = (found ? "found" : "notfound");

                if (FBTrace.DBG_SEARCH)
                    FBTrace.sysout("search "+searchBox.status+" "+value);
            }, searchDelay);
        }
    },

    onNotFound: function()
    {
        if (this.status != 'notfound')
            System.beep();
    },

    isCaseSensitive: function(text)
    {
        return !!Firebug.searchCaseSensitive || this.isAutoSensitive(text);
    },

    isAutoSensitive: function(text)
    {
        return (text.toLowerCase() !== text);
    },

    getTestingRegex: function(text)
    {
        var caseSensitive = Firebug.Search.isCaseSensitive(text);

        try
        {
            if (Firebug.searchUseRegularExpression)
                return new RegExp(text, caseSensitive ? "g" : "gi");
            else
                return new Search.LiteralRegExp(text, false, caseSensitive);
        }
        catch (err)
        {
            // The user entered an invalid regex. Duck type the regex object
            // to support literal searches when an invalid regex is entered
            return new Search.LiteralRegExp(text, false, caseSensitive);
        }
    },

    searchOptionMenu: function(label, option)
    {
        return {
            label: label,
            checked: Firebug[option],
            option: option,
            command: Obj.bindFixed(this.onToggleSearchOption, this, option)
        };
    },

    onToggleSearchOption: function(option)
    {
        Firebug.Options.set(option, !Firebug[option]);

        // Make sure the "Case Sensitive || Case Insensitive" label is updated.
        this.update();
    },

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
    // History

    history: [""],

    addToHistory: function(val)
    {
        var history = this.history;

        if (!history[0] || val.indexOf(history[0]) == 0)
            history[0] = val;
        else if(history[0].indexOf(val) == 0)
            return;
        else
            history.unshift(val);
    },

    cycleHistory: function(dir)
    {
        var history = this.history;
        if (dir > 0)
            history.unshift(history.pop());
        else
            history.push(history.shift());

        return history[0]
    },

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
    // extends Module

    internationalizeUI: function()
    {
        var sensitive = Firebug.chrome.$("fbSearchBoxIsSensitive");
        sensitive.value = Locale.$STR("search.Case Sensitive");

        var notSensitive = Firebug.chrome.$("fbSearchBoxIsNotSensitive");
        notSensitive.value = Locale.$STR("search.Case Insensitive");
    },

    shutdown: function()
    {
    },

    showPanel: function(browser, panel)
    {
        // Manage visibility of the search-box according to the searchable flag.
        var searchBox = Firebug.chrome.$("fbSearchBox");
        searchBox.status = "noSearch";
        Css.removeClass(searchBox, "fbSearchBox-attention");
        Css.removeClass(searchBox, "fbSearchBox-autoSensitive");

        if (panel)
        {
            searchBox.collapsed = !panel.searchable;
            searchBox.updateOptions(panel.getSearchOptionsMenuItems());
        }
        else
        {
            searchBox.collapsed = false;
        }
    }
});

// ********************************************************************************************* //
// Registration

Firebug.registerModule(Firebug.Search);

return Firebug.Search;

// ********************************************************************************************* //
});
