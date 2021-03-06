package org.jboss.tools.hibernate.runtime.v_3_5.internal;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.DefaultNamingStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.jboss.tools.hibernate.runtime.common.IFacade;
import org.jboss.tools.hibernate.runtime.common.IFacadeFactory;
import org.jboss.tools.hibernate.runtime.spi.IConfiguration;
import org.jboss.tools.hibernate.runtime.spi.IDialect;
import org.jboss.tools.hibernate.runtime.spi.IMapping;
import org.jboss.tools.hibernate.runtime.spi.IMappings;
import org.jboss.tools.hibernate.runtime.spi.INamingStrategy;
import org.jboss.tools.hibernate.runtime.spi.IPersistentClass;
import org.jboss.tools.hibernate.runtime.spi.IReverseEngineeringStrategy;
import org.jboss.tools.hibernate.runtime.spi.ISessionFactory;
import org.jboss.tools.hibernate.runtime.spi.ISettings;
import org.jboss.tools.hibernate.runtime.spi.ITable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigurationFacadeTest {
	
	private static final String FOO_HBM_XML_STRING =
			"<!DOCTYPE hibernate-mapping PUBLIC" +
			"		'-//Hibernate/Hibernate Mapping DTD 3.0//EN'" +
			"		'http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd'>" +
			"<hibernate-mapping package='org.jboss.tools.hibernate.runtime.v_3_5.internal'>" +
			"  <class name='ConfigurationFacadeTest$Foo'>" + 
			"    <id name='fooId'/>" +
			"  </class>" +
			"</hibernate-mapping>";
	
	private static final String BAR_HBM_XML_STRING =
			"<!DOCTYPE hibernate-mapping PUBLIC" +
			"		'-//Hibernate/Hibernate Mapping DTD 3.0//EN'" +
			"		'http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd'>" +
			"<hibernate-mapping package='org.jboss.tools.hibernate.runtime.v_3_5.internal'>" +
			"  <class name='ConfigurationFacadeTest$Bar'>" + 
			"    <id name='barId'/>" +
			"    <set name='fooSet' inverse='true'>" +
			"      <key column='fooId'/>" +
			"      <one-to-many class='ConfigurationFacadeTest$Foo'/>" +
			"    </set>" +
			"  </class>" +
			"</hibernate-mapping>";
	
	static class Foo {
		public String fooId;
	}
	
	static class Bar {
		public String barId;
		public Set<Foo> fooSet;
	}

	private static final IFacadeFactory FACADE_FACTORY = new FacadeFactoryImpl();

	private IConfiguration configurationFacade = null;
	private Configuration configuration = null;

	@Before
	public void setUp() {
		configuration = new Configuration();
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
	}
	
	@Test
	public void testGetProperty() {
		Assert.assertNull(configurationFacade.getProperty("foo"));
		configuration.setProperty("foo", "bar");
		Assert.assertEquals("bar", configurationFacade.getProperty("foo"));
	}
	
	@Test 
	public void testSetProperty() {
		Assert.assertNull(configuration.getProperty("foo"));
		configurationFacade.setProperty("foo", "bar");
		Assert.assertEquals("bar", configuration.getProperty("foo"));
	}

	@Test 
	public void testSetProperties() {
		Properties testProperties = new Properties();
		Assert.assertNotSame(testProperties, configuration.getProperties());
		Assert.assertSame(
				configurationFacade, 
				configurationFacade.setProperties(testProperties));
		Assert.assertSame(testProperties, configuration.getProperties());
	}
	
	@Test
	public void testAddFile() throws Exception {
		File fooFile = File.createTempFile("foo", "hbm.xml");
		PrintWriter printWriter = new PrintWriter(fooFile);
		printWriter.write(FOO_HBM_XML_STRING);
		printWriter.close();
		String fooClassName = 
				"org.jboss.tools.hibernate.runtime.v_3_5.internal.ConfigurationFacadeTest$Foo";
		Assert.assertNull(configuration.getClassMapping(fooClassName));
		Assert.assertSame(
				configurationFacade,
				configurationFacade.addFile(fooFile));
		Assert.assertNotNull(configuration.getClassMapping(fooClassName));
		Assert.assertTrue(fooFile.delete());
	}
	
	@Test
	public void testSetEntityResolver() {
		EntityResolver testResolver = new DefaultHandler();
		Assert.assertNotSame(testResolver, configuration.getEntityResolver());
		configurationFacade.setEntityResolver(testResolver);
		Assert.assertSame(testResolver, configuration.getEntityResolver());
	}
	
	@Test
	public void testGetEntityResolver() {
		EntityResolver testResolver = new DefaultHandler();
		Assert.assertNotSame(testResolver, configurationFacade.getEntityResolver());
		configuration.setEntityResolver(testResolver);
		Assert.assertSame(testResolver, configurationFacade.getEntityResolver());
	}
	
	@Test
	public void testSetNamingStrategy() {
		NamingStrategy dns = new DefaultNamingStrategy();
		INamingStrategy namingStrategy = FACADE_FACTORY.createNamingStrategy(dns);
		Assert.assertNotSame(dns, configuration.getNamingStrategy());
		configurationFacade.setNamingStrategy(namingStrategy);
		Assert.assertSame(dns, configuration.getNamingStrategy());
	}
	
	@Test
	public void testAddProperties() {
		Assert.assertNull(configuration.getProperty("foo"));
		Properties testProperties = new Properties();
		testProperties.put("foo", "bar");
		configurationFacade.addProperties(testProperties);
		Assert.assertEquals("bar", configuration.getProperty("foo"));
	}
	
	@Test
	public void testConfigure() {
		Assert.assertNull(configuration.getProperty(Environment.SESSION_FACTORY_NAME));
		configurationFacade.configure();
		Assert.assertEquals("bar", configuration.getProperty(Environment.SESSION_FACTORY_NAME));
	}
	
	@Test 
	public void testCreateMappings() {
		configuration.setProperty("createMappingsProperty", "a lot of blabla");
		IMappings mappingsFacade = configurationFacade.createMappings();
		Assert.assertNotNull(mappingsFacade);
		Object object = ((IFacade)mappingsFacade).getTarget();
		Assert.assertTrue(object instanceof Mappings);
		Mappings mappings = (Mappings)object;
		Assert.assertEquals(
				"a lot of blabla", 
				mappings.getConfigurationProperties().get("createMappingsProperty"));
	}
	
	@Test
	public void testBuildMappings() throws Exception {
		File fooFile = File.createTempFile("foo", "hbm.xml");
		PrintWriter fooWriter = new PrintWriter(fooFile);
		fooWriter.write(FOO_HBM_XML_STRING);
		fooWriter.close();
		configuration.addFile(fooFile);
		File barFile = File.createTempFile("bar", "hbm.xml");
		PrintWriter barWriter = new PrintWriter(barFile);
		barWriter.write(BAR_HBM_XML_STRING);
		barWriter.close();
		configuration.addFile(barFile);
		Collection collection = configuration.getCollectionMapping(
				"org.jboss.tools.hibernate.runtime.v_3_5.internal.ConfigurationFacadeTest$Bar.fooSet");
		OneToMany element = (OneToMany)collection.getElement();
		Assert.assertNull(element.getAssociatedClass());
		configurationFacade.buildMappings();
		Assert.assertEquals(
				"org.jboss.tools.hibernate.runtime.v_3_5.internal.ConfigurationFacadeTest$Foo",
				element.getAssociatedClass().getClassName());
	}
	
	@Test
	public void testBuildSessionFactory() throws Throwable {
		ISessionFactory sessionFactoryFacade = 
				configurationFacade.buildSessionFactory();
		Assert.assertNotNull(sessionFactoryFacade);
		Object sessionFactory = ((IFacade)sessionFactoryFacade).getTarget();
		Assert.assertNotNull(sessionFactory);
		Assert.assertTrue(sessionFactory instanceof SessionFactory);
	}
	
	@Test
	public void testBuildSettings() {
		ISettings settingsFacade = configurationFacade.buildSettings();
		Assert.assertNotNull(settingsFacade);
		Object settings = ((IFacade)settingsFacade).getTarget();
		Assert.assertNotNull(settings);
		Assert.assertTrue(settings instanceof Settings);
	}
	
	@Test
	public void testGetClassMappings() {
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
		Iterator<IPersistentClass> iterator = configurationFacade.getClassMappings();
		Assert.assertFalse(iterator.hasNext());
		configuration.configure();
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
		iterator = configurationFacade.getClassMappings();
		IPersistentClass persistentClassFacade = iterator.next();
		Assert.assertEquals(
				"org.jboss.tools.hibernate.runtime.v_3_5.internal.test.Foo",
				persistentClassFacade.getClassName());
	}
	
	@Test
	public void testSetPreferBasicCompositeIds() {
		JDBCMetaDataConfiguration configuration = new JDBCMetaDataConfiguration();
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
		// the default is false
		Assert.assertTrue(configuration.preferBasicCompositeIds());
		configurationFacade.setPreferBasicCompositeIds(false);
		Assert.assertFalse(configuration.preferBasicCompositeIds());
	}
	
	@Test
	public void testSetReverseEngineeringStrategy() {
		JDBCMetaDataConfiguration configuration = new JDBCMetaDataConfiguration();
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
		ReverseEngineeringStrategy reverseEngineeringStrategy = new DefaultReverseEngineeringStrategy();
		IReverseEngineeringStrategy strategyFacade = 
				FACADE_FACTORY.createReverseEngineeringStrategy(reverseEngineeringStrategy);
		Assert.assertNotSame(
				reverseEngineeringStrategy,
				configuration.getReverseEngineeringStrategy());
		configurationFacade.setReverseEngineeringStrategy(strategyFacade);
		Assert.assertSame(
				reverseEngineeringStrategy, 
				configuration.getReverseEngineeringStrategy());
	}
	
	@Test
	public void testReadFromJDBC() throws Exception {
		Connection connection = DriverManager.getConnection("jdbc:h2:mem:test");
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE FOO(id int primary key, bar varchar(255))");
		JDBCMetaDataConfiguration jdbcMdCfg = new JDBCMetaDataConfiguration();
		jdbcMdCfg.setProperty("hibernate.connection.url", "jdbc:h2:mem:test");
		configurationFacade = FACADE_FACTORY.createConfiguration(jdbcMdCfg);
		Iterator<?> iterator = jdbcMdCfg.getClassMappings();
		Assert.assertFalse(iterator.hasNext());		
		configurationFacade.readFromJDBC();
		iterator = jdbcMdCfg.getClassMappings();
		PersistentClass persistentClass = (PersistentClass)iterator.next();
		Assert.assertEquals("Foo", persistentClass.getClassName());
		statement.execute("DROP TABLE FOO");
		connection.close();
	}
	
	@Test
	public void testBuildMapping() {
		configuration.configure();
		IMapping mappingFacade = configurationFacade.buildMapping();
		Mapping mapping = (Mapping)((IFacade)mappingFacade).getTarget();
		Assert.assertEquals(
				"id", 
				mapping.getIdentifierPropertyName(
						"org.jboss.tools.hibernate.runtime.v_3_5.internal.test.Foo"));
	}
	
	@Test
	public void testGetClassMapping() {
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
		Assert.assertNull(configurationFacade.getClassMapping(
				"org.jboss.tools.hibernate.runtime.v_3_5.internal.test.Foo"));
		configuration.configure();
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
		Assert.assertNotNull(configurationFacade.getClassMapping(
				"org.jboss.tools.hibernate.runtime.v_3_5.internal.test.Foo"));
	}
	
	@Test
	public void testGetNamingStrategy() {
		NamingStrategy firstStrategy = new DefaultNamingStrategy();
		configuration.setNamingStrategy(firstStrategy);
		INamingStrategy firstStrategyFacade = configurationFacade.getNamingStrategy();
		Assert.assertSame(firstStrategy, ((IFacade)firstStrategyFacade).getTarget());
		NamingStrategy secondStrategy = new DefaultNamingStrategy();
		configuration.setNamingStrategy(secondStrategy);
		INamingStrategy secondStrategyFacade = configurationFacade.getNamingStrategy();
		Assert.assertNotSame(secondStrategy, ((IFacade)secondStrategyFacade).getTarget());
	}
	
	@Test
	public void testGetTableMappings() throws Exception {
		Connection connection = DriverManager.getConnection("jdbc:h2:mem:test");
		Statement statement = connection.createStatement();
		statement.execute("CREATE TABLE FOO(id int primary key, bar varchar(255))");
		JDBCMetaDataConfiguration jdbcMdCfg = new JDBCMetaDataConfiguration();
		jdbcMdCfg.setProperty("hibernate.connection.url", "jdbc:h2:mem:test");
		configurationFacade = FACADE_FACTORY.createConfiguration(jdbcMdCfg);
		Iterator<ITable> iterator = configurationFacade.getTableMappings();
		Assert.assertFalse(iterator.hasNext());
		jdbcMdCfg.readFromJDBC();
		configurationFacade = FACADE_FACTORY.createConfiguration(jdbcMdCfg);
		iterator = configurationFacade.getTableMappings();
		Table table = (Table)((IFacade)iterator.next()).getTarget();
		Assert.assertEquals("FOO", table.getName());
		statement.execute("DROP TABLE FOO");
		connection.close();
	}
	
	@Test
	public void testGetDialect() {
		configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		IDialect dialectFacade = configurationFacade.getDialect();
		Assert.assertNotNull(dialectFacade);
		Dialect dialect = (Dialect)((IFacade)dialectFacade).getTarget();
		Assert.assertEquals("org.hibernate.dialect.H2Dialect", dialect.getClass().getName());
	}
	
}
