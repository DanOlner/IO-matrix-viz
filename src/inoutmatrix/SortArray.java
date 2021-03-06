/*
 * https://github.com/DanOlner danielolner@gmail.com
 */
package inoutmatrix;

import java.util.ArrayList;

/**
 *
 * @author Dan Olner
 * For sorting an array into number order, largest number in
 * the last index adapted from http://www.daniweb.com/code/snippet18.html
 */
public class SortArray {

    static Edge a, b;

    public static void SortArray(int[] array) {

        int temp;

        for (int a = 0; a < array.length; a++) {

            for (int b = 0; b < array.length - 1; b++) {

                if (array[b] < array[b + 1]) {

                    temp = array[b];

                    array[b] = array[b + 1];

                    array[b + 1] = temp;

                }

            }


        }

        for (int a : array) {

            System.out.println("Array sorted thus:" + a);

        }

    }

    /*Version of sort array specifically for taking in an ArrayList
     */
    public static void SortArrayListOfEdges(ArrayList<Edge> array) {

        for (int k = 0; k < array.size(); k++) {

            for (int j = 0; j < array.size() - 1; j++) {

                a = array.get(j);
                b = array.get(j + 1);

                if (a.val < b.val) {

                    //temp = array[b];
                    //array[b] = array[b + 1];
                    //array[b + 1] = temp;
                    array.set(j, b);

                    array.set(j + 1, a);

                }
            }

            for (Edge bd : array) {

                System.out.println("Array sorted thus:" + bd.val);

            }
            
            System.out.println("---");

        }

    }
}
