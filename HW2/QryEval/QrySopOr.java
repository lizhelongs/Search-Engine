/**
 *  Copyright (c) 2020, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;

/**
 *  The OR operator for all retrieval models.
 */
public class QrySopOr extends QrySop {

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

    if (r instanceof RetrievalModelUnrankedBoolean) {
      return this.getScoreUnrankedBoolean (r);
    }
    else if(r instanceof RetrievalModelRankedBoolean){
      return this.getScoreRankedBoolean (r);
    }
//    else if(r instanceof Indri){
//      return this.getScoreIndri (r);
//    }

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
      double max = Integer.MIN_VALUE;
      int docId = this.docIteratorGetMatch();
      for(int i = 0; i < this.args.size(); i++) {
        QrySop q = (QrySop) this.args.get(i);
        if(!q.docIteratorHasMatch(r)) continue;
        if (q.docIteratorGetMatch() == docId){
          max = Math.max(max, q.getScore(r));
          q.docIteratorAdvancePast(docId);
        }
      }
      return max;
    }
  }

  public double getDefaultScore(RetrievalModel r, int docid) throws IOException {
    double res = 1.0;
    for(int i = 0; i < this.args.size(); i++){
      QrySop sop = (QrySop) this.args.get(i);
      res *= 1-sop.getDefaultScore(r, docid);
    }
    return 1 - res;
  }

//  public double getScoreIndriDefault(RetrievalModel r) throws IOException {
//    double res = 1.0;
//    for(int i = 0; i < this.args.size(); i++){
//      QrySop sop = (QrySop) this.args.get(i);
//      QryIop iop = (QryIop)sop.args.get(0);
//      String field = iop.getField();
//      res *= iop.getCtf()/Idx.getSumOfFieldLengths(field);
//    }
//    return Indri.getInfo()[1]* Math.pow(res, (double)1/this.args.size());
//  }
  private double getScoreIndri (RetrievalModel r) throws IOException {
    if (! this.docIteratorHasMatchCache()) {
      return 0.0;
    } else {
      double res = 1.0;
      int docid = this.docIteratorGetMatch();
      for(int i = 0; i < this.args.size(); i++){
        QrySop sop = (QrySop) this.args.get(i);
        if(!sop.docIteratorHasMatch(r)) continue;
        if(sop.docIteratorGetMatch() == docid) {
          double cur = sop.getScore(r);
          res *= 1- cur;
        }else{
          double cur = sop.getDefaultScore(r, docid);
          res *= 1- cur;
        }
      }
      return 1 - res;
    }
  }

}
