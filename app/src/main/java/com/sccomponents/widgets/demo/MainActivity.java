package com.sccomponents.widgets.demo;

import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sccomponents.widgets.ScArc;
import com.sccomponents.widgets.ScGauge;
import com.sccomponents.widgets.ScNotchs;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;
        // Set the value to 30% take as reference a range of 0, 100.
        gauge.setValue(30, 0, 100);

        // Set the color gradient on the progress arc
        gauge.getBaseArc().setStrokeColors(
                Color.parseColor("#EA3A3C"),
                Color.parseColor("#FDE401"),
                Color.parseColor("#55B20C"),
                Color.parseColor("#3FA8F9")
        );

        // As the mask filter not support the hardware acceleration I must set the layer type
        // to software.
        gauge.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Apply the filter on the progress painter
        EmbossMaskFilter filter = new EmbossMaskFilter(new float[]{0.0f, 1.0f, 0.5f}, 0.8f, 3.0f, 5.0f);
        gauge.getBaseArc().getPainter().setMaskFilter(filter);
        gauge.getProgressArc().getPainter().setMaskFilter(filter);

        // Events
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDraw(Paint baseArc, Paint notchsArc, Paint progressArc) {
                // Set the progress color by taking the color from the base arc gradient.
                // Note that I passed the current gauge value because if not the current gradient
                // color if relative at the base arc draw angle.
                int color = gauge.getBaseArc()
                        .getCurrentGradientColor(gauge.getValue());
                progressArc.setColor(color);
            }

            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Hide the first and the last notchs
                info.visible = info.index > 0 && info.index < info.source.getNotchs() + 1;
            }
        });

        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Get the text control and write the value
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;
                counter.setText((int) gauge.getValue(0, 100) + "%");
            }
        });

    }
}
