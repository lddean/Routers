import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class pkt_HELLO {
	
	private int router_id;
	private int link_id;
	
	public pkt_HELLO(int router, int link){
		
		router_id = router;
		link_id = link;
	}
	
	public int getRouter_id(){
		return router_id;
	}
	
	public int getLink_id(){
		
		return link_id;
		
	}
	
	public byte[] getData(){
		
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(router_id);
        buffer.putInt(link_id);
		return buffer.array();		
	}
	
	public static pkt_HELLO parseData(byte[] data) throws Exception{
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int router_id = buffer.getInt();
		int link_id = buffer.getInt();
		return new pkt_HELLO(router_id, link_id);		
	}
}
