package cn.zhaoliang5156.androidsvmlib.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.zhaoliang5156.androidsvmlib.R
import cn.zhaoliang5156.androidsvmlib.domain.FeatureData
import kotlinx.android.synthetic.main.feature_layout.view.*

/**
 * Created by zhaoliang on 2017/7/22.
 */
class FeatureAdapter(var context: Context, var dataList: ArrayList<FeatureData>) : RecyclerView.Adapter<FeatureAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.feature_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (onFeatureClickListener != null) {
            holder.itemView.setOnClickListener {
                onFeatureClickListener.onControllerItemClick(holder.itemView, position)
            }
        }
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        // 判断传过来的列表数据集是否为空，如果为空返回0，如果不为空返回列表数量
        if (dataList == null) {
            return 0
        } else {
            return dataList.size
        }
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: FeatureData) {
            itemView.cb_feature.text = item.featureName
            itemView.cb_feature.isChecked = item.isSelect
        }
    }

    lateinit var onFeatureClickListener: OnFeatureItemClickListener

    interface OnFeatureItemClickListener {
        fun onControllerItemClick(view: View, position: Int)
    }
}