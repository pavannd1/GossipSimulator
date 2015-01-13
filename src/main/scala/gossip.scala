import akka.actor._
import Array._
import util.control.Breaks._

case class Assign(finsiher:Int)

case class pushSum(s:Double,w:Double)

object allActors{
	var localActor: Array[ActorRef] = new Array[ActorRef](1)
}

object distribAlgo {
	def main(args: Array[String])
	{
		var numOfNodes=0;
		var topology="";
		var algorithm="";
		if(args.length > 0) {
			numOfNodes=args(0).toInt;
			topology=args(1);
			algorithm=args(2);
			var top = createTopology(numOfNodes, topology);
			//Print Topology Adjacancy Matrix
			/*for(i <- 0 to top.length-1) {
            	for(j <- 0 to top.length-1) {
                	print(top(i)(j));
                }
                println(" ");
            }*/
			val system = ActorSystem("DistributionMonitor")
			val boss = system.actorOf(Props(new BigBoss(top,algorithm)), name = "BigBoss")
			numOfNodes = top.length;
			println("Number of nodes: "+numOfNodes);
			system.awaitTermination()
		} else {
			println("Enter parameters and retry");
			println("project2.scala numNodes topology algorithm");
		}
	}

	//::: TOPOLOGIES :::
	def createTopology( nodes:Int, a:String ) : Array[Array[Int]] = {
			var top = ofDim[Int](nodes,nodes)
			print("Topology: ");
			a match {
			case "full"=>
				println("Full");
				for(i <- 0 to top.length-1) {
					for(j <- 0 to top.length-1) {
						top(i)(j)=1;
					}
				}
			case "2D"=>
				println("2D");
				top = create2D(nodes);
			case "line"=>
				top = createLineTop(nodes);
				println("Line");
			case "imp2D"=>
				top = createImp2DTop(nodes);
				println("Imperfect 2D");
			case _=>
				println("Did not recognize topology");
			}
			return top
	}

	def createLineTop(nodes:Int) : Array[Array[Int]] = {
			var top = ofDim[Int](nodes,nodes)
			var j = nodes;
			var k = 1;
			var helper = nodes - 1;
			for(i <- 0 to top.length - nodes - 1) {
				top(i)(j) = 1;
				top(j)(i) = 1;
				j+=1;
			}
			for(i <- 0 to top.length - 1) {
				breakable {
					if(helper > 0) {
						top(i)(k) = 1;
						top(k)(i) = 1;
						k+=1;
						helper-=1;
					} else {
						k+=1;
						helper = nodes - 1;
						break
					}
				}
			}
			return top;
	}

	def create2D(nodes:Int) : Array[Array[Int]] = {
			var top = ofDim[Int](nodes,nodes)
			var n_of_actors = nodes;
			var y=2;
			while( n_of_actors-1 >= y*y) {
				y+=1;
			}
			n_of_actors = y*y;
			var (size: Int)=y;
			var m = ofDim[Int](size,size);
			var (i,j) = Pair(0,0);
			var (start,end) = Pair (0,m.length);
			var count = (size*size)-1;
			//println("count is "+count);
			for(p <- m.length to 0 by -2) {
				breakable {
					if(p==m.length) {
						start=0;
						end=m.length;
					} else if(p==1) {
						j+=1;
						i+=1;
						m(i)(j)=count;
						count-=1;
						break
					} else {
						j+=1;
						i+=1;
						start=i;
						end-=1;
					}
					while(j < end) {
						m(i)(j)=count;
						count-=1;
						j+=1;
					}
					j-=1;
					i+=1;
					while(i < end) {
						m(i)(j)=count;
						count-=1;
						i+=1;
					}
					i-=1;
					j-=1;
					while(j>=start) {
						m(i)(j)=count;
						count-=1;
						j-=1;
					}
					j+=1;
					i-=1;
					while(i>start) {
						m(i)(j)=count;
						count-=1;
						i-=1;
					}
				}
			}
			top = ofDim[Int](n_of_actors,n_of_actors)
					for(k <- 0 to m.length-1) {
						for(s <- 0 to m.length-1) {
							if(m(k)(s) < n_of_actors) {
								//top
								if(k-1 < m.length && k-1>=0) {
									if(m(k-1)(s) < top.length)
										top(m(k)(s))(m(k-1)(s))=1;
								}
								//left
								if(s-1 < m.length && s-1>=0) {
									if(m(k)(s-1) < top.length)
										top(m(k)(s))(m(k)(s-1))=1;
								}
								//right
								if(s+1 < m.length && s+1>=0) {
									if(m(k)(s+1) < top.length)
										top(m(k)(s))(m(k)(s+1))=1;
								}
								//bottom
								if(k+1 < m.length && k+1 >= 0) {
									if(m(k+1)(s) < top.length)
										top(m(k)(s))(m(k+1)(s))=1;
								}
							}
						}
					}
			return top
	}

	def createImp2DTop(nodes:Int) : Array[Array[Int]] = {
			var top = create2D(nodes);
			var randomNum = 0;
			for(i <- 0 to top.length-1) {
				do {
					randomNum = util.Random.nextInt(top.length)
				}
				while(top(i)(randomNum) != 0);
				top(i)(randomNum) = 1;
			}
			return top
	}
}

//Workers
class Node(actorNumber: Int,ngh: Array[Array[Int]],boss: ActorRef) extends Actor {
	var pcount = 0;
	var s:Double = actorNumber;
	var w:Double = 1;
	var ratio:Double = s/w;
	var count: Int = 0;
	var total = ngh.length;
	var nbrs: Array[Int] = new Array[Int](total);
	var c: Int = -1;
	var nalive = 0;

	for(i <- 0 to total-1) {
		if(ngh(actorNumber)(i) == 1) {
			c+=1;
			nbrs(c)=i;
		}
	}
	var n: Array[Int] = new Array[Int](c+1);
	for(i <- 0 to c) {
		//println("Actor "+actorNumber+": "+nbrs(i));
		n(i)=nbrs(i);
	}
	nalive = c;
	//println("Created Actor "+actorNumber);
	def receive = {
	case "gossipMsg" =>
		count+=1;
		//println("Actor Number "+actorNumber+" has received the gossip message ("+count+")");
		if(count >=10) {
			println("Actor ("+actorNumber+") terminating. Received Count: "+count);
			for(i <- 0 to c) {
				this.synchronized {
					if(n(i)!= -1) {
						allActors.localActor(n(i)) ! "gossipMsg"
						allActors.localActor(n(i)) ! Assign(actorNumber)
					}
				}
			}

			boss ! "finish"
			context.stop(self)
		} else if(count==1) {
			boss ! "rxMsg"
			allActors.localActor(actorNumber) ! "self"
		}
	case "self" =>
		//Send message to neighbours
		var randomNum = 0;
		if(c > 0) {
			//println("Selecting Random number");
			randomNum=util.Random.nextInt(c+1)
		}
		//println("Actor Number "+actorNumber+" sending msg to "+ " > "+n(randomNum));
		this.synchronized {
			if(n(randomNum)!= -1) {
				allActors.localActor(n(randomNum)) ! "gossipMsg"
			}
		}
		allActors.localActor(actorNumber) ! "self"
		if(nalive < 0) {
			println("Actor ("+actorNumber+") terminating. Received Count: "+count);
			boss ! "finish"
			context.stop(self)
		}
	case Assign(key)=>
		for(i <- 0 to c) {
			if(key==n(i)) {
				this.synchronized {
					n(i) = -1;
				}
			}
		}
		nalive-=1;
	case "startPushSum" =>
		//Send push-sum message to neighbours
		var randomNum = 0;
		if(c > 0) {
			randomNum=util.Random.nextInt(c+1)
		}
		this.synchronized {
			if(n(randomNum)!= -1) {
				s=s/2;
				w=w/2;
				allActors.localActor(n(randomNum)) ! pushSum(s,w)
			}
		}
		if(nalive < 0) {
			println("Actor ("+actorNumber+") terminating. Ratio: "+ratio);
			boss ! "donePushSum"
			context.stop(self)
		}
		allActors.localActor(actorNumber) ! "startPushSum"
	case pushSum(si,wi) =>
		s = si + s;
		w = wi + w;
		val newratio = s/w;
		//println("Actor("+actorNumber+") ratio="+ratio+"  * newratio="+newratio);
		if(Math.abs(newratio - ratio) <= 1e-10) {
			pcount+=1;
			if(pcount==3) {
				for(i <- 0 to c) {
					this.synchronized {
						if(n(i)!= -1) {
							allActors.localActor(n(i)) ! pushSum(s,w)
							allActors.localActor(n(i)) ! Assign(actorNumber)
						}
					}
				}
				println("Actor ("+actorNumber+") terminating. Ratio: "+newratio);
				boss ! "donePushSum"
				context.stop(self)
			}
		} else {
			pcount = 0;
		}
		ratio = newratio;
		allActors.localActor(actorNumber) ! "startPushSum"
	}
}

class BigBoss(ngh: Array[Array[Int]],algorithm: String) extends Actor {
	var total = ngh.length;
	var ttl = total;
	var rxd = 0;
	var randomNum=util.Random.nextInt(total)
	allActors.localActor = new Array[ActorRef](ttl)
	//Siphon Workers
	for(i <- 0 to ttl-1) {
		allActors.localActor(i) = context.actorOf(Props(new Node(i,ngh,self)), name = ("LocalActor"+i))
	}
	var startTime = System.currentTimeMillis;
	algorithm match {
		case "gossip" =>
			allActors.localActor(randomNum) ! "gossipMsg"
		case "push-sum" =>
			allActors.localActor(randomNum) ! "startPushSum"
	}
	def receive = {
		case "rxMsg" =>
			rxd+=1;
			if(rxd>=total) {
				var endTime = System.currentTimeMillis;
				var convergenceTime = endTime - startTime;
				println("Gossip Convergence Time "+ convergenceTime+"ms");
				context.system.shutdown()
			}
		case "finish" =>
			ttl-=1;
			if(ttl<=0) {
				context.system.shutdown()
			}
		case "donePushSum" =>
			ttl-=1;
			if(ttl<=0) {
				var endTime = System.currentTimeMillis;
				var convergenceTime = endTime - startTime;
				println("Push-Sum Convergence Time "+ convergenceTime+"ms");
				context.system.shutdown()
			}
	}
}
