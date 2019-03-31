// This file contains the definitions of the notJS concrete semantics
// domains. See the notJS semantics document Section 2.1 for the
// specification. The State definition is in interpreter.scala since
// the state transition rules are implemented as a method of State.

package notjs.concrete.domains

import notjs.syntax._

//——————————————————————————————————————————————————————————————————————————————
// Value: BValue

sealed abstract class Value

sealed abstract class BValue extends Value {
  // binary operators: type specific
  def +( bv:BValue ): BValue = sys.error("translator reneged")
  def −( bv:BValue ): BValue = sys.error("translator reneged")
  def ×( bv:BValue ): BValue = sys.error("translator reneged")
  def ÷( bv:BValue ): BValue = sys.error("translator reneged")
  def %( bv:BValue ): BValue = sys.error("translator reneged")
  def <<( bv:BValue ): BValue = sys.error("translator reneged")
  def >>( bv:BValue ): BValue = sys.error("translator reneged")
  def >>>( bv:BValue ): BValue = sys.error("translator reneged")
  def <( bv:BValue ): BValue = sys.error("translator reneged")
  def ≤( bv:BValue ): BValue = sys.error("translator reneged")
  def &( bv:BValue ): BValue = sys.error("translator reneged")
  def |( bv:BValue ): BValue = sys.error("translator reneged")
  def ⊻( bv:BValue ): BValue = sys.error("translator reneged")
  def and( bv:BValue ): BValue = sys.error("translator reneged")
  def or( bv:BValue ): BValue = sys.error("translator reneged")
  def ++( bv:BValue ): BValue = sys.error("translator reneged")
  def ≺( bv:BValue ): BValue = sys.error("translator reneged")
  def ≼( bv:BValue ): BValue = sys.error("translator reneged")

  // binary operators: all types
  def ≈( bv:BValue ): BValue = 
    (this ≡ bv) or ((this, bv) match {
      case (Null, Undef) | (Undef, Null) ⇒ Bool.True
      case (n:Num, str:Str) ⇒ n ≡ str.tonum
      case (str:Str, n:Num) ⇒ str.tonum ≡ n
      case _ ⇒ Bool.False
    })

  def ≡( bv:BValue ): BValue = Bool(this == bv)

  // unary operators: type specific
  def negate: BValue = sys.error("translator reneged")
  def bitwisenot: BValue = sys.error("translator reneged")
  def logicnot: BValue = sys.error("translator reneged")

  // unary operators: all types
  def isprim: BValue = Bool.True
  def tobool: BValue
  def tostr: Str
  def tonum: Num
}

//——————————————————————————————————————————————————————————————————————————————
// Num

case class Num( n:Double ) extends BValue {
  override def +( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num(n + n2)
    case _ ⇒ sys.error("translator reneged")
  }

  override def −( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num(n - n2)
    case _ ⇒ sys.error("translator reneged")
  }

  override def ×( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num(n * n2)
    case _ ⇒ sys.error("translator reneged")
  }

  override def ÷( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num(n / n2)
    case _ ⇒ sys.error("translator reneged")
  }

  override def %( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num(n % n2)
    case _ ⇒ sys.error("translator reneged")
  }

  override def <<( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num( (n.toInt << n2.toInt).toDouble )
    case _ ⇒ sys.error("translator reneged")
  }

  override def >>( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num( (n.toInt >> n2.toInt).toDouble )
    case _ ⇒ sys.error("translator reneged")
  }

  override def >>>( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num( (n.toInt >>> n2.toInt).toDouble )
    case _ ⇒ sys.error("translator reneged")
  }

  override def <( bv:BValue ) = bv match {
    case Num(n2) ⇒ Bool(n < n2)
    case _ ⇒ sys.error("translator reneged")
  }

  override def ≤( bv:BValue ) = bv match {
    case Num(n2) ⇒ Bool(n <= n2)
    case _ ⇒ sys.error("translator reneged")
  }

  override def &( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num( (n.toInt & n2.toInt).toDouble )
    case _ ⇒ sys.error("translator reneged")
  }

  override def |( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num( (n.toInt | n2.toInt).toDouble )
    case _ ⇒ sys.error("translator reneged")
  }

  override def ⊻( bv:BValue ) = bv match {
    case Num(n2) ⇒ Num( (n.toInt ^ n2.toInt).toDouble )
    case _ ⇒ sys.error("translator reneged")
  }

  override def negate = Num(-n)
  override def bitwisenot = Num( (~(n.toInt)).toDouble )

  def tobool = Bool( (n != 0) && !n.isNaN )
  def tostr = if (n.toLong == n) Str(n.toLong.toString) else Str(n.toString)
  def tonum = this

  override def toString = 
    this.tostr.str
}

object Num {
  // maximum u32 value
  val maxU32 = 4294967295L

  // is this value an unsigned 32-bit integer?
  def isU32( bv:BValue ): Boolean =
    bv match {
      case Num(n) ⇒ n.toLong == n && n >= 0 && n <= maxU32 
      case _ ⇒ false
    }
}

//——————————————————————————————————————————————————————————————————————————————
// Bool

case class Bool( b:Boolean ) extends BValue {
  override def and( bv:BValue ) =
    if (!b) this else bv

  override def or( bv:BValue ) =
    if (b) this else bv

  override def logicnot = Bool(!b)

  def tobool = this
  def tostr = Str(b.toString)
  def tonum = if (b) Num(1) else Num(0)

  override def toString = 
    b.toString
}

object Bool {
  val True = Bool(true)
  val False = Bool(false)
}

//——————————————————————————————————————————————————————————————————————————————
// Str

case class Str( str:String ) extends BValue {
  override def ++( bv:BValue ) : Str = bv match {
    case Str(str2) ⇒ Str(str + str2)
    case _ ⇒ sys.error("translator reneged")
  }

  override def ≺( bv:BValue ) = bv match {
    case Str(str2) ⇒ Bool(str < str2)
    case _ ⇒ sys.error("translator reneged")
  }

  override def ≼( bv:BValue ) = bv match {
    case Str(str2) ⇒ Bool(str <= str2)
    case _ ⇒ sys.error("translator reneged")
  }

  def tobool = Bool(str != "")
  def tostr = this
  def tonum = try { Num(str.toDouble) } catch { case e: java.lang.NumberFormatException ⇒ Num(Double.NaN) }

  override def toString = str
}

//——————————————————————————————————————————————————————————————————————————————
// Null and Undef

case object Null extends BValue {
  def tobool = Bool.False
  def tostr = Str( "null" )
  def tonum = Num(0)
  override def toString = "null"
}

case object Undef extends BValue {
  def tobool = Bool.False
  def tostr = Str( "undefined" )
  def tonum = Num(Double.NaN)
  override def toString = "undefined"
}
