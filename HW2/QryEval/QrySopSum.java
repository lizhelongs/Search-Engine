/**
 *  Copyright (c) 2020, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;

/**
 *  The AND operator for all retrieval models.
 */
public class QrySopSum extends QrySop {

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

        if(r instanceof BM25){
            return this.getScoreBM25 (r);
        }

        //  STUDENTS::
        //  Add support for other retrieval models here.

        else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the OR operator.");
        }
    }

    /**
     *  getScore for the UnrankedBoolean retrieval model.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */

    public double getDefaultScore(RetrievalModel r, int docid) throws IOException {
        if(! (r instanceof Indri)) {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the SCORE operator.");
        }
        return 0.0;
    }


    private double getScoreBM25 (RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double sum = 0.0;
            int docid = this.docIteratorGetMatch();
            for(int i = 0; i < this.args.size(); i++){ // sum over query terms
                QrySop q = (QrySop) this.args.get(i);
                if(!q.docIteratorHasMatch(r)) continue;
                if(q.docIteratorGetMatch() == docid) {
                    double cur = q.getScore(r);
                    sum += cur;
                }
            }
            return sum;
        }
    }

}
