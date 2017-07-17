package onight.osgi.npojo

import org.gradle.api.Plugin
import org.gradle.api.Project


class OFWActorPlugin implements Plugin<Project> {

	void apply(Project target) {
		target.project.apply([ plugin: "maven"]);
		target.project.apply([ plugin: "osgi"]);
		target.project.apply([ plugin: 'java']);
		target.project.apply([ plugin: 'eclipse']);
		target.project.apply([ plugin: 'scala']);
		target.repositories {
			maven { url "https://plugins.gradle.org/m2/" }
		}

		target.project.apply([ plugin: 'com.google.protobuf']);
		
		target.protobuf { protoc{ artifact = 'com.google.protobuf:protoc:3.0.0-alpha-3' } }

		target.sourceSets.main.java {
			srcDir 'build/generated/source/proto/main/java'
			srcDir 'src/main/proto'
		}
		
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
			compile 'com.google.protobuf:protobuf-java:3.0.0-alpha-3.1'
			includeInJar 'com.googlecode.protobuf-java-format:protobuf-java-format:1.4'
			compile 'org.scala-lang:scala-library:2.11.7'
			
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
