// Agent lm in project smart_park

/* Initial beliefs and rules */

isres(X,Y) :-  pos(reserve,X,Y).
at(A,X,Y) :-  pos(A,X,Y).

/* Initial goals */

!addpos.

/* Plans */

+!addpos : .my_name(N) <- +pos(N,3,3); !find.

+pos(N,X,Y) : grass & not at(wet,X,Y) <- mow(X,Y).

+pos(next,X,Y,N) : .my_name(N)  <- .broadcast(tell,pos(reserve,X,Y)); !checkandmove(X,Y).

+pos(reserve,X,Y)[source(N)] : .my_name(N) <- -pos(reserve,X,Y).

+!find : biggrassexists & .my_name(N) <- ?pos(N,X,Y); nextdest(X,Y).

+!find <- !find.

+!checkandmove(X,Y) : isres(X,Y)  & .my_name(N) <-.broadcast(untell,pos(reserve,X,Y)); dontMove(X,Y,N); !find.

+!checkandmove(X,Y) : not isres(X,Y) & .my_name(N) 
				<- move(X,Y,N); ?pos(N,A,B); -+pos(N,X,Y);
				.broadcast(untell,pos(reserve,X,Y)); !find.