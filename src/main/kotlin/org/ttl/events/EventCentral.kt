/*
Copyright (c) 2026 The Third Lane

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

@file:JvmName("EventCentral")

package org.ttl.events

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.concurrent.thread

val eventCentral = Eventer()

class Eventer {
   private val logger = LoggerFactory.getLogger(this::class.java)

//    private var listenerFuncs: ConcurrentMap<Any, MutableSet<(Any, List<Any>) -> Unit>> = ConcurrentHashMap();

   val x: MutableSet<(Any, List<Any>) -> Unit> = mutableSetOf()

   val y: MutableMap<Any, MutableSet<(Any, List<Any>) -> Unit>> = mutableMapOf()

   //A Map of Event to a Map of Observer to a Set of functions for that event
   // for that observer. This will usually be just one function, and the MutableSet is
   // maybe unnecessary, but someday maybe registering seperate functions per observer
   // for the same event may make sense.  The functions are of type (Any, List<Any>) -> Unit
   var eventsToObservers:
         ConcurrentMap<Any, ConcurrentMap<Any, MutableSet<(Any, List<Any>) -> Unit>>> = ConcurrentHashMap();

   //A Map of observers (any class) to a map of Event to a Set of functions for that event
   //The functions are of type (Any, List<Any>) -> Unit
   var observersToEvents: ConcurrentMap<Any, ConcurrentMap<Any, MutableSet<(Any, List<Any>) -> Unit>>> =
      ConcurrentHashMap();

   fun addObserver(observer: Any, event: Any, function: (Any, List<Any>) -> Unit) {
      addObserver(observer, listOf(event), function)
   }


   fun addObserver(observer: Any, vararg events: Any, function: (Any, List<Any>) -> Unit) {
      addObserver(observer, listOf(*events), function)
   }

   fun addObserver(observer: Any, events: List<Any>, function: (Any, List<Any>) -> Unit) {
      for (event in events) {
         val functions = getFunctionsForObserverAndEvent(observer, event)
         functions.add(function)

         val eventFunctions = getFunctionsForEventAndObserver(observer, event)
         eventFunctions.add(function)
      }
   }

   fun removeObserver(observer: Any) {
      val eventsToFunctions = observersToEvents.get(observer)
      if (eventsToFunctions != null) {
         eventsToFunctions.forEach { (event, setOfFunctions) ->
            setOfFunctions.clear()

            //Clear from the eventsToObservers Map also
            eventsToObservers.get(event)?.let { efe ->
               efe.forEach { entry ->
                  val innerObserver = entry.key
                  if (innerObserver == observer) {
                     val functions = entry.value
                     functions.clear()
                     efe.remove(innerObserver)
                  }
               }
               if (efe.isEmpty()) {
                  eventsToObservers.remove(event)
               }
            }
         }
         eventsToFunctions.clear()
      }
      observersToEvents.remove(observer)
   }

   fun shoutOutSerial(event: Any, args: List<Any>) {
      logger.trace("ShoutoutSerial of {} with {}", event, args)
      broadCastBetter(event, args)
   }

   /**
    * Send Events Synchronously.
    */
   fun broadCastBetter(event: Any, args: List<Any>) {
      val entriesForEvent: MutableSet<MutableMap.MutableEntry<Any, MutableSet<(Any, List<Any>) -> Unit>>>? =
         eventsToObservers[event]?.entries
      entriesForEvent?.let { efe ->
         efe.forEach { entry ->
            val observer = entry.key
            val functions = entry.value

            functions.forEach { function ->
               function(event, args)
            }
         }
      }
   }

   /**
    * Send Events Synchronously.
    */
   fun broadCast(event: Any, args: List<Any>) {
      val entries = observersToEvents.entries
      //Go Through all the entries, since we don't have a Key.
      for (e in entries) {
         //The map of Event to Functions
         val eventsToFunctions = e.value
         val eToFEntries = eventsToFunctions.entries
         for (eToFEntry in eToFEntries) {
            //If the event matches the one we are going to broadcase
            if (eToFEntry.key == event) {
               //get it's functions and call them
               val funcs = eToFEntry.value
               funcs.forEach {
                  it(event, args)
               }
            }
         }
      }
   }

   fun shoutOut(event: Any, args: List<Any>) {
      logger.trace("Shoutout of {} with {}", event, args)

      //The coroutine way
//        doodleScope.launch(Dispatchers.IO) {
//            broadCastAsync(event, args)
//        }

      //The Loom way
      shoutOutLoom(event, args)
   }

   //Using Coroutines.  No.  NO. NOOOOOOO.
   suspend fun broadCastAsync(event: Any, args: List<Any>) = coroutineScope {
      val entries = observersToEvents.entries
      //Go Through all the entries, since we don't have a Key.
      for (e in entries) {
         //The map of Event to Functions
         val eventsToFunctions = e.value
         val eToFEntries = eventsToFunctions.entries
         for (eToFEntry in eToFEntries) {
            //If the event matches the one we are going to broadcast
            if (eToFEntry.key == event) {
               //get it's functions and call them
               val funcs = eToFEntry.value
               launch {
                  funcs.forEach {
                     it(event, args)
                  }
               }
            }
         }
      }
   }


   fun shoutOutLoom(event: Any, args: List<Any>) {
      logger.trace("Loom Shoutout of {} with {}", event, args)

//        ExecutorManager.submit {
//            broadCastLoom(event, args)
//        }
      Thread.ofVirtual().name("BroadCastLoomThread").start {
//         broadCastLoom(event, args)
         broadCastLoomBetter(event, args)
      }

   }

   fun broadCastLoomBetter(event: Any, args: List<Any>) {
      val entriesForEvent: MutableSet<MutableMap.MutableEntry<Any, MutableSet<(Any, List<Any>) -> Unit>>>? =
         eventsToObservers[event]?.entries
      entriesForEvent?.let { efe ->
         efe.forEach { entry ->
            val observer = entry.key
            val functions = entry.value

            functions.forEach { function ->
               Thread.ofVirtual().name("ECShoutOutThread").start {
                  function(event, args)
               }
            }
         }
      }
   }

   fun broadCastLoom(event: Any, args: List<Any>) {
      val entries = observersToEvents.entries
      //Go Through all the entries (observers), since we don't have a Key.
      for (e in entries) {
         //The map of Event to Functions
         val eventToFunctions = e.value
         val eToFEntries = eventToFunctions.entries
         for (eToFEntry in eToFEntries) {
            //If the event matches the one we are going to broadcast
            if (eToFEntry.key == event) {
               //get it's functions and call them
               val funcs = eToFEntry.value
               //Each message in its own thread.
               funcs.forEach {
//                  ExecutorManager.submit {
                  Thread.ofVirtual().name("ECShoutOutThread").start {
                     it(event, args)
                  }
//                  }
               }
            }
         }
      }
   }


//    fun getFunctionsForEvent(event: Any): MutableSet<(Any, List<Any>) -> Unit> {
//        return listenerFuncs.computeIfAbsent(event) { event ->
//            val set: MutableSet<(Any, List<Any>) -> Unit> = ConcurrentHashMap.newKeySet()
//            set
//        }
//    }

   /**
    * Get the Set of Functions that have been registered for
    * the given Observer and Event.
    */
   fun getFunctionsForObserverAndEvent(observer: Any, event: Any):
         MutableSet<(Any, List<Any>) -> Unit> {
      val eventToFunc = observersToEvents.computeIfAbsent(observer) { observer ->
         val map: ConcurrentHashMap<Any, MutableSet<(Any, List<Any>) -> Unit>> = ConcurrentHashMap()
         map
      }
      val set = eventToFunc.computeIfAbsent(event) { event ->
         val set: MutableSet<(Any, List<Any>) -> Unit> = ConcurrentHashMap.newKeySet()
         set
      }
      return set
   }

   /**
    * Get the Set of Functions that have been registered for
    * the given Event and Observers.
    */
   fun getFunctionsForEventAndObserver(observer: Any, event: Any):
         MutableSet<(Any, List<Any>) -> Unit> {
      val eventToObserver = eventsToObservers.computeIfAbsent(event) { it ->
         val map: ConcurrentHashMap<Any, MutableSet<(Any, List<Any>) -> Unit>> = ConcurrentHashMap()
         map
      }
      val set = eventToObserver.computeIfAbsent(observer) { event ->
         val set: MutableSet<(Any, List<Any>) -> Unit> = ConcurrentHashMap.newKeySet()
         set
      }
      return set
   }

   fun dumpAllObservers() {
      println("Event Central All Observers by Observer")
      val entries = observersToEvents.entries
      //Go Through all the entries, since we don't have a Key.
      for (e in entries) {
         println("Observer: ${e.key}")
         //The map of Event to Functions
         val eToF = e.value
         val eToFEntries = eToF.entries
         for (eToFEntry in eToFEntries) {
            println("\tEvent: ${eToFEntry.key}")
            //get it's functions and call them
            val funcs = eToFEntry.value
            funcs.forEach {
               println("\t\tfunction: $it")
            }
         }
      }

      println("Event Central All Observers by Event")
      val byEventsEntries = eventsToObservers.entries
      byEventsEntries.forEach { entry ->
         println("Event: ${entry.key}")
         val observers = entry.value
         observers.entries.forEach { observer ->
            println("\tObserver: ${observer.key}")
            val funcs = observer.value
            funcs.forEach {
               println("\t\tfunc: $it")
            }
         }
      }
   }

   fun dumpObserverClasses() {
      println("Event Central All Observers")
      observersToEvents.asSequence().map {
         it.key::class.java.toString()
      }.sorted().forEach {
         println("Observer: ${it}")
      }
//        val entries = observers.entries
//        //Go Through all the entries, since we don't have a Key.
//        for (e in entries) {
//            println("Observer: ${e.key::class.java}")
//        }
   }

   companion object {
      enum class MyEvents {
         Event1,
         Event2,
         OtherEvent
      }

      @JvmStatic
      fun main(args: Array<String>) {

         eventCentral.addObserver(this, MyEvents.Event2) { event, args ->
            println("observer got event $event, with args: $args")
         }

         eventCentral.addObserver(this, MyEvents.Event1, MyEvents.OtherEvent) { event, args ->
            println("multiple event observer got event $event, with args: $args")
         }

         val dummyForEventer = DummyForEventer()
         dummyForEventer.registerForEvent(MyEvents.Event1)

         val x = eventCentral.eventsToObservers.get(MyEvents.Event1)
         println("Before x: $x")
         println("Before  map: ${eventCentral.eventsToObservers}")
         println("Before observerMap: ${eventCentral.observersToEvents}")

         println("Dumping")
         eventCentral.dumpAllObservers()

         eventCentral.removeObserver(this)

         println("Afterremove x: $x")
         println("Afterremove map: ${eventCentral.eventsToObservers}")
         println("Afterremove observerMap: ${eventCentral.observersToEvents}")

         println("Dumping")
         eventCentral.dumpAllObservers()

         thread(name = "ShouterOuter") {
            sleep(5500)
            eventCentral.shoutOut(MyEvents.Event2, listOf(this, MyEvents.Event2))

            sleep(5500)
            eventCentral.shoutOut(MyEvents.Event1, listOf(this, "x"))

            sleep(5000)
            eventCentral.shoutOut(MyEvents.OtherEvent, listOf(this, "y"))
         }

      }
   }
}

class DummyForEventer {
   fun registerForEvent(event: Any) {
      eventCentral.addObserver(this, event) { event, args ->
         println("DummyForEventer got event $event, with args: $args")
      }
   }
}
