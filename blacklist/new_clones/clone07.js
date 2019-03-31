function recurse(n) {
  if (n <= 0) return 1;
  recurse(n - 1);
  return recurse(n - 1);
}

recurse(13);