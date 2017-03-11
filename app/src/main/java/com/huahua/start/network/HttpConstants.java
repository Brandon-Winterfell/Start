package com.huahua.start.network;

/**
 * Created by Administrator on 2017/2/14.
 * @fuction 所有请求相关地址
 */

public class HttpConstants {

    private static final String ROOT_URL = "http://imooc.com/api";

    /**
     * 首页产品请求接口
     */
    public static String HOME_RECOMMAND = ROOT_URL + "/product/home_recommand.php";

    /**
     * 检查更新接口
     */
    public static String CHECK_UPDATE = ROOT_URL + "/config/check_update.php";

    /**
     * 登陆接口
     */
    public static String LOGIN = ROOT_URL + "/user/login_phone.php";

    /**
     * 课程详情接口
     */
    public static String COURSE_DETAIL = ROOT_URL + "/product/course_detail.php";


}





























