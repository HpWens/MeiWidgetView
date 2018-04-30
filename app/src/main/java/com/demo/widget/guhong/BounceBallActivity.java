package com.demo.widget.guhong;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.demo.widget.R;
import com.demo.widget.dialog.BallDialog;
import com.meis.widget.ball.BounceBallView;

/**
 * desc:
 * author: wens
 * date: 2018/4/29.
 */
public class BounceBallActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private BounceBallView bbv1;
    private EditText bounceCount;
    private EditText ballCount;
    private EditText ballDelay;
    private EditText duration;
    private EditText radius;
    private CheckBox physicMode;
    private CheckBox randomPath;
    private CheckBox randomColor;
    private CheckBox randomRadius;
    private Button b1;
    private Button b2;
    private BallDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bounce_ball_activity);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dialog = new BallDialog();

        bbv1 = (BounceBallView) findViewById(R.id.bbv1);
        ballCount = (EditText) findViewById(R.id.ball_count);
        ballDelay = (EditText) findViewById(R.id.ball_delay);
        bounceCount = (EditText) findViewById(R.id.bounce_count);
        radius = (EditText) findViewById(R.id.radius);
        duration = (EditText) findViewById(R.id.duration);
        physicMode = (CheckBox) findViewById(R.id.physic_mode);
        randomColor = (CheckBox) findViewById(R.id.random_color);
        randomPath = (CheckBox) findViewById(R.id.random_path);
        randomRadius = (CheckBox) findViewById(R.id.random_radius);

        bbv1.post(new Runnable() {
            @Override
            public void run() {
                initText();
            }
        });
        bbv1.start();

        b2 = (Button) findViewById(R.id.b2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply(bbv1);
                initText();
                bbv1.start();
            }
        });

        b1 = (Button) findViewById(R.id.b1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show(getFragmentManager(), "1");
            }
        });
    }

    private void initText() {
        ballCount.setText(bbv1.getBallCount() + "");
        ballDelay.setText(bbv1.getBallDelay() + "");
        bounceCount.setText(bbv1.getBounceCount() + "");
        duration.setText(bbv1.getDefaultDuration() + "");
        radius.setText(bbv1.getRadius() + "");
        physicMode.setChecked(bbv1.isPhysicsMode());
        randomRadius.setChecked(bbv1.isRandomRadius());
        randomPath.setChecked(bbv1.isRandomBallPath());
        randomColor.setChecked(bbv1.isRandomColor());
    }

    public void apply(BounceBallView bbv) {
        if (bbv == null) {
            Toast.makeText(this, "BounceBallView is null", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            bbv.config()
                    .ballCount(Integer.parseInt(ballCount.getText().toString()))
                    .bounceCount(Integer.parseInt(bounceCount.getText().toString()))
                    .ballDelay(Integer.parseInt(ballDelay.getText().toString()))
                    .duration(Integer.parseInt(duration.getText().toString()))
                    .radius(Float.parseFloat(radius.getText().toString()))
                    .isPhysicMode(physicMode.isChecked())
                    .isRamdomPath(randomPath.isChecked())
                    .isRandomColor(randomColor.isChecked())
                    .isRandomRadius(randomRadius.isChecked())
                    .apply();
        } catch (Exception e) {
            Toast.makeText(this, "错误", Toast.LENGTH_LONG).show();
        }
    }
}
