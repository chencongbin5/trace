package com.akuchen.trace.parse.dto;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class CodeInfoDTO implements Serializable {
    private String  threadName;

    private List<String> importList=new ArrayList<>();
    private List<String> anotationsList=new ArrayList<>();
    private List<String> headList=new ArrayList<>();
    private List<String> bodyList=new ArrayList<>();
    //入参不为空的body
    private List<String> bodyParamNonNullList=new ArrayList<>();
    //body前缀 就是方法名称
    private List<String> methodList=new ArrayList<>();
    @Builder.Default
    private Integer whenCount=0;

    /**
     * 默认0  0常规方法 1入口方法
     */
    private Integer type=0;

    public CodeInfoDTO(String threadName) {
        this.threadName = threadName;
    }
    public CodeInfoDTO() {
    }
    /**
     * key:报错信息
     * value:debugger需要的信息
     */
    private Map<String, List<DebuggerDTO>> debuggerMap;

    public void addCodeInfo(CodeInfoDTO codeInfoDTO ) {
        if (Objects.isNull(codeInfoDTO)){
            return ;
        }
        Optional.ofNullable(codeInfoDTO.getImportList()).ifPresent(v->{
            v.stream().forEach(t -> {
                if (!importList.contains(t)) {
                    importList.add(t);
                }
            });
        });
        Optional.ofNullable(codeInfoDTO.getAnotationsList()).ifPresent(v->{
            v.stream().forEach(t -> {
                if (!anotationsList.contains(t)) {
                    anotationsList.add(t);
                }
            });
        });
        codeInfoDTO.getHeadList().stream().forEach(t -> {
            if (!headList.contains(t)) {
                headList.add(t);
            }
        });
        codeInfoDTO.getMethodList().stream().forEach(t -> {
            if (!methodList.contains(t)) {
                methodList.add(t);
            }
        });
        //when().thenReturn()
        Integer bodyCount=0;
        for(String t : codeInfoDTO.getBodyList()){
            if(codeInfoDTO.getType()==1 && bodyCount==0){
                //带方法入口的代码块
                bodyList.add(0,t);
            }
            bodyCount++;
            //when 开头 和doNothing开头的不相同才添加
            if (!bodyList.contains(t) && (t.startsWith("when(") || t.startsWith("doNothing("))){
                int i = t.indexOf(".thenReturn");
                //如果前半截相同,找到相同的记录,截取新记录补充到后面去
                //有一调记录符合要求  修改原记录
                String filterBody = bodyList.stream().filter(body -> (i > 0 && body.startsWith(t.substring(0, i)))).findFirst().orElse(null);
                if(StringUtils.isEmpty(filterBody)){
                    bodyList.add(t);
                }else{
                    bodyList.remove(filterBody);
                    bodyList.add(filterBody.substring(0,filterBody.length()-1) + t.substring(i));
                }

                if(t.indexOf("when(")>-1){
                    whenCount++;
                }
            }else{
                bodyList.add(t);
            }
        }
        Integer bodyParamNonNullCount=0;
        for(String t : codeInfoDTO.getBodyParamNonNullList()){
            if(codeInfoDTO.getType()==1 && bodyParamNonNullCount==0){
                //带方法入口的代码块
                bodyParamNonNullList.add(0,t);
            }
            bodyParamNonNullCount++;
            //不相同才添加
            if (!bodyParamNonNullList.contains(t)) {
                int i = t.indexOf(".thenReturn");
                //如果前半截相同,找到相同的记录,截取新记录补充到后面去
                //有一调记录符合要求  修改原记录
                String filterBody = bodyParamNonNullList.stream().filter(body -> (i > 0 && body.startsWith(t.substring(0, i)))).findFirst().orElse(null);
                if(StringUtils.isEmpty(filterBody)){
                    bodyParamNonNullList.add(t);
                }else{
                    bodyParamNonNullList.remove(filterBody);
                    bodyParamNonNullList.add(filterBody.substring(0,filterBody.length()-1) + t.substring(i));
                }
            }
        }

        if(CollectionUtils.isNotEmpty(codeInfoDTO.getMethodList())){
            codeInfoDTO.getMethodList().stream().forEach(t->{
                bodyList.add(0, "/**\n" +
                        "    * "+threadName+"\n" +
                        "    */\n" +
                        "    @Test\n    public void "+t+"() throws Exception {\n");
                bodyList.add(bodyList.size(), "}\n");
            });
        }

    }


    public String generateRandomString(int length) {
        Random random = new Random();
        return random.ints(48, 91) // 91 is exclusive, so the max is 90
                .filter(i -> (i <= 57 || i >= 65))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public void check(){
        //如果没有入口方法
        // 两种情况
        // 1 新的入口没有被解析,需要开发尽快解析  比如dubbo入口 job入口,mq入口
        // 2 方法内另开线程 执行的方法 .这里处理这种情况
        //处理方案:这些所有无入口的 mock类,扔到每一个有入口的最前列去,
        // 反正我也不知道是谁的, 大家都mock一遍呗,大不mock方法没执行到,不影响啥, 放最前面这样即使有冲突 也可以抛弃先写入的.
        // 这样做 虽然能解决部分问题 比如查订单详情,开了各种各样的线程去查其他信息 这些都能mock下来,但是也会带来新的问题 比如业务代码循环执行某线程,,
        // 以前无法mock某线程内的运行, 但是能运行到这里,然后不mock,直接调用测试环境现有接口或者数据库
        // 现在就运行到这里 然后mock一堆错误,和一个正确,
        //
        // 想到折中办法了, 这些所有无入口的 mock类,扔到每一个有入口的最前列去, 但是他们的when语句 不能用any 必须指定内容 很好,
        if(CollectionUtils.isEmpty(this.getMethodList())){
            //循环bodyList,每一行都插入字符"//"
            bodyList=this.getBodyList().stream().map(t-> "//"+t).collect(Collectors.toList());
            //还有引用头
            //headList=this.getHeadList().stream().map(t-> "//"+t).collect(Collectors.toList());
        }
    }
}
