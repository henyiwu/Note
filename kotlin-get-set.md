## get() set()

- get()

  ```kotlin
  var name: String = ""
          set(va) {
              field = if (va == "wzp") {
                  "wangzhiping"
              } else {
                  "nemo"
              }
          }
          get() {
              return "aaa"
          }
  ```

- Test1

  ```kotlin
  val person = Person()
  person.name = "wzp"
  Log.d("west" , "persion ${person.name}")
  
  // D/west: persion aaa
  // 重写了get返回aaa，结果总是aaa
  ```

- 去掉get()

  ```kotlin
  var name: String = ""
          set(va) {
              field = if (va == "wzp") {
                  "hllo"
              } else {
                  "dd"
              }
          }
  // 去掉重写get()
  ```

- test2

  ```
  D/west: persion hllo
  ```

- test3

  ```kotlin
  var name: String = ""
          set(va) {
              field = if (va == "wzp") {
                  "hllo"
              } else {
                  "dd"
              }
          }
  
  val person = Person()
  person.name = "wzpd"
  Log.d("west" , "persion ${person.name}")
  
  // D/west: persion dd
  ```

