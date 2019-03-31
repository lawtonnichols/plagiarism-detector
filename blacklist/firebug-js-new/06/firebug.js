// For a detailed description of all preferences see http://getfirebug.com/wiki/index.php/Firebug_Preferences

// Global
pref("extensions.firebug.architecture", "inProcess");
pref("extensions.firebug.defaultModuleList", "");

pref("javascript.options.strict.debug", false);
pref("extensions.firebug.defaultPanelName", "html");
pref("extensions.firebug.throttleMessages", true);
pref("extensions.firebug.textSize", 0);
pref("extensions.firebug.showInfoTips", true);
pref("extensions.firebug.textWrapWidth", 100);
pref("extensions.firebug.framePosition", "bottom");
pref("extensions.firebug.previousPlacement", 0);
pref("extensions.firebug.showErrorCount", true);
pref("extensions.firebug.viewPanelOrient", false);
pref("extensions.firebug.allPagesActivation", "none");
pref("extensions.firebug.hiddenPanels", "");
pref("extensions.firebug.panelTabMinWidth", 50);
pref("extensions.firebug.sourceLinkLabelWidth", 17);
pref("extensions.firebug.currentVersion", "");
pref("extensions.firebug.showFirstRunPage", true);
pref("extensions.firebug.useDefaultLocale", false);
pref("extensions.firebug.activateSameOrigin", true);
pref("extensions.firebug.toolbarCustomizationDone", false);
pref("extensions.firebug.addonBarOpened", false);
pref("extensions.firebug.showBreakNotification", true);
pref("extensions.firebug.showStatusIcon", false);
pref("extensions.firebug.stringCropLength", 50);

// Command line
pref("extensions.firebug.commandEditor", false);
pref("extensions.firebug.alwaysShowCommandLine", false);

// Search
pref("extensions.firebug.searchCaseSensitive", false);
pref("extensions.firebug.searchGlobal", true);
pref("extensions.firebug.searchUseRegularExpression", false);

pref("extensions.firebug.netSearchHeaders", false);
pref("extensions.firebug.netSearchParameters", false);
pref("extensions.firebug.netSearchResponseBody", false);

// Console
pref("extensions.firebug.showJSErrors", true);
pref("extensions.firebug.showJSWarnings", false);
pref("extensions.firebug.showCSSErrors", false);
pref("extensions.firebug.showXMLErrors", false);
pref("extensions.firebug.showChromeErrors", false);
pref("extensions.firebug.showChromeMessages", false);
pref("extensions.firebug.showExternalErrors", false);
pref("extensions.firebug.showNetworkErrors", true);
pref("extensions.firebug.showXMLHttpRequests", true);
pref("extensions.firebug.showStackTrace", false);
pref("extensions.firebug.console.logLimit", 500);
pref("extensions.firebug.console.enableSites", false);
pref("extensions.firebug.tabularLogMaxHeight", 200);
pref("extensions.firebug.consoleFilterTypes", "all");
pref("extensions.firebug.memoryProfilerEnable", false);

// HTML
pref("extensions.firebug.showCommentNodes", false);
pref("extensions.firebug.showTextNodesWithWhitespace", false);
pref("extensions.firebug.showTextNodesWithEntities", true);
pref("extensions.firebug.showFullTextNodes", true);
pref("extensions.firebug.highlightMutations", true);
pref("extensions.firebug.expandMutations", false);
pref("extensions.firebug.scrollToMutations", false);
pref("extensions.firebug.shadeBoxModel", true);
pref("extensions.firebug.showQuickInfoBox", false);
pref("extensions.firebug.displayedAttributeValueLimit", 1024);
pref("extensions.firebug.multiHighlightLimit", 250);

// CSS
pref("extensions.firebug.onlyShowAppliedStyles", false);
pref("extensions.firebug.showUserAgentCSS", false);
pref("extensions.firebug.expandShorthandProps", false);
pref("extensions.firebug.showMozillaSpecificStyles", false);
pref("extensions.firebug.computedStylesDisplay", "grouped");
pref("extensions.firebug.cssEditMode", "Source");

// Script
pref("extensions.firebug.breakOnErrors", false);
pref("extensions.firebug.showAllSourceFiles", false);
pref("extensions.firebug.trackThrowCatch", false);
pref("extensions.firebug.script.enableSites", false);
pref("extensions.firebug.scriptsFilter", "all");
pref("extensions.firebug.replaceTabs", 4);
pref("extensions.firebug.filterSystemURLs", true);
pref("extensions.firebug.maxScriptLineLength", 10000);

// Stack
pref("extensions.firebug.omitObjectPathStack", false);

// DOM
pref("extensions.firebug.showUserProps", true);
pref("extensions.firebug.showUserFuncs", true);
pref("extensions.firebug.showDOMProps", true);
pref("extensions.firebug.showDOMFuncs", false);
pref("extensions.firebug.showDOMConstants", false);
pref("extensions.firebug.showInlineEventHandlers", false);
pref("extensions.firebug.ObjectShortIteratorMax", 3);
pref("extensions.firebug.showEnumerableProperties", true);
pref("extensions.firebug.showOwnProperties", false);

// Layout
pref("extensions.firebug.showRulers", true);

// Net
pref("extensions.firebug.netFilterCategory", "all");
pref("extensions.firebug.net.logLimit", 500);
pref("extensions.firebug.net.enableSites", false);
pref("extensions.firebug.netDisplayedResponseLimit", 102400);
pref("extensions.firebug.netDisplayedPostBodyLimit", 10240);
pref("extensions.firebug.net.hiddenColumns", "netProtocolCol netLocalAddressCol");
pref("extensions.firebug.netPhaseInterval", 1000);
pref("extensions.firebug.sizePrecision", 1);
pref("extensions.firebug.netParamNameLimit", 25);
pref("extensions.firebug.netShowPaintEvents", false);
pref("extensions.firebug.netShowBFCacheResponses", true);
pref("extensions.firebug.netHtmlPreviewHeight", 100);
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
// JSON Preview
pref("extensions.firebug.sortJsonPreview", false);

// Cache
pref("extensions.firebug.cache.mimeTypes", "");
pref("extensions.firebug.cache.responseLimit", 5242880);

// External Editors
pref("extensions.firebug.externalEditors", "");

// Keyboard
pref("extensions.firebug.key.shortcut.reenterCommand", "control shift e");
pref("extensions.firebug.key.shortcut.toggleInspecting", "accel shift c");
pref("extensions.firebug.key.shortcut.toggleQuickInfoBox", "accel shift i");
pref("extensions.firebug.key.shortcut.toggleProfiling", "accel shift p");
pref("extensions.firebug.key.shortcut.focusCommandLine", "accel shift l");
pref("extensions.firebug.key.shortcut.focusFirebugSearch", "accel f");
pref("extensions.firebug.key.shortcut.focusWatchEditor", "accel shift n");
pref("extensions.firebug.key.shortcut.focusLocation", "control shift VK_SPACE");
pref("extensions.firebug.key.shortcut.nextObject", "control .");
pref("extensions.firebug.key.shortcut.previousObject", "control ,");
pref("extensions.firebug.key.shortcut.toggleFirebug", "VK_F12");
pref("extensions.firebug.key.shortcut.detachFirebug", "accel VK_F12");
pref("extensions.firebug.key.shortcut.leftFirebugTab", "accel shift VK_PAGE_UP");
pref("extensions.firebug.key.shortcut.rightFirebugTab", "accel shift VK_PAGE_DOWN");
pref("extensions.firebug.key.shortcut.previousFirebugTab", "control `");
pref("extensions.firebug.key.shortcut.clearConsole", "accel shift r");
pref("extensions.firebug.key.shortcut.navBack", "accel shift VK_LEFT");
pref("extensions.firebug.key.shortcut.navForward", "accel shift VK_RIGHT");
pref("extensions.firebug.key.shortcut.increaseTextSize", "accel +");
pref("extensions.firebug.key.shortcut.decreaseTextSize", "accel -");
pref("extensions.firebug.key.shortcut.normalTextSize", "accel 0");

// Accessibility
pref("extensions.firebug.a11y.enable", false);
