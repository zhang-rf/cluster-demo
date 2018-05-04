package me.rfprojects.clusterdemo;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScheduledTask implements Runnable {

    private final Runnable runnable;
    private final Supplier<Integer> intervalSupplier;
    private final Consumer<Exception> exceptionHandler;
    private Thread thread;
    private volatile boolean canceled;

    public ScheduledTask(Runnable runnable, int interval) {
        this(runnable, () -> interval, null);
    }

    public ScheduledTask(Runnable runnable, Supplier<Integer> intervalSupplier) {
        this(runnable, intervalSupplier, null);
    }

    public ScheduledTask(Runnable runnable, Supplier<Integer> intervalSupplier, Consumer<Exception> exceptionHandler) {
        this.runnable = runnable;
        this.intervalSupplier = intervalSupplier;
        this.exceptionHandler = exceptionHandler;
        (thread = new Thread(this)).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(intervalSupplier.get());
                runnable.run();
            } catch (InterruptedException ignored) {
                if (canceled) {
                    return;
                }
                // Reset when interrupted
            } catch (Exception e) {
                if (exceptionHandler != null) {
                    exceptionHandler.accept(e);
                }
            }
        }
    }

    public void reset() {
        thread.interrupt();
    }

    public void cancel() {
        try {
            canceled = true;
            thread.interrupt();
            thread.join();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
