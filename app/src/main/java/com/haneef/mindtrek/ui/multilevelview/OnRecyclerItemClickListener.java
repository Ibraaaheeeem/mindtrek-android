package com.haneef.mindtrek.ui.multilevelview;

import android.view.View;

import com.haneef.mindtrek.ui.multilevelview.models.RecyclerViewItem;

public interface OnRecyclerItemClickListener {
    void onItemClick(View view, RecyclerViewItem item, int position);
}