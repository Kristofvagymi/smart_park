// Agent sprinkler in project smart_park

/* Initial beliefs and rules */

isres(X,Y) :-  pos(reserve,X,Y).
/* Initial goals */

!find.

/* Plans */


+!find : noteverythingwet <- findgrass.

+!find <- !find.

+sprinkle(X,Y) <- .broadcast(tell,reserve(X,Y)); !checkandsprinkle(X,Y).

+!checkandsprinkle(X,Y) : isres(X,Y) <- .broadcast(untell,reserve(X,Y)); !find.

+!checkandsprinkle(X,Y) : not isres(X,Y) <- sprinkle(X,Y); .broadcast(untell,reserve(X,Y)); !find.