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
 * 5.稳定的堆空间(-Xms和-Xmx一样)可以减少ＧＣ次数，因此很多服务端都会将最大堆和最小堆设置为相同的数值。但也增加了每次GC的时间。
 *   让堆在一个区间中震荡，在系统不需要使用大内存时，压缩堆空间，可加快单次GC速度，可通过以下JVM参数来解决。
 *   -XX:MinHeapFreeRatio设置堆空间最小空闲比例，默认为40，当堆空间的空闲比例小于这个值时，JVM会扩展堆空间。
 *   -XX:MaxHeapFreeRation 设置堆空间最大空闲比例，默认为70，当堆空间的空闲比例大于这个值时，JVM会压缩堆空间。
 *   -Xms和-Xmx一样则上面两个参数无效。
 * 6.设置以下参数，可在系统发生OOM错误时，JVM执行一段第三方脚本
 *   -XX:OnOutOfMemoryError=C:\XX.bat
 * 7.设置以下参数，可以禁用通过System.gc()触发Full GC
 *   -XX:+DisableExplicitGC
 * 8.对于应用程序而言，绝大部分情况下，是不需要进行类的回收的，因为回收类的性价比非常低。设置以下参数，可以禁用类的回收。
 *   -Xnoclassgc
 * 9.jdk的bin目录下的工具的实现来自lib目录的tools.jar，只是做了层包装
 *   例如，jps.exe是对sun.tools.jps.Jps的包装
 * 10.java自带工具
 *    10.1 jstat 观察程序运行时信息
 *             每秒统计一次共输出两次gc信息(25596为进程ID，1000为每1秒，2为共统计两次)
 *             jstat -gc -t 25596 1000 2
 *    10.2 jinfo 查看正在运行的java应用程序的扩展参数，支持在运行时修改部分参数
 *         查看某个JVM参数的值(例子为查看新生代对象晋升到老年代对象的年龄，25596为java进程ID)
 *         执行命令，jinfo -flag MaxTenuringThreshold 25596
 *         输出日志 -XX:MaxTenuringThreshold=15
 *
 *         查看是否打印ＧＣ详细信息
 *         执行命令，jinfo -flag PrintGCDetails 25596
 *         输出日志 -XX:+PrintGCDetails
 *
 *         修改参数，将PrintGCDetails关闭
 *         jinfo -flag -PrintGCDetails 20440
 *
 *    10.3 jamp 生成堆快照、对象统计信息
 *
 *         生成java进程的对象统计信息并输出到文件
 *         jmap -histo 20440 >C:\s.txt
 *
 *         生成堆快照信息并输出到文件
 *         jmap -dump:format=b,file=C:\heap.hprof 20440
 *
 *    10.4 jhat 分析堆快照内容，分析完成后会在7000端口启动一个http服务，在浏览器中输入 http://127.0.0.1:700查看分析结果
 *         jhat c:\heap.hprof
 *
 *    10.5 jstack 打印java进程中的线程堆栈信息(-l表示输出锁相关信息)
 *         jstack -l 20440
 *
 *    10.6 jstatd 是RMI服务端程序，相当于代理服务器，建立本地计算机与远程监控工具的通信
 *         配合jps、jstat(这两个命令本身支持对远程计算机的监控，为了启用远程监控，则需要配合jstatd使用)可分别显示远程计算机的java进程和运行时信息
 *
 *    10.7 hprof 用于监控java应用程序运行时CPU信息和堆信息(待做实验验证)
 *        使用参数-agentlib:hprof=cpu=times,interval=10运行程序，times选项会在java函数的调用前后记录执行时间，进而计算函数的执行时间
 *
 *    10.8 visualVM 强大的多合一故障诊断和性能监控的可视化工具，可以替代jstat、jamp、jhat、jstac甚至JConsole
 *
 */
public class JVMTest {

    /**
     * 方法区-常量池回收，只要常量池的对象没有被任何地方引用就可以被回收。
     * intern方法：如果常量池中有当前字符串，则返回池中的字符串对象，如果没有，则先将字符串加入常量池并返回对象引用
     * 设置JVM参数(方法区的初始大小、最大值)并启动程序 -XX:PermSize=2M -XX:MaxPermSize=4M -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
     * 会不断的GC，部分输出信息如下
     *     4.799: [GC (Allocation Failure) [PSYoungGen: 379382K->12316K(389632K)] 407489K->40431K(510976K), 0.1500295 secs] [Times: user=0.19 sys=0.05, real=0.15 secs]
     *     8.004: [GC (Allocation Failure) [PSYoungGen: 379932K->11833K(607232K)] 408047K->39955K(728576K), 0.8038558 secs] [Times: user=0.80 sys=0.00, real=0.80 secs]
     *     13.544: [GC (Allocation Failure) [PSYoungGen: 598073K->1088K(606208K)] 626195K->40682K(727552K), 0.6887064 secs] [Times: user=0.70 sys=0.06, real=0.69 secs]
     *     27.130: [GC (Allocation Failure) [PSYoungGen: 587328K->544K(788992K)] 626922K->40886K(910336K), 0.9561571 secs] [Times: user=0.95 sys=0.00, real=0.96 secs]
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


    /**
     * JIT即时编译器，可以在运行时，将字节码编译成本地代码，从而提高函数的执行效率。
     * 设置参数并启动程序 -XX:CompileThreshold=1500 -XX:+PrintCompilation -XX:+CITime
     *     第一个参数设置编译阀值，当函数调用次数超过这个值时，JIT就会将此函数的字节码编译成本地代码(但本机实验时，到256次就把test3函数编译成本地代码了)
     *     第二个参数设置打印编译信息（编译了哪些函数）
     *     第三参数设置打印编译耗时信息（时间相关信息）
     * 输出日志包括两部分，上部分是编译信息，下部分是耗时信息(以 "Accumulated compiler times (for compiled methods only)"为分界线)
     */
    long i = 0;
    public void test3(){
        i++;
    }

    public void test4(){
        for (int j = 0; j < 256; j++) {
            test3();
        }
    }


    /**
     * 堆快照
     * 设置参数并启动程序 -Xmx10M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\m.hprof
     * 以上参数表示在堆内存溢出时，保存堆快照文件到C:\m.hprof
     *
     * 可使用Java自动的jvisualvm.exe工具或JProfiler分析dump文件
     */
    public void test5(){
        for (int j = 0; j < 1000; j++) {
            byte[] b = new byte[1024*1024*2];
        }
    }
}
