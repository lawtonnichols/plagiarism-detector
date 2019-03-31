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