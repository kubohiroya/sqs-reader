package net.sqs2.omr.app.deskew;


public class Silhouette implements Comparable<Silhouette>{
	int silhouetteIndex;
	int areaSize;
	int gx, gy;
	public Silhouette(int silhouetteIndex, int areaSize, int gx, int gy){
		this.silhouetteIndex = silhouetteIndex;
		this.areaSize = areaSize;
		this.gx = gx;
		this.gy = gy;
	}
	@Override
	public int compareTo(Silhouette o) {
		int ret = this.areaSize - o.areaSize;
		if(ret != 0){
			return ret;	
		}
		int r = this.gx - o.gx;
		if(0 < r){
			return 1;
		}else if(r < 0){
			return -1;
		}
		r = this.gy - o.gy;
		if(0 < r){
			return 1;
		}else if(r < 0){
			return -1;
		}
		return 0;
	}
	
	public int hashCode(){
		return this.silhouetteIndex;
	}
	
	public String toString(){
		return "Silhouette["+silhouetteIndex+"]=("+gx+","+gy+":"+areaSize+")";
	}
}