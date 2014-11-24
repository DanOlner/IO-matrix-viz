/*
 * https://github.com/DanOlner danielolner@gmail.com
 */
package inoutmatrix;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Range;
import controlP5.Textlabel;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;
import processing.core.*;
import traer.physics.ParticleSystem;
import traer.physics.Spring;

/**
 * Main class used also as Main PApplet window.
 *
 * @author Dan Olner
 */
public class Main extends PApplet {

    static Random r = new Random(9);
    //rather more methods, but don't want to break previous use of Random, so leaving
    static Randoms rands = new Randoms(1);
    //Vertices: Vertex class holds id and name
    //Integer value will be million pounds of intermediate money flow
    static Graph<Vertex, Edge> g = new SparseMultigraph<Vertex, Edge>();
    //A separate copy so that we can access them in the order they were added
    //since that order matches the order of the CSV file. 
    static ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    static ParticleSystem physics;
    PFont font;
    static StringTokenizer st;//for breaking up long names
    static int w = 800, h = 800;
    //find toRange of edge values, normalise spring strength by this
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
    Ellipse2D.Double[] vertexDetectors;
    boolean oneOrMoreIsActive = false;
    boolean mouseMove = false;//if mouseDrag starts, true. False when mouse released
    boolean justAClick = false;//if mouse clicked, may just be for dragging. Used if mouseRelease called without drag.
    boolean physOn = true;
    boolean setToCircle = false;
    boolean includeImputedRent = false;
    boolean showMoneyFlowAsMovingCircles = true;
    boolean randomWalkOn = false;
    boolean showRandomWalk = false;
    public static boolean seeAmounts = false;
    boolean onClickSelectQuarterMin = true;//if clicked, set min range to quarter, to hide smaller connections
    Vertex moveVertex;
    Pair<Vertex> pair;
    static ArrayList<Edge> drawEdges = new ArrayList<Edge>();
    static ArrayList<Edge> allEdges;
    Vertex vertexWithFocus;//to save having to research for it every time
//
    public ControlP5 controlP5;
    public Range toRange, fromRange;
    double minRange, maxRange;
    public Textlabel nameOfVertexWithFocus;
    boolean printScreen = false;

    public enum ForceMouseMode {

        springStrengh,
        springDamping,
        springRestLength
    }
    ForceMouseMode fmm = ForceMouseMode.springStrengh;

    @Override
    public void setup() {

        size(w, h, P2D);

        font = loadFont("Gautami-Bold-18.vlw");
        textFont(font, 16);
        textMode(SCREEN);

        physics = new ParticleSystem(0, 0.1f);

        loadData();

        allEdges = new ArrayList(g.getEdges());

        setUpControls();

    }

    @Override
    public void draw() {

        //Pick random vertices to random walk between
        //run for a certain number of times before redrawing
        if (randomWalkOn) {
            for (int i = 0; i < 50; i++) {

                int j = Randoms.nextInt(vertices.size());
                int k = Randoms.nextInt(vertices.size());

                //59 & 60: wholesale/retail trades; no in-links so random walk can never end
                if (j != k && k != 59 && k != 60) {
                    RandomWalkCentrality.randomWalkBetweenTheseVertices(vertices.get(j), vertices.get(k));
                }

            }//number of times to complete a random walk
        }//if randomWalkOn

        //set vertex detector positions
        oneOrMoreIsActive = false;

        for (Vertex v : vertices) {
            //check first for any 'on'

            //test
//            v.setMinMaxFromIndex(0, 10);
            if (v.on) {
                oneOrMoreIsActive = true;
            }

            if (v.mouseMoving) {

                v.p.position().set(mouseX, mouseY, 0);
                v.mouseOver = true;

                oneOrMoreIsActive = true;

            } else {

                if (v.demand / 2000 < 10) {
                    v.detector.setFrameFromCenter(v.p.position().x(), v.p.position().y(),
                            v.p.position().x() + 5, v.p.position().y() + 5);
                } else {
                    v.detector.setFrameFromCenter(v.p.position().x(), v.p.position().y(),
                            v.p.position().x() + v.demand / 2000, v.p.position().y() + v.demand / 2000);
                }

                //check for mouseOver
                //Don't react in area reserved for controls
                if (mouseX > 0 && mouseY > 22) {

                    if (v.detector.contains((double) mouseX, (double) mouseY)) {

                        v.mouseOver = true;

                        oneOrMoreIsActive = true;

                    } else {

                        v.mouseOver = false;

                    }

                }//end if mousex

            }//end else

        }//end for vertex

        background(255);
        stroke(0, 50);

        if (oneOrMoreIsActive) {

            drawEdges.clear();

            for (Vertex v : vertices) {

                if (v.on || v.mouseOver) {

                    for (int i = v.moneyToMin; i < v.moneyToMax + 1; i++) {

                        //HACKEROO: adding 1 above keeps largest but very occasionally caused out of index error
                        try {
                            drawEdges.add(v.moneyToMe.get(i));
                        } catch (Throwable t) {
                        }
                    }

                    for (int i = v.moneyFromMin; i < v.moneyFromMax + 1; i++) {
                        try {
                            drawEdges.add(v.moneyFromMe.get(i));
                        } catch (Throwable t) {
                        }
                    }

                }//end if

            }//end for Vertex v

        } else {

            drawEdges = (ArrayList<Edge>) allEdges.clone();

        }

        //draw drawEdges first. Different behaviour if any mouseOver
        for (Edge e : drawEdges) {

            double logEdgeVal = Math.log10((double) e.val);
            strokeWeight((int) logEdgeVal);

            //test drawing for random walk
            if (showRandomWalk) {
                strokeWeight(e.totalVisits / 30000);
            }

            if (oneOrMoreIsActive) {

                if (e.from.mouseOver || e.from.on) {
//                    stroke(0);
                    stroke(153, 101, 87);
                } else if (e.to.mouseOver || e.to.on) {
                    stroke(0, 255, 100);
                }
            } else {

                stroke(255 - (int) (255 * (logEdgeVal / maxLogEdgeVal)), 50);

            }

            e.drawEdge();

            //draw cycle direction: segment of above line
            //find point along line 
            //find total lengths of segment to traverse
            //value is negative if from > to, but we'll use that: if from > to, the flow is going in a negative direction on-screen
            segmentSizeX = e.to.p.position().x() - e.from.p.position().x();
            segmentSizeY = e.to.p.position().y() - e.from.p.position().y();

            //then find x,y values for the first point
            //find relative value first, along the segment size
            segmentPosX = segmentSizeX * (e.currentCycle / e.cycleSize);
            segmentPosY = segmentSizeY * (e.currentCycle / e.cycleSize);

            if ((e.currentCycle + 1) / e.cycleSize < 1) {

                segmentPosX2 = segmentSizeX * ((e.currentCycle + 1) / e.cycleSize);
                segmentPosY2 = segmentSizeY * ((e.currentCycle + 1) / e.cycleSize);

            } else {

                segmentPosX2 = segmentSizeX;
                segmentPosY2 = segmentSizeY;

            }

            strokeWeight(1);

            stroke(255);

            fill(e.from.c.getRGB());

            if (showMoneyFlowAsMovingCircles) {
                if (oneOrMoreIsActive) {

                    if (e.from.mouseOver || e.to.mouseOver
                            || e.from.on || e.to.on) {

                        e.from.drawVertexAsCircleWithOffset((float) Math.sqrt(e.val / 5),
                                segmentPosX, segmentPosY, Integer.toString(e.val));
//                    
                    }
                } else {

                    e.from.drawVertexAsCircleWithOffset((float) Math.sqrt(e.val / 5),
                            segmentPosX, segmentPosY, Integer.toString(e.val));

                }
            }

            e.currentCycle += 0.1;
            if (e.currentCycle > e.cycleSize) {
                e.currentCycle = 0;
            }

        }//end for Edge e

        stroke(0);
        fill(255);

        for (Vertex v : vertices) {

            if (showRandomWalk) {
                v.drawVertexAsCircle(v.totalVisits / 60000);
            } else {
//                v.drawVertexAsCircle(v.consumption / 1000);
                v.drawVertexAsCircle(v.demand / 1000);
            }

            if (v.mouseOver || v.on) {

                fill(0);
                text("" + v.name, v.p.position().x() - (v.name.length() * 4), v.p.position().y());

                fill(255);

            }

        }

        if (physOn) {
            physics.tick();
        }

        if (printScreen) {
            printScreen();
        }

        textMode(MODEL);
        controlP5.draw();
        textMode(SCREEN);

    }

    public void mousePressed() {

        //if mouseDragged called this will be set to false so mouseReleased can act correctly
        justAClick = true;

    }

    public void mouseDragged() {

        justAClick = false;

        for (Vertex v : vertices) {

            if (v.mouseOver) {
                v.mouseMoving = true;
            }

        }

    }

    public void mouseReleased() {

        //if no mouseDrag took place, interpret as click and act accordingly
        if (justAClick) {

            for (Vertex v : vertices) {

                if (v.mouseOver) {

                    //Give this the main focus. Only one a time gets this.
                    v.hasFocus = true;
                    vertexWithFocus = v;
                    //change labels for range finders

                    toRange.setLabel(v.shortName + " to");
                    fromRange.setLabel(v.shortName + " from");

                    //otherwise each range method broadcasts
                    toRange.setBroadcast(false);
                    fromRange.setBroadcast(false);

                    //lowvalue, highvalue are the bounds.
                    //min and max are the current values.
                    //stupid var naming!
                    //set to money-to low. Lowest is at end of index
                    toRange.setMax(v.moneyFromMe.get(v.returnMaxToIndex()).val);
                    toRange.setHighValue(v.moneyFromMe.get(v.returnMaxToIndex()).val);
                    toRange.setMin(v.moneyFromMe.get(0).val);

                    //hack. There's only one sector with no 'from' I think, imputed rent.
                    //Just dealing with that by cheating...
                    try {
                        fromRange.setMax(v.moneyToMe.get(v.returnMaxFromIndex()).val);
                        fromRange.setHighValue(v.moneyToMe.get(v.returnMaxFromIndex()).val);
                        fromRange.setMin(v.moneyToMe.get(0).val);
                    } catch (Throwable t) {
                        fromRange.setMax(0);
                        fromRange.setHighValue(0);
                        fromRange.setMin(0);
                    }

                    //trying to get that array index... 
                    int a = ((int) ((double) v.moneyFromMe.size() * 0.95));
                    int b = ((int) ((double) v.moneyToMe.size() * 0.95));

                    if (onClickSelectQuarterMin) {

                        toRange.setLowValue(v.moneyFromMe.get(a).val);

                        //again, imputed rent hack. Need to fix for empty arrays, obviously
                        try {
                            fromRange.setLowValue(v.moneyToMe.get(b).val);
                        } catch (Throwable t) {
                            fromRange.setLowValue(0);
                        }

                    } else {

                        toRange.setLowValue(v.moneyFromMe.get(0).val);

                        //again, imputed rent hack. Need to fix for empty arrays, obviously
                        try {
                            fromRange.setLowValue(v.moneyToMe.get(0).val);
                        } catch (Throwable t) {
                            fromRange.setLowValue(0);
                        }
                    }

                    //need to do this here as vals may have changed above
                    v.moneyFromMin = a;
                    v.moneyFromMax = v.returnMaxToIndex();
                    v.moneyToMin = b;
                    v.moneyToMax = v.returnMaxFromIndex();

                    toRange.setBroadcast(true);
                    fromRange.setBroadcast(true);

                    System.out.println("changing on");
                    v.mouseMoving = false;

                    v.on = (v.on == true ? false : true);

                } else {

                    //make sure no others have focus
                    v.hasFocus = false;

                }
            }

            justAClick = false;

        } else {

            //else if just a post-drag release, reset mouseMoving
            for (Vertex v : vertices) {
                v.mouseMoving = false;
            }

        }

    }

    public void keyPressed() {

        if (key == 'p' || key == 'P') {

            physOn = (physOn ? false : true);

            //randomise positions
        } else if (key == 'r' || key == 'R') {

            for (Vertex v : vertices) {

                v.p.position().set((int) (Main.r.nextDouble() * (double) Main.w), (int) (Main.r.nextDouble() * (double) Main.h), 0);

            }

        } else if (key == 's' || key == 'S') {

            printScreen = true;

        } else if (key == 'c' || key == 'C') {

            physOn = false;
            putInACircle(vertices, (width / 2) * 0.85);

        } else if (key == 'q' || key == 'Q') {
            //reset 'on'
            for (Vertex v : vertices) {
                v.on = false;
            }

            oneOrMoreIsActive = false;

//toggle show money flow
        } else if (key == 'w' || key == 'W') {

            showMoneyFlowAsMovingCircles = (showMoneyFlowAsMovingCircles ? false : true);
//            System.out.println("show: " + showMoneyFlowAsMovingCircles);

        } else if (key == 'o' || key == 'O') {

            randomWalkOn = (randomWalkOn ? false : true);
//            System.out.println("show: " + showMoneyFlowAsMovingCircles);

        } else if (key == 'i' || key == 'I') {

            showRandomWalk = (showRandomWalk ? false : true);
//            System.out.println("show: " + showMoneyFlowAsMovingCircles);

            //print list of random walk node visits
        } else if (key == 'u' || key == 'U') {

            for (Vertex v : vertices) {

                System.out.println(v.name + "&&" + v.totalVisits);

            }

            System.out.println("----");

        }

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

        ArrayList<Edge> drawEdges = null;
        ArrayList<Edge> allEdges = null;

        r.setSeed(9);

        super.start();

    }

    public void setUpControls() {

        controlP5 = new ControlP5(this);
        controlP5.setColorLabel(0);
        controlP5.setColorBackground(200);

//            controlP5.setControlFont(new ControlFont(createFont("ArialMT", 20, false), 20));
//            controlP5.addButton("mouse1", 0, width - 130, height - 100, 100, 25).setId(1);
//            controlP5.addButton("mouse2", 0, width - 130, height - 70, 100, 25).setId(2);
        controlP5.setAutoDraw(false);

        //some controlP5 things are better done here
//        controlP5.setControlFont(new ControlFont(font, 15));
        int toggleY = 40, gap = 40, count = 1;
        controlP5.addToggle("physics on/off", physOn, 0, toggleY, 20, 20);
        controlP5.addToggle("Set to circle", setToCircle, 0, toggleY + (gap * count++), 20, 20);
        controlP5.addToggle("toggle see amounts", setToCircle, 0, toggleY + (gap * count++), 20, 20);
        controlP5.addToggle("reset vertices", setToCircle, 0, toggleY + (gap * count++), 20, 20);
        controlP5.addToggle("randomise", setToCircle, 0, toggleY + (gap * count++), 20, 20);

        nameOfVertexWithFocus = controlP5.addTextlabel("focusVertex", "test label", 50, 50);

//      http://www.sojamo.de/libraries/controlP5/reference/index.html
        toRange = controlP5.addRange("money to", 0, 0, 0, 0, 0, 0, 200, 20);
        fromRange = controlP5.addRange("money from", 0, 0, 0, 0, width - 400, 0, 200, 20);

    }

    public void controlEvent(ControlEvent evt) {

        if (evt.controller().name().equals("physics on/off")) {

            physOn = (physOn == true ? false : true);

        } else if (evt.controller().name().equals("Set to circle")) {

            physOn = false;
            putInACircle(vertices, (width / 2) * 0.85);

        } else if (evt.controller().name().equals("money from")) {

            for (Vertex v : vertices) {

                if (v.hasFocus) {

                    //check we're not at each value's endpoint. If we are, use that
                    v.setMinMaxFromIndex((int) evt.controller().arrayValue()[0] - 1,
                            (int) evt.controller().arrayValue()[1] + 1);
                }

            }

        } else if (evt.controller().name().equals("money to")) {

//            System.out.println(evt.controller().arrayValue()[0]
//                    + "," + evt.controller().arrayValue()[1]);
            for (Vertex v : vertices) {

                if (v.hasFocus) {
//                    vertexWithFocus = v;

                    //check we're not at each value's endpoint. If we are, use that
                    v.setMinMaxToIndex((int) evt.controller().arrayValue()[0] - 1,
                            (int) evt.controller().arrayValue()[1] + 1);
                }

            }

        } else if (evt.controller().name().equals("randomise")) {

            for (Vertex v : vertices) {

                v.p.position().set((int) (Main.r.nextDouble() * (double) Main.w), (int) (Main.r.nextDouble() * (double) Main.h), 0);

            }

        } else if (evt.controller().name().equals("reset vertices")) {

            //reset 'on'
            for (Vertex v : vertices) {
                v.on = false;
            }

            oneOrMoreIsActive = false;

        } else if (evt.controller().name().equals("toggle see amounts")) {

            seeAmounts = (seeAmounts == true ? false : true);

        }

    }//end method controlevent
    
    private void loadData() {

        g = new SparseMultigraph<Vertex, Edge>();
        vertices = new ArrayList<Vertex>();

        physics = new ParticleSystem(0, 0.1f);

        physOn = true;

        new DataReader(this);
        
        System.out.println("number of vertices: " + vertices.size());
        System.out.println("Number of edges: " + g.getEdgeCount());

        //set up size of vertex detector
        vertexDetectors = new Ellipse2D.Double[vertices.size()];

        for (int i = 0; i < vertexDetectors.length; i++) {

            vertexDetectors[i] = new Ellipse2D.Double();
        }

        //find max Edge val
        for (Edge e : g.getEdges()) {

            maxEdgeVal = (e.val > maxEdgeVal ? e.val : maxEdgeVal);
            maxLogEdgeVal = (Math.log10((double) e.val) > maxLogEdgeVal ? (float) Math.log10((double) e.val) : maxLogEdgeVal);

            //test vals
//            System.out.println("Edge vals. From: " + e.from.name + ", to: " + e.to.name + ", val: " + e.val);

        }



        System.out.println("max edge val" + maxEdgeVal + ", max log of Edge Vals: " + maxLogEdgeVal);

        //on the basis that physics will add springs and attractions in order, add a reference to the drawEdges
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
                //            System.out.println("Raw demand = " + pair.getFirst().demand + ", log demand: " + ((float) Math.log10(pair.getFirst().demand)-3));

                count++;

            }//end pair check

        }

        allEdges = new ArrayList(g.getEdges());
        System.out.println("spring number: " + physics.numberOfSprings());

        //stick em in circle for now...
        putInACircle(vertices, width / 20);

    }

}
