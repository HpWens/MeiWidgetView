package com.meis.widget.utils;

import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author wenshi
 * @github
 * @Description
 * @since 2019/5/28
 */
public class WidgetUtil {
    /**
     * @param view   目标view
     * @param points 坐标点(x, y)
     * @return 坐标点是否在view范围内
     */
    public boolean pointInView(View view, float[] points) {
        // 像ViewGroup那样，先对齐一下Left和Top
        points[0] -= view.getLeft();
        points[1] -= view.getTop();
        // 获取View所对应的矩阵
        Matrix matrix = view.getMatrix();
        // 如果矩阵有应用过变换
        if (!matrix.isIdentity()) {
            // 反转矩阵
            matrix.invert(matrix);
            // 映射坐标点
            matrix.mapPoints(points);
        }
        //判断坐标点是否在view范围内
        return points[0] >= 0 && points[1] >= 0 && points[0] < view.getWidth() && points[1] < view.getHeight();
    }


    /**
     * 计算两个坐标点的顺时针角度，以第一个坐标点为圆心
     *
     * @param startX 起始点X轴的值
     * @param startY 起始点Y轴的值
     * @param endX   结束点X轴的值
     * @param endY   结束点Y轴的值
     * @return 以起始点为旋转中心计算的顺时针角度
     */
    private float computeClockwiseAngle(float startX, float startY, float endX, float endY) {
        //需要追加的角度
        int appendAngle = computeNeedAppendAngle(startX, startY, endX, endY);
        //线条长度
        float lineA = Math.abs(endX - startX);
        float lineB = Math.abs(endY - startY);
        //lineC = √￣ lineA² + lineB²
        float lineC = (float) Math.sqrt(Math.pow(lineA, 2) + Math.pow(lineB, 2));
        float angle;
        //如果是第二象限或第四象限，则计算斜边和水平线的夹角
        if (appendAngle == 0 || appendAngle == 180) {
            //cosB = lineA / lineC
            angle = (float) Math.toDegrees(Math.acos(lineA / lineC));
        } else {//如果是第一，第三象限，则计算斜边和垂直线的夹角
            //cosA = lineB / lineC
            angle = (float) Math.toDegrees(Math.acos(lineB / lineC));
        }
        //加上需要追加的角度
        return angle + appendAngle;
    }

    /**
     * 根据两点的位置来判断从起始点到结束点连线后的象限，并返回对应的角度
     *
     * @param startX 起始点X轴的值
     * @param startY 起始点Y轴的值
     * @param endX   结束点X轴的值
     * @param endY   结束点Y轴的值
     * @return 对应象限的顺时针基础角度
     */
    private int computeNeedAppendAngle(float startX, float startY, float endX, float endY) {
        int needAppendAngle;
        //1 or 4
        if (endX > startX) {
            if (endY > startY) {
                //4
                needAppendAngle = 0;
            } else {
                //1
                needAppendAngle = 270;
            }
        }
        //2 or 3
        else {
            if (endY > startY) {
                //3
                needAppendAngle = 90;
            } else {
                //2
                needAppendAngle = 180;
            }
        }
        return needAppendAngle;
        // return (endX > startX) ? (endY > startY ? 0 : 270) : (endY > startY ? 90 : 180);
    }

    /**
     * detachViewFromParent 注意重写改为 publish
     * detachViewFromParent 注意重写改为 publish
     *
     * @param target 目标 view 移动到顶部
     */
    private void moveToTop(View target, ViewGroup viewGroup) {
        //先确定现在在哪个位置
        int startIndex = viewGroup.indexOfChild(target);
        //计算一共需要几次交换，就可到达最上面
        int count = viewGroup.getChildCount() - 1 - startIndex;
        for (int i = 0; i < count; i++) {
            //更新索引
            int fromIndex = viewGroup.indexOfChild(target);
            //目标是它的上层
            int toIndex = fromIndex + 1;
            //获取需要交换位置的两个子View
            View from = target;
            View to = viewGroup.getChildAt(toIndex);

            // 先把它们拿出来
            // viewGroup.detachViewFromParent(toIndex);
            // viewGroup.detachViewFromParent(fromIndex);

            // 再放回去，但是放回去的位置(索引)互换了
            // viewGroup.attachViewToParent(to, fromIndex, to.getLayoutParams());
            // viewGroup.attachViewToParent(from, toIndex, from.getLayoutParams());
        }
        //刷新
        viewGroup.invalidate();
    }

}
