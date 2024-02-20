package com.datasignals.affinetransforms

import com.datasignals.affinetransforms.entry.{GenericRecord, Key, Record}

import java.util.concurrent.{ConcurrentHashMap, Executor, ThreadPoolExecutor}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{ExecutionContext, Future}

class Decrypt {

//  implicit private val ec = ExecutionContext.fromExecutorService(threadPool)
  private[this] val entries = new ConcurrentHashMap[Key, Entry]()
  private val dim = 1

  private[this] val recordFactory: (Key, Array[Byte]) => Record[Key, Array[Byte]] = {
    (k, v) => new GenericRecord(k, v)
  }

  private class Entry {
    private val value: Array[Byte] = new Array[Byte](dim)
    private[this] val counter = new AtomicInteger(0)

    @inline def apply(i: Int, v: Byte): Array[Byte] = {
      value(i) = v
      if (counter.incrementAndGet() == dim) value
      else null
    }

    override def toString: String = {
      s"Entry(${counter.get}, ${value.mkString(",")}"
    }
  }

//  private class Processor(private val index: Int) {
//    def apply(record: Record[Key, Array[Byte]]): Future[Unit] = Future {
//      mix(index, record)
//    }(ec)
//  }

  private def mix(index: Int, record: Record[Key, Array[Byte]]): Unit = {
    val key = record.key
    var entry = entries.get(key)
    if (entry eq null) entry = new Entry
//    entries.put(key, entry)
    val value = entry(index, record.value)
    if (value eq null) return
//    outputQueue.offer(recordFactory(key, value))
//    entries.remove(key)
    recordFactory(key, value)
  }


}
