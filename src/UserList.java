//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.Map.Entry;

public class UserList {
//    private static Logger logger = LoggerFactory.getLogger(UserList.class);
    private float alpha = 0.75f;
    private Map<String, UserInfo> userList; // ID map userInformation
    private int index = 2; //对应于论文中公式2-9的伽马
    private int walk_step = 3;

    public UserList() {
        userList = new LinkedHashMap<String, UserInfo>();   //map,userID：UserInfo
    }

     LinkedList<String> AllUser() {
        Collection<String> keyset = userList.keySet();
        LinkedList<String> list = new LinkedList<String>(keyset);
        return list;
    }

    private void setUserList(Map<String, UserInfo> userList) {
        this.userList = userList;
    }

    UserInfo getUser(String id) {
        return userList.get(id);
    }

    private void addUser(String key, UserInfo value) {
        userList.put(key, value);
    }

    // create userList
    public void createUserList(File fileName) throws IOException {
//        logger.debug("Reading text file " + fileName + "...");
        String pltLine;
        String[] elementStrings;
        RandomAccessFile plt = new RandomAccessFile(fileName, "r");
        int num = 0;
        while ((pltLine = plt.readLine()) != null) {
            elementStrings = pltLine.split(" ");
            addUser(elementStrings[0], new UserInfo(elementStrings[0], num));
            num++;
        }
        try {
            plt.close();
            System.out.println("creating userlist finished ");
        } catch (IOException e) {
            // TODO: handle exception while trying to close a
            e.printStackTrace();
        }
        setUserList(userList);
    }

    // 用户的邻接列表
    public void createAdjMember(File fileName, boolean directer) throws IOException {
//        logger.debug("Reading text file " + fileName + "...");
        String pltLine;
        String[] elementStrings;
        RandomAccessFile plt = new RandomAccessFile(fileName, "r");
        if (directer) {
            System.out.println("directedGraph");
            while ((pltLine = plt.readLine()) != null) {
                elementStrings = pltLine.split(" ");
                getUser(elementStrings[0]).addAdjOut(elementStrings[1]);
                getUser(elementStrings[1]).addAdjIn(elementStrings[0]);
            }
        } else {
            System.out.println("unDirectedGraph");
            while ((pltLine = plt.readLine()) != null) {
                elementStrings = pltLine.split(" ");
                getUser(elementStrings[0]).addAdjOut(elementStrings[1]);
                getUser(elementStrings[0]).addAdjIn(elementStrings[1]);
                getUser(elementStrings[1]).addAdjOut(elementStrings[0]);
                getUser(elementStrings[1]).addAdjIn(elementStrings[0]);
            }
        }
        try {
            plt.close();
            // writer.close();
        } catch (IOException e) {
            // TODO: handle exception while trying to close a
            // *.plt
            e.printStackTrace();
        }
    }

    private void count_n_hop_neighbor(UserInfo u, int hop) {
        for (int i = 1; i < hop; i++)
            for (String neighbor : u.AllHip1User()) {
                u.n_hop_neighbor.addAll(getUser(neighbor).AllHip1User());
            }
    }

    private float[][] mmltiple(float[][] a, float[][] b) {
        float[][] result = new float[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                for (int k = 0; k < a[0].length; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    private float[][] add(float[][] a, float[][] b) {
        float[][] result = new float[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;
    }

    private void LRW(UserInfo u,float[][]transMatrix,float[][]simMatrix,float[][]tempMatrix) {
        Map<Integer, String> seq2id = new HashMap<>();
        Map<String, Integer> id2seq = new HashMap<>();
        int seq = 0;
        for (String user : u.n_hop_neighbor) {
            seq2id.put(seq, user);
            id2seq.put(user, seq++);
        }
        int n_hop_neighbor_size = u.n_hop_neighbor.size();
        for (int row = 0; row < n_hop_neighbor_size; row++) {   //initial 1-step transMatrix and simMatrix
            for (int col = 0; col < n_hop_neighbor_size; col++) {
                simMatrix[row][col] = 0;
                tempMatrix[row][col] = 0;
                if (getUser(seq2id.get(row)).AllHip1User().contains(seq2id.get(col)))
                    transMatrix[row][col] = getUser(seq2id.get(row)).get1_simValue(seq2id.get(col));
                else
                    transMatrix[row][col] = 0;
            }
        }
        simMatrix =  transMatrix; //initial beginning simMatrix
        tempMatrix = transMatrix;
        for (int hop = 1; hop < walk_step; hop++) {
            tempMatrix= mmltiple(tempMatrix, transMatrix);
            simMatrix = add(simMatrix,tempMatrix );
        }
        int target_user_seq = id2seq.get(u.ID);
        for (int sequence = 0; sequence < n_hop_neighbor_size; sequence++) {
            String v_id=seq2id.get(sequence);
            float sim_uv= simMatrix[target_user_seq][sequence];
            UserInfo v=getUser(v_id);
            float weight=(float)Math.pow(sim_uv, index) * v.getIDegree(); //force compute
//            u.add_Sim(v_id,weight); //save the corelation
            u.add_wei(v_id,weight);
        }
        transMatrix = null;
        simMatrix = null;
        tempMatrix= null ;

}

    //initialazation. includes: 1-step correlation and random walk
     int initUserInfo(boolean perturb, double epsilon) {
        LinkedList<String> userSet = AllUser();
       // int userNum = userSet.size();
        int ousumEdge = 0;
        int insumEdge = 0;
         for (String ID : userSet) {
             UserInfo u = getUser(ID);
             u.setDegree();
             ousumEdge += u.getOutDegree();
             insumEdge += u.getInDegree();
         }
        System.out.println("sum of odegree " + ousumEdge + "sum of idegree " + insumEdge);
         for (String anUserSet : userSet) {  //为每个用户初始化one-hop相关度
             float sum = 0.0f;
             UserInfo u = getUser(anUserSet);
             if (!u.adjInList.isEmpty()) {
                 for (String a : u.adjInList) {
                     float sim_ku = (float) (1.0 - alpha) / u.in_degree;
                     u.add_Sim(a, (u.get1_simValue(a) + sim_ku));
                 }
             }
             if (!u.adjOutList.isEmpty()) {
                 for (String a : u.adjOutList) {
                     float sim_ku = alpha / (float) u.out_degree;
                     u.add_Sim(a, ( u.get1_simValue(a) + sim_ku));
                 }
             }
             for (String a : u.candidateSim.keySet()) {
                 sum += u.candidateSim.get(a);
             }
             if (sum != 0.0f) {
                 for (String a : u.candidateSim.keySet()) {
                     float value = u.candidateSim.get(a);
                     u.add_Sim(a, (value / sum));  //初始化相关度归一化处理
                 }
             }
         }
        System.out.println("the initialzation of corelation transition probability finished");
        int osumEdge = 0;
        int isumEdge = 0;
        int numofv=userSet.size();
        int size=0;
         System.out.println("begin to calculate maxsize of n_hop_ndighbor");
         for (String anUserSet : userSet) {
             UserInfo u = getUser(anUserSet);
             for (String a : u.AllHip1User())
                 u.n_hop_neighbor.add(a);
             count_n_hop_neighbor( u, walk_step); //get the n_hop_neighbor
             size = size > u.n_hop_neighbor.size()?size:u.n_hop_neighbor.size();
         }
         System.out.println("maxsize "+size);
         System.out.println("LRW begins");
         float[][] transMatrix = new float[size][size];
         float[][] simMatrix = new float[size][size];
         float[][] tempMatrix= new float[size][size] ;
        int ilrw=0;
        //call random walk correlation computation, you can change the walk_step.
         for (String anUserSet : userSet) {
             UserInfo u = getUser(anUserSet);
             u.setPeDegree(perturb, epsilon,numofv);  //degree differential disturb
             LRW(u,transMatrix,simMatrix,tempMatrix);
             osumEdge += u.getODegree();
             isumEdge += u.getIDegree();
             ilrw++;
             if (ilrw%1000==0){
                 System.out.println("no."+ilrw+" node finised lrw");}
         }
         transMatrix = null;
         simMatrix = null;
         tempMatrix = null;
         System.gc();
         int change = osumEdge-isumEdge;
//         出度大，加入度
         while(change>0){
             int radom = (int) (Math.random() * userSet.size());
             if (getUser(userSet.get(radom)).i_degree<numofv-1){
                 getUser(userSet.get(radom)).i_degree+=1;
                 change-=1;
                 isumEdge+=1;
             }
         }
//         入度大，加出度
         while (change<0){
             int radom = (int) (Math.random() * userSet.size());
             if (getUser(userSet.get(radom)).o_degree<numofv-1){
                 getUser(userSet.get(radom)).o_degree+=1;
                 change+=1;
                 osumEdge+=1;
             }
         }
        System.out.println("after perturbing ,sum of odegree " + osumEdge + "sum of idegree " + isumEdge);
         for (String anUserSet : userSet) {
             UserInfo u = getUser(anUserSet);
             Map<String, Float> candidate = u.candidateSim;
             if (candidate.containsKey(u.ID)) {   //remove itself from its candidates
                 candidate.remove(u.ID);
             }
             LinkedList<String> list1 = new LinkedList<>(candidate.keySet());
             u.setFistCandidateSorted(list1);
         }
        return osumEdge;
    }
}
