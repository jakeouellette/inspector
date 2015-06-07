package com.jakeout.gradle.utils.gtree


public interface HasDiff {

    public enum Diff {
        ADDED("+"), REMOVED("-"), CONTENTS_CHANGED("~"), UNCHANGED(" ")

        String symbol

        public Diff(String symbol) {
            this.symbol = symbol
        }

        @Override
        public String toString() {
            symbol
        }
    }

    public Diff getType()
}