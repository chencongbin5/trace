package com.akuchen.trace.parse.builder;

import com.akuchen.trace.parse.dto.TemplateReq;

/**
 * 生成jdi监控文件
 *
 * 本来没有必要生成这个文件, 明明可以直接触发所有步骤
 * 但是因为生成mock文件可能无法直接执行成功, 需要手动注释类多态方法 或者main执行的默认方法不是目标方法(如果这两问题可以解决 那这个类以后也就可以不要了)
 *
 */
public class GptClassBuilder {


    public static String template(TemplateReq templateReq){


        String code= "package " + templateReq.getPackageName() + ";\n" +
                "\n" +
                "import lombok.extern.slf4j.Slf4j;\n" +
                "\n" +
                "import com.akuchen.trace.parse.utils.MavenUtils;\n" +
                "import com.akuchen.trace.parse.utils.JvmUtils;\n" +
                "import java.io.*;\n" +

                "import java.util.Optional;\n" +
                "\n" +
                "@Slf4j\n" +
                "public class "+templateReq.getGptClassName()+" {\n" +
                "    /**\n" +
                "     * 因为业务类通过idea启动的时候会自带agent idea_rt.jar包 会影响我的agentlib:jdwp 连接 \n" +
                "     * @param args\n" +
                "     */\n" +
                "    public static void main(String[] args) {\n" +

                "        new Thread(()->{\n" +
                "            log.info(\"Starting monitoring class\");\n" +
                "            JvmUtils.startJvm(\"监控类\",\""+templateReq.getPackageName()+"."+templateReq.getJdiClassName() +"\",null,\"\");\n" +
                "            log.info(\"Monitoring class started\");\n" +
                "        }).start();\n" +
                "\n" +
                "\n" +
                "        try {\n" +
                "            Thread.sleep(5000);\n" +
                "        } catch (InterruptedException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "\n" +
                "\n" +
                "        new Thread(()->{\n" +
                "            log.info(\"Starting monitoring class\");\n" +
                "            JvmUtils.startJvm(\"业务类\",\""+templateReq.getPackageName()+"."+templateReq.getClassName()+"\",\"-agentlib:jdwp=transport=dt_socket,address=127.0.0.1:5005,suspend=y,server=n\",\"rmi\");\n" +
                "            log.info(\"Monitoring class started\");\n" +
                "        }).start();\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +

                "\n" +
                "}\n";
       return code;
       
    }





}
