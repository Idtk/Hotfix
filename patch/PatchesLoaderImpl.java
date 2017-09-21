package com.idtk.hotfix;

import com.idtk.base.IPatchesLoader;
import com.idtk.base.PatchClassInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Idtk on 2017/9/19.
 */

public class PatchesLoaderImpl implements IPatchesLoader {
    @Override
    public List<PatchClassInfo> getPatchedClasses() {
        List<PatchClassInfo> patchClassInfoList = new ArrayList<>();
        patchClassInfoList.add(new PatchClassInfo("com.idtk.hotfix.MainActivity","com.idtk.hotfix.MainActivityPatch"));
        return patchClassInfoList;
    }
}
