package ConsistentHashing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class ConsistentHashing {
    public int[] solve(String[] A, String[] B, int[] C) {
        int n = C.length;
        HashMap<String, Integer> serverHashMap = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> serverUserMap = new HashMap<>();
        ArrayList<Integer> serverHashPositions = new ArrayList<>();
        int[] result = new int[n];

        for(int i=0;i<n;i++){
            System.out.println("Executing step: "+(i+1));
            int code = userHash(B[i], C[i]);
            String operation = A[i];
            int insertPos, insertMapPos, removePos;
            int serverID;
            if(operation.equals("ASSIGN")){
                System.out.println("Assigning");
                System.out.println("Current Server Hash positions: "+serverHashPositions);
                System.out.println("Current Server Hash Map: "+serverHashMap);
                System.out.println("Current Server user Map: "+serverUserMap);
                insertPos = getInsertPos(serverHashPositions, code);
                if(insertPos == serverHashPositions.size()){
                    //serve req by the 0th server
                    serverID = serverHashPositions.get(0);
                }else{
                    //serve req by the insertPos' server
                    serverID = serverHashPositions.get(insertPos);
                }
                System.out.println("User with hash code - "+code+" will be inserted at "+insertPos+" index and will ber served by "+serverHashMap.get(serverID)+" with hash code : "+serverID);
                insertMapPos = getInsertPos(serverUserMap.get(serverID), code);
                serverUserMap.get(serverID).add(insertMapPos, code);
                serverUserMap.put(serverID, serverUserMap.get(serverID));
                System.out.println("Updated Server User Map: "+serverUserMap);
                result[i] = code;
            }else if(operation.equals("ADD")){
                System.out.println("Adding Server "+B[i]+" with Hash Code: "+code);
                System.out.println("Current Server Hash positions: "+serverHashPositions);
                System.out.println("Current Server Hash Map: "+serverHashMap);
                System.out.println("Current Server user Map: "+serverUserMap);
                if(serverHashPositions.size() == 0){
                    serverHashPositions.add(0, code);
                    serverUserMap.put(code, new ArrayList<Integer>());
                    serverHashMap.put(B[i], code);
                    insertMapPos = 0;
                }else{
                    serverHashMap.put(B[i], code);
                    insertPos = getInsertPos(serverHashPositions, code);
                    serverHashPositions.add(insertPos, code);
                    serverUserMap.put(code, new ArrayList<Integer>());
                    if(insertPos == serverHashPositions.size()){
                        // server 0's load might need to be adjusted
                        serverID = serverHashPositions.get(0);
                        //we have to find all users having hash code greater than first server's code
                        insertMapPos = getInsertPos(serverUserMap.get(serverID), serverID);
                        redistributeEndLoad(serverUserMap, insertMapPos, serverID, code);
                    }else if(insertPos == 0){
                        // server 0's load might need to be adjusted
                        serverID = serverHashPositions.get(0);
                        insertMapPos = getInsertPos(serverUserMap.get(serverID), code);
                        redistributeFrontLoad(serverUserMap, );
                    }else{
                        // server at insertPos' load might need to be adjusted
                        serverID = serverHashPositions.get(insertPos);
                        insertMapPos = getInsertPos(serverUserMap.get(serverID), code);
                        redistributeMidLoad();
                    }
//                    //add server to hash code mapping
//                    serverHashMap.put(B[i], code);
//                    //add new server to hash mapping
//                    serverHashPositions.add(insertPos, code);
//                    //add the new server to map with users
//                    serverUserMap.put(code, new ArrayList<Integer>());
//                    //check if users need to be redistributed
//                    insertMapPos = getInsertPos(serverUserMap.get(serverID), code);

                }
                System.out.println("Updated Server Hash positions: "+serverHashPositions);
                System.out.println("Updated Server Hash Map: "+serverHashMap);
                System.out.println("Updated Server user Map: "+serverUserMap);
                System.out.println(serverUserMap.get(code) +" "+serverUserMap.get(code).size());
                result[i] = serverUserMap.get(code).size();
            }else{
                System.out.println("Removing server: "+B[i]+" with Hash code: "+serverHashMap.get(B[i]));
                System.out.println("Current Server Hash positions: "+serverHashPositions);
                System.out.println("Current Server Hash Map: "+serverHashMap);
                System.out.println("Current Server user Map: "+serverUserMap);
                code = serverHashMap.get(B[i]);
                System.out.println("Users being served by Server ID - "+code+" : "+serverUserMap.get(code));
                result[i] = serverUserMap.get(code).size();
                removePos = getServerPos(serverHashPositions, code);
                if(removePos == serverHashPositions.size() - 1){
                    // server 0's load will increase
                    serverID = serverHashPositions.get(0);
                    updateBackLoad(serverUserMap, code, serverID);
                }else{
                    // server after removePos' load will increase
                    serverID = serverHashPositions.get(removePos+1);
                    updateFrontLoad(serverUserMap, code, serverID);
                }
                serverHashPositions.remove(removePos);
                serverUserMap.remove(code);
                serverHashMap.remove(B[i]);
                System.out.println("Updated Server Hash positions: "+serverHashPositions);
                System.out.println("Updated Server Hash Map: "+serverHashMap);
                System.out.println("Updated Server user Map: "+serverUserMap);
            }
        }
        return result;
    }
    int getInsertPos(ArrayList<Integer> serverHashPositions, int code){
        int start = 0;
        int end = serverHashPositions.size();
        while(start < end){
            int mid = (start + end) / 2;
            if(code < serverHashPositions.get(mid)){
                end = mid;
            }else{
                start = mid + 1;
            }
        }
        return start;
    }
    int getServerPos(ArrayList<Integer> serverHashPositions, int code){
        int start = 0;
        int end = serverHashPositions.size()-1;
        while(start <= end){
            int mid = (start + end) / 2;
            if(code < serverHashPositions.get(mid)){
                end = mid - 1;
            }else if(code > serverHashPositions.get(mid)){
                start = mid + 1;
            }else{
                return mid;
            }
        }
        return -1;
    }
    void redistributeEndLoad(HashMap<Integer, ArrayList<Integer>> serverUserMap, int pos, int oldServer, int newServer){
        int n = serverUserMap.get(oldServer).size();
        if(n == pos){
            return;
        }
        for(int i=pos;i<n;i++){
            serverUserMap.get(newServer).add(serverUserMap.get(oldServer).get(i));
        }
        serverUserMap.get(oldServer).subList(pos, n).clear();
    }
    void redistributeStartLoad(HashMap<Integer, ArrayList<Integer>> serverUserMap, int pos, int oldServer, int newServer){
        int n = serverUserMap.get(oldServer).size();
        if(n == pos){
            return;
        }
        for(int i=pos;i<n;i++){
            serverUserMap.get(newServer).add(serverUserMap.get(oldServer).get(i));
        }
        serverUserMap.get(oldServer).subList(pos, n).clear();
    }
    void redistributeEndLoad(HashMap<Integer, ArrayList<Integer>> serverUserMap, int pos, int oldServer, int newServer){
        int n = serverUserMap.get(oldServer).size();
        if(n == pos){
            return;
        }
        for(int i=pos;i<n;i++){
            serverUserMap.get(newServer).add(serverUserMap.get(oldServer).get(i));
        }
        serverUserMap.get(oldServer).subList(pos, n).clear();
    }
    void updateBackLoad(HashMap<Integer, ArrayList<Integer>> serverUserMap, int currServer, int nextServer){
        ArrayList<Integer> list = serverUserMap.get(currServer);
        serverUserMap.get(nextServer).addAll(list);
        serverUserMap.put(nextServer, serverUserMap.get(nextServer));
    }
    void updateFrontLoad(HashMap<Integer, ArrayList<Integer>> serverUserMap, int currServer, int nextServer){
        ArrayList<Integer> list = serverUserMap.get(nextServer);
        serverUserMap.get(currServer).addAll(list);
        serverUserMap.put(nextServer, serverUserMap.get(currServer));
    }
    int userHash(String username, int hashKey){
        int p = hashKey;
        int n = 360;
        long hashCode = 0;
        long p_pow = 1;
        for (int i = 0; i < username.length(); i++) {
            char character = username.charAt(i);
            hashCode = (hashCode + (character - 'A' + 1) * p_pow) % n;
            p_pow = (p_pow * p) % n;
        }
        return (int)hashCode;
    }
    public static void main(String args[]){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of queries");
        int n = sc.nextInt();
        sc.nextLine();
        String A[] = new String[n];
        String B[] = new String[n];
        int C[] = new int[n];
        for(int i=0;i<n;i++){
            System.out.println("Enter Instruction");
            A[i] = sc.nextLine();
            System.out.println("Enter Server/User");
            B[i] = sc.nextLine();
            System.out.println("Enter hash key");
            C[i] = sc.nextInt();
            sc.nextLine();
        }
        ConsistentHashing obj = new ConsistentHashing();
        int result[] = obj.solve(A, B, C);
        System.out.println(Arrays.toString(result));
    }
}
