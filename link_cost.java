import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class link_cost {
	private int link;
	private int cost;
	
	public link_cost(int link, int cost){
		
		this.link = link;
		this.cost = cost;
	}
	
	public int getLink(){
		
		return link;
		
	}
	
	public int getCost(){
		
		return cost;
		
	}
	
	public void resetCost(int cost){
		this.cost = cost;
	}
	
	public byte[] getData(){
		
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(link);
		buffer.putInt(cost);
		return buffer.array();		
	}
	
	public static link_cost parseData(byte[] data) throws Exception{
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int link = buffer.getInt();
		int cost = buffer.getInt();
		return new link_cost(link, cost);	
	}
	
}
