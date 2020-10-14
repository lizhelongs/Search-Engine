import java.util.*;
import java.io.*;
public class createQrySeq {

    public static void main(String[] args) throws IOException {
        File input= new File("TEST_DIR/HW2-Exp-5.1a.qry");
        //PrintWriter out = new PrintWriter(new FileOutputStream("TEST_DIR/HW2-Exp-4.1b.qry"));
        Scanner sc = new Scanner(input);
        while(sc.hasNextLine()){
            String oline = "";
            String line = sc.nextLine();
            String[] A = line.split(":");
            oline += A[0] + ": #WAND(";
            String[] B = A[1].trim().split(" ");  // terms
            oline += "0.9 #AND (";
            for(int i = 0; i < B.length; i++){  // and
                oline += B[i] + " ";
            }
            oline += ") ";
            oline += "0.1 #AND (";
            for(int i = 0; i < B.length-1; i++){  //  near
                oline += "#near/1 (";
                oline += B[i] + " " + B[i+1];
                oline += ") ";
            }
            oline += ") ";
            oline += "0.5 #AND (";
            for(int i = 0; i < B.length-1; i++){  // window
                oline += "#window/10 (";
                oline += B[i] + " " + B[i+1];
                oline += ") ";
            }
            oline += ") ";
            oline += ") ";
            System.out.println(oline);
        }

    }
}
