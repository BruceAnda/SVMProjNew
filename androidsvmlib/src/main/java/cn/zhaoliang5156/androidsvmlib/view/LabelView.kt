package cn.zhaoliang5156.androidsvmlib.view

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import cn.zhaoliang5156.androidsvmlib.adapter.LableAdapter
import cn.zhaoliang5156.androidsvmlib.domain.LableData

/**
 * 标记Label的View，这里继承自RecyclerView，方便自己添加Lable项
 *
 * 这个view有两个地方使用 1:标记数据，2:识别数据
 *
 * Created by zhaoliang on 2017/7/22.
 */
class LabelView : RecyclerView {

    // 构造方法部分，构造对象用的
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    // 列表项适配器，填充数据用
    lateinit var lableAdapter: LableAdapter

    /**
     * 设置数据和点击事件的方法
     * @param dataList lable数据
     * @param spanCount 横向列表项显示的数量
     * @param onContrllerItemClickListener 列表项点击事件
     */
    fun setData(dataList: ArrayList<LableData>, spanCount: Int, onContrllerItemClickListener: LableAdapter.OnLableItemClickListener) {
        layoutManager = GridLayoutManager(context, spanCount)
        lableAdapter = LableAdapter(context, dataList)
        lableAdapter.onLabelClickListener = onContrllerItemClickListener
        adapter = lableAdapter
    }

    // 当前mode 默认是收集数据mode
    var mIsCollectionMode = true

    /**
     * 设置当前mode
     * @param isCollectionModel 是否是收集数据模式，true 收集数据模式，false，测试模式
     */
    fun setCollectionMode(isCollectionModel: Boolean) {
        mIsCollectionMode = isCollectionModel
    }

    /**
     * 更改数据显示样式
     * @param position 要更那一项数据，1.收集数据模式就是点击的位置，识别模式就是识别的code
     */
    fun notifyDataSetIcon(position: Int) {
        var status = 0
        if (mIsCollectionMode) {    // 收集数据模式
            status = 1
        } else {     // 测试模式
            status = 2
        }
        /*
        1. 把所有lable设置成没有选择模式
        2. 把点击的lable设置成选中模式
        */
        for (lableData in lableAdapter.dataList) {
            lableData.status = 0
        }
        lableAdapter.dataList[position].status = status
        lableAdapter.notifyDataSetChanged()
    }

    /**
     * 更改lable数量
     */
    fun notifyDataSetLable(position: Int) {
        lableAdapter.dataList[position].labelCount++
        lableAdapter.notifyDataSetChanged()
    }

    /**
     * 更改数据
     */
    fun notifyDataSetChange() {
        lableAdapter.notifyDataSetChanged()
    }
}