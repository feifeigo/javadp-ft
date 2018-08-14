

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

public class Pre_Process_test {
    private static final String DATASET_DIR = "I:/DP-FTexperiment/dataSets/";
    private static  final String RESULT_DIR="I:/javadpft/";
//    private static final String DATASET_DIR = "E:/ADPFT/dataSets/";
//    private static  final String RESULT_DIR="E:/ADPFT/result";
//    private static final String DATASET_DIR = "D:/ADPFT/dataSets/";
//    private static  final String RESULT_DIR="D:/ADPFT/result";
    public static void main(String[] args) throws IOException {
        SimCompute sc=new SimCompute();
        boolean directer,privately;
        System.out.print("dataset:");
        Scanner scan = new Scanner(System.in);
        String dataSet = scan.nextLine();
        File DATASET_PATH=new File(DATASET_DIR+dataSet);
        File RESULT_PATH=new File(RESULT_DIR+dataSet);
        System.out.println("path of original dataset: "+DATASET_PATH);
        System.out.print("perturb or not ?(true/false):");
        privately=scan.nextBoolean()?true:false;
        System.out.print("direct or not?((true/false): ");
        directer=scan.nextBoolean()?true:false;
        File output_dir;
        double epsilon1=0;
        if (privately){
            System.out.print("epsilon: ");
            epsilon1=scan.nextDouble();
            sc.preProcess(DATASET_PATH,privately,directer,epsilon1);     //初始化过程，包含差分隐私扰
        }
        else
            sc.preProcess(DATASET_PATH,privately,directer,epsilon1);     //初始化过程，不包含差分隐私扰动
        output_dir=new File(RESULT_PATH,"/FT_"+String.valueOf(epsilon1));
        if(directer){
            sc.generateDLink(output_dir,dataSet);               //有向图
        }
        else
            sc.generateUDLink(output_dir,dataSet);
    }

//        SimCompute sc=new SimCompute();
//        boolean directer,privately;
////        System.out.print("输入数据集名称：");
////        Scanner scan = new Scanner(System.in);
////        String dataSet = scan.nextLine();
//        String dataSet="little";
//        File DATASET_PATH=new File(DATASET_DIR+dataSet);
//        File RESULT_PATH=new File(RESULT_DIR+dataSet);
//        System.out.println("原始数据集路径："+DATASET_PATH);
////        System.out.print("是否进行差分隐私扰动？请输入boolean值(true/false)：");
////        privately=scan.nextBoolean()?true:false;
////        privately=false;
//        privately=true;
////        System.out.print("是否为有向图？请输入boolean值(true/false)：");
////        directer=false;
//        directer=true;
////        directer=scan.nextBoolean()?true:false;
//        File output_dir;
//        double epsilon1=0;
//        if (privately){
////            System.out.print("请输入社交关系隐私预算：");
////            epsilon1=scan.nextDouble();
//            epsilon1=2.0;
//            sc.preProcess(DATASET_PATH,privately,directer,epsilon1);     //初始化过程，包含差分隐私扰
//        }
//        else
//            sc.preProcess(DATASET_PATH,privately,directer,epsilon1);     //初始化过程，不包含差分隐私扰动
//        output_dir=new File(RESULT_PATH,"/FT_"+String.valueOf(epsilon1));
//        if(directer){
//            sc.generateDLink(output_dir,dataSet);               //有向图
//        }
//        else
//            sc.generateUDLink(output_dir,dataSet);
//    }




}
