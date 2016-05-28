package com.h6ah4i.example.arvexpandablegridlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Setup expandable feature and RecyclerView
        RecyclerViewExpandableItemManager expMgr = new RecyclerViewExpandableItemManager(null);

        GridLayoutManager lm = new GridLayoutManager(this, 2);
        MyAdapter adapter = new MyAdapter();

        lm.setSpanSizeLookup(new MySpanSizeLookup(expMgr, lm, adapter));

        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(expMgr.createWrappedAdapter(adapter));
        recyclerView.addItemDecoration(new MyItemDecoration(this, lm));

        expMgr.attachRecyclerView(recyclerView);
    }

    static abstract class MyBaseItem {
        public final long id;
        public final String text;

        public MyBaseItem(long id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    static class MyGroupItem extends MyBaseItem {
        public final List<MyChildItem> children;

        public MyGroupItem(long id, String text) {
            super(id, text);
            children = new ArrayList<>();
        }
    }

    static class MyChildItem extends MyBaseItem {
        public MyChildItem(long id, String text) {
            super(id, text);
        }
    }

    static abstract class MyBaseViewHolder extends AbstractExpandableItemViewHolder {
        TextView textView;

        public MyBaseViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    static class MyGroupViewHolder extends MyBaseViewHolder {
        public MyGroupViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class MyChildViewHolder extends MyBaseViewHolder {
        public int childCountInGroup;
        public int childPosition;

        public MyChildViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class MyAdapter extends AbstractExpandableItemAdapter<MyGroupViewHolder, MyChildViewHolder> {
        List<MyGroupItem> mItems;

        public MyAdapter() {
            setHasStableIds(true); // this is required for expandable feature.

            mItems = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                MyGroupItem group = new MyGroupItem(i, "GROUP " + i);
                for (int j = 0; j < 5; j++) {
                    group.children.add(new MyChildItem(j, "child " + j));
                }
                mItems.add(group);
            }
        }

        @Override
        public int getGroupCount() {
            return mItems.size();
        }

        @Override
        public int getChildCount(int groupPosition) {
            return mItems.get(groupPosition).children.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            // This method need to return unique value within all group items.
            return mItems.get(groupPosition).id;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            // This method need to return unique value within the group.
            return mItems.get(groupPosition).children.get(childPosition).id;
        }

        @Override
        public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_group_item, parent, false);
            return new MyGroupViewHolder(v);
        }

        @Override
        public MyChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_child_item, parent, false);
            return new MyChildViewHolder(v);
        }

        @Override
        public void onBindGroupViewHolder(MyGroupViewHolder holder, int groupPosition, int viewType) {
            MyGroupItem group = mItems.get(groupPosition);
            holder.textView.setText(group.text);
        }

        @Override
        public void onBindChildViewHolder(MyChildViewHolder holder, int groupPosition, int childPosition, int viewType) {
            MyGroupItem groupItem = mItems.get(groupPosition);
            MyChildItem group = groupItem.children.get(childPosition);
            holder.textView.setText(group.text);

            // Store childCountInGroup and childPosition values into the holder.
            // These values are used by MyItemDecoration.
            holder.childCountInGroup = groupItem.children.size();
            holder.childPosition = childPosition;
        }

        @Override
        public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
            return true;
        }
    }

    private static class MySpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        RecyclerViewExpandableItemManager expMgr;
        MyAdapter adapter;
        int spanCount;

        public MySpanSizeLookup(RecyclerViewExpandableItemManager expMgr, GridLayoutManager lm, MyAdapter adapter) {
            this.expMgr = expMgr;
            this.adapter = adapter;
            this.spanCount = lm.getSpanCount();
        }

        @Override
        public int getSpanSize(int position) {
            long packedPos = expMgr.getExpandablePosition(position);
            int childPos = RecyclerViewExpandableItemManager.getPackedPositionChild(packedPos);

            if (childPos == RecyclerView.NO_POSITION) {
                // group item
                return spanCount;
            } else {
                // child item
                return 1;
            }
        }
    }

    private static class MyItemDecoration extends RecyclerView.ItemDecoration {
        private Paint groupItemPaint;
        private Paint childItemPaint;
        private int spanCount;

        public MyItemDecoration(Context context, GridLayoutManager lm) {
            float density = context.getResources().getDisplayMetrics().density;

            groupItemPaint = new Paint();
            groupItemPaint.setColor(Color.rgb(180,180,180));
            groupItemPaint.setStrokeWidth(density);

            childItemPaint = new Paint();
            childItemPaint.setColor(Color.rgb(240,240,240));
            childItemPaint.setStrokeWidth(density);

            spanCount = lm.getSpanCount();
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = parent.getChildAt(i);

                RecyclerView.ViewHolder vh = parent.getChildViewHolder(view);

                if (vh instanceof MyGroupViewHolder) {
                    onDrawOverGroupItem(c, view, ((MyGroupViewHolder) vh));
                } else if (vh instanceof MyChildViewHolder) {
                    onDrawOverChildItem(c, view, ((MyChildViewHolder) vh));
                }
            }
        }

        private void onDrawOverGroupItem(Canvas c, View view, MyGroupViewHolder vh) {
            float tx = view.getTranslationX();
            float ty = view.getTranslationY();
            float left = view.getLeft() + tx;
            float top = view.getTop() + ty;
            float right = view.getRight() + tx;
            float bottom = view.getBottom() + ty;

            groupItemPaint.setAlpha((int) (255 * view.getAlpha()));

            // top
            c.drawLine(left, top, right, top, groupItemPaint);

            // bottom
            c.drawLine(left, bottom, right, bottom, groupItemPaint);
        }

        private void onDrawOverChildItem(Canvas c, View view, MyChildViewHolder vh) {
            float tx = view.getTranslationX();
            float ty = view.getTranslationY();
            float left = view.getLeft() + tx;
            float top = view.getTop() + ty;
            float right = view.getRight() + tx;
            float bottom = view.getBottom() + ty;

            int numRows = (vh.childCountInGroup + (spanCount - 1)) / spanCount;
            int row = vh.childPosition / spanCount;
            int col = vh.childPosition % spanCount;

            childItemPaint.setAlpha((int) (255 * view.getAlpha()));

            // top
            if (row > 0) {
                c.drawLine(left, top, right, top, childItemPaint);
            }

            // bottom
            if (row < (numRows - 1)) {
                c.drawLine(left, bottom, right, bottom, childItemPaint);
            }

            // right
            if (col < (spanCount - 1)) {
                c.drawLine(right, top, right, bottom, childItemPaint);
            }
        }
    }

}
