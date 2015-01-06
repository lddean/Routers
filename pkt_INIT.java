import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class pkt_INIT {
	private int router_id;
	
	public pkt_INIT(int router){
		
		router_id = router;
		
	}
	
	public int getRouter_id(){
		return router_id;
	}
	
	
	public byte[] getData(){
		
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(router_id);
		return buffer.array();		
	}
	
	public static pkt_INIT parseData(byte[] data) throws Exception{
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int router_id = buffer.getInt();
		return new pkt_INIT(router_id);	
		
	}
}
