#!/usr/bin/env python3

import sys
import sqlite3
import csv

def process_row(row):
    a, b, is_clone = tuple(row)
    if is_clone.strip() == "True":
        is_clone = True
    elif is_clone.strip() == "False":
        is_clone = False
    else:
        raise Exception("%s is not a boolean value" % is_clone.strip())
        
    return (a.strip(), b.strip(), int(is_clone))

rows = [row for row in csv.reader(open(sys.argv[2]))]
rows = map(process_row, rows[1:])

conn = sqlite3.connect(sys.argv[1])
conn.commit() # begin transaction
c = conn.cursor()
c.executemany("INSERT OR IGNORE INTO ground_truth VALUES (?, ?, ?)", rows)
conn.commit()
conn.close()

