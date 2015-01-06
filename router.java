import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.lang.*;

public class router {
	private static final int NUM_ROUTER = 5;
	private static int routerID;
	private static String hostaddress;
	private static int nes_port;
	private static int router_port;
	private static DatagramSocket Socket;
	private static RIB[] RIBtable;
	private static link_cost[][] graph;
	private static int NBR_ROUTER = 5;
	
	private static InetAddress IPAddress;
	private static int num_links;
	
	private static ArrayList<pkt_LSPDU> lspdu_list;
	private static ArrayList<pkt_HELLO> HELLO_list;
	
	private static PrintWriter routerlog;
	
	private static link_cost[] link_list;
	
	public static void main(String[] argv) throws Exception{
		
		if (argv.length != 4){
			
			System.out.println("ERROR: incorrect number of arguements of router");
			System.exit(1);
		}
		
		routerID = Integer.parseInt (argv[0]);
		hostaddress = argv[1];
		nes_port = Integer.parseInt (argv[2]);
		router_port = Integer.parseInt (argv[3]);
		RIBtable = new RIB[NBR_ROUTER];
		graph = new link_cost[NBR_ROUTER][NBR_ROUTER];
		lspdu_list = new ArrayList<pkt_LSPDU>();
		HELLO_list = new ArrayList<pkt_HELLO>();
		
		IPAddress = InetAddress.getByName(hostaddress);
		
		Socket = new DatagramSocket(router_port);
		
		routerlog = new PrintWriter(new FileWriter(String.format("router%d.log", routerID)), true);
		
		for (int i = 0; i < NBR_ROUTER; i++){
			RIBtable[i] = new RIB(i, -1, -1, routerID, -1);
		}
		
		for (int i = 0; i < NBR_ROUTER; i++){
			for (int j = 0; j < NBR_ROUTER; j++){
				graph[i][j] = new link_cost(-1,Integer.MAX_VALUE);
			}
		}
		sendInit();
		//System.out.println("send a init msg!!!!!");
		receiveCDB();
		//System.out.println("receive a CDB!!!!!");
		sendHello();
		//System.out.println("send a HELLO!!!!!");
		receiving();
		
	}
	
	public static void sendInit() throws Exception{
        
		
		pkt_INIT packet = new pkt_INIT(routerID);
		
		DatagramPacket sendPacket =
        new DatagramPacket(packet.getData(), packet.getData().length , IPAddress, nes_port);
		
		Socket.send(sendPacket);
		routerlog.printf("R%d send an INIT: router_id %d\n", routerID, routerID);
        routerlog.flush();
		//System.out.printf("R%d sends an INIT: router_id %d\n", routerID, routerID);
	}
	
	public static void receiveCDB() throws Exception{
		
		byte[] receiveData = new byte[9999];
		DatagramPacket receivePacket =
        
        new DatagramPacket(receiveData, receiveData.length);
        
		Socket.receive(receivePacket);
		
		circuit_DB revCDB = circuit_DB.parseData(receivePacket.getData());
		processCDB(revCDB);
		
		printTopology();
	}
	
	public static void processCDB(circuit_DB cdb) throws Exception{
		
		int size = cdb.getNbr_link();
		num_links = cdb.getNbr_link();
		
		link_list = new link_cost[cdb.getNbr_link()];
		
		for (int i = 0; i < size; i++){
			link_cost lc = cdb.getonelink_cost(i);
			int cost = lc.getCost();
			int link = lc.getLink();
			link_cost one = new link_cost(link, cost);
			link_list[i] = one;
			lspdu_list.add(new pkt_LSPDU(-1,routerID,link,cost, -1));
		}
		routerlog.printf("R%d receive a CIRCUIT_DB: nbr_link %d\n", routerID, size);
        routerlog.flush();
		//System.out.printf("R%d receives a CIRCUIT_DB: nbr_link %d\n", routerID, size);
		
	}
	
	public static void sendHello() throws Exception{
        
        //System.out.println("the number of links = " + num_links);
		
		for (int i = 0; i < num_links; i ++){
			
			pkt_HELLO pH = new pkt_HELLO(routerID, link_list[i].getLink());
			byte[] pkt = pH.getData();
			int size = pkt.length;
			
			DatagramPacket sendPacket =
            new DatagramPacket(pkt,size, IPAddress, nes_port);
			
			Socket.send(sendPacket);
			
			routerlog.printf("R%d send HELLO: router_id %d, link_id %d\n",
                             routerID, routerID, link_list[i].getLink());
            
            routerlog.flush();
			
			//System.out.printf("R%d send HELLO: router_id %d, link_id %d\n",routerID, routerID, link_list[i].getLink());
		}
	}
	
	public static void receiving() throws Exception{
		
		byte[] receiveData = new byte[1024];
		
		int num = 0;
		//ArrayList<pkt_HELLO> HELLO_list;
		//HELLO_list = new ArrayList<pkt_HELLO>();
		while (true){
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			Socket.receive(receivePacket);
			
			int size = receivePacket.getLength();
			
			if (size == 8){
				
				num ++;
				pkt_HELLO packet = pkt_HELLO.parseData(receivePacket.getData());// receivePacket.
				HELLO_list.add(packet);
                ;				if (num == num_links){
					
					processHELLOmsg();
					
				}
				
			}else if(size == 20){
				//Thread.sleep(1000);
				pkt_LSPDU packet = pkt_LSPDU.parseData(receivePacket.getData());
				processLSPDUmsg(packet);
			}
		}
	}
	
	public static void processHELLOmsg() throws Exception{
		for (int index = 0 ; index < HELLO_list.size(); index ++){
			pkt_HELLO pkt = HELLO_list.get(index);
			int router_id = pkt.getRouter_id();
			int link_id = pkt.getLink_id();
            
			int size = lspdu_list.size();
			
			routerlog.printf("R%d receive HELLO: router_id %d link_id %d\n", routerID,router_id,link_id);
            routerlog.flush();
			
			//System.out.printf("R%d receives a HELLO: router_id %d link_id %d\n", routerID,router_id,link_id);
			
			for (int i = 0; i < size; i++){
                
				sendLSPDU(link_id, lspdu_list.get(i));
                
			}
            
		}
		
		
		
		
	}
	
	public static void sendLSPDU(int link_id, pkt_LSPDU pLSPDU) throws Exception{
		
		int result = pLSPDU.updatesender_via(routerID, link_id);
		byte[] pkt = pLSPDU.getData();
		int size = pkt.length;
		
		DatagramPacket sendPacket =
        new DatagramPacket(pkt,size, IPAddress, nes_port);
		
		Socket.send(sendPacket);
		routerlog.printf("R%d send LSPDU: sender %d, router_id %d, link_id %d, cost %d, via %d\n",
                         routerID,
                         pLSPDU.getSender(),
                         pLSPDU.getRouter_id(),
                         pLSPDU.getLink_id(),
                         pLSPDU.getCost(),
                         pLSPDU.getVia());
        routerlog.flush();
	}
	
	public static void processLSPDUmsg(pkt_LSPDU pLSPDU) throws Exception{
        
		
        
		for (int i = 0 ; i < lspdu_list.size(); i++){
			
			if(lspdu_list.get(i).getLink_id() == pLSPDU.getLink_id() &&
               lspdu_list.get(i).getRouter_id() == pLSPDU.getRouter_id()){
				return;
			}
		}
        routerlog.printf("R%d receive LSPDU: sender %d, router_id %d, link_id %d, cost %d, via %d\n", routerID,pLSPDU.getSender(),
                         pLSPDU.getRouter_id(), pLSPDU.getLink_id(),
                         pLSPDU.getCost(), pLSPDU.getVia());
        routerlog.flush();
        
		updataRIB(pLSPDU);
        
		
		lspdu_list.add(pLSPDU);
		
		
		for (int i = 0; i < num_links; i++){
			if (link_list[i].getLink() != pLSPDU.getLink_id()){
				
				sendLSPDU(link_list[i].getLink(), pLSPDU);
			}
		}
		
		
		
		printTopology();
		printRIB();
	}
	
	
	public static void updataRIB(pkt_LSPDU pLSPDU) throws Exception{
		
		int ld = pLSPDU.getLink_id();
		for (int index = 0; index < lspdu_list.size(); index ++){
			
			int localld = lspdu_list.get(index).getLink_id();
			
			if (ld == localld){
				int givenroutID = pLSPDU.getRouter_id();
				int localroutID = lspdu_list.get(index).getRouter_id();
				int link_id = pLSPDU.getLink_id();
				int cost = pLSPDU.getCost();
				graph[givenroutID - 1][localroutID - 1] = new link_cost(link_id, cost);
				graph[localroutID - 1][givenroutID - 1] = new link_cost(link_id, cost);
				
				
			}
		}
		//printgraph();
		DijkstraOSPF(pLSPDU);
		
	}
	
	public static void DijkstraOSPF(pkt_LSPDU pLSPDU) throws Exception{
		
		//System.out.println("the NUM_ROUTER = " + NUM_ROUTER);
		link_cost[][] new_graph;
		new_graph = new link_cost[NBR_ROUTER][NBR_ROUTER];
		
		for (int i = 0; i< NUM_ROUTER; i++){
			for (int j = 0; j < NUM_ROUTER; j++){
				
				new_graph[i][j] = new
                link_cost(graph[i][j].getLink(),graph[i][j].getCost());
			}
		}
		
		int visited[] = new int[NUM_ROUTER];
		int dist[] = new int[NUM_ROUTER];
		int prev[] = new int[NUM_ROUTER];
		//System.out.println("===========================");
		for (int i = 0; i < NUM_ROUTER; i++){
			//System.out.println("the routerID = " + routerID);
			visited[i] = 0;
			dist[i] = new_graph[routerID - 1][i].getCost();
			prev[i] = i;
			//System.out.printf("dist[ %d  ] = %d\n", i, dist[i]);
		}
		
		//System.out.println("the routerID = " + routerID);
		
		visited[routerID - 1] = 1;
		dist[routerID - 1] = 0;
		
		int k = 0;
		int start = routerID - 1;
		
		for (int i = 1; i < NUM_ROUTER; i++){
			
			int min = Integer.MAX_VALUE;
			
			for (int j = 0; j < NUM_ROUTER; j ++){
				
				int cost = new_graph[start][j].getCost();
				if (visited[j] == 0 && cost < min){
					
					min = cost;
					k = j;
					//System.out.println(" k =  " + k);
				}
			}
			//System.out.println(" k =  " + k);
			dist[k] = min;
			//System.out.printf("dist[ %d  ] = %d\n", k, dist[k]);
			visited[k] = 1;
			
			for(int j = 0; j < NUM_ROUTER; j ++){
				int cost = new_graph[k][j].getCost();
				if (visited[j] == 0 && cost != Integer.MAX_VALUE){
					if (cost + dist[k] < dist[j]){
						
						//System.out.printf("dist[ %d  ] = %d\n", k, dist[k]);
						//System.out.println("****************************");
						dist[j] = cost + dist[k];
						new_graph[start][j].resetCost(dist[j]);
						//System.out.printf("dist[ %d  ] = %d\n", j, dist[j]);
						prev[j] = prev[k];
					}
					
				}
				
			}
			
		}
		
		for (int i = 0; i < NUM_ROUTER; i++){
			int prev_index = prev[i];
			int c = new_graph[routerID - 1][prev_index].getCost();
			int l = new_graph[routerID - 1][prev_index].getLink();
			RIB one = new RIB(prev[i] + 1, l, dist[i], routerID, pLSPDU.getVia());
			RIBtable[i] = one;
		}
	}
	
	public static void printRIB(){
		
        routerlog.println("#################################");
        routerlog.println("Print RIB");
		for (int i = 0; i<NUM_ROUTER; i ++){
			
			if (RIBtable[i].cost == Integer.MAX_VALUE){
				routerlog.printf("R%d -> R%d -> INF, INF\n", routerID, i + 1);
			}else if(RIBtable[i].myRouterID == i + 1 ){
				
				routerlog.printf("R%d -> R%d -> Local, 0\n", routerID, routerID);
			}else{
				routerlog.printf("R%d -> R%d -> R%d, %d\n", routerID, i + 1, RIBtable[i].destRouterID, RIBtable[i].cost);
			}
		}
        routerlog.println("#################################");
        routerlog.flush();
	}
	
	public static void printTopology(){
        routerlog.println("#################################");
		routerlog.println("Print Topology");
		for (int i = 0; i< NUM_ROUTER; i ++){
			ArrayList<pkt_LSPDU> database = new ArrayList<pkt_LSPDU>();
			
			for (int j = 0; j < lspdu_list.size(); j++){
				
				if (lspdu_list.get(j).getRouter_id() == i + 1){
					
					database.add(lspdu_list.get(j));
				}
			}
			
			routerlog.printf("R%d -> R%d nbr link %d\n", routerID, i+1, database.size());
			
			for (int j = 0; j < database.size(); j ++){
				int link_id = database.get(j).getLink_id();
				int cost = database.get(j).getCost();
				
				routerlog.printf("R%d -> R%d link %d cost %d\n", routerID, i+1,link_id, cost);
				
			}
		}
        routerlog.println("#################################");
        routerlog.flush();
		
	}
	
	public static void printgraph(){
		
		System.out.println("==========================================");
		
		for (int i = 0; i < NUM_ROUTER; i++){
			for (int j = 0; j < NUM_ROUTER; j ++){
				
				System.out.printf("the graph[ %d ] [ %d ]: link_id %d, cost %d\n",
                                  i + 1, j + 1,
                                  graph[i][j].getLink(),
                                  graph[i][j].getCost());
			}
		}
		
		System.out.println("==========================================");
	}
}

class RIB{
	
	public int destRouterID;
	public int link_id;
	public int cost;
	public int myRouterID;
	public int path;
	
	public RIB(int destRouterID, int link_id, int cost, int myRouterID, int path){
		
		this.destRouterID = destRouterID;
		this.link_id = link_id;
		this.cost = cost;
		this.myRouterID = myRouterID;
		this.path = path;
	}
}


