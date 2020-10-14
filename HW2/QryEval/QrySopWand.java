/**
 *  Copyright (c) 2020, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;

/**
 *  The AND operator for all retrieval models.
 */
public class QrySopWand extends QrySop {

    /**
     *  Indicates whether the query has a match.
     *  @param r The retrieval model that determines what is a match
     *  @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch (RetrievalModel r) {
        return this.docIteratorHasMatchMin (r);
    }

    /**
     *  Get a score for the document that docIteratorHasMatch matched.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getScore (RetrievalModel r) throws IOException {

        if(r instanceof Indri){
            return this.getScoreIndri (r);
        }

        //  STUDENTS::
        //  Add support for other retrieval models here.

        else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the OR operator.");
        }
    }

    public double getDefaultScore(RetrievalModel r, int docid) throws IOException {
        if(! (r instanceof Indri)) {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the SCORE operator.");
        }
        double res = 1.0;
        double totalWeight = this.totalWeight;
        for(int i = 0; i < this.args.size(); i++){
            QrySop q = (QrySop) this.args.get(i);
            double weight = q.weight;
            res *= Math.pow(q.getDefaultScore(r, docid), weight/totalWeight);
        }
        return res;
    }

    private double getScoreIndri (RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double res = 1.0;
            double totalWeight = this.totalWeight;
            int docid = this.docIteratorGetMatch();
            for(int i = 0; i < this.args.size(); i++){
                QrySop sop = (QrySop) this.args.get(i);
                double weight = sop.weight;
                if(!sop.docIteratorHasMatch(r) ||sop.docIteratorGetMatch() != docid) {
                    double cur = sop.getDefaultScore(r, docid);
                    res *= Math.pow(cur, weight/totalWeight);
                }
                else{
                    double cur = sop.getScore(r);
                    res *= Math.pow(cur, weight/totalWeight);
                }
            }
            return res;
        }
    }

}
