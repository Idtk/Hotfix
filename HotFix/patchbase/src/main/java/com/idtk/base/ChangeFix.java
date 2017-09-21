package com.idtk.base;

public interface ChangeFix {
//    Object accessDispatch(Object current,String methodName,Object[] paramsArray);
    Object accessDispatch(boolean isStatic,String nameDesc,Object[] paramsArray);
//    Object accessDispatch(Object current,String methodName);
//    Object accessDispatch(Object[] paramsArray);
    // 用于判断这个方法是否存在于补丁中
    boolean isSupport(String nameDesc);
}
