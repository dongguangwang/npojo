package onight.osgi.ipojo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class ExtClassLoader extends ClassLoader {

	ClassLoader loader;
	// URLClassLoader urlLoader;

	File jarfile;

	public ExtClassLoader(ClassLoader loader, File file) {
		super();
		this.loader = loader;
		this.jarfile = file;
		System.out.println("FILE：" + file + "::" + file.exists());

	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		System.out.println("loadClass:" + name + "::");
		try {
			return super.loadClass(name);
		} catch (Exception e) {
			URLClassLoader urlLoader = null;
			System.out.println("loadClass:as url:" + name + "::jarfile="+jarfile);

			try {
				if (jarfile != null && jarfile.exists()) {
					urlLoader = new URLClassLoader(new URL[] { jarfile.toURI().toURL() },loader);
					System.out.println("urlLoader=" + urlLoader);

					if (urlLoader != null) {
						return urlLoader.loadClass(name);
					}
				}
			} catch (Throwable e1) {
				e1.printStackTrace();
				System.out.println("loadClass:as loader:" + name + "::"+loader+",,e1="+e1);
				try {
					return loader.loadClass(name);
				} catch (Exception e2) {
//					e2.printStackTrace();
					throw e2;
				}
			} finally {
				
				try {
					if (urlLoader != null)
						urlLoader.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			throw e;
		}

	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return super.loadClass(name, resolve);
	}

	public static void main(String[] args) {
		ExtClassLoader loader = new ExtClassLoader(ExtClassLoader.getSystemClassLoader(),new File("/Users/brew/Documents/KJ/MING/git/mgame/mfront/build/libs/mfront-1.0.0.jar"));
		try {
			Class clazz=loader.loadClass("onight.sm.Ssm$PBSSORet");
			System.out.println("clazz=="+clazz);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
