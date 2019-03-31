#!/usr/bin/env python3
from sys import argv
import csv

# Finds which pairs one tool said were clones but the other said weren't clones

import argparse
parser = argparse.ArgumentParser()
parser.add_argument("pairs1")
parser.add_argument("cutoff1")
parser.add_argument("pairs2")
parser.add_argument("cutoff2")
parser.add_argument("--invert1", action="store_true")
parser.add_argument("--invert2", action="store_true")

args = parser.parse_args()

pairs1 = args.pairs1
pairs2 = args.pairs2
cutoff1 = float(args.cutoff1)
cutoff2 = float(args.cutoff2)

assert(0.0 <= cutoff1 <= 1.0 and 0.0 <= cutoff2 <= 1.0)

results1 = {}
results2 = {}

def isDiscrepancy(score1, score2):
    below1Above2 = score1 <= cutoff1 and score2 >= cutoff2
    above1Below2 = score1 >= cutoff1 and score2 <= cutoff2
    return below1Above2 or above1Below2

with open(pairs1) as csvfile:
    for row in csv.reader(csvfile):
        (a, b, score) = (row[0].strip(), row[1].strip(), float(row[2].strip()))
        if args.invert1:
            score = 1.0 - score
        results1[a, b] = score

with open(pairs2) as csvfile:
    for row in csv.reader(csvfile):
        (a, b, score) = (row[0].strip(), row[1].strip(), float(row[2].strip()))
        if args.invert2:
            score = 1.0 - score
        results2[a, b] = score

if set(results1.keys()) != set(results2.keys()):
    import sys
    print("keys not the same", file=sys.stderr)
    in1butnot2 = set(results1.keys()) - set(results2.keys())
    in2butnot1 = set(results2.keys()) - set(results1.keys())
    print("in 1 but not 2", file=sys.stderr)
    print("--------------", file=sys.stderr)
    for x in in1butnot2:
        print(x[0] + "," + x[1], file=sys.stderr)
    print("in 2 but not 1", file=sys.stderr)
    print("--------------", file=sys.stderr)
    for x in in2butnot1:
        print(x[0] + "," + x[1], file=sys.stderr)
    assert(False)

for k, v in results1.items():
    score1 = v
    score2 = results2[k]
    if isDiscrepancy(score1, score2):
        print("%s,%s,%s,%s" % (k[0], k[1], score1, score2))
