package com.idtk.fix

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger


public class FixPatchTransform extends Transform implements Plugin<Project>{

    Logger logger

    private static final String packageName = "com\\idtk\\hotfix"

    @Override
    void apply(Project project) {
        logger = project.logger
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(this)
    }

    @Override
    String getName() {
        return "fix"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        println '==================================fix patch start=================================='

        transformInvocation.inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (directoryInput.file.isDirectory()){
                    directoryInput.file.eachFileRecurse { File file ->
                        if (insertClass(file.name)){
                            String filepath = file.absolutePath
                            int start = filepath.indexOf(packageName)
                            int end = filepath.length() - 6
                            if (start != -1){
//                                String className = filepath.substring(start,end).replace('\\','.').replace('/','.')
                                String className = filepath.substring(start,end).replace('\\','/')
                                logger.quiet "name: ${className}: ${file.name}"
                                logger.quiet "namePath: ${filepath}"
                                ASMInsert insert = new ASMInsert(file)
                                byte[] code = insert.transformCode(className,file.bytes)
                                String newName = file.parentFile.absolutePath + File.separator + file.name
                                logger.quiet "newName: ${newName}"
                                FileOutputStream fos = new FileOutputStream(
                                        file.parentFile.absolutePath + File.separator + file.name);
                                fos.write(code);
                                fos.close();
                            }
                        }
                    }
                }
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes,directoryInput.scopes,Format.DIRECTORY)
                logger.quiet "output: ${dest}"
                FileUtils.copyDirectory(directoryInput.file,dest)
            }
            input.jarInputs.each { JarInput jarInput ->
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() -4)
                }

                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                jarInput.contentTypes, jarInput.scopes, Format.JAR)

                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        println '==================================fix patch end=================================='
    }

    boolean insertClass(String name){
        //className.contains("com/idtk/hotfix/")
        return name.endsWith(".class") && !name.startsWith("R\$") && !"R.class".equals(name) && !"BuildConfig.class".equals(name) && name.contains("MainActivity.class")
    }
}