## 动态模糊组件HokoBlur

### 1. 描述

Kotlin实现版本见 [HokoBlur-Kotlin](https://github.com/HokoFly/HokoBlur-Kotlin)

- 组件主要提供以下功能：

	- 给图片添加模糊效果；
	- **动态模糊，对背景的实时模糊**。

- 组件主要的特性：
	- 多种实现方案，包括RenderScript、OpenGL、Native和Java；
	- 多种算法，包括Box、Stack和Gaussian算法，满足不同的模糊效果；
	- 多核多线程，提升模糊效率，增加异步调用Api；
	
### 2. 组件版本

```groovy
   implementation 'io.github.hokofly:hoko-blur:1.5.3'
```

### 3.使用姿势

#### 3.1 API调用

完整的api如下

```java
HokoBlur.with(context)
    .scheme(Blur.SCHEME_NATIVE) //设置模糊实现方案，包括RenderScript、OpenGL、Native和Java实现，默认为Native方案
    .mode(Blur.MODE_STACK) //设置模糊算法，包括Gaussian、Stack和Box，默认并推荐选择Stack算法
    .radius(10) //设置模糊半径，内部最大限制为25，默认值5
    .sampleFactor(2.0f) // 设置scale因子，factor = 2时，内部将bitmap的宽高scale为原来的 1/2，默认值5
    .forceCopy(false) //对于scale因子为1.0f时，会直接修改传入的bitmap，如果你不希望修改原bitmap，设置forceCopy为true即可，默认值false
    .translateX(150)//可对部分区域进行模糊，这里设置x轴的偏移量
    .translateY(150)//可对部分区域进行模糊，这里设置y轴的偏移量
    .processor() //获得模糊实现类
    .blur(bitmap);	//模糊图片，方法是阻塞的，底层为多核并行实现，异步请使用asyncBlur

```
日常并不需要如此复杂的参数设置，如果单纯只是想添加模糊效果，可以这样调用：

```java
//doBlur()将返回模糊后的Bitmap
Bitmap outBitmap = Blur.with(context).blur(bitmap);

```

对于尺寸很大的图，建议使用异步的方式调用


```java
HokoBlur.with(this)
    .scheme(Blur.SCHEME_NATIVE)
    .mode(Blur.MODE_STACK)
    .radius(10)
    .sampleFactor(2.0f)
    .forceCopy(false)
    .asyncBlur(bitmap, new AsyncBlurTask.CallBack() {
        @Override
        public void onBlurSuccess(Bitmap outBitmap) {
        	// do something...
        }

        @Override
        public void onBlurFailed() {

        }
    });

```

### 3.2 效果展示

#### 动画

<img src="graphic/animation_blur_progress.gif" width = "370" height = "619" alt="动态模糊" />

#### 任意部位模糊

较高的模糊处理效率，可以实现任意部位的实时模糊。实际并不需要特别大尺寸的图只需要选取屏幕的一部分即可。

<img src="graphic/dynamic_blur.gif" width = "370" height = "600" alt="动态模糊" />

#### 3.3 动态背景模糊
动态模糊提供了对View以及ViewGroup的实时背景模糊，并不是针对Bitmap的实现。组件将会对View所在区域进行模糊。详情参见工程 [HokoBlurDrawable](https://github.com/HokoFly/HokoBlurDrawable).

<img src="graphic/blur_drawable.gif" width = "370" alt="动态模糊" />


### 4. 注意事项


1. 当未对Bitmap进行scale操作(```sampleFactor(1.0f)```)，传入的Bitmap将会被之后的操作直接修改。所以当函数返回某个bitmap的时候，可以被立刻使用到控件上面去。

2. **强烈建议使用在模糊操作之前，进行downScale操作，降低被模糊图片的大小，这将大幅提升模糊效率和效果。**

3. 请将模糊半径限制在25内（组件内部同样进行了限制），增加半径对模糊效果的提升远小于通过增加scale的缩放因子的方式，而且半径增加模糊效率也将降低；

4. RenderScript方案因为兼容性有待验证，如果有需要更大计算量和更复杂模糊效果的场景，可以考虑RenderScript方案。

5. 算法的选择
	- 如果你对模糊效果要求不高，同时希望较快完成图片的模糊，请选择Box算法；
	- 如果你对模糊效果要求较高，同时可以忍受较慢完成图片的模糊，请选择Gaussian算法；
	- Stack算法有非常接近Gaussian算法的模糊效果，同时提升了算法效率，一般情况下使用Stack算法即可；
6. BlurDrawable通过OpenGL实现，因此如果页面未开启硬件加速，背景模糊将无效。

7. 示例与用法
具体示例详见组件工程


