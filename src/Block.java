import java.awt.Image;


public class Block {
	private Point3D pos = null;
	private String id = null;
	private String data = null;

	public Block (Point3D pos, String id_hex, String data){
		this.pos = pos.clone();
		this.id = new String(id_hex);
		this.data = new String (data);
	}

	public Block (String id_hex){
		this.id = new String(id_hex);
	}
	
	public Block (String id_hex, String data){
		this.id = new String(id_hex);
		this.data = new String (data);
	}
	
	public Point3D getPos(){
		if (pos != null){
			return pos.clone();
		}
		return null;
	}
	
	public void setPos(Point3D pos){
		this.pos = pos.clone();
	}
	
	public void setData(String data){
		this.data = new String (data);
	}

	public String getHexID(){
		return new String(this.id);
	}
	
	public String getData(){
		return this.data;
	}

	public int getDecID(){
		return BlockData.toDec(this.id);
	}

	public Image getTile(){
		return BlockData.getImage(id);
	}
}
