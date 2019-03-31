#!/usr/bin/env python3

import csv
from glob import glob
import sys
import os
import numpy as np
from collections import defaultdict

def chop(x):
    return x.split("/")[-1].split(".")[0]

def loadGroundTruth(qrelFile):
    f = open(qrelFile)
    rows = [row.split(" ") for row in f.readlines()]
    f.close()
    isClone = {}
    for a, b in rows:
        isClone[(a.strip(), b.strip())] = True
        isClone[(b.strip(), a.strip())] = True
    isClone = defaultdict(lambda: False, isClone)
    return isClone


def getFileName(longstring):
    res = os.path.basename(longstring.split(":")[0])
    return res

def getData(csvfile, isClone, isDistance=False):
    f = open(csvfile)
    rows = [row for row in csv.reader(f)]
    # rows = rows[1:]
    f.close()
    if isDistance:
        fScore = lambda x: 1 - float(x)
    else:
        fScore = float
    data = [(fScore(row[2].strip()), isClone[(getFileName(row[0].strip()), getFileName(row[1].strip()))]) for row in rows]
    data.sort()
    clones = [int(r[1]) for r in data]
    scores = [r[0] for r in data]
    return os.path.basename(csvfile), scores, clones


groundTruth = loadGroundTruth(sys.argv[2])
results = [getData(sys.argv[1], groundTruth)]
# results = [tuple([f[:-len(".csv")]] + list(getData(f, groundTruth))) for f in glob(os.path.join(sys.argv[1], "*.csv"))]
cutoffs = np.array([i / 100.0 for i in range(10, 91)])


precision = np.zeros(len(cutoffs))
recall = np.zeros(len(cutoffs))
success = np.zeros(len(cutoffs))

print("config,cutoff,tp,fp,tn,fn,precision,recall,success")
for config, scores, clones in results:
    tp = np.zeros(len(cutoffs))
    fp = np.zeros(len(cutoffs))
    tn = np.zeros(len(cutoffs))
    fn = np.zeros(len(cutoffs))    
    for i in range(len(cutoffs)):
        cutoff = cutoffs[i]
        for j in range(len(scores)):
            if cutoff <= scores[j]:  # positive
                tp[i] += clones[j]
                fp[i] += 1 - clones[j]
            else:
                tn[i] += 1 - clones[j]
                fn[i] += clones[j]
        precision[i] = tp[i] / (tp[i] + fp[i])
        recall[i] = tp[i] / (tp[i] + fn[i])
        # print(cutoff, precision[i], recall[i], 2 * precision[i] * recall[i] / (precision[i] + recall[i]))
        # success = np.nan_to_num(precision * recall)
        success = 2 * precision * recall / (precision + recall)  # compute f-measure as success rate, harmonic mean of precision and recall
        success = np.nan_to_num(success)
    best = np.argmax(success)
    print("%s,%f,%f,%f,%f,%f,%f,%f,%f" % (config,cutoffs[best],tp[best],fp[best],tn[best],fn[best],precision[best],recall[best],success[best]))

