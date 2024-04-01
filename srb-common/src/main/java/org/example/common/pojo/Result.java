package org.example.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wendao
 * @since 2024-04-01
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer code;//响应代码：1=成功,0=失败
    private String msg;//描述响应信息，描述状态的字符串

    private Object data;//返回的数据
    /*
     * 定义几个静态方法用于快速创建Result实例
     * */
    //增删改 成功响应
    public static Result success(){
        return new Result(1, "success", null);
    }
    //查询 成功响应
    public static Result success(Object data){
        return new Result(1,"success",data);
    }
    //失败响应
    public static Result error(String msg){
        return new Result(0,msg,null);
    }
}
