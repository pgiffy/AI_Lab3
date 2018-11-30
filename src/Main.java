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
	static ArrayList<Node> nodes = new ArrayList<>();
    static boolean wumpusAlive = true;
    static boolean arrow = true;
	public static void main(String[] args) {
        int size = 5;
        Node[][] maze = new Node[size][size];
        Random rand = new Random();
        int iter = 0;
        int startId = -1;
        Node playerNode = null;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                maze[i][j] = new Node(iter, j, i);
                nodes.add(maze[i][j]);
                if(i == size-1 && j == 0) {
                    startId = maze[i][j].id;
                    maze[i][j].start = true;
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
        
        ArrayList<Node> badSquares = new ArrayList<>();
        int steps = 0;
        int points = 0;
        boolean collectedGold = false;
        boolean killChecker = false;
        while (true) {
            System.out.println(playerNode.id + " , CSteps: " + steps);

            playerNode.safe = true;
        	if (playerNode.hasPit || (playerNode.hasWumpus && wumpusAlive)) { // check if we are dead
                points -= 1000;
                points -= steps;
                System.out.println("Dead");
                System.out.println("Steps: " + steps);
                System.out.println("Points: " + points);
        		break;
        	}
        	playerNode.safe = true; // set the current spot to safe
        	
        	if (playerNode.hasGold == true) { // if space has gold then we collect it
        		collectedGold = true;
        		steps += 1; // action of picking it up
        	}
        	
        	/*
        	 * This big boy is updating the surrounding areas with their probablility of being mean
        	 */
        	
        	if (playerNode.breeze) {
        		for (Node spot : playerNode.friends) {
        			if (!spot.safe) {
        				spot.pitNum++;
        				if(!badSquares.contains(spot)){ // adding to list of nodes to search
        				    badSquares.add(spot);
                        }
        			}
        		}
        	} else {
        		for (Node spot : playerNode.friends) {
        			if (!spot.safe) {
        				spot.pitNum--;
                        if(badSquares.contains(spot) && spot.wumpusNum <= 0 && spot.pitNum <= 0){ // removing if it is now safe
                            badSquares.remove(spot);
                        }
        			}
        		}
        	}
        	if (playerNode.smell) {
        		for (Node spot : playerNode.friends) {
        			if (!spot.safe) {
        				spot.wumpusNum++;
                        if(!badSquares.contains(spot)){ // adding to list of nodes to search
                            badSquares.add(spot);
                        }
        			}
        		}
        	} else {
        		for (Node spot : playerNode.friends) {
        			if (!spot.safe) {
        				spot.wumpusNum--;
                        if(badSquares.contains(spot) && spot.wumpusNum <= 0 && spot.pitNum <= 0){ // removing if it is now safe
                            badSquares.remove(spot);
                        }
        			}
        		}
        	}
        	
        	/*
        	 * end of big boy
        	 */
        	boolean checker = false;

        	if(collectedGold){ // checking if the player has gold
        	    //search back to start
                breadthFirstEnd(playerNode);//used to establish steps
                steps += nodes.get(startId).tail.size(); // adds tail length to steps
                points += 1000;
                killChecker = true; // ends game
            }else {

                for (Node spot : playerNode.friends) { // if there is no chance of pit or wumpus, go to that spot
                    if (spot.wumpusNum <= 0 && spot.pitNum <= 0 && !spot.safe) {
                        playerNode = spot;
                        checker = true;
                        steps++;
                        break;
                    }
                }
                if (!checker) { // has to skip if it moves one spot
                    int temp = breadthFirstZero(playerNode);
                    if (temp != -1) { //check if there is findable zero
                        playerNode = nodes.get(temp); //set player spot
                        steps += playerNode.tail.size();
                    } else { //search for best number and move there.

                        if (findSafestSquare(badSquares).wumpusNum > 0 && arrow) { //checks if best option is wumpus possible to run kill wumpus unless there is no arrow
                            System.out.println("No Zero Attemp Wump kill");
                            Node past = playerNode;
                            playerNode = findSafestSquare(badSquares);
                            breadthFirstNoZero(past, playerNode);
                            badSquares.remove(playerNode);
                            points-=10;
                            steps += playerNode.tail.size();
                            killWumpus(playerNode);
                        } else { // goes to spot without killing
                            System.out.println("No Zero No Wump attempt");
                            Node past = playerNode;
                            playerNode = findSafestSquare(badSquares);
                            breadthFirstNoZero(past, playerNode);
                            badSquares.remove(playerNode);
                            steps += playerNode.tail.size();
                        }


                    }
                }
            }
            if(killChecker){
                System.out.println("Won");
                System.out.println("Steps: " + steps);
                points -= steps;
                System.out.println("Points: " + points);
                break;
            }

        }
    }

    public static Node findSafestSquare(ArrayList<Node> toCheck){ // returns id of safest square in list
        Node best = null;
        for(Node n : toCheck){
            if(best == null){
                best = n;
            }else{
                if(best.wumpusNum + best.pitNum > n.wumpusNum + n.pitNum) { //prioritizes total score
                    best = n;
                }else if(best.wumpusNum + best.pitNum == n.wumpusNum + n.pitNum){ // if total score is equal use seconds if to choose the one with the wumpus ore likely in it
                    if(best.wumpusNum < n.wumpusNum){ // want higher wumpus num because we can kill it
                        best = n;
                    }
                }
            }
        }
	    return best;
    }


    public static void killWumpus(Node toKill){ // checks if space being moved to is a wumpus to kill is
        if(toKill.hasWumpus && arrow){//checks for if wumpus "screams"/dies
	        toKill.hasWumpus = false;
	        wumpusAlive = false;
	        arrow = false;
	        System.out.println("Wumpus Screams");
	        wumpusWumps();
        } else{
            System.out.println("Failed wumpus kill");
        }

        arrow = false;
    }

	
	public static int breadthFirstZero(Node start) { // breadth first searched for nearest zero spot
        System.out.println("Finding 0");
        clearTails();
        clearVisited();
    	LinkedList<Node> queue = new LinkedList<>();
    	queue.add(start);
    	Node current;
    	while(!queue.isEmpty()) {
    		current = queue.poll();
    		if(current.visited == true) {
    			continue;
    		}
    		if(current.wumpusNum <= 0 && current.pitNum <= 0 && !current.safe) {
    			return current.id;
    		}
    		current.visited = true;
    		current.tail.add(current);
    		for(Node n : current.friends) {
    			if((n.tail.size() <= current.tail.size() && n.tail.size() != 0) || (n.wumpusNum > 0 || current.pitNum > 0)) { continue; } // has to here to remove possiblity of two equal length lines and if it is a safe space
    			n.tail.addAll(removeDuplicates(current.tail));
    			queue.add(n);
    		}
    		
    	}
    	return -1;
    }

    public static int breadthFirstNoZero(Node start, Node end) { // breadth first searched for nearest zero spot
        clearTails();
        clearVisited();
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(start);
        Node current;
        while(!queue.isEmpty()) {
            current = queue.poll();
            if(current.visited == true) {
                continue;
            }
            if(current == end) {
                return current.id;
            }
            current.visited = true;
            current.tail.add(current);
            for(Node n : current.friends) {
                if((n.tail.size() <= current.tail.size() && n.tail.size() != 0) || (n.wumpusNum > 0 || current.pitNum > 0)) { continue; } // has to here to remove possiblity of two equal length lines and if it is a safe space
                n.tail.addAll(removeDuplicates(current.tail));
                queue.add(n);
            }

        }
        return -1;
    }

    public static int breadthFirstEnd(Node start) { // breadth first searched for start from having gold
        clearTails();
        clearVisited();
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(start);
        Node current;
        while(!queue.isEmpty()) {
            current = queue.poll();
            if(current.visited == true) {
                continue;
            }
            if(current.start) {

                return current.id;
            }
            current.visited = true;
            current.tail.add(current);
            for(Node n : current.friends) {
                if(n.tail.size() <= current.tail.size() && n.tail.size() != 0) { continue; } // has to here to remove possiblity of two equal length lines and if it is a safe space
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

    public static void clearTails(){ // utility to clear tails for multiple searches
        for(Node n : nodes){
            n.tail = new ArrayList<>();
        }
    }
    public static void clearVisited(){// utility to clear visited for multiple searches
        for(Node n : nodes){
            n.visited = false;
        }

    }

    //clear the smells and wumpus vals set to 0
    public static void wumpusWumps(){
        for(Node n : nodes){ // easy way to ignore the wumpus and deal with conditionals if he is dead
            n.smell = false;
            n.wumpusNum = 0;
        }
    }

}
