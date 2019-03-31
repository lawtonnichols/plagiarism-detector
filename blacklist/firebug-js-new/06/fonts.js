/* See license.txt for terms of usage */

define([
    "firebug/lib/trace",
    "firebug/lib/dom",
    "firebug/lib/url"
],
function(FBTrace, Dom, Url) {

// ********************************************************************************************* //
// Constants

var Ci = Components.interfaces;
var Cc = Components.classes;
var Cu = Components.utils;

var Fonts = {};

// ********************************************************************************************* //
// Fonts

/**
 * Retrieves all fonts used inside a node
 * @node: Node to return the fonts for
 * @return Array of fonts
 */
Fonts.getFonts = function(node)
{
    if (!Dom.domUtils)
        return [];

    var range = node.ownerDocument.createRange();
    range.selectNode(node);
    var fontFaces = Dom.domUtils.getUsedFontFaces(range);
    var fonts = [];
    for (var i=0; i<fontFaces.length; i++)
        fonts.push(fontFaces.item(i));

    return fonts;
}

/**
 * Retrieves the information about a font
 * @context: Context of the font
 * @win: Window the font is used in
 * @identifier: Either a URL in case of a Fonts font or the font name
 * @return Object with information about the font
 */
Fonts.getFontInfo = function(context, win, identifier)
{
    if (!context)
        context = Firebug.currentContext;
    var doc = win ? win.document : context.window.document;
    if (!doc)
    {
        if (FBTrace.DBG_ERRORS)
            FBTrace.sysout("lib.getFontInfo; NO DOCUMENT", {win:win, context:context});
        return false;
    }

    var fonts = Fonts.getFonts(doc.documentElement);
    var url = Url.splitURLBase(identifier);

    if (FBTrace.DBG_FONTS)
        FBTrace.sysout("Fonts.getFontInfo;", {fonts:fonts, url:url});
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
    for (var i=0; i<fonts.length; i++)
    {
        if ((fonts[i].rule && url && identifier == fonts[i].URI) ||
            identifier == fonts[i].CSSFamilyName || identifier == fonts[i].name)
        {
            return fonts[i];
        }
    }

    return false;
}

// ********************************************************************************************* //

return Fonts;

// ********************************************************************************************* //
});