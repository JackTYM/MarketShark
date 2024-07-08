package dev.jacktym.crashpatch.crashes;

import java.util.List;

public class CrashScan {
    private final List<Solution> solutions;

    public CrashScan(List<Solution> solutions) {
        this.solutions = solutions;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public static class Solution {
        private final String name;
        private final List<String> solutions;
        private final boolean crashReport;

        public Solution(String name, List<String> solutions, boolean crashReport) {
            this.name = name;
            this.solutions = solutions;
            this.crashReport = crashReport;
        }

        public String getName() {
            return name;
        }

        public List<String> getSolutions() {
            return solutions;
        }

        public boolean isCrashReport() {
            return crashReport;
        }
    }
}
