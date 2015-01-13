GossipSimulator
===============

The aim of the project is to determine the convergence of the Gossip and Push-sum algorithms through a simulator based on AKKA actors forming a network of different topologies. <br>
The topologies are:<br>
1. Full Network: Every actor is a neighbor of all other actors. That is,
every actor can talk directly to any other actor.<br>
2. 2D Grid: Actors form a 2D grid. The actors can only talk to the grid
neigbors.<br>
3. Line: Actors are arranged in a line. Each actor has only 2 neighbors
(one left and one right, unless you are the first or last actor).<br>
4. Imperfect 2D Grid: Grid arrangement but one random other neighbor
is selected from the list of all actors (4+1 neighbors).<br>
___________________________________________________________________________________________________________________________

File structure:
===============
<pre>
---build.sbt<br>
--+src<br>
    |<br>
    |-+main<br>
    	|<br>
    	|-+scala<br>
    		|-gossip.scala<br>
    	|-+resources<br>
    		|-application.conf<br>
</pre>
____________________________________________________________________________________
NOTE: CHANGE THE IP ADDRESS OF THE MACHINE IN application.conf

To Run :
========
local machine:
$ sbt<br>
\> compile<br>
\> run \<Number of nodes\> \<Topology\> \<Algorithm\><br>

or scala bitCoinMiner \<Number of nodes\> \<Topology\> \<Algorithm\>

Topologies:<br>
1. line<br>
2. full<br>
3. 2d<br>
4. imp2d

Algorithms:<br>
1. gossip<br>
2. push-sum<br>
______________________________________________________________________________________
