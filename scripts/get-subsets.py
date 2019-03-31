#!/usr/bin/env python3

import csv
import sys
import os
import glob
import numpy as np

def chop(x):
    return x.split("/")[-1].split(".")[0]

noHeader = len(sys.argv) > 3 and '--no-header' in [a.lower() for a in sys.argv]

def bscore(s):
    s = s.strip()
    if s == 'True':
        return True
    elif s == 'False':
        return False
    else:
        raise Exception("%s is not a Boolean!" % s)

cutoff = None
for c in sys.argv[1:]:
    if c.startswith('--cutoff='):
        cutoff = float(c[len('--cutoff='):])

isDistance = "--distance" in sys.argv

scoreFn = bscore
if cutoff is not None:
    if isDistance:
        scoreFn = lambda s: float(s) <= cutoff
    else:
        scoreFn = lambda s: float(s) >= cutoff
        
subsets = {'tp': False, 'fp': False, 'tn': False, 'fn': False}

for i in sys.argv[2:]:
    i = i.strip().lower()
    if i in subsets:
        subsets[i] = True
    subsets['tp'] |= i in ['pos', 'positive'] or i in ['true']
    subsets['fp'] |= i in ['pos', 'positive'] or i in ['false']
    subsets['tn'] |= i in ['neg', 'negative'] or i in ['true']
    subsets['fn'] |= i in ['neg', 'negative'] or i in ['true']

def loadGroundTruth(csvfile):
    f = open(csvfile)
    rows = [row for row in csv.reader(f)]
    if not noHeader:
        rows = rows[1:]
    f.close()
    isClone = {}
    for a, b, clonep in rows:
        isClone[(a.strip(), b.strip())] = bscore(clonep)
    return isClone


def getData(csvfile, isClone, isDistance=False):
    f = open(csvfile)
    rows = [row for row in csv.reader(f)]
    rows = rows[1:]
    f.close()
    data = [(scoreFn(row[2].strip()), isClone[(row[0].strip(), row[1].strip())], (row[0].strip(), row[1].strip())) for row in rows if (row[0].strip(), row[1].strip()) in isClone]
    data.sort()
    clones = [int(r[1]) for r in data]
    scores = [int(r[0]) for r in data]
    pairs = [r[2] for r in data]
    return scores, clones, pairs

groundTruth = loadGroundTruth(sys.argv[2])
justFile = os.path.isfile(sys.argv[1])
cutoffs = [sys.argv[1]] if justFile else glob.glob(os.path.join(sys.argv[1], "*.csv"))
results = [tuple([x[:-len(".csv")]] + list(getData(x, groundTruth))) for x in cutoffs]

def printr(p, r):
    a, b = p
    print(','.join([a, b, r]))

for config, scores, clones, pairs in results:
    # print('config: ', config)
    for j in range(len(scores)):
        if scores[j]:  # positive
            if subsets['tp'] and clones[j]:
                printr(pairs[j], 'tp')
            if subsets['fp'] and not clones[j]:
                printr(pairs[j], 'fp')
        else:
            if subsets['tn'] and not clones[j]:
                printr(pairs[j], 'tn')
            if subsets['fn'] and clones[j]:
                printr(pairs[j], 'fn')
