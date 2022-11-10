[toc]

## Rxjava 创建&发送事件的方式

### Create

```kotlin
// 1.创建被观察者对象
val observable = Observable.create(ObservableOnSubscribe<Int> {
    // 2.定义需要发送的事件
    it.onNext(1)
    it.onNext(2)
    it.onNext(3)
    it.onNext(4)
    it.onNext(5)
    it.onComplete()
}).subscribe(object : Observer<Int> {
    override fun onSubscribe(d: Disposable) {
        Log.d(TAG, "开始采用subscribe连接")
    }

    override fun onNext(t: Int) {
        Log.d(TAG, "接收到了事件 $t")
    }

    override fun onError(e: Throwable) {
        Log.d(TAG, "对error错误做处理")
    }

    override fun onComplete() {
        Log.d(TAG, "对complete事件做出响应")
    }
})
运行结果
D/MainActivity2: 开始采用subscribe连接
D/MainActivity2: 接收到了事件 1
D/MainActivity2: 接收到了事件 2
D/MainActivity2: 接收到了事件 3
D/MainActivity2: 接收到了事件 4
D/MainActivity2: 接收到了事件 5
D/MainActivity2: 对complete事件做出响应
```

### Just

> 快捷创建事件队列
>
> 最多只能发送10个事件

```kotlin
val observable = Observable.just(1,2,3,4)
    .subscribe(object : Observer<Int> {
    override fun onSubscribe(d: Disposable) {
        Log.d(TAG, "开始采用subscribe连接")
    }

    override fun onNext(t: Int) {
        Log.d(TAG, "接收到了事件 $t")
    }

    override fun onError(e: Throwable) {
        Log.d(TAG, "对error错误做处理")
    }

    override fun onComplete() {
        Log.d(TAG, "对complete事件做出响应")
    }
})
```

### fromArray

```kotlin
val items = arrayOf(0, 1, 2, 3, 4)
Observable.fromArray(*items).subscribe(object : Observer<Int> {
        override fun onSubscribe(d: Disposable) {
            Log.d(TAG, "数组遍历")
        }

        override fun onNext(value: Int) {
            Log.d(TAG, "数组中的元素 = $value")
        }

        override fun onError(e: Throwable) {
            Log.d(TAG, "对Error事件作出响应")
        }

        override fun onComplete() {
            Log.d(TAG, "遍历结束")
        }
    })
```

注意kotlin传入*items，表示将数组展开，等同于传入多个参数

### fromIterable

遍历集合发送事件

```kotlin
val list = arrayListOf(1, 2, 3, 4, 5, 6)
Observable.fromIterable(list).subscribe(object : Observer<Int> {
        override fun onSubscribe(d: Disposable) {
            Log.d(TAG, "数组遍历")
        }

        override fun onNext(value: Int) {
            Log.d(TAG, "数组中的元素 = $value")
        }

        override fun onError(e: Throwable) {
            Log.d(TAG, "对Error事件作出响应")
        }

        override fun onComplete() {
            Log.d(TAG, "遍历结束")
        }
    })
```

### defer()

直到有观察者(Observer)订阅时，才动态创建被观察者对象(Observable) & 发送事件

```kotlin
var i = 10
val observable = Observable.defer {
    Observable.just(i)
}
i = 15
observable.subscribe(object : Observer<Int> {
    override fun onSubscribe(d: Disposable) {
        Log.d(TAG, "开始订阅事件")
    }

    override fun onNext(t: Int) {
        Log.d(TAG, "收到事件 $t")
    }

    override fun onError(e: Throwable) {
        Log.d(TAG, "处理错误")
    }

    override fun onComplete() {
        Log.d(TAG, "流程结束")
    }
})

运行结果：
D/MainActivity2: 开始订阅事件
D/MainActivity2: 收到事件 15
D/MainActivity2: 流程结束
```

### timer()

延迟指定的时间后，发送1个数值0（Long类型）

本质 = 延迟指定时间后，调用一次 `onNext(0)`

一般用于检测

```kotlin
Observable.timer(2, TimeUnit.SECONDS).subscribe(object : Observer<Long> {
    override fun onSubscribe(d: Disposable) {
        Log.d(TAG, "开始订阅")
    }

    override fun onNext(t: Long) {
        Log.d(TAG, "收到事件 $t")
    }

    override fun onError(e: Throwable) {

    }

    override fun onComplete() {
        Log.d(TAG, "流程结束")
    }
})

D/MainActivity2: 开始订阅
D/MainActivity2: 收到事件 0
D/MainActivity2: 流程结束
```

### map

> map用于转换数据的类型，比如把string转为int

```kotlin
val observable = Observable.create(ObservableOnSubscribe<Int> {
            it.onNext(1)
            it.onNext(2)
        }).map {
            Log.d("rxjava", "map from int to string $it")
            it.toString()
        }
        val observer = object : Observer<String> {
            override fun onSubscribe(d: Disposable) {
                Log.d("rxjava", "onSubscribe")
            }

            override fun onNext(t: String) {
                Log.d("rxjava", "onNext $t")
            }

            override fun onError(e: Throwable) {
                Log.d("west", "onError $e")
            }

            override fun onComplete() {
                Log.d("west", "onComplete")
            }
        }
        observable.subscribe(observer)

D/rxjava: onSubscribe
D/rxjava: map from int to string 1
D/rxjava: onNext 1
D/rxjava: map from int to string 2
D/rxjava: onNext 2
```