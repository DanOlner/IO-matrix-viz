package inoutmatrix;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.StringTokenizer;
import processing.core.PApplet;
import traer.physics.Particle;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 * Graph vertex object: Standard Industrial Classification code and name
 *
 * @author Dan
 */
public class Vertex {

    String id;//needs to be String, it's not quite a normal number... 
    String name;
    //for breaking up long names for drawing
    String[] nameBits;
//    float x,y;//screen coordinates
    Particle p;//traer physics particle, contains coordinates
    //total intermediate consumption by this SIC across all others
    int consumption;
    //total intermediate demand of this SIC from all others
    int demand;
    //For colouring flows from this vertex
    Color c;
    //for detecting things in range
    Ellipse2D.Double detector;
    boolean mouseOver = false;
    boolean mouseMoving = false;
    boolean on = false;//toggle active state
    PApplet pa;
    //Will be ordered by amounts, so can be used to focus view on larger or smaller amounts
//    public ArrayList<Vertex> givingMoneyTo;
//    public ArrayList<Vertex> gettingMoneyFrom;    
    public ArrayList<Edge> givingMoneyTo = new ArrayList<Edge>();//Must match flow, so 'from' is the other, 'to' is me.
    public ArrayList<Edge> gettingMoneyFrom = new ArrayList<Edge>();//from' is me, 'to' is who to.
    public int givingMoneyToDisplayIndex, gettingMoneyFromDisplayIndex;//index for display where 0 = display none

    public Vertex(PApplet pa, String id, String name) {

        this.pa = pa;
        this.id = id;
        this.name = name;
        detector = new Ellipse2D.Double();

        Main.st = new StringTokenizer(name, " ");
        nameBits = new String[Main.st.countTokens()];

        for (int i = 0; i < nameBits.length; i++) {

            nameBits[i] = Main.st.nextToken();

        }

//        for (String s : nameBits) {
//
//            System.out.println("tokenised: " + s);
//
//        }

        //randomise particle starting position
        p = Main.physics.makeParticle(1, (int) (Main.r.nextDouble() * (double) Main.w), (int) (Main.r.nextDouble() * (double) Main.h), 0);
//        p = Main.physics.makeParticle(1, (int) (Main.r.nextDouble() * (double) Main.w), (int) (Main.r.nextDouble() * (double) Main.h), (int) (Main.r.nextDouble() * (double) Main.h));
        //or just start at centre
//        p = Main.physics.makeParticle(1, Main.w/2, Main.h/2, 0);

//        System.out.println("height:" + Main.height);

//        System.out.println("vals: " + p.position().x() + "," + p.position().y());
        c = new Color(Main.r.nextFloat(), Main.r.nextFloat(), Main.r.nextFloat());

//        c = new Color(Main.r.nextDouble() * 255, Main.r.nextDouble() * 255, Main.r.nextDouble() * 255);

    }

    public void drawVertexAsCircle(float radius) {

        if (mouseOver) {
            pa.strokeWeight(2);
            if (on) {
                pa.stroke(175, 0, 0);
            } else {
                pa.stroke(180);
            }
        } else if (on) {
            pa.strokeWeight(2);
            pa.stroke(0);
        } else {
            pa.strokeWeight(1);
            pa.stroke(180);
        }

        pa.ellipse(p.position().x(), p.position().y(), radius, radius);

    }

    public void drawVertexAsCircleWithOffset(float radius, float x, float y) {

        pa.ellipse(p.position().x() + x, p.position().y() + y, radius, radius);

    }

    public void drawVertexAsCircleWithOffset(float radius, float x, float y, String s) {

        pa.ellipse(p.position().x() + x, p.position().y() + y, radius, radius);

//        pa.fill(0);
//        
//        pa.text(s, p.position().x() + x, p.position().y() + y);

    }

    //changing index
    public void incFromIndex() {
        if (gettingMoneyFromDisplayIndex < gettingMoneyFrom.size()) {
            gettingMoneyFromDisplayIndex++;
        }

        System.out.print("gettingMoneyFr: " + gettingMoneyFromDisplayIndex);
        System.out.println(", max: " + gettingMoneyFrom.size());

    }

    public void decFromIndex() {
        if (gettingMoneyFromDisplayIndex > 0) {
            gettingMoneyFromDisplayIndex--;
        }
        System.out.print("gettingMoneyFr: " + gettingMoneyFromDisplayIndex);
        System.out.println(", max: " + gettingMoneyFrom.size());
    }

    public void incToIndex() {
        if (givingMoneyToDisplayIndex < givingMoneyTo.size()) {
            givingMoneyToDisplayIndex++;
        }

    }

    public void decToIndex() {
        if (givingMoneyToDisplayIndex > 0) {
            givingMoneyToDisplayIndex++;
        }
    }
}
