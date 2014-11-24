/*
 * 
 */
package inoutmatrix;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;
import peasy.PeasyCam;
import processing.core.*;
import traer.physics.ParticleSystem;
import traer.physics.Spring;

/**
 * Main class used also as Main PApplet window. Can drop in as Applet (in
 * theory.)
 *
 * @author Dan
 */
public class Main extends PApplet {

    static Random r = new Random(3);
    //Vertices: Vertex class holds SIC code id and name
    //Integer value will be million pounds of intermediate money flow
    static Graph<Vertex, Edge> g = new SparseMultigraph<Vertex, Edge>();
    //A separate copy so that we can access them in the order they were added
    //since that order matches the order of the CSV file. 
    static ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    static ParticleSystem physics;
    PFont font;
    static StringTokenizer st;//for breaking up long SIC code names
    static int w = 800, h = 800;
    //find range of edge values, normalise spring strength by this
    int maxEdgeVal = -1;
    float maxLogEdgeVal = -1;
    //
    float springDamping = 0.01f;
    float springLength = 200;
    float springStrengthMult = 0.3f;
//    float springStrengthMult = 0.15f;
    float particleStrengthMult = -2542;
    float particleMinDist = 150;
    //
    float segmentSizeX;
    float segmentSizeY;
    float segmentPosX;
    float segmentPosY;
    float segmentPosX2;
    float segmentPosY2;
    //Array for detecting mouse over
    //ArrayList<Ellipse2D.Double> vertexDetect = new ArrayList<Ellipse2D.Double>();
    Ellipse2D.Double[] vertexDetectors;
    boolean anyMouseOver = false;
    boolean mouseMove = false;//if mouseDrag starts, true. False when mouse released
    boolean physOn = true;
    boolean includeImputedRent = true;
    Vertex moveVertex;
    Pair<Vertex> pair;
//
    PeasyCam cam;
//    public static Main m;
    boolean printScreen = false;

    public enum ForceMouseMode {

        springStrengh,
        springDamping,
        springRestLength
    }
    ForceMouseMode fmm = ForceMouseMode.springStrengh;

    @Override
    public void setup() {

//        m = this;

        size(w, h, P2D);
//        cam = new PeasyCam(this, 0,0,0,2000);
//        cam.setMinimumDistance(50);
//        cam.setMaximumDistance(500);

//        font = loadFont("Gautami-Bold-18.vlw");
        font = loadFont("Gautami-Bold-18.vlw");
        textFont(font, 16);
        textMode(SCREEN);

        physics = new ParticleSystem(0, 0.1f);
//        physics.setIntegrator(ParticleSystem.MODIFIED_EULER );
        //load data into graph
        //Done in constructor using static Graph
        loadData();
        
//        new BetweennessCentrality(g, online, online);


    }

    @Override
    public void draw() {


//        translate(-width/2, -width/2, 0);


        //reset vals to keep repeatable random colours
//        r.setSeed(1);
        //set vertex detector positions
        anyMouseOver = false;

        for (Vertex v : vertices) {

            if (v.mouseMoving) {
                v.p.position().set(mouseX, mouseY, 0);
                v.mouseOver = true;
                anyMouseOver = true;
                System.out.println(" ");

            } else {

                if (v.demand / 2000 < 5) {
                    v.detector.setFrameFromCenter(v.p.position().x(), v.p.position().y(), v.p.position().x() + 5, v.p.position().y() + 5);
                } else {
                    v.detector.setFrameFromCenter(v.p.position().x(), v.p.position().y(), v.p.position().x() + v.demand / 2000, v.p.position().y() + v.demand / 2000);
                }

                //check for mouseOver
                if (v.detector.contains((double) mouseX, (double) mouseY)) {
                    v.mouseOver = true;
                    anyMouseOver = true;

//                    System.out.println("somehow still here");



//                System.out.println("mouseOver: " + v.name);
                } else {
                    v.mouseOver = false;
                }

            }

        }

        background(255);
        stroke(0, 50);
//        strokeWeight(1);


        //draw edges first. Different behaviour if any mouseOver
        for (Edge e : g.getEdges()) {

//            System.out.println("sdf: " + ((double) e.val / maxEdgeVal) * 500);
//            System.out.println("sdf: " + Math.log10((double) e.val));

//            strokeWeight((int) ((double) e.val / maxEdgeVal) * 500);

            double logEdgeVal = Math.log10((double) e.val);
//            System.out.println("log edge val: " + logEdgeVal);
            strokeWeight((int) logEdgeVal);

            if (anyMouseOver) {

                if (e.from.mouseOver) {

                    stroke(0);
                    e.drawEdge();

                } else if (e.to.mouseOver) {
                    stroke(0, 255, 100);
                    e.drawEdge();

                }


            } else {

//                stroke(255 - (int) (255 * (logEdgeVal / maxLogEdgeVal)), (int) (255 * (logEdgeVal / maxLogEdgeVal)));
                stroke(255 - (int) (255 * (logEdgeVal / maxLogEdgeVal)));
                e.drawEdge();

            }

            //draw cycle direction: segment of above line
            //find point along line 
            //find total lengths of segment to traverse
            //value is negative if from > to, but we'll use that: if from > to, the flow is going in a negative direction on-screen
            segmentSizeX = e.to.p.position().x() - e.from.p.position().x();
            segmentSizeY = e.to.p.position().y() - e.from.p.position().y();
//            float segmentSizeZ = e.to.p.position().z() - e.from.p.position().z();

            //then find x,y values for the first point
            //find relative value first, along the segment size
            segmentPosX = segmentSizeX * (e.currentCycle / e.cycleSize);
            segmentPosY = segmentSizeY * (e.currentCycle / e.cycleSize);
//            float segmentPosZ = segmentSizeZ * (e.currentCycle / e.cycleSize);
            //other part of line
//            float segmentPosZ2 = 0;

            if ((e.currentCycle + 1) / e.cycleSize < 1) {
                segmentPosX2 = segmentSizeX * ((e.currentCycle + 1) / e.cycleSize);
                segmentPosY2 = segmentSizeY * ((e.currentCycle + 1) / e.cycleSize);
//                segmentPosZ2 = segmentSizeZ * ((e.currentCycle + 1) / e.cycleSize);
            } else {
                segmentPosX2 = segmentSizeX;
                segmentPosY2 = segmentSizeY;
//                segmentPosZ2 = segmentSizeZ;
            }



            //test?
//            stroke(0, 255, 0);
            strokeWeight(1);

            stroke(255);
//            stroke(e.from.c.getRGB());
            fill(e.from.c.getRGB());
//            strokeWeight(e.val / 300);

            if (anyMouseOver) {

                if (e.from.mouseOver || e.to.mouseOver) {

                    e.from.drawVertexAsCircleWithOffset((float) Math.sqrt(e.val / 5), segmentPosX, segmentPosY, Integer.toString(e.val));
//                    ellipse(e.from.p.position().x() + segmentPosX, e.from.p.position().y() + segmentPosY, (float) Math.sqrt(e.val / 5), (float) Math.sqrt(e.val / 5));
//                    
                }
            } else {

//                ellipse(e.from.p.position().x() + segmentPosX, e.from.p.position().y() + segmentPosY, (float) logEdgeVal * 5, (float) logEdgeVal * 5);
                e.from.drawVertexAsCircleWithOffset((float) Math.sqrt(e.val / 5), segmentPosX, segmentPosY, Integer.toString(e.val));
//                e.from.drawVertexAsCircleWithOffset((float) Math.sqrt(e.val / 5), segmentPosX, segmentPosY);
//                ellipse(e.from.p.position().x() + segmentPosX, e.from.p.position().y() + segmentPosY, (float) Math.sqrt(e.val / 5), (float) Math.sqrt(e.val / 5));
//                line(e.from.p.position().x() + segmentPosX, e.from.p.position().y() + segmentPosY, e.from.p.position().z() + segmentPosZ,
//                        e.from.p.position().x() + segmentPosX2, e.from.p.position().y() + segmentPosY2, e.from.p.position().z() + segmentPosZ);

            }



            e.currentCycle += 0.1;
            if (e.currentCycle > e.cycleSize) {
                e.currentCycle = 0;
            }




        }//end for Edge e


        stroke(0);
        fill(255);

        for (Vertex v : vertices) {

            if (v.mouseOver) {
                strokeWeight(2);
//                ellipse(v.p.position().x(), v.p.position().y(), v.demand / 1000, v.demand / 1000);
                v.drawVertexAsCircle(v.demand / 1000);

                fill(0);
                text("" + v.name, v.p.position().x() - (v.name.length() * 4), v.p.position().y());
//                for (int i = 0; i < v.nameBits.length; i++) {
//                    text("" + v.nameBits[i], v.p.position().x() - (v.nameBits[i].length() * 3), v.p.position().y() - 40 + (i * 20));
//                }
                fill(255);


                //also need to write names of sectors being traded with
//                Set<Vertex> neighs = (Set) g.getNeighbors(v);
//
//                for (Vertex n : neighs) {
//
//                    fill(0);
////                    text("" + n.name, n.p.position().x() - (n.name.length() * 4), n.p.position().y());
//                    for (int i = 0; i < n.nameBits.length; i++) {
//                        text("" + n.nameBits[i], n.p.position().x() - (n.nameBits[i].length() * 3), n.p.position().y() - 40 + (i * 10));
//                    }
//                    fill(255);
//
//                }


            } else {
                strokeWeight(1);
                v.drawVertexAsCircle(v.demand / 1000);
//                ellipse(v.p.position().x(), v.p.position().y(), v.demand / 1000, v.demand / 1000);
            }
//            ellipse(v.p.position().x(), v.p.position().y(), 3 * (float) Math.log10((double) v.demand), 3 * (float) Math.log10((double) v.demand));
//            ellipse(v.p.position().x(), v.p.position().y(), 10, 10);

//            System.out.println("pos: " + v.p.position().x() + "," + v.p.position().y());

        }

        if (physOn) {
            physics.tick();
        }

//        translate(width/2, width/2, 0);

        if (printScreen) {
            printScreen();
        }

    }

    public void mouseDragged() {

        for (Vertex v : vertices) {

            if (v.mouseOver) {
                v.mouseMoving = true;
//                v.mouseOver = false;
            }

        }

        if (keyPressed) {

            if (key == 'z' || key == 'Z') {

                springLength += (mouseY - pmouseY);
                System.out.println("Springlength:" + springLength);
                resetPhysics();

            } else if (key == 'x' || key == 'X') {

                springStrengthMult += (float) (mouseY - pmouseY) / 100;
                System.out.println("springStrengthMult:" + springStrengthMult);
                resetPhysics();

            } else if (key == 'c' || key == 'C') {

                particleStrengthMult += (mouseY - pmouseY);
                System.out.println("particleStrengthMult:" + particleStrengthMult);
                resetPhysics();

            } else if (key == 'v' || key == 'V') {

                particleMinDist += (mouseY - pmouseY);
                System.out.println("particleMinDist:" + particleMinDist);
                resetPhysics();

            }

        }

    }

    public void mouseReleased() {

        for (Vertex v : vertices) {

            v.mouseMoving = false;

        }

    }

    public void keyPressed() {

        if (key == 'p' || key == 'P') {

            physOn = (physOn ? false : true);
            System.out.println("here");

            //randomise positions
        } else if (key == 'r' || key == 'R') {

            for (Vertex v : vertices) {

                v.p.position().set((int) (Main.r.nextDouble() * (double) Main.w), (int) (Main.r.nextDouble() * (double) Main.h), 0);

            }

        } else if (key == 's' || key == 'S') {

            printScreen = true;

        } else if (key == 'v' || key == 'V') {

//            removeImputedRent();
            includeImputedRent = (includeImputedRent ? false : true);
            loadData();


        } else if (key == 'c' || key == 'C') {

            putInACircle(vertices, (width / 2) * 0.75);


        } else if (key == 'i' || key == 'I') {

//            removeImputedRent();

        }

    }

    private void removeImputedRent() {

        Vertex remove = new Vertex(this, " ", " ");

        for (Vertex v : vertices) {

            if (v.name.equals("Imputed rent services")) {
                remove = v;
            }

            //remove imputed rent from total intermediate demand
            if (v.name.equals("Financial services except insurance and pension funding")) {
                v.demand -= 37870;
            }

        }

        //remove imputed rent
        g.removeVertex(remove);

    }

    public void resetPhysics() {

        for (Edge e : g.getEdges()) {

            if (e.spr != null) {

                float springEdgeVal = (float) e.val / maxEdgeVal;

                e.spr.setStrength(springEdgeVal * springStrengthMult);
                e.spr.setRestLength(springLength);

            }

            if (e.ac != null) {

                e.ac.setStrength((((float) Math.log10(e.from.demand)) - 2) * particleStrengthMult);
                e.ac.setMinimumDistance(particleMinDist);

            }

        }


//        for (int i = 0; i < physics.numberOfSprings(); i++) {
//
//
//        }
//
//        for (int i = 0; i < physics.numberOfSprings(); i++) {
//
//
//        }


//        physics.clear();
//        physics = new ParticleSystem(0, 0.1f);
//
//        for (Edge e : g.getEdges()) {
//
//            pair = g.getEndpoints(e);
//
//            //check it's not referencing itself
//            if (!pair.getFirst().equals(pair.getSecond())) {
//
//
//                //float maxSpringRestLength = 1000f;
//
//                float springEdgeVal = (float) e.val / maxEdgeVal;
//
////            System.out.println("eval/maxval: " + springMult);
//
//                //Spring makeSpring( Particle a, Particle b, float strength, float damping, float restLength )
//                physics.makeSpring(pair.getFirst().p, pair.getSecond().p, springStrengthMult * springEdgeVal, springDamping, springLength);
////                physics.makeSpring(pair.getFirst().p, pair.getSecond().p, maxSpringStrength * springMult, 0.001f, (float) Math.sqrt(maxSpringStrength * springMult * 900000));
//
//                //Attraction makeAttraction( Particle a, Particle b, float strength, float minimumDistance )
////            physics.makeAttraction(pair.getFirst().p, pair.getSecond().p, pair.getFirst().demand/100, 100);
//                physics.makeAttraction(pair.getFirst().p, pair.getSecond().p, (((float) Math.log10(pair.getFirst().demand)) - 2) * particleStrengthMult, particleMinDist);
////                physics.makeAttraction(pair.getFirst().p, pair.getSecond().p, springMult * -80000, 30);
////              physics.makeAttraction(pair.getFirst().p, pair.getSecond().p, 5f, 50);
//
////            System.out.println("Raw demand = " + pair.getFirst().demand + ", log demand: " + ((float) Math.log10(pair.getFirst().demand)-3));
//
//            }//end pair check
//           
//        }
//        
//        System.out.println("spring number: " + physics.numberOfSprings());

    }

    public void putInACircle(ArrayList<Vertex> verts, double radius) {

        double circlePoint = 0;
        //Number of steps round circle, one for each actor
        double stepsize = (2 * Math.PI / verts.size());

        for (Vertex v : verts) {

            new Point2D.Double(((double) width / 2)
                    + (radius * Math.cos(circlePoint)), ((double) width / 2) + (radius * Math.sin(circlePoint)));

            double x = ((double) width / 2) + (radius * Math.cos(circlePoint));
            double y = ((double) width / 2) + (radius * Math.sin(circlePoint));

            v.p.position().set((int) x, (int) y, 0);

            //move round circle
            circlePoint += stepsize;

        }

    }

    private void loadData() {

        if (includeImputedRent) {
            new DataReader(this, "supplyAndUse2009copy2.csv");
        } else {
            new DataReader(this, "supplyAndUse2009copy_minusImputedRent.csv");
        }

        System.out.println("number of vertices: " + vertices.size());
        System.out.println("Number of edges: " + g.getEdgeCount());

        //set up size of vertex detector
        vertexDetectors = new Ellipse2D.Double[vertices.size()];

//        Vertex remove = new Vertex(this, " ", " ");

        for (int i = 0; i < vertexDetectors.length; i++) {

//            System.out.println(vertices.get(i).name);

//            if (vertices.get(i).name.equals("Imputed rent services")) {
//                remove = vertices.get(i);
//            }
//
//            //remove imputed rent from total intermediate demand
//            if (vertices.get(i).name.equals("Financial services, except insurance and pension funding")) {
//                vertices.get(i).demand -= 37870;
//            }

            vertexDetectors[i] = new Ellipse2D.Double();
        }

        //remove imputed rent
//        g.removeVertex(remove);

//        BetweennessCentrality ranker = new BetweennessCentrality(g);
//        ranker.setRemoveRankScoresOnFinalize(false);
//        ranker.evaluate();
        //ranker.printRankings(true, true);

//        for(Vertex v: vertices) {            
//            System.out.println(v.name + "#" + ranker.getVertexRankScore(v));            
//        }


        //find max Edge val
        for (Edge e : g.getEdges()) {

            //decrease total intermediate val for finance too. 
//            if (e.from.name.equals("Financial services, except insurance and pension funding")
//                    && e.to.name.equals("Imputed rent services")) {
//                System.out.println("found");
//            }

            maxEdgeVal = (e.val > maxEdgeVal ? e.val : maxEdgeVal);
            maxLogEdgeVal = (Math.log10((double) e.val) > maxLogEdgeVal ? (float) Math.log10((double) e.val) : maxLogEdgeVal);

            //test vals
//            System.out.println("Edge vals. From: " + e.from.name + ", to: " + e.to.name + ", val: " + e.val);

        }



        System.out.println("max edge val" + maxEdgeVal + ", max log of Edge Vals: " + maxLogEdgeVal);

        //on the basis that physics will add springs and attractions in order, add a reference to the edges
        int count = 0;
        Spring spring;

        //make forces
        for (Edge e : g.getEdges()) {

            pair = g.getEndpoints(e);

            //check it's not referencing itself
            if (!pair.getFirst().equals(pair.getSecond())) {

                float springEdgeVal = (float) e.val / maxEdgeVal;

                e.setSpring(physics.makeSpring(pair.getFirst().p, pair.getSecond().p,
                        springStrengthMult * springEdgeVal, springDamping, springLength));

                //I wish I'd written a comment explaining this!
                e.setAttraction(physics.makeAttraction(pair.getFirst().p, pair.getSecond().p,
                        (((float) Math.log10(pair.getSecond().demand)) - 2) * particleStrengthMult, particleMinDist));
//                e.setAttraction(physics.makeAttraction(pair.getFirst().p, pair.getSecond().p,
//                        (((float) Math.log10(pair.getFirst().demand)) - 2) * particleStrengthMult, particleMinDist));

                //            System.out.println("Raw demand = " + pair.getFirst().demand + ", log demand: " + ((float) Math.log10(pair.getFirst().demand)-3));

                count++;

            }//end pair check
//            else {
//                System.out.println("me!");
//            }


        }

        System.out.println("spring number: " + physics.numberOfSprings());

        //stick em in circle for now...
        putInACircle(vertices, width / 20);
//        putAtCentre();


    }

    public void putAtCentre() {

        for (Vertex v : vertices) {

            v.p.position().set(width / 2, height / 2, 0);

        }

    }

    private void printScreen() {

        printScreen = false;

        Date d = new Date();
        System.out.println("today = " + d);
        String fileName = "keyPrints/" + d + "_KeyPrint_IOMatrix-####.jpeg";
        fileName = fileName.replaceAll(" ", "");
        fileName = fileName.replaceAll(":", "_");
        fileName = fileName.replaceAll("£", ":");
        saveFrame(fileName);

    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"inoutmatrix.Main"});
    }

    //to allow reload of applet
    @Override
    public void start() {

        g = new SparseMultigraph<Vertex, Edge>();
        //A separate copy so that we can access them in the order they were added
        //since that order matches the order of the CSV file. 
        vertices = new ArrayList<Vertex>();

        super.start();

    }
}
