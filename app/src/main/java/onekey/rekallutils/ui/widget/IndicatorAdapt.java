package onekey.rekallutils.ui.widget;

import android.content.Context;
import android.view.View;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import onekey.rekallutils.R;
import onekey.rekallutils.utils.ResHelper;

import java.util.List;

public class IndicatorAdapt extends CommonNavigatorAdapter {

    private final List<String> mtitles;
    private OnIndicatorTapClickListener mListener=null;

    public IndicatorAdapt(Context context,List<String> titles) {
        mtitles = titles;

    }

    @Override
    public int getCount() {
        if(mtitles!=null){
            return mtitles.size();
        }
        return 0;
    }

    @Override
    public IPagerTitleView getTitleView(Context context, final int index) {
        ColorTransitionPagerTitleView colorTransitionPagerTitleView =new ColorTransitionPagerTitleView(context);
        colorTransitionPagerTitleView.setNormalColor(ResHelper.getColor(R.color.a0mainColor));
        colorTransitionPagerTitleView.setSelectedColor(ResHelper.getColor(R.color.mainColor));
        colorTransitionPagerTitleView.setTextSize(18);
        colorTransitionPagerTitleView.setText(mtitles.get(index));
        colorTransitionPagerTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // mViewPager.setCurrentItem(index);
                if (mListener != null) {
                    mListener.onTabClick(index);
                }
            }
        });
        return  colorTransitionPagerTitleView;
    }
    
    @Override
    public IPagerIndicator getIndicator(Context context) {
        LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
        linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
        linePagerIndicator.setColors(ResHelper.getColor(R.color.mainColor));
        return linePagerIndicator;
    }
    public  void setOnIndicatorTapClickListener(OnIndicatorTapClickListener listener){
        this.mListener = listener;
    }
    public interface OnIndicatorTapClickListener{
        void onTabClick(int index);
    }
}

