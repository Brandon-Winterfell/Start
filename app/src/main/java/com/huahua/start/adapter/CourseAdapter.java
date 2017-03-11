package com.huahua.start.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.huahua.activity.AdBrowserActivity;
import com.huahua.core.AdContextInterface;
import com.huahua.core.video.VideoAdContext;
import com.huahua.imageloader.ImageLoaderManager;
import com.huahua.start.R;
import com.huahua.start.module.recommand.RecommandBodyValue;
import com.huahua.start.util.Util;
import com.huahua.util.Utils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/2/14.
 */

public class CourseAdapter extends BaseAdapter {

    /**
     * Common
     */
    private static final int CARD_COUNT = 4;
    private static final int VIDOE_TYPE = 0x00;
    private static final int CARD_TYPE_ONE = 0x01;
    private static final int CARD_TYPE_TWO = 0x02;
    private static final int CARD_TYPE_THREE = 0x03;

    private LayoutInflater mInflate;
    private Context mContext;
    private ArrayList<RecommandBodyValue> mData;
    private ViewHolder mViewHolder;
    private VideoAdContext mAdsdkContext;  // 这是使用播放器的API层
    private ImageLoaderManager mImagerLoader;

    public CourseAdapter(Context context, ArrayList<RecommandBodyValue> data) {
        mContext = context;
        mData = data;
        mInflate = LayoutInflater.from(mContext);
        mImagerLoader = ImageLoaderManager.getInstance(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return CARD_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        RecommandBodyValue value = (RecommandBodyValue) getItem(position);
        return value.type;  // type 从bean对象中一个字段取到
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        RecommandBodyValue value = (RecommandBodyValue) getItem(position);

        if (convertView == null) {
            switch (type) {
                case VIDOE_TYPE:
                    // 显示video卡片
                    mViewHolder = new ViewHolder();
                    convertView = mInflate.inflate(R.layout.item_video_layout, parent, false);

                    // 放置播放器的父容器（主要控件）
                    mViewHolder.mVieoContentLayout = (RelativeLayout) convertView.findViewById(R.id.video_ad_layout);
                    // 其他的控件
                    mViewHolder.mLogoView = (CircleImageView) convertView.findViewById(R.id.item_logo_view);
                    mViewHolder.mTitleView = (TextView) convertView.findViewById(R.id.item_title_view);
                    mViewHolder.mInfoView = (TextView) convertView.findViewById(R.id.item_info_view);
                    mViewHolder.mFooterView = (TextView) convertView.findViewById(R.id.item_footer_view);
                    mViewHolder.mShareView = (ImageView) convertView.findViewById(R.id.item_share_view);

                    // 为对应布局创建播放器 (视频播放SDK模块)
                    mAdsdkContext = new VideoAdContext(
                            mViewHolder.mVieoContentLayout,
                            new Gson().toJson(value), // TODO value本来不就是json字符串吗
                            null);

                    // 本adapter需要提供一个updateAdInScrollView方法

                    mAdsdkContext.setAdResultListener(new AdContextInterface() {
                        @Override
                        public void onAdSuccess() {

                        }

                        @Override
                        public void onAdFailed() {

                        }

                        @Override
                        public void onClickVideo(String url) {
                            Intent intent = new Intent(mContext, AdBrowserActivity.class);
                            intent.putExtra(AdBrowserActivity.KEY_URL, url);
                            mContext.startActivity(intent);
                        }
                    });

                    break;
                case CARD_TYPE_ONE:
                    mViewHolder = new ViewHolder();
                    convertView = mInflate.inflate(R.layout.item_product_card_one_layout, parent, false);
                    mViewHolder.mLogoView = (CircleImageView) convertView.findViewById(R.id.item_logo_view);
                    mViewHolder.mTitleView = (TextView) convertView.findViewById(R.id.item_title_view);
                    mViewHolder.mInfoView = (TextView) convertView.findViewById(R.id.item_info_view);
                    mViewHolder.mFooterView = (TextView) convertView.findViewById(R.id.item_footer_view);
                    mViewHolder.mPriceView = (TextView) convertView.findViewById(R.id.item_price_view);
                    mViewHolder.mFromView = (TextView) convertView.findViewById(R.id.item_from_view);
                    mViewHolder.mZanView = (TextView) convertView.findViewById(R.id.item_zan_view);
                    mViewHolder.mProductLayout = (LinearLayout) convertView.findViewById(R.id.product_photo_layout);
                    break;
                case CARD_TYPE_TWO:
                    mViewHolder = new ViewHolder();
                    convertView = mInflate.inflate(R.layout.item_product_card_two_layout, parent, false);
                    mViewHolder.mLogoView = (CircleImageView) convertView.findViewById(R.id.item_logo_view);
                    mViewHolder.mTitleView = (TextView) convertView.findViewById(R.id.item_title_view);
                    mViewHolder.mInfoView = (TextView) convertView.findViewById(R.id.item_info_view);
                    mViewHolder.mFooterView = (TextView) convertView.findViewById(R.id.item_footer_view);
                    mViewHolder.mProductView = (ImageView) convertView.findViewById(R.id.product_photo_view);
                    mViewHolder.mPriceView = (TextView) convertView.findViewById(R.id.item_price_view);
                    mViewHolder.mFromView = (TextView) convertView.findViewById(R.id.item_from_view);
                    mViewHolder.mZanView = (TextView) convertView.findViewById(R.id.item_zan_view);
                    break;
                case CARD_TYPE_THREE:
                    mViewHolder = new ViewHolder();
                    convertView = mInflate.inflate(R.layout.item_product_card_three_layout, null, false);
                    mViewHolder.mViewPager = (ViewPager) convertView.findViewById(R.id.pager);
                    // add data
                    ArrayList<RecommandBodyValue> recommandList = Util.handleData(value);
                    mViewHolder.mViewPager.setPageMargin(Utils.dip2px(mContext, 12));
                    mViewHolder.mViewPager.setAdapter(new HotSalePagerAdapter(mContext, recommandList));
                    mViewHolder.mViewPager.setCurrentItem(recommandList.size() * 100);
                    break;
            }
            convertView.setTag(mViewHolder); // 空指针异常了 因为视频type没有做，而拿到的数据有视频这个内容
            // 放进3个if里面就没有空指针出现了 应该是还有个视频type 没有进到if里面 所以报空指针异常了
        } else {  // 有Tag时候
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        // 填充item的数据
        switch (type) {
            case CARD_TYPE_ONE:
                mImagerLoader.displayImage(mViewHolder.mLogoView, value.logo);
                mViewHolder.mTitleView.setText(value.title);
                mViewHolder.mInfoView.setText(value.info.concat(mContext.getString(R.string.tian_qian)));
                mViewHolder.mFooterView.setText(value.text);
                mViewHolder.mPriceView.setText(value.price);
                mViewHolder.mFromView.setText(value.from);
                mViewHolder.mZanView.setText(mContext.getString(R.string.dian_zan).concat(value.zan));

                mViewHolder.mProductLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(mContext, PhotoViewActivity.class);
//                        intent.putStringArrayListExtra(PhotoViewActivity.PHOTO_LIST, value.url);
//                        mContext.startActivity(intent);
                    }
                });

                mViewHolder.mProductLayout.removeAllViews();
                //动态添加多个imageview
                for (String url : value.url) {
                    mViewHolder.mProductLayout.addView(createImageView(url));
                }
                break;
            case CARD_TYPE_TWO:
                mImagerLoader.displayImage(mViewHolder.mLogoView, value.logo);
                mViewHolder.mTitleView.setText(value.title);
                mViewHolder.mInfoView.setText(value.info.concat(mContext.getString(R.string.tian_qian)));
                mViewHolder.mFooterView.setText(value.text);
                mViewHolder.mPriceView.setText(value.price);
                mViewHolder.mFromView.setText(value.from);
                mViewHolder.mZanView.setText(mContext.getString(R.string.dian_zan).concat(value.zan));
                //为单个ImageView加载远程图片
                mImagerLoader.displayImage(mViewHolder.mProductView, value.url.get(0));
                break;
            case CARD_TYPE_THREE:
                // 在新建ViewHolder的时候已经处理了
                break;
        }

        return convertView;
    }

    /**
     * 自动播放方法
     *
     * listview滑动会调用SDK层的滑动方法，SDK层去调用业务逻辑层的滑动方法
     *
     * 这是实现自动播放/暂停的需求，
     * 如果第三方不需要这个需求，那在getview方法里一步就可以完成视频播放功能的添加
     */
    public void updateAdInScrollView() {
        if (mAdsdkContext != null) {
            mAdsdkContext.updateAdInScrollView();
        }
    }

    //动态创建并添加ImageView
    private ImageView createImageView(String url) {
        ImageView photoView = new ImageView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.
                LayoutParams(Utils.dip2px(mContext, 100),
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.leftMargin = Utils.dip2px(mContext, 5);
        photoView.setLayoutParams(params);
        mImagerLoader.displayImage(photoView, url);
        return photoView;
    }

    private static class ViewHolder {
        //所有Card共有属性
        private CircleImageView mLogoView;
        private TextView mTitleView;
        private TextView mInfoView;
        private TextView mFooterView;
        //Video Card特有属性
        private RelativeLayout mVieoContentLayout;
        private ImageView mShareView;

        //Video Card外所有Card具有属性
        private TextView mPriceView;
        private TextView mFromView;
        private TextView mZanView;
        //Card One特有属性
        private LinearLayout mProductLayout;
        //Card Two特有属性
        private ImageView mProductView;
        //Card Three特有属性
        private ViewPager mViewPager;
    }
}
