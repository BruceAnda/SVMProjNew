package cn.zhaoliang5156.androidsvmlib.view

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import cn.zhaoliang5156.androidsvmlib.adapter.FeatureAdapter
import cn.zhaoliang5156.androidsvmlib.domain.FeatureData

/**
 * Created by zhaoliang on 2017/7/24.
 */
class FeatureView : RecyclerView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    lateinit var featureAdapter: FeatureAdapter

    fun setData(dataList: ArrayList<FeatureData>, spanCount: Int, onFeatureItemClickListener: FeatureAdapter.OnFeatureItemClickListener) {
        layoutManager = GridLayoutManager(context, spanCount)
        featureAdapter = FeatureAdapter(context, dataList)
        featureAdapter.onFeatureClickListener = onFeatureItemClickListener
        adapter = featureAdapter
    }

    fun getData(): ArrayList<FeatureData> {
        return featureAdapter.dataList
    }

    /**
     * 更改数据显示样式
     * @param position 要更那一项数据，1.收集数据模式就是点击的位置，识别模式就是识别的code
     */
    fun notifyDataSetChange(position: Int) {
        featureAdapter.dataList[position].isSelect = !featureAdapter.dataList[position].isSelect
        featureAdapter.notifyDataSetChanged()
    }
}