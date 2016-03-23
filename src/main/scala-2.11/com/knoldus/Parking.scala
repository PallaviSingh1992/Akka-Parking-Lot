package com.knoldus
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import akka.actor.{Props, ActorSystem, Actor}
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask


class Attendant extends Actor {

  implicit val timeout = Timeout(5.seconds)

  def receive = {
    case "Request Parking" =>
      val slot = Await.result((Parking.monitor ? "Find Slot").mapTo[Int], 5.seconds)
      slot match{
        case res if res != -1=> println(s"Parking is Available at $res")
        case _ => println("Regret, Parking is Full")
      }
  }

}

class SlotMonitor extends Actor {
  def receive = {
    case "Find Slot" => sender ! Parking.allotParkingSlot
  }
}

class Vehicle extends Actor {

  def receive = {
    case "Find Parking" => Parking.attendant ! "Request Parking"
    case locId => { Parking.deallotParkingSlot(locId.toString)}
  }
}


object Parking {

  //List of Parking Slots
  val listOfSlots:ListBuffer[Int]=ListBuffer(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)

  def findParkingSlots(list:ListBuffer[Int]):Int={
    val parkingSlot=list.indexOf(0)
    parkingSlot match {
      case res if(res>=0)=> {
                              list(res) = 1
                              parkingSlot+1
                            }
      case -1 => parkingSlot
      }
    }//Closing Brace of findParkingSlots

   def allotParkingSlot:Int=findParkingSlots(listOfSlots)

   def deallotParkingSlot(locId:String):Boolean={

     val index=locId.charAt(locId.length-1).asDigit
     index match{
       case res if res>=0=>{
                               listOfSlots(res)=0
                               println("Car has vacated location " +locId )
                               true
                            }
       case _ => false
     }
   }//Closing of deallotParkingSlot

  val system = ActorSystem("Parking")
  val vehicle = system.actorOf(Props[Vehicle], "Vehicle")
  val attendant = system.actorOf(Props[Attendant], "Attendant")
  val monitor = system.actorOf(Props[SlotMonitor], "SlotMonitor")

  def main(args: Array[String]) {
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    Thread.sleep(100)
    vehicle ! "locId 4"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    vehicle ! "Find Parking"
    Thread.sleep(100)
    vehicle ! "locId 12"
    Thread.sleep(100)
    vehicle ! "locId 3"


    system.terminate()
  }


}











