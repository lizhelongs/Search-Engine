/**
 *  Copyright (c) 2020, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;

/**
 *  The AND operator for all retrieval models.
 */
public class QrySopAnd extends QrySop {

    /**
     *  Indicates whether the query has a match.
     *  @param r The retrieval model that determines what is a match
     *  @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch (RetrievalModel r) {
        if(r instanceof Indri || r instanceof BM25) {
            return this.docIteratorHasMatchMin(r);
        }else {
            return this.docIteratorHasMatchAll(r);
        }
    }

    /**
     *  Get a score for the document that docIteratorHasMatch matched.
     *  @param r The retrieval model that determines how scores are calculated.
     *  @return The document score.
     *  @throws IOException Error accessing the Lucene index
     */
    public double getScore (RetrievalModel r) throws IOException {

        if (r instanceof RetrievalModelUnrankedBoolean) {
            return this.getScoreUnrankedBoolean (r);
        }
        else if(r instanceof RetrievalModelRankedBoolean){
            return this.getScoreRankedBoolean (r);
        }
        else if(r instanceof Indri){
            return this.getScoreIndri (r);
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
    private double getScoreUnrankedBoolean (RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            return 1.0;
        }
    }

    private double getScoreRankedBoolean (RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double min = Integer.MAX_VALUE;
            for(int i = 0; i < this.args.size(); i++){
                QrySop sop = (QrySop) this.args.get(i);
                min = Math.min(min, sop.getScore(r));
            }
            return min;
        }
    }

    public double getDefaultScore(RetrievalModel r, int docid) throws IOException {
        if(! (r instanceof Indri)) {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the SCORE operator.");
        }
        double res = 1.0;
        for(int i = 0; i < this.args.size(); i++){
            QrySop q = (QrySop) this.args.get(i);
            res *= q.getDefaultScore(r, docid);
        }
        return Math.pow(res, (double)1/(double)this.args.size());
    }

    private double getScoreIndri (RetrievalModel r) throws IOException {
        if (! this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double res = 1.0;
            int docid = this.docIteratorGetMatch();
            for(int i = 0; i < this.args.size(); i++){
                QrySop sop = (QrySop) this.args.get(i);
                if(!sop.docIteratorHasMatch(r) || sop.docIteratorGetMatch() != docid){  //  sum over query terms
                    double cur = sop.getDefaultScore(r, docid);
                    res *= cur;
                }
                else {
                    double cur = sop.getScore(r);
                    res *= cur;
                }
            }
            return Math.pow(res, (double)1/(double)this.args.size());
        }
    }

}
