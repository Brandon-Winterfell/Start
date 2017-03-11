package com.huahua.start.module.recommand;

import com.huahua.start.module.BaseModel;

/**
 * Created by Administrator on 2017/2/14.
 */

public class BaseRecommandModel extends BaseModel {

    public String ecode;
    public String emsg;
    public RecommandModel data;

    @Override
    public String toString() {
        return "BaseRecommandModel{" +
                "ecode='" + ecode + '\'' +
                ", emsg='" + emsg + '\'' +
                ", data=" + data +
                '}';
    }
}























