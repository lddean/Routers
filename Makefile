JCC = javac

JFLAGS =  #-g

default: router pkt_LSPDU pkt_INIT pkt_HELLO link_cost circuit_DB

#name dependencies
router: router.java
	$(JCC) $(JFLAGS) router.java		
pkt_LSPDU: pkt_LSPDU.java
	$(JCC) $(JFLAGS) pkt_LSPDU.java
pkt_INIT: pkt_INIT.java
	$(JCC) $(JFLAGS) pkt_INIT.java	
pkt_HELLO: pkt_INIT.java
	$(JCC) $(JFLAGS) pkt_HELLO.java
link_cost: link_cost.java
	$(JCC) $(JFLAGS) link_cost.java
circuit_DB: circuit_DB.java
	$(JCC) $(JFLAGS) circuit_DB.java	

clean:
	rm *.class *~ *#* 



