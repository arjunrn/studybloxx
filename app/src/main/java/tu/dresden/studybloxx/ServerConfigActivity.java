package tu.dresden.studybloxx;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


public class ServerConfigActivity extends Activity implements View.OnClickListener {

    private Button mContinueButton;
    private CheckBox mUseHTTPS;
    private EditText mServerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_config);

        mContinueButton = (Button) findViewById(R.id.server_config_continue_button);
        mContinueButton.setOnClickListener(this);

        mUseHTTPS = (CheckBox) findViewById(R.id.server_config_use_https);
        mServerAddress = (EditText) findViewById(R.id.server_config_server_addr);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.server_config_continue_button: {
                String serverAddr = mServerAddress.getText().toString();
                boolean useHttps = mUseHTTPS.isChecked();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("sync_server_address", (useHttps ? "https://" : "http://") + serverAddr);
                editor.commit();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                break;
            }
        }
    }
}
