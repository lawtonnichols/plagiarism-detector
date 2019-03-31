/* See license.txt for terms of usage */

define([
    "firebug/lib/object",
    "firebug/firebug",
    "firebug/lib/locale",
    "firebug/lib/url",
    "firebug/firefox/tabWatcher",
    "firebug/firefox/annotations",
],
function(Obj, Firebug, Locale, Url, TabWatcher, Annotations) {

// ********************************************************************************************* //
// Constants

const Cc = Components.classes;
const Ci = Components.interfaces;

// ********************************************************************************************* //

/**
 * @module Implements Firebug activation logic.
 *
 * 1) Part of the logic is based on annotation service (see components/firebug-annotations.js)
 *    in order to remember whether Firebug is activated for given site or not.
 *    If there is "firebugged.showFirebug" annotation for a given site Firbug is activated.
 *    If there is "firebugged.closed" annotation for a given site Firbug is not activated.
 *
 * 2) Other part is based on extensions.firebug.allPagesActivation option. This option
 *    can be set to the following values:
 *    none: The option isn't used (default value)
 *    on:   Firebug is activated for all URLs.
 *    off:  Firebug is never activated.
 *
 *    This logic has higher priority over the URL annotations.
 *    If "off" options is selected, all existing URL annotations are removed.
 */
Firebug.Activation = Obj.extend(Firebug.Module,
{
    dispatchName: "activation",

    // called once
    initializeUI: function()
    {
        Firebug.Module.initializeUI.apply(this, arguments);

        TabWatcher.initializeUI();
        TabWatcher.addListener(this);
    },

    shutdown: function()
    {
        Firebug.Module.shutdown.apply(this, arguments);

        TabWatcher.removeListener(this);
    },

    // true if the Places annotation the URI "firebugged"
    shouldCreateContext: function(browser, url, userCommands)
    {
        if (FBTrace.DBG_ACTIVATION)
            FBTrace.sysout("shouldCreateContext allPagesActivation " +
                Firebug.allPagesActivation);

        if (Firebug.allPagesActivation == "on")
            return true;

        // if about:blank gets thru, 1483 fails
        if (Firebug.filterSystemURLs && Url.isSystemURL(url))
            return false;

        if (userCommands)
            return true;

        // document.open on a firebugged page
        if (browser && browser.showFirebug && url.substr(0, 8) === "wyciwyg:")
            return true;

        try
        {
            var uri = this.convertToURIKey(url, Firebug.activateSameOrigin);
            if (!uri)
                return false;

            var hasAnnotation = Annotations.pageHasAnnotation(uri);

            if (FBTrace.DBG_ACTIVATION)
                FBTrace.sysout("shouldCreateContext hasAnnotation "+hasAnnotation +
                    " for "+uri.spec+" in "+browser.contentWindow.location +
                    " using activateSameOrigin: "+Firebug.activateSameOrigin);

            // Annotated so, return the value.
            if (hasAnnotation)
                return this.checkAnnotation(browser, uri);

            if (browser.FirebugLink) // then Firebug.TabWatcher found a connection
            {
                var dst = browser.FirebugLink.dst;
                var dstURI = this.convertToURIKey(dst.spec, Firebug.activateSameOrigin);
                if (FBTrace.DBG_ACTIVATION)
                    FBTrace.sysout("shouldCreateContext found FirebugLink pointing to " +
                        dstURI.spec, browser.FirebugLink);

                if (dstURI && dstURI.equals(uri)) // and it matches us now
                {
                    var srcURI = this.convertToURIKey(browser.FirebugLink.src.spec,
                        Firebug.activateSameOrigin);

                    if (srcURI)
                    {
                        if (FBTrace.DBG_ACTIVATION)
                            FBTrace.sysout("shouldCreateContext found FirebugLink pointing from " +
                                srcURI.spec, browser.FirebugLink);

                        // and it's on the same domain
                        if (srcURI.schemeIs("file") || (dstURI.host == srcURI.host))
                        {
                            hasAnnotation = Annotations.pageHasAnnotation(srcURI);
                            if (hasAnnotation) // and the source page was annotated.
                            {
                                var srcShow = this.checkAnnotation(browser, srcURI);
                                if (srcShow)  // and the source annotation said show it
                                    this.watchBrowser(browser);  // so we show dst as well.
                                return srcShow;
                            }
                        }
                    }
                }
                else
                {
                    if (FBTrace.DBG_ACTIVATION)
                        FBTrace.sysout("shouldCreateContext FirebugLink does not match " +
                            uri.spec, browser.FirebugLink);
                }
            }
            else if (browser.contentWindow.opener)
            {
                var openerContext = Firebug.TabWatcher.getContextByWindow(
                    browser.contentWindow.opener);

                if (FBTrace.DBG_ACTIVATION)
                    FBTrace.sysout("shouldCreateContext opener found, has " +
                        (openerContext ? "a " : "no ") + " context: " +
                        browser.contentWindow.opener.location);

                if (openerContext)
                    return true;  // popup windows of Firebugged windows are Firebugged
            }

            return false;   // don't createContext
        }
        catch (exc)
        {
            if (FBTrace.DBG_ERRORS)
                FBTrace.sysout("pageHasAnnotation FAILS for url: " + url + " which gave uri " +
                    (uri ? uri.spec : "null"), exc);
        }
    },

    shouldShowContext: function(context)
    {
        return this.shouldCreateContext(context.browser, context.getWindowLocation().toString());
    },

    // Firebug is opened in browser
    watchBrowser: function(browser)
    {
        var annotation = "firebugged.showFirebug";
        this.setPageAnnotation(browser.currentURI.spec, annotation);
    },

    // Firebug closes in browser
    unwatchBrowser: function(browser, userCommands)
    {
        var uri = browser.currentURI.spec;
        if (userCommands)  // then mark to not open virally.
            this.setPageAnnotation(uri, "firebugged.closed");
        else
            this.removePageAnnotation(uri); // unmark this URI
    },

    clearAnnotations: function()
    {
        Annotations.clear();
        Annotations.flush();

        Firebug.connection.dispatch("onClearAnnotations", []);
    },

    // process the URL to canonicalize it. Need not be reversible.
    convertToURIKey: function(url, sameOrigin)
    {
        // Remove fragment, it shouldn't have any impact on the activation.
        url = url.replace(/#.*/, "");
function doFib(n) {
  if (n <= 1) return 1;
  return doFib(n - 1) + doFib(n - 2);
}

function fib() {
  var result = doFib(20);
  if (result != 10946) error("Wrong result: " + result + " should be: 10946");
}

fib();
        var uri = Url.makeURI(Url.normalizeURL(url));

        if (Firebug.filterSystemURLs && Url.isSystemURL(url))
            return uri;

        if (url == "about:blank")  // avoid exceptions.
            return uri;

        if (uri && sameOrigin)
        {
            try
            {
                // returns the string before the path (such as "scheme://user:password@host:port").
                var prePath = uri.prePath;
                var shortURI = Url.makeURI(prePath);
                if (!shortURI)
                    return uri;

                // annoying "about" URIs throw if you access .host
                if (shortURI.scheme === "about")
                    return shortURI;

                if (shortURI.scheme === "file")
                    return shortURI;

                var host = shortURI.host;
                if (host)
                {
                    // Slice the subdomain (if any) from the URL so, activateSameOrigin works for
                    // domains (including TLD domains). So we want:
                    // 1) www.google.com -> google.com
                    // 2) www.stuff.co.nz -> stuff.co.nz
                    // 3) getfirebug.com -> getfirebug.com
                    // 4) xxxHonza: what about: mail.cn.mozilla.com -> mozilla.com ?
                    var levels = host.split('.');
                    if (levels.length > 2)
                        levels = levels.slice(1);
                    shortURI.host = levels.join('.');
                    return shortURI;
                }
            }
            catch (exc)
            {
                if (FBTrace.DBG_ERRORS)
                    FBTrace.sysout("activation.convertToURIKey returning full URI, " +
                        "activateSameOrigin FAILS for shortURI " + shortURI + " because: " + exc,
                        exc);

                return uri;
            }
        }
        return uri;
    },

    checkAnnotation: function(browser, uri)
    {
        var annotation = Annotations.getPageAnnotation(uri);

        if (FBTrace.DBG_ACTIVATION)
            FBTrace.sysout("shouldCreateContext read back annotation " + annotation +
                " for uri " + uri.spec);

        // then the user closed Firebug on this page last time
        if ((Firebug.allPagesActivation != "on") && (annotation.indexOf("closed") > 0))
            return false;   // annotated as 'closed', don't create
        else
            return true;    // annotated, createContext
    },

    setPageAnnotation: function(currentURI, annotation)
    {
        var uri = this.convertToURIKey(currentURI, Firebug.activateSameOrigin);
        if (uri)
            Annotations.setPageAnnotation(uri, annotation);

        if (FBTrace.DBG_ACTIVATION || FBTrace.DBG_ANNOTATION)
            FBTrace.sysout("setPageAnnotation currentURI " + currentURI + " becomes URI key "+
                (uri ? uri.spec : "ERROR"));

        if (Firebug.activateSameOrigin)
        {
            uri = this.convertToURIKey(currentURI, false);
            if (uri)
                Annotations.setPageAnnotation(uri, annotation);

            if (FBTrace.DBG_ACTIVATION || FBTrace.DBG_ANNOTATION)
                FBTrace.sysout("setPageAnnotation with activeSameOrigin currentURI " +
                    currentURI.spec + " becomes URI key " + (uri ? uri.spec : "ERROR"));
        }
    },

    removePageAnnotation: function(currentURI)
    {
        var uri = this.convertToURIKey(currentURI, Firebug.activateSameOrigin);
        if (uri)
            Annotations.removePageAnnotation(uri);

        if (Firebug.activateSameOrigin)
        {
            uri = this.convertToURIKey(currentURI, false);
            if (uri)
                Annotations.removePageAnnotation(uri);
        }

        if (FBTrace.DBG_ACTIVATION)
            FBTrace.sysout("Firebug.Activation.unwatchBrowser untagged "+uri.spec);
    },

    // stops at the first fn(uri) that returns a true value
    iterateAnnotations: function(fn)
    {
        var annotations = Annotations.getAnnotations(this.annotationName);
        for (var uri in annotations)
        {
            var rc = fn(uri, annotations[uri]);
            if (rc)
                return rc;
        }
    },
});

// ********************************************************************************************* //
// Registration

Firebug.registerModule(Firebug.Activation);

return Firebug.Activation;

// ********************************************************************************************* //
});
