package tu.dresden.studybloxx.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StudybloxxAuthenticatorService extends Service {
    public StudybloxxAuthenticatorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        StudybloxxAuthenticator authenticator = new StudybloxxAuthenticator(this);
        return authenticator.getIBinder();
    }
}
