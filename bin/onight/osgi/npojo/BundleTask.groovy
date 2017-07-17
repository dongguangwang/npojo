package onight.osgi.npojo

import onight.osgi.ipojo.ExtClassLoader;
import onight.osgi.ipojo.ExtPojoization

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction

class BundleTask extends DefaultTask {
	@TaskAction
	def buildbundle() {
		println "running iPojoTask:@"+project.group+"."+project.jar.archivePath
		;
//		org.apache.felix.ipojo.manipulator.Pojoization pojo = new org.apache.felix.ipojo.manipulator.Pojoization()
		org.apache.felix.ipojo.manipulator.Pojoization pojo = new ExtPojoization()
		File jarfile = project.file(project.jar.archivePath)
		File targetJarFile = project.file(project.jar.destinationDir.absolutePath +"/" +project.group+"."+ project.jar.baseName +"-"+project.version+ ".jar")

		if (!jarfile.exists()) throw new InvalidUserDataException("The specified bundle file does not exist: " + jarfile.absolutePath)

		if(targetJarFile.exists()) targetJarFile.delete()
		
		println "target="+targetJarFile.absolutePath
		
		pojo.pojoization(jarfile, targetJarFile,(File)null,new ExtClassLoader(project.getBuildscript().classLoader,jarfile));
		
//		pojo.pojoization(jarfile, targetJarFile,(File) null);//project.file(project.projectDir.absolutePath+"/src/main/resources/metadata.xml"))

//		pojo.getWarnings().each { s -> println s }
		//
		project.jar.baseName=project.group+"."+ project.jar.baseName
		
		if (!jarfile.delete()) {
//			if ( !targetJarFile.renameTo(project.file(project.group+"."+project.jar.archivePath)) ) {
//				throw new InvalidUserDataException("Cannot rename the manipulated jar file");
//			}
			throw new InvalidUserDataException("Cannot delete the input jar file")
		}  
	}
}
