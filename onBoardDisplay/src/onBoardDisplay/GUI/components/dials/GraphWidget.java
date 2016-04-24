package onBoardDisplay.GUI.components.dials;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JPanel;

import onBoardDisplay.onBoardDisplay;
import onBoardDisplay.GUI.components.Dial;
import onBoardDisplay.GUI.components.Dial.ImagePart;
import onBoardDisplay.GUI.components.Dial.Pin;
import onBoardDisplay.GUI.components.Dial.Text;
import onBoardDisplay.dataHandling.PID;

public class GraphWidget extends Dial{
	public PID[] displayedPIDs;
	private float[] times;
	private HashMap<PID,float[]> dataStore = new HashMap<PID,float[]>(){};
	public int maxMemoryRecords;
	private Color[] colors = {Color.red, Color.blue, Color.green, Color.orange, Color.pink};
	private int timeCaptionInterval = 50;
	
	public GraphWidget(PID[] pids, int startX, int startY, int realWidth, int realHeight, int maxMemory) {
		super(new PID(), startX, startY, 600, 400, realWidth, realHeight);
		displayedPIDs = pids;
		maxMemoryRecords = maxMemory;
		for (int i = 0; i< pids.length; i++) {
			dataStore.put(displayedPIDs[i],new float[]{});
		}
		
		ImagePart i1 = new ImagePart("GraphWidget_1.png",0,0,600,400,this);//Background
		ImagePart[] iArray = {i1};
		this.images = iArray;
	}
	
	public void update(float time) {
		for (PID currentPID : displayedPIDs) {
			byte[] rawBytes = onBoardDisplay.carInterface.readPID(currentPID.ID, false);//TODO consider turning autoretry on...
			float realValue = onBoardDisplay.dataHandler.decodePIDRead(rawBytes, currentPID);
			float[] oldArray = dataStore.get(currentPID);
			float[] newArray;
			float[] newTimes;
			int memoryLength;
			try {
				memoryLength = oldArray.length;
			} catch (NullPointerException e) {
				memoryLength = 0;
			}
			if (memoryLength >= maxMemoryRecords) {//If have filled up enough records, shift to lose oldest.
				newArray = new float[memoryLength];
				for (int i = 0; i < memoryLength-1; i++){
					newArray[i] = oldArray[i+1];
				}
				newArray[memoryLength-1] = realValue;
				
				newTimes = new float[memoryLength];
				for (int i = 0; i < memoryLength-1; i++){
					newTimes[i] = times[i+1];
				}
				newTimes[memoryLength-1] = time;
			} else {//If are still adding up to the max memory
				newArray = new float[memoryLength+1];
				newArray[memoryLength] = realValue;
				
				newTimes = new float[memoryLength+1];
				newTimes[memoryLength] = time;
			}
			dataStore.put(currentPID,newArray);
			times = newTimes;
		}
	}
	
	public void clear() {
		times = new float[0];
		for (int i = 0; i< displayedPIDs.length; i++) {
			dataStore.put(displayedPIDs[i],new float[]{});
		}
	}
	
	public void draw(Graphics2D g2d, JPanel panel) {
		g2d.drawImage(images[0].image,
				onBoardDisplay.ModifyAspectX(startX + (images[0].relativeX) * xMod),
				onBoardDisplay.ModifyAspectY(startY + (images[0].relativeY) * yMod),
				onBoardDisplay.ModifyAspect(images[0].relativeWidth * xMod),
				onBoardDisplay.ModifyAspect(images[0].relativeHeight * yMod),
				panel);
		float timeSpan;
		try {
			timeSpan = times[times.length-1]-times[0];
		} catch (Exception e) {
			timeSpan = 1; 
		}
		//System.out.print("Timespan:"); System.out.println(timeSpan);
		
		int length;
		try {
			length = times.length;
		} catch (Exception e) {
			length = 0;
		}
		for (int i = 0; i < displayedPIDs.length; i++) {
			PID currentPID = displayedPIDs[i];
			float pidSpan = currentPID.max-currentPID.min;
			float[] allRecords = dataStore.get(currentPID);
			g2d.setColor(colors[i]);
			for (int ii = 1; ii < length; ii++) {
				int oldTimePlot, leftPlot;
				try {
					float oldTime = times[ii-1];			oldTimePlot = (int)((600/(float)timeSpan)*(oldTime-times[0]));//oldTimePlot = (int)((600/(float)timeSpan)*(times[times.length-1]-oldTime));
					float leftEnd = allRecords[ii-1];	leftPlot = (int)((400/pidSpan)*(leftEnd-currentPID.min));
				} catch (ArrayIndexOutOfBoundsException e) {
					oldTimePlot = 0;
					leftPlot = 0;
				}
				float newTime = times[ii];		int newTimePlot = (int)((600/(float)timeSpan)*(newTime-times[0]));//int newTimePlot = (int)((600/(float)timeSpan)*(times[times.length-1]-newTime));System.out.println();
				float rightEnd = allRecords[ii];	int rightPlot = (int)((400/(pidSpan))*(rightEnd-currentPID.min));
				//System.out.print("oldTime:"); System.out.println(oldTimePlot);
				//System.out.print("newTime:"); System.out.println(newTimePlot);
				g2d.drawLine(onBoardDisplay.ModifyAspectX(startX + (oldTimePlot) * xMod),
						onBoardDisplay.ModifyAspectY(startY + (400-leftPlot) * yMod),
						onBoardDisplay.ModifyAspectX(startX + (newTimePlot) * xMod),
						onBoardDisplay.ModifyAspectY(startY + (400-rightPlot) * yMod));
				if (ii % timeCaptionInterval == 0 || ii == 1 || ii == length-1) {
					g2d.drawString((Float.toString(allRecords[ii])+currentPID.unit),startX + (newTimePlot) * xMod , startY + (400-rightPlot) * yMod);
					g2d.drawString(Float.toString(newTime/(float)1000.0), onBoardDisplay.ModifyAspectX(startX + (newTimePlot) * xMod), onBoardDisplay.ModifyAspectY(startY + (400) * yMod));
				}
			}
			g2d.drawString("Records: "+Integer.toString(length),  onBoardDisplay.ModifyAspectX(startX + (0) * xMod), onBoardDisplay.ModifyAspectY(startY + (0) * yMod));
		}
	}
}
