### kotlin 扩展函数

- kotlin扩展函数

  ```kotlin
  fun Int.orderRefundStatus() : String{
      return when(this) {
          1 -> "申请售后中"
          2 -> "部分退款成功"
          3 -> "退款成功"
          4 -> "退款失败"
          5 -> "退款异常"
          else -> ""
      }
  }
  ```

- 转为java

  ```java
  @NotNull
  public static final String orderRefundStatus(int $this$orderRefundStatus) {
      String string;
      switch ($this$orderRefundStatus) {
          case 1: {
              string = "申请售后中";
              break;
          }
          case 2: {
              string = "部分退款成功";
              break;
          }
          case 3: {
              string = "退款成功";
              break;
          }
          case 4: {
              string = "退款失败";
              break;
          }
          case 5: {
              string = "退款异常";
              break;
          }
          default: {
              string = "";
          }
      }
      return string;
  }
  ```

  转为java后发现，kotlin的扩展函数其实是一个静态函数