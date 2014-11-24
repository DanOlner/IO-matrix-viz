/*
 * https://github.com/DanOlner danielolner@gmail.com
 */
package inoutmatrix;

import au.com.bytecode.opencsv.CSVReader;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

/**
 * Parse CSV version of IO table
 *
 * Uses http://opencsv.sourceforge.net/
 *
 * @author Dan Olner
 */
public class DataReader {

    CSVReader reader;
    Vertex newVertex;
    int ID = 0;//index for each vertex, for matching
    PApplet p;
    String url;

    public DataReader(PApplet p) {

        Main.g = new SparseMultigraph<Vertex, Edge>();
        //A separate copy so that we can access them in the order they were added
        //since that order matches the order of the CSV file. 
        Main.vertices = new ArrayList<Vertex>();

        this.p = p;
        this.url = url;

        try {

            read();

        } catch (IOException ex) {
            System.out.println("couldn't read file: " + ex);
        }

    }

    public void read() throws IOException {

        String fileName = "data/2012_combinedUseMinusImputedRent.csv";

        int ID = 0;

        reader = new CSVReader(new FileReader(fileName));
        // nextLine[] is an array of values from the line
        String[] nextLine;

        //First, get id and names of categories, use as vertices
        while ((nextLine = reader.readNext()) != null) {

            newVertex = new Vertex(p, ID++, nextLine[0].trim());

            //so we have a record of them in order. Graph's getVertices method does not return a Collection with an ordered list.
            Main.vertices.add(newVertex);

            Main.g.addVertex(newVertex);

        }//end while

        //run again, load in drawEdges. 
        reader = new CSVReader(new FileReader(fileName));

        //record of the index we're checking
        int index = 0;
        //So we're clear! "From" = getting money from, "to" = giving money to.
        Vertex from, to;
        Edge edge;

        while ((nextLine = reader.readNext()) != null) {

            //load drawEdges, including connection to self.
            for (int i = 0; i < Main.vertices.size(); i++) {

                //first node source, second destination
                //don't create zero-value drawEdges...
                if (Integer.parseInt(nextLine[1 + i]) != 0) {

                    //keep a local record of what vertices this connects. Jung is probably slower to retreive this info.
                    //check correct values being loaded onto edge
                    //System.out.println("Creating Edge from: " + vertices.get(index).name + " to " + vertices.get(i).name + ", val: " + Integer.parseInt(nextLine[2 + i]));
                    from = Main.vertices.get(i);
                    to = Main.vertices.get(index);

                    edge = new Edge(p, Integer.parseInt(nextLine[1 + i]),
                            from, to);

                    //while we're here, add to each sector's record of 
                    //"moneyTo" and "moneyFrom"
                    //This is of course duplicate information that drawEdges hold
                    //But the arrays are ordered by amounts to be used for viewing
                    //Don't want to have to be re-doing that order while drawing...
                    from.moneyFromMe.add(edge);
                    to.moneyToMe.add(edge);

                    Main.g.addEdge(edge, from, to, EdgeType.DIRECTED);

                }

            }

            index++;

        }//end while

        //One more load to get column and row sums. 
        //Loading again rather than doing in the above loops just to keep things a little more legible
        reader = new CSVReader(new FileReader(fileName));

        //array for summing total consumption per sector
        int[] totalConsumption = new int[Main.vertices.size()];

        index = 0;

        //Each line is a row we want to sum. In IO table, that's each sector's demand
        while ((nextLine = reader.readNext()) != null) {

            int totalDemand = 0;

            for (int i = 0; i < Main.vertices.size(); i++) {

                //sum demand across the row
                totalDemand += Integer.parseInt(nextLine[1 + i]);

                //keep a running record of total consumption for each column
                totalConsumption[i] += Integer.parseInt(nextLine[1 + i]);

            }

            //Store demand after each row sum
            Main.vertices.get(index++).demand = totalDemand;

        }//end while nextLines loop

        //Now drop summed consumption amounts into the sectors
        for (int i = 0; i < Main.vertices.size(); i++) {
            Main.vertices.get(i).consumption = totalConsumption[i];
        }

        //Check consumption/demand numbers
        for (int i = 0; i < Main.vertices.size(); i++) {
            System.out.println(Main.vertices.get(i).name + " consumption: "
                    + Main.vertices.get(i).consumption + ", demand: "
                    + Main.vertices.get(i).demand);
        }

        //Order each sectors' "moneyTo" and "moneyFrom" amounts
        for (Vertex v : Main.vertices) {

//            System.out.println("Vertex " + v.name + ", moneyTo size: "
//                    + v.moneyTo.size()+ ", moneyFrom size: " + v.moneyFrom.size());
            Collections.sort(v.moneyToMe);
            Collections.sort(v.moneyFromMe);

            //set to view all to start with
            v.moneyToMin = 0;
            v.moneyFromMin = 0;
            v.moneyToMax = v.returnMaxFromIndex();
            v.moneyFromMax = v.returnMaxToIndex();

        }//end for vertex

    }//end method read

}//end class
