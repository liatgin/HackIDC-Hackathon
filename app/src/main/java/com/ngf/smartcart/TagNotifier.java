package com.ngf.smartcart;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by Roni on 07/04/2015.
 */
public interface TagNotifier {
    public interface TagNotificationListener {
        void notify(int tagId);
    }

    void setListener(TagNotificationListener listener) throws Exception;
    void setActivity(Activity activity) throws Exception;
    boolean isRunning();
    void start() throws Exception;
    void stop();
    void resume();
    void pause();
    void handleIntent(Intent intent);
}
