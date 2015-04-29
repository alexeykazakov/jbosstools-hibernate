package org.jboss.tools.hibernate.runtime.v_4_3.internal;

import org.hibernate.tuple.entity.EntityMetamodel;
import org.jboss.tools.hibernate.runtime.common.AbstractEntityMetamodelFacade;
import org.jboss.tools.hibernate.runtime.spi.IFacadeFactory;

public class EntityMetamodelFacadeImpl extends AbstractEntityMetamodelFacade {
	
	public EntityMetamodelFacadeImpl(
			IFacadeFactory facadeFactory, 
			EntityMetamodel emm) {
		super(facadeFactory, emm);
	}

	public EntityMetamodel getTarget() {
		return (EntityMetamodel)super.getTarget();
	}

	@Override
	public Object getTuplizerPropertyValue(Object entity, int i) {
		return getTarget().getTuplizer().getPropertyValue(entity, i);
	}

}