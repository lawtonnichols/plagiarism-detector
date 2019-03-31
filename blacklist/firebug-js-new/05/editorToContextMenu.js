/* See license.txt for terms of usage */

// ********************************************************************************************* //

// Runs during overlay processing
function OpenEditorShowHide(event)
{
    var item = document.getElementById("menu_firebugOpenWithEditor");

    var popupNode = document.popupNode;
    var hidden = (popupNode instanceof HTMLInputElement
        || popupNode instanceof HTMLIFrameElement
        || popupNode instanceof HTMLTextAreaElement)
    if(hidden)
    {
        item.hidden = true;
        return;
    }
    var editor=Firebug.ExternalEditors.getDefaultEditor();
    if(!editor)
    {
        item.hidden = true;
        return;
    }
    item.hidden = false;
    item.setAttribute('image', editor.image);
    item.setAttribute('label', editor.label);
    item.value = editor.id;
}

function addOpenEditorShowHide(event)
{
    window.removeEventListener("load", addOpenEditorShowHide, false);

    var contextMenu = document.getElementById("contentAreaContextMenu");
    if (contextMenu)
    {
        addContextToForms();
        contextMenu.addEventListener("popupshowing", OpenEditorShowHide, false);
    }
};

function addContextToForms(contextMenu)
{
    if (typeof(nsContextMenu) == "undefined")
        return;

    // https://bugzilla.mozilla.org/show_bug.cgi?id=433168
    var setTargetOriginal = nsContextMenu.prototype.setTarget;
    nsContextMenu.prototype.setTarget = function(aNode, aRangeParent, aRangeOffset)
    {
        setTargetOriginal.apply(this, arguments);
        if (this.isTargetAFormControl(aNode))
            this.shouldDisplay = true;
    };
}

// ********************************************************************************************* //

window.addEventListener("load", addOpenEditorShowHide, false);
var permuteCount;
 
function swap(n, k, array) {
  var tmp = array[n];
  array[n] = array[k];
  array[k] = tmp;
}

function doPermute(n, array) {
  permuteCount++;
  if (n != 1) {
    doPermute(n - 1, array);
    for (var k = n - 1; k >= 1; k--) {
      swap(n, k, array);
      doPermute(n - 1, array);
      swap(n, k, array);
    }
  }
}

function permute() {
  var array = new Array(8);
  for (var i = 1; i <= 7; i++) array[i] = i - 1;
  permuteCount = 0;
  doPermute(7, array);
  if (permuteCount != 8660) error("Wrong result: " + permuteCount + " should be: 8660");
}

permute();
// ********************************************************************************************* //
