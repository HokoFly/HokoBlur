
#Android动态模糊实现的研究
#####作者 橡皮

## I 模糊算法（todo）
- 基本概念
- Box Blur（均值模糊）
- Stack Blur
- Gaussian Blur（高斯模糊）

>模糊效果： Box < Stack < Gaussian 


## II 模糊的实现方案

- Java
- Native（C/C++）
- RenderScript
- OpenGL

## III 性能对比
### 模糊耗时
模糊1080×675图片10次耗时对比（并未做resize）,模糊核半径10

|   |Java|Native|RenderScript|OpenGL|
|---|:---:|:---:|:---:|:---:|
|Box Blur| 19982ms |12394ms|30ms|1ms
|Stack Blur|3972ms|3234ms|192ms|——|
|Gaussian Blur|22394ms|13942ms|213ms|1ms


- **这里OpenGL的耗时为调用onDrawFrame所需时间，并非实际GPU计算耗时**
- 测试机型：小米note

### 模糊结果示例
- Box Blur

![](https://raw.githubusercontent.com/yuxfzju/DynamicBlurDemo/master/doc/graphic/blurred_img_box.jpg)

- Stack Blur

![](https://raw.githubusercontent.com/yuxfzju/DynamicBlurDemo/master/doc/graphic/blurred_img_stack.jpg)

- Gaussian Blur

![](https://raw.githubusercontent.com/yuxfzju/DynamicBlurDemo/master/doc/graphic/blurred_img_gaussian.jpg)


模糊效果： Box < Stack < Gaussian 

### 对大尺寸图片的处理
即便是用RenderScript，当图片尺寸较大，且模糊核半径较大时，仍然会有性能上的问题。因此传入的Bitmap需要做Resize。

## IV OpenGL的实现

- 基于GLSurfaceView的实现（已完成简单的模糊）
- 基于TextureView的实现（已完成简单的模糊）

> TextureView更灵活一些，能处理View的位移、变换，GLSurfaceView不具备这些特性（todo 详细之后补上），如跟着手指拖动，GLSurfaceView就没法做到了。
> 
> 基本实现过程
> 根据Bitmap设置Texture --> 编写着色器（Vertex和Fragment）--> 设置MVP映射关系 --> 绘制


- 不同其他实现方式

  1. OpenGL的GLSL的语言特性，没法动态改变模糊核的半径。
  2. 对于OpenGL而言，不需要另写算法，只要改变模糊核即可切换为不同的模糊算法

## V 动态模糊

### RenderScript的实现方式
从上面的测试数据可以看出，Java和Native实现的方案模糊耗时明显要高于RenderScript方案。RenderScript的方案可以满足动态模糊的要求。下图是对应的demo，如果想要实现毛玻璃效果，不仅需要加大模糊半径，同时需要缩放图像至原图的1/10~1/5

<img src="https://raw.githubusercontent.com/yuxfzju/DynamicBlurDemo/master/doc/graphic/dynamic_blur.gif" width = "370" height = "600" alt="动态模糊" />

> *因截屏gif帧率不高，且已做缩放，看上去会有模糊和卡顿。*

### OpenGL的实现方式

需要解决以下难点：

1. 拖动过程中Bitmap的截取，需要截获View所处范围内的像素数据
2. 性能问题，虽然OpenGL运算效率较高，但是获得bitmap，模糊， 设置bitmap这些操作都是相对耗时的，动态性有待验证。（可能会比RenderScript慢。。）

### 毛玻璃效果的补充
毛玻璃效果实际上是对原图片的严重裂化，突出的就是朦胧感。因此实现毛玻璃效果时，一般会对原图做较大缩放因子的resize，这可进一步提高模糊的效率。除了上述的两种方式，Native方式实现的StackBlur也可以做到。

## VI 总结
- 完成Java、Native和RenderScript的BoxBlur、StackBlur和Gaussian Blur算法实现；
- 对不同算法、不同实现方案进行了测试；
- 完成OpenGL方式的BoxBlur简单实现；
- 实现RenderScript方案的动态模糊，有较好性能。

## VII TODO
- 被模糊的图像实际不需要太高的图像质量，可以适当降低。动态模糊中，对Bitmap适当缩放，进一步提高性能；（因为没做缩放，示例毛玻璃效果不明显）；
- 有时间会做StackBlur的毛玻璃实现，其实就是提高缩放因子，可以套用RenderScript实现动态模糊的方式；
- OpenGL实现方式中，将模糊核以动态的方式引入，实现动态模糊（拖动的方式，或者带模糊效果的Layout）。
- 完善文档，包括基本概念以及更多的验证结果和数据。