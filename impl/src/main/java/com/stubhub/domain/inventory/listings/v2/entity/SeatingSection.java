package com.stubhub.domain.inventory.listings.v2.entity;

import java.util.Objects;

public class SeatingSection {
    private final String name;
    private final boolean generalAdmission;

    public final static SeatingSection DEFAULT = new SeatingSection("General Admission", true);

    public SeatingSection(String name, boolean generalAdmission) {
        this.name = name;
        this.generalAdmission = generalAdmission;
    }

    public String getName() {
        return name;
    }

    public boolean getGeneralAdmission() {
        return generalAdmission;
    }

    @Override
    public String toString() {
        return "SeatingSection {" +
                "name='" + name + '\'' +
                ", generalAdmission=" + generalAdmission +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeatingSection that = (SeatingSection) o;
        return generalAdmission == that.generalAdmission && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, generalAdmission);
    }
}
