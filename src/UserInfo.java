
import java.math.BigDecimal;
import java.util.*;

public class UserInfo {
    private static final int degree_sensitivity = 2;
    private static final int numOfAff_sensitivity = 2;
    public String ID;
    private int position;
    public LinkedList<String> adjOutList;//出度邻点集
    public LinkedList<String> adjInList;//入度邻点集
    public Map<String, Float> candidateSim;  //一步转移概率
    public Map<String, Float> weight;//吸引力
    public LinkedList<String> firstCandidateSorted;//候选节点集
    public LinkedList<String> Prob_AdjList;//已成边节点集
    public Set<String> n_hop_neighbor;//n步邻居
    public int out_degree;//原图出入度
    public int in_degree;
    public int o_degree;//扰动后出入度
    public int i_degree;

    public UserInfo(String ID, int position) {
        this.ID = ID;
        this.position = position;
        this.adjOutList = new LinkedList<String>();
        this.adjInList = new LinkedList<String>();
        this.firstCandidateSorted = new LinkedList<String>();
        this.candidateSim = new HashMap<String, Float>();
        this.weight = new HashMap<String, Float>();
        this.Prob_AdjList=new LinkedList<String>();
        this.n_hop_neighbor = new HashSet<>();
    }

    public float get1_simValue(String key) {
        if (candidateSim.get(key) == null)
            return 0.0f;
        else
            return candidateSim.get(key);
    }

    public void add_Sim(String key, float value) {
        candidateSim.put(key, value);
    }
    public void add_wei(String key, float value) {
        weight.put(key, value);
    }

    public void setPeDegree(boolean perturb,double epsilon1,int numofv) {
        int odegree = out_degree;
        int idegree = in_degree;
        if (perturb){
            odegree += differiencialNoise(degree_sensitivity,epsilon1);
            idegree += differiencialNoise(degree_sensitivity,epsilon1);
            while ((odegree < 0) || (idegree < 0)
                    || ((odegree == 0) && (idegree == 0))
                    || (odegree > candidateSim.size())
                    || (idegree > candidateSim.size())
                    ||(idegree>numofv-2)
                    ||(odegree>numofv-2)) {
                odegree = out_degree + differiencialNoise(degree_sensitivity,epsilon1);
                idegree = in_degree + differiencialNoise(degree_sensitivity,epsilon1);
            }
            odegree = 1>odegree?1:odegree;
            idegree = 1>idegree?1:idegree;
            odegree = numofv<odegree?numofv:odegree;
            idegree = numofv<idegree?numofv:idegree;
        }
        this.o_degree = odegree;
        this.i_degree = idegree;
    }

    public int getODegree() {
        return o_degree;
    }

    public int getIDegree() {
        return i_degree;
    }

    public void setFistCandidateSorted(LinkedList<String> list) {
        this.firstCandidateSorted = list;
    }

    public LinkedList<String> AllHip1User() {
        return (LinkedList<String>) union(adjInList, adjOutList);
    }

    public void addAdjOut(String value) {
        adjOutList.add(value);
    }

    public void addAdjIn(String value) {
        adjInList.add(value);
    }

    public void setDegree() {
        this.out_degree = adjOutList.size();
        this.in_degree = adjInList.size();
    }

    public int getOutDegree() {
        return out_degree;
    }

    public int getInDegree() {
        return in_degree;
    }

    public int differiencialNoise(int sensitivity,double epsilon) {

        double uniformDistributionVar, noise;
        uniformDistributionVar = Math.random();
        if (uniformDistributionVar == 0) {
            noise = Double.NEGATIVE_INFINITY;
        } else if (0 < uniformDistributionVar && uniformDistributionVar < 0.5) {
            noise = sensitivity / epsilon
                    * Math.log(2 * uniformDistributionVar);
        } else
            noise = -sensitivity / epsilon
                    * Math.log(2 - 2 * uniformDistributionVar);
        BigDecimal n = new BigDecimal(noise).setScale(0,BigDecimal.ROUND_HALF_UP);

        return n.intValue();
    }

    public <T> List<T> union(LinkedList<T> ls, LinkedList<T> ls2) {
        List<T> list = new LinkedList<T>(ls);
        List<T> list2 = new LinkedList<T>(ls2);
        list2.removeAll(list);
        list.addAll(list2);

        return list;
    }


}
