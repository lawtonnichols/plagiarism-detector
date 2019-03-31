#!/usr/bin/env python3
import sys
import csv

with open(sys.argv[1]) as f:
    pairs = [row for row in csv.reader(f)]

p = {}

for a, alang, b, blang, score in pairs:
    if not ((a, b) in p or (b, a) in p):
        p[(a, b)] = (alang, blang, score)

for a, b in p:
    alang, blang, score = p[(a, b)]
    print("%s, %s, %s, %s, %s" % (a, alang, b, blang, score))
