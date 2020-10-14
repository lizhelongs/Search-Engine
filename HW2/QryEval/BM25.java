public class BM25 extends RetrievalModel {

    static double b, k_1, k_3;

    public BM25(double b, double k_1, double k_3){
        BM25.b = b;
        BM25.k_1 = k_1;
        BM25.k_3 = k_3;
    }

    public static double[] getInfo(){
        return new double[]{b, k_1, k_3};
    }
    public String defaultQrySopName () {
        return new String ("#sum");
    }

}
