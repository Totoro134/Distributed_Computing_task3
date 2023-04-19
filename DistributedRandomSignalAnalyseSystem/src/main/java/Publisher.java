import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.awt.*;
import java.util.*;

public class Publisher {
    private static String brokerURL = "tcp://localhost:61616";
    private static ConnectionFactory factory;
    private Connection connection;
    private MessageProducer producer;
    private Session session;
    private Destination destination;
    private Random random = new Random();

    public Publisher(String queuename) throws JMSException {
        factory = new ActiveMQConnectionFactory(brokerURL);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createQueue(queuename);
        producer = session.createProducer(null);
    }
    public void close() throws JMSException {
        if (connection != null) {
            connection.close();
        }
    }
    public void sendNumber() throws JMSException {
        double num = random.nextGaussian();
        Message message = session.createTextMessage(String.valueOf(num));
        producer.send(destination, message);
        System.out.println("生成信号值：" + num);
    }

    public void sendResult(int N, int cnt, double val, double mean, double sigma, double min, double max) throws JMSException {
        String msg = String.valueOf(N)+" "+String.valueOf(cnt)+" "+String.format("%.4f",val)+" "
                     +String.format("%.4f",mean)+" "+String.format("%.4f",sigma)+" "
                     +String.format("%.4f",min)+" "+String.format("%.4f",max);
        Message message = session.createTextMessage(msg);
        producer.send(destination, message);
    }

    public static void main(String[] args) throws JMSException, AWTException{
        Publisher publisher = new Publisher("Number");
        try {
            while (true) {
                publisher.sendNumber();
                Thread.sleep(100);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            publisher.close();
        }
    }
}