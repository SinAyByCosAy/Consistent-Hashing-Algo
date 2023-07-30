package ConsistentHashing;

import java.util.*;
class Pair{
    String loc;
    int code;
    Pair(String loc, int code){
        this.loc = loc;
        this.code = code;
    }
}
public class ConsistentHashing {
    public int[] solve(String[] A, String[] B, int[] C) {
        int n = C.length;
        HashMap<String, Integer> serverHashMap = new HashMap<>();
        HashMap<String, ArrayList<Pair>> serverUserMap = new HashMap<>();
        ArrayList<Pair> serverHashPositions = new ArrayList<>();
        int[] result = new int[n];

        for(int i=0;i<n;i++){
            System.out.println("Executing step: "+(i+1));

            int code = userHash(B[i], C[i]);
            String operation = A[i];
            if(operation.equals("ASSIGN")){
                System.out.println("Assigning user with code: "+code);
                System.out.println("Current Server Hash positions: ");
                printServerHashPositions(serverHashPositions);
                System.out.println("Current Server Hash Map: "+serverHashMap);
                System.out.println("Current Server user Map: ");
                printServerUserMap(serverUserMap);

                Pair pair = new Pair(B[i], code);
                assignUser(serverUserMap, serverHashPositions, pair);
                result[i] = code;

                System.out.println("Updated Server User Map: ");
                printServerUserMap(serverUserMap);
            }else if(operation.equals("ADD")){
                System.out.println("Adding Server "+B[i]+" with Hash Code: "+code);
                System.out.println("Current Server Hash positions: ");
                printServerHashPositions(serverHashPositions);
                System.out.println("Current Server Hash Map: "+serverHashMap);
                System.out.println("Current Server user Map: ");
                printServerUserMap(serverUserMap);

                Pair pair = new Pair(B[i], code);
                serverHashMap.put(B[i], code);
                addServer(serverUserMap, serverHashPositions, pair);
                result[i] = serverUserMap.get(B[i]).size();

                System.out.println("Updated Server Hash positions: ");
                printServerHashPositions(serverHashPositions);
                System.out.println("Updated Server Hash Map: "+serverHashMap);
                System.out.println("Updated Server user Map: ");
                printServerUserMap(serverUserMap);
            }else{
                System.out.println("Removing server: "+B[i]+" with Hash code: "+serverHashMap.get(B[i]));
                System.out.println("Current Server Hash positions: ");
                printServerHashPositions(serverHashPositions);
                System.out.println("Current Server Hash Map: "+serverHashMap);
                System.out.println("Current Server user Map: ");
                printServerUserMap(serverUserMap);

                Pair pair = new Pair(B[i], serverHashMap.get(B[i]));
                result[i] = serverUserMap.get(B[i]).size();
                removeServer(serverUserMap, serverHashPositions, pair);
                serverHashMap.remove(B[i]);

                System.out.println("Updated Server Hash positions: ");
                printServerHashPositions(serverHashPositions);
                System.out.println("Updated Server Hash Map: "+serverHashMap);
                System.out.println("Updated Server user Map: ");
                printServerUserMap(serverUserMap);
            }
            System.out.println(Arrays.toString(result));
        }
        return result;
    }
    void assignUser(HashMap<String, ArrayList<Pair>> serverUserMap, ArrayList<Pair> serverHashPositions, Pair pair){
        int insertPos = getInsertPos(serverHashPositions, pair);
        Pair server;
        if(insertPos == serverHashPositions.size()){
            //serve req by the 0th server
            server = serverHashPositions.get(0);
        }else{
            //serve req by the insertPos' server
            server = serverHashPositions.get(insertPos);
        }
        System.out.println("User with hash code - "+pair.code+" will be inserted at "+insertPos+" index and will be served by : "+server);
        int insertMapPos = getInsertPos(serverUserMap.get(server.loc), pair);
        serverUserMap.get(server.loc).add(insertMapPos, pair);
    }
    void addServer(HashMap<String, ArrayList<Pair>> serverUserMap, ArrayList<Pair> serverHashPositions, Pair pair){
        if(serverHashPositions.size() == 0){
            serverHashPositions.add(0, pair);
            serverUserMap.put(pair.loc, new ArrayList<Pair>());
        }else{
            int insertPos = getInsertPos(serverHashPositions, pair);
            System.out.println("Position to insert the new user: "+insertPos);
            serverUserMap.put(pair.loc, new ArrayList<Pair>());
            int insertMapPos;
            Pair server;
            if(insertPos == serverHashPositions.size()){
                System.out.println("End "+insertPos);
                // server 0's load might need to be adjusted
                server = serverHashPositions.get(0);
                //we have to find all users having hash code greater than first server's code
                int insertPosNewServer = getInsertPos(serverUserMap.get(server.loc), pair);
                int insertPosOldServer = getInsertPos(serverUserMap.get(server.loc), server);
                redistributeEndLoad(serverUserMap, insertPosNewServer, insertPosOldServer, server, pair);
            }else if(insertPos == 0){
                System.out.println("Start "+insertPos);
                // server 0's load might need to be adjusted
                server = serverHashPositions.get(0);
                int insertPosNewServer = getInsertPos(serverUserMap.get(server.loc), pair);
                int insertPosOldServer = getInsertPos(serverUserMap.get(server.loc), server);
                redistributeFrontLoad(serverUserMap, insertPosNewServer, insertPosOldServer, server, pair);
            }else{
                System.out.println("Mid "+insertPos);
                // server at insertPos' load might need to be adjusted
                server = serverHashPositions.get(insertPos);
                insertMapPos = getInsertPos(serverUserMap.get(server.loc), pair);
                redistributeMidLoad(serverUserMap, insertMapPos, server, pair);
            }
            serverHashPositions.add(insertPos, pair);
        }
    }
    void removeServer(HashMap<String, ArrayList<Pair>> serverUserMap, ArrayList<Pair> serverHashPositions, Pair pair){
        int removePos = getServerPos(serverHashPositions, pair);
        Pair server;
        if(removePos == serverHashPositions.size() - 1){
            // server 0's load will increase
            server = serverHashPositions.get(0);
            updateBackLoad(serverUserMap, pair, server);
        }else{
            // server after removePos' load will increase
            server = serverHashPositions.get(removePos+1);
            updateFrontLoad(serverUserMap, pair, server);
        }
        serverHashPositions.remove(removePos);
        serverUserMap.remove(pair.loc);
    }
    int getInsertPos(ArrayList<Pair> serverHashPositions, Pair pair){
        int start = 0;
        int end = serverHashPositions.size();
        while(start < end){
            int mid = (start + end) / 2;
            if(pair.code < serverHashPositions.get(mid).code){
                end = mid;
            }else{
                start = mid + 1;
            }
        }
        return start;
    }
    int getServerPos(ArrayList<Pair> serverHashPositions, Pair pair){
        int start = 0;
        int end = serverHashPositions.size()-1;
        while(start <= end){
            int mid = (start + end) / 2;
            if(pair.code < serverHashPositions.get(mid).code){
                end = mid - 1;
            }else if(pair.code > serverHashPositions.get(mid).code){
                start = mid + 1;
            }else{
                return mid;
            }
        }
        return -1;
    }
    void redistributeEndLoad(HashMap<String, ArrayList<Pair>> serverUserMap, int newPos, int oldPos, Pair oldServer, Pair newServer){
        int n = serverUserMap.get(oldServer.loc).size();
        for(int i=oldPos;i<newPos;i++){
            serverUserMap.get(newServer.loc).add(serverUserMap.get(oldServer.loc).get(i));
        }
        serverUserMap.get(oldServer.loc).subList(oldPos, newPos).clear();
    }
    void redistributeFrontLoad(HashMap<String, ArrayList<Pair>> serverUserMap, int newPos, int oldPos, Pair oldServer, Pair newServer){
        int n = serverUserMap.get(oldServer.loc).size();
        if(newPos == 0 && oldPos == n){
            return;
        }
        for(int i=0;i<newPos;i++){
            serverUserMap.get(newServer.loc).add(serverUserMap.get(oldServer.loc).get(i));
        }
        for(int i=oldPos;i<n;i++){
            serverUserMap.get(newServer.loc).add(serverUserMap.get(oldServer.loc).get(i));
        }
        serverUserMap.get(oldServer.loc).subList(0, newPos).clear();
        serverUserMap.get(oldServer.loc).subList(oldPos-newPos, n-newPos).clear();
    }
    void redistributeMidLoad(HashMap<String, ArrayList<Pair>> serverUserMap, int pos, Pair oldServer, Pair newServer){
        int n = serverUserMap.get(oldServer.loc).size();
        if(pos == 0){
            return;
        }
        for(int i=0;i<pos;i++){
            serverUserMap.get(newServer.loc).add(serverUserMap.get(oldServer.loc).get(i));
        }
        serverUserMap.get(oldServer.loc).subList(0, pos).clear();
    }
    void updateBackLoad(HashMap<String, ArrayList<Pair>> serverUserMap, Pair currServer, Pair nextServer){
        ArrayList<Pair> list = serverUserMap.get(currServer.loc);
        serverUserMap.get(nextServer.loc).addAll(list);
    }
    void updateFrontLoad(HashMap<String, ArrayList<Pair>> serverUserMap, Pair currServer, Pair nextServer){
        ArrayList<Pair> list = serverUserMap.get(nextServer.loc);
        serverUserMap.get(currServer.loc).addAll(list);
        serverUserMap.put(nextServer.loc, serverUserMap.get(currServer.loc));
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
    void printServerHashPositions(ArrayList<Pair> serverHashMap){
        for(Pair pair : serverHashMap){
            System.out.print("("+pair.loc+", "+pair.code+") ");
        }
        System.out.println();
    }
    void printServerUserMap(HashMap<String, ArrayList<Pair>> serverUserMap){
        for(Map.Entry<String, ArrayList<Pair>> pair : serverUserMap.entrySet()){
            System.out.print(pair.getKey()+"->{");
            for(Pair ele : pair.getValue()){
                System.out.print("("+ele.loc+", "+ele.code+"), ");
            }
            System.out.print("}, ");
        }
        System.out.println();
    }
    public static void main(String args[]){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of queries");
        int n = sc.nextInt();
        sc.nextLine();
        String A[] = new String[n];
        String B[] = new String[n];
        int C[] = new int[n];
        System.out.println("Enter Instruction");
        for(int i=0;i<n;i++) {
            A[i] = sc.next();
        }
        System.out.println("Enter Server/User");
        for(int i=0;i<n;i++) {
            B[i] = sc.next();
        }
        System.out.println("Enter hash key");
        for(int i=0;i<n;i++) {
            C[i] = sc.nextInt();
        }
        for(int i=0;i<n;i++){
            System.out.println(A[i]+" "+B[i]+" "+C[i]);
        }
        ConsistentHashing obj = new ConsistentHashing();
        int result[] = obj.solve(A, B, C);
        System.out.println(Arrays.toString(result));
    }
}
