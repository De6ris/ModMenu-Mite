package io.github.prospector.modmenu.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtils {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static Logger getLogger() {
        return LogManager.getLogger(STACK_WALKER.getCallerClass());
    }
}
