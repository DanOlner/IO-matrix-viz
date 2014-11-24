/*
 * https://github.com/DanOlner danielolner@gmail.com
 */
package inoutmatrix;

/**
 * Treat each "from/to" pair as an origin and destination for a random walk
 * Count the number of times each vertex is visited, including 'self-loop'
 * visits Choose the next step using a probability in proportion to the money
 * amounts including the "self-loop" money amount Random walk until "to" is
 * reached. Repeat until some stability condition is met Store the resulting
 * visit numbers in the vertices and edges
 *
 * Using Newman, Networks: an introduction And ideas from Blochl et al, Vertex
 * centralities in input-output networks reveal the structure of modern
 * economies 10.1103/PhysRevE.83.046127
 *
 * @author Dan Olner
 */
public class RandomWalkCentrality {

    public static Vertex origin, destination, walkerLocation;


    /*
     * Constructor = new centrality test, so delete all current visit values from the graph
     */
    public RandomWalkCentrality() {

        for (Edge e : Main.allEdges) {
            e.tempVisits = 0;
            e.totalVisits = 0;
        }

        for (Vertex v : Main.vertices) {
            v.tempVisits = 0;
            v.totalVisits = 0;
        }

    }

    /*
     * Takes in a single edge as a "from/to" pair to test. 
     * Done this way so Processing gets a chance to draw the visits.
     * That's the theory... 
     */
    public static void randomWalkBetweenTheseVertices(Vertex origin, Vertex destination) {

//        System.out.println("Origin, dest: " + origin.id + " :" + origin.name + ","
//                + destination.id + " :" + destination.name);

        Vertex v;
        int steps = 0;

        //reset all temporary counts, for assessing stopping conditions
        for (Edge e2 : Main.allEdges) {
            e2.tempVisits = 0;
        }

        for (Vertex v2 : Main.vertices) {
            v2.tempVisits = 0;
        }

        //Random walker's starting location
        walkerLocation = origin;

        boolean stoppingConditionMet = false;

        //Work out next step using the set of out-edges    
        while (!stoppingConditionMet) {

            //carry out a single random walk until destination reached
            //Choose first step using weighted probability based on out-links
            //(Where money is flowing to) 
            //First, sum total money for the moneyFrom set of vertices
            //then set fractions in each vertex
            int totalMoney = 0;

            for (Edge e : walkerLocation.moneyFromMe) {
                totalMoney += e.val;
            }

            //choose random position along a number line up to totalMoney
            //Add one as the nextInt method ranges from 0 to the input value minus one
            int randomPos = 1 + Randoms.nextInt(totalMoney);

            int currentStartPos = 0;

            for (Edge e : walkerLocation.moneyFromMe) {

                if (randomPos < (currentStartPos + e.val)) {
                    walkerLocation = e.to;
                    steps++;
                    walkerLocation.tempVisits++;
                    walkerLocation.totalVisits++;
                    e.tempVisits++;
                    e.totalVisits++;

//                    System.out.println("Walked to new vertex: " + walkerLocation.name);

                    if (walkerLocation.id == destination.id) {
//                        System.out.println("reached destination: " + steps + " steps.");
                        stoppingConditionMet = true;
                    }

                    break;
                }

                //else
                currentStartPos += e.val;

            }//foreach Edge


        }//while stopping condition not met

    }
}
