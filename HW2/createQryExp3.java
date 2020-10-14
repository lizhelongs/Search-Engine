import java.util.*;
import java.io.*;
public class createQryExp3 {

    public static void main(String[] args) throws IOException {
        File input= new File("TEST_DIR/HW2-Exp-4.1a.qry");
        //PrintWriter out = new PrintWriter(new FileOutputStream("TEST_DIR/HW2-Exp-4.1b.qry"));
        double[] weights = new double[]{0.1, 0.0, 0.1, 0.8};
        String[] fields = new String[]{".url", ".keywords", ".title", ".body"};
        Scanner sc = new Scanner(input);
        while(sc.hasNextLine()){
            String oline = "";
            String line = sc.nextLine();
            String[] A = line.split(":");
            oline += A[0] + ": #AND(";
            String[] B = A[1].trim().split(" ");
            for(int i = 0; i < B.length; i++){
                oline += "#WSUM(";
                for(int j = 0; j < 4; j++){
                    if(weights[j] == 0.0) continue;
                    oline += " " + weights[j] + " " + B[i] + fields[j];
                }
                oline += ")";
            }
            oline += ")";
            System.out.println(oline);
        }

    }
}
