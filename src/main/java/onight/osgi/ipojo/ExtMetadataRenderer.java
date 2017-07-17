package onight.osgi.ipojo;

import java.util.HashSet;

import org.apache.felix.ipojo.manipulator.render.MetadataFilter;
import org.apache.felix.ipojo.manipulator.render.MetadataRenderer;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

public class ExtMetadataRenderer extends MetadataRenderer {

	
	HashSet<String> actorwareSet = new HashSet<String>();

	
	public void addPojoClass(String classname) {
		actorwareSet.add(classname);
	}

	@Override
	public void addMetadataFilter(MetadataFilter filter) {
		super.addMetadataFilter(filter);
	}

	@Override
	public String render(Element element) {
		// System.out.println("render==:"+element);
		if (element.getName().equals("component") && actorwareSet.contains(element.getAttribute("classname"))) {
			boolean founded = false;
			for (Element ele : element.getElements()) {
				if (ele.getName().equals("provides")) {
					founded = true;
					break;
				}
			}
			if (!founded) {
				Element ele = new Element("provides", "");
				ele.addAttribute(new Attribute(
						"specifications",
						"{onight.tfw.ojpa.api.IJPAClient,onight.tfw.proxy.IActor,onight.tfw.otransio.api.session.CMDService,onight.tfw.otransio.api.PSenderService,onight.tfw.orouter.api.IQClient,onight.tfw.ntrans.api.ActorService}"));
				System.out.println("add providers:"+element.getAttribute("classname"));
				element.addElement(ele);
			}

			// System.out.println("element:redner==:"+element);
			// System.out.println("element:redner.attrlen==:"+element.getAttributes().length);
			// for(Attribute attr:element.getAttributes()){
			// System.out.println("attr="+attr.getName()+",v="+attr.getValue()+"::"+attr);
			// }
			// element.addAttribute(new Attribute("", value));;
			// Element cb = new Element("callback", "");
			// cb.addAttribute(new Attribute("transition",
			// LifecycleVisitor.Transition.VALIDATE.name().toLowerCase()));
			// cb.addAttribute(new Attribute("method", "__pojoValidate"));
			// element.addElement(cb);
			//
			// cb = new Element("callback", "");
			// cb.addAttribute(new Attribute("transition",
			// LifecycleVisitor.Transition.INVALIDATE.name().toLowerCase()));
			// cb.addAttribute(new Attribute("method", "__pojoInvalidate"));
			// element.addElement(cb);

		}
		String result = super.render(element);
//		System.out.println("render result=" + result);
		return result;

	}

}
