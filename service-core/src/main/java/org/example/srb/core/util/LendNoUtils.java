package org.example.srb.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
/**
 * 生成标的编号
 * @author wendao
 * @since 2024-04-09
 **/
public class LendNoUtils {

    public static String getNo() {

        LocalDateTime time=LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        StringBuilder result =new StringBuilder(dtf.format(time));

//        String strDate = dtf.format(time);
//        String result = "";
//        Random random = new Random();
//        for (int i = 0; i < 3; i++) {
//            //底层实现实际上也是new StringBuilder,但是每次循环都new新对象
//            result += random.nextInt(10);
//        }
//        //"yyyyMMddHHmmss"+三个随机数
//        return strDate + result;
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            //底层实现实际上也是new StringBuilder
            result.append( random.nextInt(10));
        }
        //"yyyyMMddHHmmss"+三个随机数
        return result.toString();
    }
    /**
     * 标的编号
     * @param
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 17:52
    */
    public static String getLendNo() {

        return "LEND" + getNo();
    }
    /**
     * 投资人投资条目编号
     * @param
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 17:52
     */
    public static String getLendItemNo() {

        return "INVEST" + getNo();
    }
    /**
     * 管理员放款编号
     * @param
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 17:52
     */
    public static String getLoanNo() {

        return "LOAN" + getNo();
    }
    /**
     * 借款人还款编号
     * @param
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 17:52
     */
    public static String getReturnNo() {
        return "RETURN" + getNo();
    }

    /**
     * 还款人提现编号
     * @param
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 17:52
     */
    public static Object getWithdrawNo() {
        return "WITHDRAW" + getNo();
    }

    /**
     * 借款人还款的分期编号
     * @param
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 17:52
     */
    public static String getReturnItemNo() {
        return "RETURNITEM" + getNo();
    }

    /**
     * 还款人充值编号
     * @param
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 17:52
     */
    public static String getChargeNo() {

        return "CHARGE" + getNo();
    }

    /**
     *
     * 获取交易编码
     * 上面的每一个接口都是一种交易
     * @param
     * @return java.lang.String
     * @author Administrator
     * @date 2024/4/17 0017 17:52
     */
    public static String getTransNo() {
        return "TRANS" + getNo();
    }

}