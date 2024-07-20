package dev.jacktym.crashpatch.crashes;

import cc.polyfrost.oneconfig.utils.NetworkUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jacktym.crashpatch.CrashPatch;
import dev.jacktym.marketshark.util.BugLogger;
import net.minecraft.util.EnumChatFormatting;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CrashHelper {

    private static JsonObject skyclientJson = null;
    public static final Map<String, CrashScan> simpleCache = new HashMap<>();

    public static boolean loadJson() {
        try {
            skyclientJson = NetworkUtils.getJsonElement("https://raw.githubusercontent.com/SkyblockClient/CrashData/main/crashes.json").getAsJsonObject();
            return true;
        } catch (Exception e) {
            BugLogger.logError(e);
            return false;
        }
    }

    public static CrashScan scanReport(String report, boolean serverCrash) {
        try {
            if (simpleCache.containsKey(report)) {
                return simpleCache.get(report);
            }
            Map<String, List<String>> responses = getResponses(report, serverCrash);
            List<CrashScan.Solution> solutions = responses.entrySet().stream()
                    .sorted((entry1, entry2) -> {
                        if ("Crash log".equals(entry1.getKey()) || "Disconnect reason".equals(entry1.getKey())) {
                            return 1;
                        }
                        if ("Crash log".equals(entry2.getKey()) || "Disconnect reason".equals(entry2.getKey())) {
                            return -1;
                        }
                        return entry1.getKey().compareTo(entry2.getKey());
                    })
                    .map(entry -> new CrashScan.Solution(
                            entry.getKey() + " (" + entry.getValue().size() + ")",
                            entry.getValue().stream()
                                    .map(value -> value.replace("%pathindicator%", "")
                                            .replace("%gameroot%", CrashPatch.gameDir.getAbsolutePath().replaceAll(File.separator + "$", ""))
                                            .replace("%profileroot%", new File(CrashPatch.mcDir, "OneConfig").getParentFile().getAbsolutePath().replaceAll(File.separator + "$", "")))
                                    .collect(Collectors.toList()), true))
                    .collect(Collectors.toList());

            CrashScan crashScan = new CrashScan(solutions);
            simpleCache.put(report, crashScan);
            return crashScan;
        } catch (Throwable e) {
            BugLogger.logError(e);
            return null;
        }
    }

    private static Map<String, List<String>> getResponses(String report, boolean serverCrash) {
        if (skyclientJson == null) {
            return new LinkedHashMap<>();
        }
        Map<String, List<String>> responses = new LinkedHashMap<>();
        List<Integer> triggersToIgnore = new ArrayList<>();

        JsonArray fixTypes = skyclientJson.getAsJsonArray("fixtypes");
        for (int i = 0; i < fixTypes.size(); i++) {
            JsonObject type = fixTypes.get(i).getAsJsonObject();
            boolean ignore = type.has("no_ingame_display") && type.get("no_ingame_display").getAsBoolean()
                    || !type.has("server_crashes") && serverCrash
                    || type.has("server_crashes") && !type.get("server_crashes").getAsBoolean() && !serverCrash;

            if (ignore) {
                triggersToIgnore.add(i);
            } else {
                responses.put(type.get("name").getAsString(), new ArrayList<>());
            }
        }

        JsonArray fixes = skyclientJson.getAsJsonArray("fixes");
        List<String> responseCategories = new ArrayList<>(responses.keySet());

        for (JsonElement solution : fixes) {
            JsonObject solutionJson = solution.getAsJsonObject();
            if (solutionJson.has("bot_only")) continue;
            int triggerNumber = solutionJson.has("fixtype") ? solutionJson.get("fixtype").getAsInt() : skyclientJson.get("default_fix_type").getAsInt();
            if (triggersToIgnore.contains(triggerNumber)) continue;

            JsonArray causes = solutionJson.getAsJsonArray("causes");
            boolean trigger = false;
            for (JsonElement cause : causes) {
                JsonObject causeJson = cause.getAsJsonObject();
                String theReport = report;
                if (causeJson.has("unformatted") && causeJson.get("unformatted").getAsBoolean()) {
                    theReport = EnumChatFormatting.getTextWithoutFormattingCodes(theReport);
                }

                switch (causeJson.get("method").getAsString()) {
                    case "contains":
                        trigger = theReport.contains(causeJson.get("value").getAsString());
                        break;
                    case "contains_not":
                        trigger = !theReport.contains(causeJson.get("value").getAsString());
                        break;
                    case "regex":
                        trigger = Pattern.compile(causeJson.get("value").getAsString(), Pattern.CASE_INSENSITIVE)
                                .matcher(theReport)
                                .find();
                        break;
                    case "regex_not":
                        trigger = !Pattern.compile(causeJson.get("value").getAsString(), Pattern.CASE_INSENSITIVE)
                                .matcher(theReport)
                                .find();
                        break;
                }

                if (!trigger) {
                    break;
                }
            }

            if (trigger) {
                responses.get(responseCategories.get(triggerNumber)).add(solutionJson.get("fix").getAsString());
            }
        }

        return responses.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

