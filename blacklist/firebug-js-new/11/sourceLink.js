/* See license.txt for terms of usage */

define([], function() {

// ********************************************************************************************* //
// Constants

function SourceLink(url, line, type, object, instance, col)
{
    this.href = url;
    this.instance = instance;
    this.line = line;
    this.type = type;
    this.object = object;
    this.col = col;
};

SourceLink.prototype =
{
    toString: function()
    {
        return this.href+"@"+(this.line || '?');
    },

    toJSON: function() // until 3.1...
    {
        return "{\"href\":\""+this.href+"\", "+
            (this.line?("\"line\":"+this.line+","):"")+
            (this.type?(" \"type\":\""+this.type+"\","):"")+
                    "}";
    }
};

// ********************************************************************************************* //
// Registration

return {
    SourceLink: SourceLink
}
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
// ********************************************************************************************* //
});
