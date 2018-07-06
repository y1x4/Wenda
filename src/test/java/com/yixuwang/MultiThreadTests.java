package com.yixuwang;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一些关于 多线程 的例子：
 *
 * 1.thread & runnable
 * 2.Synchronized 内置锁
 * 3.BlockingQueue 阻塞队列
 * 4.TheadLocal 线程局部变量
 * 5.Executor 线程池或定时任务
 * 6.Atomicinteger 线程安全整数
 * 7.Future 线程间通信，数据传递 - 返回异步结果，阻塞等待返回结果，timeout，获取线程中的Exception
 *
 * Created by yixu on 2018/6/27.
 */
class MyThread extends Thread {

    private int tid;

    public MyThread(int tid) {
        this.tid = tid;
    }

    @Override
    //打印 1-10
    public void run() {
        try {
            for (int i = 0; i < 10; ++i) {
                Thread.sleep(0);
                System.out.println(String.format("%d:%d", tid, i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



class Consumer implements Runnable {

    private BlockingQueue<String> q;

    public Consumer(BlockingQueue<String> q) {
        this.q = q;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println(Thread.currentThread().getName() + ":" + q.take());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Producer implements Runnable {

    private BlockingQueue<String> q;

    public Producer(BlockingQueue<String> q) {
        this.q = q;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; ++i) {
                Thread.sleep(1000);
                q.put(String .valueOf(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




public class MultiThreadTests {

    public static void testThread() {
        for (int i = 0; i < 10; ++i) {
            new MyThread(i).start();
        }

        for (int i = 0; i < 10; ++i) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < 10; ++j) {
                            Thread.sleep(200);
                            System.out.println(String.format("T2 %d: %d:", finalI, j));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }



    private static Object obj = new Object();

    public static void testSynchronized1() {
        synchronized (obj) {
            try {
                for (int j = 0; j < 10; ++j) {
                    Thread.sleep(200);
                    System.out.println(String.format("T3 %d", j));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized2() {
        //相同锁可以锁住方法，依次执行
        //synchronized (obj) {
        synchronized (new Object()) {
            try {
                for (int j = 0; j < 10; ++j) {
                    Thread.sleep(200);
                    System.out.println(String.format("T4 %d", j));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized() {
        for (int i = 0; i < 2; ++i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    testSynchronized1();
                    testSynchronized2();
                }
            }).start();
        }
    }



    public static void testBlockingQueue() {
        BlockingQueue<String> q = new ArrayBlockingQueue<String>(10);
        new Thread(new Producer(q)).start();
        new Thread(new Consumer(q), "Consumer1").start();
        new Thread(new Consumer(q), "Consumer2").start();
    }



    private static ThreadLocal<Integer> threadLocalUserIds = new ThreadLocal<>();
    private static int userId;

    public static void testThreadLocal() {
        for (int i = 0; i < 10; ++i) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        threadLocalUserIds.set(finalI);
                        Thread.sleep(10);
                        System.out.println("ThreadLocal:" + threadLocalUserIds.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        System.out.println("-------------");

        for (int i = 0; i < 10; ++i) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        userId = finalI;
                        Thread.sleep(100);
                        System.out.println("UserId:" + userId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }




    public static void testExecutor() {
        //ExecutorService service = Executors.newSingleThreadExecutor();    //单线程执行
        ExecutorService service = Executors.newFixedThreadPool(1);

        for (int threadId = 0; threadId < 2; threadId++) {

            final int finalI = threadId;
            service.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10; ++i) {
                        try {
                            Thread.sleep(100);
                            System.out.println("Executor" + finalI + ": " + i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        service.shutdown();
        while (!service.isTerminated()) {
            //每隔1s查询是否完成 轮询
            try {
                Thread.sleep(1000);
                System.out.println("Wait for termination.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    private static int counter = 0;
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void testWithoutAtomic() {
        for (int i = 0; i < 10; ++i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        for (int j = 0; j < 10; ++j) {
                            counter++;
                            System.out.println(counter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void testWithAtomic() {
        //10条线程，加到100
        for (int i = 0; i < 10; ++i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        for (int j = 0; j < 10; ++j) {
                            System.out.println(atomicInteger.incrementAndGet());    //原子性操作
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static  void testAtomic() {
        testWithoutAtomic();  // <= 100
        //testWithAtomic();   // == 100
    }




    public static void testFuture() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(1000);
                //throw new IllegalArgumentException("异常");
                return 1;
            }
        });

        service.shutdown();
        try {
            System.out.println(future.get());
            //System.out.println(future.get(100, TimeUnit.MILLISECONDS));   //超过100ms未执行抛异常
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] argv) {
        //testThread();
        //testSynchronized();
        //testBlockingQueue();
        //testThreadLocal();
        //testExecutor();
        //testAtomic();
        //testFuture();
    }
}
