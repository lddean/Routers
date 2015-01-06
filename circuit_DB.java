import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class circuit_DB {
	private int nbr_link;
	private link_cost[] linkcost;
	
	public circuit_DB(int nbr, link_cost[] linkcost){
		
		nbr_link = nbr;
		this.linkcost = linkcost;
		
	}
	
	public int getNbr_link(){
		
		return nbr_link;
		
	}
	
	public link_cost[] getLinkcost(){
		
		return linkcost;
		
	}
	
	public link_cost getonelink_cost(int i){
		
		return linkcost[i];
		
	}
	
	public byte[] getData(){
		
		ByteBuffer buffer = ByteBuffer.allocate(nbr_link * 8 + 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(nbr_link);
		for(int i = 0; i < nbr_link; i++){
			
			buffer.putInt(linkcost[i].getLink());
			buffer.putInt(linkcost[i].getCost());
		}
		return buffer.array();		
	}
	
	public static circuit_DB parseData(byte[] data) throws Exception{
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int nbr_link = buffer.getInt();
		
		link_cost[] linkcost = new link_cost[nbr_link];
		
		for(int i = 0; i < nbr_link; i ++){
			
			int link = buffer.getInt();
			int cost = buffer.getInt();
			
			linkcost[i] = new link_cost(link, cost);
		}
		
		return new circuit_DB(nbr_link, linkcost);	
	}
}
