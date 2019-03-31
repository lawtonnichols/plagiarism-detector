#!/usr/bin/env python3
# coding: utf-8

import argparse, csv, os
parser = argparse.ArgumentParser()
parser.add_argument("ground_truth")
parser.add_argument("folder_with_tests")
parser.add_argument("output_folder")
parser.add_argument("fett_executable")
parser.add_argument("class_file")
parser.add_argument("--matchScore", default="1")
parser.add_argument("--gapScore", default="-3")
parser.add_argument("--doTED", action='store_true')
parser.add_argument("--insertionScore", default="1")
parser.add_argument("--deletionScore", default="1")
parser.add_argument("--fileHeader", default="output")
parser.add_argument("--nicad_results")

args = parser.parse_args()

ground_truth = os.path.abspath(args.ground_truth)
folder_with_tests = os.path.abspath(args.folder_with_tests)
output_folder = os.path.abspath(args.output_folder)
fett_executable = os.path.abspath(args.fett_executable)
class_file = os.path.abspath(args.class_file)

assert(os.path.isdir(output_folder))

sw_cmd = ["java", "-Xmx4G", "-jar", fett_executable, "-s", 
       "smith-waterman", "-r", "-g", "function", "-X", 
       "cutoff=true", "similarity=classBased", "matchScore=%s" % args.matchScore, 
       "gapScore=%s" % args.gapScore, "classFile=%s" % class_file, "-c", os.path.join(output_folder, 'sw-results-square.csv'), "-j5", 
       "--pair-filter", ground_truth, "allpairs", folder_with_tests]

mining_cmd = ["java", "-Xmx4G", "-jar", fett_executable, "-s", 
       "mining", "-r", "-g", "function", "-X", 
       "cutoff=true", "-c", os.path.join(output_folder, 'mining-results-square.csv'), "-j5", 
       "--pair-filter", ground_truth, "allpairs", folder_with_tests]

ted_cmd = ["java", "-Xmx4G", "-jar", fett_executable, "-s", 
       "tree-edit-distance", "-r", "-g", "function", "-X", 
       "cutoff=true", "similarity=classBased", "insertionScore=%s" % args.insertionScore, 
       "deletionScore=%s" % args.deletionScore, "classFile=%s" % class_file, "-c", os.path.join(output_folder, 'ted-results-square.csv'), "-j5", 
       "--pair-filter", ground_truth, "allpairs", folder_with_tests]


import subprocess

# get results

print(sw_cmd)
subprocess.run(sw_cmd)
print(mining_cmd)
subprocess.run(mining_cmd)
if args.doTED:
	print(ted_cmd)
	subprocess.run(ted_cmd)

# pairify

with open(os.path.join(output_folder, args.fileHeader + '-sw-results-pairs.csv'), 'w') as f:
	subprocess.run(['./pairify', os.path.join(output_folder, 'sw-results-square.csv'), ground_truth], stdout=f)
with open(os.path.join(output_folder, args.fileHeader + '-mining-results-pairs.csv'), 'w') as f:
	subprocess.run(['./pairify', os.path.join(output_folder, 'mining-results-square.csv'), ground_truth], stdout=f)
if args.doTED:
	with open(os.path.join(output_folder, args.fileHeader + '-ted-results-pairs.csv'), 'w') as f:
		subprocess.run(['./pairify', os.path.join(output_folder, 'ted-results-square.csv'), ground_truth], stdout=f)	

# make graphs

tedExtras = []
if args.doTED:
	tedExtras = ['--ted_results', os.path.join(output_folder, args.fileHeader + '-ted-results-pairs.csv')]
nicadExtras = []
if args.nicad_results is not None:
	nicadExtras = ['--nicad_results', os.path.abspath(args.nicad_results)]

subprocess.run(['./cumulativeSumPlot.py', ground_truth, 
	os.path.join(output_folder, args.fileHeader + '-sw-results-pairs.csv'),
	os.path.join(output_folder, args.fileHeader + '-mining-results-pairs.csv'),
	'-o', os.path.join(output_folder, args.fileHeader + '.pdf')] + tedExtras + nicadExtras)

subprocess.run(['./binGraph.py', ground_truth, 
	os.path.join(output_folder, args.fileHeader + '-sw-results-pairs.csv'),
	os.path.join(output_folder, args.fileHeader + '-mining-results-pairs.csv'),
	'-o', os.path.join(output_folder, args.fileHeader + '-bins.pdf')] + tedExtras + nicadExtras)