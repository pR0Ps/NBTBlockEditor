
public class Point3D {
	private int x, y, z;
	
	public Point3D (int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3D (){}
	
	public void setX(int x){
		this.x = x;
	}
	
	public void setY(int y){
		this.y = y;
	}
	
	public void setZ(int z){
		this.z = z;
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}
	
	public int getZ(){
		return this.z;
	}
	
	public Point3D clone(){
		return new Point3D (this.x, this.y, this.z);
	}
}
