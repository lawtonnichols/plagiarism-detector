#!/usr/bin/env python3
from sys import argv
import csv

import argparse
parser = argparse.ArgumentParser()
parser.add_argument("pairs1")
parser.add_argument("pairs2")
parser.add_argument("--weight1", default="0.5")
parser.add_argument("--invert1", action="store_true")
parser.add_argument("--invert2", action="store_true")
parser.add_argument("--geometric", action="store_true")
parser.add_argument("--harmonic", action="store_true")
# weight2 = 1.0 - weight1
args = parser.parse_args()

pairs1 = args.pairs1
pairs2 = args.pairs2
weight1 = float(args.weight1)
weight2 = 1.0 - weight1

assert(weight1 >= 0.0 and weight2 <= 1.0)

def lang(fn):
    return fn.split(':')[0].split('.')[-1]

results1 = {}
results2 = {}

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

# confirm that results1 and results2 have the same keys

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

# combine the results

combined_results = {}

def weighted_average(x, x_weight, y, y_weight):
    if args.geometric:
        if x_weight == 0.5 and y_weight == 0.5:
            return (x * y) ** 0.5
        x_weight = 1.0 / (1.0 - x_weight)
        y_weight = 1.0 / (1.0 - y_weight)
        res = (x ** x_weight * y ** y_weight) ** (1.0 / (x_weight + y_weight))
        return res
    elif args.harmonic:
        if x == 0.0 or y == 0.0:
            return 0.0
        else:
            return 2.0 / (1.0/x + 1.0/y)
    else:
        return x * x_weight + y * y_weight

for k, v in results1.items():
    score1 = v
    score2 = results2[k]
    combined_score = weighted_average(score1, weight1, score2, weight2)
    combined_results[k] = combined_score

for k, v in combined_results.items():
    (f1, f2) = k
    print("%s, %s, %s" % (f1, f2, v))
