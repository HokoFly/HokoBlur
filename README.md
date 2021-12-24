## HokoBlur

(中文版本请参看[这里](doc/README_CN.md))

HokoBlur is an Android component which provides dynamic blur effect.

See the Kotlin implement [HokoBlur-Kotlin](https://github.com/HokoFly/HokoBlur-Kotlin)


### 1. Introductions

- Functions：

	- Add blur to the image；
	- **Dynamic blur, real-time blurring of the background**。

- Features：
	- Multiple schemes: RenderScript、OpenGL、Native and Java；
	- Multiple algorithms: Box、Stack and Gaussian algorithms. Provide different blur effect；
	- Multi-core and multi-threading, accelerate blurring，asynchronous interface；
	- 🚀iOS-like dynamic backgroud blur: Compared to using RenderScript to process bitmaps, the component has better performance and effect.


### 2. Getting started


#### Download

```groovy
   implementation 'io.github.hokofly:hoko-blur:1.3.6'
```

#### Static Blur

synchronous api

```java
HokoBlur.with(context)
    .scheme(Blur.SCHEME_NATIVE) //different implementation, RenderScript、OpenGL、Native(default) and Java
    .mode(Blur.MODE_STACK) //blur algorithms，Gaussian、Stack(default) and Box
    .radius(10) //blur radius，max=25，default=5
    .sampleFactor(2.0f) //scale factor，if factor=2，the width and height of a bitmap will be scale to 1/2 sizes，default=5
    .forceCopy(false) //If scale factor=1.0f，the origin bitmap will be modified. You could set forceCopy=true to avoid it. default=false
    .needUpscale(true) //After blurring，the bitmap will be upscaled to origin sizes，default=true
    .translateX(150)//add x axis offset when blurring
    .translateY(150)//add y axis offset when blurring
    .processor() //build a blur processor
    .blur(bitmap);	//blur a bitmap, synchronous method

```

Daily development does not need such complicated settings. If you want a blur effect, just use as follow:

```java
Bitmap outBitmap = Blur.with(context).blur(bitmap);

```

When it comes to a large size bitmap, it is recommended to use an asynchronous method. The blur job could be cancelled.


```java
Future f = HokoBlur.with(this)
    .scheme(Blur.SCHEME_NATIVE)
    .mode(Blur.MODE_STACK)
    .radius(10)
    .sampleFactor(2.0f)
    .forceCopy(false)
    .needUpscale(true)
    .asyncBlur(bitmap, new AsyncBlurTask.CallBack() {
        @Override
        public void onBlurSuccess(Bitmap outBitmap) {
        	// do something...
        }

        @Override
        public void onBlurFailed() {

        }
    });
f.cancel(false);    

```

### 3. Sample

#### Animation

<img src="doc/graphic/animation_blur_progress.gif" width = "370" height = "619" alt="动态模糊" />

#### Arbitrary Locaton Blur

<img src="doc/graphic/dynamic_blur.gif" width = "370" height = "600" alt="动态模糊" />



### 4. Dynamic background blur

Dynamic Blur provides real-time background blurring of View and ViewGroup, not based on Bitmap implementations. The component will blur the area where the View is located.  See the repository [HokoBlurDrawable](https://github.com/HokoFly/HokoBlurDrawable).

<img src="doc/graphic/blur_drawable.gif" width = "370" alt="动态模糊" />



### 5. Tips


1. When the Bitmap is not scaled (```sampleFactor(1.0f)```), the incoming Bitmap will be directly modified by the subsequent operations. So when the function returns a bitmap, it can be used immediately.

2. **It is strongly recommended to use the downScale operation before the blur operation to reduce the size of the blurred image, which will greatly improve the blur efficiency and effect.**

3. Please limit the blur radius to 25. Increasing the radius leads to much less  blur effect increase than by increasing the scale factor, and if the radius increase, blur efficiency will also decrease;

4. The RenderScript solution has to be verified for compatibility. If there are scenarios that require more computation and more complex blurring, the RenderScript scheme may be better.

5. Algorithm selection
	- If you have low effect requirements for blurring and want to blur the image faster, please choose Box algorithm.；
	- If you have a higher effect requirement for blurring and can tolerate slower blurring of the image, please choose the Gaussian algorithm;
	- The Stack algorithm has a blur effect that is very close to the Gaussian algorithm, and it improves the efficiency. Generally, the Stack algorithm is recommended;
	
6. BlurDrawable is implemented by OpenGL, so if the hardware acceleration is not enabled, the background blur will be invalid.

7. Sample and usage. Please see the sample project.
