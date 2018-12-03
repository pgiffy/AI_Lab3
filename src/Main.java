import java.util.*;
import java.io.*;

public class Main {
	
	/*Notes Section:
	 * Done by Peter Gifford and Sam Behrens
	 * 
	 * Takes in a number as an argument which corresponds to the size of the board
	 * 
	 * */
    private static ArrayList<Node> nodes = new ArrayList<>(); // Array of all nodes in the board
    private static boolean wumpusAlive = true; // keeps track of whether or not the player has killed the wumpus
    private static boolean arrow = true; // if the player has the arrow
	public static void main(String[] args) {

        String fileOut = "output.txt"; // output file
        PrintWriter out;

        try {// try catch for the output file in case it does not exist

            out = new PrintWriter(new File(fileOut));

            int size = Integer.parseInt(args[0]);
            out.println("Size: " + size + "x" + size);
            Node[][] maze = new Node[size][size]; // the matrix that holds the map
            Random rand = new Random();
            int iter = 0;
            int startId = -1;
            Node playerNode = null; // the node that points to where the player is

            for (int i = 0; i < size; i++) { // go through and create the maze nodes
                for (int j = 0; j < size; j++) {
                    maze[i][j] = new Node(iter, j, i);
                    nodes.add(maze[i][j]);
                    if(i == size-1 && j == 0) {
                        startId = maze[i][j].id; // set the start node
                        maze[i][j].start = true;
                        playerNode = maze[i][j]; // set the player node
                    }
                    iter++;
                }
            }

            int goldPlace = rand.nextInt(size * size); // where the gold is located
            int wumpusPlace = rand.nextInt(size * size); // where the wumpus is
            while(goldPlace == startId || wumpusPlace == startId) { // place the gold and wumpus
                goldPlace = rand.nextInt(size * size);
                wumpusPlace = rand.nextInt(size * size);
            }

            /**
             * this block is for setting up the graph
             */

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

            for (int i = 0; i < size; i++) { // set the pits
                for (int j = 0; j < size; j++){
                    if(i == size-1 && j == 0 || maze[i][j].hasGold){// the player in the bottom right and gold cannot have a pit
                        continue;
                    }else{
                        if(rand.nextInt(10) < 2) { //First 20% for the pit
                            maze[i][j].hasPit = true;
                        }
                    }
                }
            }

            for (int i = 0; i < size; i++) { // place the breezes and smells
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

            for (int i = 0; i < size; i++) { // print out the starting board, note that smells take precidence over breezes when displayed
                for (int j = 0; j < size; j++) {
                    if(maze[i][j].hasGold) {
                        out.print("G");
                    }
                    else if(maze[i][j].hasWumpus) {
                        out.print("W");
                    }
                    else if(maze[i][j].hasPit) {
                        out.print("P");
                    }
                    else if(maze[i][j].smell) {
                        out.print("_");
                    }
                    else if(maze[i][j].breeze) {
                        out.print("_");
                    }else{
                        out.print("_");
                    }
                }
                out.println();
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
                //System.out.println(playerNode.id + ", Steps: " + steps); // for showing each step that is made

                playerNode.safe = true; // set the current spot to safe
                if (playerNode.hasPit || (playerNode.hasWumpus && wumpusAlive)) { // check if we are dead
                    points -= 1000;
                    points -= steps;
                    out.println("Player Died");
                    out.println("Steps: " + steps);
                    out.println("Points: " + points);
                    break;
                }

                if (playerNode.hasGold) { // if space has gold then we collect it
                    collectedGold = true;
                    steps += 1; // action of picking it up
                }

                /*
                 * This big boy is updating the surrounding areas with their probablility of being mean
                 */

                if (playerNode.breeze) { // if the current spot has a breeze then increment the danger of the adjacent unvisited space
                    for (Node spot : playerNode.friends) {
                        if (!spot.safe) {
                            spot.pitNum++;
                            if(!badSquares.contains(spot)){ // adding to list of nodes to search
                                badSquares.add(spot);
                            }
                        }
                    }
                } else { // if the current spot doesn't have a breeze then decrement the danger of the adjacent unvisited space
                    for (Node spot : playerNode.friends) {
                        if (!spot.safe) {
                            spot.pitNum--;
                            if(badSquares.contains(spot) && spot.wumpusNum <= 0 && spot.pitNum <= 0){ // removing if it is now safe
                                badSquares.remove(spot);
                            }
                        }
                    }
                }
                if (playerNode.smell) { // if the current spot has a smell then increment the danger of the adjacent unvisited space
                    for (Node spot : playerNode.friends) {
                        if (!spot.safe) {
                            spot.wumpusNum++;
                            if(!badSquares.contains(spot)){ // adding to list of nodes to search
                                badSquares.add(spot);
                            }
                        }
                    }
                } else { // if the current spot doesn't have a smell then decrement the danger of the adjacent unvisited space
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

                            if (findSafestSquare(badSquares).wumpusNum > 0 && arrow) { //checks if best option is wumpus possible to run kill wumpus unless there is no arroq
                                Node past = playerNode;
                                playerNode = findSafestSquare(badSquares);
                                breadthFirstNoZero(past, playerNode);
                                badSquares.remove(playerNode);
                                points-=10;
                                steps += playerNode.tail.size();
                                killWumpus(playerNode, out);
                            } else { // goes to spot without killing
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
                    out.println("Found gold");
                    out.println("Cells entered: " + steps);
                    points -= steps;
                    out.println("Points: " + points);
                    break;
                }

            }

            out.close();//close file

        } catch(FileNotFoundException e) {
        }
    }

    private static Node findSafestSquare(ArrayList<Node> toCheck){ // returns id of safest square in list
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


    private static void killWumpus(Node toKill, PrintWriter out){ // checks if space being moved to is a wumpus to kill is
        if(toKill.hasWumpus && arrow){//checks for if wumpus "screams"/dies
	        toKill.hasWumpus = false;
	        wumpusAlive = false;
	        arrow = false;
	        out.println("Wumpus Screams");
	        wumpusWumps();
        } else{
            out.println("Failed wumpus kill");
            toKill.wumpusNum = 0;
        }

        arrow = false;
    }


    private static int breadthFirstZero(Node start) { // breadth first searched for nearest zero spot
        clearTails();
        clearVisited();
    	LinkedList<Node> queue = new LinkedList<>();
    	queue.add(start);
    	Node current;
    	while(!queue.isEmpty()) {
    		current = queue.poll();
    		if(current.visited) {
    			continue;
    		}
    		if(current.wumpusNum <= 0 && current.pitNum <= 0 && !current.safe) {
    			return current.id;
    		}
    		current.visited = true;
    		current.tail.add(current);
    		for(Node n : current.friends) {
    			if((n.tail.size() <= current.tail.size() && n.tail.size() != 0) || (n.wumpusNum > 0 || current.pitNum > 0)) continue; // has to here to remove possiblity of two equal length lines and if it is a safe space
//    			if (!n.safe) continue;
                n.tail.addAll(removeDuplicates(current.tail));
    			queue.add(n);
    		}
    		
    	}
    	return -1;
    }

    private static int breadthFirstNoZero(Node start, Node end) { // breadth first searched for nearest zero spot
        clearTails();
        clearVisited();
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(start);
        Node current;
        while(!queue.isEmpty()) {
            current = queue.poll();
            if(current.visited) {
                continue;
            }
            if(current == end) {
                return current.id;
            }
            current.visited = true;
            current.tail.add(current);
            for(Node n : current.friends) {
                if(n.tail.size() <= current.tail.size() && n.tail.size() != 0) continue; // has to here to remove possiblity of two equal length lines and if it is a safe space
                n.tail.addAll(removeDuplicates(current.tail));
                queue.add(n);
            }

        }
        return -1;
    }

    private static int breadthFirstEnd(Node start) { // breadth first searched for start from having gold
        clearTails();
        clearVisited();
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(start);
        Node current;
        while(!queue.isEmpty()) {
            current = queue.poll();
            if(current.visited) {
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


    private static ArrayList<Node> removeDuplicates(ArrayList<Node> remove){
        Set<Node> temp = new HashSet<>(remove);
    	remove.clear();
    	remove.addAll(temp);
    	return remove;
    	
    }

    private static void clearTails(){ // utility to clear tails for multiple searches
        for(Node n : nodes){
            n.tail = new ArrayList<>();
        }
    }
    private static void clearVisited(){// utility to clear visited for multiple searches
        for(Node n : nodes){
            n.visited = false;
        }

    }

    //clear the smells and wumpus vals set to 0
    private static void wumpusWumps(){
        for(Node n : nodes){ // easy way to ignore the wumpus and deal with conditionals if he is dead
            n.smell = false;
            n.wumpusNum = 0;
        }
    }

}
