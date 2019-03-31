#!/usr/bin/env python3
# coding: utf-8

import csv, sys

import argparse
parser = argparse.ArgumentParser()
parser.add_argument("ground_truth")
parser.add_argument("combined_results")
parser.add_argument("structural_results")
parser.add_argument("mining_results")
parser.add_argument("-o", "--output_file", default="graph.pdf")
parser.add_argument("--ted_results")
parser.add_argument("--nicad_results")
args = parser.parse_args()

groundTruthFile = args.ground_truth
combinedFile = args.combined_results
structuralFile = args.structural_results
miningFile = args.mining_results

useTED = False if args.ted_results is None else True

if useTED:
    tedFile = args.ted_results

useNicad = False if args.nicad_results is None else True

if useNicad:
    nicadFile = args.nicad_results

# In[1029]:

import matplotlib.pyplot as plt


# In[1030]:

plt.style.use('ggplot')


# In[1031]:

def chop(x):
    return x.split("/")[-1].split(".")[0]


# In[1032]:

import numpy as np


# In[1033]:

def groundTruth(csvfile):
    f = open(csvfile)
    rows = [row for row in csv.reader(f)]
    #rows = rows[1:]
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


# In[1034]:

isClone = groundTruth(groundTruthFile)


# In[1035]:

isClone


# In[1036]:

def getData(csvfile, isClone, isDistance=False):
    f = open(csvfile)
    rows = [row for row in csv.reader(f)]
    #rows = rows[1:]
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

combinedScores, combinedClones = getData(combinedFile, isClone)
structuralScores, structuralClones = getData(structuralFile, isClone)
miningScores, miningClones = getData(miningFile, isClone, True)

if useTED:
    tedScores, tedClones = getData(tedFile, isClone, False)

if useNicad:
    nicadScores, nicadClones = getData(nicadFile, isClone, False)


# In[1051]:

definitelyClones = [i for i in isClone if isClone[i]]


# In[1052]:

len(definitelyClones)


# In[1053]:

np.sum(miningClones)


# In[1054]:

def findBin(x, nBins):
    return int(np.floor(x * 10))


# In[1055]:

def findRatios(scores, clones, nBins):
    bins = np.zeros(nBins)
    nonCloneBins = np.zeros(nBins)
    for s, c in zip(scores, clones):
        if c:
            bin = findBin(s, nBins)
            #print(bin)
            bins[bin] += 1
        else:
            bin = findBin(s, nBins)
            #print(bin)
            nonCloneBins[bin] += 1
    cbins = np.cumsum(bins[::-1])
    cnonCloneBins = np.cumsum(nonCloneBins[::-1])
    return np.vectorize(lambda x: 0.0 if np.isnan(x) else x)(cbins / (cbins + cnonCloneBins))



# In[1057]:

mining = findRatios(miningScores, miningClones, 11)
structural = findRatios(structuralScores, structuralClones, 11)
combined = findRatios(combinedScores, combinedClones, 11)

if useTED:
    ted = findRatios(tedScores, tedClones, 11)

if useNicad:
    nicad = findRatios(nicadScores, nicadClones, 11)



# In[1061]:

bins = np.linspace(0.0, 1.0, 11)


# In[1098]:

binsReversed = bins[::-1]


# In[1100]:

binsReversed

# style stuff

# plt.rcParams['grid.color'] = 'gray'
# plt.rcParams['grid.linestyle'] = ':'
# plt.rcParams['axes.facecolor'] = 'F5F5F5'
# plt.rcParams['axes.linewidth'] = 0.25


# In[1103]:

plt.close()

plt.rcParams['ytick.labelsize'] = '14'


# In[1104]:

miningDistance = np.vectorize(lambda x: 1.0 - x)(mining)
structuralDistance = np.vectorize(lambda x: 1.0 - x)(structural)
combinedDistance = np.vectorize(lambda x: 1.0 - x)(combined)

# In[1106]:

plt.plot(binsReversed, mining, 'o-.', label='Nominal')
plt.plot(binsReversed, structural, 'v--', label='Structural')
plt.plot(binsReversed, combined, '^-', label='Fᴇᴛᴛ')

if useTED:
    plt.plot(binsReversed, ted, 'o:', label='Tree Edit Distance')

if useNicad:
    plt.plot(binsReversed, nicad, 'v:', label='NiCad')


# In[1111]:

plt.ylabel('Cumulative Clone Ratio', size='20')


# In[1112]:

plt.xlabel('Similarity', size='20')


# In[1113]:

plt.xticks(np.arange(0, 1.1, 0.1), size='14')


# In[1114]:

plt.rcParams['font.size'] = '16'

plt.legend()

ax = plt.axes()
ax.grid(color='gray', linestyle='--')
ax.set_facecolor('white')


# In[1116]:

plt.gca().invert_xaxis()

plt.tight_layout()

# In[1115]:

plt.savefig(args.output_file, format='pdf')
