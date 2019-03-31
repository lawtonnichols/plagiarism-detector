function doSum(start, end) {
  var sum = 0;
  for (var i = start; i <= end; i++) sum += i;
  return sum;
}

function sum() {
  var result = doSum(1, 10000);
  if (result != 50005000) error("Wrong result: " + result + " should be: 50005000");
}

sum();