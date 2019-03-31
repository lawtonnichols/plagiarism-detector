function loop() {
  var sum = 0;
  for (var i = 0; i < 200; i++) {
    for (var j = 0; j < 100; j++) {
      sum++;
    }
  }
  if (sum != 20000) error("Wrong result: " + sum + " should be: 20000");
}

loop();