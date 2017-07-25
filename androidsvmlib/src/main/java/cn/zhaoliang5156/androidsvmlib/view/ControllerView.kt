package cn.zhaoliang5156.androidsvmlib.view

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import cn.zhaoliang5156.androidsvmlib.adapter.ControllerAdapter
import cn.zhaoliang5156.androidsvmlib.adapter.FeatureAdapter
import cn.zhaoliang5156.androidsvmlib.domain.ControllerData

/**
 * Created by zhaoliang on 2017/7/22.
 */
class ControllerView : RecyclerView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    lateinit var controllerAdapter: ControllerAdapter

    fun setData(dataList: ArrayList<ControllerData>, spanCount: Int, onControllerItemClickListener: ControllerAdapter.OnControllerItemClickListener) {
        layoutManager = GridLayoutManager(context, spanCount)
        controllerAdapter = ControllerAdapter(context, dataList)
        controllerAdapter.onControllerClickListener = onControllerItemClickListener
        adapter = controllerAdapter
    }

    /**
     * 更改数据显示样式
     * @param position 要更那一项数据，1.收集数据模式就是点击的位置，识别模式就是识别的code
     */
    fun notifyDataSetChange(position: Int) {
        controllerAdapter.dataList[position].isSelect = !controllerAdapter.dataList[position].isSelect
        controllerAdapter.notifyDataSetChanged()
    }
}