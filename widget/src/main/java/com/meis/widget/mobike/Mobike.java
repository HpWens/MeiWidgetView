package com.meis.widget.mobike;

import android.view.ViewGroup;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import java.util.Random;

/**
 * desc:
 * author: wens
 * date: 2018/6/6.
 */
public class Mobike {

    private ViewGroup mViewGroup;

    //迭代频率
    private float dt = 1f / 60f;

    //迭代速率
    private int velocityIterations = 3;

    //迭代次数
    private int countIterations = 10;

    //模拟世界和view坐标的转化比例
    private int proportion = 50;

    //密度
    private float density = 0.6f;

    //摩擦系数
    private float frictionRatio = 0.3f;

    //恢复系数
    private float restitutionRatio = 0.3f;

    //是否绘制
    private boolean drawEnable = true;

    private Random random = new Random();

    private World world;

    private int width;
    private int height;

    public Mobike(ViewGroup viewGroup) {
        mViewGroup = viewGroup;
        density = mViewGroup.getContext().getResources().getDisplayMetrics().density;
    }

    private void createWorld() {
        if (world == null) {
            //分别设置水平方向  垂直方向的重力加速度
            //水平方向为0  垂直方向为10.0 更准确取值应该为9.8 比现实世界稍大
            world = new World(new Vec2(0, 10.0f));

            //巧妙的方法在边缘加上了刚体
            updateTopAndBottomBounds();
            updateLeftAndRightBounds();
        }
    }

    private void updateLeftAndRightBounds() {
        BodyDef bodyDef = new BodyDef();
        //生成静态刚体
        bodyDef.type = BodyType.STATIC;

        //设置形状 - 多边形
        PolygonShape shape = new PolygonShape();

        //hx 1  hy 屏幕高度
        float hx = mappingView2Body(proportion);
        float hy = mappingView2Body(height);
        shape.setAsBox(hx, hy);

        //设置 系数
        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.density = density;
        def.friction = frictionRatio;
        def.restitution = restitutionRatio;

        //-1 高度
        bodyDef.position.set(-hx, hy);
        Body leftBody = world.createBody(bodyDef);
        //设置位置
        leftBody.createFixture(def);

        //这里便于理解 应该是 w + 1  h
        //而不是 w + 1 0 验证结果一样
        bodyDef.position.set(mappingView2Body(width) + hx, 0);
        Body rightBody = world.createBody(bodyDef);
        rightBody.createFixture(def);
    }

    //现实世界 - 虚拟世界
    private float mappingView2Body(float view) {
        return view / proportion;
    }

    //虚拟世界 - 现实世界
    private float mappingBody2View(float body) {
        return body * proportion;
    }

    private void updateTopAndBottomBounds() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;

        PolygonShape shape = new PolygonShape();
        float hx = mappingView2Body(width);
        float hy = mappingView2Body(proportion);
        shape.setAsBox(hx, hy);

        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.density = density;
        def.friction = frictionRatio;
        def.restitution = restitutionRatio;

        bodyDef.position.set(0, -hy);
        Body topBody = world.createBody(bodyDef);
        topBody.createFixture(def);

        bodyDef.position.set(0, mappingView2Body(height) + hy);
        Body bottomBody = world.createBody(bodyDef);
        bottomBody.createFixture(def);
    }

    public void onSizeChange(int width, int height) {

    }

    public void onDraw() {

    }

    public void onLayout(boolean changed) {

    }
}
