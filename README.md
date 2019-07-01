<div align="left">
<a href="https://jitpack.io/#HpWens/MeiWidgetView">
    <img src="https://jitpack.io/v/HpWens/MeiWidgetView.svg">
</a>		
<a href="https://blog.csdn.net/u012551350/">
    <img src="https://img.shields.io/scrutinizer/build/g/filp/whoops.svg">
</a>
<a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img src="https://img.shields.io/hexpm/l/plug.svg">
</a>
<a href="https://github.com/HpWens/MeiWidgetView">
    <img src="https://img.shields.io/github/stars/HpWens/MeiWidgetView.svg?color=ff69b4">
</a>
<a href="https://developer.android.com/about/versions/android-4.0.3.html">
    <img src="https://img.shields.io/badge/API-15+-blue.svg?color=orange" alt="Min Sdk Version">
</a>
<img src="https://img.shields.io/badge/Gamil-wsboy.code@gmail.com-blue.svg">
<img src="https://img.shields.io/badge/QQ%E7%BE%A4-478720016-blue.svg">
</div>

### 最新动态-网页动态背景“五彩蛛网”

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190327112054473.gif)![在这里插入图片描述](https://img-blog.csdnimg.cn/20190326164634380.gif)

博客地址：[Android实现网页动态背景“五彩蛛网”](https://blog.csdn.net/u012551350/article/details/88821610)

想了解更多大厂炫酷控件，请关注微信公众号：控件人生

<div align=center><img src="https://upload-images.jianshu.io/upload_images/2258857-196f00b808ab8668.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240" width="200px"/></div>

<div align=center>扫一扫 关注我的公众号</div>

<div align=center>不定期的发放现金红包 快到碗里来~</div>

### 自定义LayoutManager

![01](https://img-blog.csdnimg.cn/20190627175756545.gif)
![02](https://img-blog.csdnimg.cn/20190627175806441.gif)

博客地址：[Android自定义控件进阶篇，自定义LayoutManager](https://blog.csdn.net/u012551350/article/details/93971801)

### GIF圆角控件

<div align="center">
<img src="https://img-blog.csdnimg.cn/20190407133537583.gif">
</div>	

博客地址：[探一探，非常实用的GIF图圆角控件（3行代码）](https://blog.csdn.net/u012551350/article/details/89068414)

### 小红书任意拖拽标签控件

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190311165834470.gif)![在这里插入图片描述](https://img-blog.csdnimg.cn/20190313182236310.gif)

博客地址：[Android控件人生第一站，小红书任意拖拽标签控件](https://blog.csdn.net/u012551350/article/details/88395427)

### 小红书自定义CoordinatorLayout联动效果

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190305114642699.gif)![在这里插入图片描述](https://img-blog.csdnimg.cn/20190305114735124.gif)

博客地址：[第一站小红书图片裁剪控件之二，自定义CoordinatorLayout联动效果](https://blog.csdn.net/u012551350/article/details/88173578)

### 小红书图片裁剪控件

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190225141450923.gif)![在这里插入图片描述](https://img-blog.csdnimg.cn/20190225141507355.gif)

博客地址：[第一站小红书图片裁剪控件，深度解析大厂炫酷控件](https://blog.csdn.net/u012551350/article/details/87928720)

### Flutter“蛛网”控件

<div align="center">
<img src="https://img-blog.csdnimg.cn/20190418165735299.gif">
</div>	

博客地址：[Flutter自定义控件第一式，炫酷“蛛网”控件](https://blog.csdn.net/u012551350/article/details/89383421)

[源码地址](https://github.com/HpWens/MeiWidgetView/blob/master/app/src/main/java/com/demo/flutter/widget_spider_web.dart)

# MeiWidgetView

一款汇总了郭霖，鸿洋，以及自己平时收集的自定义控件的集合库。主旨帮助大家学习自定义控件中的一些技巧，分析问题解决问题的一种思路。

## 引入

### Step 1. Add the JitPack repository to your build file

root build.gradle

````
    allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }

````

### Step 2. Add the dependency

app build.gradle

````
	dependencies {
	       implementation 'com.github.HpWens:MeiWidgetView:v0.1.6'
	}
````

## [Download APK](https://www.pgyer.com/zKF4)

## 使用

### 1、文字路径

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_main.gif" width="280px"/> 

#### b、xml布局

````
    <com.meis.widget.MeiTextPathView
        ...
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
````

#### c、xml属性 

````
    <declare-styleable name="MeiTextPathView">
        <!-- 路径文字 -->
        <attr name="text" format="string"/>  
	<!-- 路径文字大小 -->
        <attr name="textSize" format="dimension"/>
	<!-- 路径文字颜色 -->
        <attr name="textColor" format="color"/>
	<!-- 路径绘制时长 -->
        <attr name="duration" format="integer"/>
	<!-- 文字的描边宽度 -->
        <attr name="strokeWidth" format="dimension"/>
	<!-- 是否循环绘制 -->
        <attr name="cycle" format="boolean"/>
	<!-- 是否自动开始播放 -->
        <attr name="autoStart" format="boolean"/>
    </declare-styleable>
````

#### d、参考文章

[文字路径动画控件TextPathView解析](https://blog.csdn.net/totond/article/details/79375200)

### 2、弹跳小球

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/bounce_ball.gif" width="280px"/> 

#### b、xml布局

````
    <com.meis.widget.ball.BounceBallView
        ...
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
````

#### c、属性

- bounce_count :小球弹跳次数
- ball_color:小球颜色
- ball_count:小球数量
- ball_radius:小球半径
- ball_delay:小球出现时间间隔（当小球数大于1时）
- anim_duration:小球一次动画时长
- physic_mode : 开启物理效果（下落加速上升减速）
- random_color: 开启小球颜色随机
- random_radius: 开启小球大小随机（在基础大小上下浮动）
- random_path: 开启小球路径随机（在基础路径坐标上下浮动）

#### d、参考文章

[自定义View之小球自由落体弹跳加载控件](http://blog.csdn.net/ccy0122/article/details/77427795)

### 3、扩散圆（主题切换）

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_ripple.gif" width="280px"/> 

#### b、xml布局

````
    <com.meis.widget.MeiRippleView
        ...
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
````

#### c、相关方法

````
    /**
     * @param startX      被点击view相对屏幕的 view中心点x坐标
     * @param startY      被点击view相对屏幕的 view中心点y坐标
     * @param startRadius 开始扩散的半径
     */
    public void startRipple(int startX, int startY, int startRadius)
````

#### d、参考文章

[Android自定义View实现炫酷的主题切换动画(仿酷安客户端)](https://blog.csdn.net/u011387817/article/details/79604418)

### 4、酷炫的路径

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_line_path.gif" width="280px"/> 

#### b、xml布局

````
    <com.meis.widget.MeiLinePathView
        ...
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
````

#### c、相关方法

````
    //设置路径
    public void setPath(Path path) {
        mKeyframes = new Keyframes(path);
        mAlpha = 0;
    }
````

#### d、参考文章

[Android仿bilibili弹幕聊天室后面的线条动画](https://blog.csdn.net/u011387817/article/details/78817827)

### 5、MEI-图片滚动视差控件

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_parallax.gif" width="280px"/> 

#### b、xml布局

````
    <com.meis.widget.MeiScrollParallaxView
        ...
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>	
````

#### c、属性

````
    <declare-styleable name="MeiScrollParallaxView">
        <!-- 滚动速率 （0~1） 值越大滚动视差越明显 -->
        <attr name="parallaxRate" format="float"/>
        <!-- 滑动是否显示视差 默认 true -->
        <attr name="enableParallax" format="boolean"/>
        <!-- 圆角宽度 默认 0  若通过修改父类来实现 则不需要设置此值-->
        <attr name="roundWidth" format="dimension"/>
        <!-- 是否显示圆形 默认 0  若通过修改父类来实现 则不需要设置此值-->
        <attr name="enableCircle" format="boolean"></attr>
        <!--圆角外的颜色 默认白色  若通过修改父类来实现 则不需要设置此值-->
        <attr name="outRoundColor" format="color"/>
    </declare-styleable>
````

#### d、参考文章

[打造丝滑的滑动视差控件（ScrollParallaxView）](https://blog.csdn.net/u012551350/article/details/79275773)

### 6、MEI-直播间送爱心

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_heart.gif" width="280px"/> 

#### b、xml布局

````
    <com.meis.widget.heart.MeiHeartView
        ...
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
````

#### c、属性

````
    <declare-styleable name="MeiHeartView">
        <!--爱心动画时长-->
        <attr name="heartDuration" format="integer"/>
        <!--是否显示透明度动画-->
        <attr name="heartEnableAlpha" format="boolean"/>
        <!--是否显示缩放动画-->
        <attr name="heartEnableScale" format="boolean"/>
    </declare-styleable>
````

#### d、参考文章

[PathMeasure之直播间送爱心](https://blog.csdn.net/u012551350/article/details/78168459)

### 7、Mei-selector控件集

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_shape.gif" width="280px"/> 

通过 `xml` 布局的方式替换掉 `selector` 文件 , 这么做的优势在于 , 减少 `apk` 体积 , 避免后期维护大量的 `selector` 文件 , 扩展性更强 , 易修改 , 直观 ， 功能更加强大 

#### b、特性

-  支持圆角（单独设定四个角角度，圆角半径是高度的一半）
-  支持背景Pressed，Disabled，Selected，Checked四种状态切换
-  支持描边（虚线，四种状态切换）
-  支持文本（四种状态切换）
-  支持涟漪（水波纹）
-  支持leftDrawable，topDrawable，rightDrawable，bottomDrawable四种状态切换

#### c、支持原生控件

-  RadiusTextView
-  RadiusCheckBox
-  RadiusEditText
-  RadiusFrameLayout
-  RadiusLinearLayout
-  RadiusRelativeLayout

#### d、扩展

委托的扩展方式（Delegate）, 参考的是（AppCompatActivity实现方式）, 具体请参考（RadiusTextView）

### 8、MEI-仿百度浏览器图片拖拽控件

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_drag.gif" width="280px"/> 

#### b、xml布局

````
//PhotoDragRelativeLayout 继承 RelativeLayout 委托的方式 易扩展
<com.meis.widget.photodrag.PhotoDragRelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/pdr_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000">

    <android.support.v7.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/colorPrimary"
        app:navigationIcon="@mipmap/ic_arrow_back_white_24dp"
        app:title="仿百度浏览器图片拖拽控件"
        app:titleTextColor="#FFF" />

    <me.relex.photodraweeview.PhotoDraweeView
        android:id="@+id/pdv_photo"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@+id/toolbar"
        android:src="@mipmap/ic_mei_ripple" />

</com.meis.widget.photodrag.PhotoDragRelativeLayout>
````

#### c、相关代码

````
   mPdrLayout.setDragListener(new PhotoDragHelper().setOnDragListener(new PhotoDragHelper.OnDragListener() {
       @Override
       public void onAlpha(float alpha) {
           //透明度的改变
           mPdrLayout.setAlpha(alpha);
       }
       @Override
       public View getDragView() {
           //返回需要拖拽的view
           return mPdvView;
       }
       @Override
       public void onAnimationEnd(boolean isRestoration) {
           //isRestoration true 执行恢复动画  false 执行结束动画
           if (!isRestoration) {
               finish();
               overridePendingTransition(0, 0);
           }
       }
   }));
````

### 9、MEI-仿头条小视频拖拽控件

针对头条效果做了如下优化

-  列表图片没有完全展示点击的转场动画图片明显变形压缩
-  详情页往顶部拖拽有明显的卡顿现象

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_video_drag.gif" width="280px"/> 

#### b、xml布局

````
<com.meis.widget.photodrag.VideoDragRelativeLayout
    ...
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
````

#### c、属性

````
    <declare-styleable name="VideoDragRelativeLayout">

        <!-- 统一前缀 mei 当前父控件是否拦截事件 默认true-->
        <attr name="mei_self_intercept_event" format="boolean"></attr>

        <!-- 进入动画时长  默认 400 -->
        <attr name="mei_start_anim_duration" format="integer"></attr>

        <!-- 结束动画时长  默认 400 -->
        <attr name="mei_end_anim_duration" format="integer"></attr>

        <!-- 恢复系数有关[0~1] 恢复系数越大则需要拖动越大的距离 -->
        <attr name="mei_restoration_ratio" format="float"></attr>

        <!-- y轴偏移速率 值越大偏移越慢 默认2 -->
        <attr name="mei_offset_rate_y" format="integer"></attr>

        <!-- y轴开始偏移系数 默认0.5 -->
        <attr name="mei_start_offset_ratio_y" format="float"></attr>

        <!-- 开始动画是否进入 默认true -->
        <attr name="mei_start_anim_enable" format="boolean"></attr>

    </declare-styleable>
````

`VideoDragRelativeLayout` 继承 `RelativeLayout` 默认拦截并消费事件 , 若子控件想消费事件请在 `xml` 布局文件中设置子控件 `android:tag="dispatch"` 

#### d、回调接口

````
    public interface OnVideoDragListener {

        //开始拖拽
        void onStartDrag();

        /**
         * 释放拖拽
         * @param isRestoration 是否恢复 true 则执行恢复动画  false 则执行结束动画
         */
        void onReleaseDrag(boolean isRestoration);

        /**
         * 动画结束
         * @param isRestoration 是否恢复 true 执行的恢复动画结束  false执行的结束动画结束
         */
        void onCompleteAnimation(boolean isRestoration);
    }
````

### 10、仿膜拜单车贴纸效果 

基于[jbox2d](https://github.com/jbox2d/jbox2d)引擎实现 , 文中有相应的代码注释请查阅

#### a、效果图

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_mo_bike.gif" width="280px"/> 

#### b、xml布局

````
    <com.meis.widget.mobike.MoBikeView
        android:layout_width="0dp"
        android:layout_height="0dp"
	...   
	/>
````

### 11、LOVE玫瑰

#### a、效果预览

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_rose.gif" width="280px"/> 

#### b、xml布局

````
    <com.meis.widget.rose.RoseGiftSurfaceView
        android:id="@+id/rose"
        android:layout_width="0dp"
        android:layout_height="0dp"
        ... />
````

#### c、开始动画

````
 mRoseGiftSurfaceView.startAnimation();
````

### 12、浮动粒子

通过三阶贝塞尔曲线，绘制每个粒子的运动轨迹

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_float_particle.gif" width="280px"/> 

[请下载apk查看实际效果](https://www.pgyer.com/zKF4)

#### b、xml布局

````
    <com.meis.widget.particle.FireflyView
        android:id="@+id/firfly"
        android:layout_width="0dp"
        android:layout_height="0dp"
        ... />
````

#### c、属性

````
    <declare-styleable name="FireflyView">
        <!-- 浮点粒子数量 默认400 -->
        <attr name="firefly_num" format="integer"></attr>
        <!-- 浮点粒子的最大半径 默认5 -->
        <attr name="firefly_max_radius" format="integer"></attr>
        <!-- 浮点粒子的移动速率 默认5 越大移动越慢 -->
        <attr name="firefly_move_rate" format="integer"></attr>
    </declare-styleable>
````

### 13、直播间点赞控件

贝塞尔曲线来计算点赞小图标的位置

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_praise.gif" width="280px"/> 

#### b、相关代码

````
// 初始化
mPraiseAnimator = new BezierPraiseAnimator(this);
// 开始动画
mPraiseAnimator.startAnimation(mIvPraise);
````

### 14、文本跳动控件

一款炫酷的文本跳动控件

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_evaporate.gif" width="280px"/> 

文章博客地址：[一篇文本跳动控件，为你打开一扇大门，学会这两点心得，控件你也会写](https://www.jianshu.com/p/2a549c5f0a82)

### 15、豆瓣弹性滑动控件

<img src="https://github.com/HpWens/MCropImageView/blob/master/gif/mei_scroll.gif" width="280px"/> 

文章博客地址：[仿豆瓣弹性滑动控件，史上最全方位讲解事件滑动冲突](https://www.jianshu.com/p/2d7a63455c83)

## Contact

QQ群：478720016

## [wiki](https://github.com/HpWens/MeiWidgetView/wiki)

欢迎发邮件或者提issue

## LICENSE

````
Copyright 2018 文淑

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````
