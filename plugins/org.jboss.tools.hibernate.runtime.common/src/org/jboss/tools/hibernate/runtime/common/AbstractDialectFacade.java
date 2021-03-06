package org.jboss.tools.hibernate.runtime.common;

import org.jboss.tools.hibernate.runtime.spi.IDialect;

public abstract class AbstractDialectFacade extends AbstractFacade implements IDialect {

	public AbstractDialectFacade(IFacadeFactory facadeFactory, Object target) {
		super(facadeFactory, target);
	}

	@Override
	public String toString() {
		return getTarget().toString();
	}

	@Override
	public char openQuote() {
		return (char)Util.invokeMethod(
				getTarget(), 
				"openQuote", 
				new Class[] {}, 
				new Object[] {});
	}

	@Override
	public char closeQuote() {
		return (char)Util.invokeMethod(
				getTarget(), 
				"closeQuote", 
				new Class[] {}, 
				new Object[] {});
	}

}
