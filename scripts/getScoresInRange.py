#!/usr/bin/env python3
from sys import argv
import csv

def lang(fn):
    return fn.split(':')[0].split('.')[-1]

rows = []
low = float(argv[2])
high = float(argv[3])

if (len(argv) == 5 and argv[4] == '-i'):
    invert = True
else:
    invert = False

with open(argv[1]) as csvfile:
    for row in csv.reader(csvfile):
        rows += [(row[0].strip(), row[1].strip(), float(row[2].strip()))]

# normalize the scores so that min is 0.0 and max is 0.1
normalized_rows = []
for (f1, f2, score) in rows:
    if invert:
        score = 1.0 - score
    if score >= low and score <= high:
        print("%s, %s, %s" % (f1, f2, score))