import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class FileUtil {
//    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static boolean createFile(String destFileName) {
        File file = new File(destFileName);
        if(file.exists()) {
            System.out.println("creating " + destFileName + " failed，it already exists,now delete");
            file.delete();
            file = new File(destFileName);

        }
        if (destFileName.endsWith(File.separator)) {
            System.out.println("creating " + destFileName + " failed，it can't be a directory ");
            return false;
        }
        //判断目标文件所在的目录是否存在
        if(!file.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            System.out.println("the directory doesn't exists，now create it ");
            if(!file.getParentFile().mkdirs()) {
                System.out.println("creating directory failed ");
                return false;
            }
        }
        //创建目标文件
        try {
            if (file.createNewFile()) {
                System.out.println("creating the file " + destFileName + " finished ");
                return true;
            } else {
                System.out.println("creating the file " + destFileName + " failed ");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("creating the file " + destFileName + " failed " + e.getMessage());
            return false;
        }
    }

    public static void writeTextFile(String fileName, List<String> set)
            throws IOException {
        createFile(fileName);
//        logger.debug("Writing text file " + fileName + "...");
        PrintWriter writer = null;
        System.out.println("begin writing " + fileName);
        try {
            writer = new PrintWriter(new BufferedWriter(
                    new FileWriter(fileName)));

            for (int i = 0; i < set.size(); i++) {
                String data = set.get(i);
                writer.print(data);
                writer.println();
            }
            System.out.println("finished writting " + fileName);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
