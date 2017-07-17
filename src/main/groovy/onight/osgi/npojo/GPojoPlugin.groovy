package onight.osgi.npojo

import org.gradle.api.Plugin
import org.gradle.api.Project

 
class GPojoPlugin implements Plugin<Project> {

	void apply(Project target) {
		target.dependencies()
		target.task('buildbundle',type:BundleTask)
		
		
		target.project.apply([ plugin: "maven"]);
		target.project.apply([ plugin: "osgi"]);
		target.project.apply([ plugin: 'java']);
		target.project.apply([ plugin: 'eclipse']);
		
		target.configurations {
			includeInJar
			deployerJars
		}

		target.project.sourceCompatibility = 1.7
		target.project.targetCompatibility = 1.7

		target.compileJava.options.encoding = 'UTF-8'
		
		target.dependencies {
			compile 'org.apache.felix:org.apache.felix.ipojo.manipulator:1.12.1'
			compile 'org.javassist:javassist:3.20.0-GA'
			compile   'org.apache.felix:org.apache.felix.ipojo.annotations:1.12.1'
			compile   'org.apache.felix:org.apache.felix.ipojo.api:1.12.1'
			compile   'org.apache.felix:org.apache.felix.framework:5.0.1'
			compile   'org.apache.felix:org.apache.felix.main:5.4.0'
			deployerJars "org.apache.maven.wagon:wagon-ssh:2.10"
			compile   'ch.qos.logback:logback-classic:1.1.3'

			target.configurations.compile.extendsFrom(target.configurations.includeInJar)
		}

		if(target.hasProperty('obr_host')){
			target.uploadArchives  {
				repositories {
					mavenDeployer {
						repository(url: target.obr_host) {
							authentication(userName:target.obr_usr,password:target.obr_pwd)
						}
					}
				}
			}
		}

		//		update.updateRepository();

		target.jar {
			into('lib') { from target.configurations.includeInJar }
			manifest{ instruction 'Export-Package','*' //attributes( 'Bundle-Activator': 'org.nights.stringio.Activator')
			}
		}

		target.jar.doLast{
			target.tasks.buildbundle.execute();
		}
	}

	public String getProp(String key,String defaultv){
		if(System.getProperty(key)!=null) {
			return System.getProperty(key);
		}
		if(System.getenv(key)!=null){
			return System.getenv(key);
		}
		return defaultv;
	}
}
