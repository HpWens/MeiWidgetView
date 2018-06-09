package com.meis.widget.mobike;

import android.view.View;
import android.view.ViewGroup;

import com.meis.widget.R;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
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
public class MoBike {

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
    private float density = 0.5f;

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

    public MoBike(ViewGroup viewGroup) {
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

    private void createWorldChild(boolean change) {
        if (null != mViewGroup) {
            int count = mViewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View childView = mViewGroup.getChildAt(i);
                if (!isBodyView(childView) || change) {
                    createBody(childView);
                }
            }
        }
    }

    private void createBody(View childView) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;

        //设置view中心点位置
        bodyDef.position.set(mappingView2Body(childView.getX() + childView.getWidth() / 2),
                mappingView2Body(childView.getY() + childView.getHeight() / 2));

        Shape shape = null;
        Boolean isCircle = (boolean) childView.getTag(R.id.wd_view_circle_tag);
        if (isCircle != null && isCircle) {
            shape = createCircleBody(childView);
        } else {
            shape = createPolygonBody(childView);
        }

        //设置系数
        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.density = density;
        def.friction = frictionRatio;
        def.restitution = restitutionRatio;

        Body body = world.createBody(bodyDef);
        body.createFixture(def);

        childView.setTag(R.id.wd_view_body_tag, body);
        body.setLinearVelocity(new Vec2(random.nextFloat(), random.nextFloat()));
    }

    private Shape createPolygonBody(View childView) {
        PolygonShape polygonShape = new PolygonShape();
        //形状的大小为 view 的一半 （还可以等比缩放）
        polygonShape.setAsBox(mappingView2Body(childView.getWidth() / 2), mappingView2Body(childView.getHeight() / 2));
        return polygonShape;
    }

    private Shape createCircleBody(View childView) {
        CircleShape circleShape = new CircleShape();
        //半径为 宽、高的一半
        circleShape.setRadius(mappingView2Body(childView.getHeight() / 2));
        return circleShape;
    }

    public void onSizeChange(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void onDraw() {
        if (world != null) {
            world.step(dt, velocityIterations, countIterations);
        }
        int count = mViewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mViewGroup.getChildAt(i);
            if (isBodyView(view)) {
                view.setX(getViewX(view));
                view.setY(getViewY(view));
                view.setRotation(getViewRotation(view));
            }
        }
        //这里不要使用post会导致界面延迟绘制导致抖动
        mViewGroup.invalidate();
    }

    public void onLayout(boolean changed) {
        createWorld();
        createWorldChild(changed);
    }

    private boolean isBodyView(View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        return body != null;
    }

    private float getViewX(View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        if (null != body) {
            //注意换算
            return mappingBody2View(body.getPosition().x) - view.getWidth() / 2;
        }
        return 0;
    }

    private float getViewY(View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        if (null != body) {
            //注意换算
            return mappingBody2View(body.getPosition().y) - view.getHeight() / 2;
        }
        return 0;
    }

    private float getViewRotation(View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        if (null != body) {
            float angle = body.getAngle();
            //注意换算
            return (angle / 3.14f * 180f) % 360;
        }
        return 0;
    }

    public void onSensorChanged(float x, float y) {
        int childCount = mViewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mViewGroup.getChildAt(i);
            if (isBodyView(view)) {
                applyLinearImpulse(x, y, view);
            }
        }
    }

    public void onRandomChanged() {
        int childCount = mViewGroup.getChildCount();
        float x = random.nextInt(800) - 800;
        float y = random.nextInt(800) - 800;
        for (int i = 0; i < childCount; i++) {
            View view = mViewGroup.getChildAt(i);
            if (isBodyView(view)) {
                applyLinearImpulse(x, y, view);
            }
        }
    }

    private void applyLinearImpulse(float x, float y, View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        if (null != body) {
            //N秒或kg-m / s为单位
            Vec2 vec2 = new Vec2(x, y);
            //对body施加一个冲量
            /// 应用一个冲量到一个点上，这将立即改变速度。（这句话的意思是突然在一个点上作用一个力）
            /// 如果这个点不再应用程序的质心上，它还会修改角速度. 唤醒 body.
            /// @param impulse  world 的矢量冲量, 通常 N-seconds or kg-m/s.
            /// @param point 应用里面的某个点的 world 位置
            body.applyLinearImpulse(vec2, body.getPosition(), true);
        }
    }
}
