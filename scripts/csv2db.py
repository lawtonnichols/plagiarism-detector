#!/usr/bin/env python3
from sys import argv
import csv
import sqlite3

def lang(fn):
    return fn.split(':')[0].split('.')[-1]

db = argv[1]
csvfile = open(argv[2])

rows = [row for row in csv.reader(csvfile)]

csvfile.close()

conn = sqlite3.connect(db)
c = conn.cursor()

print("creating table and indices")

c.execute('''CREATE TABLE results (a text, alang text, b text, blang text, score real, primary key (a, b))''')

for idx in ['a', 'alang', 'blang', 'score']:
    c.execute('''CREATE INDEX results_%s ON results (%s)''' % (idx, idx))

conn.commit()

c = conn.cursor()

print("filling the table")

n = len(rows)

tupls = []
for i in range(1, n):
    for j in range(1, n):
        a = rows[0][i]
        b = rows[j][0]
        score = float(rows[i][j])
        tupl = (a, lang(a), b, lang(b), score)
        tupls.append(tupl)

c.executemany('''INSERT INTO results (a, alang, b, blang, score) VALUES (?, ?, ?, ?, ?)''', tupls)

conn.commit()
conn.close()
print("success")
