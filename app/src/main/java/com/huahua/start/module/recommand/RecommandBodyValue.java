package com.huahua.start.module.recommand;

import com.huahua.module.monitor.Monitor;
import com.huahua.module.monitor.emevent.EMEvent;
import com.huahua.start.module.BaseModel;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/2/14.
 */

public class RecommandBodyValue extends BaseModel {

    public int type;
    public String logo;
    public String title;
    public String info;
    public String price;
    public String text;
    public String site;
    public String from;
    public String zan;
    public ArrayList<String> url;

    // 还有个视频专用部分

    //视频专用
    public String thumb;
    public String resource;
    public String resourceID;
    public String adid;
    public ArrayList<Monitor> startMonitor;
    public ArrayList<Monitor> middleMonitor;
    public ArrayList<Monitor> endMonitor;
    public String clickUrl;
    public ArrayList<Monitor> clickMonitor;
    public EMEvent event;

}

























