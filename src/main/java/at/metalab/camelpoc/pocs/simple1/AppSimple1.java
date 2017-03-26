package at.metalab.camelpoc.pocs.simple1;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import net.timewalker.ffmq3.FFMQConstants;

public class AppSimple1 {

	public static void main(String[] args) throws Exception {
		CamelContext context = new DefaultCamelContext();

		final String compTest = "test-jms";
		final String qTestFoo = compTest + ":queue:TESTFOO";
		final String qTestFoo2 = compTest + ":queue:TESTFOO2";
		final String fJmsTest = "file://./target/jmstest";
		final String fJmsTest2 = "file://./target/jmstest2";

		context.addRoutes(new RouteBuilder() {
			public void configure() {
				from(qTestFoo).to(qTestFoo2);
				from(qTestFoo2).to(fJmsTest);
				from(fJmsTest).to(fJmsTest2);
			}
		});

		ConnectionFactory connectionFactory = buildFFMQ3CF("test", "test");

		// Note we can explicit name the component
		context.addComponent(compTest, JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

		ProducerTemplate template = context.createProducerTemplate();

		context.start();

		for (int i = 0; i < 10; i++) {
			System.out.println("sending message #" + i);
			template.sendBody(qTestFoo, "Test Message: " + i);
		}

		Thread.sleep(5000 * 2);

		context.suspend();
		context.stop();
	}

	private static ConnectionFactory buildFFMQ3CF(String userName, String password) throws Exception {
		// Create and initialize a JNDI context
		Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, "tcp://localhost:10002");
		env.put("userName", userName);
		env.put("password", password);

		Context context = new InitialContext(env);

		// Lookup a connection factory in the context
		ConnectionFactory connFactory = (ConnectionFactory) context.lookup(FFMQConstants.JNDI_CONNECTION_FACTORY_NAME);

		return connFactory;
	}

}
