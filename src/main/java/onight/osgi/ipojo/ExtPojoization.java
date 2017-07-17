package onight.osgi.ipojo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;

import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import onight.osgi.annotation.NActorProvider;
import onight.osgi.annotation.iPojoBean;

import org.apache.felix.ipojo.manipulator.ManipulationVisitor;
import org.apache.felix.ipojo.manipulator.MetadataProvider;
import org.apache.felix.ipojo.manipulator.Pojoization;
import org.apache.felix.ipojo.manipulator.ResourceStore;
import org.apache.felix.ipojo.manipulator.ResourceVisitor;
import org.apache.felix.ipojo.manipulator.store.JarFileResourceStore;
import org.apache.felix.ipojo.manipulator.store.builder.DefaultManifestBuilder;

public class ExtPojoization extends Pojoization {

	@Override
	public void pojoization(final ResourceStore store, MetadataProvider metadata, ManipulationVisitor visitor, ClassLoader classLoader) {
		final JarFileResourceStore jfrs = (JarFileResourceStore) store;
		final ArrayList<String> jarlibList = new ArrayList<>();
		System.out.println("pojoization::"+store);
		
		try {
			jfrs.accept(new ResourceVisitor() {
				@Override
				public void visit(String name) {
					System.out.println("visit jar:"+name);
					if (name.endsWith(".jar")) {
						byte[] data = null;
						try {
							data = jfrs.read(name);
							ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(data));
							zin.close();
							jarlibList.add(name);
						} catch (Exception e) {
						}
						// ZipEntry zip=new ZipInputStream(in)
					}
				}
			});
		} catch (Throwable e1) {
			System.out.println("编译失败："+e1.getMessage());
			e1.printStackTrace();
			
		}

		DefaultManifestBuilder dmb = new ManifestBuilder(jarlibList);
		final ExtMetadataRenderer render = new ExtMetadataRenderer();
		dmb.setMetadataRenderer(render);
		jfrs.setManifestBuilder(dmb);
		final ExtJarFileRS rsproxy = new ExtJarFileRS(store);
		registClass(iPojoBean.class, rsproxy);
		registClass(NActorProvider.class, rsproxy);

		store.accept(new ResourceVisitor() {
			public void visit(String name) {
				System.out.println("readClass::" + name);
				if (name.endsWith(".class")) {
					byte[] data = null;
					ByteArrayInputStream bin = null;
					try {
						data = store.read(name);
						bin = new ByteArrayInputStream(data);

						ClassFile cf = new ClassFile(new DataInputStream(bin));
						boolean founded = false;
						for (Object attr : cf.getAttributes()) {
							if (attr instanceof AttributeInfo) {
								AttributeInfo attrcopy = (AttributeInfo) attr;
//								if (attrcopy.toString().equals("@" + iPojoBean.class.getName())) {
//									founded = true;
////									break;
//								}
								if (attrcopy.toString().equals("@" + NActorProvider.class.getName())) {
									founded = true;
//									break;
									render.addPojoClass(name.replace('/', '.').replaceAll(".class", ""));
									break;
								}
								
							}
						}
						if (!founded)
							return;
//						render.addPojoClass(name.replace('/', '.').replaceAll(".class", ""));

//						ConstPool cp = cf.getConstPool();
//
//						// cf.addMethod(genMethodInfo(cp, Validate.class));
//						// cf.addMethod(genMethodInfo(cp, Invalidate.class));
//
//						ByteArrayOutputStream bout = new ByteArrayOutputStream();
//						cf.write(new DataOutputStream(bout));
//
//						bout.flush();
//						bout.close();
//						byte bb[] = bout.toByteArray();
						// rsproxy.write(name, bb);

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (bin != null) {
							try {
								bin.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		super.pojoization(rsproxy, metadata, visitor, classLoader);
	}

	public void registClass(Class clazz, ExtJarFileRS store) {
		try {
//			System.out.println("registClass::" + clazz);
			String classAsPath = clazz.getName().replace('.', '/') + ".class";

			URL dirURL = clazz.getClassLoader().getResource(classAsPath);

			InputStream stream;
			JarFile jar = null;
			if (dirURL.getProtocol().equals("jar")) {
				/* A JAR path */
				String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
				jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
				JarEntry entry = jar.getJarEntry(classAsPath);
				stream = jar.getInputStream(entry);
			} else {
				stream = ClassLoader.getSystemClassLoader().getResourceAsStream(classAsPath);
			}
			ClassFile cfcopy = new ClassFile(new DataInputStream(stream));
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			cfcopy.write(new DataOutputStream(bout));
			bout.close();
			store.write(classAsPath, bout.toByteArray());
			if (jar != null) {
				stream.close();
				jar.close();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}
