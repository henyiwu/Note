## HAL

### HAL是什么

硬件抽象层在linux内核之上，向上提供统一的接口

![](https://s2.51cto.com/images/blog/202112/21154445_61c185eda534380952.png?x-oss-process=image/watermark,size_16,text_QDUxQ1RP5Y2a5a6i,color_FFFFFF,t_30,g_se,x_10,y_10,shadow_20,type_ZmFuZ3poZW5naGVpdGk=/format,webp/resize,m_fixed,w_1184)

- 为什么要搞这个东西？
  1. 谷歌搭建好hal的框架，为framework提供统一的接口，应用开发和驱动开发都不需要关心对方的细节，只需要针对hal提供的标准接口进行开发
  2. 从商业角度来说，硬件厂商需要为硬件编写驱动代码，如果驱动放在内核，必须遵循GPL协议开源。有了hal层，硬件厂商可以把核心功能放在hal层，hal层位于用户空间，不属于linux内核，和android源码一样遵循apache协议，可以选择不开源

### aosp中HAL的位置

- 目录

  /[hardware](http://aospxref.com/android-10.0.0_r47/xref/hardware/)/[libhardware](http://aospxref.com/android-10.0.0_r47/xref/hardware/libhardware/)/