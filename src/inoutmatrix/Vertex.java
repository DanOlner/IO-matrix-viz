/*
 * https://github.com/DanOlner danielolner@gmail.com
 */
package inoutmatrix;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.StringTokenizer;
import processing.core.PApplet;
import traer.physics.Particle;


/**
 * Graph vertex object
 *
 * @author Dan Olner
 */
public class Vertex implements Comparable {

    int id;//for quicker matching
    String name;
    //for breaking up long names for drawing
    String[] nameBits;
    String shortName;
    Particle p;//traer physics particle, contains coordinates
    //total intermediate consumption by this SIC across all others
    int consumption;
    //total intermediate demand of this SIC from all others
    int demand;
    //For colouring flows from this vertex
    Color c, cLight;
    //for detecting things in toRange
    Ellipse2D.Double detector;
    boolean mouseOver = false;
    boolean mouseMoving = false;
    boolean on = false;//toggle active state
    boolean hasFocus = false;//only one at a time will have this. Controls will affect it. Last one clicked.
    PApplet pa;
    //Will be ordered by amounts, so can be used to focus view on larger or smaller amounts
    //min and max indices refer to array indices. So min value is also at 0 index and max at arraySize.
    public ArrayList<Edge> moneyFromMe = new ArrayList<Edge>();//from' is me, 'to' is who to.
    public ArrayList<Edge> moneyToMe = new ArrayList<Edge>();//Must match flow, so 'from' is the other, 'to' is me.
    //min index for range display. Maps to ArrayList index. So if min = 0 and max = arraySize all will be displayed.
    public int moneyFromMin, moneyFromMax;
    //max index for range display. 
    public int moneyToMin, moneyToMax;//index for display where 0 = display none
    
    //Visit stats for testing random walk centrality
    //tempVisits for testing stopping condition on any one random walk set
    //i.e. between a single "from/to" pair, doing enough random walks for 'stability' to emerge
    public int tempVisits, totalVisits;
    //Each vertex in and out-set is assigned a fraction of one, in proportion to the total
    //money spent in that out-set. Used to select a weighted random walk step.
    public double outSetMoneyFraction;
    
    public Vertex(PApplet pa, int ID, String name) {

        this.id = ID;
        this.pa = pa;
        this.name = name;
        detector = new Ellipse2D.Double();

        Main.st = new StringTokenizer(name, " ");
        nameBits = new String[Main.st.countTokens()];

        for (int i = 0; i < nameBits.length; i++) {

            nameBits[i] = Main.st.nextToken();

        }

        //try and get sensible short name
        if (nameBits.length > 1) {
            if (nameBits[1].equals("and")) {
                shortName = nameBits[0];
            } else if (nameBits[1].equals("of")) {
                shortName = nameBits[0] + " " + nameBits[1] + " " + nameBits[2];

                //to avoid embarrassment i.e. avoid "services of head"...!
                if (nameBits[2].equals("head")) {
                    shortName += " offices";
                }

            } else {
                shortName = nameBits[0] + " " + nameBits[1];
            }
        }

        //randomise particle starting position
        p = Main.physics.makeParticle(1, (int) (Main.r.nextDouble() * (double) Main.w), (int) (Main.r.nextDouble() * (double) Main.h), 0);

        c = new Color(Main.r.nextFloat(), Main.r.nextFloat(), Main.r.nextFloat());
        //cLight - a lighter version. Work out once, keep copy.
        float r, g, b, lighten;

        lighten = 0.5f;

        r = (c.getRed() < 185 ? c.getRed() + 70 : 255);
        g = (c.getGreen() < 185 ? c.getGreen() + 70 : 255);
        b = (c.getBlue() < 185 ? c.getBlue() + 70 : 255);

        r /= 255;
        g /= 255;
        b /= 255;

        cLight = new Color(r, g, b);

    }
    
    public void drawVertexAsCircle(float radius) {

        pa.fill(255);

        if (mouseOver) {
            if (on) {
                pa.strokeWeight(3);
                pa.fill(c.getRGB(), 200);

            } else {
                pa.strokeWeight(2);
                pa.fill(c.getRGB(), 200);

            }
        } else if (on) {
            pa.strokeWeight(2);
            pa.stroke(c.getRGB());
            pa.fill(cLight.getRGB());

        } else {
            pa.strokeWeight(1);
            pa.stroke(20);
            pa.fill(255);
        }

        pa.ellipse(p.position().x(), p.position().y(), radius, radius);

    }
    public void drawRandomVisitsAsCircle(float radius) {

        pa.noFill();
        
        pa.stroke(0);
        pa.strokeWeight(totalVisits/10000);

        pa.ellipse(p.position().x(), p.position().y(), radius, radius);

    }

    public void drawVertexAsCircleWithOffset(float radius, float x, float y) {

        pa.ellipse(p.position().x() + x, p.position().y() + y, radius, radius);

    }

    public void drawVertexAsCircleWithOffset(float radius, float x, float y, String s) {

        pa.ellipse(p.position().x() + x, p.position().y() + y, radius, radius);

        if (Main.seeAmounts) {
            pa.text(s, p.position().x() + x + (radius / 2), p.position().y() + y);
        }

    }

    //changing index
    public void incFromIndex() {
        if (moneyToMax < moneyToMe.size()) {
            moneyToMax++;
        }

        System.out.print("gettingMoneyFr: " + moneyToMax);
        System.out.println(", max: " + moneyToMe.size());

    }

    public void decFromIndex() {
        if (moneyToMax > 0) {
            moneyToMax--;
        }
        System.out.print("gettingMoneyFr: " + moneyToMax);
        System.out.println(", max: " + moneyToMe.size());
    }

    public void incToIndex() {
        if (moneyFromMax < moneyFromMe.size()) {
            moneyFromMax++;
        }

    }

    public void decToIndex() {
        if (moneyFromMax > 0) {
            moneyFromMax++;
        }
    }

    /**
     * Method for returning max index. Will be zero if empty array so we want to
     * avoid answers of -1.
     *
     * @return
     */
    public int returnMaxFromIndex() {

        return (moneyToMe.size() == 0 ? 0 : moneyToMe.size() - 1);

    }

    /**
     * Method for returning max index. Will be zero if empty array so we want to
     * avoid answers of -1.
     *
     * @return
     */
    public int returnMaxToIndex() {

        return (moneyFromMe.size() == 0 ? 0 : moneyFromMe.size() - 1);

    }

    public void setMinMaxFromIndex(int min, int max) {

        //check for zero-size array. Shouldn't have any, but hacking now
        if (moneyToMe.size() == 0) {
            return;
        }

        if (moneyToMe.get(0).val == min) {

            System.out.println(name + ", found min from: "
                    + moneyToMe.get(0).val);

            moneyToMin = 0;
        } else {
            //go through all vals, look for min
            //Run backwards as min needs look through high to low values
            for (int i = moneyToMe.size() - 1; i > -1; i--) {

                //So if the value is less than the min set by range, we ignore it
                if (moneyToMe.get(i).val > min) {
                    moneyToMin = i;
                }

            }
        }

        if (moneyToMe.get(moneyToMe.size() - 1).val == max) {
            System.out.println(name + ", found max from: "
                    + moneyToMe.get(moneyToMe.size() - 1).val);

            moneyToMax = moneyToMe.size() - 1;
        } else {
            //go through all vals, look for max
            for (int i = 0; i < moneyToMe.size(); i++) {

                //Look forward through array vals, low to high. If max is less than this,
                //set to that val. Will stop setting above that.
                if (moneyToMe.get(i).val < max) {
                    moneyToMax = i;
                }

            }
        }

    }//end method setMinMaxFromIndex

    public void setMinMaxToIndex(int min, int max) {

        //check for zero-size array. Shouldn't have any, but hacking now
        if (moneyFromMe.size() == 0) {
            return;
        }

        if (moneyFromMe.get(0).val == min) {

            System.out.println(name + ", found min to: "
                    + moneyFromMe.get(0).val);


            moneyFromMin = 0;
        } else {
            //go through all vals, look for min
            //Run backwards as min needs look through high to low values
            for (int i = moneyFromMe.size() - 1; i > -1; i--) {

                //So if the value is less than the min set by range, we ignore it
                if (moneyFromMe.get(i).val > min) {
                    moneyFromMin = i;
                }

            }
        }

        if (moneyFromMe.get(moneyFromMe.size() - 1).val == max) {

            System.out.println(name + ", found max to: "
                    + moneyFromMe.get(moneyFromMe.size() - 1).val);

            moneyFromMax = moneyFromMe.size() - 1;
        } else {
            //go through all vals, look for max
            for (int i = 0; i < moneyFromMe.size(); i++) {
//                for(Edge e : moneyFrom) {

                //Look forward through array vals, low to high. If max is less than this,
                //set to that val. Will stop setting above that.
                if (moneyFromMe.get(i).val < max) {
                    moneyFromMax = i;
                }

            }
        }

    }//end method setMinMaxFromIndex

    //Use compareTo for ranking the order of visited vertices
    //to test for a single "from/to" random walk batch becoming stable
    //i.e. the top visited vertex emerging
    public int compareTo(Object o) {

        int weight1 = tempVisits;
        int weight2 = ((Edge) o).tempVisits;

        if (weight1 > weight2) {
            //p.p("returned 1");
            return 1;
        } else if (weight1 < weight2) {
            //p.p("returned -1");
            return -1;
        } else {
            return 0;
        }

    }
}
