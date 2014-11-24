package inoutmatrix;

import au.com.bytecode.opencsv.CSVReader;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import processing.core.PApplet;

/**
 * Parse CSV version of UK intermediate good flow supply and use table
 *
 * Uses http://opencsv.sourceforge.net/
 *
 * @author Dan
 */
public class DataReader {

    CSVReader reader;
    //for storing matrix for searching/matching
//    String[] m = new String[2];
    Vertex newVertex;
    PApplet p;
    String url;

    public DataReader(PApplet p, String url) {

        Main.g = new SparseMultigraph<Vertex, Edge>();
        //A separate copy so that we can access them in the order they were added
        //since that order matches the order of the CSV file. 
        Main.vertices = new ArrayList<Vertex>();

        this.p = p;
        this.url = url;

        try {
//            read();
            readURL(url);
        } catch (IOException ex) {
            System.out.println("couldn't read file: " + ex);
        }

    }

    public void read() throws IOException {

        int ID = 0;

        reader = new CSVReader(new FileReader("data/supplyAndUse2009copy2.csv"));
        // nextLine[] is an array of values from the line
        String[] nextLine;

        //First, get id and names of SIC categories, use as vertices
        while ((nextLine = reader.readNext()) != null) {

            //Stop at total intermediate consumption row
            if (nextLine[1].startsWith("Total intermediate consumption")) {
                break;
            }

            if (!nextLine[0].startsWith("*")) {
                //there's a space at the end of the numbers in the file...

//                newVertex = new SICVertex((nextLine[0].substring(0, nextLine[0].length()- 1)), nextLine[1]);
//                newVertex = new SICVertex((nextLine[0].substring(0, nextLine[0].length()- 1)), nextLine[1]);
                newVertex = new Vertex(p, (nextLine[0].trim()), nextLine[1].trim());

                //so we have a record of them in order. Graph's getVertices method does not return a Collection with an ordered list.
                Main.vertices.add(newVertex);

                Main.g.addVertex(newVertex);

            }

        }//end while


        //run again, load in edges. 
        reader = new CSVReader(new FileReader("data/supplyAndUse2009copy2.csv"));

        //record of the index we're checking
        int index = 0;

        while ((nextLine = reader.readNext()) != null) {

            //then store intermediate consumption
            if (nextLine[1].startsWith("Total intermediate consumption")) {

                //load intermediate consumption value into vertices
                for (int i = 0; i < Main.vertices.size(); i++) {

//                    System.out.println("nextLine: " + nextLine[2 + i]);

                    Main.vertices.get(i).consumption = Integer.parseInt(nextLine[2 + i]);

                }


            }

            if (!nextLine[0].startsWith("*")) {


                //load edges, including connection to self.
                //+1 because we want intermediate demand too.
                for (int i = 0; i < Main.vertices.size(); i++) {

                    //first node source, second destination
                    //don't create zero-value edges...
                    if (Integer.parseInt(nextLine[2 + i]) != 0) {

                        //keep a local record of what vertices this connects. Jung is probably slower to retreive this info.
//                        Main.g.addEdge(new Edge(p, Integer.parseInt(nextLine[2 + i]), Main.vertices.get(i), Main.vertices.get(index)),
//                                Main.vertices.get(i), Main.vertices.get(index), EdgeType.DIRECTED);
                        Main.g.addEdge(new Edge(p, Integer.parseInt(nextLine[2 + i]), Main.vertices.get(index), Main.vertices.get(i)),
                                Main.vertices.get(index), Main.vertices.get(i), EdgeType.DIRECTED);

                        //check correct values being loaded onto edge
//                        System.out.println("Creating Edge from: " + vertices.get(index).name + " to " + vertices.get(i).name + ", val: " + Integer.parseInt(nextLine[2 + i]));

                    }

                }

                //Also record intermediate demand for this sector - the last value column in the table
                Main.vertices.get(index).demand = Integer.parseInt(nextLine[2 + Main.vertices.size()]);

                index++;

            }//end if nextline[0] 

            //System.out.println("index: " + index);

        }//end while






    }//end method read

    public void readURL(String url) throws IOException {

        int ID = 0;
        URL dataStream;

        dataStream =
                new URL("http://www.personal.leeds.ac.uk/~geodo/grit/SupplyUseNetworkViz/" + url);

        BufferedReader in = new BufferedReader(new InputStreamReader(dataStream.openStream()));

        //Initially getting just SIC code and name from first two columns
        String[] nextLine = new String[2];

        String str;

        while ((str = in.readLine()) != null) {

            //stop after getting list of all SIC codes and names
            if (str.startsWith("end")) {
                break;
            }

            if (!str.startsWith("*")) {

                StringTokenizer st = new StringTokenizer(str, ",");

                nextLine[0] = st.nextToken();
                nextLine[1] = st.nextToken();

                //Set up vertices with SIC number and name using first two columns in file
                //there's a space at the end of the numbers in the file, so trim
                newVertex = new Vertex(p, (nextLine[0].trim()), nextLine[1].trim());

                //so we have a record of them in order. 
                //Graph's getVertices method does not return a Collection with an ordered list.
                Main.vertices.add(newVertex);

                Main.g.addVertex(newVertex);

            }

        }//end while

        //run again, load in edges. 
        dataStream =
                new URL("http://www.personal.leeds.ac.uk/~geodo/grit/SupplyUseNetworkViz/" + url);

        in = new BufferedReader(new InputStreamReader(dataStream.openStream()));

        //record of the index we're checking
        int index = 0;

        //So we're clear! "From" = getting money from, "to" = giving money to.
        Vertex from, to;
        Edge edge;

        while ((str = in.readLine()) != null) {

            //convert to array. Since I'm retrofitting the above CSVreader method
            ArrayList<String> nextLineList = new ArrayList<String>();

            StringTokenizer st = new StringTokenizer(str, ",");
            String token;

            while (st.hasMoreTokens()) {
                nextLineList.add(st.nextToken());
            }

            nextLine = new String[nextLineList.size()];
            nextLineList.toArray(nextLine);

            //then store intermediate consumption
            if (nextLine[1].startsWith("Total intermediate consumption")) {

                //load intermediate consumption value into vertices
                for (int i = 0; i < Main.vertices.size(); i++) {

                    Main.vertices.get(i).consumption = Integer.parseInt(nextLine[2 + i]);

                }

            }

            if (!nextLine[0].startsWith("*") && !nextLine[0].startsWith("end")) {


                //load edges, including connection to self.
                //+1 because we want intermediate demand too.

                for (int i = 0; i < Main.vertices.size(); i++) {

                    //first node source, second destination
                    //don't create zero-value edges...
                    if (Integer.parseInt(nextLine[2 + i]) != 0) {

                        from = Main.vertices.get(i);
                        to = Main.vertices.get(index);

                        edge = new Edge(p, Integer.parseInt(nextLine[2 + i]),
                                from, to);

                        //while we're here, add to each sector's record of 
                        //"givingMoneyTo" and "gettingMoneyFrom"
                        //This is of course duplicate information that edges hold
                        //But the arrays are ordered by amounts to be used for viewing
                        //Don't want to have to be doing that order every time...
                        from.givingMoneyTo.add(edge);
                        to.gettingMoneyFrom.add(edge);

                        Main.g.addEdge(edge, from, to, EdgeType.DIRECTED);

                    }

                }

                //Also record intermediate demand for this sector - the last value column in the table
                Main.vertices.get(index).demand = Integer.parseInt(nextLine[2 + Main.vertices.size()]);

                index++;

            }//end if nextline[0] 

            //System.out.println("index: " + index);

        }//end while

        //Order each sectors' "givingMoneyTo" and "gettingMoneyFrom" amounts
        //test
        for (Vertex v : Main.vertices) {

//            System.out.println("Vertex " + v.name + ", givingMoneyTo size: "
//                    + v.givingMoneyTo.size()+ ", gettingMoneyFrom size: " + v.gettingMoneyFrom.size());
//            SortArray.SortArrayListOfEdges(v.gettingMoneyFrom);
//            SortArray.SortArrayListOfEdges(v.givingMoneyTo);            
            Collections.sort(v.gettingMoneyFrom);
            Collections.sort(v.givingMoneyTo);
            
            Collections.reverse(v.gettingMoneyFrom);
            Collections.reverse(v.givingMoneyTo);

//            for (Edge bd : v.gettingMoneyFrom) {
//                System.out.println("Array sorted thus:" + bd.val);
//            }
//            
//            System.out.println("***");
//            
//            for (Edge bd : v.givingMoneyTo) {
//                System.out.println("Array sorted thus:" + bd.val);
//            }
//            
//            System.out.println("---");

        }//end for vertex

    }//end method read
}//end class
