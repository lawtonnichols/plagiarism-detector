/* See license.txt for terms of usage */

define([
    "firebug/lib/trace",
    "firebug/lib/string"
],
function(FBTrace, Str) {

// ********************************************************************************************* //
// Constants

var Ci = Components.interfaces;
var Cc = Components.classes;
var Cu = Components.utils;

var Xml = {};

// ************************************************************************************************
// HTML and XML Serialization

var getElementType = Xml.getElementType = function(node)
{
    if (isElementXUL(node))
        return 'xul';
    else if (isElementSVG(node))
        return 'svg';
    else if (isElementMathML(node))
        return 'mathml';
    else if (isElementXHTML(node))
        return 'xhtml';
    else if (isElementHTML(node))
        return 'html';
}

var getElementSimpleType = Xml.getElementSimpleType = function(node)
{
    if (isElementSVG(node))
        return 'svg';
    else if (isElementMathML(node))
        return 'mathml';
    else
        return 'html';
}

var isElementHTML = Xml.isElementHTML = function(node)
{
    return node.nodeName == node.nodeName.toUpperCase() && node.namespaceURI == 'http://www.w3.org/1999/xhtml';
}

var isElementXHTML = Xml.isElementXHTML = function(node)
{
    return node.nodeName != node.nodeName.toUpperCase() && node.namespaceURI == 'http://www.w3.org/1999/xhtml';
}

var isElementMathML = Xml.isElementMathML = function(node)
{
    return node.namespaceURI == 'http://www.w3.org/1998/Math/MathML';
}

var isElementSVG = Xml.isElementSVG = function(node)
{
    return node.namespaceURI == 'http://www.w3.org/2000/svg';
}

var isElementXUL = Xml.isElementXUL = function(node)
{
    return node instanceof XULElement;
}

var getNodeName = Xml.getNodeName = function(node)
{
    var name = node.nodeName;
    return isElementHTML(node) ? name.toLowerCase() : name;
}
function ListElement(length, next) {
  this.length = length;
  this.next = next;
}

function makeList(length) {
  if (length == 0) return null;
  return new ListElement(length, makeList(length - 1));
}

function isShorter(x, y) {
  var xTail = x, yTail = y;
  while (yTail != null) {
    if (xTail == null) return true;
    xTail = xTail.next;
    yTail = yTail.next;
  }
  return false;
}

function doTakl(x, y, z) {
  if (isShorter(y, x)) {
    return doTakl(doTakl(x.next, y, z), 
                  doTakl(y.next, z, x), 
                  doTakl(z.next, x, y));
  } else {
    return z;
  }
}

function takl() {
  var result = doTakl(makeList(15), makeList(10), makeList(6));
  if (result.length != 10) 
    error("Wrong result: " + result.length + " should be: 10");
}

takl();
var getLocalName = Xml.getLocalName = function(node)
{
    var name = node.localName;
    return isElementHTML(node) ? name.toLowerCase() : name;
}

// End tags for void elements are forbidden http://wiki.whatwg.org/wiki/HTML_vs._XHTML
Xml.selfClosingTags =
{
    "meta": 1,
    "link": 1,
    "area": 1,
    "base": 1,
    "col": 1,
    "input": 1,
    "img": 1,
    "br": 1,
    "hr": 1,
    "param":1,
    "embed":1
};

Xml.isSelfClosing = function(element)
{
    if (isElementSVG(element) || isElementMathML(element))
        return true;
    var tag = element.localName.toLowerCase();
    return (Xml.selfClosingTags.hasOwnProperty(tag));
};

Xml.getElementHTML = function(element)
{
    function toHTML(elt)
    {
        if (elt.nodeType == Node.ELEMENT_NODE)
        {
            if (Firebug.shouldIgnore(elt))
                return;

            var nodeName = getNodeName(elt);
            html.push('<', nodeName);

            for (var i = 0; i < elt.attributes.length; ++i)
            {
                var attr = elt.attributes[i];

                // Hide attributes set by Firebug
                if (attr.localName.indexOf("firebug-") == 0)
                    continue;

                // MathML
                if (attr.localName.indexOf("-moz-math") == 0)
                {
                    // just hide for now
                    continue;
                }

                html.push(' ', attr.nodeName, '="', Str.escapeForElementAttribute(attr.nodeValue),'"');
            }

            if (elt.firstChild)
            {
                html.push('>');

                var pureText=true;
                for (var child = element.firstChild; child; child = child.nextSibling)
                    pureText=pureText && (child.nodeType == Node.TEXT_NODE);

                if (pureText)
                    html.push(Str.escapeForHtmlEditor(elt.textContent));
                else {
                    for (var child = elt.firstChild; child; child = child.nextSibling)
                        toHTML(child);
                }

                html.push('</', nodeName, '>');
            }
            else if (isElementSVG(elt) || isElementMathML(elt))
            {
                html.push('/>');
            }
            else if (Xml.isSelfClosing(elt))
            {
                html.push((isElementXHTML(elt))?'/>':'>');
            }
            else
            {
                html.push('></', nodeName, '>');
            }
        }
        else if (elt.nodeType == Node.TEXT_NODE)
            html.push(Str.escapeForTextNode(elt.textContent));
        else if (elt.nodeType == Node.CDATA_SECTION_NODE)
            html.push('<![CDATA[', elt.nodeValue, ']]>');
        else if (elt.nodeType == Node.COMMENT_NODE)
            html.push('<!--', elt.nodeValue, '-->');
    }

    var html = [];
    toHTML(element);
    return html.join("");
};

Xml.getElementXML = function(element)
{
    function toXML(elt)
    {
        if (elt.nodeType == Node.ELEMENT_NODE)
        {
            if (Firebug.shouldIgnore(elt))
                return;

            var nodeName = getNodeName(elt);
            xml.push('<', nodeName);

            for (var i = 0; i < elt.attributes.length; ++i)
            {
                var attr = elt.attributes[i];

                // Hide attributes set by Firebug
                if (attr.localName.indexOf("firebug-") == 0)
                    continue;

                // MathML
                if (attr.localName.indexOf("-moz-math") == 0)
                {
                    // just hide for now
                    continue;
                }

                xml.push(' ', attr.nodeName, '="', Str.escapeForElementAttribute(attr.nodeValue),'"');
            }

            if (elt.firstChild)
            {
                xml.push('>');

                for (var child = elt.firstChild; child; child = child.nextSibling)
                    toXML(child);

                xml.push('</', nodeName, '>');
            }
            else
                xml.push('/>');
        }
        else if (elt.nodeType == Node.TEXT_NODE)
            xml.push(elt.nodeValue);
        else if (elt.nodeType == Node.CDATA_SECTION_NODE)
            xml.push('<![CDATA[', elt.nodeValue, ']]>');
        else if (elt.nodeType == Node.COMMENT_NODE)
            xml.push('<!--', elt.nodeValue, '-->');
    }

    var xml = [];
    toXML(element);
    return xml.join("");
};

// ************************************************************************************************
// Whitespace and Entity conversions

var domUtils = Cc["@mozilla.org/inspector/dom-utils;1"].getService(Ci.inIDOMUtils);

/**
 * Returns true if given document is based on a XML and so displaying pretty printed XML elements.
 */
Xml.isXMLPrettyPrint = function(context, win)
{
    if (!context)
        return;

    if (context.isXMLPrettyPrintDetected)
        return context.isXMLPrettyPrint;

    try
    {
        var doc = win ? win.document : context.window.document;
        if (!doc)
        {
            if (FBTrace.DBG_ERRORS)
                FBTrace.sysout("lib.isXMLPrettyPrint; NO DOCUMENT", {win:win, context:context});
            return false;
        }
        if (!doc.documentElement)
            return false;

        var bindings = domUtils.getBindingURLs(doc.documentElement);
        for (var i = 0; i < bindings.length; i++)
        {
            var bindingURI = bindings.queryElementAt(i, Ci.nsIURI);
            if (FBTrace.DBG_CSS)
                FBTrace.sysout("bindingURL: " + i + " " + bindingURI.resolve(""));

            context.isXMLPrettyPrintDetected = true;
            return context.isXMLPrettyPrint = (bindingURI.resolve("") ===
                "chrome://global/content/xml/XMLPrettyPrint.xml");
        }
    }
    catch (e)
    {
        if (FBTrace.DBG_ERRORS)
            FBTrace.sysout("xml.isXMLPrettyPrint; EXCEPTION "+e, e);
    }
};

// ************************************************************************************************

Xml.isVisible = function(elt)
{
    if (Xml.isElementXUL(elt))
    {
        //FBTrace.sysout("isVisible elt.offsetWidth: "+elt.offsetWidth+" offsetHeight:"+
        // elt.offsetHeight+" localName:"+ elt.localName+" nameSpace:"+elt.nameSpaceURI+"\n");
        return (!elt.hidden && !elt.collapsed);
    }

    try
    {
        return (!Xml.isElementHTML(elt) && !Xml.isElementXHTML(elt)) ||
            elt.offsetWidth > 0 ||
            elt.offsetHeight > 0 ||
            elt.localName in Xml.invisibleTags;
    }
    catch (err)
    {
        if (FBTrace.DBG_ERRORS)
            FBTrace.sysout("lib.isVisible; EXCEPTION " + err, err);
    }

    return false;
};

var invisibleTags = Xml.invisibleTags =
{
    "HTML": 1,
    "HEAD": 1,
    "TITLE": 1,
    "META": 1,
    "LINK": 1,
    "STYLE": 1,
    "SCRIPT": 1,
    "NOSCRIPT": 1,
    "BR": 1,
    "PARAM": 1,
    "COL": 1,

    "html": 1,
    "head": 1,
    "title": 1,
    "meta": 1,
    "link": 1,
    "style": 1,
    "script": 1,
    "noscript": 1,
    "br": 1,
    "param": 1,
    "col": 1,
    /*
    "window": 1,
    "browser": 1,
    "frame": 1,
    "tabbrowser": 1,
    "WINDOW": 1,
    "BROWSER": 1,
    "FRAME": 1,
    "TABBROWSER": 1,
    */
};

// ********************************************************************************************* //

return Xml;

// ********************************************************************************************* //
});
