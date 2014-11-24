/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package inoutmatrix;

import processing.core.PApplet;
import traer.physics.Attraction;
import traer.physics.Spring;

/**
 *
 * @author Dan
 */
public class Edge implements Comparable {

    static int edgeIDCount = 0;
    int id;
    //Flow from/to in millions of pounds
    int val;
    Vertex from, to;
    //For cycling to indicate direction of flow
    //Needs to go here so each edge can have its flow at a slightly different position
    float cycleSize, currentCycle;
    PApplet p;
    Spring spr;
    Attraction ac;

    public Edge(PApplet p, int val, Vertex from, Vertex to) {

        this.p = p;
        this.val = val;
        this.from = from;
        this.to = to;

        id = edgeIDCount++;

        cycleSize = 20;
        currentCycle = (float) Main.r.nextDouble() * cycleSize;
        currentCycle = 0;



    }

    public void setSpring(Spring s) {

        spr = s;
        
    }

    public void setAttraction(Attraction ac) {

        this.ac = ac;

    }

    public void drawEdge() {

        p.line(from.p.position().x(), from.p.position().y(), to.p.position().x(), to.p.position().y());
        
//        p.text(Integer.toString(val), (from.p.position().x() + to.p.position().x())/2, (from.p.position().y() + to.p.position().y())/2);
        
        

    }

    public int compareTo(Object o) {
        
        int weight1 = val;
        int weight2 = ((Edge) o).val;

        if (weight1 > weight2) {
            //p.p("returned 1");
            return 1;
        } else if (weight1 < weight2) {
            //p.p("returned -1");
            return -1;
        } else return 0;
        
    }
    
    
    
}
