package dev.jacktym.crashpatch.crashes;

import dev.jacktym.crashpatch.hooks.StacktraceDeobfuscator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.ArrayList;
import java.util.List;

public class DeobfuscatingRewritePolicy implements RewritePolicy {
    @Override
    public LogEvent rewrite(LogEvent source) {
        //if (CrashPatchConfig.deobfuscateCrashLog) {
            if (source.getThrown() != null) {
                StacktraceDeobfuscator.INSTANCE.deobfuscateThrowable(source.getThrown());
            }
        //}
        return source;
    }

    public static void install() {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        LoggerConfig loggerConfig = rootLogger.getContext().getConfiguration().getLoggerConfig(rootLogger.getName());

        // Remove appender refs from config
        List<AppenderRef> appenderRefs = new ArrayList<>(loggerConfig.getAppenderRefs());
        for (AppenderRef appenderRef : appenderRefs) {
            loggerConfig.removeAppender(appenderRef.getRef());
        }

        // Create the RewriteAppender, which wraps the appenders
        RewriteAppender rewriteAppender = RewriteAppender.createAppender(
                "CrashPatchDeobfuscatingAppender",
                "true",
                appenderRefs.toArray(new AppenderRef[0]),
                rootLogger.getContext().getConfiguration(),
                new DeobfuscatingRewritePolicy(),
                null
        );
        rewriteAppender.start();

        // Add the new appender
        loggerConfig.addAppender(rewriteAppender, null, null);
    }
}
