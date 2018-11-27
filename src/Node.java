import java.util.*;


//Self build Node class that can hold extra content
//all names inside are self explanitory.
public class Node {
    int id;
    int X;
    int Y;
    int content;
    int smellNum;
    int breezeNum;
    boolean hasGold = false;
    boolean hasWumpus = false;
    boolean hasPit = false;
    boolean smell = false;
    boolean breeze = false;
    boolean safe = false;

    ArrayList<Node> friends = new ArrayList<>();


    

    public Node(int nodeId, int xSpot, int ySpot){
    	id = nodeId;
    	X = xSpot;
    	Y = ySpot;
    }


    
   

    
    public String toString() { return "<" + id + ">"; }
}
