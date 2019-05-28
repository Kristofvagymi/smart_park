// Agent visitor in project smart_park

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : .my_name(N) <- +pos(N,1,0); !move.

+!move : .my_name(N) <- ?pos(N,X,Y); randNext(X,Y).

+pos(next,X,Y,N) : .my_name(N)  <- .broadcast(tell,pos(reserve,X,Y)); !checkandmove(X,Y).

+pos(reserve,X,Y)[source(N)] : .my_name(N) <- -pos(reserve,X,Y).

+!checkandmove(X,Y) : .my_name(N) <- moveVis(X,Y); ?pos(N,A,B); -+pos(N,X,Y); .broadcast(untell, pos(reserve,X,Y)); !move.
