package kr.khub.bitmapfun.provider;

public class ImagesFromServer {
	public String[] imageUrls;
	public String[] imageThumbUrls;
	public ImagesFromServer(int size){
		imageUrls = new String[size];
		imageThumbUrls = new String[size];
	}
}
