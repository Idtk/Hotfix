package com.idtk.base;

/**
 * Created by Idtk on 2017/9/19.
 */

public class PatchClassInfo {
    private String patchedClassName;
    private String patchClassName;
    public PatchClassInfo(String patchedClassName, String patchClassName) {
        super();
        this.patchedClassName = patchedClassName;
        this.patchClassName = patchClassName;
    }

    public String getPatchedClassName() {
        return patchedClassName;
    }

    public String getPatchClassName() {
        return patchClassName;
    }

}
