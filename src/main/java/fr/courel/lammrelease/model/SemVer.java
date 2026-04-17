package fr.courel.lammrelease.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record SemVer(int major, int minor, int patch, String suffix) {

    private static final Pattern PATTERN =
            Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(-[A-Za-z0-9.-]+)?$");

    public static SemVer parse(String raw) {
        Matcher m = PATTERN.matcher(raw.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Version non SemVer : " + raw);
        }
        int major = Integer.parseInt(m.group(1));
        int minor = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
        int patch = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
        String suffix = m.group(4);
        return new SemVer(major, minor, patch, suffix);
    }

    public SemVer bumpMajor() { return new SemVer(major + 1, 0, 0, null); }
    public SemVer bumpMinor() { return new SemVer(major, minor + 1, 0, null); }
    public SemVer bumpPatch() { return new SemVer(major, minor, patch + 1, null); }

    public SemVer bump(Bump type) {
        return switch (type) {
            case MAJOR -> bumpMajor();
            case MINOR -> bumpMinor();
            case PATCH -> bumpPatch();
        };
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (suffix != null ? suffix : "");
    }

    public enum Bump { MAJOR, MINOR, PATCH }
}
