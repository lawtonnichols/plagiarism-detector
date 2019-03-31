#!/usr/bin/env python3

import sys
import sqlite3 as sq

if len(sys.argv) != 5:
    print("usage: %s db-file langa langb output-csv" % sys.argv[1], file=sys.stderr)
    exit(1)

_, dbfile, langa, langb, outfname = tuple(sys.argv)

csvfile = open(outfname, 'w')
conn = sq.connect(dbfile)
c = conn.cursor()

csvfile.write("a, b, score\n")
for row in c.execute("select a, b, score from results where alang = ? and blang = ?", (langa, langb)):
    t = list(tuple(row))
    if t[2] is None:
        t[2] = float('nan')
    t = tuple(t)
    csvfile.write("%s, %s, %f\n" % t)

csvfile.close()
