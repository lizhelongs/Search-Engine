public class Indri extends RetrievalModel {
    static double mu, lambda;
    public Indri(double mu, double lambda){
        Indri.mu = mu;
        Indri.lambda = lambda;
    }

    public static double[] getInfo(){
        return new double[]{mu, lambda};
    }
    public String defaultQrySopName () {
        return new String ("#and");
    }

}
