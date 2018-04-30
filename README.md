# MeiWidgetView
[![](https://jitpack.io/v/HpWens/MeiBaseModule.svg)](https://jitpack.io/#HpWens/MeiBaseModule)
[![](https://img.shields.io/scrutinizer/build/g/filp/whoops.svg)](https://blog.csdn.net/u012551350/)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Hex.pm](https://img.shields.io/github/stars/HpWens/MeiBaseModule.svg)](https://github.com/HpWens/MeiBaseModule)


# MeiWidgetView
A powerful multi-function library that extended base activity for Android!

## 引入

### Step 1. Add the JitPack repository to your build file

root gradle

````
    allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }

````

### Step 2. Add the dependency

app gradle 

````
	dependencies {
	        compile 'com.github.HpWens:MeiBaseModule:1.0.5'
	}
````

### Step 3. extends MeiBase_

Activity extends MeiBaseActivity ,  Fragment extends MeiBaseFragment ,  Dialog extends MeiBaseDialog 

## Preview

<img src="/gif/demo1.gif" width="280px"/> <img src="/gif/demo2.gif" width="280px"/>
<img src="/gif/demo3.gif" width="280px"/> <img src="/gif/demo4.gif" width="280px"/> 
<img src="/gif/demo5.gif" width="280px"/> 

## Feature 

**1. 支持网络错误，空数据，正在加载，自定义等状态界面（一行代码切换状态）**

**2. 注入的方式实现下拉刷新，上拉加载（一行代码）且支持自定义下拉刷新样式**

**3. 支持单类型，多类型列表**

**4. 支持"单Activity ＋ 多Fragment","多模块Activity + 多Fragment"**

**5. 支持透明状态栏**

**6. 支持软键盘的状态监听**

**7. 支持软键盘触摸非输入区域自动隐藏**

**8. 实现可拖动的底部提示框**

## Example

````
//效果见图2
@PullToRefresh  // 一行代码 注入下拉刷新功能
@PullToLoadMore // 注入上拉加载
public class PullRefreshActivity extends BaseActivity {
// 需要继承 BaseActivity ; 同理 Fragment 继承 BaseFragment ; Dialog 继承 BaseDialog 

    @Override
    protected void initView() {
    //抽象方法，必须被重写 初始化控件 调用的先后顺序 initView  ->  initData
    }

    @Override
    protected void initData() {
    //抽象方法，必须被重写 初始化数据
        getToolbarView().setTitle(getResources().getString(R.string.refresh));
        getToolbarView().setNavigationIcon(R.mipmap.ic_arrow_back_white_24dp);
        getToolbarView().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        //初始显示空界面（可以配置空界面的图标和文本）
        setState(ViewState.EMPTY, new Object[]{getString(R.string.drag_refresh)});
    }

    @Override
    protected int layoutResId() {
    //抽象方法
        return 0;
    }

    @Override
    protected void onRefreshing() { //正在刷新
        super.onRefreshing();
        //rx方式（移除订阅防止泄露）
        postUiThread(2000, new UiSubscriber<Long>() {
            @Override
            public void onCompleted() {
                Toast.makeText(PullRefreshActivity.this, getResources().getString(R.string
                        .mei_refresh_success), Toast.LENGTH_SHORT).show();
                PullRefreshActivity.this.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onLoadingMore() { //正在加载更多
        super.onLoadingMore();
        postUiThread(2000, new UiSubscriber<Long>() {
            @Override
            public void onCompleted() {
                Toast.makeText(PullRefreshActivity.this, getResources().getString(R.string
                        .mei_refresh_success), Toast.LENGTH_SHORT).show();
                PullRefreshActivity.this.setLoadingMore(false);
            }
        });
    }
}

````


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

