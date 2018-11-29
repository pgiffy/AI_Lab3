import java.util.*;
import java.io.*;

public class Main {
	
	/*Notes Section:
	 * Done by Peter Gifford and Sam Behrens
	 * 
	 * Definitions:
	 *
	 * 
	 * */
    static boolean wumpusAlive = true;
	public static void main(String[] args) {
        int size = 4;
        Node[][] maze = new Node[size][size];
        Random rand = new Random();
        int iter = 0;
        int startId = -1;
        ArrayList<Node> nodes = new ArrayList<>();
        Node playerNode = null;

        for (int i = 0; i < size; i++) { // write the final solution to output.txt
            for (int j = 0; j < size; j++) {
                maze[i][j] = new Node(iter, j, i);
                nodes.add(maze[i][j]);
                if(i == size-1 && j == 0) {
                    startId = maze[i][j].id;
                    playerNode = maze[i][j];
                }
                iter++;
            }
        }
        
        int goldPlace = rand.nextInt(size * size);
        int wumpusPlace = rand.nextInt(size * size);
        while(goldPlace == startId || wumpusPlace == startId) {
            goldPlace = rand.nextInt(size * size);
            wumpusPlace = rand.nextInt(size * size);
        }

        for (int X = 0; X < size; X++) {
            for (int Y = 0; Y < size; Y++) {
                Node current = maze[X][Y];
                //checking all cardinal directions since can't move diagonal
                //so i know this is dumb but its the fastest thing to type and think of at the moment so change it if you want
                try {
                    current.friends.add(maze[X+1][Y]);
                    current.friends.add(maze[X][Y+1]);
                    current.friends.add(maze[X-1][Y]);
                    current.friends.add(maze[X][Y-1]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    try {
                        current.friends.add(maze[X][Y+1]);
                        current.friends.add(maze[X-1][Y]);
                        current.friends.add(maze[X][Y-1]);
                    } catch(ArrayIndexOutOfBoundsException r) {
                        try {
                            current.friends.add(maze[X-1][Y]);
                            current.friends.add(maze[X][Y-1]);
                        } catch(ArrayIndexOutOfBoundsException t) {
                            try {
                                current.friends.add(maze[X][Y-1]);
                            } catch(ArrayIndexOutOfBoundsException u) {

                            }
                        }
                    }
                }
                if(maze[X][Y].id == goldPlace){
                    maze[X][Y].hasGold = true;
                }
                if(maze[X][Y].id == wumpusPlace){
                    maze[X][Y].hasWumpus = true;
                }
            }
        }


        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++){
                if(i == size-1 && j == 0 || maze[i][j].hasGold == true){// the player in the bottom right and gold cannot have a pit
                    continue;
                }else{
                    if(rand.nextInt(10) < 2) { //First 20% for the pit
                        maze[i][j].hasPit = true;
                    }
                }
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if(maze[i][j].hasPit){
                    for(Node n : maze[i][j].friends){
                        n.breeze = true;
                    }
                }
                if(maze[i][j].hasWumpus){
                    for(Node n : maze[i][j].friends){
                        n.smell = true;
                    }
                }
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if(maze[i][j].hasGold) {
                    System.out.print("G");
                }
                else if(maze[i][j].hasWumpus) {
                    System.out.print("W");
                }
                else if(maze[i][j].hasPit) {
                    System.out.print("P");
                }
                else if(maze[i][j].smell) {
                    System.out.print("S");
                }
                else if(maze[i][j].breeze) {
                    System.out.print("B");
                }else{
                    System.out.print("_");
                }
            }
            System.out.println();
        }
        
        /*
         * Start of solution
         */
        

        int steps = 0;
        boolean collectedGold = false;
        
        while (true) {
        	if (playerNode.hasPit || (playerNode.hasWumpus && wumpusAlive)) { // check if we are dead
        		break;
        	}
        	playerNode.safe = true; // set the current spot to safe
        	
        	if (playerNode.hasGold == true) { // if space has gold then we collect it
        		collectedGold = true;
        	}
        	
        	/*
        	 * This big boy is updating the surrounding areas with their probablility of being mean
        	 */
        	
        	if (playerNode.breeze) {
        		for (Node spot : playerNode.friends) {
        			if (!spot.safe) {
        				spot.pitNum++;
        			}
        		}
        	} else {
        		for (Node spot : playerNode.friends) {
        			if (!spot.safe) {
        				spot.pitNum--;
        			}
        		}
        	}
        	if (playerNode.smell) {
        		for (Node spot : playerNode.friends) {
        			if (!spot.safe) {
        				spot.wumpusNum++;
        			}
        		}
        	} else {
        		for (Node spot : playerNode.friends) {
        			if (!spot.safe) {
        				spot.wumpusNum--;
        			}
        		}
        	}
        	
        	/*
        	 * end of big boy
        	 */
        	
        	for (Node spot : playerNode.friends) { // if there is no chance of pit or wumpus, go to that spot
        		if (spot.wumpusNum <= 0 && spot.pitNum <= 0) {
        			playerNode = spot;
        			break;
        		} else if(){ //check if there is findable zero
        		    //set player spot
                } else { //search for best number and move there.

                }
        	}


        	
        	steps++;
        }
    }

    public static void killWumpus(Node current){ // checks friends and shoots the one with the highest wumpus number
	    Node toKill = null;
	    for(Node n: current.friends){
	        if(toKill == null){
	            toKill = n;
            }else if(n.wumpusNum > toKill.wumpusNum){
	            toKill = n;
            }
        }
        if(toKill.hasWumpus){//checks for if wumpus "screams"/dies
	        toKill.hasWumpus = false;
	        wumpusAlive = false;
        }
    }

	
	public static int breadthFirst(Node start) { // remember to wipe all of the tails 
    	LinkedList<Node> queue = new LinkedList<>();
    	queue.add(start);
    	Node current;
    	while(!queue.isEmpty()) {
    		current = queue.poll();
    		if(current.visited == true) {
    			continue;
    		}
    		if(current.hasGold) { 
    			
    			return current.id;
    		}
    		current.visited = true;
    		current.tail.add(current);
    		for(Node n : current.friends) {
    			if(n.tail.size() <= current.tail.size() && n.tail.size() != 0) { continue; } // has to here to remove possiblity of two equal length lines
    			n.tail.addAll(removeDuplicates(current.tail));
    			queue.add(n);
    		}
    		
    	}
    	return -1;
    }
	
	
	public static ArrayList<Node> removeDuplicates(ArrayList<Node> remove){
    	Set<Node> temp = new HashSet<>();
    	temp.addAll(remove);
    	remove.clear();
    	remove.addAll(temp);
    	return remove;
    	
    }
}
