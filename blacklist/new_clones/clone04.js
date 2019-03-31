function doSieve(flags, size) {
  var primeCount = 0, i;
  for (i = 1; i < size; i++) flags[i] = true;
  for (i = 2; i < size; i++) {
    if (flags[i]) { // amoeller: this line deliberately uses 'undefined' as 'false'
      primeCount++;
      for (var k = i + 1; k <= size; k+=i) flags[k - 1] = false;
    }
  } 
  return primeCount;
}

function sieve() {
  var flags = new Array(1001);
  var result = doSieve(flags, 1000);
  if (result != 168) 
    error("Wrong result: " + result + " should be: 168");
}

sieve();