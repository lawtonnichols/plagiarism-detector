#!/usr/bin/env python3
# coding: utf-8

import argparse, csv
parser = argparse.ArgumentParser()
parser.add_argument("ground_truth")
parser.add_argument("nicad_xml_results")
args = parser.parse_args()

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

results = loadGroundTruth(args.ground_truth)

import xml.etree.ElementTree as ET, os
tree = ET.parse(args.nicad_xml_results)
root = tree.getroot()

def getFile(s):
    # print(s)
    return os.path.basename(s.split(':')[0]).strip()

def getStartLine(s):
    # print(s)
    return s.split(':')[1].strip()

nicad_results = {}

for clone in root.findall('clone'):
    assert(len(clone) == 2)
    file1 = os.path.basename(clone[0].attrib['file']).strip()
    file2 = os.path.basename(clone[1].attrib['file']).strip()
    start1 = clone[0].attrib['startline'].strip()
    start2 = clone[1].attrib['startline'].strip()
    sim = clone.attrib['similarity']
    nicad_results[file1,start1,file2,start2] = sim
    nicad_results[file2,start2,file1,start1] = sim

def did_nicad_say_it_was_a_clone(x, y):
    
    filex = getFile(x)
    filey = getFile(y)
    startx = getStartLine(x)
    starty = getStartLine(y)

    res = False, 0.0

    if (filex, startx, filey, starty) in nicad_results:
        res = True, float(nicad_results[filex, startx, filey, starty]) / 100.0

    return res

for x, y in results:
    res, sim = did_nicad_say_it_was_a_clone(x, y)
    print(','.join([x, y, str(sim)]))