package com.datasignals.affinetransforms.entry
//
//import scala.reflect.runtime.universe.TypeTag
//
//object SerialisationInfo {
//
//  def apply[T: TypeTag](l: Int): SerialisationInfo[T] =
//    new AbstractSerialisationInfo[T] with FixedLengthSerialisationInfo[T]{override val length: Int = l}
//
//  def apply[T: TypeTag](f: T => Int): SerialisationInfo[T] =
//    new AbstractSerialisationInfo[T] with VariableLengthSerialisationInfo[T]{override def length(v: T): Int = f(v)}
//}
//
//trait SerialisationInfo[T] {
//  val tag: TypeTag[T]
//  def length(v: T): Int
//}
