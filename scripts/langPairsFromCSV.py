#!/usr/bin/env python3

import sys
import csv

if len(sys.argv) != 5:
    print("usage: %s db-file langa langb output-csv" % sys.argv[1], file=sys.stderr)
    exit(1)

_, pairfile, langa, langb, outfname = tuple(sys.argv)

pairfile = open(pairfile)
csvfile = open(outfname, 'w')

langs = {
    'java': 'java',
    'go': 'go',
    'js': 'js',
    'cpp': ['cpp', 'h', 'hpp', 'c', 'cc', 'hh']
}

invLangs = {}
for l in langs:
    ext = langs[l]
    if type(ext) == str:
        invLangs[ext] = l
    else:
        for e in ext:
            invLangs[e] = l

def getLang(f):
    return invLangs[f.split(':')[0].split('.')[-1].lower()]

# csvfile.write("a, b, score\n")
for row in csv.reader(pairfile):
    a = row[0]
    b = row[1]
    if getLang(a) != langa or getLang(b) != langb:
        continue
    csvfile.write(",".join([a, b] + row[2:]) + "\n")

csvfile.close()
