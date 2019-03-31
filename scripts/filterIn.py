#!/usr/bin/env python3
from sys import argv
import csv
import sqlite3

def lang(fn):
    return fn.split(':')[0].split('.')[-1]

filterfile = open(argv[2])
filtr = set([(row[0].strip(), row[1].strip()) for row in csv.reader(filterfile)])
filterfile.close()

with open(argv[1]) as csvfile:
    for row in csv.reader(csvfile):
        r = (row[0].strip(), row[1].strip(), row[2].strip())
        if tuple(r[0:2]) in filtr:
            print('%s,%s,%s' % r)
