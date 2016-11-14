
public class DataExchange extends Thread{

	private boolean obstacleDetected = false,stop = false, followLeftSide = true;
		
	private float color = 0, distance = 0, rate=0, middleColor = 0;
	float[] RGB = new float[3]; //Red = 0, Green = 1, Blue = 2
	
	private int time = 0;
	
	public DataExchange(){
		
	}

	public void setObstacleDetected(boolean status){
		obstacleDetected = status;
	}
	public boolean getObstacleDetected(){
		return obstacleDetected;
	}
	
	public void setColor(float newColor){
		color = newColor;
	}
	
	public float getColor(){
		return color;
	}
	
	public void SetDistance(float newDistance){
		distance = newDistance;
	}
	
	public float GetDistance(){
		return distance;
	}
	
	public void setStop(boolean bool){
		stop = bool;
	}
	
	public boolean getStop(){
		return stop;
	}
	
	public void FollowLeftSide(boolean bool){
		followLeftSide = bool;
	}
	public boolean GetFollow(){
		return followLeftSide;
	}
	
	public void AddRate(float newRate){
		rate = rate + newRate;
	}
	
	public float GetRate(){
		return rate;
	}
	
	public void IncreaseTime(){
		time++;
	}
	
	public int GetTime(){
		return time;
	}
	
	public void ResetTime(){
		time = 0;
	}
	
	public void SetMiddleColor(double newMiddle){
		middleColor = (float)newMiddle;
	}
	
	public float GetMiddle(){
		return middleColor;
	}
	
	public void SetRGB(float[] newRGB){
		/*
		RGB[0] = newRGB[0];
		RGB[1] = newRGB[1];
		RGB[2] = newRGB[2];
		*/
		for(int i=0;i<3;i++){
			RGB[i] = newRGB[i];
		}
	}
	
	public void SetRed(float newRed){
		RGB[0] = newRed;
	}
	
	public void SetGreen(float newGreen){
		RGB[1] = newGreen;
	}
	
	public void SetBlue(float newBlue){
		RGB[2] = newBlue;
	}
	
	public float GetRed(){
		return RGB[0];
	}
	
	public float GetGreen(){
		return RGB[1];
	}
	
	public float GetBlue(){
		return RGB[2];
	}
}
