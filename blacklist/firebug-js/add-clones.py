# firebug commit 05475b695225c867bc1d85757aeace275528aedf
# ls | grep ".js" | head | gxargs -i mv {} 13
# find . -name "*.js" | gxargs -i cp {} firebug-js
# python add-clones.py . /Users/lawtonnichols/Desktop/notjs/src/test/resources/base
# sbt "run-main notjs.concrete.interpreter.notJS /Users/lawtonnichols/Downloads/firebug/firebug-js/13/watchPanel.js"

# 01 'aitests_a001.js', 
# 02 'aitests_a002.js', 
# 03 'aitests_a003.js', 
# 04 'aitests_a004.js', 
# 05 'aitests_a006.js', 
# 06 'aitests_a007.js', 
# 07 'aitests_a008.js', 
# 08 'array_a001.js', 
# 09 'array_a002.js', 
# 10 'array_a003.js', 
# 11 'array_a004.js', 
# 12 'array_a005.js,
# 13 'array_a006.js]

import argparse, os, random, re, traceback, subprocess
parser = argparse.ArgumentParser()
# the code in each separate directory will have the same code added to it somewhere
parser.add_argument('directory_of_directories')
parser.add_argument('directory_containing_js_files_to_insert')
parser.add_argument('notjs_dir')
args = parser.parse_args()

def get_immediate_subdirectories(a_dir):
    return [os.path.abspath(os.path.join(a_dir, name)) for name in os.listdir(a_dir)
            if os.path.isdir(os.path.join(a_dir, name))]

def get_files_in_dir(a_dir):
    return [f for f in os.listdir(a_dir) if os.path.isfile(os.path.join(a_dir, f))]

def get_file_text(f):
    with open(f) as fp:
        return fp.read()

def run_parser(f):
    f = os.path.abspath(f)
    cwd = os.getcwd()
    # print "cwd:", cwd
    os.chdir(args.notjs_dir)

    cmd = ['sbt', 'run-main notjs.concrete.interpreter.notJS {}'.format(f)]
    p = subprocess.Popen(cmd)
    p.communicate()

    os.chdir(cwd)

    return p.returncode

# get all the directories
dirs = get_immediate_subdirectories(args.directory_of_directories)

# get all the .js files to insert
jsdir = args.directory_containing_js_files_to_insert
files = get_files_in_dir(jsdir)
files = filter(lambda f: f[-3:] == '.js', files)
files.sort()
print files
# quit()

files_that_didnt_work = []

current_clone = get_file_text(os.path.join(jsdir, files[0]))
which_clone = 0

dirs.sort()
print dirs
# quit()

# for each directory
for d in dirs:
    # for every file in that directory
    for f in get_files_in_dir(d):
        if f[-3:] != ".js":
            continue

        f = os.path.join(d, f)
        # randomly pick an empty line on which to insert the clone
        with open(f) as fp:
            f_text = fp.read()
        newlinelocs = [m.start() for m in re.finditer(r'\n\n', f_text)]
        if (len(newlinelocs) == 0):
            print f
            print "========================="
            print f_text
            print "========================="
            newlinelocs = [len(f_text) - 1]
        assert(len(newlinelocs) > 0)
        random.shuffle(newlinelocs)
        randomloc = newlinelocs[0]
        print "adding to loc", randomloc
        # replace the \n with the clone
        f_text_old = f_text
        f_text = f_text_old[:randomloc+1] + current_clone + f_text_old[randomloc+1:]

        # make sure the new stuff parses
        # try 100 times
        for i in range(10):
            # run parser and get output
            with open(f, 'w') as fp:
                fp.write(f_text)
            try:
                res = run_parser(f)
            except Exception as e:
                traceback.print_exc()
                with open(f, 'w') as fp:
                    fp.write(f_text_old)
            if res == 0:
                break

            # try some other spot
            random.shuffle(newlinelocs)
            randomloc = newlinelocs[0]
            print "adding to loc", randomloc
            f_text = f_text_old[:randomloc+1] + current_clone + f_text_old[randomloc+1:]

            if i == 9:
                with open(f, 'w') as fp:
                    fp.write(f_text_old)
                print "couldn't get", f, "to parse"
                files_that_didnt_work += [f]

        # print "++++++++++++++++++++++++++"
        # print f_text
        # print "++++++++++++++++++++++++++"
        with open(f, 'w') as fp:
            fp.write(f_text)

    # move to the next clone for the next directory
    which_clone += 1
    if which_clone < len(files):
        current_clone = get_file_text(os.path.join(jsdir, files[which_clone]))

print "files that didn't work:", files_that_didnt_work