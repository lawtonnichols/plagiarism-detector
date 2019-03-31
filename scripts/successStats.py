#!/usr/bin/env python3

import csv
import sys
import os
import numpy as np

def chop(x):
    return x.split("/")[-1].split(".")[0]

noHeader = len(sys.argv) > 3 and '--no-header' in [a.lower() for a in sys.argv]

def loadGroundTruth(csvfile):
    f = open(csvfile)
    rows = [row for row in csv.reader(f)]
    if not noHeader:
        rows = rows[1:]
    f.close()
    isClone = {}
    for a, b, clonep in rows:
        if clonep.strip() == "True":
            clonep = True
        elif clonep.strip() == "False":
            clonep = False
        else:
            raise Exception("%s is not a Boolean!" % clonep)
        isClone[(a.strip(), b.strip())] = clonep
    return isClone


def getData(csvfile, isClone, isDistance=False):
    f = open(csvfile)
    rows = [row for row in csv.reader(f)]
    rows = rows[1:]
    f.close()
    if isDistance:
        fScore = lambda x: 1 - float(x)
    else:
        fScore = float
    data = [(fScore(row[2].strip()), isClone[(row[0].strip(), row[1].strip())]) for row in rows]
    data.sort()
    clones = [int(r[1]) for r in data]
    scores = [r[0] for r in data]
    return scores, clones


isDistance = False
for a in sys.argv:
    if a.strip() == "--distance":
        isDistance = True
        break

print(os.path.join(sys.argv[1], "*.csv"))
groundTruth = loadGroundTruth(sys.argv[2])
results = [tuple([sys.argv[1][:-len(".csv")]] + list(getData(sys.argv[1], groundTruth)))]
cutoffs = np.linspace(.1, .9, 9)


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
            if isDistance:
                if cutoff >= scores[j]:  # positive
                    tp[i] += clones[j]
                    fp[i] += 1 - clones[j]
                else:
                    tn[i] += 1 - clones[j]
                    fn[i] += clones[j]
            else:
                if cutoff <= scores[j]:  # positive
                    tp[i] += clones[j]
                    fp[i] += 1 - clones[j]
                else:
                    tn[i] += 1 - clones[j]
                    fn[i] += clones[j]
        precision[i] = tp[i] / (tp[i] + fp[i])
        recall[i] = tp[i] / (tp[i] + fn[i])
        # success = np.nan_to_num(precision * recall)
        success = 2 * precision * recall / (precision + recall)  # compute f-measure as success rate, harmonic mean of precision and recall
        success = np.nan_to_num(success)
    for c,tp_,tn_,fp_,fn_,p,r,s in zip(cutoffs,tp,fp,tn,fn,precision,recall,success):
        print("%s,%f,%f,%f,%f,%f,%f,%f,%f" % (config,c,tp_,tn_,fp_,fn_,p,r,s))

