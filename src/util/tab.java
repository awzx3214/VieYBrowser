package kawaii.viey.browser;

import android.net.Uri;
import android.os.*;
import android.content.Intent;
import android.app.Service;

import java.util.List;

public class tab extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new stub();
    }

    private static class stub extends Binder {
        public Bundle extraCommand(String cmd, Bundle args) {
            return new Bundle();
        }

        public boolean isEngagementSignalsApiAvailable(Object cb, Bundle b) {
            return true;
        }

        public boolean isEphemeralBrowsingSupported(Bundle b) {
            return true;
        }

        public boolean mayLaunchUrl(Object cb, Uri uri, Bundle extras, List<Bundle> other) {
            return true;
        }

        public boolean newAuthTabSession(Object cb, Bundle b) {
            return false;
        }

        public boolean newSession(Object cb) {
            return true;
        }

        public boolean newSessionWithExtras(Object cb, Bundle b) {
            return true;
        }

        public int postMessage(Object cb, String msg, Bundle b) {
            return 1;
        }

        public void prefetch(Object cb, Uri uri, Bundle b) {}

        public void prefetchWithMultipleUrls(Object cb, List<Uri> uris, Bundle b) {}

        public boolean receiveFile(Object cb, Uri uri, int mode, Bundle b) {
            return true;
        }

        public boolean requestPostMessageChannel(Object cb, Uri uri) {
            return true;
        }

        public boolean requestPostMessageChannelWithExtras(Object cb, Uri uri, Bundle b) {
            return true;
        }

        public boolean setEngagementSignalsCallback(Object cb, IBinder binder, Bundle b) {
            return true;
        }

        public boolean updateVisuals(Object cb, Bundle b) {
            return true;
        }

        public boolean validateRelationship(Object cb, int relation, Uri uri, Bundle b) {
            return true;
        }

        public boolean warmup(long flags) {
            return true;
        }
    }
}
