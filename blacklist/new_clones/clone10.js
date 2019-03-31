function doQuicksort(a, low, high) {
  var pivot = a[(low + high) >> 1];
  var i = low, j = high;
  while (i <= j) {
    while (a[i] < pivot) i++;
    while (pivot < a[j]) j--;
    if (i <= j) {
      var tmp = a[i];
      a[i] = a[j];
      a[j] = tmp;
      i++;
      j--;
    }
  }

  if (low < j) doQuicksort(a, low, j);
  if (i < high) doQuicksort(a, i, high);
}

doQuicksort([5,4,3,2,1], 0, 4);