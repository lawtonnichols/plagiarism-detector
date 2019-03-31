# Syntax-based Improvements to Plagiarism Detectors and their Evaluations

This repository contains the implementation of our plagiarism detection tool.

## Dependencies

 * Java 8+
 * Scala 2.12.0+
 * SBT 0.13.13+
 * Check `build.sbt` for dependencies fetched by SBT
 * Check `project/assembly.sbt` for used SBT plugins

## How to Build

You can run `sbt assembly` to get an executable CLI program immediately. If build fails, you can stage the build as follows to figure out where the problem is:

 1. Run `sbt clean` to fetch the dependencies.
 2. Run `sbt compile` to compile the code.
 3. Run `sbt assembly` to create an executable at `target/scala-2.12/fett`

## How to run

We assume that the current working directory set to the root directory of this repository.

Running our method on the tests from the paper:
```console
target/scala-2.12/fett -s smith-waterman -g file -X similarity=classBased matchScore=1 gapScore=-2 classFile=src/main/resources/new.json -c ~/Downloads/results.csv -j6 allpairs src/test/resources/plagiarism-detection/generated/java/*.java;
```

Running Zhang et al.'s method on the tests from the paper:
```console
target/scala-2.12/fett -s zhang13-smith-waterman -g file -X matchScore=1 gapScore=-1 -j6 -c ~/Downloads/results.csv allpairs src/test/resources/plagiarism-detection/generated/java/*.java;
```

It will compute similarity scores for each pair, and save those results in `~/Downloads/results.csv`.

## Project structure

 * `build.sbt` and `project/`: SBT configurations
 * `lib`: binary dependencies
 * `src/main/scala`
   + `main.scala`: Entry point of the program
   + `sexp.scala`: S-expressions
   + `scoring`: Scoring algorithms
   + `util`: Utility functions