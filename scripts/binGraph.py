#!/usr/bin/env python3
# coding: utf-8

import argparse, csv
parser = argparse.ArgumentParser()
parser.add_argument("ground_truth")
parser.add_argument("combined_scores")
parser.add_argument("structural_scores")
parser.add_argument("mining_scores")
parser.add_argument("-o", "--graph_output_file", default="graph.pdf")
parser.add_argument("--ted_results")
parser.add_argument("--nicad_results")
args = parser.parse_args()

def loadScores(csvfile, isDistance=False):
    with open(csvfile) as f:
        rows = [row for row in csv.reader(f)]
        # rows = rows[1:]
        f.close()
        results = []
        for a, b, score in rows:
            if isDistance:
                results += [(a, b, 1.0 - float(score))]
            else:
                results += [(a, b, float(score))]
        return results

def loadGroundTruth(csvfile):
    with open(csvfile) as f:
        rows = [row for row in csv.reader(f)]
        # rows = rows[1:]
        f.close()
        results = {}
        for a, b, yesno in rows:
            if yesno == 'True':
                results[a,b] = True
            else:
                results[a,b] = False
        return results

ground_truth = loadGroundTruth(args.ground_truth)
combined = loadScores(args.combined_scores)
structural = loadScores(args.structural_scores)
mining = loadScores(args.mining_scores, True)

useTED = False if args.ted_results is None else True

if useTED:
    tedFile = args.ted_results
    ted = loadScores(tedFile, False)

useNicad = False if args.nicad_results is None else True

if useNicad:
    nicadFile = args.nicad_results
    nicad = loadScores(nicadFile, False)

import numpy as np

bins = list(zip(np.linspace(1.0, 0.1, 10), np.linspace(0.9, 0.0, 10)))

def getBin(score):
    global bins
    i = 0
    for hi, lo in bins:
        if score >= lo and score <= hi:
            return i
        i += 1
    raise "Uh oh"

def getBinDistribution(scores):
    count = [0,0,0,0,0,0,0,0,0,0]
    numClones = [0,0,0,0,0,0,0,0,0,0]
    for a, b, score in scores:
        isClone = ground_truth[a, b]
        which_bin = getBin(score)
        count[which_bin] += 1
        if isClone:
            numClones[which_bin] += 1
    res = np.vectorize(lambda x: 0.0 if np.isnan(x) else x)(np.array(numClones) / np.array(count))
    # print(numClones)
    # print(count)
    # print(res)
    return res

mining_dist = getBinDistribution(mining)
structural_dist = getBinDistribution(structural)
combined_dist = getBinDistribution(combined)
if useTED:
    ted_dist = getBinDistribution(ted)
if useNicad:
    nicad_dist = getBinDistribution(nicad)

import matplotlib.pyplot as plt
plt.style.use('ggplot')
plt.close()

plt.rcParams['ytick.labelsize'] = '14'

plt.plot(mining_dist, 'o-.', label='Nominal')
plt.plot(structural_dist, 'v--', label='Structural')
plt.plot(combined_dist, '^-', label='Fᴇᴛᴛ')
if useTED:
    plt.plot(ted_dist, 'o:', label='Tree Edit Distance')
if useNicad:
    plt.plot(nicad_dist, 'v:', label='NiCad')

ticks = [1,2,3,4,5,6,7,8,9,10]
labels = ["1.0–0.9", "0.9–0.8", "0.8–0.7", "0.7–0.6", "0.6–0.5", "0.5–0.4", "0.4–0.3", "0.3–0.2", "0.2–0.1", "0.1–0.0"]
# plt.xticks(ticks, labels)
plt.xticks(range(len(labels)), labels, size='14', rotation=40)

ax = plt.axes()
ax.grid(color='gray', linestyle='--')
ax.set_facecolor('white')

plt.rcParams['font.size'] = '16'

plt.ylabel('Clone Ratio', size='20')
plt.xlabel('Similarity', size='20')
plt.legend()
plt.tight_layout()
plt.savefig(args.graph_output_file, format='pdf')