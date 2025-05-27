package com.example.movieapp.Untils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemSpacingDerco(private val space:Int):RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.right = space
        outRect.left = space
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = 0 // Không thêm khoảng cách bên trái cho item đầu tiên
        }
        if (parent.getChildAdapterPosition(view) == parent.adapter?.itemCount?.minus(1)) {
            outRect.right = 0 // Không thêm khoảng cách bên phải cho item cuối cùng
        }
    }
}