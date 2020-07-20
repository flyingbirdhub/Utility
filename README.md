## 使用注解进行属性拷贝

### 背景

作为一名CRUD boy，非常厌烦了Ctrl+C & Ctrl+V的方式进行开发。尤其是在Web类项目开发中，我们经常会遇到PO(Persistent Object, 持久化对象)，DTO(Data Transfer Object, 数据传输对象)，VO(View Object, 视图对象)对象之间的转换，毕竟后端总不能把数据库原始数据直接丢给前端进行处理（估计会被🔪），返回一个合适的DTO给前端，是一个优秀CRUD工程师的自觉。

### 问题

通常我们从数据库查询某个PO对象后，经过一番加工，将其某些属性拷贝给DTO对象或者VO对象返回到前端进行进一步处理或者直接展示。所以代码出现最多的语句就是这几种对象之间各种属性的相互赋值：

* 属性较多，手写比较繁琐
* 增加冗余代码量，给后续维护带来不必要的工作量
* 由于赋值在代码中的位置比较分散，属性之间的对应关系难以进行维护
* 为了便于语义化理解，DTO、VO以及PO对象中属性的名称可能不同，也要能实现正确的拷贝
* 在属性值类型不同时，能够实现不同类型对象间的转换
* ...

### 已有方案

虽然已有对象拷贝的各种类库：```BeanUtils,BeanCopier```等，但是在使用上总不是那么顺手，我们需要一种非常轻量级的对象拷贝方法，于是就有了借助于注解和反射进行对象拷贝的方法。

### 注解方案

之所以采用注解方案有以下几点原因：

* 注解可以实现对源数据无侵入性，不会涉及源数据的改动
* 注解不会目标对象的原有逻辑
* 易扩展，可以通过不断增加解析类的功能来扩展对象拷贝功能

#### 特性

* 通过注解中的name字段，可以实现不同名称属性的拷贝
* 通过注解中的methodClass、methodName、parameters字段可以实现不同属性值之间的类型转换
* 注解作用与单个属性上，按需取用即可，默认相同属性名称自动进行拷贝

#### 注解代码

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AliasField {
    /**
     * 可供拷贝的属性的名称，以注解中注册的名称优先，寻找名称对应的getter函数
     * 第一个找到的getter函数即为源
     */
    String[] name() default {};

    /**
     * 将源值转换为目标值的方法所在的类，默认为目标对象
     */
    Class methodClass() default Class.class;

    /**
     * 将源值转换为目标值的方法名称
     */
    String methodName() default "";

    /**
     * 将源值转换为目标值的方法需要的参数
     */
    Class[] parameters() default {};
}
```

#### DEMO

##### 源对象

```java
public class UserTable {
    public UserTable(long id, String des, String userId){
        this.id = id;
        this.des = des;
        this.userId = userId;
    }

    private long id;

    private String des;

    private String userId;

    public long getId(){
        return id;
    }

    public String getDes(){
        return des;
    }

    public String getUserId(){
        return userId;
    }
}
```

##### 目标对象

```java
public class UserProtocol {
    private long id;

    public void setId(long id){
        this.id = id;
    }

    @AliasField(name={"des"})
    private String content;
    public void setContent(String content){
        this.content = content;
    }

    @AliasField(name={"userId"}, methodName = "String2Int", parameters = {String.class})
    private long selfId;
    public void setSelfId(long userId){
        this.selfId = userId;
    }

    public static int String2Int(String userId){
        return Integer.valueOf(userId);
    }

    @Override
    public boolean equals(Object object){
        if(object == null){
            return false;
        }
        else if(object == this){
            return true;
        }
        else if(object instanceof UserTable){
            UserTable target = (UserTable)object;
            return id == target.getId() && content.equals(target.getDes()) && selfId == Integer.valueOf(target.getUserId());
        }
        else if(object instanceof UserProtocol){
            UserProtocol target = (UserProtocol)object;
            return id == target.id && content.equals(target.content) && selfId == target.selfId;
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return id+"-"+content+"-"+selfId;
    }
}
```

##### 对象拷贝

通过一个函数简单调用即可实现对象拷贝

```java
userTable = new UserTable(2019, "I am bast!", "2020");
UserProtocol userProtocol = new UserProtocol();
Convert.convert(userProtocol, userTable);
```

### 更多使用方法请见代码中的单元测试案例

