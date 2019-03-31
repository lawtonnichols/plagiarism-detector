function doBubblesort(a, len) {
  for (var i = len - 2; i >= 0; i--) {
    for (var j = 0; j <= i; j++) {
      var c = a[j], n = a[j + 1];
      if (c > n) {
        a[j] = n;
        a[j + 1] = c;
      }
    }
  }
}

doBubblesort([5,4,3,2,1], 5)
