/**
 *  Copyright (c) 2020, Carnegie Mellon University.  All Rights Reserved.
 */
import java.io.*;
import java.util.*;

/**
 *  The SYN operator for all retrieval models.
 */
public class QryIopNear extends QryIop {

    int dist = 0;

    public QryIopNear(int dist){
        this.dist = dist;
    }

    private boolean advanceUntilSuccess() {
        QryIop q_0 = (QryIop) this.args.get(0);
        int prev = q_0.locIteratorGetMatch();
        for (int i = 1; i < this.args.size(); i++){
            QryIop q_i = (QryIop) this.args.get(i);
            if (q_i.locIteratorGetMatch() - prev <= dist) {
                prev = q_i.locIteratorGetMatch();
            } else {
                return false;
            }
        }
        return true;
    }
    private boolean advanceUntilIncreasing(int start){
        QryIop q_0 = (QryIop)this.args.get(start);
        if(!q_0.locIteratorHasMatch()){  // runOut of loc
            return true;
        }
        int curLoc = q_0.locIteratorGetMatch();
        for(int i = start+1; i < this.args.size(); i++) {
            QryIop q_i = (QryIop)this.args.get(i);
            q_i.locIteratorAdvancePast(curLoc);
            if(!q_i.locIteratorHasMatch()){  // runOut of loc
                return true;
            }
            curLoc = q_i.locIteratorGetMatch();
        }
        return false;
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
            //  Create a new posting that is the union of the posting lists
            //  that match the minDocid.  Save it.
            //  Note:  This implementation assumes that a location will not appear
            //  in two or more arguments.  #SYN (apple apple) would break it.
            List<Integer> positions = new ArrayList<Integer>();
            while (true) {
                boolean runOut = advanceUntilIncreasing(0);  // run out of locations
                if (runOut) {
                    break;
                }
                boolean found = advanceUntilSuccess();
                if(!found) {
                    QryIop q_0 = (QryIop)this.args.get(0);
                    q_0.locIteratorAdvance();
                    continue;
                }
                QryIop q_last = (QryIop)this.args.get(this.args.size()-1);
                positions.add(q_last.locIteratorGetMatch());
                for(int i = 0; i < this.args.size(); i++){  // increment all iterators
                    QryIop q = (QryIop)this.args.get(i);
                    q.locIteratorAdvance();
                }
            }
            Collections.sort (positions);
            if(positions.size() > 0) {
                this.invertedList.appendPosting(minDocid, positions);
            }
            this.args.get(0).docIteratorAdvancePast(minDocid);
        }
    }

}
