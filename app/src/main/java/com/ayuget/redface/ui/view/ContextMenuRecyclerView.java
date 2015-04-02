package com.ayuget.redface.ui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;

public class ContextMenuRecyclerView extends RecyclerView {
    public ContextMenuRecyclerView(Context context) {
        super(context);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private ContextMenu.ContextMenuInfo contextMenuInfo = null;

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return contextMenuInfo;
    }

    /**
     * Used to initialize before creating context menu and Bring up the context menu for this view.
     *
     * @param position for ContextMenuInfo
     */
    public void showContextMenuForPosition(int position) {
        if (position >= 0) {
            final long childId = getAdapter().getItemId(position);
            contextMenuInfo = createContextMenuInfo(position, childId);
        }
        showContextMenu();
    }


    private ContextMenu.ContextMenuInfo createContextMenuInfo(int position, long id) {
        return new RecyclerContextMenuInfo(position, id);
    }

    public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {
        private final int position;
        private final long id;

        public RecyclerContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        public int getPosition() {
            return position;
        }

        public long getId() {
            return id;
        }
    }

}
