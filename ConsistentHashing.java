package ConsistentHashing;

public class ConsistentHashing {
    public int[] solve(String[] A, String[] B, int[] C) {
        int n = C.length;
        HashMap<Integer, ArrayList<Integer>> serverUserMap = new HashMap<>();
        ArrayList<Integer> serverHashPositions = new ArrayList<>();
        int[] result = new int[n];

        for(int i=0;i<n;i++){
            int code = userHash(B[i], C[i]);
            String operation = A[i];
            int insertPos, insertMapPos, removePos;
            int serverID;
            if(operation.equals("ASSIGN")){
                insertPos = getInsertPos(serverHashPositions, code);
                if(insertPos == serverHashPositions.size()){
                    //serve req by the 0th server
                    serverID = serverHashPositions.get(0);
                }else{
                    //serve req by the insertPos' server
                    serverID = serverHashPositions.get(insertPos);
                }
                insertMapPos = getInsertPos(serverUserMap.get(serverID), code);
                serverUserMap.get(serverID).add(insertMapPos, code);
                serverUserMap.put(serverID, serverUserMap.get(serverID));
                result[i] = code;
            }else if(operation.equals("ADD")){
                if(serverHashPositions.size() == 0){
                    serverHashPositions.add(0, code);
                    serverUserMap.put(code, new ArrayList<Integer>());
                    insertMapPos = 0;
                }else{
                    insertPos = getInsertPos(serverHashPositions, code);
                    if(insertPos == serverHashPositions.size()){
                        // server 0's load might need to be adjusted
                        serverID = serverHashPositions.get(0);
                    }else{
                        // server at insertPos' load might need to be adjusted
                        serverID = serverHashPositions.get(insertPos);
                    }
                    //add new server to hash mapping
                    serverHashPositions.add(insertPos, code);
                    //add the new server to map with users
                    serverUserMap.put(code, new ArrayList<Integer>());
                    //check if users need to be redistributed
                    insertMapPos = getInsertPos(serverUserMap.get(serverID), code);
                    if(insertMapPos != 0){
                        for(int j=0;j<insertMapPos;j++){
                            //copy user from the previous server to the new server
                            serverUserMap.get(code).add(serverUserMap.get(serverID).get(0));
                            serverUserMap.put(code, serverUserMap.get(code));
                            //remove user from the previous server
                            serverUserMap.get(serverID).remove(0);
                            serverUserMap.put(serverID, serverUserMap.get(serverID));
                        }
                    }
                }
                result[i] = insertMapPos;
            }else{
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
                result[i] = serverUserMap.get(code).size();
                serverHashPositions.remove(removePos);
                serverUserMap.remove(code);
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
}
