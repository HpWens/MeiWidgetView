package com.meis.widget.praise;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * Created by wenshi on 2018/7/10.
 * Description 点赞估值器
 */
public class PraiseEvaluator implements TypeEvaluator<PointF> {

    // 贝塞尔曲线的控制点
    private PointF controlF1;
    private PointF controlF2;

    public PraiseEvaluator(PointF controlF1) {
        this.controlF1 = controlF1;
    }

    public PraiseEvaluator(PointF controlF1, PointF controlF2) {
        this.controlF1 = controlF1;
        this.controlF2 = controlF2;
    }

    @Override
    public PointF evaluate(float time, PointF startValue, PointF endValue) {
        //估值
        float currentX = 0;
        float currentY = 0;
        if (controlF2 == null) {
            // 二阶贝塞尔曲线
            currentX = arithmeticProduct(1 - time, 2) * startValue.x
                    + 2 * time * (1 - time) * controlF1.x
                    + arithmeticProduct(time, 2) * endValue.x;
            currentY = arithmeticProduct(1 - time, 2) * startValue.y
                    + 2 * time * (1 - time) * controlF1.y
                    + arithmeticProduct(time, 2) * endValue.y;
        } else {
            // 三阶贝塞尔曲线
            currentX = arithmeticProduct(1 - time, 3) * (startValue.x)
                    + 3 * arithmeticProduct(1 - time, 2) * time * (controlF1.x)
                    + 3 * (1 - time) * arithmeticProduct(time, 2) * (controlF2.x)
                    + arithmeticProduct(time, 3) * (endValue.x);
            currentY = arithmeticProduct(1 - time, 3) * (startValue.y)
                    + 3 * arithmeticProduct(1 - time, 2) * time * (controlF1.y)
                    + 3 * (1 - time) * arithmeticProduct(time, 2) * (controlF2.y)
                    + arithmeticProduct(time, 3) * (endValue.y);
        }
        return new PointF(currentX, currentY);
    }

    // 返回浮点数的开方值
    private float arithmeticProduct(float value, float square) {
        double pow = Math.pow(value, square);
        return (float) pow;
    }
}
