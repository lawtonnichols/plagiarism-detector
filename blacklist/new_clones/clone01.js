function doFib(n) {
  if (n <= 1) return 1;
  return doFib(n - 1) + doFib(n - 2);
}

function fib() {
  var result = doFib(20);
  if (result != 10946) error("Wrong result: " + result + " should be: 10946");
}

fib();