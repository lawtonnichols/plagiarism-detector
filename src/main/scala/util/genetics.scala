package fett.util.genetics

import fett.util
import fett.util.utils.Implicits._
import fett.util.hashConsing._

import scala.util.Random
import scala.collection.mutable.{Map => MMap}
import java.io._
import scala.collection.BitSet
import scala.collection.concurrent.TrieMap
import scala.annotation.tailrec

trait GeneticAlgorithm[Chromosome] {
  val initialSet: IndexedSeq[Chromosome]
  val populationSizePerGeneration: Int
  val ratioToKeep: Double
  val ratioToChooseRandomly: Double

  assert(ratioToKeep + ratioToChooseRandomly <= 1)

  // mutation probability as a function of current generation
  def pMutate(t: Int): Double
  var currentGeneration = initialSet
  var currentGenerationNum = 0

  def fitness(c: Chromosome): (Double, Double)
  /** mutate `c` by choosing a random mutation.
    */
  def mutate(c: Chromosome): Chromosome
  def crossOver(c1: Chromosome, c2: Chromosome): Seq[Chromosome]

  def getRandomElement[A](xs: IndexedSeq[A]): A = {
    xs(Random.nextInt(xs.size))
  }

  def init() {
    // add enough new things to get the population to the right size
    while (currentGeneration.size < populationSizePerGeneration) {
      // pick a random individual, mutate it, and add it to the currentGeneration
      val new_i = mutate(getRandomElement(initialSet))
      currentGeneration = currentGeneration :+ new_i
    }
  }

  // returns the most fit individual from the previous generation
  def nextGeneration(): ((Double, Double), Chromosome) = {
    val population = currentGeneration

    // evaluate the fitness of each member of the population
    val sortedPopulation = population.par.map(i => (fitness(i), i)).toIndexedSeq.sortBy(_._1._1).reverse

    // get the best members of the population
    val bestMembers = sortedPopulation.take((ratioToKeep * populationSizePerGeneration).intValue)
    val randomMembers = Random.shuffle(sortedPopulation).take((ratioToChooseRandomly * populationSizePerGeneration).intValue)
    val survivedMembers = bestMembers ++ randomMembers

    // cross over and mutate to get the rest of the new population
    var newGeneration: IndexedSeq[Chromosome] = survivedMembers.map(_._2)
    while (newGeneration.size < populationSizePerGeneration) {
      // pick two random individuals, cross them, possibly mutate, and add it to the newGeneration
      val newMembers = crossOver(getRandomElement(survivedMembers)._2, getRandomElement(survivedMembers)._2)

      if (newGeneration.size < populationSizePerGeneration - 1)
        newGeneration ++= newMembers
      else
        newGeneration +:= newMembers.head
    }

    // mutation
    newGeneration = newGeneration map { c ⇒
        if (Random.nextDouble < pMutate(currentGenerationNum)) {
          mutate(c)
        } else {
          c
        }
    }

    currentGeneration = newGeneration
    currentGenerationNum += 1

    println(s"best fitness in generation $currentGenerationNum: " + bestMembers.head._1)
    bestMembers.head
  }

  def run(outputFileName: String) {
    var bestIndividual = initialSet.head // dummy value
    var score = (Double.NaN, Double.NaN) // dummy value

    val out = new FileWriter(outputFileName)

    while (
      currentGenerationNum < 10000 &&
        // in between generations, see if the user wants to stop
        !(java.lang.System.in.available > 0 && scala.io.StdIn.readLine() == "quit")
    ) {

      // do a generation
      val nextBest = nextGeneration()

      val prevBest = bestIndividual
      score = nextBest._1
      bestIndividual = nextBest._2

      if (prevBest != bestIndividual) {
        out.write(s"new best individual at generation $currentGenerationNum with score $score")
        out.write(bestIndividual.toString)
        out.write("\n")
      }
    }

    out.write("\n---------------------------------------------\n")
    out.write("---------------------------------------------\n")
    out.write("final generation (best to worst): \n")
    val sortedPopulation = currentGeneration.par.map(i => (fitness(i), i)).toIndexedSeq.sortBy(_._1._1).reverse
    for ((score, individual) ← sortedPopulation) {
      out.write("score: $score\n")
      out.write("individual:\n")
      out.write(individual.toString)
      out.write("---------------------------------------------\n")
    }

    out.close()
    println(bestIndividual)
  }
}

case class ClassConfiguration(nodeNamesToIgnore: Set[Symbol], nodeEquivalenceClasses: Set[Set[Symbol]]) {
  assert(nodeEquivalenceClasses.forall(_.nonEmpty))

  private def setToString(s: Set[Symbol]) = {
    s.mkString(", ")
  }

  override def toString = {
    val ignore = setToString(nodeNamesToIgnore)
    val eqClasses = for ((eqClass, i) ← nodeEquivalenceClasses.toIndexedSeq.zipWithIndex) yield {
      s"$i: ${setToString(eqClass)}"
    }

    s"""
configuration {
  ignore: $ignore
  classes:
    ${eqClasses.mkString("\n    ")}
}""".stripMargin
  }
}
case class NodeNameClasses(
  allNodeNames: Set[Symbol],
  pairsThatShouldBeClones: Set[(HashedIndexedSeq[Symbol], HashedIndexedSeq[Symbol])],
  pairsThatShouldntBeClones: Set[(HashedIndexedSeq[Symbol], HashedIndexedSeq[Symbol])],
  populationSizePerGeneration: Int = 100,
  ratioToKeep: Double = 0.5,
  ratioToChooseRandomly: Double = 0.3,
  maxRatioOfNodesToIgnore: Double = 0.1
) extends GeneticAlgorithm[ClassConfiguration] {
  val defaultConfiguration = ClassConfiguration(Set(), allNodeNames.map(Set(_)))
  val initialSet = (1 to populationSizePerGeneration / 5).map(_ => randomConfiguration()).toIndexedSeq

  val maxNodesToIgnore = (maxRatioOfNodesToIgnore * allNodeNames.size).toInt

  def randomConfiguration() = {
    // TODO: choose a set of nodes to be ignored
    val n = allNodeNames.size
    val k = Random.nextInt(n-3) + 4 // # of equivalence classes, have at least 4 classes
    // to generate k subsets, pick a permutation and k-1 cutpoints
    val permutation = Random.shuffle(allNodeNames).toArray
    val cutpoints = (0 +: Random.shuffle(1 to (n-1)).take(k-1).sorted :+ n).toIndexedSeq // pick k-1 cut points for subsets
    val subsets = Array.fill(k)(Set.empty[Symbol]) // subsets
    for (i ← 1 to k) {
      subsets(i-1) = permutation.slice(cutpoints(i-1), cutpoints(i)).toSet
    }
    assert(subsets.forall(_.nonEmpty))
    // this is with not ignoring any nodes
    ClassConfiguration(Set.empty, subsets.toSet)
  }

  // usually pMutation ~ 1/L where L is length of our chromosomes is good [citation needed]
  // define pMutation as an exponential decay to get higher mutation rate thus searching space more
  // initially
  val pMInf = 1.0 / allNodeNames.size // eventual mutation rate
  val pM0 = 0.3 // initial mutation rate
  val decayRate = 0.05
  @inline
  def pMutate(t: Int) = {
    (pM0 - pMInf)*math.exp(t * decayRate) + pMInf
  }
  
  currentGeneration = initialSet

  def mutate(c: ClassConfiguration) = mutate(c, BitSet.empty)

  /** mutate `c` by choosing a random mutation. The `flags` are used for keeping track of unsuitable mutations.
    */
  @tailrec
  final def mutate(c: ClassConfiguration, flag: BitSet): ClassConfiguration = {
    val suitableMutations = BitSet(0, 1, 2, 3) &~ flag
    if (suitableMutations.isEmpty) {
      c
    } else {
      getRandomElement(suitableMutations.toIndexedSeq) match {
        case 0 =>
          // randomly start ignoring a node name
          val nodeToIgnore = getRandomElement((allNodeNames -- c.nodeNamesToIgnore).toIndexedSeq)
          // get rid of it if it's in any equivalence class
          val newEquivalenceClasses = c.nodeEquivalenceClasses.map(s => s.filter(_ != nodeToIgnore)).filter(_.nonEmpty)

          val newIgnoreSet = c.nodeNamesToIgnore + nodeToIgnore

          // if we reached a maximum, stop ignoring a node at the same time to keep below maximum
          // so that we are still getting some mutation
          if (c.nodeNamesToIgnore.size == maxNodesToIgnore) {
            val nodeToStopIgnoring = getRandomElement(c.nodeNamesToIgnore.toIndexedSeq)
            ClassConfiguration(newIgnoreSet - nodeToStopIgnoring, newEquivalenceClasses + Set(nodeToStopIgnoring))
          } else {
            ClassConfiguration(newIgnoreSet, newEquivalenceClasses)
          }
        case 1 =>
          // randomly stop ignoring a node name
          if (c.nodeNamesToIgnore.nonEmpty) {
            val nodeToStopIgnoring = getRandomElement(c.nodeNamesToIgnore.toIndexedSeq)
            ClassConfiguration(c.nodeNamesToIgnore - nodeToStopIgnoring, c.nodeEquivalenceClasses + Set(nodeToStopIgnoring))
          } else {
            mutate(c, flag + 1)
          }
        case 2 =>
          // randomly combine two equivalence classes if there are at least two
          if (c.nodeEquivalenceClasses.size == 1) {
            mutate(c, flag + 2)
          } else {
          val nodeSeq = c.nodeEquivalenceClasses.toIndexedSeq
          val class1 = getRandomElement(nodeSeq)
          val class2 = getRandomElement(nodeSeq)
            ClassConfiguration(c.nodeNamesToIgnore, c.nodeEquivalenceClasses - class1 - class2 + (class1 ++ class2))
          }
        case 3 =>
          // randomly take something out of an equivalence class with size > 1
          val suitableClasses = c.nodeEquivalenceClasses.filter(_.size > 1)
          if (suitableClasses.isEmpty) {
            mutate(c, flag + 3) // this mutation is not suitable
          } else {
            val classToSplit = getRandomElement(suitableClasses.toIndexedSeq)
            val elementToTakeOut = getRandomElement(classToSplit.toIndexedSeq)
            ClassConfiguration(c.nodeNamesToIgnore, c.nodeEquivalenceClasses - classToSplit + (classToSplit - elementToTakeOut) + Set(elementToTakeOut))
          }
      }
    }
  }

  // returns (isThereADuplicate, (the duplicates))
  def containsDuplicates(s: Set[Set[Symbol]]): (Boolean, (Set[Symbol], Set[Symbol])) = {
    val setContainingSymbol = MMap[Symbol, Set[Symbol]]()
    for (class_ <- s) {
      for (nodeName <- class_) {
        if (setContainingSymbol.contains(nodeName))
          return (true, (setContainingSymbol(nodeName), class_))
        setContainingSymbol(nodeName) = class_
      }
    }

    return (false, (Set(), Set()))
  }

  // should really be using union find here
  def combineClasses(s: Set[Set[Symbol]]): Set[Set[Symbol]] = {
    var newSet = s
    var (b, (set1, set2)) = containsDuplicates(newSet)
    while (b) {
      newSet = newSet - set1 - set2 + (set1 ++ set2)
      containsDuplicates(newSet) match {
        case (b_, (set1_, set2_)) =>
          b = b_
          set1 = set1_
          set2 = set2_
      }
    }

    newSet
  }

  def crossOver(c1: ClassConfiguration, c2: ClassConfiguration): Seq[ClassConfiguration] = {
    // swap nodeNamesToIgnore to have a single-point crossover
    // add spurious nodes to new equivalence classes
    val diff1 = c1.nodeNamesToIgnore -- c2.nodeNamesToIgnore
    val diff2 = c2.nodeNamesToIgnore -- c1.nodeNamesToIgnore
    val eqClasses1 = c1.nodeEquivalenceClasses.map(_ -- diff2).filter(_.nonEmpty) ++ diff1.map(Set(_))
    val eqClasses2 = c2.nodeEquivalenceClasses.map(_ -- diff1).filter(_.nonEmpty) ++ diff2.map(Set(_))
    val c1_ = ClassConfiguration(c2.nodeNamesToIgnore, eqClasses1)
    val c2_ = ClassConfiguration(c1.nodeNamesToIgnore, eqClasses2)

    Seq(c1_, c2_)
  }

  type Score = (Double, Double)
  val fitnessMemo = TrieMap.empty[ClassConfiguration, Score]

  def fitness(c: ClassConfiguration): (Double, Double) = fitnessMemo.getOrElseUpdate(c, {

    import fett.scoring._

    val SW = SmithWaterman(
      similarityFn=SmithWaterman.classBasedNodeNameSimilarity,
      eqClasses=Some(c.nodeEquivalenceClasses),
      uselessNodeSet=Some(c.nodeNamesToIgnore))
    // take averages of scores of clone pairs and non-clone pairs
    val combinedClonesScore = pairsThatShouldBeClones.map(p => SW.similarityOfSeqs(p._1, p._2).score).sum / pairsThatShouldBeClones.size
    val combinedNonClonesScore = pairsThatShouldntBeClones.map(p => SW.similarityOfSeqs(p._1, p._2).score).sum / pairsThatShouldntBeClones.size
    val avgScore = (combinedClonesScore + combinedNonClonesScore) / 2.0
    // compute normazlized deviation from average as score
    ((combinedClonesScore - combinedNonClonesScore) / avgScore, avgScore)
  })
}

object GenerateNodeNameClasses {
  // https://www.safaribooksonline.com/library/view/scala-cookbook/9781449340292/ch12s09.html
  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def main(args: Array[String]): Unit = {
    val debugOn = false
    if (debugOn) {
      util.setLogLevel(util.Level.Debug)
    } else {
      util.setLogLevel(util.Level.Off)
    }

    val folders = args.init.toIndexedSeq
    val outputFileName = args.last

    val filesInFolders = folders.map(getListOfFiles)

    println("Reading files")

    val readFile = (f:File) ⇒ HashedIndexedSeq(fett.CloneDetection.readAST(None, f).head.toSExp.preOrder)

    println("Creating clone sets, pairs, etc.")

    val cloneSets: Seq[Set[HashedIndexedSeq[Symbol]]] = filesInFolders.map(_.map(readFile).toSet)
    val allClones = cloneSets.flatten
    val pairs: Set[(HashedIndexedSeq[Symbol], HashedIndexedSeq[Symbol])] = allClones.combinations(2).map(l => (l(0), l(1))).filter(p => p._1 != p._2).toSet
    val clonePairs = pairs.filter({ case (l1, l2) => {
      val s = Set(l1, l2)
      cloneSets.exists(s.subsetOf)
    }})
    val nonClonePairs = pairs -- clonePairs

    val allNodeNames = allClones.map(_.seq.toSet).toSet.flatten
    val NNC = NodeNameClasses(allNodeNames, clonePairs, nonClonePairs)

    println("Running genetic algorithm")
    NNC.run(outputFileName)
  }
}
