package com.example.demo.jvm;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * JVM调优
 * 1.方法区的大小决定了系统要以支持多少类定义和多少常量
 * 2.通过Full GC的日志，可知Full GC会回收年轻代、老年代、方法区
 * 3.因为堆内存也是向操作系统申请的，如果堆内存空间过大，会导致操作系统可用于线程栈的内存减少。
 *   当操作系统内存减去堆内存后，剩余的系统空间不足而无法创建新的线程时(栈空间不足导致内存溢出)，会报java.lang.OutOfMemoryError:unable to create new native thread。可适量减少堆内存空间来缓解此问题。
 * 4.GC策略评价指标
 *   4.1 吞吐量：应用程序生命周期内，应用程序耗时与系统总运行时间的比值
 *   4.2 停顿时间：垃圾回收器运行时，应用程序暂停时间
 */
public class JVMTest {

    /**
     * 方法区-常量池回收，只要常量池的对象没有被任何地方引用就可以被回收。
     * intern方法：如果常量池中有当前字符串，则返回池中的字符串对象，如果没有，则先将字符串加入常量池并返回对象引用
     * 设置JVM参数(方法区的初始大小、最大值)并启动程序 -XX:PermSize=2M -XX:MaxPermSize=4M -XX:+PrintGCDetails
     * 会不断的GC，部分输出信息如下
     *     [GC (Allocation Failure) [PSYoungGen: 379382K->12316K(389632K)] 407489K->40431K(510976K), 0.1500295 secs] [Times: user=0.19 sys=0.05, real=0.15 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 379932K->11833K(607232K)] 408047K->39955K(728576K), 0.8038558 secs] [Times: user=0.80 sys=0.00, real=0.80 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 598073K->1088K(606208K)] 626195K->40682K(727552K), 0.6887064 secs] [Times: user=0.70 sys=0.06, real=0.69 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 587328K->544K(788992K)] 626922K->40886K(910336K), 0.9561571 secs] [Times: user=0.95 sys=0.00, real=0.96 secs]
     *  运行了好久，每当常量池饱和时，都会触发GC顺利回收常量池中的数据
     */
    public void test(){
        for (int i = 0; i <Integer.MAX_VALUE ; i++) {
            String s = String.valueOf(i).intern();
        }
    }


    /**
     * 方法区-类元数据回收，JVM确认该类的所有实例被回收，并且加载该类的CLassLoader被回收，则ＧＣ就有可能回收该类型。
     * 设置JVM参数(方法区的初始大小、最大值)并启动程序 -XX:PermSize=2M -XX:MaxPermSize=4M -XX:+PrintGCDetails
     *
     *     运行了好久，一直在GC、Full GC,一段时间后，全都是Full GC部分输出信息如下
     *     [GC (Metadata GC Threshold) [PSYoungGen: 164712K->15339K(257536K)] 178442K->55007K(389120K), 0.0209825 secs] [Times: user=0.14 sys=0.03, real=0.02 secs]
     *     [Full GC (Metadata GC Threshold) [PSYoungGen: 15339K->0K(257536K)] [ParOldGen: 39667K->48661K(219136K)] 55007K->48661K(476672K), [Metaspace: 56687K->56666K(1097728K)], 0.1090824 secs] [Times: user=0.80 sys=0.00, real=0.11 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 242176K->28640K(270848K)] 290837K->156813K(489984K), 0.0542076 secs] [Times: user=0.27 sys=0.16, real=0.05 secs]
     *     [GC (Metadata GC Threshold) [PSYoungGen: 57592K->40032K(377856K)] 185765K->168205K(596992K), 0.0211297 secs] [Times: user=0.05 sys=0.11, real=0.02 secs]
     *     [Full GC (Metadata GC Threshold) [PSYoungGen: 40032K->0K(377856K)] [ParOldGen: 128173K->160800K(409088K)] 168205K->160800K(786944K), [Metaspace: 96009K->96009K(1118208K)], 0.2004081 secs] [Times: user=1.56 sys=0.05, real=0.20 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 312320K->67552K(379904K)] 473120K->299464K(788992K), 0.0772101 secs] [Times: user=0.47 sys=0.17, real=0.08 secs]
     *     [GC (Metadata GC Threshold) [PSYoungGen: 203454K->58592K(504832K)] 435366K->360176K(913920K), 0.0782481 secs] [Times: user=0.26 sys=0.17, real=0.08 secs]
     *     [Full GC (Metadata GC Threshold) [PSYoungGen: 58592K->0K(504832K)] [ParOldGen: 301584K->354001K(685568K)] 360176K->354001K(1190400K), [Metaspace: 161726K->161726K(1150976K)], 0.5086058 secs] [Times: user=3.99 sys=0.06, real=0.51 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 399360K->106464K(505856K)] 753361K->532249K(1191424K), 0.1427649 secs] [Times: user=0.39 sys=0.58, real=0.14 secs]
     *     [GC (Metadata GC Threshold) [PSYoungGen: 452080K->148704K(620544K)] 877865K->684417K(1306112K), 0.1933687 secs] [Times: user=0.61 sys=0.92, real=0.19 secs]
     *     [Full GC (Metadata GC Threshold) [PSYoungGen: 148704K->0K(620544K)] [ParOldGen: 535713K->676542K(1108480K)] 684417K->676542K(1729024K), [Metaspace: 271107K->271107K(1204224K)], 0.7096016 secs] [Times: user=5.52 sys=0.09, real=0.71 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 468992K->177152K(646144K)] 1145534K->882838K(1754624K), 0.1534850 secs] [Times: user=0.62 sys=0.37, real=0.15 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 646144K->193600K(758272K)] 1351830K->1081934K(1866752K), 0.2982821 secs] [Times: user=0.83 sys=1.62, real=0.30 secs]
     *     [GC (Metadata GC Threshold) [PSYoungGen: 526031K->248288K(787456K)] 1414366K->1228230K(1895936K), 0.1764769 secs] [Times: user=1.01 sys=0.30, real=0.18 secs]
     *     [Full GC (Metadata GC Threshold) [PSYoungGen: 248288K->106751K(787456K)] [ParOldGen: 979942K->1108280K(1651712K)] 1228230K->1215032K(2439168K), [Metaspace: 453660K->453660K(1294336K)], 1.6319235 secs] [Times: user=13.20 sys=0.11, real=1.63 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 645887K->226272K(908288K)] 1754168K->1442384K(2560000K), 0.3285085 secs] [Times: user=1.33 sys=1.30, real=0.33 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 809440K->346112K(929280K)] 2025552K->1693288K(2580992K), 0.2754484 secs] [Times: user=1.67 sys=0.50, real=0.28 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 929280K->254656K(921088K)] 2276456K->1957680K(2624512K), 0.4648454 secs] [Times: user=2.28 sys=1.58, real=0.46 secs]
     *     [Full GC (Ergonomics) [PSYoungGen: 254656K->236107K(921088K)] [ParOldGen: 1703024K->1703220K(2453504K)] 1957680K->1939327K(3374592K), [Metaspace: 699288K->699288K(1415168K)], 2.7940272 secs] [Times: user=23.12 sys=0.13, real=2.79 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 703051K->440992K(908288K)] 2406271K->2144212K(3361792K), 0.3833541 secs] [Times: user=2.75 sys=0.00, real=0.38 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 907936K->201696K(924672K)] 2611156K->2348836K(3378176K), 0.4270154 secs] [Times: user=1.81 sys=1.83, real=0.43 secs]
     *     [Full GC (Ergonomics) [PSYoungGen: 201696K->0K(924672K)] [ParOldGen: 2147140K->2341006K(2772992K)] 2348836K->2341006K(3697664K), [Metaspace: 835271K->835271K(1482752K)], 2.1792781 secs] [Times: user=16.88 sys=0.19, real=2.18 secs]
     *     [GC (Allocation Failure) [PSYoungGen: 462848K->199104K(924672K)] 2803854K->2540118K(3697664K), 0.2069397 secs] [Times: user=1.22 sys=0.00, real=0.21 secs]
     *
     *     [Full GC (Ergonomics) [PSYoungGen: 462848K->155692K(886784K)] [ParOldGen: 2735848K->2772943K(2772992K)] 3198696K->2928635K(3659776K), [Metaspace: 1035410K->1035410K(1581056K)], 4.4878196 secs] [Times: user=36.38 sys=0.27, real=4.49 secs]
     *     [Full GC (Ergonomics) [PSYoungGen: 462848K->283552K(886784K)] [ParOldGen: 2772943K->2772494K(2772992K)] 3235791K->3056047K(3659776K), [Metaspace: 1078755K->1078755K(1601536K)], 4.5135068 secs] [Times: user=34.20 sys=2.84, real=4.51 secs]
     *     [Full GC (Ergonomics) [PSYoungGen: 462848K->348829K(886784K)] [ParOldGen: 2772494K->2772989K(2772992K)] 3235342K->3121819K(3659776K), [Metaspace: 1102705K->1102705K(1613824K)], 5.1225725 secs] [Times: user=42.78 sys=0.28, real=5.12 secs]
     *     [Full GC (Ergonomics) [PSYoungGen: 462848K->389459K(886784K)] [ParOldGen: 2772989K->2772975K(2772992K)] 3235837K->3162434K(3659776K), [Metaspace: 1116485K->1116485K(1622016K)], 3.3657896 secs] [Times: user=27.64 sys=0.09, real=3.37 secs]
     *     [Full GC (Ergonomics) [PSYoungGen: 462848K->357876K(886784K)] [ParOldGen: 2772975K->2772730K(2772992K)] 3235823K->3130607K(3659776K), [Metaspace: 1123862K->1123732K(1626112K)], 3.6131541 secs] [Times: user=25.90 sys=0.03, real=3.61 secs]
     */
    public void test2(){
        try {
            for (int i = 0; i <Integer.MAX_VALUE ; i++) {
                // 定义类名
                CtClass ct = ClassPool.getDefault().makeClass("ClassName" + i);

                // 设置生成类的父类
                ct.setSuperclass(ClassPool.getDefault().get("com.example.demo.jvm.JavaBeanObject"));
                Class clz = ct.toClass();
                // 生成新实例，下一循环开始时，上一循环生成的实例就成为了垃圾
                JavaBeanObject javaBeanObject = (JavaBeanObject)clz.newInstance();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
