// 蛛网控件 欢迎大家关注「控件人生」公众号 每日现金红包等你来拿
import 'dart:math';

import 'package:flutter/material.dart';

void main() {
  runApp(new SpiderStatefulWidget());
}

class SpiderStatefulWidget extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return new SpiderWidget();
  }
}

class SpiderWidget extends State<SpiderStatefulWidget> {
  int edge = 6;

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return new MaterialApp(
      title: 'spider demo',
      home: new Scaffold(
        appBar: AppBar(
          title: Text('欢迎关注「控件人生」公众号'),
        ),
        body: new Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            new GestureDetector(
              child: Container(
                color: Colors.purpleAccent,
                width: 100,
                height: 48,
                margin: EdgeInsets.all(8),
                child: Center(
                    child: Text(
                  '增加边数',
                  style: new TextStyle(fontSize: 20, color: Colors.white),
                )),
              ),
              onTap: () {
                setState(() {
                  if (edge < 20) {
                    edge++;
                  }
                });
              },
            ),
            new GestureDetector(
              child: Container(
                color: Colors.blue,
                width: 100,
                height: 48,
                margin: EdgeInsets.all(8),
                child: Center(
                    child: Text(
                  '减少边数',
                  style: new TextStyle(fontSize: 20, color: Colors.white),
                )),
              ),
              onTap: () {
                setState(() {
                  if (edge > 3) {
                    edge--;
                  }
                });
              },
            ),
            new Center(
              child: new CustomPaint(
                painter: SpiderView(edge),
                size: new Size(300, 300),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class NetView extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    // TODO: implement paint
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) {
    // TODO: implement shouldRepaint
    return oldDelegate != this;
  }
}

class SpiderView extends CustomPainter {
  Paint mPaint;

  // 覆盖物画笔
  Paint mCoverPaint;

  // 文本画笔
  Paint mTextPaint;

  Path mPath;

  // 绘制边数默认为6
  int mEdgeSize = 6;

  final double CIRCLE_ANGLE = 360;

  // 整个绘制区域的中点坐标
  double mCenterX = 0;
  double mCenterY = 0;

  SpiderView(this.mEdgeSize) {
    // 初始化画笔
    mPaint = new Paint();
    mPaint.color = randomRGB();
    // 设置抗锯齿
    mPaint.isAntiAlias = true;
    // 样式为描边
    mPaint.style = PaintingStyle.stroke;

    mPath = new Path();

    mCoverPaint = new Paint();
    mCoverPaint.isAntiAlias = true;
    mCoverPaint.style = PaintingStyle.fill;
    mCoverPaint.color = randomARGB();

    mTextPaint = new Paint();
    mTextPaint.isAntiAlias = true;
    mTextPaint.style = PaintingStyle.fill;
    mTextPaint.color = Colors.blue;
  }

  @override
  void paint(Canvas canvas, Size size) {
    // TODO: implement paint

    mCenterX = size.width / 2;
    mCenterY = size.height / 2;

    // 图层 防止刷新属性结构
    canvas.save();
    drawSpiderEdge(canvas);
    drawCover(canvas);
    drawText(canvas);
    canvas.restore();
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) {
    // TODO: implement shouldRepaint
    return oldDelegate != this;
  }

  /**
   * 绘制边线
   */
  void drawSpiderEdge(Canvas canvas) {
    double angle = CIRCLE_ANGLE / mEdgeSize;
    double radius = 0;
    double radiusMaxLimit = mCenterX > mCenterY ? mCenterX : mCenterY;
    for (int i = 0; i < mEdgeSize; i++) {
      mPath.reset();
      radius = radiusMaxLimit / mEdgeSize * (i + 1);
      for (int j = 0; j < mEdgeSize + 1; j++) {
        // 移动
        if (j == 0) {
          mPath.moveTo(mCenterX + radius, mCenterY);
        } else {
          double x = mCenterX + radius * cos(degToRad(angle * j));
          double y = mCenterY + radius * sin(degToRad(angle * j));
          mPath.lineTo(x, y);
        }
      }
      mPath.close();
      canvas.drawPath(mPath, mPaint);
    }
    drawSpiderAxis(canvas, radiusMaxLimit, angle);
  }

  /**
   * 绘制轴线
   */
  void drawSpiderAxis(Canvas canvas, double radius, double angle) {
    for (int i = 0; i < mEdgeSize; i++) {
      mPath.reset();
      mPath.moveTo(mCenterX, mCenterX);
      double x = mCenterX + radius * cos(degToRad(angle * i));
      double y = mCenterY + radius * sin(degToRad(angle * i));
      mPath.lineTo(x, y);
      canvas.drawPath(mPath, mPaint);
    }
  }

  num degToRad(num deg) => deg * (pi / 180.0);

  num radToDeg(num rad) => rad * (180.0 / pi);

  /**
   * 绘制覆盖区域
   */
  void drawCover(Canvas canvas) {
    mPath.reset();
    Random random = new Random();

    double angle = CIRCLE_ANGLE / mEdgeSize;
    double radiusMaxLimit = min(mCenterY, mCenterY);
    for (int i = 0; i < mEdgeSize; i++) {
      double value = (random.nextInt(10) + 1) / 10;
      double x = mCenterX + radiusMaxLimit * cos(degToRad(angle * i)) * value;
      double y = mCenterY + radiusMaxLimit * sin(degToRad(angle * i)) * value;
      if (i == 0) {
        mPath.moveTo(x, mCenterY);
      } else {
        mPath.lineTo(x, y);
      }
    }
    mPath.close();
    canvas.drawPath(mPath, mCoverPaint);
  }

  /***
   * 绘制文本
   */
  void drawText(Canvas canvas) {
    // 尴尬了竟没有绘制文本的方法
  }

  /**
   * @return 获取到随机颜色值
   */
  Color randomARGB() {
    Random random = new Random();
    return Color.fromARGB(
        125, random.nextInt(255), random.nextInt(255), random.nextInt(255));
  }

  Color randomRGB() {
    Random random = new Random();
    return Color.fromARGB(
        255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
  }
}
