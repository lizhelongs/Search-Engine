/**
 *  Copyright (c) 2020, Carnegie Mellon University.  All Rights Reserved.
 */
import java.io.*;
import java.util.*;

/**
 *  The SYN operator for all retrieval models.
 */
public class QryIopWindow extends QryIop {

    int dist = 0;

    public QryIopWindow(int dist){
        this.dist = dist;
    }

    private int[] findMinMax(){
        int min = 0;
        int max = 0;
        if(!this.getArg(0).locIteratorHasMatch()) return new int[]{-1,-1};
        for(int i = 1;i < this.args.size(); i++){
            QryIop q_i = this.getArg(i);
            if(q_i.locIteratorHasMatch()){
                if(q_i.locIteratorGetMatch() < this.getArg(min).locIteratorGetMatch()){
                    min = i;
                }
                if(q_i.locIteratorGetMatch() > this.getArg(max).locIteratorGetMatch()){
                    max = i;
                }
            }else{
                return new int[]{-1,-1};
            }
        }
        return new int[]{min, max};
    }

    private int findMin(){
        int min = 0;
        for(int i = 1;i < this.args.size(); i++){
            QryIop q_i = this.getArg(i);
            if(q_i.locIteratorGetMatch() < this.getArg(min).locIteratorGetMatch()){
                min = i;
            }
        }
        return min;
    }

    /**
     *  Evaluate the query operator; the result is an internal inverted
     *  list that may be accessed via the internal iterators.
     *  @throws IOException Error accessing the Lucene index.
     */
    protected void evaluate () throws IOException {

        //  Create an empty inverted list.  If there are no query arguments,
        //  this is the final result.

        this.invertedList = new InvList (this.getField());

        if (args.size () == 0) {
            return;
        }

        //  Each pass of the loop adds 1 document to result inverted list
        //  until all of the argument inverted lists are depleted.

        while (true) {

            //  Find the minimum next document id.  If there is none, we're done.
            if(!this.docIteratorHasMatchAll(null)){
                break;
            }
            int minDocid = this.args.get(0).docIteratorGetMatch();

            List<Integer> positions = new ArrayList<Integer>();

            int[] minMax;
            minMax = findMinMax();  // check for run out
            int min = minMax[0];
            int max = minMax[1];
            while (true) {
                if(this.getArg(max).locIteratorGetMatch() - this.getArg(min).locIteratorGetMatch() >= dist){  // not a match
                    this.getArg(min).locIteratorAdvance();
                    if(!this.getArg(min).locIteratorHasMatch()) break;  // break if run out
                    if(this.getArg(min).locIteratorGetMatch() > this.getArg(max).locIteratorGetMatch()){
                        max = min;
                    }
                    min = findMin();
                    continue;
                }
                positions.add(this.getArg(max).locIteratorGetMatch());
                for(int i = 0; i < this.args.size(); i++){  // increment all iterators
                    this.getArg(i).locIteratorAdvance();
                }
                minMax = findMinMax();  // check for run out
                if (minMax[0] == -1) {
                    break;
                }
                min = minMax[0];
                max = minMax[1];
            }
            if(positions.size() > 0) {
                Collections.sort (positions);
                this.invertedList.appendPosting(minDocid, positions);
            }
            this.args.get(0).docIteratorAdvancePast(minDocid);
        }
    }

}
