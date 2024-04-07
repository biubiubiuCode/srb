package org.example.srb.core;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author wendao
 * @since 2024-04-02
 **/
public class AssertTests {
    @Test
    public void test1(){
        Object o=null;
        if(o==null){
            throw new IllegalArgumentException("用户不存在");
        }
    }
    @Test
    public void test2(){
        Object o=null;
        //用断言代替if结构：满足就跳过，不满足就输出指定信息
        Assert.notNull(o,"用户不存在");
    }


}
