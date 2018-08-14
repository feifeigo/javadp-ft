import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FtSample {
    /**
     * 根据Math.random()产生一个float型的随机数，判断每个候选集用户出现的概率
     * @param candidates
     * @return random：candidate列表中的序列（candidates中的第random个就是抽中的用户）
     */
    public int getPrizeIndex(LinkedList<String> firstCandidateSorted,Map<String,Float> candidates) {
        DecimalFormat df = new DecimalFormat("######0.00");
        int random = 0;
        try{
            //计算总权重
            float sumWeight = 0;
            for (int i=0;i<firstCandidateSorted.size();i++){
                sumWeight+=candidates.get(firstCandidateSorted.get(i));
            }
            //产生随机数
            float randomNumber;
            randomNumber = (float)Math.random();
            //根据随机数在所有candidate user分布的区域并确定所抽user
            float d1 = 0;
            float d2 = 0;
            for (int i=0;i<firstCandidateSorted.size();i++){
                d2+=candidates.get(firstCandidateSorted.get(i))/sumWeight;
                if (i==0)
                    d1=0;
                else
                    d1+=candidates.get(firstCandidateSorted.get(i-1))/sumWeight;
                if(randomNumber >= d1 && randomNumber <= d2){
                    random = i;
                    break;
                }
            }
        }catch(Exception e){
            System.out.println("error occured in generating random numbers,cause: " +e.getMessage());
        }
        return random;
    }
}
