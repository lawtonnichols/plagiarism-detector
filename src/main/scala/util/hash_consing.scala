package fett.util.hashConsing

trait SmartHash extends Product {
  private lazy val cached = scala.runtime.ScalaRunTime._hashCode(this)
  override def hashCode() = cached
}

case class HashedList[+A](list: List[A]) extends SmartHash {
    def toIndexedSeq: HashedIndexedSeq[A] = HashedIndexedSeq(list.toIndexedSeq)
}
case class HashedIndexedSeq[+A](seq: IndexedSeq[A]) extends SmartHash
