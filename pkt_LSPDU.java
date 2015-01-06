import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class pkt_LSPDU {
	private int sender;
	private int router_id;
	private int link_id;
	private int cost;
	private int via;
	
	public pkt_LSPDU(int sender,int router, int link, int cost, int via){
		
		this.sender = sender;
		router_id = router;
		link_id = link;
		this.cost = cost;
		this.via = via;
	}
	
	public int getSender(){
		return sender;
	}
	
	public int getCost(){
		return cost;
	}
	
	public int getVia(){
		return via;
	}
	
	public int getRouter_id(){
		return router_id;
	}
	
	public int getLink_id(){
		
		return link_id;
		
	}
	
	public int updatesender_via(int sender, int via){
		
		this.sender = sender;
		this.via = via;
		
		return 0;
		
	}
	
	public byte[] getData(){
		
		ByteBuffer buffer = ByteBuffer.allocate(20);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(sender);
		buffer.putInt(router_id);
        buffer.putInt(link_id);
        buffer.putInt(cost);
        buffer.putInt(via);
		return buffer.array();		
	}
	
	public static pkt_LSPDU parseData(byte[] data) throws Exception{
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int sender = buffer.getInt();
		int router_id = buffer.getInt();
		int link_id = buffer.getInt();
		int cost = buffer.getInt();
		int via = buffer.getInt();
		return new pkt_LSPDU(sender,router_id,link_id, cost, via);		
	}
}
