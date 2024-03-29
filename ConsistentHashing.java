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
class ServerPos{
    int index;
    Boolean found;
    ServerPos(int index, Boolean found){
        this.index = index;
        this.found = found;
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

                Pair pair = new Pair(B[i], code);
                assignUser(serverUserMap, serverHashPositions, pair);
                result[i] = code;
            }else if(operation.equals("ADD")){
                Pair pair = new Pair(B[i], code);
                serverHashMap.put(B[i], code);
                addServer(serverUserMap, serverHashPositions, pair);
                result[i] = serverUserMap.get(B[i]).size();
            }else{
                Pair pair = new Pair(B[i], serverHashMap.get(B[i]));
                result[i] = serverUserMap.get(B[i]).size();
                removeServer(serverUserMap, serverHashPositions, pair);
                serverHashMap.remove(B[i]);
            }
            System.out.println(Arrays.toString(result));
        }
        return result;
    }
    void assignUser(HashMap<String, ArrayList<Pair>> serverUserMap, ArrayList<Pair> serverHashPositions, Pair pair){
        int insertPos = getInsertPos(serverHashPositions, pair);
        Pair server;
        int serverPos;
        //when server and user have same hash
        if(insertPos !=0 && pair.code == serverHashPositions.get(insertPos-1).code)
            insertPos--;
        if(insertPos == serverHashPositions.size()){
            //serve req by the 0th server
            serverPos = 0;
        }else{
            //serve req by the insertPos' server
            serverPos = insertPos;
        }
        while(serverPos+1 < serverHashPositions.size() && serverHashPositions.get(serverPos).code == serverHashPositions.get(serverPos+1).code){
            serverPos++;
        }
        server = serverHashPositions.get(serverPos);
        int insertMapPos = getInsertPos(serverUserMap.get(server.loc), pair);
        serverUserMap.get(server.loc).add(insertMapPos, pair);
    }
    void addServer(HashMap<String, ArrayList<Pair>> serverUserMap, ArrayList<Pair> serverHashPositions, Pair pair){
        if(serverHashPositions.size() == 0){
            serverHashPositions.add(0, pair);
            serverUserMap.put(pair.loc, new ArrayList<Pair>());
        }else{
            int insertPos = getInsertPos(serverHashPositions, pair);
            serverUserMap.put(pair.loc, new ArrayList<Pair>());
            int insertMapPos;
            Pair server;
            int serverPos;
            if(insertPos == serverHashPositions.size()){
                if(serverHashPositions.get(insertPos-1).code != pair.code){
                    // server 0's load might need to be adjusted
                    //duplicate old server's load should not be redistributed when a new server is added
                    serverPos = 0;
                    while(serverPos+1 < serverHashPositions.size() && serverHashPositions.get(serverPos).code == serverHashPositions.get(serverPos+1).code){
                        serverPos++;
                    }
                    server = serverHashPositions.get(serverPos);
                    //we have to find all users having hash code greater than first server's code
                    int insertPosNewServer = getInsertPos(serverUserMap.get(server.loc), pair);
                    int insertPosOldServer = getInsertPos(serverUserMap.get(server.loc), server);
                    redistributeEndLoad(serverUserMap, insertPosNewServer, insertPosOldServer, server, pair);
                }
            }else if(insertPos == 0){
                // server 0's load might need to be adjusted
                server = serverHashPositions.get(0);
                int insertPosNewServer = getInsertPos(serverUserMap.get(server.loc), pair);
                int insertPosOldServer = getInsertPos(serverUserMap.get(server.loc), server);
                redistributeFrontLoad(serverUserMap, insertPosNewServer, insertPosOldServer, server, pair);
            }else{
                if(serverHashPositions.get(insertPos-1).code != pair.code) {
                    //duplicate old servers load shouldn't be redistributed when a new server is added
                    serverPos = insertPos;
                    while(serverPos+1 < serverHashPositions.size() && serverHashPositions.get(serverPos).code == serverHashPositions.get(serverPos+1).code){
                        serverPos++;
                    }
                    // server at insertPos' load might need to be adjusted
                    server = serverHashPositions.get(serverPos);
                    insertMapPos = getInsertPos(serverUserMap.get(server.loc), pair);
                    redistributeMidLoad(serverUserMap, insertMapPos, server, pair);
                }
            }
            serverHashPositions.add(insertPos, pair);
        }
    }
    void removeServer(HashMap<String, ArrayList<Pair>> serverUserMap, ArrayList<Pair> serverHashPositions, Pair pair){
        ServerPos serverPos = getServerPos(serverHashPositions, pair);
        int removePos = -1;
        if(serverPos.found){
            removePos = serverPos.index;
        }else{
            boolean flag = false;
            int index=-1;
            for(int i=serverPos.index;i>=0;i--){
                if(serverHashPositions.get(i).loc == pair.loc){
                    index = i;
                    flag = true;
                    break;
                }
                if(serverHashPositions.get(i).code != pair.code){
                    index = i+1;
                    break;
                }
            }
            if(flag) removePos = index;
            else{
                for(int i=index;i<serverHashPositions.size();i++){
                    if((serverHashPositions.get(i).loc).equals(pair.loc)){
                        removePos = i;
                        break;
                    }
                }
            }
        }
        ArrayList<Pair> removeList = serverUserMap.get(pair.loc);
        serverHashPositions.remove(removePos);
        serverUserMap.remove(pair.loc);
        for(Pair addUser : removeList){
            assignUser(serverUserMap, serverHashPositions, new Pair(addUser.loc, addUser.code));
        }
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
    ServerPos getServerPos(ArrayList<Pair> serverHashPositions, Pair pair){
        int start = 0;
        int end = serverHashPositions.size()-1;
        int index = -1;
        while(start <= end){
            int mid = (start + end) / 2;
            if(pair.code < serverHashPositions.get(mid).code){
                end = mid - 1;
            }else if(pair.code > serverHashPositions.get(mid).code){
                start = mid + 1;
            }else{
                if((pair.loc).equals(serverHashPositions.get(mid).loc))
                return new ServerPos(mid, true);
                index = mid;
                end = mid - 1;
            }
        }
        return new ServerPos(index, false);
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
            System.out.println((i+1)+" -> "+A[i]+" "+B[i]+" "+C[i]);
        }
        ConsistentHashing obj = new ConsistentHashing();
        int result[] = obj.solve(A, B, C);
        System.out.println(Arrays.toString(result));
    }
}
