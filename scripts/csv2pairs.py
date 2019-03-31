#!/usr/bin/env python3
from sys import argv
import csv
import sqlite3

def lang(fn):
    return fn.split(':')[0].split('.')[-1]

csvfile = open(argv[1])

rows = [row for row in csv.reader(csvfile)]

csvfile.close()

n = len(rows)

seen = {}

tupls = ()
for i in range(1, n):
    for j in range(1, n):
        a = rows[0][i]
        b = rows[j][0]
        score = float(rows[i][j])
        if a != b and (a,b) not in seen:
            print('%s,%s,%f' % (a, b, score))
            seen[a,b] = True
            seen[b,a] = True
