package util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.util.Duration;

public class MakeAnimation {
	
	public static Timeline rotateTimeline(Group group, double rate, double valFrom, double valTo, int numberOfCycle, double rotateFrom, double rotateTo){
		
		//set indefinite earth rotation in a timeline
		 Timeline rot = new Timeline();
	        rot.setCycleCount(numberOfCycle);
	        rot.setRate(rate);
	        rot.getKeyFrames().addAll(
	        		//new KeyFrame(Duration.seconds(valFrom), new KeyValue(group.rotateProperty(), rotateFrom)),
	                //new KeyFrame(Duration.seconds(valTo), new KeyValue(group.rotateProperty(), rotateTo))  
	        		new KeyFrame(Duration.millis(valFrom), new KeyValue(group.rotateProperty(), rotateFrom)),
	                new KeyFrame(Duration.millis(valTo), new KeyValue(group.rotateProperty(), rotateTo))
	        		);
	        
	        return rot;
		
	}
	
	
		
	
	
}
