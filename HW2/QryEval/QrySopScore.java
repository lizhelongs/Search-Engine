/**
 *  Copyright (c) 2020, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;
import java.lang.IllegalArgumentException;

/**
 *  The SCORE operator for all retrieval models.
 */
public class QrySopScore extends QrySop {

  /**
   *  Document-independent values that should be determined just once.
   *  Some retrieval models have these, some don't.
   */
  
  /**
   *  Indicates whether the query has a match.
   *  @param r The retrieval model that determines what is a match
   *  @return True if the query matches, otherwise false.
   */

  public boolean docIteratorHasMatch (RetrievalModel r) {
    return this.docIteratorHasMatchFirst (r);
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
    else if(r instanceof RetrievalModelRankedBoolean) {
      return this.getScoreRankedBoolean (r);
    }
    else if(r instanceof Indri) {
        return this.getScoreIndri (r);
    }
    else if(r instanceof BM25) {
      return this.getScoreBM25 (r);
    }

    //  STUDENTS::
    //  Add support for other retrieval models here.

    else {
      throw new IllegalArgumentException
        (r.getClass().getName() + " doesn't support the SCORE operator.");
    }
  }
  
  /**
   *  getScore for the Unranked retrieval model.
   *  @param r The retrieval model that determines how scores are calculated.
   *  @return The document score.
   *  @throws IOException Error accessing the Lucene index
   */
  public double getScoreUnrankedBoolean (RetrievalModel r) throws IOException {
    if (! this.docIteratorHasMatchCache()) {
      return 0.0;
    } else {
      return 1.0;
    }
  }

  public double getScoreRankedBoolean (RetrievalModel r) throws IOException {
    if (! this.docIteratorHasMatchCache()) {
      return 0.0;
    } else {
      QryIop q = this.getArg(0);
      return (double)q.docIteratorGetMatchPosting().tf;
    }
  }

  public double getDefaultScore(RetrievalModel r, int docId) throws IOException {
    if(! (r instanceof Indri)) {
      throw new IllegalArgumentException
              (r.getClass().getName() + " doesn't support the SCORE operator.");
    }
    if(defaultIsSet){
      QryIop iop = this.getArg(0);
      String field = iop.getField();
      return (1-Indri.lambda)*(0+defaultValues[0])/(Indri.mu + Idx.getFieldLength(field, docId)) + defaultValues[1];
    }else{
      double[] info = Indri.getInfo();
      double mu = info[0];
      double lambda = info[1];
      QryIop iop = this.getArg(0);
      String field = iop.getField();
      double mle = iop.getCtf()/(double)Idx.getSumOfFieldLengths(field);
      defaultValues[0] = mu*mle;
      defaultValues[1] = lambda*mle;
      defaultIsSet = true;
      return (1-Indri.lambda)*(0+defaultValues[0])/(Indri.mu + Idx.getFieldLength(field, docId)) + defaultValues[1];
    }
  }

  private double getScoreIndri (RetrievalModel r) throws IOException {  // for each query over docs
    if (! this.docIteratorHasMatchCache()) {
      return 0.0;
    } else {
      if(defaultIsSet){
        int docId = this.docIteratorGetMatch();
        QryIop iop = this.getArg(0);
        String field = iop.getField();
        int tf = iop.docIteratorGetMatchPosting().tf;
        return (1-Indri.lambda)*(tf+defaultValues[0])/(Indri.mu + Idx.getFieldLength(field, docId)) + defaultValues[1];
      }else{
        double[] info = Indri.getInfo();
        double mu = info[0];
        double lambda = info[1];
        int docId = this.docIteratorGetMatch();
        QryIop iop = this.getArg(0);
        String field = iop.getField();
        int tf = iop.docIteratorGetMatchPosting().tf;
        double mle = iop.getCtf()/(double)Idx.getSumOfFieldLengths(field);
        defaultValues[0] = mu*mle;
        defaultValues[1] = lambda*mle;
        defaultIsSet = true;
        return (1-Indri.lambda)*(tf+defaultValues[0])/(Indri.mu + Idx.getFieldLength(field, docId)) + defaultValues[1];
      }
    }
  }

  private double getScoreBM25 (RetrievalModel r) throws IOException {
    if (! this.docIteratorHasMatchCache()) {
      return 0.0;
    } else {
      QryIop q = this.getArg(0);
      String field = q.getField();
      float N = Idx.getNumDocs();
      int df = q.invertedList.df;
      double idf = Math.log((N-df+0.5)/(df+0.5));
      double[] info = BM25.getInfo();
      //double user_weight = (info[2]+1)/(info[2]+1);
      int docid = this.docIteratorGetMatch();
      int tf = q.docIteratorGetMatchPosting().tf;
      double normLen = (double)Idx.getFieldLength(field, docid)*Idx.getDocCount(field)/(double)Idx.getSumOfFieldLengths(field);
      double cur = (double)tf/(tf + info[1]*(1-info[0]+info[0]*normLen));
      return cur*idf;
    }
  }
  /**
   *  Initialize the query operator (and its arguments), including any
   *  internal iterators.  If the query operator is of type QryIop, it
   *  is fully evaluated, and the results are stored in an internal
   *  inverted list that may be accessed via the internal iterator.
   *  @param r A retrieval model that guides initialization
   *  @throws IOException Error accessing the Lucene index.
   */
  public void initialize (RetrievalModel r) throws IOException {

    Qry q = this.args.get (0);
    q.initialize (r);
  }

}
