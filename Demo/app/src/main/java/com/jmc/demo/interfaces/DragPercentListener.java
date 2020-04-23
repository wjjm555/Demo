package com.jmc.demo.interfaces;

import android.view.View;

public interface DragPercentListener {
    void onDragPercentChanged(float percent, View control, View content);

    void onDragPercentSet(float percent, View control, View content);
}
