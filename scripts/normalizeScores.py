#!/usr/bin/env python3
from sys import argv
import csv

def lang(fn):
    return fn.split(':')[0].split('.')[-1]

rows = []
min = max = None

with open(argv[1]) as csvfile:
    for row in csv.reader(csvfile):
        if min is None or float(row[2]) < min:
            min = float(row[2])
        if max is None or float(row[2]) > max:
            max = float(row[2])
        rows += [(row[0].strip(), row[1].strip(), float(row[2].strip()))]


# normalize the scores so that min is 0.0 and max is 0.1
normalized_rows = []
for (f1, f2, score) in rows:
    normalized_score = (score - min) / (max - min)
    print("%s, %s, %s" % (f1, f2, normalized_score))