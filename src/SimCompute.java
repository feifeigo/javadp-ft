//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.sun.xml.internal.bind.v2.model.core.ID;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class SimCompute {
    private UserList ul;
    private List<String> edges;
    private int sumLength;

    public SimCompute() {
        ul = new UserList();
        edges = new ArrayList<String>();
    }

    public static File getFileFromSd(File path, String fileDot) {
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                if (fileName.endsWith(fileDot)) {
                    System.out.println(file);
                    return file;
                }
            }
        }
        System.out.println("there is no file in type of " + fileDot );
        return null;
    }

    public boolean preProcess(File DATASET_PATH,boolean perturb, boolean directer, double epsilon) {
        try {
            ul.createUserList(getFileFromSd(DATASET_PATH, "node"));
            ul.createAdjMember(getFileFromSd(DATASET_PATH, "edge"), directer);
            sumLength = ul.initUserInfo(perturb,epsilon); // 初始化出入度及排序
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public void generateUDLink(File DATASET_PATH,String dataSet) throws IOException { //图生成过程
        LinkedList<String> userSet = ul.AllUser();
        LinkedList<String> sequence = new LinkedList<>();
//        度数为奇数则减1
        if (sumLength % 2 == 1) {
            System.out.println("after perturbing ,sum of degree is odd,deal...");
            for (String i : userSet){
                if (ul.getUser(i).o_degree>1){
                    ul.getUser(i).o_degree = ul.getUser(i).getODegree() - 1;
                    break;
                }
            }
        }
        for (String user : userSet) { //按照出度倍将用户加入sequence中
            for (int num = 0; num < ul.getUser(user).getODegree(); num++) {
                sequence.add(user);
            }
        }
        System.out.println("generating undirecedgraph");
//        可能进入死循环的外层循环试过的点
        Set<String> loop1 = new HashSet<>();
        //        可能进入死循环的内层循环试过的点
        Set<String> loop2 = new HashSet<>();
        while (!sequence.isEmpty()) {
            if (loop1.containsAll(sequence)){
                System.out.println("step into endless loop");
                newdealUD(sequence);
                break;
            }
//            选ID1
            int radom = (int) (Math.random() * sequence.size());
            String ID1 = sequence.get(radom);
            UserInfo node1 = ul.getUser(ID1);
            if (!(node1.firstCandidateSorted).isEmpty()) {
//                候选节点集选IDn
                FtSample ftSample=new FtSample();
                int seqNum=ftSample.getPrizeIndex(node1.firstCandidateSorted,node1.weight);
                String IDn=node1.firstCandidateSorted.get(seqNum);
//                重新选择IDn
                while (!sequence.contains(IDn)) {
                    node1.firstCandidateSorted.remove(IDn);
                    if ((node1.firstCandidateSorted).isEmpty()) {
                        IDn = null;
                        break;
                    } else
                        IDn = node1.firstCandidateSorted.get(ftSample.getPrizeIndex(node1.firstCandidateSorted,node1.weight));
                }
                UserInfo noden = ul.getUser(IDn);
                if (IDn != null) {
                    String edge = ID1 + " " + IDn;
                    node1.Prob_AdjList.add(IDn);
                    noden.Prob_AdjList.add(ID1);
                    sequence.remove(ID1);
                    sequence.remove(IDn);
                    node1.firstCandidateSorted.remove(IDn);
                    noden.firstCandidateSorted.remove(ID1);
                    edges.add(edge);
                }
            }
            else {
                loop1.add(ID1);
                radom = (int) (Math.random() * sequence.size());
                String IDn = sequence.get(radom);
                UserInfo noden = ul.getUser(IDn);
//                已连成边，则重新选IDn
                while (ID1.equals(IDn) || node1.Prob_AdjList.contains(IDn) || noden.Prob_AdjList.contains(ID1)) {
                    radom = (int) (Math.random() * sequence.size());
                    IDn = sequence.get(radom);
                    loop2.add(IDn);
                    if (loop2.containsAll(sequence)){
                        IDn = null;
                        break;
                    }
                }
                if (IDn!=null){
                    String edge = ID1 + " " + IDn;
                    loop1.remove(ID1);
                    node1.Prob_AdjList.add(IDn);
                    noden.Prob_AdjList.add(ID1);
                    sequence.remove(ID1);
                    sequence.remove(IDn);
                    edges.add(edge);
                }
//                不论是否生成边，均清空loop2
                loop2.clear();
            }
        }
        System.out.println("sum of edges: " + edges.size());
        FileUtil.writeTextFile(DATASET_PATH.getAbsolutePath() +dataSet+ ".edges", edges);

    }

    public void newdealUD(LinkedList<String> sequence){
        LinkedList<String> kinl = new LinkedList<>();
        LinkedList<String> kins = new LinkedList<>();
        //        可能进入死循环的外层循环试过的点
        Set<String> loop1 = new HashSet<>();
        //        可能进入死循环的内层循环试过的点
        Set<String> loop2 = new HashSet<>();
//        获取度满的节点
        kinl = ul.AllUser();
        for(int i =0;i<kinl.size();i++){
            if(sequence.contains(kinl.get(i)))
                kinl.remove(i);
        }
//        for (String i:kinl){
//            if (sequence.contains(i))
//                kinl.remove(i);
//        }
        while (!sequence.isEmpty()){
            if (loop1.size()==sequence.size()*(sequence.size()-1)){
                System.out.println("step into endless loop in dealing with endless loop,please try again");
                return;//
            }
//            选IDi
            int radom = (int) (Math.random() *sequence.size());
            String IDi = sequence.get(radom);
            //            选IDj
            radom = (int) (Math.random() *sequence.size());
            String IDj = sequence.get(radom);
//            若IDi只有一个，则不能被选两次
            while ((Collections.frequency(sequence, IDi)==1)&&(IDi==IDj)){
                radom = (int) (Math.random() *sequence.size());
                IDj = sequence.get(radom);
            }
            UserInfo nodei = ul.getUser(IDi);
            UserInfo nodej = ul.getUser(IDj);
            loop1.add(IDi + " " + IDj);
            loop1.add(IDj + " " + IDi);
//            根据度数加入序列
            for (String i :kinl){
                for (int num=0;num<ul.getUser(i).getODegree();num++)
                    kins.add(i);
            }
//            选vk
            radom = (int) (Math.random() *kins.size());
            String IDk = kins.get(radom);
//            选vl
            String IDl=null;
            for (String i :ul.getUser(IDk).Prob_AdjList){
                if ((kinl.contains(i))&&(!ul.getUser(IDi).Prob_AdjList.contains(i))&&(!ul.getUser(IDj).Prob_AdjList.contains(i))){
                    IDl = i;
                    break;
                }
            }
            while (IDl==null){
//           选k
                radom = (int) (Math.random() *kins.size());
                IDk = kins.get(radom);
                loop2.add(IDk);
//                选l
                for (String i :ul.getUser(IDk).Prob_AdjList){
                    if ((kinl.contains(i))&&(!ul.getUser(IDi).Prob_AdjList.contains(i))&&(!ul.getUser(IDj).Prob_AdjList.contains(i))){
                        IDl = i;
                        break;
                    }
                }
                if (loop2.containsAll(kinl)){
                    loop2.clear();
                    break;
                }
            }
            UserInfo nodek = ul.getUser(IDk);
            UserInfo nodel = ul.getUser(IDl);
            if (IDl!=null){
//                连il,kj
                String edge = IDk+" "+IDl;
                String edge1 ="";
                String edge2 ="";
//                无向边是IDk+" "+IDl
                if (edges.contains(edge)){
                    edges.remove(edge);
                    edge1 = IDi+" "+IDl;
                    nodei.Prob_AdjList.add(IDl);
                    nodel.Prob_AdjList.add(IDi);
                    edge2 = IDk+" "+IDj;
                    nodek.Prob_AdjList.add(IDj);
                    nodej.Prob_AdjList.add(IDk);
                }
//                无向边是IDl + " " + IDk
                else {
                    edges.remove(IDl + " " + IDk);
                    edge1 = IDi + " " + IDk;
                    nodei.Prob_AdjList.add(IDk);
                    nodek.Prob_AdjList.add(IDi);
                    edge2 = IDl + " " + IDj;
                    nodel.Prob_AdjList.add(IDj);
                    nodej.Prob_AdjList.add(IDl);
                }
                nodek.Prob_AdjList.remove(IDl);
                nodel.Prob_AdjList.remove(IDk);
                edges.add(edge1);
                edges.add(edge2);
                sequence.remove(IDi);
                sequence.remove(IDj);
                loop1.remove(IDi + " " + IDj);
                if (IDi!=IDj)
                    loop1.remove(IDj + " " + IDi);
                if (nodei.getODegree()==nodei.Prob_AdjList.size())
                    kinl.add(IDi);
                if (nodej.getODegree()==nodej.Prob_AdjList.size())
                    kinl.add(IDj);
            }
        }

    }

    void generateDLink(File DATASET_PATH, String dataSet) throws IOException {
        LinkedList<String> userSet = ul.AllUser();
        LinkedList<String> Outsequence = new LinkedList<String>();
        LinkedList<String> Insequence = new LinkedList<String>();
        for (String user : userSet) {
            for (int num = 0; num < ul.getUser(user).getODegree(); num++) {
                Outsequence.add(user);
            }
            for (int num = 0; num < ul.getUser(user).getIDegree(); num++) {
                Insequence.add(user);
            }
        }
        System.out.println("generating directedgraph ");
        //        可能进入死循环的外层循环试过的点
        Set<String> loop1 = new HashSet<>();
        //        可能进入死循环的内层循环试过的点
        Set<String> loop2 = new HashSet<>();
        while ((!Outsequence.isEmpty()) && (!Insequence.isEmpty())) {
//             若所有出度节点均已被选为ID1，处理死循环
            if (loop1.containsAll(Outsequence)){
                System.out.println("step into endless loop ");
                newdealD(Outsequence, Insequence);
                break;
            }
//            从出度节点集选一个用户做起始节点ID1
            int radom = (int) (Math.random() * Outsequence.size());
            String ID1 = Outsequence.get(radom);
            UserInfo node1 = ul.getUser(ID1);
            if (!(node1.firstCandidateSorted).isEmpty()) {
                FtSample ftSample=new FtSample();
                int seqNum=ftSample.getPrizeIndex(node1.firstCandidateSorted,node1.weight);
                String IDn=node1.firstCandidateSorted.get(seqNum);
                while (!Insequence.contains(IDn)) {
//                    候选节点集中去除IDn
                    node1.firstCandidateSorted.remove(IDn);
                    if ((node1.firstCandidateSorted).isEmpty()) {
                        IDn = null;
                        break;
                    } else
                        IDn = node1.firstCandidateSorted.get(ftSample.getPrizeIndex(node1.firstCandidateSorted,node1.weight));
                }
                if (IDn != null) {
                    String edge = ID1 + " " + IDn;
                    node1.Prob_AdjList.add(IDn);
                    Outsequence.remove(ID1);
                    Insequence.remove(IDn);
                    node1.firstCandidateSorted.remove(IDn);
                    edges.add(edge);
                }
            }
            else {
                loop1.add(ID1);
//                选IDn
                radom = (int) (Math.random() * Insequence.size());
                String IDn = Insequence.get(radom);
                loop2.add(IDn);
//                重新选IDn
                while (ID1 == IDn || node1.Prob_AdjList.contains(IDn)) {
                    radom = (int) (Math.random() * Insequence.size());
                    IDn = Insequence.get(radom);
                    if (loop2.containsAll(Insequence)){
                        IDn=null;
                        break;
                    }
                }
                if (IDn!=null){
                    String edge = ID1 + " " + IDn;
                    loop1.remove(ID1);
                    node1.Prob_AdjList.add(IDn);
                    Outsequence.remove(ID1);
                    Insequence.remove(IDn);
                    edges.add(edge);
                }
                loop2.clear();
            }
        }
        System.out.println("sum of edges:" + edges.size());
        FileUtil.writeTextFile(DATASET_PATH.getAbsolutePath() + dataSet+".edges", edges);
    }

    void newdealD(LinkedList<String>Outsequence,LinkedList<String>Insequence) {
        LinkedList<String> kinl = new LinkedList<>();
        LinkedList<String> kins = new LinkedList<>();
        //        可能进入死循环的外层循环试过的点
        Set<String> loop1 = new HashSet<>();
        //        可能进入死循环的内层循环试过的点
        Set<String> loop2 = new HashSet<>();
        kinl = ul.AllUser();
//        for (String i:kinl){
//            if (Outsequence.contains(i) ||Insequence.contains(i))
//                kinl.remove(i);
//        }
        for(int i = 0;i<kinl.size();i++){
            if (Outsequence.contains(kinl.get(i))||Insequence.contains(kinl.get(i)))
                kinl.remove(i);
        }
       while ((!Outsequence.isEmpty()) &&(!Insequence.isEmpty())){
            for (String i:kinl)
                for (int num=0;num<ul.getUser(i).o_degree;num++)
                    kins.add(i);
//            选IDi
           int radom = (int) (Math.random() *Outsequence.size());
           String IDi = Outsequence.get(radom);
           //            选IDj
           radom = (int) (Math.random() *Insequence.size());
           String IDj = Insequence.get(radom);
           //            选IDk
           radom = (int) (Math.random() *kins.size());
           String IDk = kins.get(radom);
//           选IDl
           String IDl = null;
           for (String i :ul.getUser(IDk).Prob_AdjList){
               if ((kinl.contains(i))&&(!edges.contains(IDi+" "+i))&&(!edges.contains(IDk+" "+IDj ))){
                   IDl=i;
                   break;
               }
           }
           while (IDl ==null){
               //            选IDk
               radom = (int) (Math.random() *kins.size());
               IDk = kins.get(radom);
//           选IDl
               for (String i :ul.getUser(IDk).Prob_AdjList){
                   if ((kinl.contains(i))&&(!edges.contains(IDi+" "+i))&&(!edges.contains(IDk+" "+IDj ))){
                       IDl=i;
                       break;
                   }
               }
           }
//           连il,kj
           edges.remove(IDk+" "+IDl);
           ul.getUser(IDk).Prob_AdjList.remove(IDl);
           String edge1 = IDi+" "+IDl;
           ul.getUser(IDi).Prob_AdjList.add(IDl);
           String edge2 = IDk+" "+IDj;
           ul.getUser(IDk).Prob_AdjList.add(IDj);
           edges.add(edge1);
           edges.add(edge2);
           Outsequence.remove(IDi);
           Insequence.remove(IDj);
           if (!Outsequence.contains(IDi))
                kinl.add(IDi);
           if (!Insequence.contains(IDj))
                kinl.add(IDj);
//           只剩出度
           while(!Outsequence.isEmpty()){
               for (String i:kinl)
                   for (int num=0;num<ul.getUser(i).i_degree;num++)
                       kins.add(i);
               //        选IDi
               radom = (int) (Math.random() *Outsequence.size());
               IDi = Outsequence.get(radom);
               //            选IDl
               radom = (int) (Math.random() *kins.size());
               IDl = kins.get(radom);
               while (edges.contains(IDi+" "+IDl)){
                   radom = (int) (Math.random() *kins.size());
                   IDl = kins.get(radom);
               }
//                连il
               edge1 = IDi+" "+IDl;
               ul.getUser(IDi).Prob_AdjList.add(IDl);
               edges.add(edge1);
               Outsequence.remove(IDi);
               if (!Outsequence.contains(IDi))
                kinl.add(IDi);
           }
           //           只剩入度
           while(!Insequence.isEmpty()){
               for (String i:kinl)
                   for (int num=0;num<ul.getUser(i).o_degree;num++)
                       kins.add(i);
               //        选IDi
               radom = (int) (Math.random() *Insequence.size());
               IDi = Insequence.get(radom);
               //            选IDl
               radom = (int) (Math.random() *kins.size());
               IDl = kins.get(radom);
               while (edges.contains(IDl+" "+IDi)){
                   radom = (int) (Math.random() *kins.size());
                   IDl = kins.get(radom);
               }
//                连il
               edge1 = IDl+" "+IDi;
               ul.getUser(IDl).Prob_AdjList.add(IDi);
               edges.add(edge1);
               Insequence.remove(IDi);
               if (!Insequence.contains(IDi))
                   kinl.add(IDi);
           }
       }
    }


}
