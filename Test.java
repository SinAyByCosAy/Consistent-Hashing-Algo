package ConsistentHashing;

public class Test {
    public static void main(String args[]){
        String[] a = {"ADD","ASSIGN","ASSIGN","ASSIGN","ADD","ASSIGN","ASSIGN","ASSIGN","ADD","ASSIGN","ASSIGN","ASSIGN","ADD","ASSIGN","ASSIGN","ASSIGN","REMOVE","ASSIGN","ASSIGN","ASSIGN","REMOVE","ASSIGN","ASSIGN","ASSIGN","REMOVE","ASSIGN","ASSIGN"};
        String[] b = {"INDIA","GYQF","SSAH","DVTQ","RUSSIA","ZIVQ","VBWW","ACDW","CHINA","YNXC","MWUN","NECZ","GERMANY","OOHQ","RSTZ","WRJJ","INDIA","YLDR","XDFH","SCCV","RUSSIA","QECH","WPCA","ZLVQ","CHINA","RQPJ","PFWJ"};
        int[] c = {947,613,821,701,193,683,19,467,503,347,433,887,971,587,509,283,727,359,443,883,499,487,853,223,137,13,739};
        String s1 = "", s2 = "", s3 = "";
        for(int i=0;i<a.length;i++){
            s1+= a[i]+" ";
            s2+= b[i]+" ";
            s3+= c[i]+" ";
        }
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
    }
}
